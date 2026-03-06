# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in this library, please report it responsibly.

**Do NOT open a public GitHub issue for security vulnerabilities.**

Instead, email security concerns to: support@kwtsms.com

Include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

We will acknowledge receipt within 48 hours and provide an update within 7 days.

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.1.x   | Yes       |

## Security Best Practices

When using this library:

1. **Never hardcode credentials** in source code
2. **Use environment variables** or `.env` files for API credentials
3. **Add `.env` to `.gitignore`** to prevent credential leaks
4. **Mask credentials in logs**: the library does this automatically
5. **Use HTTPS only**: the library enforces this
6. **Implement rate limiting** in your application before going live
7. **Use CAPTCHA/bot protection** on forms that trigger SMS sends
