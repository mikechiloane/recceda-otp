package com.recceda;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.recceda.core.distributor.OtpDistributor;
import com.recceda.core.generator.OtpGenerator;
import com.recceda.core.policy.Policy;
import com.recceda.core.store.OtpStore;
import com.recceda.exception.OtpGenerationException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReccedaOtpTest {

  @Mock private OtpGenerator otpGenerator;

  @Mock private OtpStore otpStore;

  private ReccedaOtp reccedaOtp;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void generateOtpShouldCallGeneratorAndStoreAndDistributor() {
    // Given
    reccedaOtp = new ReccedaOtp(otpGenerator, otpStore, Collections.emptyList());
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
  void generateOtpShouldCheckPolicies() {
    // Given
    Policy policy = mock(Policy.class);
    reccedaOtp = new ReccedaOtp(otpGenerator, otpStore, Arrays.asList(policy));
    String key = "test-user";
    OtpDistributor distributor = (k, o) -> {};

    // When
    reccedaOtp.generateOtp(key, distributor);

    // Then
    verify(policy).check(key, otpStore);
  }

  @Test
  void generateOtpShouldThrowExceptionWhenPolicyFails() {
    // Given
    Policy policy = mock(Policy.class);
    doThrow(new OtpGenerationException("Policy failed")).when(policy).check(any(), any());
    reccedaOtp = new ReccedaOtp(otpGenerator, otpStore, Arrays.asList(policy));
    String key = "test-user";
    OtpDistributor distributor = (k, o) -> {};

    // Then
    assertThrows(
        OtpGenerationException.class,
        () -> {
          reccedaOtp.generateOtp(key, distributor);
        });
  }

  @Test
  void verifyOtpShouldCallStore() {
    // Given
    reccedaOtp = new ReccedaOtp(otpGenerator, otpStore, Collections.emptyList());
    String key = "test-user";
    String otp = "123456";
    when(otpStore.verifyOtp(key, otp)).thenReturn(true);

    // When
    boolean result = reccedaOtp.verifyOtp(key, otp);

    // Then
    assertTrue(result);
    verify(otpStore).verifyOtp(key, otp);
  }
}
