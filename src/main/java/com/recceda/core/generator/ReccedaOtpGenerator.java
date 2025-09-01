package com.recceda.core.generator;

import java.security.SecureRandom;

public class ReccedaOtpGenerator implements OtpGenerator
{
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateOtp(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int digit = this.secureRandom.nextInt(10);
            otp.append(digit);
        }
        return otp.toString();
    }


}
