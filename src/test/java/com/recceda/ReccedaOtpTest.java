package com.recceda;

import com.recceda.core.distributor.OtpDistributor;
import com.recceda.core.generator.OtpGenerator;
import com.recceda.core.store.OtpStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReccedaOtpTest {

    @Mock
    private OtpGenerator otpGenerator;

    @Mock
    private OtpStore otpStore;

    @InjectMocks
    private ReccedaOtp reccedaOtp;

    @Test
    void generateOtpShouldCallGeneratorAndStoreAndDistributor() {
        // Given
        String key = "test-user";
        String generatedOtp = "123456";
        OtpDistributor distributor = (k, o) -> {};
        when(otpGenerator.generateOtp(6)).thenReturn(generatedOtp);

        // When
        reccedaOtp.generateOtp(key, distributor);

        // Then
        verify(otpGenerator).generateOtp(6);
        verify(otpStore).storeOtp(key, generatedOtp, 300000L);
    }

    @Test
    void generateOtpWithLengthAndTtlShouldCallGeneratorAndStore() {
        // Given
        String key = "test-user";
        String generatedOtp = "98765432";
        OtpDistributor distributor = (k, o) -> {};
        when(otpGenerator.generateOtp(8)).thenReturn(generatedOtp);

        // When
        reccedaOtp.generateOtp(key, 8, 600000L, distributor);

        // Then
        verify(otpGenerator).generateOtp(8);
        verify(otpStore).storeOtp(key, generatedOtp, 600000L);
    }

    @Test
    void verifyOtpShouldCallStore() {
        // Given
        String key = "test-user";
        String otp = "123456";
        when(otpStore.verifyOtp(key, otp)).thenReturn(true);

        // When
        boolean result = reccedaOtp.verifyOtp(key, otp);

        // Then
        assertTrue(result);
        verify(otpStore).verifyOtp(key, otp);
    }

    @Test
    void invalidateOtpShouldCallStore() {
        // Given
        String key = "test-user";

        // When
        reccedaOtp.invalidateOtp(key);

        // Then
        verify(otpStore).invalidateOtp(key);
    }
}
