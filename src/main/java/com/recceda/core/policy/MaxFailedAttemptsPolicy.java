package com.recceda.core.policy;

import com.recceda.OtpEntry;
import com.recceda.core.store.OtpStore;
import com.recceda.exception.OtpGenerationException;

/**
 * A policy that prevents OTP generation if the user has exceeded the maximum number of failed
 * verification attempts.
 */
public class MaxFailedAttemptsPolicy implements Policy {

  private final int maxAttempts;

  /**
   * Creates a new {@code MaxFailedAttemptsPolicy} with the specified maximum number of attempts.
   *
   * @param maxAttempts the maximum number of failed attempts allowed.
   */
  public MaxFailedAttemptsPolicy(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  @Override
  public void check(String key, OtpStore store) {
    OtpEntry entry = store.getOtpEntry(key);
    if (entry != null && entry.getFailedAttempts() >= maxAttempts) {
      throw new OtpGenerationException("OTP generation failed due to a policy violation.");
    }
  }
}
