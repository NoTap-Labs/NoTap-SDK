# NoTap API Authentication Guide

This document explains all authentication mechanisms used in the NoTap API.

---

## Overview: 6 Authentication Types

| Type | Header | Use Case | Token Lifetime |
|------|--------|----------|----------------|
| **Management Token** | `Authorization: Bearer mgmt_...` | Self-service portal | 15 minutes |
| **Admin API Key** | `X-Admin-API-Key: xxx` | Admin dashboard | Permanent |
| **PSP API Key** | `Authorization: Bearer psp_...` | Payment integrations | Permanent |
| **Developer JWT** | `Authorization: Bearer eyJ...` | Developer portal | 24 hours |
| **Merchant Auth** | `X-Merchant-ID` + `X-API-Key` | Merchant APIs | Permanent |
| **Webhook Signature** | `X-Webhook-Signature` | Webhook verification | Per-request |

---

## 1. Management Token (Self-Service Portal)

**Purpose:** Allow users to manage their enrollment remotely (e.g., lost device).

### How It Works

1. User visits management portal with their UUID
2. User completes ALL enrolled factors (multi-factor verification)
3. Server generates a signed management token (15 min expiry)
4. Token is used for subsequent management operations

### Flow

```
POST /v1/management/verify
{
  "uuid": "abc-123-def-456",
  "session_id": "sess_xxx",
  "factors": {
    "PIN": "a665a459...",        // SHA-256 digest
    "PATTERN": "5e884898...",
    "EMOJI": "03ac6742..."
  }
}

Response:
{
  "success": true,
  "auth_token": "mgmt_1703123456789_abc-123-def-456_hmac_signature",
  "expires_at": 1703124356789,
  "expires_in_seconds": 900
}
```

### Using the Token

```bash
curl -X DELETE https://api.notap.io/v1/management/enrollment/abc-123-def-456 \
  -H "Authorization: Bearer mgmt_1703123456789_abc-123-def-456_hmac_signature"
```

### Protected Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/management/enrollment/:uuid` | DELETE | Delete enrollment |
| `/v1/management/export/:uuid` | GET | Export user data (GDPR) |
| `/v1/management/auto-renewal/:uuid` | PUT | Toggle auto-renewal |
| `/v1/management/payment/:uuid` | DELETE | Unlink payment |
| `/v1/management/devices/:uuid` | GET | List devices |

### Security

- Token format: `mgmt_{timestamp}_{uuid}_{hmac_signature}`
- HMAC-SHA256 signed with `MANAGEMENT_TOKEN_SECRET`
- 15-minute expiry (configurable)
- UUID must match route parameter

### Environment Variables

```bash
MANAGEMENT_TOKEN_SECRET=your_32_byte_secret_key_here
MANAGEMENT_TOKEN_EXPIRY_MINUTES=15  # Optional, default: 15
```

**Status:** ✅ Fully implemented

---

## 2. Admin API Key

**Purpose:** Protect admin dashboard and system management endpoints.

### How It Works

Simple API key authentication - key is compared against environment variable.

### Using the Key

```bash
curl https://api.notap.io/v1/admin/stats \
  -H "X-Admin-API-Key: your_admin_api_key_here"
```

### Protected Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/admin/stats` | GET | System statistics |
| `/v1/admin/alias/stats` | GET | Alias statistics |
| `/v1/admin/audit/logs` | GET | Audit trail |
| `/v1/admin/users/*` | ALL | User management |
| `/v1/admin/security/*` | ALL | Security monitoring |
| `/v1/admin/analytics/*` | ALL | Analytics |
| `/v1/admin/teams/*` | ALL | Team management |
| `/v1/admin/billing/*` | ALL | Billing management |

### Environment Variables

```bash
ADMIN_API_KEY=your_super_secret_admin_key_here
```

**Generate a secure key:**
```bash
openssl rand -hex 32
```

### Security

- Key stored in environment variable (never in code)
- Constant-time comparison to prevent timing attacks
- All access logged with IP address
- Rate limited: 100 requests/minute

**Status:** ✅ Fully implemented

---

## 3. PSP API Key (Payment Service Providers)

**Purpose:** Authenticate payment gateway integrations (Stripe, MercadoPago, etc.)

### How It Works

PSP partners are issued API keys with format: `psp_{env}_{pspId}_{random}`

### Key Format

```
psp_test_stripe_abc123def456
psp_live_mercadopago_xyz789ghi012
```

| Part | Description |
|------|-------------|
| `psp` | Prefix (always "psp") |
| `test/live` | Environment |
| `pspId` | PSP identifier (stripe, mercadopago, etc.) |
| `random` | Random alphanumeric string |

### Using the Key

```bash
curl -X POST https://api.notap.io/v1/psp/session/create \
  -H "Authorization: Bearer psp_live_stripe_abc123def456" \
  -H "Content-Type: application/json" \
  -d '{"user_uuid": "...", "amount": 49.99}'
```

### Protected Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/psp/session/create` | POST | Create verification session |
| `/v1/psp/session/submit` | POST | Submit factors |
| `/v1/psp/session/:id/status` | GET | Get session status |

### Pre-configured Test Keys

```javascript
// Defined in backend/config/sandboxConfig.js (SINGLE SOURCE OF TRUTH)
// TEST_PSP_API_KEYS:
'psp_test_mercadopago_abc123'  // MercadoPago test
'psp_test_stripe_xyz789'       // Stripe test
'psp_test_square_def456'       // Square test
```

> **Note:** All test credentials are centralized in `backend/config/sandboxConfig.js`.
> The pspRouter.js imports keys from this config.

### Production Setup

For production, PSP keys should be stored in database:

```sql
CREATE TABLE psp_api_keys (
  key_hash VARCHAR(64) PRIMARY KEY,  -- SHA-256 of key
  psp_id VARCHAR(50) NOT NULL,
  environment VARCHAR(10) NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  last_used_at TIMESTAMP,
  is_active BOOLEAN DEFAULT TRUE
);
```

**Status:** ✅ Test keys centralized in sandboxConfig.js (need DB storage for production)

---

## 4. Developer JWT Token

**Purpose:** Authenticate developer portal access (API keys, webhooks, usage).

### How It Works

1. Developer logs in via OAuth or email/password
2. Server issues JWT token with developer ID
3. Token used for all developer portal operations

### Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "developerId": "dev_abc123",
    "email": "developer@example.com",
    "iat": 1703123456,
    "exp": 1703209856
  }
}
```

### Using the Token

```bash
curl https://api.notap.io/v1/developer/keys \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### Protected Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/developer/keys` | GET/POST | Manage API keys |
| `/v1/developer/keys/:id` | PUT/DELETE | Update/revoke key |
| `/v1/developer/webhooks` | GET/POST | Manage webhooks |
| `/v1/developer/usage` | GET | View usage stats |

### Environment Variables

```bash
JWT_SECRET=your_jwt_signing_secret_here
JWT_EXPIRY_HOURS=24  # Optional, default: 24
```

**Status:** ⚠️ Partially implemented (middleware commented out, needs developer auth flow)

---

## 5. Merchant Authentication

**Purpose:** Authenticate merchant dashboard and transaction APIs.

### How It Works

Dual-header authentication with merchant ID and API key.

### Using the Auth

```bash
curl https://api.notap.io/api/merchant/dashboard/stats \
  -H "X-Merchant-ID: merchant_abc123" \
  -H "X-API-Key: ntpk_live_xyz789..."
```

### Protected Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/merchant/dashboard/*` | ALL | Dashboard APIs |
| `/api/merchant/transactions/*` | ALL | Transaction history |
| `/api/merchant/settings/*` | ALL | Merchant settings |

### API Key Format

```
ntpk_live_abc123...   (production)
ntpk_test_xyz789...   (sandbox)
```

**Status:** ⚠️ Partially implemented (needs merchant registration flow)

---

## 6. Webhook Signature Verification

**Purpose:** Verify that webhooks actually came from NoTap.

### How It Works

NoTap signs webhook payloads with HMAC-SHA256.

### Verification

```javascript
const crypto = require('crypto');

function verifyWebhook(payload, signature, secret) {
  const expected = crypto
    .createHmac('sha256', secret)
    .update(JSON.stringify(payload))
    .digest('hex');

  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expected)
  );
}
```

### Headers Sent with Webhooks

```
X-Webhook-Signature: sha256=abc123...
X-Webhook-Timestamp: 1703123456
X-Webhook-ID: wh_xyz789
```

**Status:** ✅ Fully implemented

---

## What's Missing / Needs Setup

### 1. Environment Variables (Required)

Add these to Railway environment:

```bash
# Management Portal
MANAGEMENT_TOKEN_SECRET=<generate with: openssl rand -hex 32>

# Admin Dashboard
ADMIN_API_KEY=<generate with: openssl rand -hex 32>

# JWT for Developer Portal
JWT_SECRET=<generate with: openssl rand -hex 32>

# Webhook Signing
WEBHOOK_SECRET=<generate with: openssl rand -hex 32>
```

### 2. Database Tables (Optional for Production)

```sql
-- PSP API Keys (for production)
CREATE TABLE psp_api_keys (
  key_hash VARCHAR(64) PRIMARY KEY,
  psp_id VARCHAR(50) NOT NULL,
  environment VARCHAR(10) NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

-- Developer accounts
CREATE TABLE developers (
  id VARCHAR(50) PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255),
  created_at TIMESTAMP DEFAULT NOW()
);

-- Merchant accounts
CREATE TABLE merchants (
  id VARCHAR(50) PRIMARY KEY,
  business_name VARCHAR(255) NOT NULL,
  api_key_hash VARCHAR(64) NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
```

### 3. Code Changes Needed

| Component | Status | What's Needed |
|-----------|--------|---------------|
| Management Token | ✅ Done | Nothing |
| Admin API Key | ✅ Done | Set `ADMIN_API_KEY` env var |
| PSP API Key | ⚠️ Partial | Move keys from memory to database |
| Developer JWT | ⚠️ Partial | Uncomment middleware, add login flow |
| Merchant Auth | ⚠️ Partial | Add merchant registration flow |
| Webhook Signature | ✅ Done | Nothing |

---

## Quick Test Commands

### Test Admin Auth
```bash
# Without key (should fail)
curl https://api.notap.io/v1/admin/stats

# With key (should work)
curl https://api.notap.io/v1/admin/stats \
  -H "X-Admin-API-Key: YOUR_ADMIN_KEY"
```

### Test PSP Auth
```bash
# Without key (should fail)
curl -X POST https://api.notap.io/v1/psp/session/create

# With test key (should work in dev)
curl -X POST https://api.notap.io/v1/psp/session/create \
  -H "Authorization: Bearer psp_test_stripe_xyz789" \
  -H "Content-Type: application/json" \
  -d '{"user_uuid": "test-user-success-001", "amount": 10.00}'
```

### Test Management Auth
```bash
# Step 1: Get token by verifying factors
curl -X POST https://api.notap.io/v1/management/verify \
  -H "Content-Type: application/json" \
  -d '{
    "uuid": "your-uuid",
    "session_id": "sess_xxx",
    "factors": {"PIN": "digest...", "PATTERN": "digest..."}
  }'

# Step 2: Use token
curl https://api.notap.io/v1/management/devices/your-uuid \
  -H "Authorization: Bearer mgmt_xxx_your-uuid_signature"
```

---

**Last Updated:** 2025-12-27
