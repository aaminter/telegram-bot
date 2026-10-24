
package pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderSchedulerServiceTest {

    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @Mock
    private TelegramBot telegramBot;

    @InjectMocks
    private ReminderSchedulerService reminderSchedulerService;

    @Test
    void shouldNotSendMessagesWhenNoRemindersAreDue() {
        when(notificationTaskRepository
                .findByStatusAndSentFalseAndDateTime(
                        eq("PENDING"),
                        any(LocalDateTime.class)
                ))
                .thenReturn(Collections.emptyList());

        reminderSchedulerService.checkReminders();

        verify(telegramBot, never()).execute(any(SendMessage.class));
    }

    @Test
    void shouldSendDueReminderAndMarkItAsSent() {
        NotificationTask task = new NotificationTask(
                123456789L,
                "Проверить курсовую",
                LocalDateTime.now().withSecond(0).withNano(0)
        );
        task.setId(1L);

        when(notificationTaskRepository.resetStaleProcessing(
                any(LocalDateTime.class)
        )).thenReturn(0);

        when(notificationTaskRepository
                .findByStatusAndSentFalseAndDateTime(
                        eq("PENDING"),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(task));

        when(notificationTaskRepository.claimTask(
                eq(1L),
                any(LocalDateTime.class)
        )).thenReturn(1);

        SendResponse response = mock(SendResponse.class);
        when(telegramBot.execute(any(SendMessage.class)))
                .thenReturn(response);
        when(response.isOk()).thenReturn(true);

        when(notificationTaskRepository.markAsSentSuccessfully(1L))
                .thenReturn(1);

        reminderSchedulerService.checkReminders();

        verify(telegramBot).execute(any(SendMessage.class));
        verify(notificationTaskRepository).markAsSentSuccessfully(1L);
        verify(notificationTaskRepository, never()).releaseTask(1L);
    }
}
