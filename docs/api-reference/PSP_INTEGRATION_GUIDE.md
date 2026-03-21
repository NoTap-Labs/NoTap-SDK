# PSP & Billing Provider Integration Guide

> How to add a new Payment Service Provider (checkout sessions) or Billing Provider (subscriptions) to NoTap.

**Last Updated:** 2026-03-21

---

## Two Separate Systems

NoTap has two independent payment integration layers:

| System | Purpose | Files | When to Use |
|--------|---------|-------|-------------|
| **Checkout Sessions** (`pspSessionService`) | One-time payment sessions created in parallel with NoTap auth | `services/pspSessionService.js` | A merchant's customer pays for a product/service |
| **Billing Providers** (`PaymentProvider`) | Recurring subscription billing for NoTap itself | `services/PaymentProvider.js`, `PaymentProviderFactory.js`, `StripeProvider.js`, `MoonPayProvider.js` | Charging merchants/consumers for using NoTap |

**Key distinction:** Checkout Sessions handle the merchant's payment flow (NoTap creates the session, merchant completes the payment). Billing Providers handle NoTap's own revenue (subscriptions, plan management).

---

## Part 1: Adding a New Checkout Session PSP

**Current PSPs:** Stripe, Tilopay, Adyen, MercadoPago, Square

### What This System Does

When a merchant calls `POST /v1/verification/initiate` with `psp_config`, NoTap creates a checkout session with the PSP **in parallel** with the authentication session. The merchant gets back both the auth token and a checkout URL. NoTap never processes the payment — it only creates the session.

```
Merchant → POST /v1/verification/initiate { psp_config: { psp: "stripe", ... } }
         ← { auth_session_id, psp_session: { checkout_url, session_id } }
```

### Step-by-Step: Add a New PSP (e.g., PayPal)

#### 1. Add Environment Variables

```bash
# .env and .env.example
PAYPAL_API_KEY=your_api_key
PAYPAL_API_URL=https://api.paypal.com   # Optional — set default in code
```

#### 2. Add Endpoint Config (`pspSessionService.js` line ~61)

```javascript
ENDPOINTS: {
  tilopay: process.env.TILOPAY_API_URL || 'https://api.tilopay.com',
  adyen: process.env.ADYEN_API_URL || 'https://checkout-test.adyen.com',
  mercadopago: process.env.MERCADOPAGO_API_URL || 'https://api.mercadopago.com',
  square: process.env.SQUARE_API_URL || 'https://connect.squareup.com',
  paypal: process.env.PAYPAL_API_URL || 'https://api.paypal.com'        // ← ADD
}
```

#### 3. Create Session Method (`pspSessionService.js`)

Add a method following the existing pattern. Copy any existing PSP method (e.g., `createTilopaySession`) as a template:

```javascript
// ==========================================================================
// PAYPAL INTEGRATION
// ==========================================================================

async createPayPalSession({ merchantAccount, amount, currency, metadata }) {
  try {
    const response = await axios.post(
      `${PSP_CONFIG.ENDPOINTS.paypal}/v2/checkout/orders`,
      {
        intent: 'CAPTURE',
        purchase_units: [{
          amount: {
            currency_code: currency.toUpperCase(),
            value: amount.toFixed(2)
          },
          description: metadata?.description || 'NoTap authenticated payment'
        }],
        application_context: {
          return_url: metadata?.success_url,
          cancel_url: metadata?.cancel_url
        }
      },
      {
        headers: {
          'Authorization': `Bearer ${process.env.PAYPAL_API_KEY}`,
          'Content-Type': 'application/json'
        },
        timeout: PSP_CONFIG.API_TIMEOUT
      }
    );

    // Map PSP response to NoTap's standard format
    return {
      provider: 'paypal',
      session_id: response.data.id,
      checkout_url: response.data.links.find(l => l.rel === 'approve')?.href,
      status: response.data.status,
      expires_at: new Date(Date.now() + PSP_CONFIG.SESSION_TTL).toISOString()
    };

  } catch (error) {
    logger.error(`[PayPal] Session creation failed:`, error.message);
    throw error;
  }
}
```

**Return format (MUST match):**

```javascript
{
  provider: 'paypal',           // PSP name (lowercase)
  session_id: 'ORDER-12345',   // PSP's session/order ID
  checkout_url: 'https://...',  // URL where user completes payment
  status: 'CREATED',           // PSP session status
  expires_at: '2026-...'       // ISO 8601 expiration timestamp
}
```

#### 4. Add Switch Case (`pspSessionService.js` line ~112)

```javascript
case 'paypal':
  sessionResult = await this.createPayPalSession({
    merchantAccount: psp_merchant_id,
    amount,
    currency,
    metadata: {
      notap_user: userId,
      notap_merchant: merchantId,
      ...metadata
    }
  });
  break;
```

#### 5. Update Stats (`pspSessionService.js` — `getStats()` method)

Add `'paypal'` to the supported PSPs list in the stats output.

#### 6. Add Tests

Create test cases in `tests/` following existing PSP session tests. At minimum:
- Validation accepts/rejects `paypal` as PSP name
- Session creation returns correct format
- Error handling returns null (non-blocking)

#### 7. Update Documentation

- Add PayPal to `SUPPORTED_CURRENCIES` in `PSP_CONFIG` if it supports additional currencies
- Update `backend/docs/API_ENDPOINTS_INVENTORY.md`
- Update `documentation/03-developer-guides/PARALLEL_PSP_INTEGRATION.md`

### Files Changed (Summary)

| File | Change |
|------|--------|
| `.env.example` | Add `PAYPAL_API_KEY`, `PAYPAL_API_URL` |
| `services/pspSessionService.js` | Add endpoint, method, switch case, stats |
| `tests/` | Add test cases |
| Docs | Update supported PSP lists |

**Total: ~50-70 lines of code**

---

## Part 2: Adding a New Billing Provider

**Current Providers:** Stripe (full), MoonPay (crypto+fiat)

### What This System Does

NoTap charges merchants and consumers for using the platform. The billing system handles subscription creation, plan upgrades/downgrades, cancellation, and webhook processing. It uses the Factory pattern — switch providers by changing one env var.

```
PAYMENT_PROVIDER=stripe   →  StripeProvider handles all billing
PAYMENT_PROVIDER=moonpay  →  MoonPayProvider handles all billing
PAYMENT_PROVIDER=paypal   →  PayPalProvider would handle all billing
```

### Step-by-Step: Add a New Billing Provider (e.g., PayPal)

#### 1. Add Environment Variables

```bash
# .env and .env.example
PAYMENT_PROVIDER=paypal
PAYPAL_BILLING_API_KEY=your_key
PAYPAL_BILLING_WEBHOOK_SECRET=your_webhook_secret
```

#### 2. Create Provider Class

Create `backend/services/PayPalProvider.js` extending `PaymentProvider`:

```javascript
const PaymentProvider = require('./PaymentProvider');
const axios = require('axios');
const crypto = require('crypto');
const logger = require('../utils/logger');

class PayPalProvider extends PaymentProvider {
  constructor() {
    super({
      apiKey: process.env.PAYPAL_BILLING_API_KEY,
      webhookSecret: process.env.PAYPAL_BILLING_WEBHOOK_SECRET
    });
    this.providerName = 'paypal';
    this.apiBaseUrl = process.env.PAYPAL_API_URL || 'https://api.paypal.com';
  }

  // --- 7 REQUIRED METHODS (all must be implemented) ---

  async createCheckout(params) {
    // Create subscription checkout session
    // params: { plan_code, billing_cycle, email, customer_id, customer_type, metadata }
    // Return: { checkout_url, session_id, provider_customer_id }
  }

  async cancelSubscription(subscriptionId) {
    // Cancel a subscription
    // Return: true/false
  }

  async updateSubscription(subscriptionId, new_plan_code) {
    // Upgrade/downgrade plan
    // Return: { subscription_id, status, current_period_end }
  }

  verifyWebhookSignature(payload, signature) {
    // Verify incoming webhook is authentic
    // Return: parsed event object
    // Throw on invalid signature
  }

  async processWebhookEvent(event, db) {
    // Handle subscription lifecycle events
    // Must handle: created, updated, deleted, payment_succeeded, payment_failed
    // Write to merchant_subscriptions / consumer_subscriptions tables
  }

  async getCustomerPortalUrl(customerId) {
    // Return URL where customer can manage their subscription
    // Return: URL string
  }

  async getSubscription(subscriptionId) {
    // Fetch subscription details
    // Return: { id, status, current_period_start, current_period_end, cancel_at_period_end }
  }

  async createCustomer(params) {
    // Create a customer record with the provider
    // params: { email, name, metadata }
    // Return: provider customer ID string
  }
}

module.exports = PayPalProvider;
```

#### 3. Register in Factory (`PaymentProviderFactory.js`)

Three changes:

```javascript
// 1. Import (line ~20)
const PayPalProvider = require('./PayPalProvider');

// 2. Add case (line ~36, in switch)
case 'paypal':
  return new PayPalProvider();

// 3. Update supported list (line ~60)
static getSupportedProviders() {
  return ['stripe', 'moonpay', 'paypal'];
}

// 4. Update isConfigured (line ~69)
case 'paypal':
  return !!(process.env.PAYPAL_BILLING_API_KEY && process.env.PAYPAL_BILLING_WEBHOOK_SECRET);
```

#### 4. Handle Webhooks

The webhook handler at `routes/unifiedWebhookHandler.js` already supports any provider automatically:

```
POST /v1/webhooks/:provider   →  PaymentProviderFactory.create(provider)
                              →  provider.verifyWebhookSignature()
                              →  provider.processWebhookEvent()
```

No changes needed in the webhook handler — it routes by provider name from the URL.

#### 5. Database Schema

The subscription tables (`merchant_subscriptions`, `consumer_subscriptions`) have provider-specific columns. Add columns for your provider:

```sql
-- Migration: add PayPal columns to subscription tables
ALTER TABLE merchant_subscriptions
  ADD COLUMN IF NOT EXISTS paypal_subscription_id VARCHAR(255),
  ADD COLUMN IF NOT EXISTS paypal_customer_id VARCHAR(255);

ALTER TABLE consumer_subscriptions
  ADD COLUMN IF NOT EXISTS paypal_subscription_id VARCHAR(255),
  ADD COLUMN IF NOT EXISTS paypal_customer_id VARCHAR(255);
```

#### 6. Implement Webhook Event Handlers

Your `processWebhookEvent()` must handle these subscription lifecycle events and write to the database. See `StripeProvider.js` lines 147-374 for the complete reference implementation.

**Required event handlers:**

| Event | Action | DB Update |
|-------|--------|-----------|
| Subscription created | Insert/upsert subscription row | `INSERT INTO {table} ... ON CONFLICT DO UPDATE` |
| Subscription updated | Update status, period dates | `UPDATE {table} SET status = $1 ...` |
| Subscription deleted | Downgrade to free plan | `UPDATE {table} SET plan_code = 'free' ...` |
| Payment succeeded | Mark active | `UPDATE {table} SET status = 'active' ...` |
| Payment failed | Mark past_due | `UPDATE {table} SET status = 'past_due' ...` |

**Important:** Map your PSP's event names to these handlers. For example:
- Stripe uses `customer.subscription.created`
- MoonPay uses `subscription.created`
- PayPal would use `BILLING.SUBSCRIPTION.CREATED`

#### 7. Test & Switch

```bash
# Set provider in .env
PAYMENT_PROVIDER=paypal

# Restart backend
npm run dev

# Verify configuration
curl http://localhost:3000/v1/webhooks/status
# Returns: { active_provider: "paypal", configuration: { paypal: { configured: true } } }
```

### Files Changed (Summary)

| File | Change |
|------|--------|
| `.env.example` | Add PayPal env vars |
| `services/PayPalProvider.js` | **NEW** — Full provider implementation (~300-400 LOC) |
| `services/PaymentProviderFactory.js` | Import, switch case, supported list, isConfigured |
| `migrations/0XX_add_paypal_columns.sql` | Add provider-specific columns |
| `tests/` | Provider unit tests |

**Total: ~400-500 lines of code** (mostly webhook event handlers)

---

## Part 3: Quick Reference

### Checkout Session PSP vs Billing Provider

| Question | Checkout Session | Billing Provider |
|----------|-----------------|-----------------|
| **Who pays?** | Merchant's customer | Merchant/consumer pays NoTap |
| **Payment type?** | One-time (per transaction) | Recurring subscription |
| **Who processes payment?** | The PSP (NoTap only creates session) | The provider (NoTap manages lifecycle) |
| **When is it called?** | During `POST /v1/verification/initiate` | During billing checkout flow |
| **Blocking?** | No — auth succeeds even if PSP fails | Yes — billing flow depends on it |
| **How to switch?** | Per-request (`psp_config.psp`) | Per-environment (`PAYMENT_PROVIDER`) |

### Environment Variables

**Checkout Session PSPs:**

| PSP | Required Env Vars |
|-----|-------------------|
| Stripe | `STRIPE_SECRET_KEY` |
| Tilopay | `TILOPAY_API_KEY`, `TILOPAY_API_URL` (optional) |
| Adyen | `ADYEN_API_KEY`, `ADYEN_MERCHANT_ACCOUNT`, `ADYEN_API_URL` (optional) |
| MercadoPago | `MERCADOPAGO_ACCESS_TOKEN`, `MERCADOPAGO_API_URL` (optional) |
| Square | `SQUARE_ACCESS_TOKEN`, `SQUARE_API_URL` (optional) |

**Billing Providers:**

| Provider | Required Env Vars |
|----------|-------------------|
| Stripe | `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `PAYMENT_PROVIDER=stripe` |
| MoonPay | `MOONPAY_API_KEY`, `MOONPAY_SECRET_KEY`, `MOONPAY_WEBHOOK_SECRET`, `PAYMENT_PROVIDER=moonpay` |

### Architecture Diagram

```
                    ┌─────────────────────────────────────────────────┐
                    │              NoTap Backend                       │
                    │                                                 │
  Merchant ──────▶  │  verificationRouter                             │
  (one-time)        │    └─▶ pspSessionService.createSession()        │
                    │         ├─ createStripeSession()                │
                    │         ├─ createTilopaySession()               │
                    │         ├─ createAdyenSession()                 │
                    │         ├─ createMercadoPagoSession()           │
                    │         └─ createSquareSession()                │
                    │                                                 │
  Consumer/  ─────▶ │  unifiedBillingRouter                           │
  Merchant          │    └─▶ PaymentProviderFactory.create()          │
  (subscription)    │         ├─ StripeProvider                       │
                    │         └─ MoonPayProvider                      │
                    │                                                 │
  PSP Webhooks ──▶  │  unifiedWebhookHandler                          │
                    │    └─▶ PaymentProviderFactory.create(provider)  │
                    │         └─▶ provider.processWebhookEvent()      │
                    └─────────────────────────────────────────────────┘
```

---

## Checklist: Before Merging a New PSP/Provider

- [ ] All required interface methods implemented
- [ ] Environment variables added to `.env.example`
- [ ] Environment variables added to `ci-cd.yml` (if applicable)
- [ ] Database migration created (billing providers only)
- [ ] Webhook signature verification implemented (billing providers)
- [ ] Error handling: returns `null` or `false` on failure (never crashes auth flow)
- [ ] Logging uses `logger` (not `console.log`)
- [ ] Tests written and passing
- [ ] Documentation updated (this file, API inventory, PARALLEL_PSP_INTEGRATION.md)
- [ ] Compliance gate completed (see CLAUDE.md) if >100 LOC
