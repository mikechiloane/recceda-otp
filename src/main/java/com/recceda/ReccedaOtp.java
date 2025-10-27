package com.recceda;

import com.recceda.core.distributor.OtpDistributor;
import com.recceda.core.generator.OtpGenerator;
import com.recceda.core.generator.ReccedaOtpGenerator;
import com.recceda.core.policy.Policy;
import com.recceda.core.store.OtpStore;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The main class for generating and verifying OTPs.
 *
 * <p>This class orchestrates the OTP generation and verification process by coordinating the {@link
 * OtpGenerator}, {@link OtpStore}, and a list of {@link Policy} implementations.
 */
public class ReccedaOtp {

  private final OtpGenerator otpGenerator;
  private final OtpStore otpStore;
  private final List<Policy> policies;
  private static final int MIN_OTP_LENGTH = 6;

  /**
   * Creates a new {@code ReccedaOtp} with the default OTP generator and no policies.
   *
   * @param otpStore the OTP store to use.
   */
  public ReccedaOtp(OtpStore otpStore) {
    this(new ReccedaOtpGenerator(), otpStore, Collections.emptyList());
  }

  /**
   * Creates a new {@code ReccedaOtp} with the default OTP generator.
   *
   * @param otpStore the OTP store to use.
   * @param policies the list of policies to apply before generating an OTP.
   */
  public ReccedaOtp(OtpStore otpStore, List<Policy> policies) {
    this(new ReccedaOtpGenerator(), otpStore, policies);
  }

  /**
   * Creates a new {@code ReccedaOtp} with a custom OTP generator and a list of policies.
   *
   * @param otpGenerator the OTP generator to use.
   * @param otpStore the OTP store to use.
   * @param policies the list of policies to apply before generating an OTP.
   */
  public ReccedaOtp(OtpGenerator otpGenerator, OtpStore otpStore, List<Policy> policies) {
    this.otpGenerator = otpGenerator;
    this.otpStore = otpStore;
    this.policies = policies;
  }

  /**
   * Generates a new OTP with default settings and sends it to the user via the provided
   * distributor.
   *
   * @param key the unique key to associate with the OTP (e.g., user ID, email address).
   * @param distributor the distributor to use for sending the OTP.
   * @return a {@link CompletableFuture} that completes when the OTP has been sent.
   */
  public CompletableFuture<Void> generateOtp(String key, OtpDistributor distributor) {
    return generateOtp(key, OtpConfig.builder().build(), distributor);
  }

  /**
   * Generates a new OTP with the specified configuration and sends it to the user via the provided
   * distributor.
   *
   * @param key the unique key to associate with the OTP (e.g., user ID, email address).
   * @param config the OTP configuration.
   * @param distributor the distributor to use for sending the OTP.
   * @return a {@link CompletableFuture} that completes when the OTP has been sent.
   */
  public CompletableFuture<Void> generateOtp(
      String key, OtpConfig config, OtpDistributor distributor) {
    if (config.getLength() < MIN_OTP_LENGTH) {
      throw new IllegalArgumentException("OTP length must be at least " + MIN_OTP_LENGTH);
    }

    CompletableFuture<Void> policyChecks =
        CompletableFuture.allOf(
            policies.stream()
                .map(policy -> policy.check(key, otpStore))
                .toArray(CompletableFuture[]::new));

    return policyChecks.thenCompose(
        v -> {
          String otp = otpGenerator.generateOtp(config.getLength());
          return otpStore
              .storeOtp(key, otp, config.getTtlMillis())
              .thenCompose(v2 -> distributor.send(key, otp));
        });
  }

  /**
   * Verifies the given OTP for the specified key.
   *
   * @param key the unique key associated with the OTP.
   * @param otp the OTP to verify.
   * @return a {@link CompletableFuture} that completes with {@code true} if the OTP is valid, or
   *     {@code false} otherwise.
   */
  public CompletableFuture<Boolean> verifyOtp(String key, String otp) {
    return otpStore.verifyOtp(key, otp);
  }

  /**
   * Invalidates the current OTP for the specified key.
   *
   * @param key the unique key associated with the OTP.
   * @return a {@link CompletableFuture} that completes when the OTP has been invalidated.
   */
  public CompletableFuture<Void> invalidateOtp(String key) {
    return otpStore.invalidateOtp(key);
  }
}
