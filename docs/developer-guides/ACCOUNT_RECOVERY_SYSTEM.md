# Account Recovery & Portal Authentication System

**Version:** 1.0.0
**Last Updated:** 2026-02-22
**Status:** Production Ready

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Recovery Codes](#recovery-codes)
4. [Tiered Management Portal](#tiered-management-portal)
5. [Auth Mode System](#auth-mode-system)
6. [Login Lockout](#login-lockout)
7. [Enrollment Redirect](#enrollment-redirect)
8. [Grace Period](#grace-period)
9. [API Reference](#api-reference)
10. [Database Migrations](#database-migrations)
11. [Frontend Integration](#frontend-integration)
12. [Security Patterns](#security-patterns)
13. [Testing](#testing)
14. [Troubleshooting](#troubleshooting)

---

## Overview

The Account Recovery & Portal Authentication System solves the permanent lockout problem: previously, the management portal required ALL enrolled factors to access, and there was zero recovery mechanism when users forgot even one factor.

### Problem Statement

| Issue | Impact |
|-------|--------|
| Management portal required ALL factors | Users locked out if they forgot 1 of 8+ factors |
| No recovery mechanism for end users | Permanent lockout (zero-knowledge = no server-side recovery) |
| Merchants had no password reset | Lost credentials = lost dashboard access |
| Developers had NoTap login stub (501) | Could not dogfood our own product |
| No brute-force protection on user tables | Only admin table had lockout |

### Solution Summary

| Component | Description |
|-----------|-------------|
| **Recovery Codes** | 8 one-time codes (XXXX-XXXX), bcrypt + KMS wrapped, single-use |
| **Tiered Threshold** | READ=60%, WRITE=80% of enrolled factors (not ALL) |
| **Auth Mode** | Per-account choice: password-only, NoTap-only, or both |
| **Login Lockout** | 5 failed attempts = 1 hour lock (ported from AdminAuthService) |
| **Enrollment Redirect** | URL-based routing for merchant/dev onboarding integration |
| **Grace Period** | 7-day reduced threshold after recovery re-enrollment |

### Actor Types

| Actor | Auth Method | Recovery Mechanism | Has Email? |
|-------|------------|-------------------|------------|
| **End User (enrollment)** | NoTap factors only | Recovery codes | No |
| **Regular User** | Email/password + optional NoTap | Password reset + recovery codes | Yes |
| **Merchant** | Email/password + optional NoTap | Password reset + recovery codes | Yes |
| **Developer** | Email/password + OAuth + optional NoTap | Password reset + recovery codes | Yes |
| **Admin** | NoTap factors (existing) | Recovery codes | No |

---

## Architecture

### System Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    ENROLLMENT FLOW                           │
│                                                             │
│  User enrolls factors → Recovery codes generated (8 codes)  │
│  → Codes displayed ONCE → User saves them securely          │
│  → bcrypt hash → KMS wrap → PostgreSQL storage              │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  MANAGEMENT PORTAL                           │
│                                                             │
│  POST /initiate → Backend selects random factor subset      │
│  → READ: 60% factors, WRITE: 80% factors (min 3)           │
│  → CSPRNG selection (Fisher-Yates shuffle)                  │
│  → Client completes factors → POST /verify                  │
│  → Constant-time comparison → Auth token (15 min)           │
└─────────────────────────────────────────────────────────────┘
                           │
                    ┌──────┴──────┐
                    │  Can't      │
                    │  remember?  │
                    ▼             ▼
┌──────────────────────┐  ┌──────────────────────┐
│   RECOVERY FLOW      │  │   PASSWORD RESET      │
│ (end users / admins) │  │ (merchants / devs)    │
│                      │  │                       │
│ UUID + recovery code │  │ Email + reset token   │
│ → bcrypt compare     │  │ → New password        │
│ → Re-enrollment token│  │ → Login restored      │
│ → 7-day grace period │  │                       │
│ → Re-enroll factors  │  └──────────────────────-┘
└──────────────────────┘
```

### Key Files

| File | Purpose |
|------|---------|
| `backend/services/RecoveryCodeService.js` | Core recovery code logic (generate, verify, consume) |
| `backend/routes/recoveryRouter.js` | Recovery API endpoints |
| `backend/routes/managementRouter.js` | Tiered management portal (initiate/verify flow) |
| `backend/routes/merchantAuthRouter.js` | Merchant auth (password reset, NoTap login, auth mode) |
| `backend/routes/developerAuthRouter.js` | Developer auth (NoTap login, auth mode) |
| `backend/routes/regularAuthRouter.js` | Regular user auth (NoTap login, auth mode) |
| `backend/database/migrations/025_create_recovery_codes.sql` | Recovery codes + audit log tables |
| `backend/database/migrations/026_add_auth_mode.sql` | Auth mode column on 3 user tables |
| `backend/database/migrations/027_add_login_lockout.sql` | Lockout columns on 3 user tables |
| `online-web/.../RecoveryCodeStep.kt` | Recovery code display UI step |
| `online-web/.../VerificationChallengeFlow.kt` | Two-step initiate/verify UI flow |
| `online-web/.../ManagementPortal.kt` | Recovery form in management portal |
| `online-web/src/jsMain/kotlin/main.kt` | URL-based routing |

---

## Recovery Codes

### Design Principles

1. **Zero PII**: No email, phone, or personal data needed for end users
2. **Defense-in-depth**: CSPRNG generation -> bcrypt (12 rounds) -> KMS encryption -> PostgreSQL
3. **Single-use**: Each code consumed after successful verification
4. **Batch management**: 8 codes per batch, old batch invalidated on regeneration
5. **Audit trail**: All operations logged (generation, use, failure, invalidation)

### Code Format

- Format: `XXXX-XXXX` (e.g., `XKCD-7291`)
- Alphabet: `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` (28 chars, no I/O/0/1 for readability)
- Entropy: ~38.6 bits per code (28^8 = ~7.2 x 10^11 combinations)
- Codes per batch: 8

### Generation Flow

```
1. crypto.randomBytes(4) → 4 random bytes
2. Each byte mod 28 → index into ambiguity-free alphabet
3. Repeat for second segment → "XXXX-XXXX"
4. bcrypt(code, 12 rounds) → one-way hash
5. KMS.wrap(bcryptHash) → encrypted hash
6. Store encrypted hash in PostgreSQL (recovery_codes table)
7. Return plaintext code to user ONCE (never retrievable again)
```

### Verification Flow

```
1. User submits UUID + recovery code
2. Rate limit check (3 attempts/hour/IP)
3. Normalize code: uppercase, trim
4. Format validation: /^[A-Z0-9]{4}-[A-Z0-9]{4}$/
5. Retrieve all unused codes for this owner from PostgreSQL
6. For each code:
   a. KMS.unwrap(encrypted_hash) → bcrypt hash
   b. bcrypt.compare(submitted_code, hash)  [inherently constant-time]
   c. wipeBuffer(hash)  [memory cleanup]
7. If match: mark code as used (used_at = NOW())
8. Generate re-enrollment token: crypto.randomBytes(32)
9. Store token in Redis (1-hour TTL, single-use)
10. Activate 7-day grace period in Redis
11. Return token + remaining code count
```

### RecoveryCodeService API

```javascript
const { recoveryCodeService } = require('../services/RecoveryCodeService');

// Generate codes (returns plaintext ONCE)
const result = await recoveryCodeService.generateCodes(
  'enrollment',  // ownerType: 'enrollment' | 'merchant' | 'developer' | 'admin'
  'uuid-here',   // ownerId
  req.ip         // ipAddress (anonymized before storage)
);
// result = { codes: ['XKCD-7291', ...], batchId: 'uuid', codeCount: 8 }

// Verify and consume a code
const result = await recoveryCodeService.verifyAndConsume(
  'enrollment', ownerId, 'XKCD-7291', req.ip
);
// result = { valid: true, remainingCodes: 7, message: '...' }

// Check remaining codes
const result = await recoveryCodeService.getRemainingCount('enrollment', ownerId);
// result = { count: 7, hasRecoveryCodes: true }

// Regenerate codes (invalidates old batch)
const result = await recoveryCodeService.regenerateCodes('enrollment', ownerId, req.ip);
// result = { codes: [...], batchId: 'new-uuid', codeCount: 8 }
```

---

## Tiered Management Portal

### Factor Threshold Model

Previously: ALL enrolled factors required (100%).
Now: Proportional threshold based on operation type.

| Access Level | Normal Threshold | Grace Period Threshold | Minimum |
|-------------|-----------------|----------------------|---------|
| **READ** (view devices, status) | 60% of enrolled | 40% of enrolled | 3 |
| **WRITE** (delete, export, update) | 80% of enrolled | 60% of enrolled | 3 |

**Examples:**

| Enrolled | READ (normal) | WRITE (normal) | READ (grace) | WRITE (grace) |
|----------|--------------|----------------|-------------|---------------|
| 3 | 3 | 3 | 3 | 3 |
| 5 | 3 | 4 | 3 | 3 |
| 8 | 5 | 7 | 4 | 5 |
| 10 | 6 | 8 | 4 | 6 |
| 15 | 9 | 12 | 6 | 9 |

### Two-Step Authentication Flow

**Step 1: Initiate Session**

```
POST /v1/management/initiate
Body: { uuid, access_level: "READ" | "WRITE" }

Response: {
  success: true,
  session_id: "hex-string",
  access_level: "WRITE",
  required_factors: ["PIN", "PATTERN", "EMOJI", ...],
  required_count: 5,
  enrolled_total: 8,
  expires_in_seconds: 600
}
```

The backend:
1. Retrieves enrolled factors from Redis
2. Checks for grace period (`grace_period:{uuid}` key)
3. Calculates required count via `getManagementFactorCount()`
4. Selects random factors via `selectRandomFactors()` (CSPRNG Fisher-Yates)
5. Creates session in Redis (`mgmt_session:{id}`, 10-minute TTL)

**Step 2: Verify Factors**

```
POST /v1/management/verify
Body: {
  uuid,
  session_id,
  factors: {
    "PIN": "hex-digest-64-chars",
    "PATTERN": "hex-digest-64-chars",
    ...
  }
}

Response: {
  success: true,
  auth_token: "hmac-signed-token",
  access_level: "WRITE",
  expires_at: 1708614000000,
  expires_in_seconds: 900,
  factors_verified: 5,
  factors_enrolled: 8
}
```

The backend:
1. Retrieves and validates session from Redis
2. Checks UUID matches session
3. Checks session not already used (single-use)
4. Retrieves enrolled factor digests from Redis
5. Constant-time comparison for each required factor (no early returns)
6. On success: marks session verified, generates HMAC-SHA256 auth token (15 min)
7. On failure: invalidates session (must re-initiate)

### Factor Selection (CSPRNG)

```javascript
function selectRandomFactors(enrolledFactors, count) {
  const shuffled = [...enrolledFactors];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = crypto.randomInt(i + 1);  // CSPRNG, not Math.random()
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled.slice(0, count);
}
```

---

## Auth Mode System

### Per-Account Authentication Mode

Each merchant, developer, and regular user account can choose their authentication mode:

| Mode | Description | Login Method |
|------|-------------|-------------|
| `password` (default) | Traditional email/password | `POST .../login` with email + password |
| `notap` | NoTap passwordless only | `POST .../notap/login` with NoTap UUID + verification session |
| `both` | Email/password + NoTap as 2FA | Password login, then NoTap verification |

### Setting Auth Mode

```
GET  /v1/auth/{merchant|developer|user}/settings/auth-mode
PUT  /v1/auth/{merchant|developer|user}/settings/auth-mode
Body: { authMode: "password" | "notap" | "both" }
```

Requires JWT authentication. Changes are audit-logged.

### NoTap Login Flow (All 3 User Types)

All three auth routers (`merchantAuthRouter`, `developerAuthRouter`, `regularAuthRouter`) share the same NoTap login pattern:

```
1. Frontend calls POST /v1/verification/initiate with user's notapUuid
2. User completes factor verification in web flow
3. Frontend calls POST /v1/auth/{type}/notap/login with:
   { notapUuid, verificationSessionId }
4. Backend validates session via cacheService.getSession()
5. Checks: session.status === 'verified' && session.uuid === notapUuid
6. Issues user-type-specific JWT token
```

### Linking NoTap to an Account

Users must first link their NoTap enrollment UUID to their account:

```
POST /v1/auth/{merchant|developer|user}/notap/link
Body: { notapUuid }
Requires: JWT authentication (must be logged in with password first)
```

### Database Schema

```sql
-- Migration 026_add_auth_mode.sql
ALTER TABLE developers ADD COLUMN auth_mode VARCHAR(20) NOT NULL DEFAULT 'password'
  CHECK (auth_mode IN ('password', 'notap', 'both'));
ALTER TABLE regular_users ADD COLUMN auth_mode VARCHAR(20) NOT NULL DEFAULT 'password'
  CHECK (auth_mode IN ('password', 'notap', 'both'));
ALTER TABLE merchant_users ADD COLUMN auth_mode VARCHAR(20) NOT NULL DEFAULT 'password'
  CHECK (auth_mode IN ('password', 'notap', 'both'));
```

---

## Login Lockout

### Policy

- **Threshold:** 5 failed login attempts
- **Duration:** 1 hour lockout
- **Scope:** Per-account (not per-IP)
- **Pattern:** Ported from `AdminAuthService.js:129-155`
- **Applied to:** merchants, developers, regular users

### Implementation

Each service (`MerchantUserService`, `DeveloperUserService`, `RegularUserService`) has:

```javascript
// Before bcrypt comparison in loginWithEmail():
async function loginWithEmail(email, password) {
  const user = await findByEmail(email);
  
  // Check lockout
  if (user.account_locked_until && new Date(user.account_locked_until) > new Date()) {
    return { success: false, error: 'Account temporarily locked. Try again later.' };
  }
  
  const isValid = await bcrypt.compare(password, user.password_hash);
  
  if (!isValid) {
    await incrementFailedLogins(user.id);
    return { success: false, error: 'Invalid credentials' };
  }
  
  // Success: reset counter
  await resetFailedLogins(user.id);
  return { success: true, token: generateJWT(user) };
}

// Helper: increment + lock if threshold reached
async function incrementFailedLogins(userId) {
  await pool.query(
    `UPDATE table SET
       failed_login_attempts = failed_login_attempts + 1,
       account_locked_until = CASE
         WHEN failed_login_attempts + 1 >= 5
         THEN NOW() + INTERVAL '1 hour'
         ELSE account_locked_until
       END
     WHERE id = $1`,
    [userId]
  );
}

// Helper: reset on successful login
async function resetFailedLogins(userId) {
  await pool.query(
    `UPDATE table SET failed_login_attempts = 0, account_locked_until = NULL
     WHERE id = $1`,
    [userId]
  );
}
```

### Database Schema

```sql
-- Migration 027_add_login_lockout.sql
ALTER TABLE developers ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE developers ADD COLUMN account_locked_until TIMESTAMPTZ;
-- Same for regular_users and merchant_users
-- Partial indexes: WHERE account_locked_until IS NOT NULL
```

---

## Enrollment Redirect

### Purpose

Enable merchant and developer portals to redirect users to NoTap for enrollment, then receive the enrollment UUID back via callback URL.

### URL Parameters

```
https://enroll.notap.io/?mode=enroll&callback=<url>&context=<string>
https://enroll.notap.io/?mode=enroll&reenrollment_token=<token>&uuid=<uuid>
https://enroll.notap.io/?mode=verify&uuid=<uuid>
https://enroll.notap.io/?mode=manage
```

| Parameter | Description | Example |
|-----------|-------------|---------|
| `mode` | Flow to launch | `enroll`, `verify`, `manage` |
| `callback` | Redirect URL after enrollment | `https://merchant.notap.io/onboarding` |
| `context` | Opaque string passed through to callback | `session_abc123` |
| `reenrollment_token` | Token from recovery flow | `hex-string-64-chars` |
| `uuid` | Existing UUID for re-enrollment or verification | `abc-123-def-456` |

### Domain Allowlist

Redirect callbacks are validated against an allowlist to prevent open-redirect attacks:

```kotlin
private val ALLOWED_REDIRECT_DOMAINS = listOf(
    "notap.io",
    "zeropay.com",
    "localhost"
)
```

### Redirect Flow

```
1. Merchant portal redirects user to:
   https://enroll.notap.io/?mode=enroll&callback=https://merchant.notap.io/done&context=session123

2. User completes 7-step enrollment (including recovery code display)

3. ConfirmationStep validates callback domain against allowlist

4. Auto-redirect after 5 seconds:
   https://merchant.notap.io/done?uuid=<new-uuid>&context=session123&status=success
```

### Frontend Implementation (main.kt)

```kotlin
fun initializeApp() {
    val urlParams = js("new URLSearchParams(window.location.search)")
    val mode = urlParams.get("mode")?.toString()

    when (mode) {
        "enroll" -> {
            val callbackUrl = urlParams.get("callback")?.toString()
            val context = urlParams.get("context")?.toString()
            val reenrollmentToken = urlParams.get("reenrollment_token")?.toString()
            val uuid = urlParams.get("uuid")?.toString()
            EnrollmentFlow.start(root, callbackUrl, context, reenrollmentToken, uuid)
        }
        "verify" -> VerificationFlow.start(root, uuidFromUrl)
        "manage" -> ManagementPortal.start(root)
        else -> renderWelcomeScreen(root)
    }
}
```

---

## Grace Period

### Purpose

After a user recovers their account (via recovery code), they need to re-enroll new factors. During the transition period, the management portal uses reduced factor thresholds so the user can access their account even with fewer or recently-changed factors.

### Configuration

| Parameter | Value |
|-----------|-------|
| Duration | 7 days |
| READ threshold | 40% (reduced from 60%) |
| WRITE threshold | 60% (reduced from 80%) |
| Storage | Redis: `grace_period:{uuid}` with 7-day TTL |
| Activation | Automatic on successful recovery code verification |

### Activation

In `recoveryRouter.js`, after successful recovery code verification:

```javascript
// Set grace period — reduced factor threshold for 7 days after recovery
const gracePeriodKey = `grace_period:${uuid}`;
await redisClient.setEx(
    gracePeriodKey,
    7 * 24 * 60 * 60,  // 7 days in seconds
    JSON.stringify({
        activated_at: Date.now(),
        reason: 'recovery_code_used',
        expires_in_days: 7
    })
);
```

### Usage in Management Router

```javascript
// Check for grace period
const gracePeriodKey = `grace_period:${uuid}`;
const graceActive = await redisClient.get(gracePeriodKey);
const inGracePeriod = !!graceActive;

// Calculate threshold with grace period awareness
const requiredCount = getManagementFactorCount(enrolledFactors.length, level, inGracePeriod);
```

---

## API Reference

### Recovery Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/v1/recovery/verify` | None (code is auth) | Verify recovery code, get re-enrollment token |
| `GET` | `/v1/recovery/status/:uuid` | Management token | Check remaining codes |
| `POST` | `/v1/recovery/regenerate` | Management token | Generate new codes (invalidates old) |
| `POST` | `/v1/enrollment/recovery-codes/:uuid` | None | Generate codes during enrollment |

### Management Endpoints (Updated)

| Method | Path | Auth | Level | Description |
|--------|------|------|-------|-------------|
| `POST` | `/v1/management/initiate` | None | - | Start session, get required factors |
| `POST` | `/v1/management/verify` | None | - | Verify factors, get auth token |
| `DELETE` | `/v1/management/enrollment/:uuid` | Token | WRITE | Delete enrollment (GDPR Art. 17) |
| `GET` | `/v1/management/export/:uuid` | Token | WRITE | Export data (GDPR Art. 15) |
| `PUT` | `/v1/management/auto-renewal/:uuid` | Token | WRITE | Toggle auto-renewal |
| `DELETE` | `/v1/management/payment/:uuid` | Token | WRITE | Unlink payment provider |

### Auth Mode Endpoints (New, All 3 User Types)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/v1/auth/{type}/notap/link` | JWT | Link NoTap UUID to account |
| `POST` | `/v1/auth/{type}/notap/login` | None | Login via NoTap verification |
| `GET` | `/v1/auth/{type}/settings/auth-mode` | JWT | Get current auth mode |
| `PUT` | `/v1/auth/{type}/settings/auth-mode` | JWT | Update auth mode |

Where `{type}` = `merchant`, `developer`, or `user`.

### Merchant-Specific Endpoints (New)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/v1/auth/merchant/request-reset` | None | Request password reset email |
| `POST` | `/v1/auth/merchant/reset-password` | None | Reset password with token |

---

## Database Migrations

### 025_create_recovery_codes.sql

Creates two tables:

**`recovery_codes`** — Stores KMS-encrypted bcrypt hashes
- `owner_type` CHECK ('enrollment', 'merchant', 'developer', 'admin')
- `owner_id` UUID
- `encrypted_code_hash` BYTEA (KMS-wrapped bcrypt hash)
- `kms_key_id`, `key_version` (key rotation support)
- `batch_id` UUID (for atomic batch invalidation)
- `used_at` TIMESTAMPTZ (null = unused)
- Partial index on `owner_type, owner_id WHERE used_at IS NULL`

**`recovery_audit_log`** — Immutable audit trail
- `action` CHECK ('CODES_GENERATED', 'CODES_REGENERATED', 'CODE_USED', 'CODE_FAILED', 'BATCH_INVALIDATED')
- `ip_address_prefix` INET (anonymized)
- `metadata` JSONB

**`cleanup_recovery_audit_logs()`** — PL/pgSQL function for 90-day GDPR retention cleanup

### 026_add_auth_mode.sql

Adds `auth_mode VARCHAR(20) NOT NULL DEFAULT 'password'` with CHECK constraint to `developers`, `regular_users`, `merchant_users`. Partial indexes on `WHERE auth_mode != 'password'`.

### 027_add_login_lockout.sql

Adds `failed_login_attempts INTEGER NOT NULL DEFAULT 0` and `account_locked_until TIMESTAMPTZ` to all three user tables. Partial indexes on `WHERE account_locked_until IS NOT NULL`.

---

## Frontend Integration

### Enrollment Flow (7 Steps)

The enrollment flow is now 7 steps (was 5):

1. **Welcome** — Introduction and requirements
2. **Factor Selection** — Choose 3-15 factors
3. **Factor Enrollment** — Complete each selected factor canvas
4. **Payment Provider** — Optional PSP linking
5. **Review** — Summary of selections
6. **Recovery Codes** (NEW) — Display 8 codes, require confirmation checkbox
7. **Confirmation** — UUID/alias display, optional redirect

### RecoveryCodeStep.kt

- Fetches codes from `POST /v1/enrollment/recovery-codes/{uuid}`
- Displays codes in a numbered grid
- "Copy All Codes" button using Clipboard API
- Confirmation checkbox: "I have saved my recovery codes in a secure location"
- Continue button disabled until checkbox checked
- Memory cleanup: `recoveryCodes = emptyList()` on step exit
- Error handling: Retry button + "Skip (Not Recommended)" with security warning

### VerificationChallengeFlow.kt (Rewritten)

Two-step flow matching the new backend API:

1. Calls `POST /v1/management/initiate` with UUID + access level
2. Receives server-selected factor subset
3. Renders factor canvases one-by-one with progress bar
4. Collects digests as hex strings
5. Calls `POST /v1/management/verify` with session_id + all digests
6. Returns auth token on success

### ManagementPortal.kt Updates

- `showRecoveryForm()` — Full recovery form UI with UUID input + code input
- `handleLogin()` updated to use new `VerificationChallengeFlow.start()` signature
- Recovery button in the login screen for users who can't remember factors

---

## Security Patterns

### Recovery Code Security

| Layer | Mechanism |
|-------|-----------|
| Generation | CSPRNG (`crypto.randomBytes`) |
| Hashing | bcrypt (12 rounds) |
| Encryption | KMS wrapping (AES-256-GCM) |
| Storage | PostgreSQL (encrypted BYTEA) |
| Comparison | bcrypt.compare (inherently constant-time) |
| Memory | `wipeBuffer()` after each hash use |
| Rate limit | 3 attempts per hour per IP |
| Single-use | `used_at = NOW()` after successful match |
| Audit | All operations logged (anonymized IP) |
| GDPR | 90-day audit log retention cleanup |

### Management Portal Security

| Pattern | Implementation |
|---------|---------------|
| Factor selection | CSPRNG Fisher-Yates shuffle (`crypto.randomInt`) |
| Digest comparison | `crypto.timingSafeEqual` with length-leak padding |
| Session management | Redis with 10-minute TTL, single-use |
| Auth tokens | HMAC-SHA256, 15-minute expiry |
| No early returns | All required factors always compared (constant-time) |

### Login Lockout Security

| Pattern | Implementation |
|---------|---------------|
| Threshold | 5 failed attempts = 1 hour lock |
| Reset | Successful login resets counter to 0 |
| Scope | Per-account (prevents distributed attacks) |
| Timing | Lockout check before bcrypt (prevents CPU waste) |

---

## Testing

### Backend Tests

All 140 existing backend tests pass with the new changes. Key areas to test:

```bash
# Run all backend tests
cd backend && npm test

# Run specific test files (when created)
npx mocha tests/services/recovery-code.test.js --timeout 10000
npx mocha tests/routes/management.test.js --timeout 10000
```

### Frontend Compilation

```bash
# Verify Kotlin/JS compiles cleanly
cmd.exe /c 'set "JAVA_HOME=C:\Program Files\Java\jre1.8.0_461" && gradlew.bat :online-web:compileKotlinJs --no-daemon'
```

### Manual Testing Checklist

- [ ] Enrollment flow completes with recovery codes displayed at step 6
- [ ] Recovery codes can be copied to clipboard
- [ ] Continue button disabled until checkbox checked
- [ ] Management portal initiate returns random factor subset
- [ ] Management portal verify with correct digests returns auth token
- [ ] Management portal verify with wrong digests invalidates session
- [ ] Recovery code verification returns re-enrollment token
- [ ] Grace period activates after recovery (check Redis key)
- [ ] Auth mode GET/PUT works for all 3 user types
- [ ] NoTap login works for merchant, developer, regular user
- [ ] Login lockout triggers after 5 failed attempts
- [ ] Lockout clears after 1 hour
- [ ] URL routing works: `?mode=enroll`, `?mode=verify`, `?mode=manage`
- [ ] Redirect enrollment works with callback URL
- [ ] Domain allowlist blocks unauthorized redirect URLs

---

## Troubleshooting

### "No recovery codes available"

**Cause:** All 8 codes have been used, or codes were never generated.
**Fix:** User must re-enroll (if possible) or contact support. After re-enrollment, new codes are generated automatically.

### "Too many recovery attempts"

**Cause:** Rate limit exceeded (3 attempts per hour per IP).
**Fix:** Wait 1 hour and try again. This is enforced per-IP via Redis counter.

### "Session not found or expired"

**Cause:** Management session expired (10-minute TTL) or was already used.
**Fix:** Call `POST /v1/management/initiate` again to create a new session.

### "Account temporarily locked"

**Cause:** 5 failed login attempts triggered the 1-hour lockout.
**Fix:** Wait 1 hour. Successful login resets the counter.

### "Verification service unavailable" (NoTap login)

**Cause:** `cacheService` not available in `req.app.locals`.
**Fix:** Ensure Redis is running and `cacheService` is initialized in `server.js`.

### Grace period not activating

**Cause:** Redis `grace_period:{uuid}` key not being set.
**Fix:** Check that `recoveryRouter.js` has access to `req.app.locals.redisClient` and the `setEx` call is not failing silently.

---

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-02-22 | Initial release: recovery codes, tiered threshold, auth mode, lockout, redirect, grace period |

---

**End of Account Recovery & Portal Authentication System Guide**
