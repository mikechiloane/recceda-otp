package com.recceda.core.store;

import net.openhft.chronicle.map.ChronicleMap;

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
                .averageValue(new OtpEntry("", 0))
                .entries(1000)
                .create();
    }

    @Override
    public void storeOtp(String key, String otp, long ttlMillis) {
        long expiryTime = System.currentTimeMillis() + ttlMillis;
        String otpHash = hashOtp(otp);
        otpMap.put(key, new OtpEntry(otpHash, expiryTime));
    }

    @Override
    public void invalidateOtp(String key) {
        otpMap.remove(key);
    }

    @Override
    public void cleanupExpiredOtps() {
        long currentTime = System.currentTimeMillis();
        otpMap.entrySet().removeIf(entry -> entry.getValue().expiryTime < currentTime);
    }

    @Override
    public boolean verifyOtp(String key, String otp) {
        OtpEntry entry = otpMap.get(key);
        if (entry == null || entry.expiryTime < System.currentTimeMillis()) {
            return false;
        }
        String otpHash = hashOtp(otp);
        return otpHash.equals(entry.otpHash);
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

    public static class OtpEntry {
        public String otpHash;
        public long expiryTime;

        public OtpEntry() {
        }

        public OtpEntry(String otpHash, long expiryTime) {
            this.otpHash = otpHash;
            this.expiryTime = expiryTime;
        }
    }
}
