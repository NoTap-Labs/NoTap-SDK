# Integration Guide

This guide covers common integration scenarios and best practices for NoTap SDK.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [E-Commerce Integration](#e-commerce-integration)
- [Banking & Fintech](#banking--fintech)
- [Blockchain/Web3](#blockchainweb3)
- [Security Best Practices](#security-best-practices)
- [Production Checklist](#production-checklist)

---

## Architecture Overview

### How NoTap Works

```
┌─────────────────────────────────────────────────────────┐
│                  Your Application                       │
│  ┌──────────────────┐       ┌───────────────────────┐  │
│  │  User Device     │       │  Merchant/Backend     │  │
│  │  (Enrollment)    │       │  (Verification)       │  │
│  └──────────────────┘       └───────────────────────┘  │
└─────────────────────────────────────────────────────────┘
           ↓                              ↓
┌─────────────────────────────────────────────────────────┐
│                    NoTap SDK                            │
│  - Factor Capture                                       │
│  - Local Digest Generation (SHA-256)                    │
│  - Secure Storage                                       │
└─────────────────────────────────────────────────────────┘
           ↓ HTTPS/TLS 1.3                 ↓
┌─────────────────────────────────────────────────────────┐
│                 NoTap Cloud API                         │
│  - Double Encryption                                    │
│  - Zero-Knowledge Verification                          │
│  - Rate Limiting & Fraud Detection                      │
└─────────────────────────────────────────────────────────┘
```

### Key Concepts

**1. UUID (User Identifier)**
- Generated during enrollment
- Unique per user (NOT per device)
- User can authenticate from any device using their UUID

**2. Factors**
- Minimum 6 required
- Across 2+ categories (PSD3 SCA compliant)
- Stored as SHA-256 hashes (never plaintext)

**3. Zero-Knowledge Proofs**
- Merchants never see which factors were used
- Privacy-preserving verification
- Cryptographic proof of authentication

---

## E-Commerce Integration

### Complete Checkout Flow

```kotlin
class CheckoutActivity : ComponentActivity() {

    private lateinit var verificationManager: VerificationManager
    private var cartTotal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize verification manager
        verificationManager = VerificationManager(
            context = this,
            sdk = MyApplication.noTapSDK,
            config = VerificationConfig(
                timeout = 120_000,  // 2 minutes
                requireProof = true
            )
        )

        setContent {
            CheckoutScreen()
        }
    }

    @Composable
    fun CheckoutScreen() {
        Column {
            // Cart summary
            CartSummary(cartTotal = cartTotal)

            // NoTap checkout button
            Button(onClick = { initiateNoTapCheckout() }) {
                Text("Pay with NoTap")
            }
        }
    }

    private fun initiateNoTapCheckout() {
        // Get user's UUID (from your backend or local storage)
        val userUUID = getUserUUID()

        if (userUUID == null) {
            // User not enrolled - prompt enrollment
            promptEnrollment()
            return
        }

        // Start verification
        verificationManager.startVerification(
            uuid = userUUID,
            amount = cartTotal,
            currency = "USD",
            onSuccess = { result ->
                if (result.verified) {
                    // Verification successful
                    processPaymentWithGateway(result.zkProof)
                } else {
                    // Verification failed
                    showError("Authentication failed. Please try again.")
                }
            },
            onError = { error ->
                handleVerificationError(error)
            }
        )
    }

    private fun processPaymentWithGateway(proof: String?) {
        // Example: Stripe integration
        val stripe = Stripe(applicationContext, "pk_live_...")

        val paymentIntent = PaymentIntent.createPaymentIntent(
            amount = (cartTotal * 100).toInt(),  // Cents
            currency = "usd",
            metadata = mapOf(
                "notap_proof" to (proof ?: "")
            )
        )

        stripe.confirmPayment(paymentIntent) { result ->
            if (result.isSuccess) {
                // Payment successful
                showOrderConfirmation()
            } else {
                showError("Payment failed: ${result.error}")
            }
        }
    }

    private fun handleVerificationError(error: VerificationError) {
        when (error) {
            is VerificationError.UserNotFound -> {
                // UUID not found - user needs to re-enroll
                showError("Account not found. Please enroll first.")
                promptEnrollment()
            }
            is VerificationError.Timeout -> {
                showError("Verification timed out. Please try again.")
            }
            is VerificationError.RateLimitExceeded -> {
                val retrySeconds = error.retryAfter / 1000
                showError("Too many attempts. Retry in $retrySeconds seconds.")
            }
            else -> {
                showError("Verification error. Please contact support.")
            }
        }
    }
}
```

---

## Banking & Fintech

### High-Security Transaction Verification

```kotlin
class TransferActivity : ComponentActivity() {

    private lateinit var verificationManager: VerificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Strict config for banking
        verificationManager = VerificationManager(
            context = this,
            sdk = MyApplication.noTapSDK,
            config = VerificationConfig(
                timeout = 180_000,  // 3 minutes
                requireProof = true,
                minimumConfidence = 0.98,  // Higher confidence for banking
                allowPartialMatch = false  // All factors required
            )
        )
    }

    private fun initiateTransfer(
        fromAccount: String,
        toAccount: String,
        amount: Double
    ) {
        val userUUID = getUserUUID()

        // Step 1: Verify user identity
        verificationManager.startVerification(
            uuid = userUUID,
            amount = amount,
            currency = "USD",
            onSuccess = { result ->
                if (result.verified && result.confidence >= 0.98) {
                    // High confidence - proceed with transfer
                    executeTransfer(fromAccount, toAccount, amount, result.zkProof)
                } else {
                    // Low confidence - additional verification
                    requestAdditionalFactors()
                }
            },
            onError = { error ->
                // Log security event
                logSecurityEvent("TRANSFER_VERIFICATION_FAILED", error)
                showError("Unable to verify identity")
            }
        )
    }

    private fun executeTransfer(
        from: String,
        to: String,
        amount: Double,
        proof: String?
    ) {
        // Call your banking API
        bankingAPI.transfer(
            from = from,
            to = to,
            amount = amount,
            authProof = proof,
            onSuccess = {
                showSuccess("Transfer completed")
                logAuditTrail("TRANSFER_SUCCESS", amount, proof)
            },
            onError = { error ->
                showError("Transfer failed: ${error.message}")
                logAuditTrail("TRANSFER_FAILED", amount, proof)
            }
        )
    }
}
```

---

## Blockchain/Web3

### Wallet-Free Blockchain Payments

```kotlin
class Web3CheckoutActivity : ComponentActivity() {

    private lateinit var verificationManager: VerificationManager
    private lateinit var solanaClient: SolanaClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verificationManager = VerificationManager(this, MyApplication.noTapSDK)
        solanaClient = SolanaClient(network = Network.MAINNET)
    }

    private fun payWithSolana(
        recipientAddress: String,
        amountSOL: Double
    ) {
        val userUUID = getUserUUID()

        // Verify user
        verificationManager.startVerification(
            uuid = userUUID,
            amount = amountSOL,
            currency = "SOL",
            onSuccess = { result ->
                if (result.verified) {
                    // Create and sign Solana transaction
                    createAndSignSolanaTransaction(recipientAddress, amountSOL, result.zkProof)
                }
            },
            onError = { error ->
                showError("Verification failed: ${error.message}")
            }
        )
    }

    private fun createAndSignSolanaTransaction(
        recipient: String,
        amount: Double,
        proof: String?
    ) {
        // Get user's linked wallet address
        val walletAddress = getUserWalletAddress()

        // Create transaction
        val transaction = solanaClient.createTransaction(
            from = walletAddress,
            to = recipient,
            amount = amount,
            memo = "NoTap payment - Proof: ${proof?.take(16)}..."
        )

        // Sign and send
        solanaClient.signAndSend(transaction) { result ->
            if (result.isSuccess) {
                val signature = result.signature
                showSuccess("Payment sent! Signature: $signature")

                // Verify on blockchain
                pollTransactionStatus(signature)
            } else {
                showError("Transaction failed: ${result.error}")
            }
        }
    }
}
```

---

## Security Best Practices

### 1. Secure UUID Storage

**❌ Bad:**
```kotlin
// DON'T: Store UUID in plain SharedPreferences
val prefs = getSharedPreferences("app", MODE_PRIVATE)
prefs.edit().putString("uuid", userUUID).apply()
```

**✅ Good:**
```kotlin
// DO: Use EncryptedSharedPreferences
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

encryptedPrefs.edit().putString("uuid", userUUID).apply()
```

### 2. API Key Protection

**❌ Bad:**
```kotlin
// DON'T: Hardcode API key in code
val config = ZeroPayConfig(
    apiKey = "sk_live_1234567890abcdef"  // EXPOSED IN APK!
)
```

**✅ Good:**
```kotlin
// DO: Store in BuildConfig or use NDK
// In build.gradle.kts:
android {
    buildTypes {
        release {
            buildConfigField("String", "NOTAP_API_KEY", "\"${System.getenv("NOTAP_API_KEY")}\"")
        }
    }
}

// In code:
val config = ZeroPayConfig(
    apiKey = BuildConfig.NOTAP_API_KEY
)
```

### 3. Network Security

```kotlin
// Enable certificate pinning
val config = ZeroPayConfig(
    apiKey = apiKey,
    networkConfig = NetworkConfig(
        enableCertificatePinning = true,
        pinnedCertificates = listOf(
            "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        )
    )
)
```

### 4. Proguard Rules

Add to `proguard-rules.pro`:

```proguard
# NoTap SDK
-keep class com.zeropay.sdk.** { *; }
-keep class com.zeropay.enrollment.** { *; }
-keep class com.zeropay.merchant.** { *; }

# Serialization
-keepclassmembers class * {
    @kotlinx.serialization.* <fields>;
}
```

---

## Production Checklist

### Before Going Live

- [ ] **Switch to production environment**
  ```kotlin
  environment = Environment.PRODUCTION
  ```

- [ ] **Use production API key**
  ```kotlin
  apiKey = BuildConfig.NOTAP_API_KEY
  ```

- [ ] **Enable certificate pinning**
  ```kotlin
  enableCertificatePinning = true
  ```

- [ ] **Set appropriate log level**
  ```kotlin
  logLevel = LogLevel.ERROR  // Not DEBUG
  ```

- [ ] **Configure Proguard/R8**
  - Add keep rules for NoTap SDK
  - Test obfuscated build

- [ ] **Test error handling**
  - Network failures
  - Rate limiting
  - Invalid UUIDs
  - Timeout scenarios

- [ ] **Security review**
  - No hardcoded secrets
  - Encrypted storage for UUIDs
  - HTTPS/TLS for all requests

- [ ] **GDPR compliance**
  - Implement data deletion
  - Implement data export
  - Display privacy policy

- [ ] **Performance testing**
  - Test on low-end devices
  - Test with slow network
  - Monitor memory usage

- [ ] **Analytics & monitoring**
  - Track enrollment success rate
  - Track verification success rate
  - Monitor error rates

---

## Common Integration Patterns

### Pattern 1: Guest Checkout

Allow users to enroll during first checkout:

```kotlin
fun handleCheckout() {
    val uuid = getUserUUID()

    if (uuid == null) {
        // First time user - enroll then pay
        enrollUserThenPay()
    } else {
        // Returning user - verify then pay
        verifyUserThenPay(uuid)
    }
}
```

### Pattern 2: Multi-Device Support

Users can authenticate from any device:

```kotlin
// On Device A: User enrolls
enrollmentManager.startEnrollment { result ->
    val uuid = result.uuid
    // Save UUID to user's account on your backend
    saveUUIDToBackend(userId, uuid)
}

// On Device B: User logs in
loginUser { userId ->
    // Retrieve UUID from your backend
    val uuid = fetchUUIDFromBackend(userId)
    // Verify using retrieved UUID
    verifyUser(uuid)
}
```

### Pattern 3: Subscription Services

Monthly subscription authentication:

```kotlin
fun verifySubscriptionRenewal(subscriptionAmount: Double) {
    verificationManager.startVerification(
        uuid = userUUID,
        amount = subscriptionAmount,
        currency = "USD",
        onSuccess = { result ->
            if (result.verified) {
                renewSubscription()
            } else {
                notifySubscriptionFailed()
            }
        }
    )
}
```

---

## Need Help?

- 📧 Email: support@notap.com
- 💬 Discussions: [GitHub Discussions](https://github.com/keikworld/NoTap/discussions)
- 📖 API Reference: [api-reference.md](api-reference.md)

---

**Last Updated: 2025-11-06**
