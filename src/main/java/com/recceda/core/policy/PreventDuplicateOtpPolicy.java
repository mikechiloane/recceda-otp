package com.recceda.core.policy;

import com.recceda.core.store.OtpStore;
import com.recceda.exception.OtpGenerationException;

import java.util.concurrent.CompletableFuture;

/**
 * A policy that prevents the generation of a new OTP if the user already has an active
 * (non-expired) one.
 */
public class PreventDuplicateOtpPolicy implements Policy {

  @Override
  public CompletableFuture<Void> check(String key, OtpStore store) {
    return store
        .getOtpEntry(key)
        .thenAccept(
            entry -> {
              if (entry != null && entry.getExpiryTime() > System.currentTimeMillis()) {
                throw new OtpGenerationException("OTP generation failed due to a policy violation.");
              }
            });
  }
}
