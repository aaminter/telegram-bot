
package pro.sky.telegrambot.repository;

import pro.sky.telegrambot.entity.NotificationTask;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE NotificationTask t SET t.sent = true WHERE t.id = :id")
    int markAsSent(@Param("id") Long id);

    List<NotificationTask> findBySentFalseAndDateTimeLessThanEqual(
            LocalDateTime dateTime
    );

}
