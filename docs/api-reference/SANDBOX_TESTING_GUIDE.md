# NoTap Sandbox Testing Guide

**Complete guide for testing the NoTap API without production dependencies.**

**Last Updated:** 2025-12-28

---

## Quick Start

```bash
# 1. Get test tokens for all user types
curl https://api.notap.io/v1/sandbox/tokens

# 2. Use a token to test an authenticated endpoint
curl -H "Authorization: Bearer <token>" https://api.notap.io/v1/auth/developer/profile

# 3. Test blockchain name resolution (mock)
curl https://api.notap.io/v1/sandbox/names/resolve/alice.sol

# 4. Login with test credentials
curl -X POST https://api.notap.io/v1/auth/developer/login \
  -H "Content-Type: application/json" \
  -d '{"email": "developer@test.notap.io", "password": "TestPassword123!"}'
```

### Test Credentials (All User Types)

| User Type | Email | Password | Database ID |
|-----------|-------|----------|-------------|
| Developer | `developer@test.notap.io` | `TestPassword123!` | `00000000-0000-4000-a000-000000000001` |
| User | `user@test.notap.io` | `TestPassword123!` | `00000000-0000-4000-a000-000000000002` |
| Merchant | `merchant@test.notap.io` | `TestPassword123!` | `00000000-0000-4000-a000-000000000003` |
| Admin | `admin@test.notap.io` | `TestPassword123!` | `00000000-0000-4000-a000-000000000004` |

> **Single Source of Truth:** All test credentials are defined in `backend/config/sandboxConfig.js`
>
> Database records are created by migration `018_seed_sandbox_test_accounts.sql`

### Test API Keys

For testing protected endpoints that require API key authentication:

| Purpose | API Key | Header |
|---------|---------|--------|
| **Admin Endpoints** | `test-admin-api-key-00000000-0000-4000-a000-000000000000` | `X-Admin-Api-Key` |
| **Demo Analytics** | `test-demo-analytics-key-00000000-0000-4000-a000-000000000000` | `x-api-key` |

**Example - Demo Analytics:**
```bash
# Get analytics metrics (requires API key)
curl -H "x-api-key: test-demo-analytics-key-00000000-0000-4000-a000-000000000000" \
  "https://api.notap.io/v1/demo-analytics/metrics?startDate=2026-01-01&endDate=2026-01-16"

# Or as query parameter
curl "https://api.notap.io/v1/demo-analytics/funnel?apiKey=test-demo-analytics-key-00000000-0000-4000-a000-000000000000&startDate=2026-01-01&endDate=2026-01-16"
```

> **Note:** Demo analytics event tracking (POST endpoints) is public and doesn't require API keys.
> Only read endpoints (GET /metrics, /funnel, /factors, /sessions) require authentication.

---

## Table of Contents

1. [What is the Sandbox?](#what-is-the-sandbox)
2. [Test JWT Tokens](#test-jwt-tokens)
3. [Test API Keys](#test-api-keys-reference)
4. [Mock Blockchain Names](#mock-blockchain-names)
5. [Mock Enrollments](#mock-enrollments)
6. [Test Payment Cards](#test-payment-cards)
7. [Demo Analytics Testing](#demo-analytics-testing)
8. [PSP API Keys](#psp-api-keys)
9. [Wallet Testing](#wallet-testing)
10. [Crypto Payments](#crypto-payments)
11. [ZK Proof Testing](#zk-proof-testing)
12. [Environment Configuration](#environment-configuration)
13. [API Reference](#api-reference)
14. [Troubleshooting](#troubleshooting)

---

## What is the Sandbox?

The sandbox is a **testing environment** that lets you:

- Generate **test JWT tokens** for any user type (developer, user, merchant, admin)
- Get **mock blockchain name resolution** without connecting to real chains
- Create **mock enrollments** for verification testing
- Use **test payment cards** for payment flow testing
- Test API endpoints without needing real users or data

**Base URL:** `https://api.notap.io/v1/sandbox/*`

**Production vs Sandbox:**

| Feature | Production | Sandbox |
|---------|------------|---------|
| JWT Tokens | Real users from database | Pre-configured test accounts |
| Blockchain Names | Real on-chain resolution | Mock deterministic addresses |
| Enrollments | Real stored in Redis | Mock data for testing |
| Payments | Real PSP transactions | Mock success/failure responses |

---

## Test JWT Tokens

### Why Use Test Tokens?

Many API endpoints require authentication. Instead of creating real users, use sandbox tokens to test:

- `401 Unauthorized` → Use test token
- `403 Forbidden` → Use correct user type token

### Get All Test Tokens

```bash
curl https://api.notap.io/v1/sandbox/tokens
```

**Response:**
```json
{
  "success": true,
  "tokens": {
    "developer": {
      "token": "eyJhbGciOiJIUzI1NiIs...",
      "account": {
        "id": "dev_test_00000000-0000-4000-a000-000000000001",
        "email": "developer@test.notap.io",
        "fullName": "Test Developer"
      },
      "header": "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
    },
    "user": { ... },
    "merchant": { ... },
    "admin": { ... }
  },
  "expiresIn": "24h"
}
```

### Generate Specific Token

```bash
curl -X POST https://api.notap.io/v1/sandbox/tokens/generate \
  -H "Content-Type: application/json" \
  -d '{"userType": "developer"}'
```

**Available User Types:**

| Type | Email | Use For |
|------|-------|---------|
| `developer` | developer@test.notap.io | API development, developer portal |
| `user` | user@test.notap.io | Regular user flows, enrollment |
| `merchant` | merchant@test.notap.io | Merchant dashboard, verification |
| `admin` | admin@test.notap.io | Admin operations, user management |

### Use Token in Requests

```bash
# Get developer profile
curl -H "Authorization: Bearer <developer_token>" \
  https://api.notap.io/v1/auth/developer/profile

# Get user profile
curl -H "Authorization: Bearer <user_token>" \
  https://api.notap.io/v1/auth/user/profile

# Get merchant profile
curl -H "Authorization: Bearer <merchant_token>" \
  https://api.notap.io/v1/auth/merchant/profile

# Get admin profile
curl -H "Authorization: Bearer <admin_token>" \
  https://api.notap.io/v1/auth/admin/profile
```

### Validate Token

```bash
curl -X POST https://api.notap.io/v1/sandbox/tokens/validate \
  -H "Content-Type: application/json" \
  -d '{"token": "eyJhbGciOiJIUzI1NiIs..."}'
```

**Response:**
```json
{
  "success": true,
  "valid": true,
  "decoded": {
    "type": "developer",
    "developerId": "dev_test_...",
    "email": "developer@test.notap.io",
    "sandbox": true,
    "iat": 1735344000,
    "exp": 1735430400
  },
  "expired": false
}
```

---

## Mock Blockchain Names

### Why Mock Names?

Real blockchain name resolution requires:
- Solana RPC connection (`SNS_ENABLED=true`)
- Ethereum RPC connection (`ENS_ENABLED=true`)
- etc.

Sandbox provides mock resolution that works without any blockchain connection.

### Resolve a Name (Mock)

```bash
# Solana Name Service
curl https://api.notap.io/v1/sandbox/names/resolve/alice.sol

# NoTap subdomain
curl https://api.notap.io/v1/sandbox/names/resolve/alice.notap.sol

# ENS (Ethereum)
curl https://api.notap.io/v1/sandbox/names/resolve/vitalik.eth

# Unstoppable Domains
curl https://api.notap.io/v1/sandbox/names/resolve/crypto.wallet

# BASE Name Service
curl https://api.notap.io/v1/sandbox/names/resolve/coinbase.base.eth
```

**Response:**
```json
{
  "success": true,
  "name": "alice.sol",
  "address": "SoKv3q2h5g8j1n2m3o4p5q6r7s8t9u0v1w2x3y4z5a6b",
  "chain": "solana",
  "provider": "bonfida",
  "mock": true,
  "note": "This is a mock response for testing."
}
```

### Supported Mock TLDs

| TLD | Chain | Provider |
|-----|-------|----------|
| `.sol` | Solana | Bonfida |
| `.notap.sol` | Solana | Bonfida |
| `.eth` | Ethereum | ENS |
| `.crypto` | Polygon | Unstoppable |
| `.wallet` | Polygon | Unstoppable |
| `.nft` | Polygon | Unstoppable |
| `.base.eth` | Base L2 | Basenames |

### Check Availability (Mock)

```bash
curl https://api.notap.io/v1/sandbox/names/available/alice.sol
```

**Response:**
```json
{
  "success": true,
  "name": "alice.sol",
  "available": false,
  "suggestions": ["alice123", "alice_pay", "alice2025", "myalice"],
  "mock": true
}
```

**Mock Logic:** Names with base length > 5 chars = available, otherwise taken.

---

## Mock Enrollments

### Create Mock Enrollment

```bash
curl -X POST https://api.notap.io/v1/sandbox/enrollments/create \
  -H "Content-Type: application/json" \
  -d '{
    "uuid": "test_user_001",
    "factors": ["PIN", "PATTERN", "EMOJI"],
    "alias": "tiger-1234"
  }'
```

**Response:**
```json
{
  "success": true,
  "enrollment": {
    "uuid": "test_user_001",
    "alias": "tiger-1234",
    "factors": ["PIN", "PATTERN", "EMOJI"],
    "factorDigests": {
      "PIN": "abc123...",
      "PATTERN": "def456...",
      "EMOJI": "ghi789..."
    },
    "createdAt": "2025-12-28T12:00:00Z",
    "expiresAt": "2025-12-29T12:00:00Z",
    "status": "active",
    "mock": true
  },
  "testData": {
    "pin": "1234",
    "pattern": [0, 1, 2, 4, 6, 7, 8],
    "emoji": ["😀", "🎉", "🚀", "💎"],
    "note": "Use these values when testing verification"
  }
}
```

### Test Data for Verification

When testing verification flows, use these values:

| Factor | Test Value |
|--------|------------|
| PIN | `1234` |
| PATTERN | `[0, 1, 2, 4, 6, 7, 8]` |
| EMOJI | `["😀", "🎉", "🚀", "💎"]` |
| COLOUR | `["#FF0000", "#00FF00", "#0000FF"]` |
| WORDS | `["apple", "banana", "cherry"]` |

---

## Test Payment Cards

### Get Test Cards

```bash
curl https://api.notap.io/v1/sandbox/cards
```

**Response:**
```json
{
  "success": true,
  "cards": [
    {
      "number": "4111111111111111",
      "type": "Visa",
      "expiry": "12/25",
      "cvv": "123",
      "result": "success",
      "description": "Successful payment"
    },
    {
      "number": "4000000000000002",
      "type": "Visa",
      "expiry": "12/25",
      "cvv": "123",
      "result": "declined",
      "description": "Card declined"
    },
    {
      "number": "4000000000000119",
      "type": "Visa",
      "expiry": "12/25",
      "cvv": "123",
      "result": "insufficient_funds",
      "description": "Insufficient funds"
    }
  ]
}
```

### Card Results

| Card Number | Result |
|-------------|--------|
| 4111111111111111 | Success |
| 5555555555554444 | Success (Mastercard) |
| 4000000000000002 | Declined |
| 4000000000000119 | Insufficient funds |

---

## Demo Analytics Testing

The demo analytics system tracks engagement and conversion funnels for the NoTap demo (`notap-demo-v7.0.html`).

### Public Endpoints (No Authentication)

Event tracking endpoints are public to allow the demo to track usage without API keys:

```bash
# Track single event
curl -X POST https://api.notap.io/v1/demo-analytics/event \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "eventType": "enrollment_start",
    "eventData": {
      "url": "https://example.com/demo",
      "screenWidth": 1920
    }
  }'

# Track batch events
curl -X POST https://api.notap.io/v1/demo-analytics/batch \
  -H "Content-Type: application/json" \
  -d '{
    "events": [
      {
        "sessionId": "550e8400-e29b-41d4-a716-446655440000",
        "eventType": "enrollment_start",
        "eventData": {}
      },
      {
        "sessionId": "550e8400-e29b-41d4-a716-446655440000",
        "eventType": "factor_selected",
        "eventData": {"factorId": "PIN"}
      }
    ]
  }'
```

### Protected Endpoints (Require API Key)

Analytics read endpoints require the demo analytics API key:

**Test API Key:** `test-demo-analytics-key-00000000-0000-4000-a000-000000000000`

```bash
# Get aggregated metrics
curl -H "x-api-key: test-demo-analytics-key-00000000-0000-4000-a000-000000000000" \
  "https://api.notap.io/v1/demo-analytics/metrics?startDate=2026-01-01&endDate=2026-01-16"

# Get conversion funnel
curl -H "x-api-key: test-demo-analytics-key-00000000-0000-4000-a000-000000000000" \
  "https://api.notap.io/v1/demo-analytics/funnel?startDate=2026-01-01&endDate=2026-01-16"

# Get factor analytics
curl -H "x-api-key: test-demo-analytics-key-00000000-0000-4000-a000-000000000000" \
  "https://api.notap.io/v1/demo-analytics/factors?startDate=2026-01-01&endDate=2026-01-16"

# List sessions
curl -H "x-api-key: test-demo-analytics-key-00000000-0000-4000-a000-000000000000" \
  "https://api.notap.io/v1/demo-analytics/sessions?limit=10"

# Get session details
curl -H "x-api-key: test-demo-analytics-key-00000000-0000-4000-a000-000000000000" \
  "https://api.notap.io/v1/demo-analytics/session/550e8400-e29b-41d4-a716-446655440000"
```

**Supported Event Types:**
- `session_start`, `session_end`
- `enrollment_start`, `enrollment_complete`, `enrollment_abandon`
- `factor_selected`, `factor_skipped`
- `mode_selected`, `gateway_selected`
- `verification_initiated`, `verification_complete`, `verification_failed`
- `challenge_started`, `challenge_completed`
- `error`, `consent_given`, `theme_changed`, `quickstart_loaded`

**Privacy:**
- All data is anonymous (session UUIDs only)
- No PII collected
- 90-day automatic data retention (GDPR compliant)

---

## Test API Keys Reference

### Available Test API Keys

| Purpose | API Key | Header | Endpoints |
|---------|---------|--------|-----------|
| **Admin** | `test-admin-api-key-00000000-0000-4000-a000-000000000000` | `X-Admin-Api-Key` | Admin operations |
| **Demo Analytics** | `test-demo-analytics-key-00000000-0000-4000-a000-000000000000` | `x-api-key` | GET /v1/demo-analytics/* |

> **Get All Keys:** `GET /v1/sandbox/config` returns all sandbox API keys

---

## PSP API Keys

### Test API Keys for PSP Integration

PSP (Payment Service Provider) endpoints require API key authentication in the Authorization header.

**Test API Keys:**

| PSP | API Key | Environment |
|-----|---------|-------------|
| Stripe | `psp_test_stripe_xyz789` | test |
| MercadoPago | `psp_test_mercadopago_abc123` | test |
| Square | `psp_test_square_def456` | test |

> **Single Source of Truth:** PSP API keys are defined in `backend/config/sandboxConfig.js` under `TEST_PSP_API_KEYS`

### Using PSP API Keys

```bash
# Create a PSP verification session
curl -X POST https://api.notap.io/v1/psp/session/create \
  -H "Authorization: Bearer psp_test_stripe_xyz789" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "00000000-0000-4000-a000-000000000002",
    "merchantId": "00000000-0000-4000-a000-000000000003",
    "amount": 1000
  }'
```

**Response:**
```json
{
  "success": true,
  "sessionId": "sess_test_...",
  "expiresAt": "2025-12-28T13:00:00Z",
  "requiredFactors": ["PIN", "PATTERN"]
}
```

---

## Wallet Testing

### Test Wallet Addresses

Test wallet addresses for blockchain integration testing.

| Type | Address | Chain |
|------|---------|-------|
| User | `GsbwXfJraMomNxBcjYLcG3mxkBUiyWXAB32fGbSMQRdW` | Solana (devnet) |
| Merchant | `DRpbCBMxVnDK7maPGDUYQ2VnbEjxoYvA4gWHzBCDRxKW` | Solana (devnet) |

> **Single Source of Truth:** Test wallets are defined in `backend/config/sandboxConfig.js` under `TEST_WALLETS`
> These are valid Solana devnet addresses (Base58 format).

### Generate Wallet Challenge

```bash
curl -X POST https://api.notap.io/v1/wallet/challenge/generate \
  -H "Content-Type: application/json" \
  -d '{
    "walletAddress": "GsbwXfJraMomNxBcjYLcG3mxkBUiyWXAB32fGbSMQRdW",
    "uuid": "00000000-0000-4000-a000-000000000002"
  }'
```

### Check Wallet Status

```bash
curl https://api.notap.io/v1/wallet/status/00000000-0000-4000-a000-000000000002
```

---

## Crypto Payments

### Test Crypto Payment Data

For testing device-free blockchain payments.

| Field | Value |
|-------|-------|
| Charge ID | `charge_test_00000000-0000-4000-a000-000000000001` |
| Merchant UUID | `00000000-0000-4000-a000-000000000003` |
| Amount | 1000 (cents) |
| Currency | USDC |
| Chain | Solana |

> **Single Source of Truth:** Test crypto data is defined in `backend/config/sandboxConfig.js` under `TEST_CRYPTO_PAYMENTS`

### Check Supported Chains

```bash
curl https://api.notap.io/v1/crypto/config/chains
```

### Check Supported Tokens

```bash
curl https://api.notap.io/v1/crypto/config/tokens
```

### Get Spending History

```bash
curl https://api.notap.io/v1/crypto/spending/00000000-0000-4000-a000-000000000002
```

---

## ZK Proof Testing

### Test ZK Proof Data

For testing zero-knowledge proof generation and verification.

| Field | Value |
|-------|-------|
| User ID | `00000000-0000-4000-a000-000000000002` |
| Merkle Root | `test-merkle-root-hash-0000000000000000` |
| Factor Count | 3 |

> **Single Source of Truth:** Test ZK data is defined in `backend/config/sandboxConfig.js` under `TEST_ZK_PROOFS`

### Get Verification Key

```bash
curl https://api.notap.io/v1/zkproof/verification-key
```

### Generate Proof (Test)

```bash
curl -X POST https://api.notap.io/v1/zkproof/generate \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "00000000-0000-4000-a000-000000000002",
    "submittedDigests": {"PIN": "abc123"},
    "enrolledDigests": {"PIN": "abc123"},
    "merkleRoot": "test-merkle-root",
    "timestamp": 1735344000
  }'
```

---

## Environment Configuration

### Enable Sandbox (Default: Enabled)

In `.env`:
```bash
# Enable sandbox endpoints
SANDBOX_ENABLED=true

# Enable mock payment responses
SANDBOX_MOCK_PAYMENTS=true

# Enable mock name resolution
SANDBOX_MOCK_NAMES=true

# Test token expiry
SANDBOX_TOKEN_EXPIRY=24h
```

### Disable for Production

```bash
SANDBOX_ENABLED=false
```

When disabled, all `/v1/sandbox/*` endpoints return 404.

### Check Current Config

```bash
curl https://api.notap.io/v1/sandbox/config
```

**Response:**
```json
{
  "success": true,
  "config": {
    "sandbox_enabled": true,
    "mock_payments": true,
    "sns_enabled": false,
    "ens_enabled": false,
    "database_enabled": true,
    "blockchain_network": "devnet",
    "jwt_expiry": "24h"
  },
  "testAccounts": ["developer", "user", "merchant", "admin"],
  "endpoints": {
    "tokens": {
      "generate": "POST /v1/sandbox/tokens/generate",
      "list": "GET /v1/sandbox/tokens",
      "validate": "POST /v1/sandbox/tokens/validate"
    },
    "names": {
      "resolve": "GET /v1/sandbox/names/resolve/:name",
      "available": "GET /v1/sandbox/names/available/:name"
    }
  }
}
```

---

## API Reference

### Token Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/sandbox/tokens` | Get all test tokens |
| POST | `/v1/sandbox/tokens/generate` | Generate specific token |
| POST | `/v1/sandbox/tokens/validate` | Validate a token |

### Name Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/sandbox/names/resolve/:name` | Mock resolve name |
| GET | `/v1/sandbox/names/available/:name` | Mock availability check |

### Enrollment Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/sandbox/enrollments/create` | Create mock enrollment |
| POST | `/v1/sandbox/enrollments/generate` | Generate test enrollment |
| GET | `/v1/sandbox/users` | List test users |
| GET | `/v1/sandbox/users/:uuid` | Get specific test user |

### Other Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/sandbox/status` | Sandbox status |
| GET | `/v1/sandbox/config` | Current configuration |
| GET | `/v1/sandbox/cards` | Test payment cards |
| POST | `/v1/sandbox/reset` | Reset sandbox data |
| POST | `/v1/sandbox/keys/generate` | Generate test API key |
| POST | `/v1/sandbox/webhooks/generate` | Generate test webhook |
| POST | `/v1/sandbox/webhooks/test` | Send test webhook event |

---

## Troubleshooting

### "Token expired" Error

Test tokens expire in 24 hours. Generate a new one:
```bash
curl https://api.notap.io/v1/sandbox/tokens
```

### "401 Unauthorized" on Authenticated Endpoints

1. Get a test token for the correct user type
2. Include the token in the Authorization header:
```bash
curl -H "Authorization: Bearer <token>" https://api.notap.io/v1/auth/developer/profile
```

### "404 Not Found" on Sandbox Endpoints

Check if sandbox is enabled:
```bash
curl https://api.notap.io/v1/sandbox/status
```

If it returns 404, sandbox is disabled. Enable in `.env`:
```bash
SANDBOX_ENABLED=true
```

### "500 Server Error" on Real Name Resolution

The real SNS/ENS endpoints require blockchain configuration. Use sandbox instead:
```bash
# Instead of:
curl https://api.notap.io/v1/sns/resolve/alice.sol  # ❌ Requires SNS_ENABLED=true

# Use:
curl https://api.notap.io/v1/sandbox/names/resolve/alice.sol  # ✅ Always works
```

### Different User Types Return Different Errors

Each user type has different permissions:

| User Type | Can Access |
|-----------|------------|
| `developer` | `/v1/auth/developer/*`, `/v1/developer/*` |
| `user` | `/v1/auth/user/*` |
| `merchant` | `/v1/auth/merchant/*`, `/api/merchant/*` |
| `admin` | `/v1/auth/admin/*`, `/v1/admin/*` |

Use the correct token type for the endpoint you're testing.

### Test Accounts Not Found in Database

If profile endpoints return "user not found", the test data migration may not have run:

```bash
# Run the migration manually via Railway
npx railway run psql $DATABASE_URL -f database/migrations/018_seed_sandbox_test_accounts.sql

# Or verify accounts exist
npx railway run psql $DATABASE_URL -c "SELECT email FROM developers WHERE email LIKE '%test.notap.io'"
```

The migration `018_seed_sandbox_test_accounts.sql` creates all test accounts.

---

## Complete Testing Workflow

### Step 1: Get Test Tokens

```bash
# Save tokens to file for easy reuse
curl -s https://api.notap.io/v1/sandbox/tokens | jq > /tmp/tokens.json

# Extract specific token
DEV_TOKEN=$(cat /tmp/tokens.json | jq -r '.tokens.developer.token')
```

### Step 2: Test Authentication

```bash
# Developer auth
curl -H "Authorization: Bearer $DEV_TOKEN" \
  https://api.notap.io/v1/auth/developer/profile
```

### Step 3: Test Blockchain Names (Mock)

```bash
curl https://api.notap.io/v1/sandbox/names/resolve/alice.sol
```

### Step 4: Create Test Enrollment

```bash
curl -X POST https://api.notap.io/v1/sandbox/enrollments/create \
  -H "Content-Type: application/json" \
  -d '{"factors": ["PIN", "EMOJI"]}'
```

### Step 5: Test Verification Flow

Use the test data from the enrollment response to verify.

---

**Need Help?** Check `/v1/sandbox/config` for current settings or `/v1/sandbox/status` for available features.

---

**Last Updated:** 2025-12-28
