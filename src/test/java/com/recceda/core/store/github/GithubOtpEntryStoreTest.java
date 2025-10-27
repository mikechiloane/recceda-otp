package com.recceda.core.store.github;

import com.recceda.OtpConfig;
import com.recceda.ReccedaOtp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

class GithubOtpEntryStoreTest {

    // This is an end-to-end test that requires a valid GitHub API token with repository creation and deletion permissions.
    // To run this test, you must set the API_TOKEN environment variable.
    @Test
    void e2eWithReccedaOtp() throws Exception {
        String repoName = "astro-" + new Random().nextInt(1000);
        // This test requires a real GitHub API token.
        GithubOtpStore githubOtpStore = new GithubOtpStore(System.getenv("API_TOKEN"), repoName);
        ReccedaOtp reccedaOtp = new ReccedaOtp(githubOtpStore);

        AtomicReference<String> otp1 = new AtomicReference<>();
        AtomicReference<String> key1 = new AtomicReference<>();

        CompletableFuture<Void> future1 = reccedaOtp.generateOtp("test-user-1", OtpConfig.builder().build(), (k, o) -> {
            otp1.set(o);
            key1.set(k);
            return CompletableFuture.completedFuture(null);
        });

        future1.get();

        Assertions.assertTrue(reccedaOtp.verifyOtp(key1.get(), otp1.get()).get(), "The first OTP should be valid.");

        AtomicReference<String> otp2 = new AtomicReference<>();
        AtomicReference<String> key2 = new AtomicReference<>();

        CompletableFuture<Void> future2 = reccedaOtp.generateOtp("test-user-2", OtpConfig.builder().ttlMillis(1).build(), (k, o) -> {
            otp2.set(o);
            key2.set(k);
            return CompletableFuture.completedFuture(null);
        });

        future2.get();
        Thread.sleep(10); // wait for otp to expire

        Assertions.assertFalse(reccedaOtp.verifyOtp(key2.get(), otp2.get()).get(), "The second OTP should have expired.");

        // Clean up the repository
        githubOtpStore.destroy();
    }

    // This is an end-to-end test that requires a valid GitHub API token with repository creation and deletion permissions.
    // To run this test, you must set the API_TOKEN environment variable.
    @Test
    void e2eTest() throws Exception {
        String repoName = "astro-" + new Random().nextInt(1000);
        String token = System.getenv("API_TOKEN");

        // This test requires a real GitHub API token.
        GithubOtpStore otpStore = new GithubOtpStore(token, repoName);

        otpStore.storeOtp("test-user", "123456", 300000L).get();

        Assertions.assertTrue(otpStore.verifyOtp("test-user", "123456").get(), "The OTP should be valid.");
        Assertions.assertFalse(otpStore.verifyOtp("test-user", "654321").get(), "The OTP should be invalid.");

        otpStore.invalidateOtp("test-user").get();

        Assertions.assertFalse(otpStore.verifyOtp("test-user", "123456").get(), "The OTP should be invalid after invalidation.");

        // Clean up the repository
        otpStore.destroy();
    }
}