# SSL Certificate Pinning Implementation Guide

**NoTap Mobile SDK - Certificate Pinning**

**Document Version:** 1.0.0
**Date:** 2026-01-09
**Status:** Production Implementation Guide
**Audience:** Mobile Developers, DevOps Engineers, Security Teams

---

## Table of Contents

1. [Overview](#overview)
2. [What is Certificate Pinning](#what-is-certificate-pinning)
3. [Implementation Details](#implementation-details)
4. [Configuration](#configuration)
5. [Getting Certificate Fingerprints](#getting-certificate-fingerprints)
6. [Certificate Rotation Strategy](#certificate-rotation-strategy)
7. [Testing](#testing)
8. [Troubleshooting](#troubleshooting)
9. [Security Considerations](#security-considerations)

---

## Overview

### What is Implemented

✅ **SSL Certificate Pinning** is **ALREADY IMPLEMENTED** in the NoTap SDK with:
- **Toggleable flag** (`enableCertificatePinning`)
- **Disabled by default** in development/staging
- **Enabled by default** in production
- **Multiple pin support** (primary + backup + rotation)
- **Automatic hostname extraction**
- **Clear error messages** on pin mismatch

### Key Files

| File | Purpose |
|------|---------|
| `sdk/src/commonMain/kotlin/com/zeropay/sdk/api/ApiConfig.kt` | Configuration interface |
| `sdk/src/androidMain/kotlin/com/zeropay/sdk/network/OkHttpClientImpl.kt` | Android implementation (OkHttp CertificatePinner) |
| `sdk/src/jsMain/kotlin/com/zeropay/sdk/network/ZeroPayHttpClientImpl.kt` | Web implementation |

---

## What is Certificate Pinning?

### Problem: Man-in-the-Middle Attacks

**Normal TLS (Without Pinning):**
```
Your App → Trusts any certificate signed by 200+ CAs
            (Google, DigiCert, Let's Encrypt, etc.)

Attacker installs malicious CA → Intercepts traffic → App accepts it ❌
```

**With Certificate Pinning:**
```
Your App → ONLY trusts YOUR specific certificate fingerprint

Attacker presents fake cert → App rejects it → Connection fails ✅
```

### Benefits

✅ **Prevents MITM attacks** even if device is compromised
✅ **Prevents malicious CA certificates**
✅ **Defense-in-depth** (additional security layer)
✅ **Industry best practice** for financial apps
✅ **PCI DSS compliant**

### Trade-Offs

⚠️ **Certificate rotation complexity** - Must update app when renewing SSL certificates
⚠️ **Development testing** - Requires separate pins for dev/staging/prod
⚠️ **Corporate proxies** - May break for enterprises using SSL inspection

---

## Implementation Details

### How It Works (Android)

**Code:** `OkHttpClientImpl.kt:80-97`

```kotlin
// Certificate Pinning (production only)
if (config.enableCertificatePinning && config.certificatePins.isNotEmpty()) {
    val certificatePinner = CertificatePinner.Builder().apply {
        // Extract hostname from baseUrl
        val hostname = config.baseUrl
            .removePrefix("https://")
            .removePrefix("http://")
            .split("/").first()
            .split(":").first()

        config.certificatePins.forEach { pin ->
            add(hostname, "sha256/$pin")
        }
    }.build()

    builder.certificatePinner(certificatePinner)
    Log.d(TAG, "Certificate pinning enabled for ${config.certificatePins.size} pins")
}
```

### Security Properties

- ✅ **SHA-256 fingerprints** - Strong cryptographic hashing
- ✅ **Public key pinning** - Pins the public key (not full certificate)
- ✅ **Multiple pins** - Supports primary + backup + rotation
- ✅ **Automatic validation** - OkHttp validates on every request
- ✅ **Fail-secure** - Connection fails if pin doesn't match

---

## Configuration

### Development (Pinning Disabled)

```kotlin
val config = ApiConfig.development(
    baseUrl = "http://10.0.2.2:3000",
    enableLogging = true
)

// Certificate pinning is automatically disabled
// config.enableCertificatePinning = false
```

**Why disabled?**
- Development uses HTTP (not HTTPS)
- Dev/staging certificates change frequently
- Local testing with self-signed certificates

---

### Staging (Pinning Optional)

```kotlin
val config = ApiConfig.staging(
    baseUrl = "https://staging-api.notap.io"
)

// Certificate pinning is disabled by default
// Enable if testing rotation strategy:
val configWithPinning = config.copy(
    enableCertificatePinning = true,
    certificatePins = listOf(
        "STAGING_CERT_FINGERPRINT_HERE"
    )
)
```

---

### Production (Pinning Enabled)

```kotlin
val config = ApiConfig.production(
    baseUrl = "https://api.notap.io",
    certificatePins = listOf(
        // Primary certificate (current)
        "Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=",

        // Backup certificate (in case primary expires)
        "C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=",

        // Next certificate (for rotation)
        "YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg="
    )
)

// Certificate pinning is automatically enabled
// config.validate() throws if pins are empty
```

**⚠️ CRITICAL:** Production configuration **requires** at least **3 pins**:
1. **Primary** - Current certificate
2. **Backup** - In case primary fails
3. **Next** - For rotation (add before renewing)

---

### Toggle for Testing

**Scenario:** You want to test in production but temporarily disable pinning.

```kotlin
// Production config with pinning DISABLED (for debugging only!)
val config = ApiConfig.production(
    baseUrl = "https://api.notap.io",
    certificatePins = listOf("DUMMY_PIN")  // Required by validation
).copy(
    enableCertificatePinning = false  // ← TOGGLE OFF
)

// ⚠️ WARNING: Only use for debugging SSL issues
// ⚠️ NEVER ship to production with pinning disabled
```

**Use cases:**
- Debugging certificate renewal issues
- Testing with corporate SSL inspection
- Emergency hotfix if pins become invalid

---

## Getting Certificate Fingerprints

### Method 1: OpenSSL (Recommended)

**Get fingerprint from production server:**

```bash
# Step 1: Connect to server and extract certificate
openssl s_client -servername api.notap.io -connect api.notap.io:443 </dev/null 2>/dev/null | \
  openssl x509 -pubkey -noout | \
  openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | \
  base64

# Output:
# Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=
```

**Breakdown:**
1. `openssl s_client` - Connect to server and get certificate
2. `openssl x509 -pubkey` - Extract public key
3. `openssl pkey` - Convert to DER format
4. `openssl dgst -sha256` - Hash with SHA-256
5. `base64` - Encode in Base64

---

### Method 2: Online Tools (Quick Testing)

**⚠️ WARNING:** Do NOT use for production certificates (security risk).

1. Visit: https://www.ssllabs.com/ssltest/
2. Enter: `api.notap.io`
3. Scroll to "Public Key Pinning"
4. Copy SHA-256 fingerprint

**Why not recommended?**
- Third-party sees your certificate
- Potential MITM during lookup
- Not auditable (compliance issue)

---

### Method 3: Certificate File

**If you have the .crt file:**

```bash
# Extract fingerprint from certificate file
openssl x509 -in server.crt -pubkey -noout | \
  openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | \
  base64
```

---

### Method 4: Android Debug (Runtime Inspection)

**Get fingerprint from current connection:**

```kotlin
// Add this code temporarily in development
val client = OkHttpClient.Builder()
    .eventListener(object : EventListener() {
        override fun callEnd(call: Call) {
            val certificate = call.request().url.toHttpUrl()
            Log.d("CertPin", "SHA-256: ${CertificatePinner.pin(certificate)}")
        }
    })
    .build()

// Output in logcat:
// CertPin: SHA-256: sha256/Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=
```

**⚠️ Remove this code before production!**

---

## Certificate Rotation Strategy

### Problem

**SSL certificates expire** (typically every 1-2 years). When you renew:
- Old certificate stops working
- Apps with old pins fail to connect
- Users must update app

### Solution: Multi-Pin Strategy

**Always configure 3 pins:**

```kotlin
certificatePins = listOf(
    "PRIMARY_CERT",   // Current certificate
    "BACKUP_CERT",    // Backup (in case primary fails)
    "NEXT_CERT"       // For rotation (add BEFORE renewing)
)
```

---

### Rotation Process (90-Day Timeline)

**Timeline:**

```
Day 0: Current certificate expires in 90 days
Day 1: Generate new certificate, get fingerprint
Day 2: Add new fingerprint to app (keep old one!)
Day 7: Release app update (v1.1.0)
Day 90: 90% of users have updated
Day 91: Activate new certificate server-side
Day 120: Remove old fingerprint in next app version (v1.2.0)
```

---

### Step-by-Step Rotation

#### **Step 1: Generate New Certificate (90 days before expiry)**

```bash
# Generate new certificate (your SSL provider)
# Example with Let's Encrypt:
certbot certonly --manual -d api.notap.io

# Extract fingerprint
openssl x509 -in /etc/letsencrypt/live/api.notap.io/cert.pem -pubkey -noout | \
  openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | \
  base64

# Output:
# YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=  ← NEW FINGERPRINT
```

---

#### **Step 2: Add New Pin to App (Keep Old)**

```kotlin
// BEFORE rotation (v1.0.0):
certificatePins = listOf(
    "Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=",  // Primary (current)
    "C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M="   // Backup
)

// AFTER adding new pin (v1.1.0):
certificatePins = listOf(
    "Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=",  // Old primary (keep!)
    "C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=",  // Backup (keep!)
    "YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg="   // NEW (rotation)
)
```

**✅ Key Point:** App now accepts BOTH old and new certificates.

---

#### **Step 3: Release App Update**

```bash
# Update version
# gradle.properties
VERSION_NAME=1.1.0

# Build release
./gradlew :sdk:assembleRelease

# Publish to Google Play Store
# Wait 90 days for user adoption (target: 90%+)
```

---

#### **Step 4: Activate New Certificate Server-Side (After 90 days)**

```bash
# Backup old certificate
sudo cp /etc/nginx/ssl/server.crt /etc/nginx/ssl/server.crt.old

# Install new certificate
sudo cp /path/to/new/cert.pem /etc/nginx/ssl/server.crt
sudo cp /path/to/new/key.pem /etc/nginx/ssl/server.key

# Reload Nginx
sudo nginx -t && sudo systemctl reload nginx
```

**What happens?**
- ✅ **Updated apps (v1.1.0):** Work fine (have new pin)
- ✅ **Old apps (v1.0.0):** Still work (have old pin for 90 days)
- ❌ **Very old apps (<v1.0.0):** Break (force update)

---

#### **Step 5: Remove Old Pin (After 120 days)**

```kotlin
// After 120 days, remove old pin (v1.2.0):
certificatePins = listOf(
    "YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=",  // Current (was "new")
    "C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=",  // Backup (keep)
    "NEXT_ROTATION_CERT_HERE"                         // Next rotation
)
```

---

### Emergency Rotation (Certificate Compromised)

**If certificate is compromised (security breach):**

```
Day 0: Certificate compromised, IMMEDIATE rotation needed
Day 1: Generate new certificate, get fingerprint
Day 2: Release EMERGENCY UPDATE (force update)
Day 3: Activate new certificate
Day 4: Revoke old certificate
```

**Emergency config:**

```kotlin
// Emergency: Only new certificate
certificatePins = listOf(
    "EMERGENCY_NEW_CERT_ONLY"
)
// Old apps will break → Force update via Google Play
```

---

## Testing

### Test Certificate Pinning (Development)

**Step 1: Enable pinning in dev config**

```kotlin
val testConfig = ApiConfig.development(
    baseUrl = "https://api.notap.io"  // Use HTTPS!
).copy(
    enableCertificatePinning = true,
    certificatePins = listOf(
        "Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys="  // Correct pin
    )
)

// Test: Should connect successfully
val client = ZeroPayHttpClient.create(testConfig)
val response = client.get("/health")
println("Response: ${response.statusCode}") // Should be 200
```

---

**Step 2: Test with WRONG pin (should fail)**

```kotlin
val badConfig = ApiConfig.development(
    baseUrl = "https://api.notap.io"
).copy(
    enableCertificatePinning = true,
    certificatePins = listOf(
        "WRONG_PIN_HERE_AAAABBBBCCCCDDDD=="  // Incorrect pin
    )
)

// Test: Should throw SSLException
val client = ZeroPayHttpClient.create(badConfig)
try {
    val response = client.get("/health")
    println("ERROR: Should have failed!")
} catch (e: NetworkException.SslException) {
    println("✅ PASS: Certificate pinning blocked connection")
    println("Error: ${e.message}")
    // Expected: "SSL/TLS error: Certificate validation failed or pinning mismatch"
}
```

---

### Test Certificate Rotation

**Simulate rotation scenario:**

```kotlin
// Scenario: Certificate will be rotated in 30 days

// Step 1: Current configuration
val oldConfig = ApiConfig.production(
    baseUrl = "https://api.notap.io",
    certificatePins = listOf("OLD_CERT")
)

// Step 2: Add new certificate (before rotation)
val rotationConfig = ApiConfig.production(
    baseUrl = "https://api.notap.io",
    certificatePins = listOf(
        "OLD_CERT",  // Keep old
        "NEW_CERT"   // Add new
    )
)

// Step 3: Test both work
// Point staging server to OLD_CERT → Should work
// Point staging server to NEW_CERT → Should also work
```

---

### Automated Testing (CI/CD)

```bash
# Run certificate pinning tests
./gradlew :sdk:testDebugUnitTest --tests "*CertificatePinningTest*"

# Expected output:
# ✅ testCertificatePinningEnabled_validPin_success
# ✅ testCertificatePinningEnabled_invalidPin_throwsSSLException
# ✅ testCertificatePinningDisabled_anyPin_success
```

---

## Troubleshooting

### Error: "SSL/TLS error: Certificate validation failed"

**Symptom:** App fails to connect in production.

**Possible causes:**

1. **Certificate expired**
   ```bash
   # Check certificate expiry
   echo | openssl s_client -servername api.notap.io -connect api.notap.io:443 2>/dev/null | \
     openssl x509 -noout -dates

   # Output:
   # notBefore=Jan  1 00:00:00 2025 GMT
   # notAfter=Jan  1 00:00:00 2026 GMT  ← Check this date!
   ```

2. **Wrong pin in app**
   ```bash
   # Get current server fingerprint
   openssl s_client -servername api.notap.io -connect api.notap.io:443 </dev/null 2>/dev/null | \
     openssl x509 -pubkey -noout | \
     openssl pkey -pubin -outform der | \
     openssl dgst -sha256 -binary | \
     base64

   # Compare with pin in ApiConfig.production()
   ```

3. **Server using different certificate** (load balancer issue)
   ```bash
   # Check certificate on all servers
   curl -vI https://api.notap.io 2>&1 | grep "subject:"
   ```

**Solution:**
```kotlin
// Emergency: Disable pinning temporarily
val emergencyConfig = config.copy(
    enableCertificatePinning = false
)
```

---

### Error: "Certificate pinning enabled but no pins configured"

**Symptom:** App crashes on startup with `IllegalStateException`.

**Cause:** Production config has `enableCertificatePinning = true` but `certificatePins = emptyList()`.

**Solution:**
```kotlin
// FIX: Add certificate pins
val config = ApiConfig.production(
    baseUrl = "https://api.notap.io",
    certificatePins = listOf(
        "YOUR_CERT_FINGERPRINT_HERE"
    )
)
```

---

### Corporate Proxy Issues

**Symptom:** Enterprise users report app not working.

**Cause:** Corporate SSL inspection intercepts TLS traffic (legitimate MITM).

**Solution:**
```kotlin
// Option 1: Disable pinning for enterprise builds
val enterpriseConfig = config.copy(
    enableCertificatePinning = false
)

// Option 2: Add corporate proxy certificate to pins
val enterpriseConfig = config.copy(
    certificatePins = listOf(
        "PRODUCTION_CERT",
        "CORPORATE_PROXY_CERT"  // Add corporate cert
    )
)
```

**Recommendation:** Provide enterprise build variant without pinning.

---

## Security Considerations

### Attack Scenarios

#### **Scenario 1: Compromised Device**

**Attack:** Attacker installs malicious CA certificate on user's phone.

**Without Pinning:**
- ❌ Attacker intercepts TLS traffic
- ❌ App trusts malicious CA
- ❌ Attacker decrypts factor digests

**With Pinning:**
- ✅ App rejects malicious certificate
- ✅ Connection fails (fail-secure)
- ✅ User sees error, cannot authenticate

---

#### **Scenario 2: Certificate Renewal**

**Attack:** Attacker waits for certificate expiry, hopes app breaks.

**Without Multi-Pin:**
- ❌ Certificate expires
- ❌ Old app breaks
- ❌ Users frustrated, may disable security

**With Multi-Pin:**
- ✅ 3 pins configured (current + backup + next)
- ✅ App continues working during rotation
- ✅ Seamless user experience

---

#### **Scenario 3: DNS Hijacking**

**Attack:** Attacker hijacks DNS, redirects api.notap.io to malicious server.

**Without Pinning:**
- ⚠️ Depends on CA validation
- ⚠️ If attacker has valid CA cert → attack succeeds

**With Pinning:**
- ✅ Malicious server cannot provide correct certificate
- ✅ Pin mismatch → connection fails
- ✅ Attack blocked

---

### Best Practices

✅ **DO:**
1. Always configure 3 pins (primary + backup + rotation)
2. Test pinning in staging before production
3. Document certificate renewal dates (set reminders!)
4. Monitor connection failures (detect pin mismatches)
5. Have emergency disable mechanism (hotfix builds)

❌ **DON'T:**
1. Use only 1 pin (no rotation strategy)
2. Forget to add new pin before renewal
3. Remove old pin immediately after rotation
4. Skip testing with wrong pins
5. Disable pinning in production (unless emergency)

---

### Compliance

**PCI DSS 4.0:**
- ✅ Requirement 4.2.1: TLS for transmission
- ✅ Requirement 6.5.4: Secure communications
- ✅ Certificate pinning = additional control (not required but recommended)

**OWASP MASVS:**
- ✅ MSTG-NETWORK-4: "The app either uses its own certificate store, or pins the endpoint certificate or public key"

**NIST SP 800-52:**
- ✅ Recommends certificate pinning for high-security applications

---

## Quick Reference

### Commands Cheat Sheet

```bash
# Get certificate fingerprint from production
openssl s_client -servername api.notap.io -connect api.notap.io:443 </dev/null 2>/dev/null | \
  openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | base64

# Check certificate expiry
echo | openssl s_client -servername api.notap.io -connect api.notap.io:443 2>/dev/null | \
  openssl x509 -noout -dates

# Extract from .crt file
openssl x509 -in server.crt -pubkey -noout | openssl pkey -pubin -outform der | \
  openssl dgst -sha256 -binary | base64

# Test connection with specific certificate
openssl s_client -showcerts -servername api.notap.io -connect api.notap.io:443 </dev/null
```

---

### Configuration Examples

```kotlin
// Development (no pinning)
val devConfig = ApiConfig.development()

// Staging (optional pinning)
val stagingConfig = ApiConfig.staging().copy(
    enableCertificatePinning = true,
    certificatePins = listOf("STAGING_CERT")
)

// Production (pinning required)
val prodConfig = ApiConfig.production(
    baseUrl = "https://api.notap.io",
    certificatePins = listOf(
        "PRIMARY_CERT",
        "BACKUP_CERT",
        "ROTATION_CERT"
    )
)

// Emergency disable (debugging only)
val emergencyConfig = prodConfig.copy(
    enableCertificatePinning = false
)
```

---

## Conclusion

### Summary

✅ **SSL Certificate Pinning is IMPLEMENTED** in NoTap SDK with:
- Toggleable flag (disabled for dev/staging, enabled for production)
- Multi-pin support (rotation strategy)
- Clear error messages
- Emergency disable capability

✅ **Prevents:**
- Man-in-the-middle attacks
- Malicious CA certificates
- DNS hijacking

✅ **Requires:**
- 3 certificate pins (primary + backup + rotation)
- 90-day rotation process
- Monitoring for pin mismatches
- Emergency disable mechanism

### Next Steps

1. ✅ **Get production certificate fingerprints** (use OpenSSL command)
2. ✅ **Configure ApiConfig.production()** with 3 pins
3. ✅ **Test in staging** with real HTTPS
4. ✅ **Set calendar reminder** for certificate renewal (60 days before expiry)
5. ✅ **Document rotation process** in runbook
6. ✅ **Monitor connection failures** (detect pin mismatches)

---

**Document Metadata:**
- **Version:** 1.0.0
- **Created:** 2026-01-09
- **Authors:** NoTap Security Team
- **Next Review:** 2026-04-09 (Quarterly)

**References:**
- OWASP Certificate Pinning Guide: https://owasp.org/www-community/controls/Certificate_and_Public_Key_Pinning
- OkHttp CertificatePinner: https://square.github.io/okhttp/features/certificate_pinning/
- NIST SP 800-52: Guidelines for TLS Implementations
