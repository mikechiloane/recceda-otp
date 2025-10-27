package com.recceda.core.store.github;

import com.recceda.ReccedaOtp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

class GithubOtpEntryStoreTest {

    @Test
    void e2eWithReccedaOtp() throws Exception {
        String str = "astro-" + String.valueOf(new Random().nextInt(1000));
        GithubOtpStore githubOtpStore = new GithubOtpStore(System.getenv("API_TOKEN"), str);
        ReccedaOtp reccedaOtp = new ReccedaOtp(githubOtpStore);
        AtomicReference<String> otp = new AtomicReference<>();
        AtomicReference<String> key = new AtomicReference<>();
        reccedaOtp.generateOtp("test-user", 10, Duration.ofMinutes(1).toMillis(), (k, o) -> {
            otp.set(o);
            key.set(k);
        });
        Assertions.assertTrue(reccedaOtp.verifyOtp(key.get(), otp.get()));
        reccedaOtp.generateOtp("test-user-2", 10, Duration.ofMillis(1).toMillis(), (k, o) -> {
            otp.set(o);
            key.set(k);
        });
        Assertions.assertFalse(reccedaOtp.verifyOtp(key.get(), otp.get()));
    }

    @Test
    void e2eTest() throws Exception {

        String str = "astro-" + String.valueOf(new Random().nextInt(1000));
        String token = System.getenv("API_TOKEN");

        GithubOtpStore otpStore = new GithubOtpStore(token, str);

        otpStore.storeOtp("test-user", "123456", 300000L);
        boolean isCorrect = otpStore.verifyOtp("test-user", "123456");
        assert isCorrect;

        isCorrect = otpStore.verifyOtp("test-use", "123456");
        assert !isCorrect;

        isCorrect = otpStore.verifyOtp("test-user", "12345");
        assert !isCorrect;

        otpStore.invalidateOtp("test-user");

        isCorrect = otpStore.verifyOtp("test-user", "123456");
        assert !isCorrect;

        otpStore.destroy();
    }
}