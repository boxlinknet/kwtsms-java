# kwtSMS Java Client

[![Tests](https://github.com/boxlinknet/kwtsms-java/actions/workflows/test.yml/badge.svg)](https://github.com/boxlinknet/kwtsms-java/actions/workflows/test.yml)
[![CodeQL](https://github.com/boxlinknet/kwtsms-java/actions/workflows/codeql.yml/badge.svg)](https://github.com/boxlinknet/kwtsms-java/actions/workflows/codeql.yml)
[![JitPack](https://jitpack.io/v/boxlinknet/kwtsms-java.svg)](https://jitpack.io/#boxlinknet/kwtsms-java)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-blue)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Official Java client for the [kwtSMS](https://www.kwtsms.com) SMS gateway API. Zero dependencies, Java 8+, thread-safe.

[kwtSMS](https://www.kwtsms.com) is a Kuwait-based SMS gateway for sending SMS messages, OTP codes, notifications, and marketing campaigns. It supports Kuwait and international numbers, offers sender ID registration, delivery reports, and a REST/JSON API. This library wraps the full API so you can integrate SMS into any Java application in minutes.

Send SMS, verify credentials, check balance, validate phone numbers, and more.

## Install

### Gradle (JitPack)

```gradle
// settings.gradle
repositories {
    maven { url 'https://jitpack.io' }
}

// build.gradle
dependencies {
    implementation 'com.github.boxlinknet:kwtsms-java:0.2.0'
}
```

### Gradle Kotlin DSL

```kotlin
// settings.gradle.kts
repositories {
    maven("https://jitpack.io")
}

// build.gradle.kts
dependencies {
    implementation("com.github.boxlinknet:kwtsms-java:0.2.0")
}
```

### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.boxlinknet</groupId>
    <artifactId>kwtsms-java</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Quick Start

```java
import com.kwtsms.*;

// Option 1: Load from environment variables / .env file
KwtSMS sms = KwtSMS.fromEnv();

// Option 2: Pass credentials directly
KwtSMS sms = new KwtSMS("java_your_api_user", "java_your_api_pass");

// Verify credentials
VerifyResult verify = sms.verify();
System.out.println("OK: " + verify.isOk() + ", Balance: " + verify.getBalance());

// Send SMS
SendResult result = sms.send("96598765432", "Hello from Java!");
System.out.println("Result: " + result.getResult());
System.out.println("Message ID: " + result.getMsgId());
System.out.println("Balance after: " + result.getBalanceAfter());
```

## Environment Variables

Create a `.env` file (or set system environment variables):

```ini
KWTSMS_USERNAME=java_your_api_user
KWTSMS_PASSWORD=java_your_api_pass
KWTSMS_SENDER_ID=YOUR-SENDER
KWTSMS_TEST_MODE=1
KWTSMS_LOG_FILE=kwtsms.log
```

`KwtSMS.fromEnv()` reads environment variables first, falls back to `.env` file.

## API Reference

### Constructor

```java
// All parameters
KwtSMS sms = new KwtSMS(username, password, senderId, testMode, logFile);

// Defaults: senderId="KWT-SMS", testMode=false, logFile="kwtsms.log"
KwtSMS sms = new KwtSMS("java_user", "java_pass");

// From environment / .env file
KwtSMS sms = KwtSMS.fromEnv();
KwtSMS sms = KwtSMS.fromEnv("/path/to/.env");
```

### verify()

Test credentials and get balance. Never throws.

```java
VerifyResult result = sms.verify();
result.isOk();       // true if credentials are valid
result.getBalance();  // available credits (Double)
result.getError();    // error message if failed
```

### balance()

Get current SMS credit balance. Returns cached value if API call fails.

```java
Double balance = sms.balance();  // null if no cached value and API fails
```

### send()

Send SMS to one or more numbers. Automatically validates, normalizes, deduplicates, and cleans the message.

```java
// Single number
SendResult r = sms.send("96598765432", "Hello!");

// Multiple (comma-separated)
SendResult r = sms.send("96598765432,96512345678", "Hello!");

// Multiple (list)
SendResult r = sms.send(Arrays.asList("96598765432", "96512345678"), "Hello!");

// Custom sender ID
SendResult r = sms.send("96598765432", "Hello!", "MY-SENDER");

r.getResult();        // "OK" or "ERROR"
r.getMsgId();         // message ID (save this!)
r.getNumbers();       // count of numbers sent
r.getPointsCharged(); // credits deducted
r.getBalanceAfter();  // balance after send (save this!)
r.getCode();          // error code (e.g., "ERR003")
r.getDescription();   // error description
r.getAction();        // developer-friendly action message
r.getInvalid();       // list of numbers that failed local validation
```

### sendBulk()

Send to >200 numbers with auto-batching.

```java
BulkSendResult r = sms.sendBulk(phoneList, "Campaign message");
r.getResult();        // "OK", "PARTIAL", or "ERROR"
r.getBatches();       // number of batches
r.getMsgIds();        // message IDs per batch
r.getErrors();        // per-batch errors
```

### validate()

Validate phone numbers via the kwtSMS API.

```java
ValidateResult r = sms.validate(Arrays.asList("96598765432", "invalid"));
r.getOk();       // valid and routable numbers
r.getEr();       // format errors
r.getNr();       // no route (country not activated)
r.getRejected(); // failed local validation
```

### senderIds()

List available sender IDs on your account.

```java
SenderIdResult r = sms.senderIds();
r.getSenderIds();  // ["KWT-SMS", "MY-APP"]
```

### coverage()

List active country prefixes.

```java
CoverageResult r = sms.coverage();
r.getPrefixes();  // ["965", "966", ...]
```

### status()

Check message queue status.

```java
StatusResult r = sms.status("msg-id-from-send");
r.getStatus();             // "sent", "pending", etc.
r.getStatusDescription();  // human-readable status
```

### deliveryReport()

Get delivery reports (international numbers only, not Kuwait).

```java
DeliveryReportResult r = sms.deliveryReport("msg-id-from-send");
for (DeliveryReportEntry entry : r.getReport()) {
    System.out.println(entry.getNumber() + ": " + entry.getStatus());
}
```

## Utility Functions

### PhoneUtils

```java
// Normalize: strip +, 00, spaces, dashes, convert Arabic digits
String normalized = PhoneUtils.normalizePhone("+965 9876 5432");
// -> "96598765432"

// Validate: returns ValidationResult with isValid(), getError(), getNormalized()
PhoneUtils.ValidationResult vr = PhoneUtils.validatePhoneInput("user@email.com");
// -> isValid()=false, getError()="'user@email.com' is an email address..."

// Deduplicate
List<String> unique = PhoneUtils.deduplicatePhones(phoneList);
```

### MessageUtils

```java
// Clean: strip emojis, HTML, control chars, convert Arabic digits
String clean = MessageUtils.cleanMessage("Hello \uD83D\uDE00 <b>World</b>");
// -> "Hello  World"
```

### ApiErrors

```java
// Full error code map (read-only)
Map<String, String> errors = ApiErrors.API_ERRORS;
String action = errors.get("ERR003");
// -> "Wrong API username or password..."

// Enrich a raw API response
Map<String, Object> enriched = ApiErrors.enrichError(apiResponse);
// Adds "action" field with developer-friendly guidance
```

## Credential Management

**Never hardcode credentials.** Use one of these approaches:

1. **Environment variables / .env file** (default): `KwtSMS.fromEnv()` handles this automatically
2. **Spring Boot**: set in `application.properties`, load via `@Value` or environment
3. **Constructor injection**: `new KwtSMS(username, password)` for custom config systems
4. **Secrets manager**: load from AWS Secrets Manager, HashiCorp Vault, etc.

## Best Practices

### Always save msg-id and balance-after

```java
SendResult r = sms.send(phone, message);
if ("OK".equals(r.getResult())) {
    db.save("sms_msg_id", r.getMsgId());         // needed for status/DLR
    db.save("sms_balance", r.getBalanceAfter());  // no extra API call needed
}
```

### Validate before calling the API

```java
PhoneUtils.ValidationResult vr = PhoneUtils.validatePhoneInput(userInput);
if (!vr.isValid()) {
    return error(vr.getError());  // rejected locally, no API call
}
```

### Cache coverage for local country checks

```java
// At startup
CoverageResult coverage = sms.coverage();
Set<String> activePrefixes = new HashSet<>(coverage.getPrefixes());

// Before send
if (!activePrefixes.stream().anyMatch(normalized::startsWith)) {
    return error("SMS delivery to this country is not available.");
}
```

### User-facing error messages

Never expose raw API errors to end users:

| Situation | Show to user | Log for admin |
|-----------|-------------|---------------|
| Invalid phone | "Please enter a valid phone number (e.g., +965 9876 5432)." | ERR006/ERR025 |
| Auth/balance | "SMS service is temporarily unavailable." | ERR003/ERR010/ERR011 |
| Rate limited | "Please wait a moment before requesting another code." | ERR028 |
| Network error | "Could not connect to SMS service." | Connection timeout |

### Thread safety

`KwtSMS` is thread-safe. Create one instance and share it across threads. Use it as a singleton bean in Spring.

### Sender ID

- `KWT-SMS` is for testing only. Register a private sender ID before going live.
- Use **Transactional** sender ID for OTP (bypasses DND filtering).
- Sender ID is **case sensitive**: `Kuwait` is not the same as `KUWAIT`.

## Security Checklist

Before going live:

- [ ] Bot protection enabled (CAPTCHA for web)
- [ ] Rate limit per phone number (max 3-5/hour)
- [ ] Rate limit per IP address (max 10-20/hour)
- [ ] Rate limit per user/session if authenticated
- [ ] Monitoring/alerting on abuse patterns
- [ ] Admin notification on low balance
- [ ] Test mode OFF (`KWTSMS_TEST_MODE=0`)
- [ ] Private sender ID registered (not KWT-SMS)
- [ ] Transactional sender ID for OTP (not promotional)

## Timestamps

`unix-timestamp` values in API responses are in **GMT+3 (Asia/Kuwait)** server time, not UTC. Convert when storing or displaying.

## Examples

See the [examples/](examples/) directory:

| # | Example | Description |
|---|---------|-------------|
| 01 | [Basic Usage](examples/01-basic-usage/) | Send SMS, check balance, verify credentials |
| 02 | [OTP Flow](examples/02-otp-flow/) | One-time password implementation |
| 03 | [Bulk SMS](examples/03-bulk-sms/) | Auto-batched bulk sending |
| 04 | [Spring Endpoint](examples/04-spring-endpoint/) | REST API with Spring Boot |
| 05 | [Error Handling](examples/05-error-handling/) | Handle all error codes gracefully |
| 06 | [OTP Production](examples/06-otp-production/) | Production OTP with rate limiting and CAPTCHA |

## Requirements

- Java 8+ (runtime)
- Zero runtime dependencies

## Links

- [kwtSMS website](https://www.kwtsms.com)
- [API documentation (PDF)](https://www.kwtsms.com/doc/KwtSMS.com_API_Documentation_v41.pdf)
- [Best practices](https://www.kwtsms.com/articles/sms-api-implementation-best-practices.html)
- [Integration test checklist](https://www.kwtsms.com/articles/sms-api-integration-test-checklist.html)
- [All integrations](https://www.kwtsms.com/integrations.html)
- [Support](https://www.kwtsms.com/support.html)

## License

[MIT](LICENSE)
