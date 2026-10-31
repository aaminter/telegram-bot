package pro.sky.telegrambot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.entity.NotificationTask;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class NotificationTaskRepositoryIntegrationTest {

    @Autowired
    private NotificationTaskRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldFindOverduePendingTasks() {
        LocalDateTime now = LocalDateTime.now();
        NotificationTask task = repository.saveAndFlush(
                new NotificationTask(10001L, "overdue", now.minusHours(2))
        );

        var dueTasks = repository.findByStatusAndSentFalseAndDateTimeLessThanEqual(
                "PENDING",
                now
        );

        assertTrue(dueTasks.stream().anyMatch(item -> item.getId().equals(task.getId())));
    }

    @Test
    void shouldAllowOnlyOneWorkerToClaimTask() {
        LocalDateTime now = LocalDateTime.now();
        NotificationTask task = repository.saveAndFlush(
                new NotificationTask(10002L, "claim once", now.minusMinutes(1))
        );

        int firstClaim = repository.claimTask(task.getId(), now);
        int secondClaim = repository.claimTask(task.getId(), now);

        assertEquals(1, firstClaim);
        assertEquals(0, secondClaim);

        entityManager.clear();
        NotificationTask stored = repository.findById(task.getId()).orElseThrow();
        assertEquals("PROCESSING", stored.getStatus());
    }

    @Test
    void shouldReturnStaleProcessingTaskToPendingAfterRestartRecovery() {
        LocalDateTime now = LocalDateTime.now();
        NotificationTask task = new NotificationTask(
                10003L,
                "recover after restart",
                now.minusHours(1)
        );
        task.setStatus("PROCESSING");
        task.setProcessingStartedAt(now.minusMinutes(20));
        task = repository.saveAndFlush(task);

        int resetCount = repository.resetStaleProcessing(now.minusMinutes(5));

        assertEquals(1, resetCount);
        entityManager.clear();

        NotificationTask recovered = repository.findById(task.getId()).orElseThrow();
        assertEquals("PENDING", recovered.getStatus());
        assertNull(recovered.getProcessingStartedAt());
        assertFalse(recovered.isSent());
    }
}
