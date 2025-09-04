package com.recceda.core.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.OtpStore;
import com.recceda.core.store.ReccedaOtpStore.OtpEntry;
import com.recceda.exception.OtpGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaxFailedAttemptsPolicyTest {

  @Mock private OtpStore otpStore;

  @Test
  void shouldThrowExceptionWhenMaxAttemptsExceeded() {
    // Given
    String key = "test-user";
    OtpReason reason = OtpReason.LOGIN;
    MaxFailedAttemptsPolicy policy = new MaxFailedAttemptsPolicy(3);
    OtpEntry entry = new OtpEntry("hash", System.currentTimeMillis() + 10000, reason);
    entry.failedAttempts = 3;
    when(otpStore.getOtpEntry(key, reason)).thenReturn(entry);

    // Then
    assertThrows(
        OtpGenerationException.class,
        () -> {
          policy.check(key, reason, otpStore);
        });
  }

  @Test
  void shouldNotThrowExceptionWhenMaxAttemptsNotExceeded() {
    // Given
    String key = "test-user";
    OtpReason reason = OtpReason.LOGIN;
    MaxFailedAttemptsPolicy policy = new MaxFailedAttemptsPolicy(3);
    OtpEntry entry = new OtpEntry("hash", System.currentTimeMillis() + 10000, reason);
    entry.failedAttempts = 2;
    when(otpStore.getOtpEntry(key, reason)).thenReturn(entry);

    // Then
    assertDoesNotThrow(
        () -> {
          policy.check(key, reason, otpStore);
        });
  }

  @Test
  void shouldNotThrowExceptionWhenNoEntryExists() {
    // Given
    String key = "test-user";
    OtpReason reason = OtpReason.LOGIN;
    MaxFailedAttemptsPolicy policy = new MaxFailedAttemptsPolicy(3);
    when(otpStore.getOtpEntry(key, reason)).thenReturn(null);

    // Then
    assertDoesNotThrow(
        () -> {
          policy.check(key, reason, otpStore);
        });
  }
}
