package com.recceda;

import com.recceda.core.distributor.OtpDistributor;
import com.recceda.core.generator.OtpGenerator;
import com.recceda.core.generator.ReccedaOtpGenerator;
import com.recceda.core.policy.GenerationPolicy;
import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.OtpStore;

import java.util.Collections;
import java.util.List;

public class ReccedaOtp {

    private final OtpGenerator otpGenerator;
    private final OtpStore otpStore;
    private final List<GenerationPolicy> policies;

    public ReccedaOtp(OtpStore otpStore) {
        this(new ReccedaOtpGenerator(), otpStore, Collections.emptyList());
    }

    public ReccedaOtp(OtpStore otpStore, List<GenerationPolicy> policies) {
        this(new ReccedaOtpGenerator(), otpStore, policies);
    }

    public ReccedaOtp(OtpGenerator otpGenerator, OtpStore otpStore, List<GenerationPolicy> policies) {
        this.otpGenerator = otpGenerator;
        this.otpStore = otpStore;
        this.policies = policies;
    }

    public void generateOtp(String key, OtpReason reason, OtpDistributor distributor) {
        // Default to a 6-digit OTP with a 5-minute validity
        generateOtp(key, reason, 6, 5 * 60 * 1000, distributor);
    }

    public void generateOtp(String key, OtpReason reason, int length, long ttlMillis, OtpDistributor distributor) {
        for (GenerationPolicy policy : policies) {
            policy.check(key, reason, otpStore);
        }

        String otp = otpGenerator.generateOtp(length);
        otpStore.storeOtp(key, otp, ttlMillis, reason);
        distributor.send(key, otp);
    }

    public boolean verifyOtp(String key, String otp, OtpReason reason) {
        return otpStore.verifyOtp(key, otp, reason);
    }

    public void invalidateOtp(String key, OtpReason reason) {
        otpStore.invalidateOtp(key, reason);
    }
}

