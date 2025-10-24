package com.recceda.core.policy;

import com.recceda.core.store.OtpStore;
import com.recceda.core.store.reccedda.ReccedaOtpStore.Otp;
import com.recceda.exception.OtpGenerationException;

/**
 * A policy that prevents the generation of a new OTP if the user already has an active
 * (non-expired) one.
 */
public class PreventDuplicateOtpPolicy implements Policy {

  @Override
  public void check(String key, OtpStore store) {
    Otp entry = store.getOtpEntry(key);
    if (entry != null && entry.expiryTime > System.currentTimeMillis()) {
      throw new OtpGenerationException("An active OTP already exists for this user.");
    }
  }
}
