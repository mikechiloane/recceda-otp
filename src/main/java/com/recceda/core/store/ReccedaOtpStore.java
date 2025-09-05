package com.recceda.core.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.recceda.core.reason.OtpReason;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * An in-memory OTP store that uses a Caffeine cache for high-performance, concurrent access.
 *
 * <p>This class is responsible for storing, verifying, and invalidating OTPs. It uses a secure
 * SHA-256 hash to store OTPs and relies on the Caffeine library's time-based eviction for
 * automatic cleanup of expired OTPs.
 */
public class ReccedaOtpStore implements OtpStore {

  private final Cache<String, OtpEntry> otpMap;

  /**
   * Creates a new {@code ReccedaOtpStore} with the default expiry policy.
   *
   * <p>The default policy expires entries based on the `expiryTime` in the {@link OtpEntry}.
   */
  public ReccedaOtpStore() {
    this(
        new Expiry<String, OtpEntry>() {
          @Override
          public long expireAfterCreate(String key, OtpEntry value, long currentTime) {
            long millis = value.expiryTime - System.currentTimeMillis();
            return TimeUnit.MILLISECONDS.toNanos(millis);
          }

          @Override
          public long expireAfterUpdate(
              String key, OtpEntry value, long currentTime, long currentDuration) {
            return currentDuration;
          }

          @Override
          public long expireAfterRead(
              String key, OtpEntry value, long currentTime, long currentDuration) {
            return currentDuration;
          }
        });
  }

  /**
   * Creates a new {@code ReccedaOtpStore} with a custom expiry policy.
   *
   * @param expiry the custom expiry policy to use for the Caffeine cache.
   */
  public ReccedaOtpStore(Expiry<String, OtpEntry> expiry) {
    this.otpMap = Caffeine.newBuilder().expireAfter(expiry).build();
  }

  @Override
  public void storeOtp(String key, String otp, long ttlMillis, OtpReason reason) {
    String compositeKey = key + ":" + reason.name();
    long expiryTime = System.currentTimeMillis() + ttlMillis;
    String otpHash = hashOtp(otp);
    otpMap.put(compositeKey, new OtpEntry(otpHash, expiryTime, reason));
  }

  @Override
  public boolean verifyOtp(String key, String otp, OtpReason reason) {
    String compositeKey = key + ":" + reason.name();
    OtpEntry entry = otpMap.getIfPresent(compositeKey);
    if (entry == null) {
      return false;
    }

    String otpHash = hashOtp(otp);
    boolean isValid = otpHash.equals(entry.otpHash);
    if (!isValid) {
      entry.failedAttempts++;
      otpMap.put(compositeKey, entry);
    }
    return isValid;
  }

  @Override
  public OtpEntry getOtpEntry(String key, OtpReason reason) {
    String compositeKey = key + ":" + reason.name();
    return otpMap.getIfPresent(compositeKey);
  }

  @Override
  public void invalidateOtp(String key, OtpReason reason) {
    String compositeKey = key + ":" + reason.name();
    otpMap.invalidate(compositeKey);
  }

  private String hashOtp(String otp) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not found", e);
    }
  }

  public static class OtpEntry {
    public String otpHash;
    public long expiryTime;
    public OtpReason reason;
    public int failedAttempts;

    public OtpEntry() {}

    public OtpEntry(String otpHash, long expiryTime, OtpReason reason) {
      this.otpHash = otpHash;
      this.expiryTime = expiryTime;
      this.reason = reason;
      this.failedAttempts = 0;
    }
  }
}
