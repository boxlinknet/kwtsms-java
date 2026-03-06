# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2026-03-06

### Changed
- `fromEnv()` now reads `JAVA_USERNAME` / `JAVA_PASSWORD` instead of `KWTSMS_USERNAME` / `KWTSMS_PASSWORD` for credentials, so each language client uses distinct env vars
- ERR003 error message references `JAVA_USERNAME` / `JAVA_PASSWORD`
- Updated all docs and examples to use `JAVA_USERNAME` / `JAVA_PASSWORD`

### Updated
- actions/checkout v4 → v6
- actions/setup-java v4 → v5
- gradle/actions v4 → v5
- github/codeql-action v3 → v4

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
