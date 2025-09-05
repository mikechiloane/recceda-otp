package com.recceda.core.reason;

/**
 * An enum representing the reason for generating an OTP.
 *
 * <p>Using a reason allows for better auditing and enables different policies to be applied based on
 * the context. A user can have multiple active OTPs as long as they are for different reasons.
 */
public enum OtpReason {
  /** For user login. */
  LOGIN,
  /** For new user signup. */
  SIGNUP,
  /** For resetting a user's password. */
  RESET_PASSWORD,
  /** For resending an OTP. */
  RESEND
}
