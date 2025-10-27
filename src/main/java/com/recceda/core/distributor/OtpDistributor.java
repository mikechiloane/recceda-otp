package com.recceda.core.distributor;

import java.util.concurrent.CompletableFuture;

/**
 * A functional interface for sending an OTP to a user.
 *
 * <p>Implement this interface to define how the generated OTP should be delivered, such as by SMS,
 * email, or another method.
 */
@FunctionalInterface
public interface OtpDistributor {
  /**
   * Sends the OTP to the user associated with the given key.
   *
   * @param key the unique key associated with the user (e.g., user ID, email address).
   * @param otp the OTP to send.
   */
  CompletableFuture<Void> send(String key, String otp);
}
