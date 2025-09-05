package com.recceda.core.store;

import com.recceda.core.reason.OtpReason;
import com.recceda.core.store.ReccedaOtpStore.OtpEntry;

/**
 * An interface for storing, verifying, and managing OTPs.
 *
 * <p>Implement this interface to provide a custom storage mechanism for OTPs, such as a database or a
 * distributed cache.
 */
public interface OtpStore {
  /**
   * Stores a new OTP for the given key and reason.
   *
   * @param key the unique key to associate with the OTP (e.g., user ID, email address).
   * @param otp the OTP to store.
   * @param ttlMillis the time-to-live for the OTP in milliseconds.
   * @param reason the reason for generating the OTP.
   */
  void storeOtp(String key, String otp, long ttlMillis, OtpReason reason);

  /**
   * Verifies the given OTP for the specified key and reason.
   *
   * @param key the unique key associated with the OTP.
   * @param otp the OTP to verify.
   * @param reason the reason for which the OTP was generated.
   * @return {@code true} if the OTP is valid, {@code false} otherwise.
   */
  boolean verifyOtp(String key, String otp, OtpReason reason);

  /**
   * Retrieves the OTP entry for the specified key and reason.
   *
   * @param key the unique key associated with the OTP.
   * @param reason the reason for which the OTP was generated.
   * @return the {@link OtpEntry} if it exists, or {@code null} otherwise.
   */
  OtpEntry getOtpEntry(String key, OtpReason reason);

  /**
   * Invalidates the current OTP for the specified key and reason.
   *
   * @param key the unique key associated with the OTP.
   * @param reason the reason for which the OTP was generated.
   */
  void invalidateOtp(String key, OtpReason reason);
}
