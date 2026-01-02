# Biometric Assistance Guide

**Version:** 1.0.0
**Date:** 2025-12-18
**Feature:** Smart "Help" Button for Biometric-Only Authentication

---

## Overview

The **Biometric Assistance** feature provides an optional "Help" button in the merchant verification UI that allows users to switch to biometric-only authentication when they're having difficulty with memory-based factors (PIN, Pattern, etc.).

### Key Features

- ✅ **Smart Auto-Detection** - Only shows if user has biometrics enrolled AND device supports them
- ✅ **Device-Aware** - Detects fingerprint sensors, cameras (face), and NFC readers
- ✅ **User-Initiated** - User clicks button themselves (not automatic)
- ✅ **Privacy-Preserving** - Merchant doesn't know why user needs help (tipsy, tired, injured, etc.)
- ✅ **Zero Security Degradation** - Still requires 2 factors (2 biometrics OR 1 biometric + NFC)
- ✅ **Works with Escalation** - Integrates seamlessly with existing factor escalation system

---

## When To Use

**Perfect for scenarios where users can't recall memory-based factors:**
- 🍺 At bars/restaurants (tipsy customers)
- 😴 Late night purchases (tired, cognitive load reduced)
- 🤕 Injured hand (can't draw patterns accurately)
- 🏃 Post-workout (fatigued, memory recall harder)
- 👴 Elderly users (prefer biometrics over memorization)
- 🌍 International travelers (jet-lagged, stressed)

**User clicks "Need Help?" → System offers Fingerprint + NFC instead of PIN + Pattern**

---

## How It Works

### Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: Session Initiation                                  │
├─────────────────────────────────────────────────────────────┤
│ Merchant: POST /v1/verification/initiate                    │
│ Backend: Returns session + enrolled_biometrics              │
│          { has_biometric_enrolled: true,                    │
│            enrolled_biometrics: ['FINGERPRINT', 'NFC'] }    │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 2: Device Capability Detection (Merchant App)          │
├─────────────────────────────────────────────────────────────┤
│ DeviceCapabilityDetector.detectCapabilities(context)        │
│ Returns: { fingerprint: true, face: false, nfc: true }     │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: Show/Hide Help Button                               │
├─────────────────────────────────────────────────────────────┤
│ IF (has_biometric_enrolled == true) AND                     │
│    (device supports >= 1 enrolled biometric)                │
│ THEN: Show "🤝 Need Help?" button                           │
│ ELSE: Hide button (not available)                           │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 4: User Struggles with Memory Factors                  │
├─────────────────────────────────────────────────────────────┤
│ User: Enters wrong PIN (tipsy, forgot it)                  │
│ System: Escalates to Pattern + Emoji                        │
│ User: Still struggling → Sees "Need Help?" button           │
│ User: Clicks button                                         │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 5: Switch to Biometric-Only                            │
├─────────────────────────────────────────────────────────────┤
│ Merchant: POST /v1/verification/biometric-assist            │
│           { session_id, device_capabilities }               │
│ Backend: Filters enrolled biometrics by device support      │
│          Returns: { required_factors: ['FINGERPRINT'] }     │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 6: User Completes Biometric Factors                    │
├─────────────────────────────────────────────────────────────┤
│ User: Taps fingerprint on merchant device → ✅             │
│ System: Authentication successful!                          │
│ Result: Payment authorized                                  │
└─────────────────────────────────────────────────────────────┘
```

---

## Implementation

### 1. Device Capability Detection (Merchant App)

```kotlin
import com.zeropay.merchant.helpers.DeviceCapabilityDetector

// In your VerificationViewModel or Activity
class VerificationViewModel : ViewModel() {

    private val _deviceCapabilities = MutableStateFlow<BiometricCapabilities?>(null)
    val deviceCapabilities: StateFlow<BiometricCapabilities?> = _deviceCapabilities.asStateFlow()

    fun detectDeviceCapabilities(context: Context) {
        viewModelScope.launch {
            val capabilities = DeviceCapabilityDetector.detectCapabilities(context)
            _deviceCapabilities.value = capabilities

            Log.d("Verification", "Device capabilities detected:")
            Log.d("Verification", "  Fingerprint: ${capabilities.fingerprint}")
            Log.d("Verification", "  Face: ${capabilities.face}")
            Log.d("Verification", "  NFC: ${capabilities.nfc}")
        }
    }
}
```

### 2. Session Initiation with Biometric Info

```kotlin
// Call initiate endpoint
val response = verificationManager.createSession(
    userId = "alice.notap.sol",
    merchantId = "merchant-123",
    transactionAmount = 75.00
)

// Backend returns:
// {
//   "success": true,
//   "session_id": "sess_abc123",
//   "required_factors": ["PIN", "PATTERN"],
//   "has_biometric_enrolled": true,
//   "enrolled_biometrics": ["FINGERPRINT", "NFC"]
// }

// Store this info for Help button logic
val hasBiometricEnrolled = response.has_biometric_enrolled
val enrolledBiometrics = response.enrolled_biometrics
```

### 3. Show Help Button (Conditional)

```kotlin
@Composable
fun VerificationScreen(
    viewModel: VerificationViewModel,
    session: VerificationSession
) {
    val deviceCapabilities by viewModel.deviceCapabilities.collectAsState()
    val hasBiometricEnrolled = session.hasBiometricEnrolled
    val enrolledBiometrics = session.enrolledBiometrics

    // Detect capabilities on first composition
    LaunchedEffect(Unit) {
        viewModel.detectDeviceCapabilities(LocalContext.current)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Factor input UI (PIN, Pattern, etc.)
        FactorInputCanvas(...)

        // Help button (only if biometrics available)
        AnimatedVisibility(
            visible = shouldShowHelpButton(
                hasBiometricEnrolled = hasBiometricEnrolled,
                enrolledBiometrics = enrolledBiometrics,
                deviceCapabilities = deviceCapabilities
            )
        ) {
            HelpButton(
                onClick = {
                    viewModel.enableBiometricAssistance(
                        sessionId = session.sessionId,
                        deviceCapabilities = deviceCapabilities!!
                    )
                }
            )
        }
    }
}

/**
 * Determine if Help button should be shown.
 *
 * Conditions:
 * 1. User has at least one biometric factor enrolled
 * 2. Device supports at least one of user's enrolled biometrics
 */
fun shouldShowHelpButton(
    hasBiometricEnrolled: Boolean,
    enrolledBiometrics: List<String>,
    deviceCapabilities: BiometricCapabilities?
): Boolean {
    if (!hasBiometricEnrolled || deviceCapabilities == null) {
        return false
    }

    // Check if device supports ANY of user's enrolled biometrics
    return enrolledBiometrics.any { factor ->
        when (factor) {
            "FINGERPRINT" -> deviceCapabilities.fingerprint
            "FACE" -> deviceCapabilities.face
            "NFC" -> deviceCapabilities.nfc
            else -> false
        }
    }
}
```

### 4. Help Button UI Component

```kotlin
@Composable
fun HelpButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Help,
            contentDescription = "Get help",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "🤝 Need Help? Use Biometric",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

### 5. Call Biometric Assistance Endpoint

```kotlin
// In VerificationViewModel
suspend fun enableBiometricAssistance(
    sessionId: String,
    deviceCapabilities: BiometricCapabilities
): Result<BiometricAssistResponse> {
    return withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("$baseUrl/v1/verification/biometric-assist") {
                contentType(ContentType.Application.Json)
                setBody(BiometricAssistRequest(
                    session_id = sessionId,
                    device_capabilities = DeviceCapabilitiesDto(
                        fingerprint = deviceCapabilities.fingerprint,
                        face = deviceCapabilities.face,
                        nfc = deviceCapabilities.nfc
                    )
                ))
            }

            if (response.status.isSuccess()) {
                val body = response.body<BiometricAssistResponse>()

                // Update UI with new factors (biometric only)
                _requiredFactors.value = body.required_factors
                _completedFactors.value = emptyList()
                _assistanceMode.value = true

                Log.d("Verification", "✅ Assistance mode enabled")
                Log.d("Verification", "   Factors: ${body.required_factors.joinToString()}")

                Result.success(body)
            } else {
                Result.failure(Exception("HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("Verification", "❌ Biometric assistance failed: ${e.message}")
            Result.failure(e)
        }
    }
}

// Data classes
@Serializable
data class BiometricAssistRequest(
    val session_id: String,
    val device_capabilities: DeviceCapabilitiesDto
)

@Serializable
data class DeviceCapabilitiesDto(
    val fingerprint: Boolean,
    val face: Boolean,
    val nfc: Boolean
)

@Serializable
data class BiometricAssistResponse(
    val success: Boolean,
    val session_id: String,
    val assistance_mode: Boolean,
    val message: String,
    val required_factors: List<String>,
    val required_count: Int,
    val completed_factors: List<String>,
    val remaining_factors: List<String>,
    val expires_in: Int
)
```

### 6. Complete Biometric Factors

```kotlin
// After assistance mode enabled, user completes biometric factors
// Use existing factor verification flow (unchanged)

// Example: Fingerprint verification
val fingerprintDigest = FingerprintProcessor.authenticate(context)

val result = verificationManager.submitFactor(
    sessionId = session.sessionId,
    factor = Factor.FINGERPRINT,
    digest = fingerprintDigest
)

when (result) {
    is VerificationResult.Success -> {
        // ✅ Authentication successful!
        Log.d("Verification", "Payment authorized")
    }
    is VerificationResult.PendingFactors -> {
        // Show next biometric factor (if 2 required)
        Log.d("Verification", "Remaining: ${result.remainingFactors}")
    }
    is VerificationResult.Failure -> {
        // Handle error
        Log.e("Verification", "Error: ${result.message}")
    }
}
```

---

## API Reference

### POST /v1/verification/biometric-assist

Switch current verification session to biometric-only factors.

**Request:**
```json
{
  "session_id": "sess_abc123",
  "device_capabilities": {
    "fingerprint": true,
    "face": false,
    "nfc": true
  }
}
```

**Response (Success):**
```json
{
  "success": true,
  "session_id": "sess_abc123",
  "assistance_mode": true,
  "message": "Biometric assistance enabled. Complete the following factors:",
  "required_factors": ["FINGERPRINT", "NFC"],
  "required_count": 2,
  "completed_factors": [],
  "remaining_factors": ["FINGERPRINT", "NFC"],
  "expires_in": 285
}
```

**Response (Error - No Biometrics Enrolled):**
```json
{
  "success": false,
  "error": "NO_BIOMETRICS_ENROLLED",
  "message": "User has no biometric factors enrolled. Cannot use assistance mode.",
  "can_retry": false
}
```

**Response (Error - Device Not Supported):**
```json
{
  "success": false,
  "error": "NO_DEVICE_BIOMETRICS",
  "message": "Device does not support any of your enrolled biometric factors.",
  "enrolled_biometrics": ["FACE"],
  "device_supports": {
    "fingerprint": true,
    "face": false,
    "nfc": false
  },
  "can_retry": false
}
```

---

## Security Considerations

### ✅ **No Security Degradation**

Biometric assistance maintains same security level as normal flow:

| Flow Type | Factors Required | Security Level |
|-----------|------------------|----------------|
| **Normal** | 2-3 factors (mixed types) | HIGH |
| **Assistance** | 2 biometric factors | **HIGH** (same) |

**Why biometric = high security:**
- Fingerprint = Unique biological trait (can't be guessed)
- Face = Biometric verification (can't be replicated easily)
- NFC = Possession factor (physical card required)

### ✅ **Privacy Preserved**

Merchant never knows WHY user clicked Help button:
- Could be tipsy
- Could be tired
- Could be injured hand
- Could prefer biometrics

**Zero stigma** - just "Need Help?" button

### ✅ **User-Initiated Only**

System NEVER automatically switches to assistance mode:
- No time-based logic (not "after 9pm")
- No location detection (not "at bars")
- No automatic impairment detection

**User must click button themselves**

### ✅ **Works Everywhere**

No environmental context required:
- No GPS tracking
- No merchant category
- Works at all merchants, all times
- Privacy by design

---

## Real-World Examples

### Example 1: Tipsy Customer at Bar

**Scenario:** Alice at bar, 11:45 PM, $85 tab

```
1. Merchant initiates session
   Required: PIN + Pattern (normal flow)

2. Alice enters wrong PIN (tipsy, forgot it)
   System escalates to: Pattern + Emoji + Colors

3. Alice still struggling, sees "Need Help?" button
   Clicks button

4. System switches to: Fingerprint + NFC
   (Device has fingerprint sensor, Alice has NFC card)

5. Alice taps fingerprint → Success!
   Taps NFC card → Success!
   Payment authorized ✅

Total time: ~15 seconds (vs would have failed without Help)
```

### Example 2: Elderly User at Grocery Store

**Scenario:** Bob (72 years old) at grocery store, $120 purchase

```
1. Merchant initiates session
   Required: PIN + Pattern + Emoji (high amount = 3 factors)

2. Bob tries PIN → Wrong (forgot it)
   System escalates to: Pattern + Emoji + Colors + Rhythm

3. Bob confused by complex factors, clicks "Need Help?"

4. System switches to: Fingerprint
   (Device has sensor, Bob has fingerprint enrolled, only 1 biometric available)

5. Bob taps fingerprint → Success!
   Payment authorized ✅

Note: Only 1 factor required because Bob only has 1 biometric enrolled
      (System requires minimum factors available, not fixed 2)
```

### Example 3: Post-Workout at Gym

**Scenario:** Maria after intense workout, $12 smoothie

```
1. Merchant initiates session
   Required: PIN + Colors (low amount = 2 factors)

2. Maria tries PIN → Wrong (fatigued, memory recall impaired)
   System escalates to: Colors + Emoji

3. Maria clicks "Need Help?"

4. System switches to: Face + NFC
   (Gym kiosk has camera, Maria has Face + NFC enrolled)

5. Maria looks at camera → Success!
   Taps NFC card → Success!
   Payment authorized ✅
```

---

## Testing

### Unit Tests

```kotlin
class DeviceCapabilityDetectorTest {

    @Test
    fun `detect fingerprint sensor on device with BiometricManager support`() {
        val context = mockContext(hasFingerprintSensor = true)
        val capabilities = DeviceCapabilityDetector.detectCapabilities(context)
        assertTrue(capabilities.fingerprint)
    }

    @Test
    fun `shouldShowHelpButton returns true when biometrics enrolled and device supports`() {
        val hasBiometricEnrolled = true
        val enrolledBiometrics = listOf("FINGERPRINT", "NFC")
        val deviceCapabilities = BiometricCapabilities(
            fingerprint = true,
            face = false,
            nfc = true
        )

        val result = shouldShowHelpButton(
            hasBiometricEnrolled = hasBiometricEnrolled,
            enrolledBiometrics = enrolledBiometrics,
            deviceCapabilities = deviceCapabilities
        )

        assertTrue(result)
    }

    @Test
    fun `shouldShowHelpButton returns false when no device support`() {
        val hasBiometricEnrolled = true
        val enrolledBiometrics = listOf("FINGERPRINT")
        val deviceCapabilities = BiometricCapabilities(
            fingerprint = false,
            face = false,
            nfc = false
        )

        val result = shouldShowHelpButton(
            hasBiometricEnrolled = hasBiometricEnrolled,
            enrolledBiometrics = enrolledBiometrics,
            deviceCapabilities = deviceCapabilities
        )

        assertFalse(result)
    }
}
```

### Integration Tests

```bash
# Test biometric assistance endpoint
curl -X POST http://localhost:3000/v1/verification/biometric-assist \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "sess_test_123",
    "device_capabilities": {
      "fingerprint": true,
      "face": false,
      "nfc": true
    }
  }'

# Expected response:
# {
#   "success": true,
#   "assistance_mode": true,
#   "required_factors": ["FINGERPRINT", "NFC"]
# }
```

---

## FAQs

### Q: Is assistance mode less secure than normal mode?

**A:** No! Biometric factors (fingerprint, face, NFC) are HIGH security - same level as memory factors. The system still requires 2 factors minimum.

### Q: Can attackers exploit this by clicking Help every time?

**A:** No. User must have biometrics ENROLLED during registration. Can't bypass if never enrolled biometrics.

### Q: What if device doesn't support biometrics?

**A:** Help button won't show. User continues with normal factor escalation flow.

### Q: Does this work with existing escalation system?

**A:** Yes! Seamlessly integrates. User can click Help at any point (before or after escalation).

### Q: What if user has NO biometrics enrolled?

**A:** Help button won't show. Backend returns error if somehow called.

### Q: Can merchant force assistance mode automatically?

**A:** No. User MUST click button. Merchant cannot auto-enable.

---

## Troubleshooting

### Help Button Not Showing

**Check:**
1. ✅ User has biometrics enrolled? (`has_biometric_enrolled` in response)
2. ✅ Device capabilities detected? (`DeviceCapabilityDetector.detectCapabilities()`)
3. ✅ Intersection exists? (user enrolled FINGERPRINT, device supports FINGERPRINT)

### Assistance Endpoint Returns Error

**Common errors:**
- `NO_BIOMETRICS_ENROLLED` → User never enrolled any biometric factors
- `NO_DEVICE_BIOMETRICS` → Device doesn't support user's enrolled biometrics
- `SESSION_NOT_FOUND` → Session expired or invalid session_id

### Fingerprint Prompt Not Showing

**Check:**
1. BiometricPrompt properly configured in merchant app
2. Device has fingerprint enrolled in system settings
3. App has biometric permission in AndroidManifest

---

## Best Practices

1. **Always detect device capabilities early** - On VerificationScreen composition, not on button click

2. **Show Help button prominently** - After 1 failed attempt, make it visible

3. **Use clear messaging** - "Need Help? Use Fingerprint" is better than just "Help"

4. **Preserve user privacy** - Never log WHY user clicked Help

5. **Test on real devices** - Emulators may not have biometric sensors

6. **Handle gracefully** - If backend returns error, show fallback message

---

## Summary

The Biometric Assistance feature provides a **smart, privacy-preserving** way to help users authenticate when they're struggling with memory-based factors.

**Key Benefits:**
- ✅ Zero security degradation (biometric = high security)
- ✅ Zero privacy invasion (no location tracking, no time-based logic)
- ✅ Zero merchant burden (optional feature, auto-detects)
- ✅ Zero user stigma (generic "Need Help?" button)

**Perfect for:** Bars, gyms, late-night purchases, elderly users, post-workout, jet-lagged travelers

**Result:** NoTap works BEST when people need it MOST! 🎯
