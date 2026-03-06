import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory OTP store for development and testing.
 * Replace with Redis, database, or another persistent store for production.
 */
public class MemoryStore implements OtpService.OtpStore {

    private static class OtpEntry {
        final String hashedOtp;
        final long expiresAt;
        OtpEntry(String hashedOtp, long expiresAt) {
            this.hashedOtp = hashedOtp;
            this.expiresAt = expiresAt;
        }
    }

    private static class RateEntry {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart;
        RateEntry(long windowStart) {
            this.windowStart = windowStart;
        }
    }

    private final ConcurrentHashMap<String, OtpEntry> otpMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateEntry> rateMap = new ConcurrentHashMap<>();

    @Override
    public void saveOtp(String phone, String hashedOtp, long expiresAt) {
        otpMap.put(phone, new OtpEntry(hashedOtp, expiresAt));
    }

    @Override
    public String getHashedOtp(String phone) {
        OtpEntry entry = otpMap.get(phone);
        return entry != null ? entry.hashedOtp : null;
    }

    @Override
    public long getExpiresAt(String phone) {
        OtpEntry entry = otpMap.get(phone);
        return entry != null ? entry.expiresAt : 0;
    }

    @Override
    public void deleteOtp(String phone) {
        otpMap.remove(phone);
    }

    @Override
    public int getRequestCount(String key, long windowStart) {
        RateEntry entry = rateMap.get(key);
        if (entry == null || entry.windowStart < windowStart) {
            return 0;
        }
        return entry.count.get();
    }

    @Override
    public void incrementRequestCount(String key) {
        long now = System.currentTimeMillis();
        rateMap.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStart < now - 3600_000) {
                RateEntry fresh = new RateEntry(now);
                fresh.count.set(1);
                return fresh;
            }
            existing.count.incrementAndGet();
            return existing;
        });
    }
}
