---
hidden: true
---

# NoTap API Endpoints Inventory

**Auto-generated endpoint count and catalog.**

**Last Updated:** 2025-12-27

***

## 📊 Quick Stats

* **Total Endpoints:** 322
* **Total Routers:** 39
* **API Version:** v1
* **Production URL:** `https://api.notap.io`

> ⚠️ **CRITICAL:** Always use `https://api.notap.io` - NOT any `*.railway.app` URLs!

***

## 🔄 Auto-Update Instructions

**CRITICAL:** Update this document whenever you add, remove, or modify endpoints.

### When to Update:

1. ✅ **After creating a new router** - Add router to count
2. ✅ **After adding endpoints to existing router** - Update endpoint count
3. ✅ **After removing endpoints** - Update endpoint count
4. ✅ **Before committing changes** - Verify count is accurate

### How to Update:

```bash
# 1. Navigate to routes directory
cd backend/routes

# 2. Run auto-count script
bash /tmp/count_endpoints.sh > /tmp/endpoint_count.txt

# 3. Update this document with new counts

# 4. Commit with the code changes
git add docs/API_ENDPOINTS_INVENTORY.md
git commit -m "docs: Update endpoint count to XXX"
```

### Auto-Count Script:

The script `/tmp/count_endpoints.sh` automatically counts all endpoints:

```bash
#!/bin/bash
cd /mnt/c/Users/USUARIO/StudioProjects/zero-pay-sdk/zeropay-android/backend/routes
total=0
for file in *.js; do
    count=$(grep -E "router\.(get|post|put|patch|delete)\(" "$file" 2>/dev/null | wc -l)
    if [ $count -gt 0 ]; then
        printf "%-40s | %d\n" "$file" "$count"
        total=$((total + count))
    fi
done
echo "TOTAL: $total endpoints"
```

***

## 📈 Endpoints by Category

| Category                       | Count   | Percentage |
| ------------------------------ | ------- | ---------- |
| 🔐 **Admin**                   | 116     | 36.0%      |
| 👨‍💻 **Developer & Merchant** | 60      | 18.6%      |
| ⛓️ **Blockchain & Crypto**     | 51      | 15.8%      |
| 🔑 **Authentication**          | 35      | 10.9%      |
| 🔧 **Other**                   | 26      | 8.1%       |
| ⚡ **Core NoTap**               | 21      | 6.5%       |
| 💳 **Payment & Billing**       | 13      | 4.0%       |
| **TOTAL**                      | **322** | **100%**   |

***

## 🔐 Admin Endpoints (116)

| Router                           | Endpoints | Base Path                   |
| -------------------------------- | --------- | --------------------------- |
| adminBillingRouter.js            | 18        | `/v1/admin/billing`         |
| adminManagementRouter.js         | 14        | `/v1/admin/admins`          |
| adminUserRouter.js               | 13        | `/v1/admin/users`           |
| adminTeamRouter.js               | 12        | `/v1/admin/teams`           |
| adminUserManagementRouter.js     | 10        | `/v1/admin/user-management` |
| adminRouter.js                   | 10        | `/v1/admin`                 |
| adminAliasRouter.js              | 8         | `/v1/admin/alias`           |
| adminAuditRouter.js              | 8         | `/v1/admin/audit`           |
| adminConfigurationRouter.js      | 7         | `/v1/admin/configuration`   |
| adminAnalyticsRouter.js          | 6         | `/v1/admin/analytics`       |
| adminSecurityMonitoringRouter.js | 6         | `/v1/admin/security`        |
| adminAuthRouter.js               | 4         | `/v1/auth/admin`            |

**Purpose:** System administration, user management, analytics, billing, audit, security monitoring.

***

## 🔑 Authentication Endpoints (35)

| Router                 | Endpoints | Base Path            | User Type    |
| ---------------------- | --------- | -------------------- | ------------ |
| developerAuthRouter.js | 13        | `/v1/auth/developer` | Developer    |
| regularAuthRouter.js   | 12        | `/v1/auth/user`      | Regular User |
| merchantAuthRouter.js  | 6         | `/v1/auth/merchant`  | Merchant     |
| adminAuthRouter.js     | 4         | `/v1/auth/admin`     | Admin        |

**Purpose:** JWT-based authentication for 4 user types with type separation.

**Features:**

* Email/password registration & login
* OAuth (Google, GitHub) - Developer, Regular, Merchant only
* NoTap passwordless authentication - Optional for Developer/Regular/Merchant, **REQUIRED for Admin**
* Profile management
* Token validation

**Security:**

* JWT tokens with type field (`developer`, `user`, `merchant`, `admin`)
* bcrypt password hashing (12 rounds)
* Admin: 12-char password minimum, MFA required
* Failed login tracking with account lockout (Admin only)
* Rate limiting: 10 req/min per IP

***

## 👨‍💻 Developer & Merchant Endpoints (60)

| Router                     | Endpoints | Base Path                 |
| -------------------------- | --------- | ------------------------- |
| merchantRouter.js          | 21        | `/v1/merchant`            |
| merchantDashboardRouter.js | 17        | `/api/merchant/dashboard` |
| developerPortalRouter.js   | 16        | `/v1/developer`           |
| merchantBillingRouter.js   | 6         | `/api/merchant/billing`   |

**Purpose:** Developer portal (API keys, webhooks, usage), merchant dashboard (analytics, transactions, settings).

***

## ⛓️ Blockchain & Crypto Endpoints (51)

| Router                 | Endpoints | Base Path        |
| ---------------------- | --------- | ---------------- |
| cryptoPaymentRouter.js | 14        | `/v1/crypto`     |
| didRouter.js           | 10        | `/v1/did`        |
| blockchainRouter.js    | 8         | `/v1/blockchain` |
| namesRouter.js         | 7         | `/v1/names`      |
| walletRouter.js        | 6         | `/v1/wallet`     |
| snsRouter.js           | 6         | `/v1/sns`        |

**Purpose:** Crypto payments (Solana, USDC), blockchain name services (SNS, ENS, Unstoppable, BASE), DID, wallet management.

**Supported Chains:**

* Solana (SNS: `.sol`, `.notap.sol`)
* Ethereum (ENS: `.eth`)
* Polygon (Unstoppable: `.crypto`, `.wallet`, `.nft`, etc.)
* Base L2 (`.base.eth`)

***

## ⚡ Core NoTap Endpoints (21)

| Router                     | Endpoints | Base Path                |
| -------------------------- | --------- | ------------------------ |
| enrollmentRouter.js        | 7         | `/v1/enrollment`         |
| managementRouter.js        | 5         | `/v1/management`         |
| managementDevicesRouter.js | 5         | `/v1/management/devices` |
| verificationRouter.js      | 4         | `/v1/verification`       |

**Purpose:** Core NoTap functionality - factor enrollment, verification, self-service management.

**Key Endpoints:**

* POST `/v1/enrollment/initiate` - Start enrollment
* POST `/v1/enrollment/submit` - Submit factors
* POST `/v1/verification/initiate` - Start verification
* POST `/v1/verification/verify` - Verify factors
* DELETE `/v1/management/enrollment/:uuid` - Delete enrollment

***

## 💳 Payment & Billing Endpoints (13)

| Router                   | Endpoints | Base Path               |
| ------------------------ | --------- | ----------------------- |
| consumerBillingRouter.js | 3         | `/api/consumer/billing` |
| paymentRouter.js         | 3         | `/v1/payment`           |
| pspRouter.js             | 3         | `/v1/psp`               |
| unifiedBillingRouter.js  | 5         | `/api/billing`          |
| unifiedWebhookHandler.js | 2         | `/webhook`              |
| stripeWebhookHandler.js  | 1         | `/webhook/stripe`       |

**Purpose:** Payment processing, billing, webhook handling, PSP integration.

**Supported PSPs:**

* Stripe
* MercadoPago
* Square
* Adyen
* Tilopay

***

## 🔧 Other Endpoints (26)

| Router              | Endpoints | Base Path     |
| ------------------- | --------- | ------------- |
| sandboxRouter.js    | 11        | `/v1/sandbox` |
| ssoRouter.js        | 6         | `/v1/sso`     |
| sessionKeyRouter.js | 5         | `/v1/session` |
| zkProofRouter.js    | 4         | `/v1/zkproof` |

**Purpose:** Sandbox testing, SSO/federation, session key management, zero-knowledge proofs.

***

## 📋 Complete Router List (39 Routers)

| #  | Router                           | Endpoints | Status   |
| -- | -------------------------------- | --------- | -------- |
| 1  | merchantRouter.js                | 21        | ✅ Active |
| 2  | adminBillingRouter.js            | 18        | ✅ Active |
| 3  | merchantDashboardRouter.js       | 17        | ✅ Active |
| 4  | developerPortalRouter.js         | 16        | ✅ Active |
| 5  | adminManagementRouter.js         | 14        | ✅ Active |
| 6  | cryptoPaymentRouter.js           | 14        | ✅ Active |
| 7  | developerAuthRouter.js           | 13        | ✅ Active |
| 8  | adminUserRouter.js               | 13        | ✅ Active |
| 9  | regularAuthRouter.js             | 12        | ✅ Active |
| 10 | adminTeamRouter.js               | 12        | ✅ Active |
| 11 | sandboxRouter.js                 | 11        | ✅ Active |
| 12 | adminRouter.js                   | 10        | ✅ Active |
| 13 | adminUserManagementRouter.js     | 10        | ✅ Active |
| 14 | didRouter.js                     | 10        | ✅ Active |
| 15 | adminAliasRouter.js              | 8         | ✅ Active |
| 16 | adminAuditRouter.js              | 8         | ✅ Active |
| 17 | blockchainRouter.js              | 8         | ✅ Active |
| 18 | adminConfigurationRouter.js      | 7         | ✅ Active |
| 19 | enrollmentRouter.js              | 7         | ✅ Active |
| 20 | namesRouter.js                   | 7         | ✅ Active |
| 21 | adminAnalyticsRouter.js          | 6         | ✅ Active |
| 22 | adminSecurityMonitoringRouter.js | 6         | ✅ Active |
| 23 | merchantAuthRouter.js            | 6         | ✅ Active |
| 24 | merchantBillingRouter.js         | 6         | ✅ Active |
| 25 | snsRouter.js                     | 6         | ✅ Active |
| 26 | ssoRouter.js                     | 6         | ✅ Active |
| 27 | walletRouter.js                  | 6         | ✅ Active |
| 28 | managementRouter.js              | 5         | ✅ Active |
| 29 | managementDevicesRouter.js       | 5         | ✅ Active |
| 30 | sessionKeyRouter.js              | 5         | ✅ Active |
| 31 | unifiedBillingRouter.js          | 5         | ✅ Active |
| 32 | adminAuthRouter.js               | 4         | ✅ Active |
| 33 | verificationRouter.js            | 4         | ✅ Active |
| 34 | zkProofRouter.js                 | 4         | ✅ Active |
| 35 | consumerBillingRouter.js         | 3         | ✅ Active |
| 36 | paymentRouter.js                 | 3         | ✅ Active |
| 37 | pspRouter.js                     | 3         | ✅ Active |
| 38 | unifiedWebhookHandler.js         | 2         | ✅ Active |
| 39 | stripeWebhookHandler.js          | 1         | ✅ Active |

***

## 🧪 Testing

**Comprehensive test script:** `/tmp/test_all_endpoints.sh`

Run all endpoint tests:

```bash
bash /tmp/test_all_endpoints.sh
```

**Test Results:** See `API_ENDPOINT_TEST_RESULTS.md`

***

## 📊 Growth Tracking

| Date       | Total Endpoints | Change | Notes                                                   |
| ---------- | --------------- | ------ | ------------------------------------------------------- |
| 2025-12-27 | 322             | +35    | Added Regular User, Merchant, Admin auth (35 endpoints) |
| 2025-12-XX | 287             | -      | Previous count (before multi-user auth)                 |

**Update this table when endpoint count changes!**

***

## 🚀 Adding New Endpoints - Checklist

When creating new endpoints, follow this checklist:

* [ ] Create router file in `backend/routes/`
* [ ] Implement endpoint logic with proper error handling
* [ ] Add authentication/authorization middleware if needed
* [ ] Register router in `server.js`
* [ ] Add rate limiting if needed
* [ ] **Run endpoint count script and update this document**
* [ ] Add endpoint to test suite
* [ ] Document endpoint in appropriate API docs
* [ ] Update `.env.example` with any new variables
* [ ] Create database migrations if needed
* [ ] Test endpoint locally
* [ ] Commit changes with updated documentation

**Remember:** Always update the endpoint count BEFORE committing!

***

**Last Updated:** 2025-12-27\
**Next Review:** Update when new routers/endpoints are added
