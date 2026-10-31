# Engineering Roadmap

This roadmap lists quality improvements to validate against the current implementation. Items are not claims of completed functionality.

## Reliability

- [ ] Define retry/backoff behavior for Telegram API failures.
- [ ] Ensure repeated updates do not create duplicate side effects.
- [ ] Define scheduler behavior after restart and during concurrent execution.
- [ ] Add clear handling for missing/invalid user input.

## Persistence

- [ ] Verify Liquibase changelog is reproducible from an empty database.
- [ ] Add repository integration tests.
- [ ] Define transaction boundaries for multi-step operations.

## Testing

- [ ] Unit-test service/business logic.
- [ ] Test scheduler timing through controllable abstractions rather than long sleeps.
- [ ] Add integration tests for database behavior.
- [ ] Test Telegram API integration through a mockable client boundary.

## Delivery

- [ ] Add Docker Compose for local PostgreSQL.
- [ ] Add CI running `mvn clean verify`.
- [ ] Add dependency and secret scanning.
- [ ] Document configuration and operational troubleshooting.
