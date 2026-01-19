---
hidden: true
---

# Test Architecture - ZeroPay SDK

**Document Version:** 1.0.0 **Last Updated:** 2025-12-19 **Status:** Production Ready

***

## Table of Contents

1. [Overview](TEST_ARCHITECTURE.md#overview)
2. [Testing Strategy](TEST_ARCHITECTURE.md#testing-strategy)
3. [Test Types and Tools](TEST_ARCHITECTURE.md#test-types-and-tools)
4. [Mocha Tests (Backend)](TEST_ARCHITECTURE.md#mocha-tests-backend)
5. [Bugster Tests (Web UI)](TEST_ARCHITECTURE.md#bugster-tests-web-ui)
6. [Kotlin Tests (SDK)](TEST_ARCHITECTURE.md#kotlin-tests-sdk)
7. [CI/CD Integration](TEST_ARCHITECTURE.md#cicd-integration)
8. [Running Tests Locally](TEST_ARCHITECTURE.md#running-tests-locally)
9. [Writing New Tests](TEST_ARCHITECTURE.md#writing-new-tests)
10. [Best Practices](TEST_ARCHITECTURE.md#best-practices)

***

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

***

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

* Unit Tests: 70% (fast, isolated)
* Integration Tests: 20% (moderate speed, dependencies)
* E2E Tests: 10% (slow, full system)

***

## Test Types and Tools

| Test Type               | Tool              | Target                      | Location                     | Count    |
| ----------------------- | ----------------- | --------------------------- | ---------------------------- | -------- |
| **Backend Unit**        | Mocha + Chai      | Services, utils, validators | `backend/tests/`             | 21 files |
| **Backend Integration** | Mocha + Supertest | API routes, Redis, DB       | `backend/tests/integration/` | 5 files  |
| **Backend E2E**         | Mocha + Supertest | Complete workflows          | `backend/tests/e2e/`         | 6 files  |
| **Web UI E2E**          | Bugster           | User flows, browser         | `.bugster/tests/`            | 3 files  |
| **SDK Unit**            | JUnit             | Factors, crypto, KMP        | `sdk/src/test/`              | Multiple |

***

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

```bash
# All tests
cd backend && npm test

# Specific test suite
npm run test:e2e
npm run test:integration
npm run test:crypto

# Single file
npx mocha tests/e2e/complete-flow.test.js --timeout 10000
```

***

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

***

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

***

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

***

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

***

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

**File:** `sdk/src/commonTest/kotlin/MyProcessorTest.kt`

```kotlin
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class MyProcessorTest {
    @Test
    fun `should validate valid input`() {
        val result = MyProcessor.validate("valid-input")
        assertTrue(result.isValid)
    }

    @Test
    fun `should reject invalid input`() {
        val result = MyProcessor.validate("invalid")
        assertFalse(result.isValid)
        assertEquals("Invalid input", result.error)
    }
}
```

***

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

### 5. Error Testing

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

***

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

***

## Test Coverage Goals

| Module           | Current | Target |
| ---------------- | ------- | ------ |
| Backend Services | 75%     | 80%    |
| Backend APIs     | 85%     | 90%    |
| SDK Processors   | 70%     | 85%    |
| Web UI Flows     | 60%     | 75%    |

***

## See Also

* [BUGSTER\_INTEGRATION.md](../BUGSTER_INTEGRATION.md) - Bugster setup guide
* [CI/CD Workflow](../../.github/workflows/ci-cd.yml) - Main pipeline
* [Test Suite Summary](../../backend/tests/TEST_SUITE_SUMMARY.txt) - Test inventory
* [LESSONS\_LEARNED.md](../10-internal/LESSONS_LEARNED.md) - Testing lessons

***

**Last Updated:** 2025-12-19 **Maintained By:** ZeroPay Development Team
