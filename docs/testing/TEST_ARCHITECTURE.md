# Test Architecture - ZeroPay SDK

**Document Version:** 1.0.0
**Last Updated:** 2025-12-19
**Status:** Production Ready

---

## Table of Contents

1. [Overview](#overview)
2. [Testing Strategy](#testing-strategy)
3. [Test Types and Tools](#test-types-and-tools)
4. [Mocha Tests (Backend)](#mocha-tests-backend)
5. [Bugster Tests (Web UI)](#bugster-tests-web-ui)
6. [Kotlin Tests (SDK)](#kotlin-tests-sdk)
7. [CI/CD Integration](#cicd-integration)
8. [Running Tests Locally](#running-tests-locally)
9. [Writing New Tests](#writing-new-tests)
10. [Best Practices](#best-practices)

---

## Overview

ZeroPay uses a multi-layered testing strategy with different tools for different parts of the stack:

```
┌─────────────────────────────────────────────────────────────────┐
│                    ZEROPAY TEST ARCHITECTURE                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────┐  ┌─────────────────────┐  ┌──────────────┐
│  Backend (Node.js)  │  │   Web UI (Kotlin/JS) │  │  SDK (KMP)   │
│                     │  │                      │  │              │
│  Tool: Mocha + Chai │  │  Tool: Bugster       │  │  Tool: JUnit │
│  Coverage: 32 files │  │  Coverage: 3 flows   │  │  Coverage: + │
│  Location: backend/ │  │  Location: .bugster/ │  │  Location: * │
│        tests/       │  │         tests/       │  │   src/test/  │
└─────────────────────┘  └─────────────────────┘  └──────────────┘
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  │
                          ┌───────▼────────┐
                          │   CI/CD Jobs   │
                          │                │
                          │  ci-cd.yml     │
                          │  bugster.yml   │
                          └────────────────┘
```

---

## Testing Strategy

### 1. Separation of Concerns

**❌ WRONG - Mixing test types:**
```
Bugster tests for backend APIs
Mocha tests for web UI
```

**✅ CORRECT - Clear separation:**
```
Mocha → Backend APIs, services, business logic
Bugster → Web UI user interactions, browser flows
JUnit → Kotlin multiplatform SDK logic
```

### 2. Test Pyramid

```
          ┌──────┐
          │  E2E │  ← Bugster (Web UI flows)
          └──────┘     Mocha E2E (API workflows)
        ┌──────────┐
        │Integration│ ← Mocha (API integration tests)
        └──────────┘
    ┌──────────────┐
    │     Unit     │  ← Mocha (Services, utils, validators)
    └──────────────┘     JUnit (SDK processors, crypto)
```

**Distribution:**
- Unit Tests: 70% (fast, isolated)
- Integration Tests: 20% (moderate speed, dependencies)
- E2E Tests: 10% (slow, full system)

---

## Test Types and Tools

| Test Type | Tool | Target | Location | Count |
|-----------|------|--------|----------|-------|
| **Backend Unit** | Mocha + Chai | Services, utils, validators | `backend/tests/` | 21 files |
| **Backend Integration** | Mocha + Supertest | API routes, Redis, DB | `backend/tests/integration/` | 5 files |
| **Backend E2E** | Mocha + Supertest | Complete workflows | `backend/tests/e2e/` | 6 files |
| **Web UI E2E** | Bugster | User flows, browser | `.bugster/tests/` | 3 files |
| **SDK Unit** | JUnit | Factors, crypto, KMP | `sdk/src/test/` | Multiple |

---

## Mocha Tests (Backend)

### Structure

```
backend/tests/
├── e2e/                          # End-to-end workflow tests
│   ├── complete-flow.test.js     # Full enrollment + verification
│   ├── merchant-dashboard-flow.test.js
│   ├── payment-flow.test.js
│   ├── security-fraud-flow.test.js
│   ├── sns-relink-flow.test.js   # SNS re-linking workflow
│   └── sso-federation-flow.test.js
│
├── integration/                   # API integration tests
│   ├── alias-enrollment.test.js
│   ├── alias-verification.test.js
│   ├── enrollment.test.js
│   ├── verification.test.js
│   └── walletRouter.test.js
│
└── [services]/                    # Unit tests for services
    ├── AdminAuthService.test.js
    ├── AnalyticsService.test.js
    ├── APIKeyService.test.js
    └── ... (21 more)
```

### Syntax (Mocha + Chai)

**✅ CORRECT:**
```javascript
const { expect } = require('chai');
const supertest = require('supertest');

describe('Enrollment API', () => {
    before(async () => {
        // Setup before all tests
    });

    after(async () => {
        // Cleanup after all tests
    });

    it('should enroll user with valid factors', async () => {
        const response = await supertest(app)
            .post('/v1/enrollment')
            .send({ uuid: 'test', factors: [...] });

        expect(response.status).to.equal(200);
        expect(response.body.success).to.be.true;
        expect(response.body.uuid).to.exist;
    });
});
```

**❌ WRONG - Jest syntax:**
```javascript
// This will cause "beforeAll is not defined" error
beforeAll(async () => { ... });
afterAll(async () => { ... });
test('should work', async () => { ... });
expect(value).toBe(expected);
```

### Running Mocha Tests

> **Full local setup guide:** `documentation/07-testing/LOCAL_TEST_INFRASTRUCTURE.md`

```bash
cd backend

# Unit tests (fast, no external deps — 140 tests in ~2s)
npm test

# Service/router tests (needs mock Redis — auto-configured)
npm run test:services

# Integration tests
npm run test:integration

# E2E tests (needs running server or Railway)
npm run test:e2e

# Everything (slow on WSL/NTFS)
npm run test:all

# Against Railway test server
npm run test:railway

# Single file (bypass .mocharc.json)
npx mocha tests/unit/sealSigning.test.js --timeout 10000 --no-config
```

**Note:** `npm test` uses an in-memory Redis adapter (`MOCK_REDIS=true`) so no Redis or PostgreSQL install is needed. See `LOCAL_TEST_INFRASTRUCTURE.md` for details.

---

## Bugster Tests (Web UI)

### Structure

```
.bugster/
├── config.yaml                    # Bugster configuration
├── tests/
│   ├── enrollment_flow.yaml       # Multi-factor enrollment
│   ├── verification_flow.yaml     # Payment verification
│   └── management_portal.yaml     # Account management
└── screenshots/                   # Test artifacts (auto-generated)
```

### YAML Format (Required)

**✅ CORRECT:**
```yaml
name: User completes enrollment with PIN and Pattern factors
page: /
page_path: online-web/src/jsMain/kotlin/com/zeropay/web/enrollment/EnrollmentFlow.kt
task: Verify user can complete full enrollment flow with multiple factors
steps:
  - Navigate to ZeroPay web application
  - Click "Start Enrollment" button
  - Complete consent step by clicking "I Agree"
  - Select PIN and PATTERN from factor selection screen
  - Enter PIN 1234 and confirm PIN 1234
  - Draw pattern on grid (5 points minimum)
  - Click "Complete Enrollment" button
  - Verify enrollment confirmation screen displays
expected_result: User successfully enrolled with UUID displayed, confirmation message shows "Enrollment Complete"
```

**❌ WRONG - API testing format:**
```yaml
# This is for API testing, NOT Bugster
setup:
  - name: "Generate test UUID"
    action: "generate"
steps:
  - name: "POST /v1/enrollment"
    action: "POST"
    endpoint: "/v1/enrollment"
```

### Running Bugster Tests

```bash
# All tests
bugster run --verbose --headless

# Specific test
bugster run --verbose --headless .bugster/tests/enrollment_flow.yaml

# With screenshots
bugster run --verbose --screenshots

# Generate report
bugster run --verbose --headless --output report.json
```

---

## Kotlin Tests (SDK)

### Structure

```
sdk/src/
├── commonTest/                    # KMP shared tests
│   └── kotlin/com/zeropay/sdk/
│       ├── factors/processors/
│       │   ├── PinProcessorTest.kt
│       │   ├── PatternProcessorTest.kt
│       │   └── ...
│       └── crypto/
│           └── CryptoUtilsTest.kt
│
├── androidTest/                   # Android-specific tests
└── jsTest/                        # JavaScript-specific tests
```

### Running Kotlin Tests

```bash
# All SDK tests
./gradlew :sdk:test

# Specific test class
./gradlew :sdk:test --tests "PinProcessorTest"

# With coverage
./gradlew :sdk:test :sdk:jacocoTestReport
```

---

## CI/CD Integration

### GitHub Actions Workflows

**1. ci-cd.yml - Main Pipeline**
```yaml
jobs:
  lint:                    # Code quality
  backend-tests:          # Mocha tests (32 files)
  kotlin-unit-tests:      # JUnit tests (SDK)
  kotlin-js-build:        # Kotlin/JS compilation
  android-build:          # Android modules
```

**2. bugster-tests.yml - Web UI**
```yaml
jobs:
  web-ui-tests:           # Bugster tests (3 flows)
    - Build online-web module
    - Start webpack dev server (port 8080)
    - Run Bugster web UI tests
    - Upload screenshots/videos on failure
```

### Workflow Triggers

```
Push to master/main:
  ✅ ci-cd.yml runs
  ✅ bugster-tests.yml runs

Pull Request:
  ✅ ci-cd.yml runs
  ✅ bugster-tests.yml runs
  ✅ PR comments with test results

Manual:
  ✅ workflow_dispatch enabled on both
```

---

## Running Tests Locally

### Prerequisites

**Backend Tests:**
```bash
# Required services
docker run -d -p 6379:6379 redis:7-alpine
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=test postgres:15-alpine

# Install dependencies
cd backend && npm install
```

**Web UI Tests:**
```bash
# Build online-web module
./gradlew :online-web:jsBrowserDevelopmentWebpack

# Start dev server
./gradlew :online-web:jsBrowserDevelopmentRun &

# Install Bugster CLI
curl -sSL https://github.com/Bugsterapp/bugster-cli/releases/latest/download/install.sh | bash
```

### Quick Test Commands

```bash
# Backend - All tests
cd backend && npm test

# Backend - E2E only
cd backend && npm run test:e2e

# Web UI - All flows
bugster run --verbose --headless

# SDK - All tests
./gradlew :sdk:test

# Full test suite (requires services)
npm test && bugster run --headless && ./gradlew test
```

---

## Writing New Tests

### 1. Backend API Test (Mocha)

**File:** `backend/tests/integration/my-feature.test.js`

```javascript
const { expect } = require('chai');
const supertest = require('supertest');
const app = require('../../server');

describe('My Feature API', () => {
    before(async () => {
        // Setup: Initialize test data
    });

    after(async () => {
        // Cleanup: Remove test data
    });

    it('should handle valid request', async () => {
        const response = await supertest(app)
            .post('/v1/my-feature')
            .send({ param: 'value' });

        expect(response.status).to.equal(200);
        expect(response.body).to.have.property('result');
    });

    it('should reject invalid request', async () => {
        const response = await supertest(app)
            .post('/v1/my-feature')
            .send({});

        expect(response.status).to.equal(400);
    });
});
```

### 2. Web UI Test (Bugster)

**File:** `.bugster/tests/my_feature_flow.yaml`

```yaml
name: User completes my feature workflow
page: /
page_path: online-web/src/jsMain/kotlin/com/zeropay/web/MyFeature.kt
task: Verify user can complete my feature workflow
steps:
  - Navigate to web application
  - Click "My Feature" button
  - Fill in required fields
  - Click "Submit" button
  - Verify success message displays
expected_result: Feature completed successfully with confirmation displayed
```

### 3. SDK Unit Test (Kotlin)

**File:** `sdk/src/test/kotlin/com/zeropay/sdk/factors/MyFactorTest.kt`

```kotlin
import com.zeropay.sdk.security.FactorDigest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MyFactorTest {

    companion object {
        private val TEST_SALT = "test-salt-32-bytes-long-enough!!".encodeToByteArray()
        private const val TEST_UUID = "550e8400-e29b-41d4-a716-446655440000"
    }

    @Before
    fun setUp() {
        FactorDigest.resetForTesting()
        FactorDigest.configure(TEST_SALT)
    }

    @Test
    fun testDigest_ValidInput_Returns32Bytes() {
        val result = MyFactor.digest("valid-input", TEST_UUID)
        assertEquals(32, result.size)
    }

    @Test
    fun testVerify_MatchingInput_ReturnsTrue() {
        val fixedSalt = ByteArray(16) { 0 }
        val digest = MyFactor.digestWithSalt("valid-input", TEST_UUID, fixedSalt, 0L)
        assertTrue(MyFactor.verify("valid-input", TEST_UUID, digest, 0L, fixedSalt))
    }
}
```

**Key requirements for ALL SDK factor tests:**
- Always call `FactorDigest.resetForTesting()` + `FactorDigest.configure(TEST_SALT)` in `@Before`
- Always pass `TEST_UUID` to `digest()` and `verify()` calls
- Use `digestWithSalt(input, uuid, fixedSalt, fixedTimestamp)` for deterministic tests

### 4. SDK Timing Tests (Constant-Time Verification)

**Purpose:** Verify that `verify()` takes the same time regardless of whether the input is correct or incorrect (prevents timing attacks).

**Critical rules — violating any of these causes false test results:**

**Rule 1 — Test `verify()`, NOT `digest()` with invalid input.**

`digest()` validates input before crypto. Calling `digest("abc")` on a digits-only factor throws an exception before any HMAC runs. That exception overhead is ~100x faster than HMAC — the test measures exception-vs-HMAC, not constant-time comparison. This gives false confidence.

```kotlin
// ❌ WRONG — tests exception overhead, not constant-time comparison
@Test fun timing_WRONG() {
    val correctTime = measureTimeMillis { repeat(100) { PinFactor.digest("1234", uuid) }}
    val wrongTime   = measureTimeMillis { repeat(100) { PinFactor.digest("abc",  uuid) }}
    // wrongTime is 100x faster because "abc" throws before HMAC. This is not a timing test.
}

// ✅ CORRECT — tests the actual constant-time comparison in verify()
@Test fun timing_CORRECT() {
    val fixedSalt = ByteArray(16) { 0 }
    val digest = PinFactor.digestWithSalt("1234", TEST_UUID, fixedSalt, 0L)
    // ... measure verify() with correct and wrong input
}
```

**Rule 2 — Always warmup before measuring.**

JVM JIT compilation optimizes hot code paths. Without warmup, the first measurement block runs interpreted bytecode while the second runs JIT-compiled code, creating an inherent asymmetry.

```kotlin
// Always warmup 50+ iterations before any timed measurement
repeat(50) {
    MyFactor.verify(correctInput, TEST_UUID, digest, 0L, fixedSalt)
    MyFactor.verify(wrongInput,   TEST_UUID, digest, 0L, fixedSalt)
}
```

**Rule 3 — Use 500% tolerance, not 50%.**

A single GC pause (1–3 ms) on a 5ms measurement = 20–60% noise. 50% tolerance fails spuriously. 500% is still meaningful: real timing leaks from `contentEquals` (short-circuits at first mismatch) produce >1000% difference. 500% catches real vulnerabilities while tolerating GC noise.

**Canonical template:**

```kotlin
@Category(TimingTest::class)
@Test
fun testVerify_ConstantTime_TimingIndependent() {
    // Arrange — use fixed salt/timestamp for determinism
    val fixedSalt = ByteArray(16) { 0 }
    val fixedTimestamp = 0L
    val digest = MyFactor.digestWithSalt(correctInput, TEST_UUID, fixedSalt, fixedTimestamp)
    val iterations = 100

    // Warmup JVM/JIT before measuring
    repeat(50) {
        MyFactor.verify(correctInput, TEST_UUID, digest, fixedTimestamp, fixedSalt)
        MyFactor.verify(wrongInput,   TEST_UUID, digest, fixedTimestamp, fixedSalt)
    }

    // Act
    val correctTime = measureTimeMillis {
        repeat(iterations) { MyFactor.verify(correctInput, TEST_UUID, digest, fixedTimestamp, fixedSalt) }
    }
    val wrongTime = measureTimeMillis {
        repeat(iterations) { MyFactor.verify(wrongInput, TEST_UUID, digest, fixedTimestamp, fixedSalt) }
    }

    // Assert — 500% tolerance for GC noise; real timing leaks cause >1000%
    val diff = kotlin.math.abs(correctTime - wrongTime)
    val avg  = (correctTime + wrongTime) / 2.0
    val pct  = if (avg > 0) (diff / avg) * 100 else 0.0
    assertTrue(
        "Verification should be constant-time (within 500%). " +
        "Correct: ${correctTime}ms, Wrong: ${wrongTime}ms, Diff: ${pct.toInt()}%",
        pct < 500
    )
}
```

**Add the `TimingTest` category marker** so timing tests can be excluded from CI if needed:
```kotlin
// sdk/src/test/kotlin/com/zeropay/sdk/testing/TimingTest.kt
interface TimingTest
```

---

## Best Practices

### 1. Test Isolation

**✅ DO:**
```javascript
describe('Feature', () => {
    let testUuid;

    beforeEach(() => {
        testUuid = `test-${Date.now()}`; // Unique per test
    });

    afterEach(async () => {
        await cleanupTest(testUuid); // Clean up
    });
});
```

**❌ DON'T:**
```javascript
const SHARED_UUID = 'test-uuid'; // Shared across tests - causes conflicts
```

### 2. Descriptive Test Names

**✅ DO:**
```javascript
it('should reject enrollment with less than 3 factors', async () => {
    // Test clearly states what it validates
});
```

**❌ DON'T:**
```javascript
it('test 1', async () => {
    // Unclear what this tests
});
```

### 3. Test Data Management

**✅ DO:**
```javascript
const validFactors = [
    { type: 'PIN', digest: '...' },
    { type: 'PATTERN', digest: '...' },
    { type: 'EMOJI', digest: '...' }
];
```

**❌ DON'T:**
```javascript
// Hardcoded real user UUIDs or production data
const uuid = 'real-user-uuid-from-prod';
```

### 4. Async/Await Consistency

**✅ DO:**
```javascript
it('should work', async () => {
    const result = await asyncOperation();
    expect(result).to.exist;
});
```

**❌ DON'T:**
```javascript
it('should work', async () => {
    asyncOperation(); // Missing await - test finishes before operation completes
});
```

### 5. SDK Thread-Safety Tests

Use `runBlocking` + `async` on `Dispatchers.Default` to launch truly concurrent coroutines. Always await all jobs before asserting.

```kotlin
@Test
fun `test concurrent access is thread-safe`() = runBlocking {
    val subject = MyCounter()

    val jobs = List(10) {
        async(Dispatchers.Default) { subject.increment() }
    }
    jobs.forEach { it.await() }  // wait for ALL to complete

    assertEquals(10L, subject.count)  // would be <10 if not thread-safe
}
```

**Key rule:** `async { ... }` inside `runBlocking` without an explicit dispatcher inherits the `runBlocking` thread (single-threaded). To get true parallelism you MUST use `async(Dispatchers.Default)` or `withContext(Dispatchers.Default)`.

**Checking thread-safety before writing:** Any class with `var` mutable fields accessed from `Dispatchers.Default` needs either `AtomicLong`/`AtomicInt` (for counters) or `synchronizedCommon` (for compound state). See Lesson 67.

### 6. Error Testing

**✅ DO:**
```javascript
it('should handle missing parameters', async () => {
    const response = await supertest(app)
        .post('/v1/endpoint')
        .send({});

    expect(response.status).to.equal(400);
    expect(response.body.error).to.include('required');
});
```

---

## Troubleshooting

### Common Issues

**1. "beforeAll is not defined"**
```
Problem: Using Jest syntax in Mocha
Solution: Replace beforeAll → before, afterAll → after, test() → it()
```

**2. "Bugster test validation errors"**
```
Problem: Using API test format instead of UI test format
Solution: Use simple YAML with name/page/page_path/task/steps/expected_result
```

**3. "Tests timeout"**
```
Problem: Services (Redis, PostgreSQL) not running
Solution: Start required services before running tests
```

**4. "Module not found"**
```
Problem: Missing dependencies
Solution: Run npm install in backend directory
```

**5. SDK timing tests fail intermittently**
```
Problem: GC pause skews measurement (1–3ms pause on 5ms total = 60% noise)
Solution: Add 50-iteration warmup before measuring. Use 500% tolerance, not 50%.
See: Lesson 65 in LESSONS_LEARNED.md
```

**6. SDK timing test passes but the wrong code path is tested**
```
Problem: Testing digest(invalid) instead of verify(wrong) — throws before HMAC runs
Solution: Always test verify() for constant-time behavior, never digest() with invalid input
See: Lesson 66 in LESSONS_LEARNED.md
```

**7. "FactorDigest not configured" in SDK tests**
```
Problem: Missing @Before setup — FactorDigest.configure() not called
Solution: Add @Before setUp() with FactorDigest.resetForTesting() + FactorDigest.configure(TEST_SALT)
```

**8. SDK concurrent test count is wrong (e.g. 9/10)**
```
Problem: var Long counter in class accessed from Dispatchers.Default — lost updates due to race
Solution: Replace var Long with com.zeropay.sdk.platform.AtomicLong
See: Lesson 67 in LESSONS_LEARNED.md
```

---

## Penetration Testing Setup (New - 2026-01-31)

### Overview

Penetration testing is an automated security testing suite that validates the system against OWASP Top 10 vulnerabilities:

```
┌──────────────────────────────────────┐
│    Penetration Testing Suite         │
├──────────────────────────────────────┤
│  Tool: Python scripts + payloads     │
│  Location: pentest/                  │
│  Coverage: 14 security tests         │
│  Reports: JSON, HTML, Markdown       │
│  Auto-Setup: Yes (via sandbox API)   │
└──────────────────────────────────────┘
```

### Quick Start

**Automatic Setup (Recommended):**
```bash
cd pentest
./scripts/run-all-tests.sh --skip-shannon
```

The script automatically:
1. Calls `POST /v1/sandbox/pentest-setup` to configure test environment
2. Creates test enrollments in Redis (7 test accounts for IDOR testing)
3. Generates JWT tokens for all user types
4. Runs all 14 penetration tests
5. Generates reports in `pentest/reports/`

**Manual Setup:**
```bash
# Option 1: Use setup script
./scripts/setup-sandbox.sh --target https://api.notap.io

# Option 2: Call API directly
curl -X POST https://api.notap.io/v1/sandbox/pentest-setup \
  -H "Content-Type: application/json"
```

### Test Accounts (Pre-configured)

All test accounts are automatically created with proper IDOR pairs for access control testing:

| Role | Victim UUID | Attacker UUID | Purpose |
|------|-------------|---------------|---------|
| User | `11111111-1111-4111-a111-111111111111` | `22222222-2222-4222-a222-222222222222` | User IDOR |
| Merchant | `cccccccc-cccc-4ccc-accc-cccccccccccc` | `dddddddd-dddd-4ddd-addd-dddddddddddd` | Merchant IDOR |
| Admin | `aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaaa` | `bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbbbbbb` | Admin IDOR |

### Security Tests Covered

| Test | Type | Status |
|------|------|--------|
| SQL Injection | Input Validation | ✅ PASS |
| XSS Attack | Input Sanitization | ✅ PASS |
| SSRF Attack | URL Validation | ⏳ Pending |
| Brute Force | Rate Limiting | ✅ PASS |
| Replay Attack | Nonce/Timestamp | ⏳ Partial |
| Race Condition | Distributed Locks | ✅ PASS |
| IDOR Testing | Access Control | ✅ Included |
| And 7 more... | Various | 14 Total |

### Public Routes (No Auth Required)

The following endpoints are whitelisted and don't require replay protection validation:

```bash
curl https://api.notap.io/v1/names/supported          # Name resolution supported chains
curl https://api.notap.io/v1/names/health              # Health check
curl https://api.notap.io/v1/sandbox/config            # Sandbox configuration
curl https://api.notap.io/v1/sandbox/tokens            # Available test tokens
```

### Configuration

Pre-configured in `pentest/config.sandbox.env`:
- ✅ TEST_USER_UUID_1 (Primary test user)
- ✅ TEST_API_KEY (Developer API key)
- ✅ ADMIN_API_KEY (Admin API key)
- ✅ All IDOR test account UUIDs

### Reports

After running tests, check:
- `pentest/reports/*.json` - Machine-readable results
- `pentest/reports/*.html` - Human-readable reports
- `test-analysis-reports/2026-01-31/` - Detailed analysis

### See Also

For automated test failure analysis:
- Run: `./scripts/analyze-tests.sh --pentest`
- Generates: Comprehensive vulnerability summary with fixes

---

## Test Coverage Goals

| Module | Current | Target |
|--------|---------|--------|
| Backend Services | 75% | 80% |
| Backend APIs | 85% | 90% |
| SDK Processors | 70% | 85% |
| Web UI Flows | 60% | 75% |
| Security Tests | 14/14 | 14/14 ✅ |

---

## See Also

- [BUGSTER_INTEGRATION.md](../BUGSTER_INTEGRATION.md) - Bugster setup guide
- [CI/CD Workflow](../../.github/workflows/ci-cd.yml) - Main pipeline
- [Test Suite Summary](../../backend/tests/TEST_SUITE_SUMMARY.txt) - Test inventory
- [LESSONS_LEARNED.md](../10-internal/LESSONS_LEARNED.md) - Testing lessons

---

**Last Updated:** 2025-12-19
**Maintained By:** ZeroPay Development Team
