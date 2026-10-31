
package pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderSchedulerService {

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderSchedulerService.class);

    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;

    public ReminderSchedulerService(
            NotificationTaskRepository notificationTaskRepository,
            TelegramBot telegramBot
    ) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
                int resetCount = notificationTaskRepository.resetStaleProcessing(
                now.minusMinutes(5)
        );

        if (resetCount > 0) {
            logger.warn(
                    "Reset {} stale reminder tasks",
                    resetCount
            );
        }

        List<NotificationTask> tasks =
                notificationTaskRepository
                        .findByStatusAndSentFalseAndDateTimeLessThanEqual(
                                "PENDING",
                                now
                        );

        for (NotificationTask task : tasks) {
            int claimed = notificationTaskRepository.claimTask(
                    task.getId(),
                    now
            );

            if (claimed == 0) {
                continue;
            }

            try {
                SendResponse response = telegramBot.execute(
                        new SendMessage(
                                task.getChatId(),
                                "Напоминание: " + task.getMessage()
                        )
                );

                if (response.isOk()) {
                    int updated = notificationTaskRepository
                            .markAsSentSuccessfully(task.getId());

                    if (updated == 1) {
                        logger.info(
                                "Reminder {} sent successfully",
                                task.getId()
                        );
                    } else {
                        logger.error(
                                "Reminder {} was sent, but its status was not updated",
                                task.getId()
                        );
                    }
                } else {
                    notificationTaskRepository.releaseTask(task.getId());

                    logger.warn(
                            "Failed to send reminder {}. Telegram error: {}",
                            task.getId(),
                            response.errorCode()
                    );
                }
            } catch (Exception exception) {
                notificationTaskRepository.releaseTask(task.getId());

                logger.error(
                        "Error while sending reminder {}",
                        task.getId(),
                        exception
                );
            }
        }
    }
}
