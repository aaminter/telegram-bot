# Telegram Reminder Bot

![Java CI](https://github.com/aaminter/telegram-bot/actions/workflows/ci.yml/badge.svg)

Учебный backend-проект на Java и Spring Boot для работы с Telegram Bot API, PostgreSQL, Liquibase и отложенными напоминаниями.

## Текущая реализация

- Java 11, Spring Boot 2.6.5;
- Telegram Bot API (pengrad);
- PostgreSQL, Spring Data JPA и Liquibase;
- хранение задач-напоминаний;
- планировщик обработки просроченных задач;
- атомарный claim через условное обновление статуса;
- восстановление зависших задач PROCESSING;
- отправка сообщений и фиксация успешной доставки;
- CI с PostgreSQL service и Maven verify.

Просроченные напоминания выбираются по условию dateTime <= now, поэтому пропуск точного минутного окна не приводит к потере задачи.

## Ограничения надёжности

Атомарный claim защищает от одновременной обработки одной задачи несколькими воркерами. Однако внешний Telegram API не предоставляет универсальный idempotency key для sendMessage. При неопределённом сетевом исходе возможна повторная отправка.

Retry policy, максимальное число попыток, backoff и обработка UNKNOWN результата отслеживаются в [Issue #8](https://github.com/aaminter/telegram-bot/issues/8). Не заявляется exactly-once доставка.

## Технологии

- Java 11;
- Spring Boot 2.6.5;
- Spring Web;
- Spring Data JPA;
- PostgreSQL;
- Liquibase;
- Telegram Bot API;
- Maven;
- JUnit / Spring Boot Test.

## Конфигурация

Параметры передаются через переменные окружения:

| Переменная | Назначение |
|---|---|
| DB_URL | JDBC URL PostgreSQL |
| DB_USERNAME | Пользователь БД |
| DB_PASSWORD | Пароль БД |
| TELEGRAM_BOT_TOKEN | Токен Telegram-бота |

Не добавляй реальные токены, пароли и дампы БД в Git. Локальный пример находится в .env.example.

## Запуск

1. Установи JDK 11 и Maven.
2. Подними PostgreSQL и создай отдельную базу.
3. Установи переменные окружения.
4. Выполни:

```bash
mvn clean verify
mvn spring-boot:run
```

## Дальнейшее развитие

- ограничение повторных попыток и backoff;
- обработка неопределённого результата внешней отправки;
- интеграционные тесты восстановления после рестарта;
- Docker Compose;
- метрики и мониторинг;
- расширение тестов на конкурентную обработку.

## Статус

Учебный backend-проект. Не production-ready сервис. Текущие гарантии доставки и незакрытые риски описаны выше и в Issues.
