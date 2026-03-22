# Merchant vs PSP Integration — Developer Guide

**Last Updated:** 2026-03-21

NoTap supports two distinct integration paths for payment authentication. This guide explains when to use each, how they differ, and how to plug in.

---

## TL;DR — Which One Am I Looking At?

| Question | Merchant Integration | PSP Integration |
|----------|---------------------|-----------------|
| **Who are you?** | Online store, app, POS system | Payment processor (Stripe, Adyen, etc.) |
| **Who controls the checkout?** | You do | The PSP does |
| **Who handles payment?** | You (via your own PSP) | The PSP (directly) |
| **Auth mechanism** | JWT (email/password login) | API Key (`psp_live_stripe_...`) |
| **Base path** | `/v1/verification/` | `/v1/psp/` |
| **SDK** | `merchant` module (Android) / `online-web` (Web) | `psp-sdk` module (KMP) |

---

## The Nike Purchase Example

Imagine a customer buying $120 Nike shoes online. Here's how each flow handles it:

### Flow A: Merchant Integration (Nike.com uses NoTap directly)

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Customer on │     │  Nike.com    │     │  NoTap       │     │  Stripe      │
│  Nike.com    │     │  (Merchant)  │     │  Backend     │     │  (PSP)       │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │  Clicks "Pay"      │                     │                    │
       │ ──────────────────>│                     │                    │
       │                    │  POST /v1/verification/initiate          │
       │                    │  {uuid, amount: 120, currency: "USD",   │
       │                    │   merchant_id, psp_config: {provider:   │
       │                    │   "stripe", ...}}                        │
       │                    │ ───────────────────>│                    │
       │                    │                     │  [PARALLEL]        │
       │                    │                     │  Creates auth      │
       │                    │                     │  session            │
       │                    │                     │        AND          │
       │                    │                     │  Creates Stripe ──>│
       │                    │                     │  checkout session   │
       │                    │                     │<─── session_url ───│
       │                    │<────────────────────│                    │
       │                    │  {session_id,       │                    │
       │                    │   required_factors:  │                    │
       │                    │   [PIN, PATTERN, EMOJI],                 │
       │                    │   psp_session: {url}}│                    │
       │  Show factor UI    │                     │                    │
       │<───────────────────│                     │                    │
       │                    │                     │                    │
       │  User enters PIN   │                     │                    │
       │  + draws pattern   │                     │                    │
       │  + picks emojis    │                     │                    │
       │ ──────────────────>│                     │                    │
       │                    │  POST /v1/verification/verify            │
       │                    │  {session_id, factors: [{type: "PIN",    │
       │                    │   digest: "a1b2..."}, ...]}              │
       │                    │ ───────────────────>│                    │
       │                    │                     │  Constant-time     │
       │                    │                     │  digest compare    │
       │                    │                     │  + ZK proof gen    │
       │                    │<────────────────────│                    │
       │                    │  {auth_token,       │                    │
       │                    │   zk_proof}         │                    │
       │                    │                     │                    │
       │                    │  Redirect to Stripe │                    │
       │                    │  checkout_url ──────────────────────────>│
       │  Payment complete  │                     │                    │
       │<───────────────────│                     │                    │
```

**Key point:** Nike.com controls the entire experience. They show the factor UI, collect digests, and then redirect to Stripe. NoTap + Stripe work in parallel (saves 200-300ms).

---

### Flow B: PSP Integration (Stripe uses NoTap as auth layer)

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Customer on │     │  Stripe      │     │  NoTap       │     │  Nike.com    │
│  checkout    │     │  (PSP)       │     │  Backend     │     │  (Merchant)  │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │  Starts payment    │                     │                    │
       │ ──────────────────>│                     │                    │
       │                    │  POST /v1/psp/session/create             │
       │                    │  Authorization: Bearer psp_live_stripe_xyz│
       │                    │  {uuid, amount: 120, currency: "USD",   │
       │                    │   merchant_id: "nike"}                    │
       │                    │ ───────────────────>│                    │
       │                    │                     │  Hash API key      │
       │                    │                     │  (SHA-256)         │
       │                    │                     │  DB lookup         │
       │                    │                     │  Fetch enrollment  │
       │                    │                     │  from Redis        │
       │                    │                     │  Decrypt factors   │
       │                    │<────────────────────│                    │
       │                    │  {session_id,       │                    │
       │                    │   required_factors:  │                    │
       │                    │   [{type: "PIN",    │                    │
       │                    │     name: "Personal PIN",               │
       │                    │     category: "knowledge"},             │
       │                    │    {type: "PATTERN", │                    │
       │                    │     name: "Pattern Lock",               │
       │                    │     category: "knowledge"},             │
       │                    │    {type: "EMOJI",  │                    │
       │                    │     name: "Emoji Sequence",             │
       │                    │     category: "knowledge"}]}            │
       │                    │                     │                    │
       │  Show NoTap UI     │                     │                    │
       │<───────────────────│                     │                    │
       │                    │                     │                    │
       │  User completes    │                     │                    │
       │  factors           │                     │                    │
       │ ──────────────────>│                     │                    │
       │                    │  POST /v1/psp/session/submit             │
       │                    │  {session_id, factors: [...]}            │
       │                    │ ───────────────────>│                    │
       │                    │                     │  IDOR check        │
       │                    │                     │  (psp_id match)    │
       │                    │                     │  Constant-time     │
       │                    │                     │  digest compare    │
       │                    │                     │  Memory wipe       │
       │                    │<────────────────────│                    │
       │                    │  {auth_token:       │                    │
       │                    │   "auth_9f3a..."}   │                    │
       │                    │                     │                    │
       │                    │  Stripe charges     │                    │
       │                    │  the card           │                    │
       │                    │  Webhook ───────────────────────────────>│
       │  Payment confirmed │                     │                    │
       │<───────────────────│                     │                    │
```

**Key point:** Stripe controls the experience. They call NoTap's API, render the factor UI in their checkout, verify the user, and then process the payment themselves. Nike.com doesn't touch NoTap directly.

---

## Authentication Deep Dive

### Merchant Auth (JWT)

```
POST /v1/auth/merchant/login
{
  "email": "store@nike.com",
  "password": "..."
}
→ { "token": "eyJhbGciOiJIUzI1NiI...", "expiresIn": 1800 }

// All subsequent requests:
Authorization: Bearer eyJhbGciOiJIUzI1NiI...
```

- **Token lifetime:** 30 minutes
- **Token claims:** `{ merchantId, email, businessName, type: 'merchant' }`
- **Revocation:** Redis blacklist on logout
- **Files:** `merchantAuthRouter.js`, `middleware/merchantAuth.js`

### PSP Auth (API Keys)

```
// Key format: psp_{env}_{pspId}_{random}
// Example:    psp_live_stripe_x7k9m2p4

// All requests:
Authorization: Bearer psp_live_stripe_x7k9m2p4
```

- **Storage:** SHA-256 hash in PostgreSQL (`psp_api_keys` table)
- **Validation:** Hash incoming key → DB lookup → check active + expiration
- **Rate limits:** Per-key (100/min, 1000/hr default, configurable per PSP)
- **Sandbox fallback:** `TEST_PSP_API_KEYS` from `sandboxConfig.js` (non-production only)
- **Files:** `pspRouter.js` (authenticatePSP middleware)

---

## Endpoint Reference

### Merchant Endpoints

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/v1/auth/merchant/login` | None | Get JWT token |
| POST | `/v1/verification/initiate` | JWT | Create auth session (+ optional parallel PSP) |
| POST | `/v1/verification/verify` | JWT | Submit factor digests |
| GET | `/v1/verification/status/:id` | JWT | Check session status |

### PSP Endpoints

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/v1/psp/session/create` | API Key | Create verification session |
| POST | `/v1/psp/session/submit` | API Key | Submit factor digests |
| GET | `/v1/psp/session/:id/status` | API Key | Check session status |

---

## Request/Response Comparison

### Session Creation

**Merchant** (`POST /v1/verification/initiate`):
```json
{
  "user_uuid": "abc-123-def",
  "transaction_amount": 120.00,
  "risk_level": "high",
  "psp_config": {
    "provider": "stripe",
    "api_key": "sk_live_...",
    "success_url": "https://nike.com/success",
    "cancel_url": "https://nike.com/cancel"
  }
}
```

**PSP** (`POST /v1/psp/session/create`):
```json
{
  "uuid": "abc-123-def",
  "amount": 120.00,
  "currency": "USD",
  "merchant_id": "nike",
  "order_id": "order-456",
  "required_factors": ["PIN", "PATTERN"],
  "metadata": { "store_id": "store-789" }
}
```

### Factor Submission

**Merchant** (`POST /v1/verification/verify`):
```json
{
  "session_id": "sess-uuid",
  "factors": [
    { "type": "PIN", "digest": "a1b2c3...64hex" },
    { "type": "PATTERN", "digest": "d4e5f6...64hex" }
  ]
}
```

**PSP** (`POST /v1/psp/session/submit`):
```json
{
  "session_id": "sess-uuid",
  "factors": [
    { "type": "PIN", "digest": "a1b2c3...64hex" },
    { "type": "PATTERN", "digest": "d4e5f6...64hex" }
  ]
}
```

---

## Risk-Based Factor Selection

Both flows use the same risk logic — the transaction amount determines how many factors the user must complete:

| Amount | Required Factors | Example ($120 Nike shoes) |
|--------|-----------------|---------------------------|
| < $50 | 2 factors | PIN + PATTERN |
| $50 - $99 | 2 factors | PIN + EMOJI |
| >= $100 | 3 factors | PIN + PATTERN + EMOJI |

The $120 Nike purchase triggers 3 factors. The system selects them randomly from the user's enrolled factors using CSPRNG (Fisher-Yates shuffle with `crypto.randomInt()`).

PSPs can override this by specifying `required_factors` in the session creation request.

---

## Files Involved

### Merchant Integration Stack

```
Backend:
  backend/routes/merchantAuthRouter.js     ← Login/register (JWT issuance)
  backend/middleware/merchantAuth.js        ← JWT validation middleware
  backend/routes/verificationRouter.js      ← Session creation + verification
  backend/services/pspSessionService.js     ← Parallel PSP checkout sessions

Android SDK:
  merchant/src/commonMain/.../VerificationManager.kt   ← Verification orchestration
  merchant/src/androidMain/.../MerchantVerificationScreen.kt  ← UI screens
  sdk/src/commonMain/.../factors/processors/           ← Factor processors (shared)

Web:
  online-web/src/jsMain/.../verification/VerificationFlow.kt  ← Web verification
  online-web/src/jsMain/.../verification/canvases/            ← Factor canvases (10)
```

### PSP Integration Stack

```
Backend:
  backend/routes/pspRouter.js              ← API key auth + session endpoints
  backend/config/sandboxConfig.js          ← Test API keys (non-production)
  backend/database/migrations/030_create_psp_api_keys.sql  ← Key storage schema

PSP SDK (KMP):
  psp-sdk/src/commonMain/.../NoTapPSP.kt              ← Main SDK entry point
  psp-sdk/src/commonMain/.../api/PSPApiClient.kt       ← API client
  psp-sdk/src/commonMain/.../PSPModels.kt              ← Data models
  psp-sdk/src/commonMain/.../VerificationResult.kt     ← Result types
```

### Shared Infrastructure (Both Flows)

```
  backend/crypto/encryption.js             ← AES-256-GCM (enrollment data)
  backend/crypto/memoryWipe.js             ← secureCompare() + wipeBuffer()
  backend/services/zkProofService.js       ← ZK-SNARK proof generation
  sdk/src/commonMain/.../crypto/CryptoUtils.kt   ← SHA-256, PBKDF2
  sdk/src/commonMain/.../factors/          ← Factor definitions (shared)
```

---

## How to Plug In

### As a Merchant

1. **Register** your merchant account via `/v1/auth/merchant/register`
2. **Login** to get a JWT via `/v1/auth/merchant/login`
3. **Initiate** verification when user clicks "Pay": `POST /v1/verification/initiate`
4. **Render** factor UI using the `merchant` module (Android) or `online-web` (Web)
5. **Submit** completed factors: `POST /v1/verification/verify`
6. **Receive** `auth_token` + optional `zk_proof` on success
7. **Process payment** through your own PSP (Stripe, Adyen, etc.)

**Optional:** Add `psp_config` to step 3 to create a PSP checkout session in parallel (saves 200-300ms).

### As a PSP

1. **Obtain** an API key (format: `psp_live_{yourPspId}_{random}`)
2. **Create session** when merchant triggers payment: `POST /v1/psp/session/create`
3. **Render** factor UI using NoTap's web components or your own UI
4. **Submit** completed factors: `POST /v1/psp/session/submit`
5. **Receive** `auth_token` on success
6. **Process payment** on your side using the auth token as proof of authentication
7. **Poll** status if needed: `GET /v1/psp/session/:id/status`

---

## Security Comparison

| Security Layer | Merchant | PSP |
|---------------|----------|-----|
| **Auth** | JWT (30-min TTL, blacklist revocation) | API Key (SHA-256 hashed, DB-backed, expiration) |
| **Rate limiting** | Per-user/IP (20/min) | Per-API-key (100/min, 1000/hr) |
| **IDOR protection** | Via JWT merchantId | Via session.psp_id === req.psp.pspId |
| **Replay protection** | Nonce + timestamp headers | Rate limiting per key |
| **Digest comparison** | Constant-time (`secureCompare`) | Constant-time (`secureCompare`) |
| **Memory wiping** | `wipeBuffer()` in finally blocks | `wipeBuffer()` in finally blocks |
| **Auth token** | Returned to merchant | Stored in Redis (5-min TTL) |
| **Audit logging** | Structured logger, hashed UUIDs | Structured logger, hashed UUIDs |

---

## When to Use Which

| Scenario | Use |
|----------|-----|
| You're building an e-commerce site and want NoTap auth | **Merchant** |
| You're a PSP and want to offer NoTap to your merchants | **PSP** |
| You want to control the entire checkout UX | **Merchant** |
| You want NoTap as a drop-in auth step in your payment flow | **PSP** |
| You want parallel PSP session creation (fastest checkout) | **Merchant** (with `psp_config`) |
| You're integrating NoTap into an existing payment gateway | **PSP** |

---

## Supported PSPs (Parallel Integration)

When using the Merchant flow with `psp_config`, these PSPs are supported for parallel session creation:

| PSP | Config Key | Session Type |
|-----|-----------|-------------|
| Stripe | `stripe` | Checkout Session |
| Tilopay | `tilopay` | Checkout Session |
| Adyen | `adyen` | Payment Session |
| MercadoPago | `mercadopago` | Checkout Preference |
| Square | `square` | Payment Link |

See `documentation/03-developer-guides/PARALLEL_PSP_INTEGRATION.md` for full configuration details.
