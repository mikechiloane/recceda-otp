package com.recceda;

import com.recceda.core.distributor.OtpDistributor;
import com.recceda.core.generator.OtpGenerator;
import com.recceda.core.generator.ReccedaOtpGenerator;
import com.recceda.core.policy.Policy;
import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.OtpStore;
import java.util.Collections;
import java.util.List;

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
   * Generates a new OTP with default settings (6 digits, 5-minute validity) and sends it to the
   * user via the provided distributor.
   *
   * @param key the unique key to associate with the OTP (e.g., user ID, email address).
   * @param reason the reason for generating the OTP.
   * @param distributor the distributor to use for sending the OTP.
   */
  public void generateOtp(String key, OtpReason reason, OtpDistributor distributor) {
    // Default to a 6-digit OTP with a 5-minute validity
    generateOtp(key, reason, 6, 5 * 60 * 1000, distributor);
  }

  /**
   * Generates a new OTP with the specified length and time-to-live (TTL) and sends it to the user
   * via the provided distributor.
   *
   * @param key the unique key to associate with the OTP (e.g., user ID, email address).
   * @param reason the reason for generating the OTP.
   * @param length the length of the OTP to generate.
   * @param ttlMillis the time-to-live for the OTP in milliseconds.
   * @param distributor the distributor to use for sending the OTP.
   */
  public void generateOtp(
      String key, OtpReason reason, int length, long ttlMillis, OtpDistributor distributor) {
    for (Policy policy : policies) {
      policy.check(key, reason, otpStore);
    }

    String otp = otpGenerator.generateOtp(length);
    otpStore.storeOtp(key, otp, ttlMillis, reason);
    distributor.send(key, otp);
  }

  /**
   * Verifies the given OTP for the specified key and reason.
   *
   * @param key the unique key associated with the OTP.
   * @param otp the OTP to verify.
   * @param reason the reason for which the OTP was generated.
   * @return {@code true} if the OTP is valid, {@code false} otherwise.
   */
  public boolean verifyOtp(String key, String otp, OtpReason reason) {
    return otpStore.verifyOtp(key, otp, reason);
  }

  /**
   * Invalidates the current OTP for the specified key and reason.
   *
   * @param key the unique key associated with the OTP.
   * @param reason the reason for which the OTP was generated.
   */
  public void invalidateOtp(String key, OtpReason reason) {
    otpStore.invalidateOtp(key, reason);
  }
}
