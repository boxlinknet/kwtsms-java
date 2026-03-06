# Production OTP Service

A production-ready OTP (One-Time Password) service with rate limiting, CAPTCHA, and secure storage.

## Features

- 6-digit OTP generation with `SecureRandom`
- SHA-256 hashed storage (never stores OTP in plaintext)
- Rate limiting per phone number (max 5/hour)
- Rate limiting per IP address (max 20/hour)
- OTP expiry (5 minutes)
- Resend cooldown (4 minutes, KNET standard)
- New code on resend (invalidates previous codes)
- Pluggable CAPTCHA: Cloudflare Turnstile (free, recommended) or hCaptcha (GDPR-safe)
- Pluggable storage: in-memory for dev, replace with Redis/database for production

## Architecture

```
OtpService (core logic)
  +-- OtpStore (interface)
  |     +-- MemoryStore (development)
  |     +-- RedisStore (production, implement yourself)
  |     +-- JdbcStore (production, implement yourself)
  +-- CaptchaVerifier (interface)
        +-- TurnstileVerifier (Cloudflare, free)
        +-- HcaptchaVerifier (GDPR-safe)
```

## Security Checklist

Before going live:

- [ ] Bot protection enabled (Turnstile/hCaptcha)
- [ ] Rate limit per phone number (max 5/hour)
- [ ] Rate limit per IP address (max 20/hour)
- [ ] OTP stored as SHA-256 hash, never plaintext
- [ ] OTP expires after 5 minutes
- [ ] Resend cooldown of 4 minutes
- [ ] Transactional sender ID registered (not KWT-SMS)
- [ ] Test mode OFF (`KWTSMS_TEST_MODE=0`)
- [ ] Admin notification on low balance
- [ ] Monitoring/alerting on abuse patterns
