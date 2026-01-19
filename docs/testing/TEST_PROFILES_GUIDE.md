---
hidden: true
---

# Test Profiles Guide

**Complete step-by-step guide for testing all NoTap platform features using dynamic test profiles.**

**Last Updated:** 2026-01-04

***

## Table of Contents

1. [What Are Test Profiles?](TEST_PROFILES_GUIDE.md#what-are-test-profiles)
2. [Quick Start](TEST_PROFILES_GUIDE.md#quick-start)
3. [Pre-Built Test Profiles](TEST_PROFILES_GUIDE.md#pre-built-test-profiles)
4. [Testing Enrollment Flow](TEST_PROFILES_GUIDE.md#testing-enrollment-flow)
5. [Testing Verification Flow](TEST_PROFILES_GUIDE.md#testing-verification-flow)
6. [Testing Management Features](TEST_PROFILES_GUIDE.md#testing-management-features)
7. [Testing Admin Panel](TEST_PROFILES_GUIDE.md#testing-admin-panel)
8. [Creating Custom Test Profiles](TEST_PROFILES_GUIDE.md#creating-custom-test-profiles)
9. [Test Inputs Reference](TEST_PROFILES_GUIDE.md#test-inputs-reference)
10. [Common Issues](TEST_PROFILES_GUIDE.md#common-issues)

***

## What Are Test Profiles?

Test profiles are **pre-configured user accounts stored in the database** that simulate real users with all necessary data:

* ✅ Complete enrollment with all factors
* ✅ Verification history and sessions
* ✅ Payment methods and transaction history
* ✅ Admin permissions and API keys
* ✅ Test credentials and JWT tokens
* ✅ Pre-filled test inputs for easy factor verification

**Key Benefits:**

| Feature               | Benefit                                                 |
| --------------------- | ------------------------------------------------------- |
| **No Hardcoding**     | All data stored in database (editable via API)          |
| **Full Testing**      | Test user, merchant, admin, and developer features      |
| **Easy Verification** | `test_inputs` included in responses for instant testing |
| **Flexible**          | Create/edit profiles via admin API                      |
| **Isolated**          | Test data doesn't affect production                     |

***

## Quick Start

### 1. Load rastafalso (Super Test User)

```bash
curl https://api.notap.io/v1/test-profiles/load/rastafalso.sol
```

**Response:**

```json
{
  "success": true,
  "isTestProfile": true,
  "profile": {
    "identifier": "rastafalso",
    "displayName": "Rasta Test User (Super)",
    "profileType": "super",
    "blockchainName": "rastafalso.sol",
    "enrollment": {
      "status": "verified",
      "factors": ["PIN", "PATTERN", "EMOJI", "COLOUR", "WORDS", ...],
      "test_inputs": {
        "PIN": "1234",
        "PATTERN": [0, 1, 4, 7],
        "EMOJI": ["😀", "🎉", "🚀", "💎"]
      }
    },
    "credentials": {
      "email": "rastafalso@test.notap.io",
      "jwt_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  }
}
```

### 2. Check If Identifier Is a Test Profile

```bash
curl https://api.notap.io/v1/test-profiles/check/rastafalso.sol
```

**Response:**

```json
{
  "success": true,
  "isTestProfile": true,
  "identifier": "rastafalso.sol",
  "baseIdentifier": "rastafalso"
}
```

### 3. Initiate Verification Session

```bash
curl -X POST https://api.notap.io/v1/verification/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "user_uuid": "rastafalso.sol",
    "transaction_amount": 100,
    "risk_level": "HIGH"
  }'
```

**Response (Test Mode):**

```json
{
  "success": true,
  "isTestProfile": true,
  "testMode": true,
  "session_id": "test_sess_a1b2c3d4",
  "required_factors": ["PIN", "PATTERN", "EMOJI"],
  "test_inputs": {
    "PIN": "1234",
    "PATTERN": [0, 1, 4, 7],
    "EMOJI": ["😀", "🎉", "🚀", "💎"]
  },
  "message": "Test verification session created. Use test_inputs for factor verification."
}
```

***

## Pre-Built Test Profiles

### rastafalso (Super User) ⭐

**Best for:** Testing all platform features

```bash
# Identifier: rastafalso.sol
# Type: super (all features)
# Factors: 10 (PIN, PATTERN, EMOJI, COLOUR, WORDS, RHYTHM_TAP, IMAGE_TAP, VOICE, MOUSE_DRAW, STYLUS_DRAW)
```

**Features:**

* ✅ Full enrollment (verified, no expiration)
* ✅ Admin permissions (role: super\_admin)
* ✅ Payment methods (Visa, Mastercard, test cards)
* ✅ API keys (developer access)
* ✅ Merchant access
* ✅ Crypto wallet integration

**Test Inputs:**

```json
{
  "PIN": "1234",
  "PATTERN": [0, 1, 4, 7],
  "EMOJI": ["😀", "🎉", "🚀", "💎"],
  "COLOUR": ["#FF0000", "#00FF00", "#0000FF", "#FFFF00"],
  "WORDS": ["apple", "banana", "cherry", "dragon"],
  "RHYTHM_TAP": "tap-tap-pause-tap",
  "VOICE": "my secret passphrase is sunshine",
  "MOUSE_DRAW": "signature pattern",
  "STYLUS_DRAW": "stylus signature",
  "IMAGE_TAP": [[0.25, 0.25], [0.75, 0.25], [0.5, 0.75]]
}
```

### testmerchant (Merchant User)

**Best for:** Testing merchant dashboard and payment flows

```bash
# Identifier: testmerchant.sol
# Type: merchant
# Factors: 3 (PIN, PATTERN, EMOJI)
```

**Test Input:**

```json
{
  "PIN": "5678"
}
```

### testadmin (Admin User)

**Best for:** Testing admin panel features

```bash
# Identifier: testadmin.sol
# Type: admin
# Factors: 2 (PIN, PATTERN)
```

**Test Input:**

```json
{
  "PIN": "9999"
}
```

### testdev (Developer)

**Best for:** Testing developer portal

```bash
# Identifier: testdev.sol
# Type: developer
# Factors: 1 (PIN)
```

**Test Input:**

```json
{
  "PIN": "0000"
}
```

### testuser (Regular User)

**Best for:** Testing regular user features

```bash
# Identifier: testuser.sol
# Type: user
# Factors: 3 (PIN, PATTERN, EMOJI)
```

**Test Input:**

```json
{
  "PIN": "1111"
}
```

***

## Testing Enrollment Flow

### Step 1: Check Enrollment Data

```bash
curl https://api.notap.io/v1/enrollment/retrieve/rastafalso.sol
```

**Response:**

```json
{
  "success": true,
  "isTestProfile": true,
  "testMode": true,
  "data": {
    "uuid": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "alias": "rastafalso-test",
    "sns_name": "rastafalso.sol",
    "factors": {
      "PIN": "test_pin_digest_1234",
      "PATTERN": "test_pattern_digest_0147",
      "EMOJI": "test_emoji_digest_happy"
    },
    "status": "verified",
    "auto_renewal": true,
    "test_inputs": {
      "PIN": "1234",
      "PATTERN": [0, 1, 4, 7]
    }
  }
}
```

### Step 2: Verify Enrollment Status

The enrollment is already pre-configured:

* ✅ Status: verified
* ✅ Auto-renewal: enabled
* ✅ Factors: 10 enrolled
* ✅ Expiration: 2099-12-31 (never expires for testing)

***

## Testing Verification Flow

### Step 1: Initiate Verification

```bash
curl -X POST https://api.notap.io/v1/verification/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "user_uuid": "rastafalso.sol",
    "transaction_amount": 49.99,
    "risk_level": "LOW"
  }'
```

**Response:**

```json
{
  "success": true,
  "isTestProfile": true,
  "testMode": true,
  "session_id": "test_sess_abc123",
  "required_factors": ["PIN", "PATTERN"],
  "test_inputs": {
    "PIN": "1234",
    "PATTERN": [0, 1, 4, 7]
  }
}
```

### Step 2: Verify First Factor (PIN)

Use the PIN from `test_inputs`:

```bash
curl -X POST https://api.notap.io/v1/verification/submit-factor \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "test_sess_abc123",
    "factor_type": "PIN",
    "input": "1234"
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "factorVerified": true,
  "completedFactors": ["PIN"],
  "remainingFactors": ["PATTERN"]
}
```

### Step 3: Verify Second Factor (PATTERN)

Use the PATTERN from `test_inputs`:

```bash
curl -X POST https://api.notap.io/v1/verification/submit-factor \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "test_sess_abc123",
    "factor_type": "PATTERN",
    "input": [0, 1, 4, 7]
  }'
```

**Expected Response:**

```json
{
  "success": true,
  "factorVerified": true,
  "completedFactors": ["PIN", "PATTERN"],
  "remainingFactors": [],
  "verification_complete": true,
  "verification_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Step 4: Complete Verification

The verification is now complete. Use the token to:

* Update payment status
* Complete transaction
* Update user profile

***

## Testing Management Features

### Step 1: Load Management Profile

```bash
curl https://api.notap.io/v1/management/profile/rastafalso.sol
```

**Response includes:**

* Profile information
* Enrolled factors
* Payment methods
* Auto-renewal settings
* Device information

### Step 2: Test Auto-Renewal Settings

```bash
curl -X PUT https://api.notap.io/v1/management/settings \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "auto_renewal": true,
    "renewal_threshold_days": 7
  }'
```

### Step 3: Check Device Management

```bash
curl https://api.notap.io/v1/management/devices/rastafalso.sol
```

***

## Testing Admin Panel

### Step 1: Login as Admin

```bash
curl -X POST https://api.notap.io/v1/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testadmin@test.notap.io",
    "password": "TestPassword123!"
  }'
```

**Response:**

```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "admin": {
    "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "email": "testadmin@test.notap.io",
    "fullName": "Test Admin User",
    "role": "super_admin"
  }
}
```

### Step 2: View Audit Logs

```bash
curl https://api.notap.io/v1/admin/audit-logs \
  -H "Authorization: Bearer <jwt_token>"
```

### Step 3: Monitor User Activity

```bash
curl https://api.notap.io/v1/admin/users/rastafalso/activity \
  -H "Authorization: Bearer <jwt_token>"
```

### Step 4: Manage Security Settings

```bash
curl -X PUT https://api.notap.io/v1/admin/settings/security \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "mfa_required": true,
    "session_timeout_minutes": 30,
    "max_login_attempts": 5
  }'
```

***

## Creating Custom Test Profiles

### Step 1: List Existing Profiles

```bash
curl https://api.notap.io/v1/test-profiles \
  -H "X-Admin-Key: <admin_api_key>"
```

### Step 2: Create New Profile

```bash
curl -X POST https://api.notap.io/v1/test-profiles \
  -H "X-Admin-Key: <admin_api_key>" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "mytest",
    "displayName": "My Custom Test User",
    "blockchainName": "mytest.sol",
    "profileType": "super",
    "enrollment": {
      "status": "verified",
      "factors": ["PIN", "PATTERN", "EMOJI"],
      "test_inputs": {
        "PIN": "9999",
        "PATTERN": [0, 2, 4, 6, 8],
        "EMOJI": ["🎉", "🚀", "💎", "⭐"]
      }
    },
    "payment": {
      "cards": [
        {
          "number": "4111111111111111",
          "type": "Visa",
          "expiry": "12/28",
          "cvv": "123",
          "is_default": true
        }
      ]
    }
  }'
```

### Step 3: Update Profile

```bash
curl -X PUT https://api.notap.io/v1/test-profiles/<profile_id> \
  -H "X-Admin-Key: <admin_api_key>" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "Updated Test User",
    "enrollment": {
      "factors": ["PIN", "PATTERN", "EMOJI", "COLOUR"]
    }
  }'
```

### Step 4: Use Custom Profile

```bash
curl https://api.notap.io/v1/test-profiles/load/mytest.sol
```

***

## Test Inputs Reference

### PIN (Personal Identification Number)

```json
{
  "PIN": "1234"
}
```

Expected length: 4-6 digits

### PATTERN (Grid Pattern)

```json
{
  "PATTERN": [0, 1, 4, 7]
}
```

Array of grid positions (3x3 grid = 0-8)

### EMOJI (Emoji Sequence)

```json
{
  "EMOJI": ["😀", "🎉", "🚀", "💎"]
}
```

Array of 4+ emoji characters

### COLOUR (Color Sequence)

```json
{
  "COLOUR": ["#FF0000", "#00FF00", "#0000FF", "#FFFF00"]
}
```

Array of 4+ hex color codes

### WORDS (Word Sequence)

```json
{
  "WORDS": ["apple", "banana", "cherry", "dragon"]
}
```

Array of 4+ dictionary words

### VOICE (Passphrase)

```json
{
  "VOICE": "my secret passphrase is sunshine"
}
```

Natural language passphrase (3+ words)

### RHYTHM\_TAP (Tap Rhythm)

```json
{
  "RHYTHM_TAP": "tap-tap-pause-tap"
}
```

Pattern of taps and pauses

### MOUSE\_DRAW (Mouse Pattern)

```json
{
  "MOUSE_DRAW": "signature pattern"
}
```

User-drawn pattern with mouse

### STYLUS\_DRAW (Stylus Signature)

```json
{
  "STYLUS_DRAW": "stylus signature"
}
```

User-drawn signature with stylus

### IMAGE\_TAP (Image Tap Positions)

```json
{
  "IMAGE_TAP": [[0.25, 0.25], [0.75, 0.25], [0.5, 0.75]]
}
```

Array of normalized coordinates \[x, y] (0.0-1.0)

***

## Common Issues

### Issue: "Test profile not found"

**Cause:** Incorrect identifier format

**Solution:**

* Use full SNS name: `rastafalso.sol` ✅
* Not just: `rastafalso` ❌
* Or use base identifier: `rastafalso` ✅

```bash
# ✅ Both work
curl https://api.notap.io/v1/test-profiles/load/rastafalso.sol
curl https://api.notap.io/v1/test-profiles/load/rastafalso
```

### Issue: "Verification failed" with correct test\_inputs

**Cause:** Test inputs might be case-sensitive or format-specific

**Solution:**

* PIN: Use exact string (no quotes in JSON)
* PATTERN: Use exact array order
* EMOJI: Copy-paste from response
* COLOUR: Use hex format #RRGGBB

**Debug:**

```bash
# Load profile and inspect test_inputs
curl https://api.notap.io/v1/test-profiles/load/rastafalso.sol | jq '.profile.enrollment.test_inputs'
```

### Issue: "Permission denied" on admin endpoints

**Cause:** Missing or invalid API key

**Solution:**

* Use `X-Admin-Key` header (development mode)
* Or use valid JWT token with admin permissions

```bash
# Development mode (sandbox)
curl https://api.notap.io/v1/test-profiles \
  -H "X-Admin-Key: your-admin-key"

# Production mode (use JWT)
curl https://api.notap.io/v1/test-profiles \
  -H "Authorization: Bearer <jwt_token>"
```

### Issue: Custom profile not appearing

**Cause:** Profile might be inactive

**Solution:**

* Check profile status: `is_active: true`
* Verify identifier format (lowercase, no spaces)
* Use correct SNS format: identifier.sol

```bash
# Check profile details
curl https://api.notap.io/v1/test-profiles/check/mytest.sol
```

***

## Best Practices

### ✅ DO

* Use test profiles for development/testing
* Copy test\_inputs from responses
* Create separate profiles for different test scenarios
* Document custom test profiles
* Use descriptive identifiers (e.g., `test-payment-success`)

### ❌ DON'T

* Use test profiles in production
* Hardcode test inputs in code
* Share test profile API keys
* Use real user data in test profiles
* Delete pre-built profiles

***

## Related Documentation

* [**TEST\_ARCHITECTURE.md**](TEST_ARCHITECTURE.md) - Complete testing framework guide
* [**SANDBOX\_TESTING\_GUIDE.md**](../../backend/docs/SANDBOX_TESTING_GUIDE.md) - Sandbox environment reference
* [**Verification Flow**](../../03-developer-guides/VERIFICATION_FLOW.md) - Complete verification documentation

***

## Support

For issues or questions:

1. Check [Common Issues](TEST_PROFILES_GUIDE.md#common-issues) section above
2. Review test profile response format
3. Verify test inputs match expected format
4. Check database migration status: `node scripts/run-migrations.js --status`

***

**Last Updated:** 2026-01-04 **Status:** ✅ Complete and ready for testing
