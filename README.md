# Recceda OTP

A simple, secure, and time-based One-Time Password (OTP) generation and verification library for Java.

## Features

*   **Secure OTP Generation**: Uses `java.security.SecureRandom` for cryptographically strong OTPs.
*   **Time-Based Validation**: OTPs are valid for a configurable amount of time.
*   **Secure Storage**: Stores OTPs using a secure SHA-256 hash, not in plain text.
*   **Thread-Safe**: The OTP store is designed for concurrent use in multi-threaded environments.
*   **Simple API**: A clean and easy-to-use facade for generating and verifying OTPs.

## Installation

To use this library in your project, add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.recceda</groupId>
  <artifactId>recceda-otp</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

Here is a simple example of how to generate and verify an OTP for a user. The `generateOtp` method uses a callback to allow you to implement your own distribution logic (e.g., sending an email or SMS).

```java
import com.recceda.ReccedaOtp;
import com.recceda.core.distributor.OtpDistributor;
import com.recceda.core.store.OtpStore;
import com.recceda.core.store.ReccedaOtpStore;

public class Example {

    public static void main(String[] args) {
        // 1. Create an instance of the OTP store.
        // ReccedaOtpStore is an in-memory store.
        OtpStore otpStore = new ReccedaOtpStore();

        // 2. Create the main ReccedaOtp object, injecting the store.
        ReccedaOtp reccedaOtp = new ReccedaOtp(otpStore);

        String userId = "test-user-123";

        // 3. Generate a new OTP for the user and distribute it.
        // The second parameter is a callback that handles the distribution of the OTP.
        reccedaOtp.generateOtp(userId, (key, otp) -> {
            System.out.println("Generated OTP for " + key + ": " + otp);
            // In a real application, you would send the OTP via email or SMS here.
        });

        // 4. To verify, you would get the OTP from the user.
        // For this example, we'll just assume the user entered "123456".
        boolean isCorrect = reccedaOtp.verifyOtp(userId, "123456");
        System.out.println("Verification of '123456' successful: " + isCorrect);
    }
}
```

### Customizing OTP Length and Validity

You can also specify the length of the OTP and its time-to-live (TTL) in milliseconds.

```java
// Generate an 8-digit OTP that is valid for 10 minutes (600,000 ms)
reccedaOtp.generateOtp(userId, 8, 10 * 60 * 1000, (key, otp) -> {
    System.out.println("Generated 8-digit OTP for " + key + ": " + otp);
    // Distribution logic here...
});
```

## Building from Source

To build the project from source, you will need:
*   Java 8 or higher
*   Apache Maven

Clone the repository and run the following command from the project root:

```bash
mvn clean install
```

This will compile the source code, run the tests, and install the package into your local Maven repository.

## Contact

For any questions or inquiries, please contact Mike at [mike@mikechiloane.co.za](mailto:mike@mikechiloane.co.za).
