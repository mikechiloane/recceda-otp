package com.recceda.core.policy;

import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.OtpStore;
import com.recceda.core.store.ReccedaOtpStore.OtpEntry;
import com.recceda.exception.OtpGenerationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventDuplicateOtpPolicyTest {

    @Mock
    private OtpStore otpStore;

    private final PreventDuplicateOtpPolicy policy = new PreventDuplicateOtpPolicy();

    @Test
    void shouldThrowExceptionWhenActiveOtpExists() {
        // Given
        String key = "test-user";
        OtpReason reason = OtpReason.LOGIN;
        OtpEntry activeEntry = new OtpEntry("hash", System.currentTimeMillis() + 10000, reason);
        when(otpStore.getOtpEntry(key, reason)).thenReturn(activeEntry);

        // Then
        assertThrows(OtpGenerationException.class, () -> {
            policy.check(key, reason, otpStore);
        });
    }

    @Test
    void shouldNotThrowExceptionWhenNoActiveOtpExists() {
        // Given
        String key = "test-user";
        OtpReason reason = OtpReason.LOGIN;
        when(otpStore.getOtpEntry(key, reason)).thenReturn(null);

        // Then
        assertDoesNotThrow(() -> {
            policy.check(key, reason, otpStore);
        });
    }

    @Test
    void shouldNotThrowExceptionWhenOtpIsExpired() {
        // Given
        String key = "test-user";
        OtpReason reason = OtpReason.LOGIN;
        OtpEntry expiredEntry = new OtpEntry("hash", System.currentTimeMillis() - 10000, reason);
        when(otpStore.getOtpEntry(key, reason)).thenReturn(expiredEntry);

        // Then
        assertDoesNotThrow(() -> {
            policy.check(key, reason, otpStore);
        });
    }
}
