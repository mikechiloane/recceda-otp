package com.recceda.core.policy;

import com.recceda.core.store.OtpStore;
import com.recceda.exception.OtpGenerationException;

/**
 * A functional interface for defining a policy that must be met before an OTP can be generated.
 *
 * <p>Implement this interface to create custom rules for OTP generation, such as preventing
 * duplicate OTPs or limiting failed attempts.
 */
@FunctionalInterface
public interface Policy {
  /**
   * Checks if the policy is met for the given key.
   *
   * @param key the unique key associated with the OTP (e.g., user ID, email address).
   * @param store the OTP store, which can be used to check for existing OTPs or other relevant
   *     data.
   * @throws OtpGenerationException if the policy check fails.
   */
  void check(String key, OtpStore store);
}
