
package pro.sky.telegrambot.repository;

import pro.sky.telegrambot.entity.NotificationTask;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationTaskRepository
        extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findByStatusAndSentFalseAndDateTimeLessThanEqual(
            String status,
            LocalDateTime dateTime
    );й


    @Transactional
    @Modifying
    @Query("UPDATE NotificationTask t SET t.status = 'PROCESSING', " +
            "t.processingStartedAt = :startedAt " +
            "WHERE t.id = :id " +
            "AND t.status = 'PENDING' " +
            "AND t.sent = false " +
            "AND t.dateTime <= :startedAt")
    int claimTask(
            @Param("id") Long id,
            @Param("startedAt") LocalDateTime startedAt
    );

    @Transactional
    @Modifying
    @Query("UPDATE NotificationTask t SET t.sent = true, " +
            "t.status = 'SENT', t.processingStartedAt = null " +
            "WHERE t.id = :id AND t.status = 'PROCESSING'")
    int markAsSentSuccessfully(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE NotificationTask t SET t.status = 'PENDING', " +
            "t.processingStartedAt = null " +
            "WHERE t.id = :id AND t.status = 'PROCESSING'")
    int releaseTask(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE NotificationTask t " +
            "SET t.status = 'PENDING', t.processingStartedAt = null " +
            "WHERE t.status = 'PROCESSING' " +
            "AND t.processingStartedAt <= :cutoff")
    int resetStaleProcessing(@Param("cutoff") LocalDateTime cutoff);
}
