package com.recceda.core.policy;

import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.OtpStore;
import com.recceda.core.store.ReccedaOtpStore.OtpEntry;
import com.recceda.exception.OtpGenerationException;

/**
 * A policy that prevents the generation of a new OTP if the user already has an active (non-expired)
 * one for the same reason.
 */
public class PreventDuplicateOtpPolicy implements Policy {

  @Override
  public void check(String key, OtpReason reason, OtpStore store) {
    OtpEntry entry = store.getOtpEntry(key, reason);
    if (entry != null && entry.expiryTime > System.currentTimeMillis()) {
      throw new OtpGenerationException("An active OTP already exists for this user and reason.");
    }
  }
}
