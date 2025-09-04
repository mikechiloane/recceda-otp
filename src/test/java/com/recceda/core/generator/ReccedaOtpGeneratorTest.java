package com.recceda.core.generator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReccedaOtpGeneratorTest {

  private final OtpGenerator otpGenerator = new ReccedaOtpGenerator();

  @Test
  void testGenerateOtpLength() {
    String otp = otpGenerator.generateOtp(6);
    assertEquals(6, otp.length());

    String otp8 = otpGenerator.generateOtp(8);
    assertEquals(8, otp8.length());
  }

  @Test
  void testGenerateOtpContainsOnlyDigits() {
    String otp = otpGenerator.generateOtp(10);
    assertTrue(otp.matches("^[0-9]+$"));
  }
}
