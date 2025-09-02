package com.recceda.core.store;

import net.openhft.chronicle.map.ChronicleMap;

import com.recceda.core.reason.OtpReason;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ReccedaOtpStore implements OtpStore {

    private final ChronicleMap<String, OtpEntry> otpMap;

    public ReccedaOtpStore() {
        this.otpMap = ChronicleMap
                .of(String.class, OtpEntry.class)
                .name("otp-store")
                .averageKeySize(50)
                .averageValue(new OtpEntry("", 0, OtpReason.LOGIN))
                .entries(1000)
                .create();
    }

    @Override
    public void storeOtp(String key, String otp, long ttlMillis, OtpReason reason) {
        String compositeKey = key + ":" + reason.name();
        long expiryTime = System.currentTimeMillis() + ttlMillis;
        String otpHash = hashOtp(otp);
        otpMap.put(compositeKey, new OtpEntry(otpHash, expiryTime, reason));
    }

    @Override
    public boolean verifyOtp(String key, String otp, OtpReason reason) {
        String compositeKey = key + ":" + reason.name();
        OtpEntry entry = otpMap.get(compositeKey);
        if (entry == null || entry.expiryTime < System.currentTimeMillis()) {
            return false;
        }

        String otpHash = hashOtp(otp);
        boolean isValid = otpHash.equals(entry.otpHash);
        if (!isValid) {
            entry.failedAttempts++;
            otpMap.put(compositeKey, entry);
        }
        return isValid;
    }

    @Override
    public OtpEntry getOtpEntry(String key, OtpReason reason) {
        String compositeKey = key + ":" + reason.name();
        return otpMap.get(compositeKey);
    }

    @Override
    public void invalidateOtp(String key, OtpReason reason) {
        String compositeKey = key + ":" + reason.name();
        otpMap.remove(compositeKey);
    }

    @Override
    public void cleanupExpiredOtps() {
        long currentTime = System.currentTimeMillis();
        otpMap.entrySet().removeIf(entry -> entry.getValue().expiryTime < currentTime);
    }

    private String hashOtp(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static class OtpEntry implements java.io.Serializable {
        private static final long serialVersionUID = 2L; // Incremented serialVersionUID
        public String otpHash;
        public long expiryTime;
        public OtpReason reason;
        public int failedAttempts;

        public OtpEntry() {
        }

        public OtpEntry(String otpHash, long expiryTime, OtpReason reason) {
            this.otpHash = otpHash;
            this.expiryTime = expiryTime;
            this.reason = reason;
            this.failedAttempts = 0;
        }
    }
}
