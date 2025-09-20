package com.recceda.core.store;

import com.recceda.core.store.ReccedaOtpStore.OtpEntry;

/**
 * An interface for storing, verifying, and managing OTPs.
 *
 * <p>Implement this interface to provide a custom storage mechanism for OTPs, such as a database or
 * a distributed cache.
 */
public interface OtpStore {
  /**
   * Stores a new OTP for the given key.
   *
   * @param key the unique key to associate with the OTP (e.g., user ID, email address).
   * @param otp the OTP to store.
   * @param ttlMillis the time-to-live for the OTP in milliseconds.
   */
  void storeOtp(String key, String otp, long ttlMillis);

  /**
   * Verifies the given OTP for the specified key.
   *
   * @param key the unique key associated with the OTP.
   * @param otp the OTP to verify.
   * @return {@code true} if the OTP is valid, {@code false} otherwise.
   */
  boolean verifyOtp(String key, String otp);

  /**
   * Retrieves the OTP entry for the specified key.
   *
   * @param key the unique key associated with the OTP.
   * @return the {@link OtpEntry} if it exists, or {@code null} otherwise.
   */
  OtpEntry getOtpEntry(String key);

  /**
   * Invalidates the current OTP for the specified key.
   *
   * @param key the unique key associated with the OTP.
   */
  void invalidateOtp(String key);
}
