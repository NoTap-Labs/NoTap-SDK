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

## 2b. Session Storage for Browser Credentials

**Use `sessionStorage` for per-tab secrets, NEVER `localStorage` for credentials.**

- ❌ `localStorage.setItem('api_key', key)` — persists indefinitely (large XSS window)
- ✅ `sessionStorage.setItem('api_key', key)` — cleared when tab closes
- ❌ `window.location.href = '/export?apiKey=' + key` — logged in proxy/server/history
- ✅ `fetch('/export', { headers: { 'X-Admin-API-Key': key } })` — headers only

**Storage decision guide:**
```
localStorage  → user preferences (theme, language, non-sensitive)
sessionStorage → auth tokens, API keys, session-scoped secrets
Headers only   → ALL authentication (never URL params)
```

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

## 6. Logging (CRITICAL - Security / GDPR Compliance)

**NEVER use `console.log`, `console.error`, or `console.warn` in backend code.**

- ❌ `console.log('User logged in', uuid)` - Exposes PII in logs
- ❌ `console.error('Failed:', err)` - Inconsistent error handling
- ✅ Use `logger` from `./utils/logger` for all logging
- ✅ `logger.info('User logged in', { uuid: uuid.slice(0, 8) })` - Structured
- ✅ `logger.error('Failed to authenticate', { error: err.message })` - Safe
- ✅ `logger.debug('Request details', { body: req.body })` - Disabled in prod

### Three Mandatory Logging Rules (Enforced by pre-push agent)

**Rule 1 — AUTOMATIC REDACTION: Logger now redacts sensitive fields automatically.**

As of v3.19.8, `logger.js` auto-redacts sensitive keys (password, secret, token, key, digest, private, seed, salt, iv, hash) from all logged objects. Error objects have stack traces stripped in production.

```javascript
// ✅ NOW SAFE — logger redacts sensitive fields automatically
logger.error('Database error:', error);  // error object redacted; stack stripped in prod

// ✅ SAFE — nested sensitive fields redacted
logger.error('Auth failed:', { user_id: 'u123', password: 'xyz' });
// → logs: { user_id: 'u123', password: '[REDACTED]' }

// ✅ SAFE — messages still visible (only sensitive keys redacted)
logger.error('Failed to get config:', { config_key: 'db.host', value: 'localhost' });
// → logs: { config_key: 'db.host', value: 'localhost' }  (not redacted, not sensitive)
```

**When to still use `error.message`:**
- `logger.error('message', error)` — now auto-redacts stack traces (production-safe)
- `logger.error('message', error.message)` — also works, but no longer necessary

**Old Rule (still valid but now redundant):**
```javascript
// ❌ FORBIDDEN in old code — stack trace leaks internal data
logger.error('POST /keys error:', error);

// ✅ REQUIRED in old code — only log the message
logger.error('POST /keys error:', error.message);
```

**Rule 2 — NEVER log user-supplied values in security events. Redact them.**

When logging security blocks (SSRF, rate limits, invalid input), log the parameter NAME but never its VALUE — values come from untrusted user input and may contain tokens, API keys, or PII.

```javascript
// ❌ FORBIDDEN — logs user-controlled data verbatim
logger.warn(`SSRF_BLOCKED: param ${key}="${value}" from IP ${req.ip}`);

// ✅ REQUIRED — log name only, redact value
logger.warn(`SSRF_BLOCKED: param "${key}"=[REDACTED] from IP ${req.ip}`);
```

**Rule 3 — NEVER log variable names containing sensitive terms as their value.**

The compliance checker flags `logger.*` containing the words: `password`, `token`, `secret`, `key`, `credential`, `cvv`, `pan`, `biometric`. If your log message or template literal variable includes these words, it will be blocked.

```javascript
// ❌ BLOCKED — `key` variable in template string triggers checker
logger.error(`Failed to get config ${key}:`, error);  // blocked

// ✅ SAFE — log error.message (not full error), word "key" is in descriptive context
logger.error(`Failed to get config ${key}:`, error.message);  // still shows in message
// → add to verify-compliance.sh exclusion list if false positive
```

### Handling False Positives from the Compliance Checker

If your logger line is semantically safe (logs route names, descriptive context — not actual secrets) but still flagged:

1. **Fix the real risk first**: Change `error` → `error.message`, redact user values with `[REDACTED]`
2. **If still flagged after that**: Add to the exclusion list in `scripts/verify-compliance.sh`
3. **Add a comment** explaining why the log is safe
4. **NEVER bypass** the check with `--no-verify` without fixing the underlying issue

Safe patterns already in exclusion list (see `scripts/verify-compliance.sh`):
- `POST /keys`, `GET /keys`, `DELETE /keys`, `PUT /keys` — route-level error logs (message not value)
- `SSRF_BLOCKED.*REDACTED` — security blocks with redacted user data
- `Failed to (get|set) config` — config key names (not secrets)
- `Re-populated Redis`, `generating.*key`, `validating.*token`, etc.

### Why:
- `logger.debug()` is DISABLED in production (NODE_ENV=production)
- `console.*` always outputs, exposing UUIDs, IPs, tokens to logs
- Full `error` objects contain stack traces that can leak DB credentials, file paths, internal state
- Structured logging enables log analysis without PII exposure
- GDPR prohibits logging sensitive user data without anonymization

### Migration:
```bash
# Use the automated script:
./scripts/replace-console-log.sh <file>

# Or manually replace:
const logger = require('./utils/logger');
console.log('...') → logger.info/debug('...')
console.error('Failed:', err) → logger.error('Failed:', err.message)
logger.warn(`event: ${key}="${value}"`) → logger.warn(`event: "${key}"=[REDACTED]`)
```

### Legacy exceptions (tests only):
- Test files (`*.test.js`) may use `console.*` for test output
- Standalone CLI scripts may use `console.*` for user output

---

## 7a. No Inline Tests (CRITICAL)

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

## 7b. Test-Driven Development & Test Maintenance (CRITICAL)

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

## 7c. Feature Environment Variables

**ALWAYS add feature flags/variables to BOTH `.env.example` AND your local `.env`.**

### Required steps:
1. **Add to `.env.example`** (committed to git — the template) with:
   - Clear comments explaining purpose
   - Default values (disabled by default for new features)
   - Examples/recommendations
   - Required dependencies (npm packages, API keys)
   - Security warnings if applicable

2. **Add to your local `backend/.env`** (gitignored — your machine's config):
   - Copy the new var from `.env.example` with an appropriate local value
   - **This step is required.** Skipping it causes the pre-push agent to warn on every push.
   - The agent compares `.env.example` vs `.env` and warns about missing vars.

3. **Add to CI/CD workflow files** if the var is needed in automated pipelines:
   - `.github/workflows/ci-cd.yml` and any other workflow files

4. **Place in migration folder** if the feature requires database schema:
   - ✅ Add SQL to `backend/database/migrations/XXX_feature_name.sql`
   - ❌ Don't leave in `backend/database/schemas/` (won't auto-run)
   - Use sequential numbering (check highest existing number)

### Example - Crypto Payments:
```bash
# Step 1: Add to .env.example (committed)
CRYPTO_PAYMENTS_ENABLED=false  # Master toggle
SOLANA_RELAYER_PRIVATE_KEY=    # Empty = must configure
USDC_ENABLED=true              # Sensible defaults

# Step 2: Add to backend/.env (local, gitignored)
CRYPTO_PAYMENTS_ENABLED=false
SOLANA_RELAYER_PRIVATE_KEY=your-local-key-here
USDC_ENABLED=true

# Step 3: Migration (if schema changed)
010_add_crypto_payments.sql    # Auto-runs on DB init
```

### Checking sync manually:
```bash
# See which vars are in .env.example but missing from .env
grep -vE "^#|^$" backend/.env.example | cut -d= -f1 | while read var; do
  grep -q "^$var=" backend/.env || echo "MISSING: $var"
done
```

**Why:** `.env` is gitignored (machine-specific secrets), so it never auto-updates when `.env.example` changes. Every developer must manually copy new vars to their local `.env` after pulling changes that add them.

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
5. **CLAUDE.md + GEMINI.md + .antigravity.md** — If >500 LOC changed or new patterns/commands (MUST stay in sync)

### Documentation File Routing
- NEVER create .md files in repository root (only README.md, CLAUDE.md, GEMINI.md, .antigravity.md, LICENSE allowed)
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

---

## 14. Redis Data Retention (CRITICAL - GDPR Compliance)

**NEVER call .set() without expiry — ALL data must have TTL.**

**Rule:** Every Redis key containing user data MUST have an expiration (TTL). No exceptions.

- ❌ `redis.set(key, value)` — GDPR violation (no expiration)
- ✅ `redis.setEx(key, ttlSeconds, value)` — Compliant
- ✅ `redis.set(key, value, 'EX', ttlSeconds)` — Compliant

### Why (Legal Requirement):

- **GDPR Article 5(1)(e) "Storage Limitation"** — Data must be deleted when no longer needed
- **CCPA Section 1798.105** — Right to deletion
- **PIPEDA Principle 4.5** — Retention limits required
- Personal data cannot persist indefinitely in cache

**Violation consequences:**
- Pre-push agent BLOCKS deployment
- GDPR fines up to €20M or 4% revenue
- Legal liability for data breaches

### Default TTLs (Use as Guidelines):

| Data Type | TTL | Env Var | Reason |
|-----------|-----|---------|--------|
| **Session data** | 300-900s (5-15 min) | `SESSION_TTL` | Active use only |
| **Factor digests** | 86400s (24 hours) | N/A | Daily rotation |
| **Audit logs** | 7776000s (90 days) | `AUDIT_LOG_RETENTION_DAYS` | Compliance requirement |
| **Verification sessions** | 300s (5 min) | `VERIFICATION_SESSION_TTL` | Transaction window |
| **Rate limit counters** | 3600s (1 hour) | N/A | Rolling window |
| **Nonces** | 300s (5 min) | `NONCE_TTL` | Replay protection |

### Code Pattern (Defensive):

```javascript
// ✅ CORRECT - TTL is required
async function cacheUserData(key, data, ttlSeconds) {
  if (!ttlSeconds || ttlSeconds <= 0) {
    throw new Error('TTL required for GDPR compliance — all cached data must have expiration');
  }
  await redis.setEx(key, ttlSeconds, data);
}

// ✅ CORRECT - With default TTL
const DEFAULT_SESSION_TTL = 900; // 15 minutes
await redis.setEx(`session:${id}`, DEFAULT_SESSION_TTL, sessionData);

// ❌ WRONG - No TTL (will throw error in HybridCacheService/RedisCacheService)
await redis.set(`session:${id}`, sessionData);  // GDPR violation
```

### Migration from .set() to .setEx():

```bash
# Find all .set() calls without TTL
grep -rn "\.set(" backend/services backend/routes | grep -v "setEx\|'EX'\|'PX'"

# Pattern replacement:
# Before: await redis.set(key, value);
# After:  await redis.setEx(key, appropriateTTL, value);
```

### Detection & Enforcement:

**Pre-push agent checks:**
```bash
# Agent 3 (verify-compliance.sh) scans for:
grep -rE "(redisClient|redis)\.set\(" backend/ | grep -vE "'EX'|'PX'|ttl"
```

**Blocked if found:**
- ⛔ Push rejected with GDPR violation warning
- Must fix before merge

### See Also:

- `documentation/05-security/PRIVACY_IMPLEMENTATION.md` — Full GDPR guide
- `backend/services/cache/HybridCacheService.js:235` — TTL enforcement implementation
- `backend/services/cache/RedisCacheService.js:75` — TTL enforcement implementation

---

## 15. Constant-Time Security Functions (CRITICAL - Timing Attacks)

**NEVER create private copies of security functions.**

**Rule:** Security-critical functions MUST have a single canonical implementation. Duplication leads to vulnerable copies, inconsistency, and maintenance burden.

- ❌ `fun privateConstantTimeEquals() { ... }` — Duplication, likely vulnerable
- ✅ `import com.zeropay.sdk.crypto.ConstantTime` — Canonical source

### Canonical Security Functions (NEVER duplicate):

| Function | Location | Purpose |
|----------|----------|---------|
| **ConstantTime.equals()** | `sdk/src/commonMain/kotlin/com/zeropay/sdk/crypto/ConstantTime.kt` | All secret comparisons |
| **CryptoUtils.generateRandomBytes()** | `sdk/src/commonMain/kotlin/com/zeropay/sdk/crypto/CryptoUtils.kt` | All randomness |
| **CryptoUtils.shuffleSecure()** | `sdk/src/commonMain/kotlin/com/zeropay/sdk/crypto/CryptoUtils.kt` | Sensitive array shuffling |
| **CryptoUtils.generateNonce()** | `sdk/src/commonMain/kotlin/com/zeropay/sdk/crypto/CryptoUtils.kt` | Nonce generation |
| **constantTimeCompare()** (Backend) | `backend/utils/constantTimeCompare.js` | All string/token comparisons (Node.js) |

### Why Single Source of Truth:

1. **Security consistency** — One fix applies everywhere, no vulnerable copies
2. **Audit efficiency** — Security review in one place
3. **No drift** — Can't accidentally reintroduce vulnerabilities
4. **Documentation clarity** — Clear canonical reference in SECURITY_PATTERNS_REFERENCE.md
5. **Testing** — One implementation = thorough testing, not scattered edge cases

### What NOT to Do:

```kotlin
// ❌ WRONG - Private vulnerable copy
object MyFactor {
    // This will likely be vulnerable (early return on size, etc.)
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false  // Timing leak!
        var result = 0
        for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
        return result == 0
    }

    fun verify(input: String, storedDigest: ByteArray): Boolean {
        val computed = digest(input)
        return constantTimeEquals(computed, storedDigest)  // Using private copy
    }
}
```

### What TO Do:

```kotlin
// ✅ CORRECT - Import canonical implementation
import com.zeropay.sdk.crypto.ConstantTime

object MyFactor {
    fun verify(input: String, storedDigest: ByteArray): Boolean {
        val computed = digest(input)
        return ConstantTime.equals(computed, storedDigest)  // Canonical source
    }
}
```

### Detection (Pre-Commit):

```bash
# Find private timing-safe functions (potential duplication)
grep -rn "fun.*TimeEquals\|fun.*constantTime" sdk/src --include="*.kt"

# Find hardcoded comparisons on secrets (vulnerable pattern)
grep -rn "\.contentEquals\|===" sdk/src --include="*.kt" | grep -i "digest\|secret\|token"

# Backend (JavaScript)
grep -rn "===.*apiKey\|!==.*apiKey" backend/ --include="*.js"
```

### Migration Pattern:

**Before:**
```kotlin
// Each factor had its own copy (4 files duplicating same logic)
private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean { ... }
```

**After:**
```kotlin
// All factors import from canonical location (1 audited implementation)
import com.zeropay.sdk.crypto.ConstantTime
// Use: ConstantTime.equals(a, b)
```

### Deprecated Locations:

- ❌ `sdk/src/commonMain/kotlin/com/zeropay/sdk/security/ConstantTime.kt` — Old location, now `@Deprecated`
- ✅ `sdk/src/commonMain/kotlin/com/zeropay/sdk/crypto/ConstantTime.kt` — New canonical location

**Compiler enforces migration:** Deprecated annotation guides developers to new location.

### Pre-Push Verification:

**Agent 1 (verify-patterns.sh) checks:**
- No private `constantTime*` functions
- No `contentEquals()` on digest variables
- No `===` on secret/token/key variables

**Blocked if found:**
- ⛔ Push rejected with security pattern violation
- Must migrate to canonical implementation

### See Also:

- `documentation/05-security/SECURITY_PATTERNS_REFERENCE.md` — Constant-time implementation details
- `documentation/10-internal/LESSONS_LEARNED.md` — Lesson 51: Constant-Time Unification
- `documentation/05-security/SECURITY_AUDIT.md` — Part 10: Dead Code Removal (4 files fixed)

---

## 16. Account Recovery & Auth Patterns (CRITICAL)

### Recovery Code Generation

Recovery codes MUST follow the defense-in-depth pipeline:
1. **CSPRNG generation** (`crypto.randomBytes`) — NEVER `Math.random()`
2. **Ambiguity-free alphabet** (28 chars: no I, O, 0, 1)
3. **bcrypt hash** (12+ rounds)
4. **KMS wrapping** before PostgreSQL storage
5. **Memory wipe** after each hash comparison (`wipeBuffer()`)

**Reference implementation:** `backend/services/RecoveryCodeService.js`

### Login Lockout (Brute-Force Protection)

All user-facing login endpoints MUST implement lockout:

```javascript
// Pattern: check lockout AFTER bcrypt (prevent timing oracle — locked vs unlocked timing diff)
// Order: bcrypt comparison → lockout check → status checks → all return "Invalid email or password"
const bcryptMatch = await bcrypt.compare(password, user.password_hash);
if (!bcryptMatch) {
    await incrementFailedAttempts(user.uuid);
    return { success: false, error: 'Invalid email or password' };
}
if (user.account_locked_until && new Date(user.account_locked_until) > new Date()) {
    return { success: false, error: 'Invalid email or password' };
}

// On success: always reset to 0
```

**Configuration:** 5 failed attempts = 1 hour lockout (per-account, not per-IP).

**Applied to:** `MerchantUserService`, `DeveloperUserService`, `RegularUserService` (and already existed in `AdminAuthService`).

### Auth Mode System

When adding new user types that support NoTap login:

1. Add `auth_mode` column: `VARCHAR(20) NOT NULL DEFAULT 'password' CHECK (IN ('password', 'notap', 'both'))`
2. Add `notap_uuid` column for linking
3. Implement NoTap login using verification session pattern:
   ```javascript
   const session = await cacheService.getSession(verificationSessionId);
   if (session?.status !== 'verified' || session?.uuid !== notapUuid) { /* reject */ }
   ```
4. Add `GET/PUT /settings/auth-mode` endpoints
5. Add `POST /notap/link` and `POST /notap/login` endpoints

### Tiered Factor Threshold

When implementing factor-based authentication for management/admin portals:

1. **Use proportional thresholds**, not 100% factor requirement
2. **CSPRNG factor selection** (`crypto.randomInt` in Fisher-Yates shuffle)
3. **Two-step flow**: `POST /initiate` (get factors) → `POST /verify` (submit digests)
4. **Grace period** after recovery: reduced thresholds stored in Redis with TTL
5. **Single-use sessions**: invalidated on success or failure

### Single module.exports Rule

Each Node.js service file MUST have exactly ONE `module.exports` at the bottom. Dead early exports cause silent bugs (see Lesson 52).

```bash
# Check for duplicate exports:
grep -n "module\.exports" backend/services/*.js | awk -F: '{print $1}' | sort | uniq -d
```

---

## 17. Compliance-First Development (MANDATORY)

**Every new feature >100 LOC MUST include a Compliance Matrix BEFORE writing code.**

### Why This Rule Exists

Root cause (2026-03-16): An implementation plan for Agent Protocol Integration was designed feature-first instead of compliance-first. This produced 6 violations that were caught only during post-design audit:
- Callback URL stored without SSRF middleware wired in
- Callback secret stored unencrypted in Redis
- Usage log used bare SHA-256 instead of salted hash via `privacyUtils`
- Biometric factors via new flow skipped BIPA jurisdiction check
- External response body logged without sanitization
- Enrollment info endpoint returned more data than needed (data minimization)

**Fix:** Compliance matrix is the FIRST deliverable, not the last. Code is designed around the matrix.

### Required Deliverables (Before Code)

1. **Data Handling Matrix** — What data enters, is stored, leaves, and is explicitly NOT collected. For each: format, encryption, anonymization, retention, cleanup method.

2. **Legal Basis Matrix** — GDPR Article 6 basis for each stored data point: Contract 6(1)(b), Legitimate Interest 6(1)(f), Legal Obligation 6(1)(c), or Consent 6(1)(a).

3. **Security Controls Matrix** — For each new endpoint: auth method, rate limit, replay protection, SSRF check, audit logging, input sanitization.

4. **Risk Assessment** — Threat model (what if key compromised? what if URL manipulated? what if data leaked?), blast radius, regulatory fine exposure.

5. **Regulatory Matrix** — Map each data point against: GDPR, BIPA (if biometric), CCPA, PIPEDA, LGPD.

### Where To Put It

`documentation/05-security/[FEATURE_NAME]_COMPLIANCE.md`

**Reference template:** `documentation/05-security/AGENT_INTEGRATION_COMPLIANCE.md`

### Never Bypass

- NEVER skip compliance matrix for speed
- NEVER store data without identified legal basis
- NEVER accept external URLs without SSRF validation
- NEVER store secrets without encryption + memory wipe
- NEVER log data without checking sanitization rules
- NEVER expose user data without data minimization check
- NEVER allow biometric factors without jurisdiction/BIPA check

---

## 18. Stateless Service Design — Scalability-First (MANDATORY)

**All backend services MUST be safe for horizontal scaling (multiple instances).**

### Why This Rule Exists

Root cause (2026-03-20): The `dataRetentionCleanup.js` used a module-level mutable `CleanupStats` singleton. If two cleanup runs execute concurrently (e.g., two containers), they overwrite each other's statistics — a race condition. Similar patterns in other services could cause data corruption under horizontal scaling.

### Rules

1. **No module-level mutable state** — Use factory functions (`createX()`) that return fresh objects. Module-level `const` config is fine; module-level `let`/mutable objects are not.

2. **Use cacheService/dbService abstractions** — Never call Redis or PostgreSQL directly. Abstractions enable provider swaps (Redis → Valkey, PG → CockroachDB) without touching service code.

3. **Batch operations for bulk data** — Use `BATCH_SIZE` pattern with SCAN/cursor pagination. Never `KEYS *` or unbounded `SELECT`.

4. **Non-blocking side effects** — Fire-and-forget operations (audit logs, callback delivery, usage tracking) must use `.catch()` pattern. Never let a side effect failure block the main flow.

5. **SSRF re-validation at delivery time** — URLs validated at registration may resolve differently later (DNS rebinding). Always re-validate with `validateURL()` before making outbound HTTP requests.

6. **Dependency injection via constructor/config** — Services should accept dependencies (db, cache, logger) as parameters, not import singletons. Enables testing and provider swaps.

7. **Plug-and-play services** — Each service must be independently replaceable. No circular imports between services. Shared utilities go in `utils/` or `crypto/`.

### Quick Checklist

- [ ] No `let` or mutable objects at module scope (except lazy singletons with `resetX()`)
- [ ] All Redis/DB access through abstraction layer
- [ ] Bulk operations use SCAN + BATCH_SIZE
- [ ] Side effects are non-blocking (`.catch()`)
- [ ] Outbound URLs re-validated before delivery
- [ ] Service accepts deps as params, not global imports

---

### See Also:

- `documentation/03-developer-guides/ACCOUNT_RECOVERY_SYSTEM.md` — Full recovery system guide
- `documentation/05-security/SECURITY_PATTERNS_REFERENCE.md` — Recovery code crypto patterns
- `documentation/05-security/AGENT_INTEGRATION_COMPLIANCE.md` — Reference compliance document
- `documentation/10-internal/LESSONS_LEARNED.md` — Lessons 52-54, 58-64

---

## 19. Automated Violation Detection (MANDATORY)

**Three layers of automated detection prevent security/privacy regressions.**

### Why This Rule Exists

Root cause (2026-03-20): Full-project audit discovered 30+ violations (unsalted hashes, raw IPs in logs, console.log bypassing PII redaction, private DB pools, Redis without TTL) that had been present for months. No automated system caught them because checks only covered KMP separation and a few security patterns.

### Layer 1: Startup Validator (`backend/utils/startupValidator.js`)

Runs at server boot. Checks environment configuration (required/recommended env vars, PRIVACY_APP_SALT strength, production-specific settings). Logs structured warnings — does NOT block startup. Violations appear in observability dashboards.

### Layer 2: Pre-Push Agent (`scripts/verify-patterns.sh`)

Runs before every `git push`. Checks 20-23 added for this audit:
- Check 20: `console.*` in backend production code → **BLOCKS push**
- Check 21: Unsalted SHA-256 hashes → **BLOCKS push**
- Check 22: Private `new Pool()` creation → **BLOCKS push**
- Check 23: Broken `require('../config/redis')` → **BLOCKS push**

### Layer 3: CI Audit Script (`scripts/audit-violations.sh`)

Run in CI pipeline or manually. 10 checks covering all audit violations:
1. console.* in production code
2. Unsalted SHA-256 hashes
3. Raw IP addresses in logs
4. Redis .set() without TTL
5. Private DB Pool creation
6. Module-level mutable state
7. Math.random() usage
8. Hardcoded secrets
9. Broken redis config imports
10. Permission leaks in error responses

### Adding New Checks

When a new violation pattern is discovered:
1. Add a lesson to `LESSONS_LEARNED.md` (root cause + fix)
2. Add a check to `scripts/audit-violations.sh` (CI detection)
3. If blocking, add to `scripts/verify-patterns.sh` (pre-push gate)
4. If runtime-detectable, add to `backend/utils/startupValidator.js`

---

## 20. Verify Before You Write (MANDATORY — BLOCKING)

**Severity: BLOCKING — applies to ALL contributors (human, LLM agent, AI assistant, IDE copilot)**

**NEVER write code that references existing modules, services, utilities, or APIs without first reading the actual source files.** Assumptions about file paths, export names, function signatures, or class APIs are FORBIDDEN.

### Why This Rule Exists

**Incident (2026-03-22):** `bipaConsentRouter.js` was deployed to production with 3 non-existent imports:
1. `require('../database/pool')` — file was `../database/database`
2. `require('../services/auditService')` — file was `../services/AuditService` (PascalCase)
3. Called `logAuditEvent({eventAction, metadata})` — actual API was `new AuditService(pool).logEvent({eventType, uuid, details, severity})`

Each was an assumption that could have been caught in <30 seconds by reading the actual files. Result: **3 consecutive production crashes, 4 emergency commits, ~30 minutes of downtime.**

### What You MUST Verify

Before writing ANY `require()`, `import`, or function call to an existing module:

| Check | How to verify | Example of failure |
|-------|---------------|-------------------|
| **File exists** | `ls` / Glob the exact path | `require('../database/pool')` — file was `database/database` |
| **Correct casing** | Read the actual filename on disk | `require('../services/auditService')` — file was `AuditService.js` |
| **Export shape** | Read the `module.exports` or `export` block | Expected standalone `logAuditEvent()` — actual was `AuditService` class |
| **Function signature** | Read the function/method definition | Called with `{eventAction, metadata}` — actual params were `{eventType, uuid, details, severity}` |
| **Pattern consistency** | Check how other files in the same directory use the module | All routers used `require('../database/database')` — bipaConsentRouter guessed `../database/pool` |
| **Transitive deps** | Verify the module's own imports also resolve | A module that imports a non-existent file will crash at require time |

### The Verification Workflow

```
1. PLAN    → Identify which existing modules/services you need
2. READ    → Open each module, read its exports and function signatures
3. COMPARE → If your planned usage doesn't match the actual API, adjust your plan
4. ASK     → If unsure about the intended pattern — ASK, don't guess
5. CODE    → Only now write the implementation
6. VERIFY  → Run `node -e "require('./path/to/file')"` to confirm no import crashes
```

### Planning is MANDATORY

For any non-trivial work (>50 LOC or >2 files), you MUST plan before coding. The plan MUST include:

- **Dependency inventory:** List every existing file that will be imported or modified
- **Verified exports:** For each dependency, document the actual export shape (read from source, not assumed)
- **Pattern check:** How do other files in the codebase use this module? Follow the same pattern
- **Compliance review:** Cross-reference with DEVELOPMENT_RULES.md, LESSONS_LEARNED.md, SECURITY_PATTERNS_REFERENCE.md
- **Architecture review:** Does the plan align with ARCHITECTURE.md and existing module boundaries?

**If you have doubts about a module's API, file location, or pattern — READ the codebase. If still unclear — ASK. Never proceed on assumption.**

### Quick Reference: Common Patterns to Verify

| Module | Correct import | Common mistake |
|--------|---------------|----------------|
| Database pool | `require('../database/database')` | `require('../database/pool')` |
| Audit logging | `new AuditService(pool).logEvent({...})` | Standalone `logAuditEvent()` |
| Privacy utils | `require('../utils/privacyUtils')` | `require('../utils/privacy')` |
| Logger | `require('../utils/logger')` | `require('../logger')` or `console.log` |
| Secrets | `require('../config/secrets')` | `process.env.SECRET` directly |
| Constant-time | `require('../utils/constantTimeCompare')` | Inline `timingSafeEqual` |
| Cache service | `require('../services/cacheService')` | Direct `redis.set()` |
| DB service | `require('../services/dbService')` | Direct `pool.query()` from new Pool |

### Enforcement

This rule cannot be fully enforced by static analysis, but:
1. **`node -e "require('./server')"` in pre-push** catches broken imports before they reach production
2. **Code review** must verify imports match actual source files
3. **LLM system prompts** (CLAUDE.md, gemini.md, .antigravity.md) mandate this as a blocking rule
4. **Planning phase** must include dependency verification as a checklist item

### NEVER

- ❌ Assume a module exists because the name "sounds right"
- ❌ Assume a function signature because "it probably works like X"
- ❌ Assume file casing — Node.js on Linux is case-sensitive
- ❌ Skip reading exports when using a module for the first time
- ❌ Copy import patterns from AI training data instead of reading actual project files
- ❌ Proceed with coding when unsure about a dependency — ask first

---

## 21. Kotlin/JS Object Singletons — Always Store Parameters Explicitly (MANDATORY)

**Applies to:** `online-web` canvas objects (and any Kotlin/JS `object` declaration that receives params in one function and uses them in another)

### The Problem

Kotlin `object` declarations are singletons. A parameter received in `render()` is **not** automatically available in `submit()` — they are separate function invocations. Without an explicit field assignment, the submit function reads the zero-value field (`""`), not the caller's value.

### The Rule

**Every `object` canvas MUST store caller parameters to object fields immediately in `render()`, after `cleanup()`, before creating the coroutine scope:**

```kotlin
fun render(root: HTMLDivElement, onComplete: suspend (ByteArray) -> Unit, uuid: String = "") {
    cleanup()
    this.uuid = uuid   // ← MANDATORY — do this for every parameter used outside render()
    coroutineScope = CoroutineScope(...)
```

### Checklist for new canvas objects

- [ ] `private var uuid: String = ""` declared as object field
- [ ] `this.uuid = uuid` first assignment after `cleanup()` in `render()`
- [ ] All other caller parameters that `submit*()` needs are also stored as object fields
- [ ] Object fields reset in `cleanup()` if they should not persist across re-renders

### See Also

Lesson 82 in `documentation/10-internal/LESSONS_LEARNED.md`

---

## 22. Drawing Canvas Input — Always Register Document-Level Pointer End Events (MANDATORY)

**Applies to:** Any canvas using `isDrawing` state tracked by mousedown/mouseup (or pointerdown/pointerup)

### The Problem

Element-scoped `mouseup` only fires when the pointer is released **over that element**. On desktop, users frequently press on canvas, drag beyond its bounds (common in pattern selection), and release outside — the element-level listener never fires, `isDrawing` stays `true`, and the Submit button stays permanently disabled.

### Required Listener Pattern

```kotlin
// For mouse-based drawing canvases:
canvasMouseLeaveListener = { _: Event -> endDrawing() }   // stops mid-drag exit
canvas?.addEventListener("mouseleave", canvasMouseLeaveListener!!)

documentMouseUpListener = { _: Event -> endDrawing() }    // catches release anywhere
document.addEventListener("mouseup", documentMouseUpListener!!)

// For touch/stylus (Pointer Events API):
documentPointerUpListener = { _: Event -> if (isDrawing) stopDrawing() }
document.addEventListener("pointerup", documentPointerUpListener!!)
```

### Mandatory Cleanup

All document-level listeners MUST be removed in `cleanup()`:

```kotlin
fun cleanup() {
    documentMouseUpListener?.let { document.removeEventListener("mouseup", it) }
    documentPointerUpListener?.let { document.removeEventListener("pointerup", it) }
    documentMouseUpListener = null
    documentPointerUpListener = null
}
```

**Why:** Document listeners survive DOM removal. Without cleanup, each re-render accumulates ghost listeners that call `endDrawing()`/`stopDrawing()` on the next canvas.

### Checklist

- [ ] `isDrawing` canvases have `mouseleave` OR `pointerleave` on the canvas element
- [ ] `isDrawing` canvases have `mouseup`/`pointerup` on `document`
- [ ] Touch canvases have `touchend` on `document`
- [ ] All document listeners tracked in nullable fields and removed in `cleanup()`

### See Also

Lesson 83 in `documentation/10-internal/LESSONS_LEARNED.md`

---

## Rule 23: Ownership Verification on All User-Scoped Endpoints (Backend)

**Date added:** 2026-04-27
**Trigger:** BIPA IDOR found during full security audit sprint

### The Problem

Authentication confirms WHO the caller is. It does not confirm they have the RIGHT to access a specific resource. Without an explicit ownership check, an authenticated user can read or write any other user's data by substituting a different UUID in the URL or body.

```javascript
// ❌ WRONG — authenticated but no ownership check
router.delete('/consent/:userUuid', authenticateUser, async (req, res) => {
  const { userUuid } = req.params;
  await revokeConsent(userUuid);  // ANY user can revoke ANY other user's consent
});
```

### The Fix

```javascript
// ✅ CORRECT — authentication + ownership assertion
router.delete('/consent/:userUuid', authenticateUser, async (req, res) => {
  const { userUuid } = req.params;
  if (req.user.uuid !== userUuid) {
    return res.status(403).json({ error: 'Access denied' });
  }
  await revokeConsent(userUuid);
});
```

### When This Rule Applies

Add an ownership check whenever:
- The route URL or body contains a user UUID that identifies the resource owner
- The route reads, updates, or deletes user-specific data (consent, payment tokens, account settings, biometrics)
- The caller is a regular user (not an admin acting on behalf of a user)

Admin routes that intentionally allow cross-user access are exempt — but must be protected by admin auth middleware (`requireAdminAuth`).

### Checklist

- [ ] Every user-scoped route has auth middleware (`authenticateUser` or `authenticateMerchant`)
- [ ] The handler compares `req.user.uuid` (or `req.merchantUuid`) to the resource's owner before mutating
- [ ] 403 is returned (not 404) when ownership check fails — 404 leaks resource existence

### See Also

- Lesson 94 in `documentation/10-internal/LESSONS_LEARNED.md`
- SECURITY_PATTERNS_REFERENCE.md — Ownership Assertion Pattern

---

## Rule 24: KMP `expect class` Must Explicitly Declare All Interface Members (Kotlin 2.x) (MANDATORY)

**Date added:** 2026-04-29
**Trigger:** `compileCommonMainKotlinMetadata` failure after Kotlin 2.1.20 upgrade

### The Problem

Kotlin 2.x K2 compiler requires that every abstract member of an interface supertype be explicitly listed in the `expect class` body. Kotlin 1.x inferred them silently. Omitting members causes `compileCommonMainKotlinMetadata` to fail:
```
error: 'actual' modifier is required on 'size' in ConcurrentMap
error: 'actual' modifier is required on 'entries' in ConcurrentMap
```

### The Rule

When writing `expect class Foo : SomeInterface`, list **every** abstract member in the expect body:

```kotlin
// ✅ CORRECT — all members declared explicitly
expect class ConcurrentMap<K : Any, V : Any>() : MutableMap<K, V> {
    override val size: Int
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override fun isEmpty(): Boolean
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?
    override fun put(key: K, value: V): V?
    override fun remove(key: K): V?
    override fun putAll(from: Map<out K, V>)
    override fun clear()
}

// ❌ WRONG — compiles on Kotlin 1.x, breaks compileCommonMainKotlinMetadata on 2.x
expect class ConcurrentMap<K : Any, V : Any>() : MutableMap<K, V>
```

### Corresponding `actual` classes

Every member declared in the `expect` must use `actual override` (not just `override`) in each `actual` implementation:

```kotlin
actual class ConcurrentMap<K : Any, V : Any> : MutableMap<K, V> {
    private val map = ConcurrentHashMap<K, V>()
    actual override val size: Int get() = map.size   // ← actual override, not just override
    actual override fun isEmpty(): Boolean = map.isEmpty()
    // ... all other members
}
```

### Checklist

- [ ] Every `expect class : Interface` lists all abstract members in its body
- [ ] Every member in the expect body uses `actual override` in each actual implementation
- [ ] `compileCommonMainKotlinMetadata` runs cleanly (triggered by any JS target)

### See Also

Lesson 97 in `documentation/10-internal/LESSONS_LEARNED.md`

---

## Rule 25b: Cache-Control on ALL API Responses (CRITICAL — CDN/Proxy Defense)

**Date added:** 2026-06-19
**Trigger:** Cache security audit: 50+ routers / ~200 endpoints returned JSON without `Cache-Control`.

### The Problem

Without `Cache-Control`, CDNs (CloudFront default: 24h cache) and forward proxies cache authenticated API responses, causing cross-user data exposure. This is dormant without an active CDN but becomes **critical** the moment one is deployed.

### The Rule

```javascript
// ✅ CORRECT — set via global middleware in backend/middleware/security.js
res.setHeader('Cache-Control', 'no-store', 'private');
```

| Context | Cache-Control | Implementation |
|---------|--------------|----------------|
| API JSON responses | `no-store, private` | `securityHeaders()` middleware (covers all ~200 endpoints) |
| 500/404 errors | `no-store, private` | `backend/server.js` error handlers |
| SPA HTML catch-all | `no-cache, no-store, must-revalidate` | `online-web/server.js` app.get('*') |
| Admin/merchant dashboards | `private, max-age=3600` | `backend/server.js` staticOptions('private') |
| `.well-known` files | `public, max-age=3600, must-revalidate` | `backend/server.js` staticOptions('public') |
| iOS HTTP client | `URLCache = null` | `PSPApiClient.ios.kt` |

### Checklist

- [ ] New routes are covered by global `securityHeaders()` middleware
- [ ] New `express.static` mounts use `staticOptions()` helper
- [ ] New iOS HTTP clients disable NSURLCache
- [ ] New Android HTTP clients set `OkHttpClient.cache = null`
- [ ] CDN cache behaviors documented for new origin deployments

### See Also

- `documentation/05-security/CACHE_SECURITY_AUDIT.md` — Full audit & 7-phase remediation
- `documentation/05-security/SECURITY_PATTERNS_REFERENCE.md` — Cache-Control Pattern section
- `CLAUDE.md` — KEY SECURITY PATTERNS > Cache-Control

---

## Rule 26: `commonTest` Tests Must Not Call Synchronous Platform-Specific APIs (MANDATORY)

**Date added:** 2026-04-29
**Trigger:** `DoubleLayerEncryptionTest` failed on JS browser target after being placed in `commonTest`

### The Problem

`commonTest` runs on **all** enabled KMP targets. APIs that are synchronous on JVM throw `NotImplementedError` on JS (Web Crypto is async-only). A test placed in `commonTest` may pass on Android but fail at runtime on JS browser.

### Decision Guide

| Test calls… | Correct source set |
|-------------|--------------------|
| Only `kotlin.test.*`, no platform APIs | `commonTest` ✅ |
| `CryptoUtils.sha256()` or any synchronous crypto | `src/test/kotlin/` (JVM unit tests) |
| `KeyDerivation.deriveKey()` (internally calls sha256) | `src/test/kotlin/` |
| `runTest { sha256Suspend(...) }` | `commonTest` ✅ |
| Android APIs (`Context`, `Intent`, etc.) | `androidInstrumentedTest` |

### The Rule

Before placing a test in `commonTest`, verify: **Does this test call any API that throws or behaves differently on non-JVM targets?** If yes, move it to the appropriate platform-specific test source set.

```
sdk/src/commonTest/    → runs on Android + JS browser (+ iOS if enabled)
sdk/src/test/          → JVM/Android unit tests only (androidUnitTest source set)
sdk/src/androidTest/   → Android instrumented tests (requires device/emulator)
```

---

## 26. UUID Must Be Set Before Factor Capture — Never at Submit Time

**Any HMAC identity context (UUID, salt, session ID) MUST be present before factors are captured. Never defer identity generation to the submit callback.**

```kotlin
// WRONG: UUID set after factor capture → all digests use empty-string HMAC context
fun start() {
    showStep(FactorCaptureStep(uuid = enrollmentData.userId ?: ""))  // userId is null here!
}
fun submitEnrollment() {
    enrollmentData.userId = generateUUIDv4()  // too late — digests already computed
}

// RIGHT: UUID exists before any factor is captured
fun start() {
    val uuid = enrollmentData.userId ?: generateUUIDv4()
    enrollmentData.userId = uuid
    showStep(FactorCaptureStep(uuid = uuid))
}
```

**Enforcement**: When reviewing enrollment/verification flows, trace the identity value from the capture step constructor to its mutation point. If it can be read as null/empty during capture, it is a bug.

---

## 27. K/JS Interop Functions Must Have Explicit Return Types

**ALL Kotlin/JS `external` functions or `js()` interop functions that return runtime values MUST declare explicit return types. IR compiler silently coerces inferred types to undefined/0.**

```kotlin
// WRONG: No return type — clearTimeout receives 0/undefined
private fun setTimeout(callback: () -> Unit, delay: Int) = js("setTimeout(callback, delay)")

// RIGHT: Explicit Int return type
private fun setTimeout(callback: () -> Unit, delay: Int): Int = js("setTimeout(callback, delay)")
```

**High-risk interop targets:**
- `setTimeout` / `setInterval` → `: Int` (timer ID)
- `requestAnimationFrame` → `: Int` (handle)
- `document.getElementById` → `: dynamic` or typed cast
- `JSON.parse` / `JSON.stringify` → `: dynamic`

**Enforcement**: grep for `fun.*= js\(` in Kotlin/JS modules and verify every match has an explicit return type.

---

### See Also

Lesson 98 in `documentation/10-internal/LESSONS_LEARNED.md`
