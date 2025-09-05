package com.recceda.core.generator;

/** A functional interface for generating an OTP of a specified length. */
public interface OtpGenerator {
  /**
   * Generates a new OTP.
   *
   * @param length the length of the OTP to generate.
   * @return the generated OTP as a string.
   */
  String generateOtp(int length);
}
