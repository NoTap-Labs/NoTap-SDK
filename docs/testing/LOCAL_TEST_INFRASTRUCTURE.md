# Local Test Infrastructure Guide

**Last Updated:** 2026-02-10
**Status:** Active

---

## Quick Start

```bash
cd backend
npm test          # 140 unit tests, ~2 seconds, zero dependencies
```

That's it. No Redis, no PostgreSQL, no Railway access needed.

---

## Table of Contents

1. [How It Works](#how-it-works)
2. [Test Commands](#test-commands)
3. [In-Memory Redis Adapter](#in-memory-redis-adapter)
4. [Test Tiers Explained](#test-tiers-explained)
5. [File Reference](#file-reference)
6. [Adding New Tests](#adding-new-tests)
7. [WSL/NTFS Performance Notes](#wslntfs-performance-notes)
8. [Troubleshooting](#troubleshooting)

---

## How It Works

### The Problem

Running backend tests locally required:
- Redis v5 installed and running (takes ~24 seconds to load on WSL/NTFS)
- PostgreSQL running with test database
- Railway access for e2e tests

This meant most developers couldn't run `npm test` at all.

### The Solution

Three changes make local testing work without any external services:

```
┌─────────────────────────────────────────────────────────────────┐
│                    LOCAL TEST FLOW                               │
└─────────────────────────────────────────────────────────────────┘

  npm test
     │
     ├─ 1. dotenv loads .env.test
     │      → MOCK_REDIS=true
     │      → DATABASE_ENABLED=false
     │      → RATE_LIMIT_DISABLED=true
     │
     ├─ 2. Mocha reads .mocharc.json
     │      → requires tests/setup.js (sets env vars)
     │      → loads tests/unit/**/*.test.js + tests/seal-integration.test.js
     │
     ├─ 3. Tests execute
     │      → Unit tests: no external deps, run immediately
     │      → Service tests: if they require server.js, it uses
     │        memoryRedis.js instead of real Redis (MOCK_REDIS=true)
     │
     └─ 4. Results: 140 passing, ~2 seconds
```

### Key Environment Variables

| Variable | Default in .env.test | Purpose |
|----------|---------------------|---------|
| `MOCK_REDIS` | `true` | Use in-memory Redis adapter instead of real Redis |
| `DATABASE_ENABLED` | `false` | Skip PostgreSQL connection |
| `RATE_LIMIT_DISABLED` | `true` | Bypass rate limiters (prevent 429 in tests) |
| `NODE_ENV` | `test` | Test mode (disables Redis reconnect strategy) |

---

## Test Commands

### Daily Development

```bash
cd backend

# Run unit tests (fast, always works)
npm test

# Run service-level tests (tests/*.test.js — some need mock Redis)
npm run test:services

# Run integration tests (tests/integration/**)
npm run test:integration

# Run everything (slow on WSL — see performance notes)
npm run test:all
```

### Against Railway (Remote Server)

```bash
# Full suite against Railway test server
npm run test:railway

# Just e2e against Railway
npm run test:railway:e2e
```

### Command Reference

| Command | Spec | Timeout | Dependencies |
|---------|------|---------|-------------|
| `npm test` | `tests/unit/**` + `seal-integration` | 10s | None |
| `npm run test:services` | `tests/*.test.js` | 15s | Mock Redis (auto) |
| `npm run test:integration` | `tests/integration/**` | 15s | Mock Redis (auto) |
| `npm run test:e2e` | `tests/e2e/**` | 30s | Running server |
| `npm run test:all` | `tests/**/*.test.js` | 30s | Mock Redis (auto) |
| `npm run test:railway` | `tests/**/*.test.js` | 10s | Railway access |
| `npm run test:railway:e2e` | `tests/e2e/**` | 30s | Railway + web URL |

---

## In-Memory Redis Adapter

**File:** `backend/config/memoryRedis.js`

When `MOCK_REDIS=true`, `server.js` loads `memoryRedis.js` instead of the real `redis` package. This avoids:
- The 24-second Redis v5 module load time on WSL/NTFS
- Need for a running Redis server
- TLS certificate configuration

### How It Activates

In `server.js`:

```javascript
let redisClient;

if (process.env.MOCK_REDIS === 'true') {
  const { createClient } = require('./config/memoryRedis');
  redisClient = createClient();
} else {
  // ... ALL existing Redis code unchanged ...
}
```

### What It Implements

The adapter implements all 36 Redis methods used in the codebase:

| Category | Methods |
|----------|---------|
| **Strings** | `get`, `set` (with EX), `setEx`, `mGet`, `del`, `exists` |
| **TTL** | `expire`, `ttl` |
| **Hashes** | `hSet`, `hGet`, `hGetAll`, `hLen` |
| **Sets** | `sAdd`, `sMembers`, `sRem`, `sCard` |
| **Lists** | `lPush`, `lRange`, `lTrim` |
| **Counter** | `incr` |
| **Scan** | `keys`, `scan`, `scanIterator` (async generator) |
| **Database** | `flushDb`, `dbSize` |
| **Transaction** | `multi` → pipeline with `exec` |
| **Raw** | `sendCommand` (maps basic commands) |
| **Lifecycle** | `connect`, `disconnect`, `quit`, `ping` → 'PONG', `info` |
| **Properties** | `isReady`, `isOpen` (both → `true`) |
| **Events** | `on`, `off`, `emit` (via Node.js EventEmitter) |

### TTL Strategy

Keys track expiry timestamps in a separate Map. On every read (`get`, `exists`, `hGet`, etc.), the adapter checks if the key has expired and deletes it if so. This is deterministic and avoids background timers.

### What It Does NOT Support

- `eval` / `evalSha` (Lua scripts) — throws, causing rate limiters to fail-open (expected behavior)
- Pub/Sub — not used in the codebase
- Cluster commands — not needed for tests

### Safety

`MOCK_REDIS` is **never** set in production:
- Not in `.env` or `.env.example`
- Not in Railway environment variables
- Not in CI/CD workflows
- The entire real Redis code path is in an `else` block — zero production changes

---

## Test Tiers Explained

### Tier 1: Unit Tests (`npm test`)

**What runs:** `tests/unit/**/*.test.js` + `tests/seal-integration.test.js`

**Currently 140 tests across 6 files:**
- `blockchainAnchor.test.js` — Blockchain anchor abstract class, factory, lifecycle
- `sealAssembler.test.js` — Seal assembly, PSD3 compliance, verification URLs
- `sealSecurity.test.js` — Memory wiping, session hashing, merchant sanitization
- `sealSigning.test.js` — ECDSA-P256 sign/verify, hash computation
- `walletSignatureService.test.js` — Ed25519 signatures, challenge generation
- `seal-integration.test.js` — Full seal lifecycle, PDF generation

**Dependencies:** None. These tests import service modules directly and use mock data.

**When to run:** Every code change. Fast feedback loop.

### Tier 2: Service Tests (`npm run test:services`)

**What runs:** `tests/*.test.js` (28 root-level test files)

**Tests include:** Redis-backed services (ConfigurationService, AnalyticsService, SecurityMonitoringService, etc.) and supertest-based router tests.

**Dependencies:** Mock Redis (auto-configured via MOCK_REDIS=true). Some tests create their own Redis clients — those will skip if real Redis isn't available.

**When to run:** After changing a service or router. Slower due to WSL/NTFS module loading.

### Tier 3: Integration Tests (`npm run test:integration`)

**What runs:** `tests/integration/**/*.test.js` (8 files)

**Tests include:** OAuth CSRF, session management, dynamic linking, wallet router, enrollment/verification flows, alias tests.

**Dependencies:** Mock Redis. Some tests use supertest (self-contained), others use axios (need running server).

**When to run:** Before pushing. Validates cross-module behavior.

### Tier 4: E2E Tests (`npm run test:e2e` or `npm run test:railway:e2e`)

**What runs:** `tests/e2e/**/*.test.js` (6 files)

**Tests include:** Complete enrollment-to-deletion flow, merchant dashboard, payment flow, security/fraud detection, SSO federation, SNS relink.

**Dependencies:** Running server with Redis and PostgreSQL. Use `npm run test:railway:e2e` to run against Railway's test backend.

**When to run:** Before releases. Full system validation.

---

## File Reference

```
backend/
├── .mocharc.json              # Mocha config (spec, require, timeout)
├── .env.test                  # Test environment variables (gitignored)
├── config/
│   └── memoryRedis.js         # In-memory Redis adapter (~330 LOC)
├── server.js                  # Conditional Redis: MOCK_REDIS=true → memoryRedis
├── tests/
│   ├── setup.js               # Root hook plugin: sets env vars, logs config
│   ├── unit/                  # Pure unit tests (no external deps)
│   │   ├── blockchainAnchor.test.js
│   │   ├── sealAssembler.test.js
│   │   ├── sealSecurity.test.js
│   │   ├── sealSigning.test.js
│   │   └── walletSignatureService.test.js
│   ├── integration/           # Cross-module tests
│   │   ├── oauthCsrf.test.js
│   │   ├── sessionManagement.test.js
│   │   ├── dynamicLinking.test.js
│   │   ├── walletRouter.test.js
│   │   ├── enrollment.test.js
│   │   ├── verification.test.js
│   │   ├── alias-enrollment.test.js
│   │   └── alias-verification.test.js
│   ├── e2e/                   # End-to-end flows (need running server)
│   │   ├── complete-flow.test.js
│   │   ├── merchant-dashboard-flow.test.js
│   │   ├── payment-flow.test.js
│   │   ├── security-fraud-flow.test.js
│   │   ├── sns-relink-flow.test.js
│   │   └── sso-federation-flow.test.js
│   └── *.test.js              # Service/router tests (28 files)
└── package.json               # Test scripts (npm test, test:services, etc.)
```

---

## Adding New Tests

### Unit Test (Recommended Default)

Create in `tests/unit/`:

```javascript
// tests/unit/myService.test.js
const { expect } = require('chai');
const { myFunction } = require('../../services/myService');

describe('MyService', function() {
  it('should do something', function() {
    const result = myFunction('input');
    expect(result).to.equal('expected');
  });
});
```

This automatically runs with `npm test`. No setup needed.

### Service Test (Needs Mock Redis)

Create in `tests/`:

```javascript
// tests/myRouter.test.js
const request = require('supertest');
const { expect } = require('chai');
const express = require('express');

describe('MyRouter', function() {
  let app;

  before(function() {
    // Build a minimal express app with just your router
    app = express();
    app.use(express.json());
    // ... mount your router ...
  });

  it('should respond', async function() {
    const res = await request(app).get('/my-endpoint');
    expect(res.status).to.equal(200);
  });
});
```

Runs with `npm run test:services`.

### Test That Needs server.js

**IMPORTANT:** Never `require('../server')` at module level. Always inside `before()`:

```javascript
// ✅ CORRECT — inside before() hook
describe('MyTest', function() {
  this.timeout(30000);
  let app;

  before(function() {
    app = require('../server'); // Loads lazily, only for this suite
  });

  it('should work', async function() {
    // use app...
  });
});

// ❌ WRONG — at module level (blocks ALL test file loading)
const app = require('../server'); // HANGS mocha on WSL/NTFS
```

### Test That Needs PostgreSQL

Tests requiring a database should gracefully skip when DB isn't available:

```javascript
describe('MyDBService', function() {
  let db;

  before(async function() {
    this.timeout(10000);
    try {
      db = new Pool({ /* config */ });
      await db.query('SELECT 1'); // verify connection
    } catch (err) {
      console.log('  Skipping: PostgreSQL not available');
      this.skip(); // gracefully skip entire suite
    }
  });
});
```

---

## WSL/NTFS Performance Notes

### Why Tests Are Slow on WSL

The backend runs on WSL2 with the project on the Windows filesystem (`/mnt/c/...`). Every `require()` call traverses the NTFS filesystem through the WSL compatibility layer, which is **10-50x slower** than native Linux I/O.

Real measurements:
- `require('supertest')` → 5,045ms
- `require('chai')` → 2,009ms (cumulative)
- `require('redis')` v5 → 24,000ms (1,496 command files)
- Loading all 47 test files → **several minutes**

### Why `npm test` Is Fast

The `.mocharc.json` default spec only loads 6 test files (`tests/unit/**` + `seal-integration`). These files have minimal dependencies:
- No `supertest` (no Express app creation)
- No `redis` (pure unit tests)
- No `pg` (no database)

Result: 140 tests in ~2 seconds.

### How To Speed Up `test:all`

If you frequently need to run all tests, consider:

1. **Move project to Linux filesystem:**
   ```bash
   cp -r /mnt/c/.../backend ~/backend
   cd ~/backend && npm test
   ```
   This makes module loading ~10x faster.

2. **Run on CI/CD:** Push and let GitHub Actions run the full suite.

3. **Use `test:railway`:** Tests run against Railway's server, bypassing local module loading.

---

## Troubleshooting

### Tests show "0 passing, N pending"

**Cause:** A `before()` hook failed (usually database connection). All tests in that suite are skipped.

**Fix:** Check the test output for error messages above the pending tests. Common causes:
- PostgreSQL not running → use `npm test` (unit tests don't need DB)
- Redis not running → ensure `MOCK_REDIS=true` is set (automatic via .env.test)

### Tests hang (never complete)

**Cause:** A test file has `require('../server')` at module level, which loads Redis and 30+ routers.

**Fix:** Move the require inside a `before()` hook with `this.timeout(30000)`.

### "Cannot find module" errors

**Cause:** A test imports from outside `backend/` (e.g., `pentest/scripts/`) where `node_modules` doesn't exist.

**Fix:** Check the require path. Use the backend's own dependencies.

### Rate limit errors (429)

**Cause:** `RATE_LIMIT_DISABLED` not set, or tests hitting Railway server.

**Fix:** Ensure `.env.test` has `RATE_LIMIT_DISABLED=true`. For Railway, use `npm run test:railway` which has appropriate rate limits.

### "SASL: SCRAM-SERVER-FIRST-MESSAGE" error

**Cause:** PostgreSQL password not configured. Harmless if `DATABASE_ENABLED=false`.

**Fix:** Set `DATABASE_ENABLED=false` in `.env.test` (already the default).

---

## Architecture Diagram

```
                        ┌──────────────────────────┐
                        │       npm test           │
                        │  (developer runs this)    │
                        └──────────┬───────────────┘
                                   │
                        ┌──────────▼───────────────┐
                        │    .mocharc.json         │
                        │  spec: tests/unit/**     │
                        │  require: tests/setup.js │
                        └──────────┬───────────────┘
                                   │
                 ┌─────────────────┼─────────────────┐
                 │                 │                   │
          ┌──────▼──────┐  ┌──────▼──────┐    ┌──────▼──────┐
          │  setup.js   │  │  .env.test  │    │ Test Files  │
          │             │  │             │    │             │
          │ Sets env:   │  │ MOCK_REDIS  │    │ Unit tests  │
          │ MOCK_REDIS  │  │ =true       │    │ import from │
          │ NODE_ENV    │  │ DB_ENABLED  │    │ services/   │
          │ RATE_LIMIT  │  │ =false      │    │ directly    │
          └─────────────┘  └─────────────┘    └──────┬──────┘
                                                      │
                                               ┌──────▼──────┐
                                               │   Results   │
                                               │ 140 passing │
                                               │    ~2s      │
                                               └─────────────┘

  ── When tests load server.js (service/integration tier): ──

          ┌────────────┐     MOCK_REDIS=true     ┌──────────────┐
          │ server.js  │ ──────────────────────► │ memoryRedis  │
          │            │                          │ .js          │
          │ if MOCK    │     MOCK_REDIS=false     │ (Map-based)  │
          │ else real  │ ──────────────────────► ┌──────────────┐
          └────────────┘                          │ redis v5     │
                                                  │ (real Redis) │
                                                  └──────────────┘
```
