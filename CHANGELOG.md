# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-03-06

### Added
- Initial release
- `KwtSMS` client class with constructor and `fromEnv()` factory
- `verify()`: test credentials and get balance
- `balance()`: get credits with cached fallback
- `send()`: send SMS to single or multiple numbers
- `sendBulk()`: auto-batching for >200 numbers with ERR013 retry
- `validate()`: validate phone numbers via API
- `senderIds()`: list sender IDs
- `coverage()`: list active country prefixes
- `status()`: check message queue status
- `deliveryReport()`: delivery reports for international numbers
- `PhoneUtils`: `normalizePhone()`, `validatePhoneInput()`, `deduplicatePhones()`
- `MessageUtils`: `cleanMessage()` (emoji, HTML, control chars, Arabic digit conversion)
- `ApiErrors`: complete error code map with developer-friendly action messages, `enrichError()`
- `EnvLoader`: `.env` file parser (never modifies system environment)
- JSONL logging with password masking
- Thread-safe: `volatile` cached balance fields
- Zero runtime dependencies (Java 8+ stdlib only)
- Unit tests (phone, message, errors, env loader)
- Integration tests (live API with test_mode, skipped without JAVA_USERNAME)
