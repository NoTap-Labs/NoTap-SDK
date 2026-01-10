# NoTap Billing - User Guide

**Version:** 3.0
**Last Updated:** 2026-01-08
**Status:** Production Ready (Pricing Model v2.0)

---

## Table of Contents

1. [Overview](#overview)
2. [Consumer Pricing](#consumer-pricing)
3. [Merchant Pricing](#merchant-pricing)
4. [Understanding Usage](#understanding-usage)
5. [Billing Cycles](#billing-cycles)
6. [Payment Methods](#payment-methods)
7. [Cost Optimization](#cost-optimization)
8. [Alerts & Notifications](#alerts--notifications)
9. [Upgrading & Downgrading](#upgrading--downgrading)
10. [Troubleshooting](#troubleshooting)
11. [FAQ](#faq)

---

## Overview

NoTap uses a **B2B-first, freemium** pricing model with generous free tiers for consumers and value-based pricing for merchants.

### Billing Model

**Consumer (B2C):**
- ✅ **Free tier** - UNLIMITED authentications, 6 basic factors
- ✅ **Premium tiers** - Annual subscriptions for advanced features

**Merchant (B2B):**
- ✅ **Users Enrolled** - Monthly fee based on enrolled user count (not auth count)
- ✅ **Overage** - Per-user overage charges beyond quota

**What's Always Free:**
- ✅ **Sandbox Testing** - 500 test users, unlimited test authentications
- ✅ **API Calls** - Health checks, status endpoints
- ✅ **Webhooks** - Event notifications
- ✅ **Community Support** - Docs, forums (all tiers)

### Billing Philosophy

**Transparent:** No hidden fees, no surprise charges
**Value-Based:** Price reflects value delivered, not volume
**Predictable:** Monthly quotas with clear overage pricing
**Flexible:** Easy to upgrade/downgrade

---

## Consumer Pricing

NoTap is **FREE** for consumers. Premium features available via annual subscription.

### Free Tier (Default)

**Best For:** Everyone - 90% of users stay on Free

```
┌─────────────────────────────────────┐
│  NoTap FREE                         │
├─────────────────────────────────────┤
│  $0 / forever                       │
│                                     │
│  ✅ UNLIMITED authentications        │
│  ✅ 6 basic factors:                 │
│     PIN, Pattern, Emoji, Color,     │
│     Words, NFC                      │
│  ✅ 24h storage TTL                  │
│  ✅ Standard security                │
│  ✅ Community support (docs, forums) │
│                                     │
│  [ Sign Up Free ]                   │
└─────────────────────────────────────┘
```

**Why Free?** Authentication should be free (like Google Authenticator, Apple FaceID).

---

### Plus Tier

**Best For:** Power users who want all factors + priority features

```
┌─────────────────────────────────────┐
│  NoTap PLUS                         │
├─────────────────────────────────────┤
│  $9.99 / year ($0.83/month)         │
│                                     │
│  ✅ UNLIMITED authentications        │
│  ✅ All 15 factors (incl. biometrics)│
│  ✅ 72h extended storage TTL         │
│  ✅ Priority verification queue      │
│  ✅ Crypto wallet payments           │
│  ✅ Custom alias                     │
│  ✅ Email support (24h response)     │
│                                     │
│  [ Get Plus - $9.99/year ]          │
└─────────────────────────────────────┘
```

---

### Crypto Tier

**Best For:** Privacy enthusiasts, crypto-native users

```
┌─────────────────────────────────────┐
│  NoTap CRYPTO                       │
├─────────────────────────────────────┤
│  $29.99 / year ($2.50/month)        │
│                                     │
│  ✅ Everything in Plus               │
│  ✅ Full crypto support (USDC, SOL,  │
│     USDT, ETH)                      │
│  ✅ Daily automatic key rotation     │
│  ✅ Zero-knowledge privacy mode      │
│  ✅ Blockchain audit trail           │
│  ✅ Multi-chain names (SNS, ENS, UD) │
│  ✅ Hardware wallet integration      │
│  ✅ Priority crypto support          │
│                                     │
│  [ Get Crypto - $29.99/year ]       │
└─────────────────────────────────────┘
```

---

### Consumer Pricing Comparison

| Feature | Free | Plus | Crypto |
|---------|------|------|--------|
| **Price** | $0 | $9.99/year | $29.99/year |
| **Authentications** | UNLIMITED | UNLIMITED | UNLIMITED |
| **Factors** | 6 basic | All 15 | All 15 |
| **Storage TTL** | 24h | 72h | 72h |
| **Support** | Community | Email (24h) | Priority |
| **Crypto Payments** | No | USDC, SOL | Full (4 chains) |
| **ZK Privacy** | No | No | Yes |
| **Key Rotation** | No | No | Daily |

---

## Merchant Pricing

Merchants pay based on **users enrolled** (not authentication count). This captures long-term value and aligns with Auth0/Okta pricing models.

### Sandbox (Free)

**Best For:** Testing, POCs, hackathons

```
┌─────────────────────────────────────┐
│  SANDBOX                            │
├─────────────────────────────────────┤
│  $0 / month                         │
│                                     │
│  ✅ 500 enrolled users (hard limit)  │
│  ✅ Full API access                  │
│  ✅ Test mode only (no production)   │
│  ✅ Community support                │
│  ✅ 1 API key                        │
│                                     │
│  [ Start Free ]                     │
└─────────────────────────────────────┘
```

---

### Startup ($299/month)

**Best For:** Indie devs, MVPs, small e-commerce

```
┌─────────────────────────────────────┐
│  STARTUP                            │
├─────────────────────────────────────┤
│  $299/month or $2,990/year (17% off)│
│                                     │
│  ✅ 1,000 enrolled users             │
│  ✅ $0.25/user overage               │
│  ✅ Production API access            │
│  ✅ Basic fraud detection            │
│  ✅ Email support (48h response)     │
│  ✅ 2 API keys                       │
│  ✅ Basic webhooks                   │
│                                     │
│  [ Start Building - $299/mo ]       │
└─────────────────────────────────────┘
```

**Cost Per User:** $0.30/user/month (1,000 for $299)

---

### Growth ($999/month)

**Best For:** SMB e-commerce, SaaS, neo-banks

```
┌─────────────────────────────────────┐
│  GROWTH                             │
├─────────────────────────────────────┤
│  $999/month or $9,990/year (17% off)│
│                                     │
│  ✅ 10,000 enrolled users            │
│  ✅ $0.08/user overage               │
│  ✅ All Startup features             │
│  ✅ Advanced fraud detection (7 strat)│
│  ✅ Custom branding (logo, colors)   │
│  ✅ Chat support (12h response)      │
│  ✅ 5 API keys + IP whitelist        │
│  ✅ Advanced webhooks                │
│                                     │
│  99.5% Uptime SLA                   │
│                                     │
│  [ Scale Up - $999/mo ]             │
└─────────────────────────────────────┘
```

**Cost Per User:** $0.10/user/month (10,000 for $999)

---

### Scale ($4,999/month)

**Best For:** Fintechs, large merchants, banks

```
┌─────────────────────────────────────┐
│  SCALE                              │
├─────────────────────────────────────┤
│  $4,999/month or $49,990/year       │
│                                     │
│  ✅ 100,000 enrolled users           │
│  ✅ $0.04/user overage               │
│  ✅ All Growth features              │
│  ✅ SLA 99.9% uptime guarantee       │
│  ✅ Blockchain audit trail           │
│  ✅ Dedicated Account Manager        │
│  ✅ Priority support (4h phone+email)│
│  ✅ 10 API keys + advanced whitelist │
│  ✅ White-label options              │
│                                     │
│  [ Enterprise Ready - $4,999/mo ]   │
└─────────────────────────────────────┘
```

**Cost Per User:** $0.05/user/month (100,000 for $4,999)

---

### Enterprise ($25,000+/month)

**Best For:** Acquirers, tier-1 PSPs, telcos, governments

```
┌─────────────────────────────────────┐
│  ENTERPRISE                         │
├─────────────────────────────────────┤
│  From $25,000/month (custom)        │
│                                     │
│  ✅ UNLIMITED enrolled users         │
│  ✅ All Scale features               │
│  ✅ Full white-label (complete rebrand)│
│  ✅ Dedicated infrastructure         │
│  ✅ Custom factor development        │
│  ✅ SLA 99.99% (four nines)          │
│  ✅ Custom MSA (legal terms)         │
│  ✅ 24/7 phone support               │
│  ✅ Dedicated Customer Success Mgr   │
│                                     │
│  [ Contact Sales ]                  │
└─────────────────────────────────────┘
```

---

### Merchant Pricing Comparison

| Feature | Sandbox | Startup | Growth | Scale | Enterprise |
|---------|---------|---------|--------|-------|------------|
| **Monthly Cost** | $0 | $299 | $999 | $4,999 | $25,000+ |
| **Users Enrolled** | 500 | 1,000 | 10,000 | 100,000 | Unlimited |
| **Overage/User** | N/A | $0.25 | $0.08 | $0.04 | Flat fee |
| **API Keys** | 1 | 2 | 5 | 10 | Unlimited |
| **Support** | Community | Email 48h | Chat 12h | Phone 4h | 24/7 |
| **Uptime SLA** | None | None | 99.5% | 99.9% | 99.99% |
| **White-label** | No | No | No | Partial | Full |

---

## Understanding Usage

### What Counts Toward Your Quota?

**Merchant Plans - Users Enrolled:**
- ✅ Each unique user who completes enrollment counts as 1 user
- ✅ Users remain counted until their enrollment expires or is deleted
- ✅ Re-enrollments of the same user don't count twice

**NOT Counted:**
- ❌ Authentication attempts (unlimited per enrolled user)
- ❌ Sandbox users (test mode)
- ❌ Failed enrollments
- ❌ API calls (health checks, status endpoints)

**Key Difference:** You pay for **users enrolled**, not **authentications performed**.

### Viewing Your Usage

**Dashboard → Billing → Usage**

```
┌────────────────────────────────────────────────────┐
│  Usage This Month (January 2026)                  │
├────────────────────────────────────────────────────┤
│  Plan: Growth ($999/month)                        │
│  Billing Period: Jan 1 - Jan 31                   │
│                                                    │
│  Users Enrolled: 6,250 / 10,000                   │
│  ████████████████░░░░░░░░░░░░ 63%                  │
│                                                    │
│  Breakdown:                                        │
│  • Active Users: 5,890 (94%)                      │
│  • Inactive (no auth in 30d): 360 (6%)            │
│                                                    │
│  Authentications This Month: 45,200               │
│  (unlimited - no extra charge)                    │
│                                                    │
│  Current Charges:                                 │
│  • Base Plan: $999.00                             │
│  • Overage: $0.00 (0 overage users)               │
│  • Total: $999.00                                 │
└────────────────────────────────────────────────────┘
```

### Usage by Project

**Breakdown by individual project:**

```
┌────────────────────────────────────────────────────┐
│  E-commerce Store (proj_abc123)                   │
│  54,200 verifications (87%)                       │
│  Success Rate: 98.2%                              │
│  Avg Latency: 280ms                               │
├────────────────────────────────────────────────────┤
│  Mobile App (proj_def456)                         │
│  8,250 verifications (13%)                        │
│  Success Rate: 95.8%                              │
│  Avg Latency: 420ms                               │
└────────────────────────────────────────────────────┘
```

### Usage Trends

**Graph: Daily Verifications (Last 30 Days)**

```
10K ┤                                              ╭─
    │                                          ╭───╯
 8K ┤                                      ╭───╯
    │                                  ╭───╯
 6K ┤                              ╭───╯
    │                          ╭───╯
 4K ┤                      ╭───╯
    │                  ╭───╯
 2K ┤              ╭───╯
    │          ╭───╯
  0 ┼──────────╯
    Dec 1          Dec 10          Dec 20          Dec 30

Peak: 9,850 verifications (Dec 28 - Holiday sales)
Average: 2,015 verifications/day
Trend: +12% growth week-over-week
```

---

## Billing Cycles

### Billing Period

**All plans:** Calendar month (1st to last day)

| Start Date | End Date | Invoice Date |
|------------|----------|--------------|
| Dec 1, 2025 | Dec 31, 2025 | Jan 1, 2026 |
| Jan 1, 2026 | Jan 31, 2026 | Feb 1, 2026 |

### Prorated Charges

**Example: Upgrade mid-month**

You're on **Sandbox** tier and upgrade to **Startup** on Jan 15:

```
Jan 1 - Jan 14:  Sandbox tier (14 days)
Jan 15 - Jan 31: Startup tier (17 days)

Prorated Charge:
  ($299 ÷ 31 days) × 17 days = $163.97

Invoice on Feb 1:
  • Prorated Startup (Jan 15-31): $163.97
  • Full Startup (Feb 1-28): $299.00
  • Total: $462.97
```

### Invoice Generation

**Automatic invoice generation on the 1st of each month:**

```
┌────────────────────────────────────────────────────┐
│  Invoice #INV-2026-01-001                         │
├────────────────────────────────────────────────────┤
│  Billing Period: January 1-31, 2026               │
│  Invoice Date: February 1, 2026                   │
│  Due Date: February 8, 2026                       │
│                                                    │
│  CHARGES                                           │
│  ─────────────────────────────────────            │
│  Growth Plan                  $999.00             │
│  10,000 enrolled users included                   │
│                                                    │
│  Overage Charges              $40.00              │
│  500 users × $0.08/user                           │
│                                                    │
│  ─────────────────────────────────────            │
│  SUBTOTAL                   $1,039.00             │
│  Tax (8.5% CA sales tax)      $88.32              │
│  ─────────────────────────────────────            │
│  TOTAL                      $1,127.32             │
│                                                    │
│  Payment Method: Visa •••• 4242                   │
│  Status: Paid (Feb 1, 2026 00:05 UTC)             │
│                                                    │
│  [ Download PDF ]  [ View Details ]               │
└────────────────────────────────────────────────────┘
```

### Failed Payments

**What happens if payment fails:**

| Day | Action |
|-----|--------|
| **Day 1** | Payment fails, email sent, retry in 3 days |
| **Day 4** | Second attempt, email sent |
| **Day 7** | Third attempt, email + SMS sent |
| **Day 10** | API access suspended (read-only) |
| **Day 14** | Account downgraded to Free tier |
| **Day 30** | Account scheduled for deletion |

**Grace Period:** 10 days (API continues working)

**How to Recover:**

1. **Update payment method** in Dashboard → Billing
2. **Retry payment** (automatic once new method added)
3. **Contact support** if account suspended

---

## Payment Methods

### Supported Payment Methods

| Method | Processing Time | Supported Regions | Fees |
|--------|----------------|-------------------|------|
| **Credit/Debit Card** | Instant | Worldwide | None |
| **ACH Bank Transfer** | 3-5 business days | US only | None |
| **SEPA Direct Debit** | 3-5 business days | EU only | None |
| **Wire Transfer** | 1-2 business days | Worldwide | $15 fee |
| **PayPal** | Instant | Worldwide | None |

### Add Payment Method

**Dashboard → Billing → Payment Methods → Add New**

```
┌─────────────────────────────────────┐
│  Add Payment Method                 │
├─────────────────────────────────────┤
│  ● Credit/Debit Card                │
│  ○ ACH Bank Transfer                │
│  ○ PayPal                           │
│                                     │
│  Card Number:                       │
│  [4242 4242 4242 4242          ]    │
│                                     │
│  Expiry:           CVV:             │
│  [12/26  ]         [123  ]          │
│                                     │
│  Name on Card:                      │
│  [John Doe                     ]    │
│                                     │
│  Billing Address:                   │
│  [123 Main St                  ]    │
│  [San Francisco, CA 94105      ]    │
│  [United States            ▼]       │
│                                     │
│  ☑️ Set as default payment method    │
│                                     │
│  [ Add Card ]                       │
└─────────────────────────────────────┘
```

### Security

**Payment security:**
- ✅ PCI DSS Level 1 compliant
- ✅ Card data encrypted at rest (AES-256)
- ✅ TLS 1.3 for data in transit
- ✅ 3D Secure (3DS) support
- ✅ Fraud detection (Stripe Radar)

**NoTap does NOT store:**
- ❌ Full credit card numbers (only last 4 digits)
- ❌ CVV codes
- ❌ Bank account credentials

All payment processing handled by **Stripe** (certified PCI Level 1).

---

## Cost Optimization

### Strategies to Reduce Costs

#### 1. Cache Verification Results

**Problem:** Verifying the same user multiple times in short succession

**Solution:** Cache successful verifications for 5-15 minutes

```javascript
// ❌ BAD - Verify on every request
app.get('/api/protected', async (req, res) => {
  const result = await notap.verify(uuid, factors);
  if (!result.success) return res.status(401).send('Unauthorized');
  // Handle request
});

// ✅ GOOD - Cache verification result
const verificationCache = new Map();

app.get('/api/protected', async (req, res) => {
  const sessionId = req.headers['x-session-id'];

  // Check cache first
  if (verificationCache.has(sessionId)) {
    const cached = verificationCache.get(sessionId);
    if (Date.now() - cached.timestamp < 300000) { // 5 min
      return handleRequest(req, res);
    }
  }

  // Verify and cache
  const result = await notap.verify(uuid, factors);
  if (result.success) {
    verificationCache.set(sessionId, {
      timestamp: Date.now(),
      uuid: uuid
    });
    return handleRequest(req, res);
  }

  res.status(401).send('Unauthorized');
});
```

**Savings:** ~80% reduction in verifications for active users

---

#### 2. Use Session-Based Verification

**Problem:** Re-verifying on every page load

**Solution:** Verify once, issue JWT session token

```javascript
// User completes NoTap verification
const notapResult = await notap.verify(uuid, factors);

if (notapResult.success) {
  // Issue JWT token valid for 1 hour
  const sessionToken = jwt.sign(
    { uuid: uuid, verified: true },
    process.env.JWT_SECRET,
    { expiresIn: '1h' }
  );

  res.cookie('session', sessionToken, {
    httpOnly: true,
    secure: true,
    maxAge: 3600000 // 1 hour
  });
}

// Subsequent requests: validate JWT (no NoTap API call)
app.use((req, res, next) => {
  const token = req.cookies.session;
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    res.status(401).send('Session expired - re-authenticate');
  }
});
```

**Savings:** 1 verification per session instead of per request

---

#### 3. Implement Client-Side Factor Validation

**Problem:** Sending obviously invalid factors to API

**Solution:** Validate factor format client-side before API call

```javascript
// ✅ GOOD - Validate before API call
function validatePIN(pin) {
  // Check format before calling API
  if (!/^\d{4,8}$/.test(pin)) {
    return { valid: false, error: 'PIN must be 4-8 digits' };
  }
  return { valid: true };
}

const pinCheck = validatePIN(userInput);
if (!pinCheck.valid) {
  // Show error immediately (no API call)
  showError(pinCheck.error);
  return;
}

// Only call API if format is valid
const result = await notap.verify(uuid, { pin: userInput });
```

**Savings:** Reduces failed verification costs by ~30%

---

#### 4. Use Risk-Based Step-Up Authentication

**Problem:** Always requiring maximum factors

**Solution:** Require fewer factors for low-risk actions

```javascript
// Low-risk action (view balance): 1 factor
if (action === 'view_balance') {
  requiredFactors = 1; // $0.0019 per verification
}

// Medium-risk (send payment): 2 factors
if (action === 'send_payment') {
  requiredFactors = 2; // $0.0019 per verification (same cost!)
}

// High-risk (change factors): 3+ factors
if (action === 'change_security_settings') {
  requiredFactors = 3; // $0.0019 per verification
}
```

**Note:** Cost is per verification API call, NOT per factor count

**Actual Savings:** Use fewer verifications overall by only verifying when needed

---

#### 5. Batch Verifications (Enterprise Only)

**Problem:** Verifying 1,000 users sequentially

**Solution:** Use batch verification endpoint

```javascript
// ❌ BAD - 1,000 API calls
for (const user of users) {
  await notap.verify(user.uuid, user.factors); // $0.002 × 1,000 = $2.00
}

// ✅ GOOD - 1 batch API call (Enterprise feature)
const results = await notap.verifyBatch(users); // $0.001 × 1,000 = $1.00
```

**Savings:** 50% discount on batch verifications

---

### Cost Estimator Tool

**Dashboard → Billing → Cost Estimator**

```
┌─────────────────────────────────────┐
│  Estimate Your Monthly Costs        │
├─────────────────────────────────────┤
│  Expected Users to Enroll:          │
│  [8,000                        ]    │
│                                     │
│  Average Auths per User/Month:      │
│  [12                           ]    │
│  (Unlimited - no extra charge)      │
│                                     │
│  ─────────────────────────────────  │
│  ESTIMATE                           │
│  ─────────────────────────────────  │
│  Users Enrolled: 8,000              │
│  Total Authentications: 96,000      │
│  (no extra charge - unlimited)      │
│                                     │
│  Recommended Plan: Growth           │
│  • Base Cost: $999/month            │
│  • Included: 10,000 users           │
│  • Overage: $0 (within quota)       │
│  • Total: $999/month                │
│                                     │
│  OR if expecting growth to 15K:     │
│  • Growth with overage:             │
│    $999 + (5,000 × $0.08) = $1,399  │
│  • Scale would be: $4,999           │
│    (better if >12,500 users)        │
│                                     │
│  [ Choose Growth Plan ]             │
└─────────────────────────────────────┘
```

---

## Alerts & Notifications

### Configure Billing Alerts

**Dashboard → Billing → Alerts**

```
┌─────────────────────────────────────┐
│  Billing Alert Settings             │
├─────────────────────────────────────┤
│  Quota Usage Alerts:                │
│  ☑️ 50% of quota used                │
│  ☑️ 80% of quota used                │
│  ☑️ 95% of quota used                │
│  ☑️ 100% of quota (overage started)  │
│                                     │
│  Spending Alerts:                   │
│  ☑️ Monthly cost exceeds $500        │
│  ☑️ Daily cost exceeds $50           │
│  ☐ Custom threshold: [$       ]     │
│                                     │
│  Notification Channels:             │
│  ☑️ Email (billing@company.com)      │
│  ☑️ Slack (#billing-alerts)          │
│  ☐ SMS (for critical alerts)        │
│                                     │
│  [ Save Settings ]                  │
└─────────────────────────────────────┘
```

### Sample Alert Email

```
From: NoTap Billing <support@notap.io>
Subject: ⚠️ 80% Quota Reached - Professional Plan

Hi John,

Your NoTap project "E-commerce Store" has used 80% of its monthly quota.

Current Usage:
  • 80,000 / 100,000 verifications (80%)
  • 11 days remaining in billing cycle
  • Projected end-of-month: 102,000 verifications

Action Needed:
  • Monitor usage closely
  • Consider upgrading to avoid overage charges
  • Implement caching to reduce verification calls

Estimated Overage Charges:
  • 2,000 overage verifications × $0.002 = $4.00

View Usage Dashboard:
https://developer.notap.io/billing/usage

Need help optimizing costs?
Reply to this email or visit our cost optimization guide:
https://docs.notap.io/billing/optimization
```

---

## Upgrading & Downgrading

### Upgrade Your Plan

**Dashboard → Billing → Upgrade**

```
┌─────────────────────────────────────┐
│  Upgrade Your Plan                  │
├─────────────────────────────────────┤
│  Current Plan: Startup ($299/month) │
│  Usage: 850 / 1,000 users (85%)     │
│                                     │
│  Recommended Upgrade: Growth        │
│                                     │
│  ✅ 10× more users (10,000)          │
│  ✅ Lower overage ($0.08 vs $0.25)   │
│  ✅ Advanced fraud detection         │
│  ✅ Better support (12h vs 48h)      │
│                                     │
│  New Monthly Cost: $999              │
│  Prorated Charge Today: $548.03     │
│  (17 days remaining × $32.24/day)   │
│                                     │
│  [ Upgrade Now ]                    │
└─────────────────────────────────────┘
```

**Effect:**
- ✅ Immediate access to new quotas
- ✅ Prorated charge applied
- ✅ Next invoice: full $999

---

### Downgrade Your Plan

**Dashboard → Billing → Downgrade**

```
┌─────────────────────────────────────┐
│  Downgrade Your Plan                │
├─────────────────────────────────────┤
│  Current Plan: Growth               │
│  Usage: 1,523 / 10,000 users (15%)  │
│                                     │
│  Downgrade to: Startup              │
│                                     │
│  ⚠️  WARNING: Potential Issues       │
│  • Current users (1,523) exceeds    │
│    Startup quota (1,000)            │
│  • Overage charges will apply:      │
│    523 × $0.25 = $130.75/month      │
│  • Consider staying on Growth       │
│                                     │
│  New Monthly Cost: $299              │
│  Credit Applied: $412.30            │
│  (unused portion of current plan)   │
│                                     │
│  Effective Date: Feb 1, 2026        │
│  (applied at next billing cycle)    │
│                                     │
│  [ Confirm Downgrade ]  [ Cancel ]  │
└─────────────────────────────────────┘
```

**Effect:**
- ✅ Applied at next billing cycle (not immediate)
- ✅ Credit applied to final invoice
- ❌ No partial refunds

---

### Pause Your Account

**Use Case:** Temporarily stop all API access (vacation, maintenance)

**Dashboard → Billing → Pause Account**

```
┌─────────────────────────────────────┐
│  Pause Account                      │
├─────────────────────────────────────┤
│  Pausing will:                      │
│  • Stop all API access              │
│  • Downgrade to Free tier           │
│  • Delete all API keys (reversible) │
│  • Retain all project data          │
│                                     │
│  Resume anytime with 1 click        │
│                                     │
│  Pause Duration:                    │
│  [1 month                      ▼]   │
│                                     │
│  Auto-Resume Date: Feb 1, 2026      │
│                                     │
│  [ Pause Account ]                  │
└─────────────────────────────────────┘
```

**Effect:**
- ✅ $0 charges while paused
- ✅ Data retained for 90 days
- ✅ Easy resume (regenerate API keys)

---

## Troubleshooting

### Common Billing Issues

#### ❌ "Payment Method Declined"

**Error:**

```
Your payment method was declined.
Reason: Insufficient funds / Expired card

Update your payment method to continue using NoTap.
```

**Solutions:**

1. **Check card expiry date**
2. **Verify sufficient funds**
3. **Contact your bank** (may be flagged as fraud)
4. **Try alternative payment method** (different card, PayPal)

---

#### ❌ "Unexpected Overage Charges"

**Scenario:** Invoice shows $500 in overage charges, but you expected $200

**Investigation Steps:**

1. **Check usage breakdown:**
   - Dashboard → Billing → Usage → Daily Breakdown

2. **Look for usage spikes:**
   - Graph may show sudden spike (bot attack, DDoS)

3. **Check project breakdown:**
   - One project may be consuming more than expected

4. **Review API logs:**
   - Download → Billing → Export Usage CSV

**Example CSV:**

```csv
date,project_id,verifications,success_rate
2025-12-25,proj_abc123,45000,98%  ← Spike on Christmas
2025-12-26,proj_abc123,2100,97%
2025-12-27,proj_abc123,1980,96%
```

**Solution:**
- Implement rate limiting on your side
- Contact support for refund (if proven bot attack)

---

#### ❌ "Usage Not Updating"

**Scenario:** Dashboard shows 0 verifications, but you made API calls

**Causes:**

1. **Using sandbox API keys** (not counted)
2. **Cache delay** (updates every 5 minutes)
3. **Timezone mismatch** (usage resets at midnight UTC)

**Solutions:**

1. **Check API key prefix:**
   - `sk_test_` = Sandbox (not counted)
   - `sk_live_` = Production (counted)

2. **Wait 5 minutes** and refresh

3. **Check timezone:**
   - Dashboard → Settings → Timezone → UTC

---

#### ❌ "Credit Not Applied After Downgrade"

**Scenario:** Downgraded from Growth to Startup, but invoice shows full $999

**Explanation:**

Downgrades apply at **next billing cycle**, not immediately.

**Example Timeline:**

```
Jan 15: Request downgrade Growth → Startup
Jan 31: Last day of current billing cycle
Feb 1:  Downgrade applied, invoice shows:
        • Jan 1-31: Growth ($999)
        • Credit: -$412.30 (unused days)
        • Feb 1-28: Startup ($299)
        • Total due: $885.70
```

**No action needed** - credit applied correctly

---

## FAQ

### Billing Questions

**Q: Can I get a refund if I don't use my quota?**

**A:** No, monthly quotas are "use it or lose it". Consider downgrading if consistently under quota.

---

**Q: Do failed verifications count against my quota?**

**A:** Yes, all verification API calls count (success + failure). This prevents abuse.

---

**Q: Can I share my quota across multiple projects?**

**A:** Yes! Quota is account-wide, not per-project.

---

**Q: What happens if I exceed my quota on Free tier?**

**A:** API returns `402 Payment Required`. No overage charges - hard limit enforced.

---

**Q: What happens if I exceed my quota on paid tiers?**

**A:** Overage charges apply automatically (see pricing). API continues working.

---

**Q: When are invoices charged?**

**A:** Automatically on the 1st of each month. Payment method charged within 24 hours.

---

**Q: Can I pay annually for a discount?**

**A:** Yes! Annual plans get 2 months free (16% discount). Contact sales for annual invoicing.

---

### Payment Questions

**Q: Which payment methods are accepted?**

**A:**
- ✅ Credit/debit cards (Visa, Mastercard, Amex, Discover)
- ✅ ACH bank transfer (US only)
- ✅ SEPA direct debit (EU only)
- ✅ PayPal
- ✅ Wire transfer (Enterprise only, $15 fee)

---

**Q: Is my payment information secure?**

**A:** Yes. NoTap is PCI DSS Level 1 compliant. All payment processing handled by Stripe.

---

**Q: Can I use purchase orders (PO)?**

**A:** Yes, for Enterprise plans only. Contact partnership@notap.io.

---

**Q: Do you offer invoicing (net 30)?**

**A:** Yes, for Professional and Enterprise plans with approved credit. Contact sales.

---

### Plan Questions

**Q: Can I upgrade mid-month?**

**A:** Yes! Prorated charge applied immediately, full new plan charge next month.

---

**Q: Can I downgrade mid-month?**

**A:** Yes, but downgrade applies at **next billing cycle** (end of month). Credit issued for unused portion.

---

**Q: What if I need more than Enterprise tier offers?**

**A:** Contact partnership@notap.io for custom pricing (1M+ enrolled users, dedicated infrastructure, custom factors).

---

**Q: Can I pause my account temporarily?**

**A:** Yes! Dashboard → Billing → Pause Account. $0 charges while paused, data retained 90 days.

---

## Support

**Billing Support:**

| Issue Type | Contact Method | Response Time |
|------------|---------------|---------------|
| **Payment failed** | support@notap.io | 8 hours |
| **Unexpected charges** | support@notap.io | 24 hours |
| **Refund request** | support@notap.io | 48 hours |
| **Enterprise quotes** | partnership@notap.io | 8 hours |
| **Invoice questions** | support@notap.io | 24 hours |

**Before contacting support:**

1. Check **Dashboard → Billing → Usage** for detailed breakdown
2. Review [Cost Optimization](#cost-optimization) strategies
3. Export usage CSV for analysis

**Include in support email:**

- Account email
- Invoice number (if applicable)
- Detailed description of issue
- Usage CSV export (if relevant)

---

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| 3.0 | 2026-01-08 | Complete rewrite for Pricing Model v2.0: user-based pricing, new tiers (Startup/Growth/Scale), consumer tiers (Free/Plus/Crypto) |
| 2.0 | 2025-12-03 | Added cost optimization strategies, alert configuration, pause account feature |
| 1.0 | 2025-11-19 | Initial release |

---

**End of Billing User Guide**
