package com.recceda.exception;

/** An exception that is thrown when an OTP cannot be generated due to a policy violation. */
public class OtpGenerationException extends RuntimeException {
  public OtpGenerationException(String message) {
    super(message);
  }
}
