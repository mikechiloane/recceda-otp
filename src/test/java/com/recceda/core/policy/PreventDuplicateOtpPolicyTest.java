package com.recceda.core.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.recceda.core.store.OtpStore;
import com.recceda.core.store.ReccedaOtpStore.OtpEntry;
import com.recceda.exception.OtpGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PreventDuplicateOtpPolicyTest {

  @Mock private OtpStore otpStore;

  private final PreventDuplicateOtpPolicy policy = new PreventDuplicateOtpPolicy();

  @Test
  void shouldThrowExceptionWhenActiveOtpExists() {
    // Given
    String key = "test-user";
    OtpEntry activeEntry = new OtpEntry("hash", System.currentTimeMillis() + 10000);
    when(otpStore.getOtpEntry(key)).thenReturn(activeEntry);

    // Then
    assertThrows(
        OtpGenerationException.class,
        () -> {
          policy.check(key, otpStore);
        });
  }

  @Test
  void shouldNotThrowExceptionWhenNoActiveOtpExists() {
    // Given
    String key = "test-user";
    when(otpStore.getOtpEntry(key)).thenReturn(null);

    // Then
    assertDoesNotThrow(
        () -> {
          policy.check(key, otpStore);
        });
  }

  @Test
  void shouldNotThrowExceptionWhenOtpIsExpired() {
    // Given
    String key = "test-user";
    OtpEntry expiredEntry = new OtpEntry("hash", System.currentTimeMillis() - 10000);
    when(otpStore.getOtpEntry(key)).thenReturn(expiredEntry);

    // Then
    assertDoesNotThrow(
        () -> {
          policy.check(key, otpStore);
        });
  }
}
