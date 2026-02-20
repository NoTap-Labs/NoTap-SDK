# Critical Development Rules

> **Quick Reference:** See `CLAUDE.md` for the summary table. This file contains the full rules with examples.

---

## 1. KMP Strict Separation

**NO platform-specific code in commonMain - EVER.**
- ❌ `import android.content.Context` in commonMain
- ✅ Use `expect`/`actual` pattern for platform-specific needs

---

## 2. Frontend Security (online-web)

**NEVER use `innerHTML` with dynamic data.**
- ❌ `root.innerHTML = "<div>$data</div>"` (XSS Vulnerability)
- ✅ Use `root.append { div { +data } }` (Type-safe DSL auto-escapes)
- ✅ Use `element.textContent = data` for raw text updates
- ✅ Use `element.clear()` (from `kotlinx.dom.clear`) instead of `element.innerHTML = ""`
- ❌ Never rename parameters referenced in `js()` strings (see Lesson 46)

---

## 3. Module Dependency Rules

```
✅ merchant → sdk
✅ enrollment → sdk
❌ merchant ↔ enrollment (FORBIDDEN circular dependency)
```

---

## 4. Code Reuse

SDK is single source of truth. Both enrollment and merchant use SDK functions.

---

## 5. Context Before Changes

1. Read full file first
2. Understand dependencies
3. Search for references
4. Check related files

---

## 6. Logging (CRITICAL - Security)

**NEVER use `console.log`, `console.error`, or `console.warn` in backend code.**

- ❌ `console.log('User logged in', uuid)` - Exposes PII in logs
- ❌ `console.error('Failed:', err)` - Inconsistent error handling
- ✅ Use `logger` from `./utils/logger` for all logging
- ✅ `logger.info('User logged in', { uuid: uuid.slice(0, 8) })` - Structured
- ✅ `logger.error('Failed to authenticate', { error: err.message })` - Safe
- ✅ `logger.debug('Request details', { body: req.body })` - Disabled in prod

### Why:
- `logger.debug()` is DISABLED in production (NODE_ENV=production)
- `console.*` always outputs, exposing UUIDs, IPs, tokens to logs
- Structured logging enables log analysis without PII exposure

### Migration:
```bash
# Use the automated script:
./scripts/replace-console-log.sh <file>

# Or manually replace:
const logger = require('./utils/logger');
console.log('...') → logger.info/debug('...')
console.error('...') → logger.error('...')
console.warn('...') → logger.warn('...')
```

### Legacy exceptions (tests only):
- Test files (`*.test.js`) may use `console.*` for test output
- Standalone CLI scripts may use `console.*` for user output

---

## 7. No Inline Tests (CRITICAL)

**NEVER include test code inside production source files.**

- ❌ `if (require.main === module) { ... tests ... }` - Inline test block
- ❌ `module.exports = { validate, _test: { ... } }` - Test helpers in production
- ✅ Tests go in `backend/tests/<ModuleName>.test.js`
- ✅ Run tests with: `npm test`

### Why:
- Bloats production code
- Makes code harder to review
- Mixes concerns (production vs testing)
- Violates single responsibility principle

### How to detect:
```bash
# Find inline test blocks
grep -r "require.main === module" backend/
grep -r "if \(require.main" backend/

# Find test helpers in production
grep -r "_test:" backend/
```

### If found:
1. Create proper test file in `backend/tests/`
2. Move test logic to the new test file
3. Remove inline test block from source file

---

## 7. Test-Driven Development & Test Maintenance (CRITICAL)

**Every code change MUST update ALL related tests IMMEDIATELY.**

### For NEW code:
- Write tests FIRST (TDD approach preferred)
- Minimum 80% coverage for new code
- Unit tests for functions/classes
- Integration tests for APIs
- E2E tests for user flows (Bugster)

### For CHANGED code (MANDATORY):

1. **Search for ALL tests using changed code:**
   ```bash
   # Example: Changed ProcessorX.validate() signature
   grep -r "ProcessorX.validate" sdk/src/test/
   grep -r "ProcessorX.validate" sdk/src/commonTest/
   grep -r "ProcessorX.validate" backend/tests/
   ```

2. **Update EVERY test found:**
   - Fix parameter changes
   - Fix return type changes
   - Fix import statements
   - Fix expected behavior

3. **Verify tests compile:**
   ```bash
   # Backend
   cd backend && npm test

   # SDK
   cmd.exe /c 'gradlew.bat :sdk:test --no-daemon'
   ```

4. **Run tests to ensure they pass:**
   - Don't just fix compilation - verify logic
   - Update assertions for new behavior
   - Add new test cases for new edge cases

### Why CRITICAL:
- Outdated tests break CI/CD, blocking all developers
- Test failures in production = hours of debugging
- Compilation errors waste team time
- Major code changes → Major test updates required

### Pattern to Follow:
```
Code change → Search all tests → Update all tests → Verify compilation → Run tests → Commit together
```

### NEVER:
- ❌ Change code without updating tests
- ❌ Commit code that breaks test compilation
- ❌ Leave tests with outdated API signatures
- ❌ Assume tests "probably still work"

### ALWAYS:
- ✅ Update tests in SAME commit as code changes
- ✅ Search for ALL usages before changing APIs
- ✅ Verify test compilation before committing
- ✅ Run full test suite after major changes

---

## 7. Feature Environment Variables

**ALWAYS add feature flags/variables to .env.example when building new features.**

### Required steps:
1. **Add to .env.example** - Document all environment variables with:
   - Clear comments explaining purpose
   - Default values (disabled by default for new features)
   - Examples/recommendations
   - Required dependencies (npm packages, API keys)
   - Security warnings if applicable

2. **Place in migration folder** - If feature requires database schema:
   - ✅ Add SQL to `backend/database/migrations/XXX_feature_name.sql`
   - ❌ Don't leave in `backend/database/schemas/` (won't auto-run)
   - Use sequential numbering (check highest existing number)

### Example - Crypto Payments:
```bash
# .env.example
CRYPTO_PAYMENTS_ENABLED=false  # Master toggle
SOLANA_RELAYER_PRIVATE_KEY=    # Empty = must configure
USDC_ENABLED=true              # Sensible defaults

# Migration
010_add_crypto_payments.sql    # Auto-runs on DB init
```

**Why:** Ensures variables are documented, deployment is smooth, and future developers understand configuration.

---

## 8. Privacy-First Development (CRITICAL - Legal Compliance)

**MANDATORY: Privacy principles MUST be considered in EVERY feature development.**

**Core Principle: "Collect the LEAST amount of data, for the SHORTEST time, with the STRONGEST protection."**

### Privacy Requirements (BLOCKING)

**Before collecting ANY user data, you MUST:**

#### 1. Data Minimization (GDPR Article 5(1)(c), CCPA Section 1798.100)
- ❓ **Ask:** Is this data absolutely necessary?
- ❓ **Ask:** Can we make it optional?
- ❓ **Ask:** Can we infer it instead of collecting it?
- ✅ **Example:** Device ID is now OPTIONAL (users can opt out)
- ❌ **Bad:** Collecting email when UUID is sufficient
- ✅ **Good:** Only collecting UUID (randomly generated)

#### 2. Anonymization/Pseudonymization (GDPR Article 32, CCPA Section 1798.140)
- ✅ **Hash identifiers:** Use `privacyUtils.hashDeviceId()` for device IDs
- ✅ **Anonymize IPs:** Use `privacyUtils.anonymizeIP()` (first 3 octets)
- ✅ **One-way operations:** Cannot reverse hash to original
- ❌ **Never store plaintext:** Device IDs, IPs, or trackable identifiers

```javascript
// ❌ BAD - Plaintext collection
const data = {
  device_id: req.body.device_id,  // Identifiable!
  ip_address: req.ip              // Can track user!
};

// ✅ GOOD - Privacy-enhanced collection
const { hashDeviceId, anonymizeIP } = require('./utils/privacyUtils');
const data = {
  device_id_hash: hashDeviceId(req.body.device_id, user_uuid),  // Hashed
  ip_address_prefix: anonymizeIP(req.ip)                        // Anonymized
};
```

#### 3. Storage Limitation (GDPR Article 5(1)(e), PIPEDA Principle 4.5)
- ✅ **Set retention periods:** Every data type needs expiration
- ✅ **Add to cleanup jobs:** Use `dataRetentionCleanup.js`
- ✅ **Configurable via env vars:** Allow adjustment per jurisdiction
- ❌ **Never permanent storage:** Unless operationally necessary (cryptographic keys)

**Retention periods (configurable via .env):**
- Audit logs: 90 days (`AUDIT_LOG_RETENTION_DAYS`)
- Device IDs: 365 days (`DEVICE_ID_RETENTION_DAYS`)
- IP addresses: 30 days (`IP_ADDRESS_RETENTION_DAYS`)
- Enrollment data: 24 hours (Redis TTL)

#### 4. Purpose Limitation (GDPR Article 5(1)(b), CCPA)
- ✅ **Document purpose:** Why are we collecting this?
- ✅ **Single purpose:** Don't reuse data for different purposes
- ✅ **User consent:** Clear disclosure in enrollment flow
- ❌ **Mission creep:** Collecting for fraud, using for marketing

#### 5. Transparency (GDPR Article 12, CCPA Section 1798.100)
- ✅ **Privacy policy:** Document what we collect and why
- ✅ **Code comments:** Explain privacy measures
- ✅ **User notifications:** Inform users of data collection
- ✅ **Audit trail:** Log data collection in `privacy_migration_log`

### Privacy Testing Requirements

**Every new data collection MUST include privacy tests:**

```javascript
// Example: Test device ID is hashed, not stored plaintext
describe('Privacy: Device ID Collection', () => {
  it('should hash device ID (cannot reverse)', async () => {
    const deviceId = 'iPhone14_iOS17_ABC';
    const uuid = 'test-uuid-123';
    const hash = hashDeviceId(deviceId, uuid);

    // Verify hash is different from original
    expect(hash).not.to.equal(deviceId);

    // Verify hash is deterministic (same input = same output)
    const hash2 = hashDeviceId(deviceId, uuid);
    expect(hash).to.equal(hash2);

    // Verify per-user salt (different UUIDs = different hashes)
    const hash3 = hashDeviceId(deviceId, 'different-uuid');
    expect(hash).not.to.equal(hash3);
  });

  it('should allow enrollment without device ID (optional)', async () => {
    const response = await request(app)
      .post('/v1/enrollment/store')
      .send({
        user_uuid: 'test-uuid',
        factors: { PIN: 'hashed-digest' }
        // No device_id provided
      });

    expect(response.status).to.equal(200);
    expect(response.body.success).to.be.true;
  });
});
```

### Privacy Checklist (Before EVERY Commit)

**When adding new data fields:**

- [ ] Is data absolutely necessary? (Data minimization)
- [ ] Can field be optional? (User consent)
- [ ] Is data hashed/anonymized? (Use privacyUtils)
- [ ] Is retention period set? (Add to cleanup job)
- [ ] Is purpose documented? (Code comments + privacy policy)
- [ ] Are privacy tests included? (Hash verification, optional fields)
- [ ] Is data collection logged? (Audit trail)
- [ ] Can user delete data? (Right to erasure endpoint)

### Privacy Utilities Reference

**Always use privacy utilities for sensitive data:**

```javascript
const {
  hashDeviceId,        // Hash device IDs (SHA-256 + per-user salt)
  anonymizeIP,         // Anonymize IP addresses (first 3 octets)
  getRetentionPeriod,  // Get retention period for data type
  logPrivacyAction     // Log privacy-related actions
} = require('./utils/privacyUtils');

// Example: Collect device ID (privacy-enhanced)
const deviceIdHash = hashDeviceId(req.body.device_id, user_uuid);

// Example: Collect IP address (anonymized)
const ipPrefix = anonymizeIP(req.ip);  // 203.0.113.45 → 203.0.113.0

// Example: Set expiration date
const retentionDays = getRetentionPeriod('device_id');  // 365 days
const expiresAt = new Date(Date.now() + retentionDays * 24 * 60 * 60 * 1000);
```

### Legal Compliance Matrix

| Regulation | Requirement | NoTap Implementation |
|------------|-------------|---------------------|
| **GDPR Article 5(1)(c)** | Data Minimization | Device ID optional, IP anonymized |
| **GDPR Article 5(1)(e)** | Storage Limitation | 90/365/30 day retention (automated) |
| **GDPR Article 17** | Right to Erasure | DELETE endpoint + blockchain revocation |
| **GDPR Article 25** | Privacy by Design | Hash/anonymize at collection time |
| **CCPA Section 1798.100** | Right to Know | GET /export endpoint |
| **CCPA Section 1798.105** | Right to Deletion | DELETE /delete endpoint |
| **PIPEDA Principle 4.4** | Limiting Collection | Optional fields, minimal data |
| **LGPD Articles 6, 7** | Legitimate Purpose | Fraud prevention only |

### Privacy Resources

- **Full Guide:** `documentation/05-security/PRIVACY_IMPLEMENTATION.md`
- **Quick Start:** `backend/PRIVACY_QUICKSTART.md`
- **Privacy Utilities:** `backend/utils/privacyUtils.js`
- **Cleanup Jobs:** `backend/jobs/dataRetentionCleanup.js`
- **Test Examples:** `backend/scripts/testPrivacyUtils.js`

**REMEMBER: Privacy violations can result in fines up to 20M EUR or 4% of global revenue (GDPR). Always err on the side of collecting LESS data, not more.**

---

## 9. Security Patterns (CRITICAL)

> **Full reference:** `documentation/05-security/SECURITY_PATTERNS_REFERENCE.md`

### Must-Know Rules

| Pattern | Rule | Example |
|---------|------|---------|
| **Constant-Time** | ALL digest comparisons | `ConstantTime.equals(a, b)` not `contentEquals` |
| **Memory Wipe** | After using secrets | `finally { digest.fill(0) }` |
| **CSPRNG** | ALL security randomness | `crypto.randomBytes()` not `Math.random()` |
| **No Early Returns** | In verification functions | Always compute digest, even if will fail |
| **Replay Protection** | Public read-only routes skip validation | GET `/supported`, `/health`, `/ping` skip nonce/timestamp |

### Forbidden (NEVER Use)
- ❌ `Math.random()` for security — use `crypto.randomBytes()` or `SecureRandom`
- ❌ MD5, SHA-1, DES, RC4, ECB mode — use SHA-256, AES-256-GCM
- ❌ Hardcoded secrets — use environment variables
- ❌ `contentEquals()` for digest comparison — use `ConstantTime.equals()`

### Required Algorithms
- AES-256-GCM (symmetric encryption)
- SHA-256 (hashing)
- PBKDF2 with 100K+ iterations (key derivation)
- HKDF-SHA256 (key rotation)
- SecureRandom / crypto.randomBytes() (randomness)

### Replay Protection Whitelist Pattern

Public read-only endpoints skip nonce/timestamp validation to be discoverable without pre-configuration. State-changing operations (POST/PUT/DELETE) always get full replay protection.

```javascript
// backend/middleware/replayProtection.js
const PUBLIC_ROUTES_WHITELIST = [
  { path: '/v1/names/supported', method: 'GET' },
  { path: '/v1/names/health', method: 'GET' },
  { path: '/v1/sandbox/config', method: 'GET' },
  { path: '/v1/sandbox/tokens', method: 'GET' },
  { path: '/api/v1/validation-config', method: 'GET' },
  { path: '/api/v1/ping', method: 'GET' },
  { path: '/api/v1/status', method: 'GET' }
];
```

**Key files:** `backend/middleware/replayProtection.js`, `backend/routes/sandboxRouter.js`

### Secret Generation
```bash
openssl rand -base64 32  # 256-bit secrets
openssl rand -hex 32     # API keys
```

### Timing Attack Prevention in Auth Functions

All authentication/verification functions MUST:
1. Always compute the digest comparison even when early failure is detected
2. Add constant timing padding on ALL early returns
3. Never leak timing information about which step failed

---

## 10. Kotlin/JS Critical Patterns

> **Full reference:** `documentation/03-developer-guides/KOTLIN_JS_PATTERNS.md`

### js() Function — Parameter Name Rule (Lesson 46)
Parameters referenced in `js()` strings MUST keep their exact names. NEVER underscore-prefix or rename them. The IR compiler mangles names, causing silent runtime failures (undefined/NaN with no compilation error).

```kotlin
// ❌ BROKEN - _value doesn't match js() reference
fun format(_format: String, _value: Double): String {
    return js("_value.toFixed(3)") as String  // May be undefined!
}
// ✅ CORRECT - parameter names match js() references
fun format(format: String, value: Double): String {
    return js("value.toFixed(3)") as String
}
```

### Kotlinx-html DSL Scope (Lesson 35)
- All HTML generation MUST be inside `root.append { }` block
- Never extract HTML generation to extension functions on `TagConsumer`
- Use `attributes["id"]` not `id`, `onClickFunction` not `onClick`
- `import kotlinx.html.InputType` (explicit import required)

### Async Crypto (Lesson 39)
- `sha256()`, `pbkdf2()`, `hmacSha256()` throw in JS — Web Crypto API is async-only
- Use `sha256Suspend()` in coroutines instead
- `generateRandomBytes()`, `bytesToHex()`, `constantTimeEquals()` work synchronously

### ForceRender Pattern (Lesson 40)
Kotlin/JS HTML DSL creates static HTML. State changes DON'T auto-re-render.
- Call `render(root, forceRender = true)` in event callbacks
- Attach event listeners AFTER DOM creation with `setupEventListeners()`

### Common Replacements
- `String.toByteArray()` → `encodeToByteArray()` (Lesson 1)
- `System.currentTimeMillis()` → expect/actual pattern (Lesson 2)
- `String.format()` → `asDynamic().toFixed(2)` (Lesson 8)
- `Math.random()` → `kotlin.random.Random` (Lesson 8)

---

## 11. Documentation Requirements (BLOCKING for Push)

Every commit that changes code MUST also update:
1. **task.md** — Add timestamped task entry, update version
2. **planning.md** — Add to completed features list, update version
3. **SECURITY_AUDIT.md** — Add new Part for security fixes
4. **LESSONS_LEARNED.md** — Add new lesson for novel patterns discovered
5. **CLAUDE.md + GEMINI.md** — If >500 LOC changed or new patterns/commands (MUST stay in sync)

### Documentation File Routing
- NEVER create .md files in repository root (only README.md, CLAUDE.md, LICENSE allowed)
- All documentation → `documentation/[XX-folder]/` with UPPER_SNAKE_CASE naming
- Full routing rules: `documentation/10-internal/DOCUMENTATION_ROUTING_RULES.md`

---

## 12. Scalability & Modular Development (CRITICAL)

**ALWAYS build with scaling in mind. The system must support plug-and-play infrastructure providers.**

### Provider Abstraction (MANDATORY for New Code)

**When adding new infrastructure dependencies, ALWAYS use the abstraction layer:**

| Use This | Not This |
|----------|----------|
| `req.app.locals.cacheService` | Direct `redisClient` calls |
| `ICacheService` interface | Provider-specific Redis commands |
| `IDatabaseService` interface | Direct `pg` Pool calls |
| `ServiceFactory.createCacheService()` | `new RedisCacheService()` |

### Why:
- Switch cache providers (Redis → KeyDB → Memcached) without code changes
- Switch database providers (PostgreSQL → MySQL → CockroachDB) without code changes
- Enable horizontal scaling (sessions in shared Redis, not in-memory Maps)

### How to Use Cache Service:

```javascript
// ❌ FORBIDDEN - Direct Redis, hard to swap
const redisClient = req.app.locals.redisClient;
await redisClient.setEx(`session:${id}`, 300, data);

// ✅ REQUIRED - Use abstraction layer
const cacheService = req.app.locals.cacheService;
await cacheService.setSession(id, data, 300);
```

### Session Storage Rules:

- ❌ `const sessions = new Map()` — Not shared across API instances
- ✅ `cacheService.setSession()` — Stored in Redis, shared across instances

### New Provider Adapters:

When adding support for a new provider:
1. Create adapter in `backend/services/cache/` or `backend/services/database/`
2. Implement `ICacheService` or `IDatabaseService` interface
3. Add to `ServiceFactory.js`
4. Document in `documentation/04-architecture/PROVIDER_ABSTRACTION.md`

### Environment Variables Pattern:

```bash
# Provider switching (already implemented)
CACHE_PROVIDER=redis    # redis, memory, keydb, memcached
DB_PROVIDER=postgresql  # postgresql, mysql, cockroachdb
```

### Scaling Checklist (Before EVERY Commit)

- [ ] Are new sessions stored in cacheService (not Map)?
- [ ] Are database queries using abstraction (not direct pg)?
- [ ] Is connection pool configurable via env vars?
- [ ] Are rate limits configurable?
- [ ] Can this provider be swapped without code changes?

### Key Files:
- `backend/services/ServiceFactory.js` - Provider creation
- `backend/services/cache/ICacheService.js` - Cache interface
- `backend/services/cache/RedisCacheService.js` - Redis adapter
- `backend/services/cache/MemoryCacheService.js` - Memory adapter
- `backend/services/cache/HybridCacheService.js` - Hybrid Redis + PostgreSQL fallback
- `backend/services/database/IDatabaseService.js` - Database interface
- `documentation/04-architecture/PROVIDER_ABSTRACTION.md` - Full guide
- `documentation/04-architecture/REDIS_HYBRID_ARCHITECTURE.md` - Redis hybrid guide

---

## 13. Scalability & High Availability (MANDATORY)

> **Purpose:** Build with scalability in mind from day one. Never create single points of failure.

### Core Principles

| Principle | Description | Implementation |
|----------|-------------|----------------|
| **Always Use Abstraction** | Never use direct Redis/DB calls | Use `cacheService` and `dbService` |
| **Graceful Degradation** | System must work when dependencies fail | PostgreSQL fallback when Redis down |
| **Horizontal Scaling** | Stateless API instances | Sessions in Redis, not in-memory Maps |
| **Non-Blocking** | Operations should not block each other | Async operations, connection pools |

### The Hybrid Approach (REQUIRED for Production)

**Use `CACHE_PROVIDER=redis-hybrid` for production:**

```bash
# .env for production
CACHE_PROVIDER=redis-hybrid
POSTGRES_FALLBACK_ENABLED=true
FALLBACK_RETRY_ATTEMPTS=3
```

**How it works:**
1. Try Redis first (fast)
2. If Redis fails, retry 3 times with exponential backoff
3. If still failing, fallback to PostgreSQL
4. Re-populate Redis when recovered

### Direct Redis/DB Access (FORBIDDEN)

```javascript
// ❌ FORBIDDEN - No fallback, no scalability
const redisClient = req.app.locals.redisClient;
const data = await redisClient.get(`enrollment:${uuid}`);

// ✅ REQUIRED - Automatic fallback
const cacheService = req.app.locals.cacheService;
const data = await cacheService.get(`enrollment:${uuid}`);
```

### Session Storage Rules

```javascript
// ❌ FORBIDDEN - Not shared across instances
const sessions = new Map();

// ✅ REQUIRED - Shared across all API instances
await cacheService.setSession(sessionId, data, 300);
```

### Scaling Checklist (Before EVERY Commit)

- [ ] New sessions use `cacheService.setSession()` not `Map`
- [ ] No direct `redisClient` calls in routes (use cacheService)
- [ ] Database queries use `dbService` abstraction
- [ ] Operations are non-blocking (async/await)
- [ ] Connection pools are configurable via env vars
- [ ] Graceful degradation when dependencies fail

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `CACHE_PROVIDER` | `redis` | `redis`, `redis-hybrid`, `memory` |
| `POSTGRES_FALLBACK_ENABLED` | `true` | Enable PostgreSQL fallback |
| `FALLBACK_RETRY_ATTEMPTS` | `3` | Redis retries before fallback |

### Key Files

- `backend/services/cache/HybridCacheService.js` - Hybrid Redis + PostgreSQL
- `backend/services/ServiceFactory.js` - Provider factory
- `documentation/04-architecture/REDIS_HYBRID_ARCHITECTURE.md` - Full guide
