# Recceda OTP Flow Diagram

## OTP Generation Sequence

```plantuml
@startuml
actor Client
participant "ReccedaOtp" as RO
participant "Policy" as P
participant "ReccedaOtpGenerator" as G
participant "ReccedaOtpStore" as S
participant "OtpDistributor" as D

Client -> RO: generateOtp(key, distributor)
RO -> P: check(key, store)
alt Policy Check Fails
    P -> RO: throw OtpGenerationException
    RO -> Client: Exception
else Policy Check Passes
    RO -> G: generateOtp(length)
    G -> G: SecureRandom.nextInt(10)
    G -> RO: otp
    RO -> S: storeOtp(key, otp, ttl)
    S -> S: hashOtp(otp) [SHA-256]
    S -> S: store in Caffeine cache
    RO -> D: send(key, otp)
    D -> Client: OTP sent via SMS/Email
end
@enduml
```

## OTP Verification Sequence

```plantuml
@startuml
actor Client
participant "ReccedaOtp" as RO
participant "ReccedaOtpStore" as S

Client -> RO: verifyOtp(key, otp)
RO -> S: verifyOtp(key, otp)
S -> S: getIfPresent(key)
alt OTP Entry Not Found
    S -> RO: false
    RO -> Client: false
else OTP Entry Found
    S -> S: hashOtp(otp) [SHA-256]
    alt Hash Matches
        S -> RO: true
        RO -> Client: true
    else Hash Doesn't Match
        S -> S: increment failedAttempts
        S -> RO: false
        RO -> Client: false
    end
end
@enduml
```

## Class Diagram

```plantuml
@startuml
class ReccedaOtp {
    -otpGenerator: OtpGenerator
    -otpStore: OtpStore
    -policies: List<Policy>
    +generateOtp(key, distributor)
    +verifyOtp(key, otp): boolean
    +invalidateOtp(key)
}

interface OtpGenerator {
    +generateOtp(length): String
}

class ReccedaOtpGenerator {
    -secureRandom: SecureRandom
    +generateOtp(length): String
}

interface OtpStore {
    +storeOtp(key, otp, ttl)
    +verifyOtp(key, otp): boolean
    +getOtpEntry(key): OtpEntry
    +invalidateOtp(key)
}

class ReccedaOtpStore {
    -otpMap: Cache<String, OtpEntry>
    +storeOtp(key, otp, ttl)
    +verifyOtp(key, otp): boolean
    +getOtpEntry(key): OtpEntry
    -hashOtp(otp): String
}

class OtpEntry {
    +otpHash: String
    +expiryTime: long
    +failedAttempts: int
}

interface Policy {
    +check(key, store)
}

class PreventDuplicateOtpPolicy {
    +check(key, store)
}

class MaxFailedAttemptsPolicy {
    -maxAttempts: int
    +check(key, store)
}

interface OtpDistributor {
    +send(key, otp)
}

ReccedaOtp --> OtpGenerator
ReccedaOtp --> OtpStore
ReccedaOtp --> Policy
ReccedaOtp --> OtpDistributor
OtpGenerator <|-- ReccedaOtpGenerator
OtpStore <|-- ReccedaOtpStore
ReccedaOtpStore --> OtpEntry
Policy <|-- PreventDuplicateOtpPolicy
Policy <|-- MaxFailedAttemptsPolicy
@enduml
```

## Component Diagram

```plantuml
@startuml
package "Recceda OTP Library" {
    component [ReccedaOtp] as main
    
    package "Core" {
        component [OtpGenerator] as gen
        component [OtpStore] as store
        component [Policy Engine] as policy
        component [OtpDistributor] as dist
    }
    
    package "Storage" {
        component [Caffeine Cache] as cache
        component [SHA-256 Hasher] as hash
    }
    
    package "Security" {
        component [SecureRandom] as random
        component [Policy Validators] as validators
    }
}

main --> gen
main --> store
main --> policy
main --> dist
gen --> random
store --> cache
store --> hash
policy --> validators
@enduml
```

## Key Security Features

- **Secure Generation**: `SecureRandom` for cryptographically strong OTPs
- **Secure Storage**: SHA-256 hashed OTPs, never plain text
- **Time-Based Expiration**: Automatic cleanup via Caffeine cache
- **Policy Enforcement**: Configurable generation rules
- **Thread-Safe**: Concurrent access support
- **Failed Attempt Tracking**: Monitors verification attempts