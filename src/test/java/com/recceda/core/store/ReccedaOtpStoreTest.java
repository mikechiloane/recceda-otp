package com.recceda.core.store;

import static org.junit.jupiter.api.Assertions.*;

import com.recceda.core.reason.OtpReason;
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
    otpStore.storeOtp("testKey", "123456", 1000, OtpReason.LOGIN);
    assertTrue(otpStore.verifyOtp("testKey", "123456", OtpReason.LOGIN));
  }

  @Test
  void testVerifyIncorrectOtpIncrementsFailedAttempts() {
    otpStore.storeOtp("testKey", "123456", 1000, OtpReason.LOGIN);
    assertFalse(otpStore.verifyOtp("testKey", "654321", OtpReason.LOGIN));

    OtpEntry entry = otpStore.getOtpEntry("testKey", OtpReason.LOGIN);
    assertEquals(1, entry.failedAttempts);

    assertFalse(otpStore.verifyOtp("testKey", "000000", OtpReason.LOGIN));
    entry = otpStore.getOtpEntry("testKey", OtpReason.LOGIN);
    assertEquals(2, entry.failedAttempts);
  }

  @Test
  void testStoreNewOtpResetsFailedAttempts() {
    otpStore.storeOtp("testKey", "123456", 1000, OtpReason.LOGIN);
    otpStore.verifyOtp("testKey", "654321", OtpReason.LOGIN); // Fail once

    otpStore.storeOtp("testKey", "new-otp", 1000, OtpReason.LOGIN);
    OtpEntry entry = otpStore.getOtpEntry("testKey", OtpReason.LOGIN);
    assertEquals(0, entry.failedAttempts);
  }

  @Test
  void testVerifyExpiredOtp() throws InterruptedException {
    otpStore.storeOtp("testKey", "123456", 1, OtpReason.LOGIN);
    Thread.sleep(10);
    assertFalse(otpStore.verifyOtp("testKey", "123456", OtpReason.LOGIN));
  }

  @Test
  void testInvalidateOtp() {
    otpStore.storeOtp("testKey", "123456", 1000, OtpReason.LOGIN);
    otpStore.invalidateOtp("testKey", OtpReason.LOGIN);
    assertFalse(otpStore.verifyOtp("testKey", "123456", OtpReason.LOGIN));
  }
}
