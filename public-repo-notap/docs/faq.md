# Frequently Asked Questions

Common questions about NoTap SDK.

---

## General

### What is NoTap?

NoTap is a device-free, passwordless authentication system for payments. Users authenticate using memorable factors (PIN, pattern, colors, etc.) that work on any device - no passwords, no SMS codes, no biometric hardware required.

### How is NoTap different from other authentication methods?

| Feature | NoTap | Traditional 2FA | Biometric |
|---------|-------|----------------|-----------|
| Works on any device | ✅ | ❌ | ❌ |
| No SMS costs | ✅ | ❌ | ✅ |
| No passwords | ✅ | ❌ | ✅ |
| No special hardware | ✅ | ❌ | ❌ |
| Privacy-preserving | ✅ | ❌ | ⚠️ |
| Device loss recovery | ✅ | ❌ | ❌ |

### Is NoTap PSD3 SCA compliant?

Yes! NoTap exceeds PSD3 Strong Customer Authentication (SCA) requirements:
- ✅ Minimum 6 factors (exceeds requirement)
- ✅ Across 2+ categories (knowledge, biometric, behavioral, possession, location)
- ✅ Dynamic linking with cryptographic proof
- ✅ Transaction amount validation

### Is NoTap GDPR compliant?

Absolutely:
- ✅ **Privacy by design** - Only cryptographic hashes stored, never raw biometric data
- ✅ **Right to erasure** - One-click deletion of all user data
- ✅ **Data portability** - Export user data as JSON
- ✅ **Consent tracking** - Explicit consent with timestamps
- ✅ **24-hour TTL** - Automatic data expiry

---

## Technical

### What package names should I use?

Use `com.zeropay.*` for all imports:

```kotlin
implementation("com.zeropay:sdk:1.0.0")
implementation("com.zeropay:enrollment:1.0.0")
implementation("com.zeropay:merchant:1.0.0")
```

**Why "zeropay" and not "notap"?**
The package names use `zeropay` for API stability. This ensures zero breaking changes for all integrations. "NoTap" is the brand name, but the technical infrastructure uses `zeropay`.

### What is a UUID?

A UUID (Universally Unique Identifier) is a unique identifier generated during enrollment. It's the user's "identity" in the NoTap system.

**Important:**
- ✅ Save the UUID securely (encrypted storage)
- ✅ The UUID is NOT device-specific (works on any device)
- ✅ Users need their UUID to verify
- ❌ Never share UUIDs publicly

### Where is data stored?

- **Device**: UUID stored locally in encrypted storage (KeyStore/Keychain)
- **NoTap Cloud**: Encrypted factor digests (24-hour TTL)
- **Your backend** (optional): UUID linked to user accounts

**We never store:**
- ❌ Raw biometric data
- ❌ Plaintext PINs or passwords
- ❌ Personal identifying information

### How secure is NoTap?

NoTap uses bank-grade security:
- **SHA-256** for all hashing
- **AES-256-GCM** for encryption
- **PBKDF2** with 100,000 iterations
- **TLS 1.3** for all network traffic
- **Constant-time operations** to prevent timing attacks
- **Rate limiting** to prevent brute-force attacks

### Can users authenticate from multiple devices?

**Yes!** NoTap is device-free:

1. User enrolls on **Device A** → Gets UUID
2. User saves UUID to their account (your backend)
3. User logs in on **Device B** → Retrieves UUID
4. User completes verification factors on **Device B** ✅

The factors (PIN, pattern, etc.) are memorable, so users can reproduce them on any device.

---

## Integration

### Do I need a backend?

**For basic usage:** No, the NoTap SDK handles everything.

**For multi-device support:** Yes, you'll need to:
- Link UUIDs to user accounts
- Retrieve UUIDs when users log in on new devices

### Can I use NoTap in sandbox mode without an API key?

**Yes!** For development and testing:

```kotlin
val config = ZeroPayConfig(
    apiKey = null,  // No key needed
    environment = Environment.SANDBOX
)
```

Sandbox mode works fully, but data is not persisted.

### What payment gateways are supported?

NoTap integrates with 14+ payment gateways:
- Stripe
- Adyen
- PayPal
- Square
- Braintree
- PayU
- Razorpay
- And more...

NoTap provides the authentication - you handle the payment processing with your preferred gateway.

### Can I customize the enrollment UI?

The default enrollment UI is customizable:

```kotlin
val config = EnrollmentConfig(
    minimumFactors = 6,
    requireBiometric = false,
    showFactorSuggestions = true,
    theme = CustomTheme(
        primaryColor = Color(0xFF6200EA),
        accentColor = Color(0xFF03DAC5)
    )
)
```

For complete customization, you can build your own UI using the SDK's headless API.

### What's the minimum Android version?

**Android 7.0 (API 24)** and above.

Biometric factors (face/fingerprint) require **Android 9.0 (API 28)** and above.

---

## Enrollment

### How long does enrollment take?

**5-10 minutes** for most users. The process involves:
1. GDPR consent (30 seconds)
2. Factor selection (1 minute)
3. Completing 6+ factors (3-8 minutes)
4. Optional payment linking (1 minute)
5. Confirmation (30 seconds)

### What if a user forgets their factors?

Users can re-enroll with a new UUID. The old enrollment is automatically deleted after 24 hours.

**Best practice:** Allow users to update individual factors without full re-enrollment (coming soon).

### Can users change their enrolled factors?

**Currently:** Users must re-enroll to change factors.

**Coming soon:** Factor update API to modify individual factors without re-enrollment.

### What happens if a user's UUID is lost?

Users need to re-enroll and get a new UUID. This is similar to losing a password - there's no recovery without the UUID.

**Recommendation:** Store UUIDs on your backend linked to user accounts.

---

## Verification

### How long does verification take?

**30-90 seconds** depending on:
- Number of factors enrolled
- User familiarity with factors
- Network speed

### What if verification fails?

Users get 3 attempts before cooldown. After 3 failures:
- **1st cooldown:** 30 seconds
- **2nd cooldown:** 2 minutes
- **3rd cooldown:** 15 minutes

This prevents brute-force attacks.

### Can I require specific factors for verification?

**Currently:** System automatically selects factors.

**Coming soon:** Merchant-specified factor requirements (e.g., "require biometric + PIN").

### Does verification work offline?

**Partially:**
- Factor capture works offline
- Verification requires network to compare with enrolled data
- **Coming soon:** Offline verification mode

---

## Pricing

### Is NoTap free?

**Sandbox:** Free forever (for development/testing)

**Production:** Pricing based on usage (details at [notap.com/pricing](https://notap.com/pricing))

### Are there per-transaction fees?

Unlike SMS-based 2FA, NoTap has **zero per-transaction costs**. You pay a flat monthly fee based on active users.

### What's included in the free tier?

**Sandbox:**
- ✅ Unlimited enrollments
- ✅ Unlimited verifications
- ✅ All features
- ❌ Data not persisted (24-hour TTL)

---

## Troubleshooting

### "Invalid API key" error

```kotlin
// Check your API key is correct
val config = ZeroPayConfig(
    apiKey = "sk_live_...",  // Not sk_test_
    environment = Environment.PRODUCTION  // Must match key type
)
```

### "User not found" error

The UUID doesn't exist in NoTap's system. Possible causes:
- UUID expired (24-hour TTL in sandbox)
- UUID was deleted (GDPR erasure)
- User never enrolled

**Solution:** Prompt user to re-enroll.

### "Rate limit exceeded" error

Too many requests from your IP or for a specific UUID. Wait for the `retryAfter` duration:

```kotlin
is VerificationError.RateLimitExceeded -> {
    val seconds = error.retryAfter / 1000
    showError("Too many attempts. Retry in $seconds seconds")
}
```

### Enrollment fails with "Storage failure"

Check device permissions:
- Ensure `INTERNET` permission in manifest
- Check available storage space
- Verify app not in battery saver mode

### Biometric factors not available

Biometric factors require:
- ✅ Android 9.0+ (API 28)
- ✅ Device with biometric hardware
- ✅ User enrolled biometrics in device settings
- ✅ `USE_BIOMETRIC` permission in manifest

---

## Migration & Updates

### How do I migrate from version 1.0 to 2.0?

We maintain backward compatibility. See [CHANGELOG.md](../CHANGELOG.md) for migration guides.

### Will upgrading the SDK break existing enrollments?

**No.** Enrollments are server-side and version-agnostic. Users enrolled on SDK v1.0 can verify on SDK v2.0.

### How often should I update the SDK?

**Security updates:** Immediately
**Feature updates:** At your discretion
**Major versions:** Review changelog and test thoroughly

---

## Support

### How do I get help?

- 📧 **Email:** support@notap.com
- 💬 **Discussions:** [GitHub Discussions](https://github.com/keikworld/NoTap/discussions)
- 🐛 **Bug Reports:** [GitHub Issues](https://github.com/keikworld/NoTap/issues)
- 📖 **Documentation:** [https://docs.notap.com](https://docs.notap.com)

### Where can I find code examples?

Check the [examples/](../examples/) directory in this repo for:
- Android quick start
- E-commerce integration
- Web SDK integration
- Blockchain payments

### Can I contribute to NoTap?

Yes! See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

---

**Can't find your question? Ask on [GitHub Discussions](https://github.com/keikworld/NoTap/discussions)!**
