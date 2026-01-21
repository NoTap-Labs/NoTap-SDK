# Railway Test Environment Configuration

**Complete guide for running tests against Railway test deployment**

---

## 🎯 Overview

This guide covers how to configure and run **all types of tests** against the Railway test environment:

| Test Type | Tool | Target |
|-----------|------|--------|
| **Backend API Tests** | Mocha | https://api-test-backend.notap.io |
| **Integration Tests** | Mocha | Railway backend + database + Redis |
| **E2E Tests** | Mocha + Bugster | Full stack (backend + frontend) |
| **Penetration Tests** | Custom suite | Railway deployment |
| **Frontend Tests** | Bugster | https://test-web-frontend.notap.io |

---

## 📋 Quick Setup

### Step 1: Environment File

**Backend has `.env.test` pre-configured for Railway:**

```bash
cd backend

# View configuration
cat .env.test

# Key settings:
# - API_BASE_URL=https://api-test-backend.notap.io
# - WEB_ENROLLMENT_URL=https://test-web-frontend.notap.io
# - SANDBOX_ENABLED=true
# - DATABASE_URL=${DATABASE_URL} (Railway injects)
# - REDIS_URL=${REDIS_URL} (Railway injects)
```

### Step 2: Load Test Environment

**Option A: Export for current session**
```bash
export $(grep -v '^#' .env.test | xargs)
```

**Option B: Use with npm scripts** (recommended)
```bash
# Add to package.json:
"test:railway": "NODE_ENV=test TEST_BASE_URL=https://api-test-backend.notap.io npm test"
```

### Step 3: Verify Connection

```bash
# Test Railway backend is accessible
curl https://api-test-backend.notap.io/health

# Should return:
{
  "status": "healthy",
  "database": "connected",
  "redis": "connected"
}
```

---

## 🧪 Running Tests

### Backend Unit Tests (Mocha)

**Against Railway deployment:**

```bash
cd backend

# Option 1: Using environment variable
TEST_BASE_URL=https://api-test-backend.notap.io npm test

# Option 2: Load full .env.test
export $(grep -v '^#' .env.test | xargs)
npm test

# Option 3: Specific test suite
TEST_BASE_URL=https://api-test-backend.notap.io npm run test:integration
```

**What this tests:**
- ✅ API endpoints respond correctly
- ✅ Authentication works
- ✅ Database operations succeed
- ✅ Redis caching works
- ✅ Business logic functions

**Example test output:**
```
  Enrollment Router
    POST /v1/enrollment/initiate
      ✓ should initiate enrollment (234ms)
      ✓ should validate required factors (123ms)
      ✓ should return 400 for invalid UUID (89ms)

  Verification Router
    POST /v1/verification/verify
      ✓ should verify factors correctly (456ms)
      ✓ should fail with incorrect digest (198ms)

  33 passing (4s)
```

### Integration Tests

**Test full backend stack:**

```bash
TEST_BASE_URL=https://api-test-backend.notap.io npm run test:integration

# Tests:
# - Enrollment flow (initiate → factors → complete)
# - Verification flow (initiate → verify → result)
# - Alias generation and resolution
# - Database transactions
# - Redis caching
```

### E2E Tests

**Test complete user flows:**

```bash
TEST_BASE_URL=https://api-test-backend.notap.io \
WEB_URL=https://test-web-frontend.notap.io \
npm run test:e2e

# Tests:
# - Complete enrollment via web
# - Complete verification via web
# - Merchant dashboard operations
# - Payment flows
# - SNS integration
# - SSO federation
```

### Frontend Tests (Bugster)

**Test web UI:**

```bash
cd ../.bugster

# Tests point to Railway frontend
# Configured in: .bugster/bugster.config.yml
bugster run --verbose

# Tests:
# - Enrollment flow UI
# - Verification flow UI
# - Management portal UI
```

### Penetration Tests

**Security testing:**

```bash
cd ../pentest

# Use Railway config
cp config.railway-test.env config.env

# Add credentials
nano config.env

# Run pentest suite
./scripts/run-all-tests.sh --skip-shannon
```

---

## 🔧 Configuration Details

### Backend Test Configuration

**File: `backend/.env.test`**

Key variables for Railway testing:

```env
# Test environment
NODE_ENV=test
SANDBOX_ENABLED=true

# Railway URLs
API_BASE_URL=https://api-test-backend.notap.io
WEB_ENROLLMENT_URL=https://test-web-frontend.notap.io
TEST_BASE_URL=https://api-test-backend.notap.io

# Database (Railway auto-injects)
DATABASE_URL=${DATABASE_URL}
DATABASE_ENABLED=true

# Redis (Railway auto-injects)
REDIS_URL=${REDIS_URL}
REDIS_HOST=test-redis.railway.internal

# Test credentials
TEST_USER_EMAIL=test@example.com
SKIP_EMAIL_VERIFICATION=true
```

### Frontend Test Configuration

**File: `.bugster/bugster.config.yml`**

```yaml
# Base URL for all tests
baseUrl: https://test-web-frontend.notap.io

# API endpoint
apiUrl: https://api-test-backend.notap.io

# Test settings
timeout: 30000
retries: 2
```

### Pentest Configuration

**File: `pentest/config.railway-test.env`**

```env
TARGET_API_URL=https://api-test-backend.notap.io
TARGET_WEB_URL=https://test-web-frontend.notap.io
ENVIRONMENT=railway_test
```

---

## 📊 Test Types & Commands

### Quick Reference

| Test Type | Command | Duration |
|-----------|---------|----------|
| **Unit Tests** | `TEST_BASE_URL=... npm test` | 30 seconds |
| **Integration** | `TEST_BASE_URL=... npm run test:integration` | 2 minutes |
| **E2E** | `TEST_BASE_URL=... npm run test:e2e` | 5 minutes |
| **Frontend (Bugster)** | `bugster run` | 10 minutes |
| **Pentest (Quick)** | `./scripts/run-all-tests.sh --quick` | 30 minutes |
| **Pentest (Full)** | `./scripts/run-all-tests.sh` | 90 minutes |

### Complete Test Suite

**Run all tests against Railway:**

```bash
#!/bin/bash
# Run complete test suite against Railway test environment

export TEST_BASE_URL=https://api-test-backend.notap.io
export WEB_URL=https://test-web-frontend.notap.io

echo "1/4: Backend Unit Tests..."
cd backend && npm test

echo "2/4: Integration Tests..."
npm run test:integration

echo "3/4: E2E Tests..."
npm run test:e2e

echo "4/4: Frontend Tests..."
cd ../.bugster && bugster run

echo "✓ All tests complete!"
```

---

## 🔍 Troubleshooting

### Issue: Tests Timeout

**Symptom:**
```
Error: Timeout of 5000ms exceeded
```

**Solutions:**

1. **Increase timeout in test files:**
```javascript
describe('Enrollment', function() {
  this.timeout(30000); // 30 seconds for Railway

  it('should create enrollment', async () => {
    // test...
  });
});
```

2. **Check Railway backend is running:**
```bash
curl https://api-test-backend.notap.io/health
```

3. **Check Railway service logs:**
- Railway Dashboard → Backend service → Deployments → View Logs

### Issue: Database Connection Failed

**Symptom:**
```
Error: Connection refused to database
```

**Solutions:**

1. **Verify PostgreSQL is running:**
- Railway Dashboard → PostgreSQL service → Check status

2. **Check DATABASE_URL in backend:**
- Backend service → Variables → DATABASE_URL exists

3. **Restart backend:**
- Backend service → Deployments → Redeploy

### Issue: Redis Connection Failed

**Same steps as database**

### Issue: CORS Errors

**Symptom:**
```
Access to XMLHttpRequest blocked by CORS policy
```

**Solution:**

Add frontend URL to backend CORS whitelist:

```bash
# In Railway backend variables:
CORS_ORIGIN=https://test-web-frontend.notap.io,http://localhost:3000
```

### Issue: 401 Unauthorized

**Symptom:**
```
Response: 401 Unauthorized
```

**Solutions:**

1. **Get fresh test API key:**
```bash
curl https://api-test-backend.notap.io/v1/sandbox/tokens
```

2. **Update test credentials:**
```javascript
const apiKey = 'sk_test_...' // Use sandbox token
```

3. **Check token expiration:**
- Sandbox tokens expire after 30 days

---

## 🎯 Best Practices

### 1. Always Use .env.test for Railway

```bash
# Good
export $(grep -v '^#' .env.test | xargs)
npm test

# Bad
npm test  # Uses .env which may point to localhost
```

### 2. Separate Local vs Railway Tests

```json
{
  "scripts": {
    "test": "npm run test:local",
    "test:local": "mocha tests/**/*.test.js",
    "test:railway": "TEST_BASE_URL=https://api-test-backend.notap.io mocha tests/**/*.test.js",
    "test:all": "npm run test:local && npm run test:railway"
  }
}
```

### 3. Mock External Services in Tests

```javascript
// For Railway tests, use sandbox mode
before(() => {
  process.env.SANDBOX_ENABLED = 'true';
  process.env.SANDBOX_MOCK_PAYMENTS = 'true';
});
```

### 4. Cleanup Test Data

```javascript
afterEach(async () => {
  // Clean up test enrollments
  await cleanupTestData();
});
```

### 5. Use Descriptive Test Names

```javascript
// Good
it('should return 400 when PIN is missing from enrollment request')

// Bad
it('test enrollment')
```

---

## 📝 Adding New Tests

### Backend Test Template

**File: `backend/tests/my-feature.test.js`**

```javascript
const { expect } = require('chai');
const axios = require('axios');

const BASE_URL = process.env.TEST_BASE_URL || 'http://localhost:3000';

describe('My Feature', function() {
  this.timeout(10000); // 10 seconds for Railway

  let apiKey;

  before(async () => {
    // Get sandbox API key
    const response = await axios.get(`${BASE_URL}/v1/sandbox/tokens`);
    apiKey = response.data.developer;
  });

  it('should test feature', async () => {
    const response = await axios.post(
      `${BASE_URL}/v1/my-endpoint`,
      { data: 'test' },
      { headers: { Authorization: `Bearer ${apiKey}` } }
    );

    expect(response.status).to.equal(200);
    expect(response.data).to.have.property('success', true);
  });
});
```

### Run your new test:

```bash
TEST_BASE_URL=https://api-test-backend.notap.io mocha tests/my-feature.test.js
```

---

## 🚀 CI/CD Integration

### GitHub Actions

**Update: `.github/workflows/ci-cd.yml`**

```yaml
env:
  # Use Railway test environment for integration tests
  TEST_BASE_URL: https://api-test-backend.notap.io
  WEB_URL: https://test-web-frontend.notap.io
  SANDBOX_ENABLED: true

jobs:
  integration-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Install dependencies
        run: cd backend && npm install

      - name: Run tests against Railway
        env:
          TEST_BASE_URL: https://api-test-backend.notap.io
        run: |
          cd backend
          npm run test:integration
          npm run test:e2e
```

---

## 📚 Related Documentation

- [RAILWAY_TEST_SETUP.md](../../pentest/RAILWAY_TEST_SETUP.md) - Railway infrastructure setup
- [TEST_ARCHITECTURE.md](../../documentation/07-testing/TEST_ARCHITECTURE.md) - Complete test framework
- [SANDBOX_TESTING_GUIDE.md](SANDBOX_TESTING_GUIDE.md) - Sandbox API usage

---

## ✅ Checklist

**Before running tests against Railway:**

- [ ] Railway backend is deployed and healthy
- [ ] Railway PostgreSQL is connected
- [ ] Railway Redis is connected
- [ ] `.env.test` is configured
- [ ] Test credentials obtained from `/v1/sandbox/tokens`
- [ ] CORS allows test frontend URL

**After tests:**

- [ ] Review test results
- [ ] Check Railway logs for errors
- [ ] Clean up test data if needed
- [ ] Document any failures

---

*Last Updated: 2026-01-17*
*Railway Test Environment Configuration*
