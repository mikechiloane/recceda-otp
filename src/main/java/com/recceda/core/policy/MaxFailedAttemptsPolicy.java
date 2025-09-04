package com.recceda.core.policy;

import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.OtpStore;
import com.recceda.core.store.ReccedaOtpStore.OtpEntry;
import com.recceda.exception.OtpGenerationException;

public class MaxFailedAttemptsPolicy implements GenerationPolicy {

  private final int maxAttempts;

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
