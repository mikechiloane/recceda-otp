package com.recceda.core.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.recceda.OtpEntry;
import com.recceda.core.store.OtpStore;
import com.recceda.exception.OtpGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
class MaxFailedAttemptsPolicyTest {

  @Mock private OtpStore otpStore;

  @Test
  void shouldThrowExceptionWhenMaxAttemptsExceeded() {
    // Given
    String key = "test-user";
    MaxFailedAttemptsPolicy policy = new MaxFailedAttemptsPolicy(3);
    OtpEntry entry = OtpEntry.builder().key(key).failedAttempts(3).expiryTime(1000).build();

    when(otpStore.getOtpEntry(key)).thenReturn(CompletableFuture.completedFuture(entry));

    // Then
    assertThrows(
        OtpGenerationException.class,
        () -> {
          policy.check(key, otpStore);
        });
  }

  @Test
  void shouldNotThrowExceptionWhenMaxAttemptsNotExceeded() {
    // Given
    String key = "test-user";
    MaxFailedAttemptsPolicy policy = new MaxFailedAttemptsPolicy(3);
    OtpEntry entry = OtpEntry.builder().key(key).failedAttempts(2).expiryTime(1000).build();

    when(otpStore.getOtpEntry(key)).thenReturn(CompletableFuture.completedFuture(entry));

    // Then
    assertDoesNotThrow(
        () -> {
          policy.check(key, otpStore);
        });
  }

  @Test
  void shouldNotThrowExceptionWhenNoEntryExists() {
    // Given
    String key = "test-user";
    MaxFailedAttemptsPolicy policy = new MaxFailedAttemptsPolicy(3);
    when(otpStore.getOtpEntry(key)).thenReturn(null);

    // Then
    assertDoesNotThrow(
        () -> {
          policy.check(key, otpStore);
        });
  }
}
