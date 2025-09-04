package com.recceda.core.distributor;

@FunctionalInterface
public interface OtpDistributor {
  void send(String key, String otp);
}
