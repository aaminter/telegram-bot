
package pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.util.Collections;

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
                .findByStatusAndSentFalseAndDateTimeLessThanEqual(
                        eq("PENDING"),
                        any()
                ))
                .thenReturn(Collections.emptyList());

        reminderSchedulerService.checkReminders();

        verify(telegramBot, never()).execute(any());
    }
}
