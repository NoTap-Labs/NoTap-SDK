# NoTap SDK API Reference

Complete API documentation for NoTap SDK.

---

## Table of Contents

- [Core SDK](#core-sdk)
  - [ZeroPaySDK](#zeropaysdk)
  - [ZeroPayConfig](#zeropayconfig)
- [Enrollment Module](#enrollment-module)
  - [EnrollmentManager](#enrollmentmanager)
  - [EnrollmentConfig](#enrollmentconfig)
  - [EnrollmentResult](#enrollmentresult)
- [Merchant Module](#merchant-module)
  - [VerificationManager](#verificationmanager)
  - [VerificationConfig](#verificationconfig)
  - [VerificationResult](#verificationresult)
- [Authentication Factors](#authentication-factors)
- [Error Handling](#error-handling)

---

## Core SDK

### ZeroPaySDK

Main SDK class for initializing NoTap.

#### `initialize()`

Initialize the NoTap SDK.

```kotlin
companion object {
    fun initialize(
        context: Context,
        config: ZeroPayConfig
    ): ZeroPaySDK
}
```

**Parameters:**
- `context: Context` - Application context
- `config: ZeroPayConfig` - SDK configuration

**Returns:**
- `ZeroPaySDK` - Initialized SDK instance

**Example:**
```kotlin
val sdk = ZeroPaySDK.initialize(
    context = applicationContext,
    config = ZeroPayConfig(
        apiKey = "your_api_key",
        environment = Environment.PRODUCTION
    )
)
```

---

### ZeroPayConfig

Configuration object for SDK initialization.

```kotlin
data class ZeroPayConfig(
    val apiKey: String?,
    val environment: Environment = Environment.PRODUCTION,
    val enableBiometrics: Boolean = true,
    val enableBlockchain: Boolean = false,
    val logLevel: LogLevel = LogLevel.INFO,
    val timeout: Long = 30_000,  // milliseconds
    val retryAttempts: Int = 3
)
```

**Properties:**
- `apiKey: String?` - Your NoTap API key (null for sandbox)
- `environment: Environment` - SANDBOX or PRODUCTION
- `enableBiometrics: Boolean` - Enable face/fingerprint factors
- `enableBlockchain: Boolean` - Enable blockchain wallet integration
- `logLevel: LogLevel` - DEBUG, INFO, WARN, ERROR
- `timeout: Long` - Network timeout in milliseconds
- `retryAttempts: Int` - Number of retry attempts for failed requests

---

## Enrollment Module

### EnrollmentManager

Manages user enrollment (registration).

#### Constructor

```kotlin
class EnrollmentManager(
    context: Context,
    sdk: ZeroPaySDK,
    config: EnrollmentConfig
)
```

#### `startEnrollment()`

Start the enrollment flow.

```kotlin
fun startEnrollment(
    onSuccess: (EnrollmentResult) -> Unit,
    onError: (EnrollmentError) -> Unit,
    onCancelled: () -> Unit = {}
)
```

**Parameters:**
- `onSuccess: (EnrollmentResult) -> Unit` - Callback when enrollment succeeds
- `onError: (EnrollmentError) -> Unit` - Callback when enrollment fails
- `onCancelled: () -> Unit` - Callback when user cancels (optional)

**Example:**
```kotlin
enrollmentManager.startEnrollment(
    onSuccess = { result ->
        val uuid = result.uuid
        saveUserUUID(uuid)
    },
    onError = { error ->
        showError(error.message)
    },
    onCancelled = {
        finish()
    }
)
```

---

### EnrollmentConfig

Configuration for enrollment process.

```kotlin
data class EnrollmentConfig(
    val minimumFactors: Int = 6,
    val minimumCategories: Int = 2,
    val requireBiometric: Boolean = false,
    val allowWeakPasswords: Boolean = false,
    val consentRequired: Boolean = true,
    val showFactorSuggestions: Boolean = true
)
```

**Properties:**
- `minimumFactors: Int` - Minimum number of factors required (default: 6)
- `minimumCategories: Int` - Minimum number of factor categories (default: 2)
- `requireBiometric: Boolean` - Require at least one biometric factor
- `allowWeakPasswords: Boolean` - Allow simple/weak passwords (not recommended)
- `consentRequired: Boolean` - Require GDPR consent
- `showFactorSuggestions: Boolean` - Show suggested factors to user

---

### EnrollmentResult

Result object returned on successful enrollment.

```kotlin
data class EnrollmentResult(
    val uuid: String,
    val alias: String,
    val enrolledFactors: List<Factor>,
    val categories: Set<FactorCategory>,
    val timestamp: Long
)
```

**Properties:**
- `uuid: String` - Unique user identifier (save this!)
- `alias: String` - Human-readable alias (e.g., "BluePhoenix42")
- `enrolledFactors: List<Factor>` - Factors the user enrolled
- `categories: Set<FactorCategory>` - Categories covered
- `timestamp: Long` - Enrollment timestamp (Unix epoch)

---

## Merchant Module

### VerificationManager

Manages payment verification.

#### Constructor

```kotlin
class VerificationManager(
    context: Context,
    sdk: ZeroPaySDK,
    config: VerificationConfig
)
```

#### `startVerification()`

Start verification for a payment.

```kotlin
fun startVerification(
    uuid: String,
    amount: Double,
    currency: String = "USD",
    onSuccess: (VerificationResult) -> Unit,
    onError: (VerificationError) -> Unit
)
```

**Parameters:**
- `uuid: String` - User's UUID from enrollment
- `amount: Double` - Payment amount
- `currency: String` - Currency code (ISO 4217)
- `onSuccess: (VerificationResult) -> Unit` - Success callback
- `onError: (VerificationError) -> Unit` - Error callback

**Example:**
```kotlin
verificationManager.startVerification(
    uuid = userUUID,
    amount = 99.99,
    currency = "USD",
    onSuccess = { result ->
        if (result.verified) {
            processPayment(result.zkProof)
        }
    },
    onError = { error ->
        showError(error.message)
    }
)
```

---

### VerificationConfig

Configuration for verification process.

```kotlin
data class VerificationConfig(
    val timeout: Long = 120_000,  // 2 minutes
    val requireProof: Boolean = true,
    val minimumConfidence: Double = 0.95,
    val allowPartialMatch: Boolean = false
)
```

**Properties:**
- `timeout: Long` - Verification timeout (milliseconds)
- `requireProof: Boolean` - Generate zero-knowledge proof
- `minimumConfidence: Double` - Minimum confidence score (0.0-1.0)
- `allowPartialMatch: Boolean` - Allow verification with fewer factors

---

### VerificationResult

Result object from verification.

```kotlin
data class VerificationResult(
    val verified: Boolean,
    val confidence: Double,
    val zkProof: String?,
    val matchedFactors: Int,
    val totalFactors: Int,
    val timestamp: Long
)
```

**Properties:**
- `verified: Boolean` - Whether verification succeeded
- `confidence: Double` - Confidence score (0.0-1.0)
- `zkProof: String?` - Zero-knowledge proof (if requested)
- `matchedFactors: Int` - Number of factors matched
- `totalFactors: Int` - Total factors enrolled
- `timestamp: Long` - Verification timestamp

---

## Authentication Factors

### Factor Categories

```kotlin
enum class FactorCategory {
    KNOWLEDGE,    // Things you know
    BIOMETRIC,    // Things you are
    BEHAVIORAL,   // How you behave
    POSSESSION,   // Things you have
    LOCATION      // Where you are
}
```

### Available Factors

#### Knowledge Factors
```kotlin
enum class Factor {
    PIN,          // 4-12 digit PIN
    PATTERN,      // Visual pattern lock
    COLOUR,       // Color sequence (3-6 colors)
    EMOJI,        // Emoji sequence (3-8 emojis)
    WORDS,        // Memorable words (3-10 words)
    // ...
}
```

#### Biometric Factors
```kotlin
    FACE,         // Face recognition
    FINGERPRINT,  // Fingerprint scan
    VOICE,        // Voice phrase (text-based)
```

#### Behavioral Factors
```kotlin
    RHYTHM_TAP,   // Rhythmic tapping pattern
    MOUSE_DRAW,   // Mouse/touchscreen drawing
    STYLUS_DRAW,  // Stylus signature
    IMAGE_TAP,    // Tap points on image
```

#### Possession Factors
```kotlin
    NFC,          // NFC tag/card
```

#### Location Factors
```kotlin
    BALANCE,      // Device tilt/balance gesture
```

### Factor Metadata

Get information about a factor:

```kotlin
val factor = Factor.PIN

println(factor.displayName)  // "PIN"
println(factor.description)  // "4-12 digit numeric code"
println(factor.category)     // FactorCategory.KNOWLEDGE
println(factor.isAvailable(context))  // true/false
```

---

## Error Handling

### EnrollmentError

```kotlin
sealed class EnrollmentError {
    data class InvalidFactor(val factor: Factor) : EnrollmentError()
    data class StorageFailure(val message: String) : EnrollmentError()
    data class NetworkError(val message: String) : EnrollmentError()
    data class RateLimitExceeded(val retryAfter: Long) : EnrollmentError()
    data class Unknown(val message: String) : EnrollmentError()
}
```

### VerificationError

```kotlin
sealed class VerificationError {
    data class InvalidUUID(val uuid: String) : VerificationError()
    data class UserNotFound(val uuid: String) : VerificationError()
    data class NetworkError(val message: String) : VerificationError()
    data class Timeout(val duration: Long) : VerificationError()
    data class RateLimitExceeded(val retryAfter: Long) : VerificationError()
    data class Unknown(val message: String) : VerificationError()
}
```

### Handling Errors

```kotlin
enrollmentManager.startEnrollment(
    onError = { error ->
        when (error) {
            is EnrollmentError.InvalidFactor -> {
                showError("Invalid factor: ${error.factor}")
            }
            is EnrollmentError.NetworkError -> {
                showError("Network error: ${error.message}")
                retryEnrollment()
            }
            is EnrollmentError.RateLimitExceeded -> {
                showError("Too many attempts. Try again in ${error.retryAfter}ms")
            }
            else -> {
                showError("Enrollment failed")
            }
        }
    }
)
```

---

## Web SDK API

### Initialization

```javascript
NoTap.initialize({
  apiKey: 'your_api_key',
  environment: 'production',  // or 'sandbox'
  debug: false
});
```

### Enrollment

```javascript
const result = await NoTap.enrollment.start({
  minimumFactors: 6,
  minimumCategories: 2,
  requireBiometric: false
});

if (result.success) {
  const { uuid, alias, enrolledFactors } = result;
  console.log(`Enrolled as ${alias}`);
}
```

### Verification

```javascript
const result = await NoTap.verification.verify({
  uuid: userUUID,
  amount: 99.99,
  currency: 'USD'
});

if (result.verified) {
  console.log('Payment verified:', result.proof);
}
```

---

## Advanced Features

### Payment Gateway Integration

```kotlin
import com.zeropay.sdk.gateway.PaymentProvider

// Link payment provider during enrollment
enrollmentManager.linkPaymentProvider(
    provider = PaymentProvider.STRIPE,
    credentials = mapOf(
        "publishableKey" to "pk_test_...",
        "customerId" to "cus_..."
    ),
    onSuccess = { tokenId ->
        println("Payment provider linked: $tokenId")
    },
    onError = { error ->
        println("Linking failed: ${error.message}")
    }
)
```

### Blockchain Wallet Integration

```kotlin
import com.zeropay.sdk.blockchain.WalletLinkingManager

val walletManager = WalletLinkingManager(sdk)

// Link Phantom wallet (Solana)
walletManager.linkPhantomWallet(
    context = context,
    onSuccess = { walletAddress ->
        println("Wallet linked: $walletAddress")
    },
    onError = { error ->
        println("Wallet linking failed: ${error.message}")
    }
)
```

---

## Rate Limiting

NoTap implements multi-layer rate limiting:

- **Global**: 1000 requests/minute (all users)
- **Per IP**: 100 requests/minute
- **Per User**: 50 requests/minute

When rate limited, the error includes `retryAfter` in milliseconds:

```kotlin
is EnrollmentError.RateLimitExceeded -> {
    val retryAfterSeconds = error.retryAfter / 1000
    showError("Too many attempts. Retry in $retryAfterSeconds seconds")
}
```

---

## GDPR Compliance

### Right to Erasure

```kotlin
sdk.deleteUserData(
    uuid = userUUID,
    onSuccess = {
        println("User data deleted")
    },
    onError = { error ->
        println("Deletion failed: ${error.message}")
    }
)
```

### Data Export

```kotlin
sdk.exportUserData(
    uuid = userUUID,
    onSuccess = { jsonData ->
        // Download or display user's data
        downloadFile("user_data.json", jsonData)
    },
    onError = { error ->
        println("Export failed: ${error.message}")
    }
)
```

---

## Support

Need help with the API?

- 📧 Email: api-support@notap.com
- 💬 Discussions: [GitHub Discussions](https://github.com/keikworld/NoTap/discussions)
- 📖 More examples: [Integration Guide](integration-guide.md)

---

**API Version: 1.0.0**
**Last Updated: 2025-11-06**
