# Security Patterns Reference

**Last Updated:** 2026-02-26

This document contains all mandatory security patterns for NoTap development. Following these patterns is NON-NEGOTIABLE.

---

## Quick Reference

| Pattern | When to Use | Key Point |
|---------|-------------|-----------|
| Constant-Time Comparison | ALL digest/secret comparisons | Prevents timing attacks |
| Memory Wiping | After using sensitive data | `finally { data.fill(0) }` |
| CSPRNG | ALL security-related randomness | Never use `Math.random()` |
| XSS Prevention | Web UI Rendering | Use `append { }` DSL, never `innerHTML` |
| Double Encryption | Long-lived secrets (30-day) | PBKDF2 + KMS layers |
| AES-256-GCM | Symmetric encryption | Never use ECB/CBC |
| Recovery Code Security | Account recovery codes | CSPRNG + bcrypt + KMS wrap |
| Tiered Factor Threshold | Management portal auth | 60/80% proportional, CSPRNG selection |
| Login Lockout | Brute-force protection | 5 attempts = 1hr lock, per-account |
| JWT Rotation | Key migration without logout cascade | Support 2 active secrets (current + previous) |
| CSRF Token Storage | Scalable token management | Use cache service (Redis) with Map fallback |
| Session Storage | Browser credential management | Use `sessionStorage` for per-tab secrets |

---

## Constant-Time Comparison

**Purpose:** Prevent timing attacks that can leak secret data bit-by-bit.

```kotlin
// ✅ CORRECT - True constant-time with NO early returns
fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
    val sizeMatch = a.size == b.size  // Compare without branching
    var result = 0
    val minLength = minOf(a.size, b.size)
    // Always iterate — do NOT exit early
    for (i in 0 until minLength) {
        result = result or (a[i].toInt() xor b[i].toInt())
    }
    return sizeMatch && result == 0  // Single final comparison
}

// ❌ WRONG - Timing attack vulnerability (early return on size mismatch)
fun insecureEquals(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) return false  // LEAKS SIZE VIA TIMING
    var result = 0
    for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
    return result == 0
}

// ❌ WRONG - Timing attack vulnerability (short-circuit on first byte)
if (digest1.contentEquals(digest2)) { ... }  // Early exit on first mismatch
```

**When to use:**
- Comparing authentication digests
- Comparing HMAC signatures
- Comparing API keys/tokens
- Any secret comparison

---

## Memory Wiping

**Purpose:** Prevent sensitive data from lingering in memory after use.

```kotlin
// ✅ CORRECT
try {
    val digest = computeDigest(userInput)
    // use digest
} finally {
    digest.fill(0)  // Always wipe
}
```

**When to use:**
- After using encryption keys
- After using authentication digests
- After using master secrets
- After using any sensitive ByteArray

---

## Factor Verification Pattern

**Purpose:** Prevent timing attacks in authentication flows.

```kotlin
// ✅ CORRECT - Prevents timing attacks
fun verify(input: InputType, storedDigest: ByteArray): Boolean {
    return try {
        val computed = digest(input)  // ALWAYS compute
        ConstantTime.equals(computed, storedDigest)
    } catch (e: Exception) {
        false  // No early returns!
    }
}
```

**Key rules:**
- ALWAYS compute the digest (even if you know it will fail)
- NO early returns in verification functions
- Use constant-time comparison for final check

---

## Secure Random (CSPRNG)

**ALL random number generation for security purposes MUST use cryptographically secure sources.**

### SDK (Kotlin)
```kotlin
val randomBytes = CryptoUtils.generateRandomBytes(32)
val shuffled = CryptoUtils.shuffleSecure(factors)
val nonce = CryptoUtils.generateNonce()
```

### Backend (Node.js)
```javascript
const crypto = require('crypto');
const randomBytes = crypto.randomBytes(32);
const randomInt = crypto.randomInt(100);  // Random int [0, 100)
const uuid = crypto.randomUUID();

// Fisher-Yates shuffle with CSPRNG
function secureShuffleArray(array) {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
        const j = crypto.randomInt(i + 1);  // CSPRNG
        [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
}
```

### FORBIDDEN
```javascript
// ❌ NEVER use Math.random() for:
// - Session IDs
// - Token generation
// - Factor selection
// - Array shuffling (security-sensitive)
// - Nonces or salts
// - Any cryptographic purpose
Math.random()  // ❌ PREDICTABLE - DO NOT USE
```

### CSPRNG Method Reference

| Use Case | Method | Platform |
|----------|--------|----------|
| Random bytes | `crypto.randomBytes(n)` / `CryptoUtils.generateRandomBytes(n)` | Backend/SDK |
| Random integer | `crypto.randomInt(max)` | Backend |
| UUID generation | `crypto.randomUUID()` | Backend |
| Secure shuffle | `CryptoUtils.shuffleSecure()` / custom Fisher-Yates | SDK/Backend |
| Nonce/salt | `CryptoUtils.generateNonce()` | SDK |
| Session tokens | `crypto.randomBytes(64).toString('base64url')` | Backend |

---

## Thread Safety

```kotlin
// ✅ Release read lock before acquiring write lock
val needsWrite = rwLock.read { checkCondition() }
if (needsWrite) rwLock.write { performWrite() }

// ❌ Nested lock acquisition causes deadlock
```

---

## Double Encryption + Daily Rotation Pattern

**Use this pattern for ALL long-lived sensitive secrets (master keys, seeds, approvals).**

### When to Use
- Secrets with 30-day lifetime (not ephemeral tokens)
- Cryptographic material (keys, seeds, signatures)
- High-value targets (payment approvals, authentication factors)

### Architecture (4 Layers)

#### Layer 1: Double Encryption (PBKDF2 + KMS)
```javascript
// Encrypt master secret (backend pattern)
const uuidKey = crypto.pbkdf2Sync(uuid, salt, 100000, 32, 'sha256');
const cipher1 = crypto.createCipheriv('aes-256-gcm', uuidKey, iv1);
const encryptedLayer1 = cipher1.update(masterSeed) + cipher1.final();

const kmsKey = await KMS.getMasterKey();
const cipher2 = crypto.createCipheriv('aes-256-gcm', kmsKey, iv2);
const encryptedLayer2 = cipher2.update(encryptedLayer1) + cipher2.final();
// Store in PostgreSQL
```

#### Layer 2: Daily HKDF Derivation
```javascript
// Derive day-specific key (forward secrecy guaranteed)
const dayIndex = Math.floor((Date.now() - enrollmentDate) / 86400000);
const info = Buffer.from(`zeropay:${type}:day:${dayIndex}`);
const dayKey = crypto.hkdfSync('sha256', masterSeed, salt, info, 32);
// Day N cannot derive Day N+1 (cryptographically impossible)
```

#### Layer 3: Redis Caching (24h TTL)
```javascript
// Cache today's key (auto-expires)
await redis.setex(`${type}:${uuid}`, 86400, dayKey);
// Same pattern as factor digests
```

#### Layer 4: Automatic Rotation (Cron Job)
```javascript
// Daily at 2 AM
cron.schedule('0 2 * * *', async () => {
  for (const record of activeRecords) {
    const masterSeed = await decryptMasterSeed(record);  // KMS + PBKDF2
    const dayKey = deriveDayKey(masterSeed, dayIndex);
    await redis.setex(`${type}:${record.uuid}`, 86400, dayKey);
    masterSeed.fill(0);  // Memory wipe
  }
});
```

### Security Guarantees
- ✅ Forward secrecy (Day N → Day N+1 is cryptographically impossible)
- ✅ 24-hour attack window (not 30 days)
- ✅ Zero server knowledge (master seed decrypted only during rotation)
- ✅ Anti-replay protection (monotonic day_index validation)
- ✅ Automatic expiry (30-day maximum, forced re-enrollment)

### Current Implementations
1. **Factor Digests** - `HKDFDerivation.kt` + `AutoRenewalWorker.kt` (Android)
2. **Session Keys** - `SessionKeyManager.js` + `SessionKeyRotationService.js` (Backend)
3. **Relayer Approvals** - `CryptoRelayerService.js` + `CryptoRelayerRotationService.js` (Backend)

### When Adding New Sensitive Data
- ✅ Use this EXACT pattern (proven in production)
- ✅ Store master secret with double encryption
- ✅ Derive daily keys with HKDF
- ✅ Cache in Redis with 86400s TTL
- ✅ Add cron job for daily rotation
- ✅ Memory wipe after use
- ❌ Don't invent new patterns (stick to proven crypto)

---

## Mandatory Encryption Standards

**ALL sensitive data MUST be cryptographically protected. This is a NON-NEGOTIABLE architectural requirement.**

### Approved Algorithms (Use ONLY These)

| Purpose | Algorithm | Parameters | Status |
|---------|-----------|------------|--------|
| **Symmetric Encryption** | AES-256-GCM | 256-bit key, 96-bit IV, 128-bit tag | ✅ Required |
| **Hashing** | SHA-256 / SHA-512 | - | ✅ Required |
| **Key Derivation** | PBKDF2-HMAC-SHA256 | 100,000+ iterations | ✅ Required |
| **Key Rotation** | HKDF-SHA256 | RFC 5869 | ✅ Required |
| **MAC** | HMAC-SHA256 | - | ✅ Required |
| **RNG** | CSPRNG | crypto.randomBytes / SecureRandom | ✅ Required |

### Forbidden Algorithms (NEVER Use)

| Algorithm | Reason | Replacement |
|-----------|--------|-------------|
| MD5 | Cryptographically broken, collisions trivial | SHA-256 |
| SHA-1 | Collision attacks demonstrated | SHA-256 |
| DES / 3DES | Key size too small | AES-256 |
| RC4 | Multiple vulnerabilities | AES-256-GCM |
| ECB mode | No semantic security | GCM mode |
| Math.random() | Predictable, not cryptographic | crypto.randomInt() |

### Data Classification & Protection Requirements

| Data Type | Encryption | At Rest | In Transit | Example |
|-----------|------------|---------|------------|---------|
| **Passwords/PINs** | NEVER store | Hash with PBKDF2 | TLS 1.2+ | User PIN |
| **Authentication Factors** | PBKDF2 → SHA-256 digest | Redis (24h TTL) | TLS 1.2+ | Pattern, Emoji |
| **Master Keys** | Double encryption (PBKDF2 + KMS) | PostgreSQL | TLS 1.2+ | Factor master keys |
| **Session Tokens** | CSPRNG generation | Redis (TTL) | TLS 1.2+ | JWT, session_id |
| **API Keys** | CSPRNG, hash for lookup | PostgreSQL (hashed) | TLS 1.2+ | ntpk_live_... |
| **User Data** | AES-256-GCM (if stored) | PostgreSQL | TLS 1.2+ | Preferences |
| **Audit Logs** | Integrity (HMAC) | PostgreSQL | TLS 1.2+ | Login events |

### Production Secret Requirements

```javascript
// ✅ REQUIRED: All secrets MUST come from environment variables in production
const JWT_SECRET = process.env.JWT_SECRET;
if (!JWT_SECRET && process.env.NODE_ENV === 'production') {
    throw new Error('JWT_SECRET is required in production');
}

// ❌ FORBIDDEN: Hardcoded secrets (even as fallbacks in production)
const secret = 'hardcoded-secret';  // NEVER DO THIS
```

### Secret Generation Commands

```bash
# Generate 256-bit secret (for JWT, encryption keys)
openssl rand -base64 32

# Generate 512-bit secret (for extra security)
openssl rand -base64 64

# Generate hex secret (for API keys)
openssl rand -hex 32
```

### Encryption Implementation Checklist

When implementing encryption:
- [ ] Use AES-256-GCM (not CBC, not ECB)
- [ ] Generate unique IV per encryption (crypto.randomBytes(12))
- [ ] Store IV with ciphertext (prepend or structured format)
- [ ] Verify authentication tag before decryption
- [ ] Wipe keys from memory after use (buffer.fill(0))
- [ ] Use constant-time comparison for MACs/digests
- [ ] Never log sensitive data (keys, plaintext, digests)

---

## Recovery Code Security Pattern

**Purpose:** Provide account recovery without storing plaintext recovery codes.

### Cryptographic Pipeline

```
CSPRNG generation (crypto.randomBytes)
  → Ambiguity-free alphabet (28 chars: no I/O/0/1)
  → Format: XXXX-XXXX (8 codes per batch)
  → bcrypt hash (12 rounds)
  → KMS wrap (AES-256-GCM envelope encryption)
  → PostgreSQL storage (encrypted BYTEA)
  → wipeBuffer() after each operation
```

### Verification Checklist

- [ ] Codes generated with CSPRNG (`crypto.randomBytes`), never `Math.random()`
- [ ] bcrypt rounds >= 12 for computational cost
- [ ] KMS wrapping provides defense-in-depth (database compromise alone is insufficient)
- [ ] `wipeBuffer()` called on bcrypt hash after each comparison
- [ ] Rate limited: max 3 attempts per hour per IP
- [ ] Single-use: `used_at = NOW()` immediately on match
- [ ] Audit logged: all operations (generation, use, failure, invalidation)
- [ ] IP anonymized before storage (GDPR compliance)
- [ ] Re-enrollment token uses `crypto.randomBytes(32)` with Redis TTL (1 hour)
- [ ] Grace period stored in Redis with 7-day TTL

### Login Lockout Pattern

```javascript
// Pattern: 5 failed attempts = 1 hour lock (per-account, not per-IP)
// Applied to: merchant_users, developers, regular_users
// Ported from: AdminAuthService.js:129-155

// Check BEFORE bcrypt to prevent CPU waste:
if (user.account_locked_until && new Date(user.account_locked_until) > new Date()) {
    return { success: false, error: 'Account temporarily locked' };
}

// On failure: increment + maybe lock
await pool.query(`UPDATE users SET
    failed_login_attempts = failed_login_attempts + 1,
    account_locked_until = CASE
        WHEN failed_login_attempts + 1 >= 5 THEN NOW() + INTERVAL '1 hour'
        ELSE account_locked_until END
    WHERE id = $1`, [userId]);

// On success: always reset
await pool.query(`UPDATE users SET
    failed_login_attempts = 0, account_locked_until = NULL
    WHERE id = $1`, [userId]);
```

### Tiered Factor Threshold Pattern

```javascript
// CSPRNG factor selection — never Math.random()
function selectRandomFactors(factors, count) {
    const shuffled = [...factors];
    for (let i = shuffled.length - 1; i > 0; i--) {
        const j = crypto.randomInt(i + 1);  // CSPRNG
        [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled.slice(0, count);
}

// Constant-time digest comparison with length-leak prevention
function constantTimeEquals(a, b) {
    if (a.length !== b.length) {
        const padded = Buffer.alloc(b.length);
        a.copy(padded, 0, 0, Math.min(a.length, b.length));
        crypto.timingSafeEqual(padded, b);  // Always compare
        return false;
    }
    return crypto.timingSafeEqual(a, b);
}
```

---

## Verification Loop Iteration (No Early Exit)

**Purpose:** Prevent timing oracles that reveal which factor failed in a multi-factor comparison loop.

### The Problem

Using `break` in a factor loop exits early on the first failure. An attacker measures response time to determine WHICH factor failed — the loop position leaks via timing.

```javascript
// ❌ WRONG — timing oracle: response faster if first factor fails
for (const [name, input] of Object.entries(factors)) {
    if (!secureCompare(input, stored)) {
        match = false;
        break;  // NEVER break — reveals which factor was wrong
    }
}

// ✅ CORRECT — always iterate ALL factors regardless of failure
// SECURITY: Always iterate all required factors (no early return)
for (const [name, input] of Object.entries(factors)) {
    const stored = enrollment.factors[name];
    if (!stored) {
        match = false;
        continue;  // keep going even if factor missing
    }
    if (!secureCompare(input, stored)) {
        match = false;
        // no break — keep iterating
    }
}
```

**Rule:** In any factor comparison loop, use `continue` not `break`, and do not return inside the loop.

### `secureCompare` Must Pad on Length Mismatch (Node.js)

When buffer lengths differ, `crypto.timingSafeEqual` throws — a plain `return false` is faster than a real comparison, leaking length via timing. Always pad both buffers first:

```javascript
// ❌ WRONG — early return, no timing work
if (a.length !== b.length) return false;

// ✅ CORRECT — pad, compare (consume constant time), then return false
if (a.length !== b.length) {
    const maxLen = Math.max(a.length, b.length);
    const paddedA = Buffer.alloc(maxLen);
    const paddedB = Buffer.alloc(maxLen);
    a.copy(paddedA);
    b.copy(paddedB);
    crypto.timingSafeEqual(paddedA, paddedB);  // consume time even though result is discarded
    return false;
}
return crypto.timingSafeEqual(a, b);
```

**Canonical implementations:** `utils/constantTimeCompare.js`, `crypto/memoryWipe.js` (`secureCompare`), `auth/jwtService.js`, `auth/tokenManager.js`.

---

## JWT Secret Rotation

**Purpose:** Allow key rotation without invalidating active tokens or requiring full restarts.

**Pattern:**
```javascript
// backend/config/secrets.js
function getJwtSecrets() {
  const current = process.env.JWT_SECRET;
  const previous = process.env.JWT_SECRET_PREVIOUS || null;
  return [current, ...(previous ? [previous] : [])];
}

function verifyJwtWithRotation(token, secrets, jwt) {
  let lastError;
  for (const secret of secrets) {
    try {
      return jwt.verify(token, secret);
    } catch (e) {
      lastError = e;
      if (e.name !== 'JsonWebTokenError') throw e;  // Rethrow expiry/other errors
    }
  }
  throw lastError;  // All secrets failed
}

// In all 5 auth middleware:
const JWT_SECRETS = getJwtSecrets();

// Signing: always use current secret
jwt.sign(payload, JWT_SECRETS[0], { expiresIn: '30m' });

// Verification: try current then previous
verifyJwtWithRotation(token, JWT_SECRETS, jwt);
```

**Rotation process:**
1. Set `JWT_SECRET_PREVIOUS=<old_secret>` in env
2. Update `JWT_SECRET=<new_secret>` in env
3. Restart server
4. Old tokens remain valid until natural expiry (30 minutes)
5. New tokens use new secret only

**Key rules:**
- Always try current secret first (index 0) — fastest path
- Only retry on `JsonWebTokenError` (signature mismatch)
- Immediately rethrow expiry/other errors (no fallback needed)
- Support only 2 versions (current + previous) — no ancient keys

---

## CSRF Token Storage with Cache Service

**Purpose:** Scale CSRF tokens across multiple instances and survive server restarts.

**Pattern:**
```javascript
// backend/middleware/security.js

// Uses cache service (Redis) when available, falls back to in-memory Map
const csrfTokensFallback = new Map();
const CSRF_TTL_SECONDS = 3600;  // 1 hour
const CSRF_PREFIX = 'csrf:';

function _getCacheService(req) {
  return req.app.locals.cacheService;  // Available in production
}

async function _getStoredToken(req, sessionId) {
  const cache = _getCacheService(req);
  if (cache) {
    return cache.get(CSRF_PREFIX + sessionId);  // Redis-backed
  }
  return csrfTokensFallback.get(sessionId);  // Fallback for tests
}

async function csrfProtection(req, res, next) {
  // ... validation ...
  const storedToken = await _getStoredToken(req, sessionId);
  if (!storedToken || !constantTimeCompare(storedToken, token)) {
    return res.status(403).json({ error: 'Invalid CSRF token' });
  }
  next();
}
```

**Key rules:**
- Functions must be `async` to support cache operations
- Always try cache service first (Redis in production)
- Automatic fallback to in-memory Map for test environments
- Use TTL on cache (Redis) instead of manual cleanup (setTimeout)
- Prefix all cache keys (`csrf:sessionId`) to avoid collisions

---

## Session Storage for Browser Credentials

**Purpose:** Store sensitive data (auth tokens, API keys) per-tab, not persistently.

**Pattern:**
```javascript
// Good: API key cleared when tab closes
const apiKey = sessionStorage.getItem('admin_api_key');
sessionStorage.setItem('admin_api_key', apiKey);

// Bad: Persists indefinitely, larger XSS exposure window
const apiKey = localStorage.getItem('admin_api_key');
localStorage.setItem('admin_api_key', apiKey);

// Good: API key in headers, never in URL
fetch('/v1/export', {
  headers: { 'X-Admin-API-Key': apiKey }
});

// Bad: API key logged in proxy/server logs, bookmarkable, shareable
window.location.href = `/v1/export?apiKey=${apiKey}`;
```

**Storage decision:**
- `localStorage` — user preferences, theme, language (non-sensitive)
- `sessionStorage` — auth tokens, API keys, temp session data (sensitive)
- HTTP Headers — all authentication/secrets (never URL params)

**Key rules:**
- Session keys = cleared on tab close
- Credentials in headers only (never URL params)
- Use fetch + Blob for authenticated downloads (not `window.location.href`)

---

## Related Documentation

- [SECURITY_AUDIT.md](SECURITY_AUDIT.md) - Vulnerability audit results
- [ENCRYPTION_SECURITY_AUDIT.md](ENCRYPTION_SECURITY_AUDIT.md) - Encryption implementation audit
- [ACCOUNT_RECOVERY_SYSTEM.md](../03-developer-guides/ACCOUNT_RECOVERY_SYSTEM.md) - Full recovery system guide
- [CLAUDE.md](../../CLAUDE.md) - Main development instructions
