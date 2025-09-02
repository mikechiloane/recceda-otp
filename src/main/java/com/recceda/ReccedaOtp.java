package com.recceda;

import com.recceda.core.distributor.OtpDistributor;
import com.recceda.core.generator.OtpGenerator;
import com.recceda.core.generator.ReccedaOtpGenerator;
import com.recceda.core.store.OtpStore;

public class ReccedaOtp {

    private final OtpGenerator otpGenerator;
    private final OtpStore otpStore;

    public ReccedaOtp(OtpStore otpStore) {
        this.otpGenerator = new ReccedaOtpGenerator();
        this.otpStore = otpStore;
    }

    public ReccedaOtp(OtpGenerator otpGenerator, OtpStore otpStore) {
        this.otpGenerator = otpGenerator;
        this.otpStore = otpStore;
    }

    public void generateOtp(String key, OtpDistributor distributor) {
        generateOtp(key, 6, 5 * 60 * 1000, distributor);
    }

    public void generateOtp(String key, int length, long ttlMillis, OtpDistributor distributor) {
        String otp = otpGenerator.generateOtp(length);
        otpStore.storeOtp(key, otp, ttlMillis);
        distributor.send(key, otp);
    }

    public boolean verifyOtp(String key, String otp) {
        return otpStore.verifyOtp(key, otp);
    }

    public void invalidateOtp(String key) {
        otpStore.invalidateOtp(key);
    }
}
