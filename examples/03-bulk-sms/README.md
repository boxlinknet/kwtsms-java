# Bulk SMS

Demonstrates sending SMS to large recipient lists.

## Auto-Batching

The `sendBulk()` method handles batching automatically:
- Splits into batches of 200 (API maximum per request)
- 0.5 second delay between batches (rate limiting)
- ERR013 (queue full) retried up to 3 times with exponential backoff (30s, 60s, 120s)

## Results

- `OK`: all batches succeeded
- `PARTIAL`: some batches succeeded, some failed
- `ERROR`: all batches failed

Always check `getInvalid()` for phone numbers that failed local validation.

## Before Bulk Sends

- Estimate credit cost: recipients x pages per message
- Check cached balance: `sms.getCachedBalance()`
- Use `validate()` to pre-check numbers
- Call `coverage()` at startup and cache prefixes for local country validation
