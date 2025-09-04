package com.recceda.core.policy;

import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.OtpStore;

@FunctionalInterface
public interface GenerationPolicy {
  void check(String key, OtpReason reason, OtpStore store);
}
