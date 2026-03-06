import com.kwtsms.*;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Production-ready OTP service with rate limiting, expiry, and anti-abuse.
 *
 * Features:
 * - 6-digit OTP generation with SecureRandom
 * - Rate limiting per phone number (max 5/hour)
 * - Rate limiting per IP address (max 20/hour)
 * - OTP expiry (5 minutes)
 * - New code on resend (invalidates previous)
 * - SHA-256 hashed storage (never store OTP in plaintext)
 * - Resend cooldown (4 minutes, KNET standard)
 *
 * Replace MemoryStore with a database or Redis adapter for production.
 */
public class OtpService {

    /** Pluggable storage interface. */
    public interface OtpStore {
        void saveOtp(String phone, String hashedOtp, long expiresAt);
        String getHashedOtp(String phone);
        long getExpiresAt(String phone);
        void deleteOtp(String phone);
        int getRequestCount(String key, long windowStart);
        void incrementRequestCount(String key);
    }

    /** Pluggable CAPTCHA verifier interface. */
    public interface CaptchaVerifier {
        boolean verify(String token, String remoteIp);
    }

    private final KwtSMS sms;
    private final OtpStore store;
    private final CaptchaVerifier captcha;
    private final String appName;
    private final SecureRandom random = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final long EXPIRY_MS = 5 * 60 * 1000;       // 5 minutes
    private static final long RESEND_COOLDOWN_MS = 4 * 60 * 1000; // 4 minutes (KNET standard)
    private static final int MAX_PER_PHONE_PER_HOUR = 5;
    private static final int MAX_PER_IP_PER_HOUR = 20;

    public OtpService(KwtSMS sms, OtpStore store, CaptchaVerifier captcha, String appName) {
        this.sms = sms;
        this.store = store;
        this.captcha = captcha;
        this.appName = appName;
    }

    public static class OtpResult {
        public final boolean success;
        public final String error;
        public final String msgId;

        public OtpResult(boolean success, String error, String msgId) {
            this.success = success;
            this.error = error;
            this.msgId = msgId;
        }
    }

    /**
     * Send an OTP to a phone number.
     *
     * @param phone       raw phone number input
     * @param remoteIp    client IP address (for rate limiting)
     * @param captchaToken CAPTCHA response token (required)
     * @return OtpResult
     */
    public OtpResult sendOtp(String phone, String remoteIp, String captchaToken) {
        // 1. Verify CAPTCHA
        if (captcha != null && !captcha.verify(captchaToken, remoteIp)) {
            return new OtpResult(false, "CAPTCHA verification failed.", null);
        }

        // 2. Validate phone
        PhoneUtils.ValidationResult vr = PhoneUtils.validatePhoneInput(phone);
        if (!vr.isValid()) {
            return new OtpResult(false, vr.getError(), null);
        }
        String normalized = vr.getNormalized();

        // 3. Rate limit per phone
        long windowStart = Instant.now().toEpochMilli() - 3600_000;
        int phoneCount = store.getRequestCount("phone:" + normalized, windowStart);
        if (phoneCount >= MAX_PER_PHONE_PER_HOUR) {
            return new OtpResult(false, "Too many requests to this number. Please try again later.", null);
        }

        // 4. Rate limit per IP
        int ipCount = store.getRequestCount("ip:" + remoteIp, windowStart);
        if (ipCount >= MAX_PER_IP_PER_HOUR) {
            return new OtpResult(false, "Too many requests. Please try again later.", null);
        }

        // 5. Resend cooldown: check if previous OTP is still within cooldown window
        long prevExpiry = store.getExpiresAt(normalized);
        if (prevExpiry > 0) {
            long sentAt = prevExpiry - EXPIRY_MS;
            long elapsed = Instant.now().toEpochMilli() - sentAt;
            if (elapsed < RESEND_COOLDOWN_MS) {
                long remaining = (RESEND_COOLDOWN_MS - elapsed) / 1000;
                return new OtpResult(false, "Please wait " + remaining + " seconds before requesting a new code.", null);
            }
        }

        // 6. Generate OTP
        int otp = generateOtp();
        String hashedOtp = sha256(String.valueOf(otp));
        long expiresAt = Instant.now().toEpochMilli() + EXPIRY_MS;

        // 7. Invalidate previous and save new
        store.deleteOtp(normalized);
        store.saveOtp(normalized, hashedOtp, expiresAt);
        store.incrementRequestCount("phone:" + normalized);
        store.incrementRequestCount("ip:" + remoteIp);

        // 8. Send SMS (one number per OTP request)
        String message = "Your OTP for " + appName + " is: " + otp;
        SendResult result = sms.send(normalized, message);

        if ("OK".equals(result.getResult())) {
            return new OtpResult(true, null, result.getMsgId());
        } else {
            store.deleteOtp(normalized);
            return new OtpResult(false, "Could not send OTP. Please try again.", null);
        }
    }

    /**
     * Verify an OTP code.
     *
     * @param phone raw phone number
     * @param code  the OTP code to verify
     * @return true if valid
     */
    public OtpResult verifyOtp(String phone, String code) {
        PhoneUtils.ValidationResult vr = PhoneUtils.validatePhoneInput(phone);
        if (!vr.isValid()) {
            return new OtpResult(false, "Invalid phone number.", null);
        }
        String normalized = vr.getNormalized();

        String storedHash = store.getHashedOtp(normalized);
        long expiresAt = store.getExpiresAt(normalized);

        if (storedHash == null) {
            return new OtpResult(false, "No OTP found. Please request a new code.", null);
        }

        if (Instant.now().toEpochMilli() > expiresAt) {
            store.deleteOtp(normalized);
            return new OtpResult(false, "OTP has expired. Please request a new code.", null);
        }

        String inputHash = sha256(code);
        if (storedHash.equals(inputHash)) {
            store.deleteOtp(normalized);
            return new OtpResult(true, null, null);
        }

        return new OtpResult(false, "Invalid OTP code.", null);
    }

    private int generateOtp() {
        int min = (int) Math.pow(10, OTP_LENGTH - 1);
        int max = (int) Math.pow(10, OTP_LENGTH) - 1;
        return min + random.nextInt(max - min + 1);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
