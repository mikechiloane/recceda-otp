package com.recceda.core.store;

import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.ReccedaOtpStore.OtpEntry;

public interface OtpStore {
    void storeOtp(String key, String otp, long ttlMillis, OtpReason reason);
    boolean verifyOtp(String key, String otp, OtpReason reason);
    OtpEntry getOtpEntry(String key, OtpReason reason);
    void invalidateOtp(String key, OtpReason reason);
    void cleanupExpiredOtps();
}
