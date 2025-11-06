# Getting Started with NoTap SDK

This guide will help you integrate NoTap authentication into your application in under 30 minutes.

---

## Prerequisites

### For Android Development
- Android Studio Hedgehog (2023.1.1) or later
- Minimum SDK: Android 7.0 (API 24)
- Target SDK: Android 14 (API 34)
- Kotlin 1.9+
- Gradle 8.0+

### For Web Development
- Node.js 18+ (for npm packages)
- Modern browser (Chrome 90+, Firefox 88+, Safari 14+)

### API Keys
You'll need a NoTap API key. Get yours at:
👉 **[https://notap.com/signup](https://notap.com/signup)** _(coming soon)_

For development/testing, you can use the sandbox environment without an API key.

---

## Android Integration

### Step 1: Add Dependencies

Add NoTap SDK to your app's `build.gradle.kts`:

```kotlin
dependencies {
    // NoTap SDK (core)
    implementation("com.zeropay:sdk:1.0.0")

    // Optional: Enrollment module (if you handle user registration)
    implementation("com.zeropay:enrollment:1.0.0")

    // Optional: Merchant module (if you verify payments)
    implementation("com.zeropay:merchant:1.0.0")
}
```

### Step 2: Add Permissions

Add required permissions to `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Required for network communication -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Optional: For biometric authentication -->
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />

    <!-- Optional: For NFC factor -->
    <uses-permission android:name="android.permission.NFC" />

    <application
        ...>
        ...
    </application>
</manifest>
```

### Step 3: Initialize the SDK

Initialize NoTap in your `Application` class:

```kotlin
import android.app.Application
import com.zeropay.sdk.ZeroPaySDK
import com.zeropay.sdk.ZeroPayConfig
import com.zeropay.sdk.Environment

class MyApplication : Application() {

    companion object {
        lateinit var noTapSDK: ZeroPaySDK
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize NoTap SDK
        noTapSDK = ZeroPaySDK.initialize(
            context = this,
            config = ZeroPayConfig(
                apiKey = "your_api_key_here",  // Or null for sandbox
                environment = Environment.SANDBOX,  // Use PRODUCTION for live
                enableBiometrics = true,
                enableBlockchain = false,
                logLevel = LogLevel.DEBUG  // Use INFO or ERROR in production
            )
        )

        println("NoTap SDK initialized successfully")
    }
}
```

Don't forget to register your `Application` class in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
```

### Step 4: User Enrollment (Registration)

Allow users to enroll their authentication factors:

```kotlin
import com.zeropay.enrollment.EnrollmentManager
import com.zeropay.enrollment.EnrollmentConfig
import com.zeropay.enrollment.EnrollmentResult

class RegistrationActivity : ComponentActivity() {

    private lateinit var enrollmentManager: EnrollmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize enrollment manager
        enrollmentManager = EnrollmentManager(
            context = this,
            sdk = MyApplication.noTapSDK,
            config = EnrollmentConfig(
                minimumFactors = 6,  // Require 6 factors minimum
                minimumCategories = 2,  // Across 2+ categories (PSD3 SCA)
                requireBiometric = false,  // Make biometric optional
                allowWeakPasswords = false
            )
        )

        setContent {
            EnrollmentScreen()
        }
    }

    @Composable
    fun EnrollmentScreen() {
        Button(onClick = { startEnrollment() }) {
            Text("Start Enrollment")
        }
    }

    private fun startEnrollment() {
        enrollmentManager.startEnrollment(
            onSuccess = { result: EnrollmentResult ->
                // Enrollment successful!
                val uuid = result.uuid
                val alias = result.alias

                // Save UUID securely (it's the user's identity)
                saveUserUUID(uuid)

                // Show success message
                Toast.makeText(
                    this,
                    "Enrolled successfully as $alias!",
                    Toast.LENGTH_LONG
                ).show()

                // Navigate to main app
                navigateToMainApp()
            },
            onError = { error ->
                // Handle enrollment error
                Toast.makeText(
                    this,
                    "Enrollment failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            },
            onCancelled = {
                // User cancelled enrollment
                Toast.makeText(this, "Enrollment cancelled", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveUserUUID(uuid: String) {
        // Save to secure storage (SharedPreferences or KeyStore)
        val sharedPrefs = getSharedPreferences("notap", MODE_PRIVATE)
        sharedPrefs.edit().putString("user_uuid", uuid).apply()
    }
}
```

### Step 5: Payment Verification (Merchants)

Verify users during checkout:

```kotlin
import com.zeropay.merchant.VerificationManager
import com.zeropay.merchant.VerificationConfig
import com.zeropay.merchant.VerificationResult

class CheckoutActivity : ComponentActivity() {

    private lateinit var verificationManager: VerificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verificationManager = VerificationManager(
            context = this,
            sdk = MyApplication.noTapSDK,
            config = VerificationConfig(
                timeout = 120_000,  // 2 minutes
                requireProof = true  // Generate ZK proof
            )
        )
    }

    private fun processPayment(userUUID: String, amount: Double) {
        verificationManager.startVerification(
            uuid = userUUID,
            amount = amount,
            currency = "USD",
            onSuccess = { result: VerificationResult ->
                if (result.verified) {
                    // Payment verified!
                    val proof = result.zkProof  // Optional cryptographic proof

                    // Process payment with your payment gateway
                    chargePaymentGateway(amount, proof)

                    Toast.makeText(
                        this,
                        "Payment authorized!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Verification failed
                    Toast.makeText(
                        this,
                        "Authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onError = { error ->
                Toast.makeText(
                    this,
                    "Verification error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}
```

---

## Web Integration

### Step 1: Install via NPM

```bash
npm install @notap/web-sdk
```

Or include via CDN:

```html
<script src="https://cdn.notap.com/sdk/1.0.0/notap.min.js"></script>
```

### Step 2: Initialize the SDK

```javascript
import NoTap from '@notap/web-sdk';

// Initialize
NoTap.initialize({
  apiKey: 'your_api_key_here',  // Or null for sandbox
  environment: 'sandbox',  // 'sandbox' or 'production'
  debug: true
});

console.log('NoTap SDK initialized');
```

### Step 3: User Enrollment

```javascript
// Start enrollment flow
const enrollButton = document.getElementById('enroll-btn');

enrollButton.addEventListener('click', async () => {
  try {
    const result = await NoTap.enrollment.start({
      minimumFactors: 6,
      minimumCategories: 2
    });

    if (result.success) {
      const { uuid, alias } = result;

      // Save UUID (e.g., to localStorage or your backend)
      localStorage.setItem('notap_uuid', uuid);

      console.log(`Enrolled as ${alias}!`);
      alert('Enrollment successful!');
    }
  } catch (error) {
    console.error('Enrollment failed:', error);
    alert(`Enrollment error: ${error.message}`);
  }
});
```

### Step 4: Payment Verification

```javascript
// Verify user during checkout
const checkoutButton = document.getElementById('checkout-btn');

checkoutButton.addEventListener('click', async () => {
  const uuid = localStorage.getItem('notap_uuid');
  const amount = 99.99;

  try {
    const result = await NoTap.verification.verify({
      uuid: uuid,
      amount: amount,
      currency: 'USD'
    });

    if (result.verified) {
      // Payment authorized
      console.log('Payment verified with proof:', result.proof);

      // Process payment
      await processPayment(amount, result.proof);

      alert('Payment successful!');
    } else {
      alert('Authentication failed');
    }
  } catch (error) {
    console.error('Verification error:', error);
    alert(`Verification failed: ${error.message}`);
  }
});
```

---

## Testing in Sandbox Mode

### Sandbox Environment

The sandbox environment allows you to test NoTap without API keys:

```kotlin
// Android
ZeroPayConfig(
    apiKey = null,  // No key needed for sandbox
    environment = Environment.SANDBOX
)
```

```javascript
// Web
NoTap.initialize({
  apiKey: null,  // No key needed for sandbox
  environment: 'sandbox'
});
```

### Test Credentials

In sandbox mode, you can use these test values:

- **Test UUID**: `test-user-12345678-1234-1234-1234-123456789abc`
- **Test Amount**: Any positive number
- **Test Factors**: All factors work in sandbox (PIN: any 4-6 digits, Pattern: any valid pattern)

---

## Next Steps

Now that you have NoTap integrated:

1. **[Explore the Integration Guide](integration-guide.md)** - Deep dive into advanced features
2. **[Check the API Reference](api-reference.md)** - Complete API documentation
3. **[Review Example Apps](../examples/)** - See full implementation examples
4. **[Read the FAQ](faq.md)** - Common questions answered

---

## Need Help?

- 📧 Email: support@notap.com
- 💬 Discussions: [GitHub Discussions](https://github.com/keikworld/NoTap/discussions)
- 🐛 Issues: [GitHub Issues](https://github.com/keikworld/NoTap/issues)
- 📖 Docs: [https://docs.notap.com](https://docs.notap.com)

---

**Happy coding! 🚀**
