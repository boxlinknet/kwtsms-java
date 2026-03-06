# OTP Flow

Demonstrates sending a one-time password via SMS.

## Important

- Use a **Transactional** sender ID for OTP (not Promotional). Promotional sender IDs are filtered by DND (Do Not Disturb) on Zain and Ooredoo. OTP messages silently fail to deliver and credits are still deducted.
- Always include your app/company name in the message: telecom compliance requirement.
- Set a 3-5 minute OTP expiry.
- Generate a new code on resend and invalidate all previous codes for that number.
- Enforce a minimum 3-4 minute resend timer (KNET standard is 4 minutes).
- Send to one number per OTP request to avoid ERR028 batch rejection.
