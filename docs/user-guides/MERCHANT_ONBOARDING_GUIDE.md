# Merchant Onboarding Guide

**Welcome to NoTap!** This guide walks you through registering your business and getting started with NoTap authentication.

---

## Quick Start (5 Minutes)

1. **Register** - Create your merchant account
2. **Verify Email** - Confirm your email address
3. **Complete Profile** - Add business details
4. **Get API Keys** - Access your sandbox credentials
5. **Test Integration** - Make your first verification

---

## Step 1: Register Your Account

### Web Registration

Visit [dashboard.notap.io/register](https://dashboard.notap.io/register) and provide:

- **Email Address** - Your business email
- **Password** - Minimum 8 characters, mix of letters/numbers recommended
- **Full Name** - Your name (account owner)
- **Business Name** - Legal business name
- **Business Type** - Select from: Restaurant, Retail, Services, E-commerce, Healthcare, Education, Nonprofit, Other

### API Registration

```bash
curl -X POST https://api.notap.io/v1/auth/merchant/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@yourbusiness.com",
    "password": "SecurePassword123",
    "fullName": "Jane Smith",
    "businessName": "Acme E-commerce Inc.",
    "businessType": "ecommerce"
  }'
```

**Response:**
```json
{
  "success": true,
  "merchant": {
    "id": "merchant-uuid-here",
    "email": "owner@yourbusiness.com",
    "businessName": "Acme E-commerce Inc.",
    "verificationStatus": "pending"
  },
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

---

## Step 2: Verify Your Email

Check your inbox for a verification email from `noreply@notap.io`. Click the verification link within **24 hours**.

**Didn't receive it?**
- Check spam/junk folders
- Request resend: `POST /v1/auth/merchant/resend-verification`
- Contact support@notap.io

---

## Step 3: Complete Business Profile

After email verification, complete your business profile:

### Required Information

| Field | Description | Example |
|-------|-------------|---------|
| Business Phone | Contact number | +1-555-123-4567 |
| Business Address | Physical address | 123 Main St, Suite 100 |
| City | City | San Francisco |
| State/Province | State or province | CA |
| Postal Code | ZIP or postal code | 94105 |
| Country | Country code | US |
| Business Website | Your website URL | https://acme.com |

### Optional (For Verification)

| Field | Description |
|-------|-------------|
| Tax ID | EIN (US), VAT (EU), GST (AU) |
| Owner Phone | Personal contact for verification |
| Business Description | Brief description of your business |
| Logo URL | Link to your business logo |

### Update Profile API

```bash
curl -X PUT https://api.notap.io/v1/auth/merchant/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "businessPhone": "+1-555-123-4567",
    "businessAddressLine1": "123 Main St",
    "businessCity": "San Francisco",
    "businessState": "CA",
    "businessPostalCode": "94105",
    "businessCountry": "US",
    "businessWebsite": "https://acme.com",
    "taxId": "12-3456789",
    "taxIdType": "EIN"
  }'
```

---

## Step 4: Business Verification

NoTap verifies businesses to prevent fraud and ensure trust.

### Verification Statuses

| Status | Description | Access |
|--------|-------------|--------|
| **pending** | Awaiting review | Sandbox only |
| **in_review** | Being reviewed | Sandbox only |
| **verified** | Approved | Production + Sandbox |
| **rejected** | Denied (see notes) | None |

### Verification Process

1. **Automatic Review** - Basic checks (email domain, business website)
2. **Manual Review** - If flagged, our team reviews within 24-48 hours
3. **Approval** - Receive email confirmation

### Speed Up Verification

- Provide tax ID (EIN, VAT)
- Use business email (not gmail/yahoo)
- Have a live business website
- Complete all profile fields

---

## Step 5: Get Your API Keys

Once registered, you automatically receive **Sandbox** access.

### API Key Types

| Key Prefix | Environment | Use Case |
|------------|-------------|----------|
| `mk_test_` | Sandbox | Development, testing |
| `mk_live_` | Production | Real transactions |
| `mk_readonly_` | Both | Analytics only |

### Retrieve Your Keys

**Dashboard:** Settings → API Keys → View Keys

**API:**
```bash
curl https://api.notap.io/v1/merchant/billing/status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "subscription": {
    "plan_code": "merchant_sandbox",
    "quota_limit": 500,
    "quota_used": 0,
    "status": "active"
  },
  "apiKeys": [
    {
      "id": "key-1",
      "name": "Default Sandbox Key",
      "keyPrefix": "mk_test_****",
      "scopes": ["verification:read", "verification:write"]
    }
  ]
}
```

---

## Step 6: Sandbox Testing

### What's Included

- **500 verifications/month** (free)
- **Test API keys** (mk_test_* prefix)
- **Mock data** for testing
- **No charges** - sandbox never bills

### Test Users

Use these pre-configured test accounts:

| UUID | PIN | Description |
|------|-----|-------------|
| `00000000-0000-4000-a000-000000000002` | 1234 | Primary test user |
| `test-user-001` | 1111 | Secondary test user |

### Your First Verification

```bash
# 1. Initiate verification
curl -X POST https://api.notap.io/v1/verification/initiate \
  -H "Authorization: Bearer mk_test_YOUR_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "user_uuid": "00000000-0000-4000-a000-000000000002",
    "transaction_amount": 49.99,
    "risk_level": "LOW"
  }'

# 2. Submit PIN factor
curl -X POST https://api.notap.io/v1/verification/submit-factor \
  -H "Authorization: Bearer mk_test_YOUR_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "SESSION_ID_FROM_STEP_1",
    "factor_type": "PIN",
    "factor_data": {
      "pin_digest": "HASHED_PIN_1234"
    }
  }'
```

---

## Step 7: Upgrade to Production

When you're ready for real transactions:

### Upgrade Path

1. **Dashboard:** Billing → Upgrade Plan
2. **Select Tier:** Starter ($49/mo), Pro ($299/mo), Business ($1,299/mo)
3. **Payment:** Enter credit card (via Stripe)
4. **Activate:** Production keys enabled immediately

### Pricing Overview

| Tier | Monthly | Verifications | Overage |
|------|---------|---------------|---------|
| **Sandbox** | $0 | 500 | N/A (hard cap) |
| **Starter** | $49 | 10,000 | $0.004/auth |
| **Pro** | $299 | 100,000 | $0.002/auth |
| **Business** | $1,299 | 1,000,000 | $0.001/auth |
| **Enterprise** | Custom | Unlimited | Negotiated |

**Note:** You pay for successful verifications only. Failed attempts, enrollments, and blocked fraud are free.

---

## Account Security

### Password Reset

If you forget your password:

1. Visit [dashboard.notap.io/forgot-password](https://dashboard.notap.io/forgot-password)
2. Enter your email
3. Click reset link in email (valid 1 hour)
4. Set new password

**Rate Limit:** 3 password reset requests per hour (security protection).

### Two-Factor Authentication

Enable 2FA in Settings → Security:
- TOTP apps (Google Authenticator, Authy)
- SMS backup (optional)

### Session Management

- JWT tokens expire after **7 days**
- Logout invalidates current session
- View active sessions in Settings → Security

---

## OAuth Login (Optional)

Register/login with Google or GitHub:

```bash
# Google OAuth
POST /v1/auth/merchant/oauth/callback
{
  "provider": "google",
  "oauthId": "google-user-id",
  "email": "owner@yourbusiness.com",
  "fullName": "Jane Smith"
}
```

**Supported Providers:**
- Google (recommended for business)
- GitHub (for developer accounts)

---

## Next Steps

1. **[Merchant Dashboard Guide](MERCHANT_DASHBOARD_USER_GUIDE.md)** - Learn to use analytics, fraud tools
2. **[Integration Quickstart](../03-developer-guides/MERCHANT_INTEGRATION_QUICKSTART.md)** - Code examples
3. **[Webhook Setup](../03-developer-guides/MERCHANT_WEBHOOK_GUIDE.md)** - Real-time notifications
4. **[Billing Guide](BILLING_USER_GUIDE.md)** - Manage subscriptions, invoices

---

## Support

| Channel | Response Time | Use For |
|---------|---------------|---------|
| **Docs** | Instant | Self-service, guides |
| **Email** | 24 hours | support@notap.io |
| **Chat** | 4 hours (Pro+) | Dashboard → Help |
| **Phone** | Immediate (Business+) | Dedicated account manager |

---

**Welcome aboard!** You're now ready to add secure, device-free authentication to your business.
