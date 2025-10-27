package com.recceda.core.store.reccedda;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.recceda.OtpEntry;
import com.recceda.core.store.OtpStore;
import com.recceda.core.util.HashingUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * An in-memory OTP store that uses a Caffeine cache for high-performance, concurrent access.
 *
 * <p>This class is responsible for storing, verifying, and invalidating OTPs. It uses a secure
 * SHA-256 hash to store OTPs and relies on the Caffeine library's time-based eviction for automatic
 * cleanup of expired OTPs.
 */
public class ReccedaOtpStore implements OtpStore {

    private final Cache<String, OtpEntry> otpMap;

    /**
     * Creates a new {@code ReccedaOtpStore} with the default expiry policy.
     *
     * <p>The default policy expires entries based on the `expiryTime` in the {@link OtpEntry}.
     */
    public ReccedaOtpStore() {
        this(
                new Expiry<String, OtpEntry>() {
                    @Override
                    public long expireAfterCreate(String key, OtpEntry value, long currentTime) {
                        long millis = value.getExpiryTime() - System.currentTimeMillis();
                        return TimeUnit.MILLISECONDS.toNanos(millis);
                    }

                    @Override
                    public long expireAfterUpdate(
                            String key, OtpEntry value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(
                            String key, OtpEntry value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                });
    }

    /**
     * Creates a new {@code ReccedaOtpStore} with a custom expiry policy.
     *
     * @param expiry the custom expiry policy to use for the Caffeine cache.
     */
    public ReccedaOtpStore(Expiry<String, OtpEntry> expiry) {
        this.otpMap = Caffeine.newBuilder().expireAfter(expiry).build();
    }

    @Override
    public CompletableFuture<Void> storeOtp(String key, String otp, long ttlMillis) {
        long expiryTime = System.currentTimeMillis() + ttlMillis;
        String otpHash = HashingUtil.hashOtp(otp);
        otpMap.put(key, OtpEntry.builder().key(key).otpHash(otpHash).expiryTime(expiryTime).failedAttempts(0).build());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> verifyOtp(String key, String otp) {
        OtpEntry entry = otpMap.getIfPresent(key);
        if (entry == null) {
            return CompletableFuture.completedFuture(false);
        }

        String otpHash = HashingUtil.hashOtp(otp);
        boolean isValid = otpHash.equals(entry.getOtpHash());
        if (!isValid) {
            entry.setFailedAttempts(entry.getFailedAttempts() + 1);
            otpMap.put(key, entry);
        }
        return CompletableFuture.completedFuture(isValid);
    }

    @Override
    public CompletableFuture<OtpEntry> getOtpEntry(String key) {
        return CompletableFuture.completedFuture(otpMap.getIfPresent(key));
    }

    @Override
    public CompletableFuture<Void> invalidateOtp(String key) {
        otpMap.invalidate(key);
        return CompletableFuture.completedFuture(null);
    }


}
