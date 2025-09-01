package com.recceda.core.store;

public interface OtpStore {
    void storeOtp(String key, String otp, long ttlMillis);
    void invalidateOtp(String key);
    void cleanupExpiredOtps();
    boolean verifyOtp(String key, String otp);
}
