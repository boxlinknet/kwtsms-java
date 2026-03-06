# Error Handling

Demonstrates handling all kwtSMS error codes gracefully.

## Two Categories of Errors

1. **User-recoverable** (bad phone, rate limited): show a helpful message to the user
2. **System-level** (auth, balance, network): show a generic message to the user, log the real error, alert the admin

## Key Principle

Never expose raw API errors (`ERR003`, `ERR025`) to end users. Map them to user-friendly messages and log the details for your admin/operations team.

## Error Code Reference

See `ApiErrors.API_ERRORS` for the complete map of all 33+ error codes with developer-friendly action messages.
