package com.recceda.core.store;

import static org.junit.jupiter.api.Assertions.*;

import com.recceda.OtpEntry;
import com.recceda.core.store.reccedda.ReccedaOtpStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReccedaOtpEntryStoreTest {

  private OtpStore otpStore;

  @BeforeEach
  void setUp() {
    otpStore = new ReccedaOtpStore();
  }

  @Test
  void testStoreAndVerifyOtp() {
    otpStore.storeOtp("testKey", "123456", 1000);
    assertTrue(otpStore.verifyOtp("testKey", "123456").join());
  }

  @Test
  void testVerifyIncorrectOtpIncrementsFailedAttempts() {
    otpStore.storeOtp("testKey", "123456", 1000);
    assertFalse(otpStore.verifyOtp("testKey", "654321").join());

    OtpEntry entry = otpStore.getOtpEntry("testKey").join();
    assertEquals(1, entry.getFailedAttempts());

    assertFalse(otpStore.verifyOtp("testKey", "000000").join());
    entry = otpStore.getOtpEntry("testKey").join();
    assertEquals(2, entry.getFailedAttempts());
  }

  @Test
  void testStoreNewOtpResetsFailedAttempts() {
    otpStore.storeOtp("testKey", "123456", 1000);
    otpStore.verifyOtp("testKey", "654321"); // Fail once

    otpStore.storeOtp("testKey", "new-otp", 1000);
    OtpEntry entry = otpStore.getOtpEntry("testKey").join();
    assertEquals(0, entry.getFailedAttempts());
  }

  @Test
  void testVerifyExpiredOtp() throws InterruptedException {
    otpStore.storeOtp("testKey", "123456", 1);
    Thread.sleep(10);
    assertFalse(otpStore.verifyOtp("testKey", "123456").join());
  }

  @Test
  void testInvalidateOtp() {
    otpStore.storeOtp("testKey", "123456", 1000);
    otpStore.invalidateOtp("testKey");
    assertFalse(otpStore.verifyOtp("testKey", "123456").join());
  }
}
