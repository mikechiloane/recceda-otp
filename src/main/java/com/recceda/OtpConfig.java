package com.recceda;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OtpConfig {

  @Builder.Default private int length = 6;

  @Builder.Default private long ttlMillis = 5 * 60 * 1000; // 5 minutes
}
