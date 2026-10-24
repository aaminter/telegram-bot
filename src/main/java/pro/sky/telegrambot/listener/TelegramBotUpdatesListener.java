
package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Logger logger =
            LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    // Шаблон: дата и время, пробел, текст напоминания.
    private static final Pattern REMINDER_PATTERN = Pattern.compile(
            "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)"
    );

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(
            TelegramBot telegramBot,
            NotificationTaskRepository notificationTaskRepository
    ) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {

            // Нам нужны только обычные текстовые сообщения.
            if (update.message() == null
                    || update.message().text() == null
                    || update.message().chat() == null) {
                continue;
            }

            Long chatId = update.message().chat().id();
            String text = update.message().text().trim();

            if (text.isEmpty()) {
                continue;
            }

            // Обрабатываем команду запуска бота.
            if ("/start".equals(text)) {
                sendMessage(chatId,
                        "Привет! Я бот напоминаний.\n\n"
                                + "Отправь напоминание в формате:\n"
                                + "24.10.2026 20:00 Сделать домашнюю работу");
                continue;
            }

            // Проверяем, соответствует ли сообщение нужному формату.
            Matcher matcher = REMINDER_PATTERN.matcher(text);

            if (!matcher.matches()) {
                sendMessage(chatId,
                        "Не удалось распознать сообщение.\n"
                                + "Используй формат:\n"
                                + "дд.ММ.гггг ЧЧ:мм Текст напоминания");
                continue;
            }

            // Группа 1 содержит дату и время, группа 3 содержит текст.
            String dateTimeText = matcher.group(1);
            String reminderText = matcher.group(3).trim();

            try {
                LocalDateTime dateTime =
                        LocalDateTime.parse(dateTimeText, DATE_TIME_FORMATTER);

                NotificationTask task =
                        new NotificationTask(chatId, reminderText, dateTime);

                // Сохраняем напоминание в PostgreSQL.
                notificationTaskRepository.save(task);

                logger.info("Saved reminder for chat {} at {}",
                        chatId, dateTime);

                sendMessage(chatId,
                        "Напоминание сохранено на "
                                + dateTime.format(DATE_TIME_FORMATTER)
                                + "\nТекст: " + reminderText);

            } catch (DateTimeParseException exception) {
                sendMessage(chatId,
                        "Дата или время указаны некорректно. "
                                + "Пример: 24.10.2026 20:00 Позвонить");
            }
        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(Long chatId, String text) {
        SendResponse response =
                telegramBot.execute(new SendMessage(chatId, text));

        if (!response.isOk()) {
            logger.warn("Telegram did not accept message. Error code: {}",
                    response.errorCode());
        }
    }
}
