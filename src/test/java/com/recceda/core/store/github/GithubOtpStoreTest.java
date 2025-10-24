package com.recceda.core.store.github;

import org.junit.jupiter.api.Test;

import java.util.Random;

class GithubOtpStoreTest {

    @Test
    void e2eTest() throws Exception {
        String str = new Random().toString().toLowerCase();
        String token = System.getenv("API_TOKEN");
        GithubOtpStore otpStore = new GithubOtpStore(token, str);

        otpStore.storeOtp("test-user", "123456", 300000L);
        boolean isCorrect = otpStore.verifyOtp("test-user", "123456");
        assert isCorrect;

        otpStore.invalidateOtp("test-user");

        isCorrect = otpStore.verifyOtp("test-user", "123456");
        assert !isCorrect;


    }
}