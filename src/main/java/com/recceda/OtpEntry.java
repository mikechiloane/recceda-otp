package com.recceda;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Getter
public class OtpEntry {

  private String key;
  private String otpHash;
  private long expiryTime;
  @Setter private int failedAttempts;
}
