package com.recceda.core.policy;

import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.OtpStore;
import com.recceda.core.store.ReccedaOtpStore.OtpEntry;
import com.recceda.exception.OtpGenerationException;

/**
 * A policy that prevents OTP generation if the user has exceeded the maximum number of failed
 * verification attempts for a given reason.
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
  public void check(String key, OtpReason reason, OtpStore store) {
    OtpEntry entry = store.getOtpEntry(key, reason);
    if (entry != null && entry.failedAttempts >= maxAttempts) {
      throw new OtpGenerationException(
          "User has exceeded the maximum number of failed OTP attempts for this reason.");
    }
  }
}
