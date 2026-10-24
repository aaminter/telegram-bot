
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

    @Scheduled(fixedRate = 10000)
    public void checkReminders() {
        List<NotificationTask> tasks =
                notificationTaskRepository
                        .findBySentFalseAndDateTimeLessThanEqual(
                                LocalDateTime.now()
                        );

        for (NotificationTask task : tasks) {
            try {
                SendResponse response = telegramBot.execute(
                        new SendMessage(
                                task.getChatId(),
                                "Напоминание: " + task.getMessage()
                        )
                );

                if (response.isOk()) {
                    markTaskAsSent(task.getId());
                    logger.info("Reminder {} sent successfully", task.getId());
                } else {
                    logger.warn(
                            "Failed to send reminder {}. Telegram error: {}",
                            task.getId(),
                            response.errorCode()
                    );
                }
            } catch (Exception exception) {
                logger.error(
                        "Error while sending reminder {}",
                        task.getId(),
                        exception
                );
            }
        }
    }
    public void markTaskAsSent(Long id) {
        notificationTaskRepository.markAsSent(id);
    }
}
