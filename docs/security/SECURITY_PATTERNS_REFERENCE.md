# Security Patterns Reference

**Last Updated:** 2025-12-16

This document contains all mandatory security patterns for NoTap development. Following these patterns is NON-NEGOTIABLE.

---

## Quick Reference

| Pattern | When to Use | Key Point |
|---------|-------------|-----------|
| Constant-Time Comparison | ALL digest/secret comparisons | Prevents timing attacks |
| Memory Wiping | After using sensitive data | `finally { data.fill(0) }` |
| CSPRNG | ALL security-related randomness | Never use `Math.random()` |
| Double Encryption | Long-lived secrets (30-day) | PBKDF2 + KMS layers |
| AES-256-GCM | Symmetric encryption | Never use ECB/CBC |

---

## Constant-Time Comparison

**Purpose:** Prevent timing attacks that can leak secret data bit-by-bit.

```kotlin
// ✅ CORRECT
fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) return false
    var result = 0
    for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
    return result == 0
}

// ❌ WRONG - Timing attack vulnerability
if (digest1.contentEquals(digest2)) { ... }
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

## Related Documentation

- [SECURITY_AUDIT.md](SECURITY_AUDIT.md) - Vulnerability audit results
- [ENCRYPTION_SECURITY_AUDIT.md](ENCRYPTION_SECURITY_AUDIT.md) - Encryption implementation audit
- [CLAUDE.md](../../CLAUDE.md) - Main development instructions
