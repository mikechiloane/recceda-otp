package com.recceda.core.store;

import static org.junit.jupiter.api.Assertions.*;

import com.recceda.core.store.ReccedaOtpStore.OtpEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  void testVerifyIncorrectOtpIncrementsFailedAttempts() {
    otpStore.storeOtp("testKey", "123456", 1000);
    assertFalse(otpStore.verifyOtp("testKey", "654321"));

    OtpEntry entry = otpStore.getOtpEntry("testKey");
    assertEquals(1, entry.failedAttempts);

    assertFalse(otpStore.verifyOtp("testKey", "000000"));
    entry = otpStore.getOtpEntry("testKey");
    assertEquals(2, entry.failedAttempts);
  }

  @Test
  void testStoreNewOtpResetsFailedAttempts() {
    otpStore.storeOtp("testKey", "123456", 1000);
    otpStore.verifyOtp("testKey", "654321"); // Fail once

    otpStore.storeOtp("testKey", "new-otp", 1000);
    OtpEntry entry = otpStore.getOtpEntry("testKey");
    assertEquals(0, entry.failedAttempts);
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
}
