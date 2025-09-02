package com.recceda.core.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReccedaOtpStoreTest {

    private OtpStore otpStore;

    @BeforeEach
    void setUp() {
        otpStore = new ReccedaOtpStore();
    }

    @Test
    void testStoreAndVerifyOtp() {
        otpStore.storeOtp("testKey", "123456", 1000);
        assertTrue(otpStore.verifyOtp("testKey", "123456"));
    }

    @Test
    void testVerifyIncorrectOtp() {
        otpStore.storeOtp("testKey", "123456", 1000);
        assertFalse(otpStore.verifyOtp("testKey", "654321"));
    }

    @Test
    void testVerifyExpiredOtp() throws InterruptedException {
        otpStore.storeOtp("testKey", "123456", 1);
        Thread.sleep(10);
        assertFalse(otpStore.verifyOtp("testKey", "123456"));
    }

    @Test
    void testInvalidateOtp() {
        otpStore.storeOtp("testKey", "123456", 1000);
        otpStore.invalidateOtp("testKey");
        assertFalse(otpStore.verifyOtp("testKey", "123456"));
    }

    @Test
    void testCleanupExpiredOtps() throws InterruptedException {
        otpStore.storeOtp("key1", "111111", 1);
        otpStore.storeOtp("key2", "222222", 10000);
        Thread.sleep(10);
        otpStore.cleanupExpiredOtps();
        assertFalse(otpStore.verifyOtp("key1", "111111"));
        assertTrue(otpStore.verifyOtp("key2", "222222"));
    }
}
