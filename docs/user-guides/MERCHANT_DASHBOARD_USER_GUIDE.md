# Merchant Dashboard User Guide

**The NoTap Merchant Dashboard** provides real-time analytics, fraud detection, customer management, and reporting tools for your business.

---

## Accessing the Dashboard

**URL:** [dashboard.notap.io](https://dashboard.notap.io)

**Login:** Use your merchant email and password, or OAuth (Google/GitHub).

**API Access:** All dashboard data is also available via API with your merchant JWT token.

---

## Dashboard Overview

### Main Sections

| Section | Purpose | Key Metrics |
|---------|---------|-------------|
| **Analytics** | Performance monitoring | Verifications, success rate, revenue |
| **Fraud Detection** | Security tools | Flagged transactions, risk scores |
| **Customers** | User management | Customer profiles, history |
| **Reports** | Business intelligence | PDF/CSV exports, scheduling |
| **Settings** | Configuration | API keys, webhooks, branding |
| **Billing** | Subscription management | Usage, invoices, upgrades |

---

## Analytics

### Overview Dashboard

The main analytics view shows key metrics at a glance:

| Metric | Description | Good Target |
|--------|-------------|-------------|
| **Total Verifications** | Successful auths this period | Growing month-over-month |
| **Success Rate** | % of attempts that succeeded | > 90% |
| **Avg Verification Time** | Seconds to complete | < 3 seconds |
| **Active Users** | Unique users this period | Growing |
| **Revenue Protected** | Transaction value verified | Your business metric |

### Time Range Filters

- **24h** - Last 24 hours (default)
- **7d** - Last 7 days
- **30d** - Last 30 days
- **Custom** - Select date range

### Transaction Analytics

View detailed breakdowns:

```
Dashboard → Analytics → Transactions
```

**Available Views:**
- **By Hour** - See traffic patterns
- **By Day** - Daily trends
- **By Factor** - Which factors used most
- **By Result** - Success vs failure breakdown

### Success Rate Analysis

Understand why verifications fail:

| Failure Reason | Description | Action |
|----------------|-------------|--------|
| **Factor Mismatch** | User entered wrong data | Expected - fraud blocked |
| **Session Expired** | Too slow to complete | Consider shorter flows |
| **Network Error** | Connection issues | Check user connectivity |
| **Quota Exceeded** | Rate limited | Upgrade plan |

### Peak Usage

Identify your busiest times:

```json
// API Response: GET /api/merchant/analytics/peak-usage
{
  "peakHour": 14,
  "peakDay": "Wednesday",
  "peakTransactions": 342,
  "recommendation": "Consider load balancing for 2-4 PM on weekdays"
}
```

---

## Fraud Detection

### Flagged Transactions

View suspicious activity:

```
Dashboard → Fraud → Flagged
```

**Flag Reasons:**
- **Velocity Abuse** - Too many attempts in short time
- **Geographic Anomaly** - Login from unusual location
- **Device Anomaly** - New device with high-risk behavior
- **Pattern Mismatch** - Behavioral factors don't match enrollment

**Actions:**
| Action | Effect | Use When |
|--------|--------|----------|
| **Dismiss** | Mark as false positive | Legitimate user flagged |
| **Block** | Add to blocklist | Confirmed fraud |
| **Investigate** | Add to review queue | Need more info |

### Risk Scores

Each user has a risk score (0-100):

| Score | Risk Level | Recommended Action |
|-------|------------|-------------------|
| 0-20 | Low | Normal verification |
| 21-50 | Medium | Request additional factor |
| 51-80 | High | Manual review |
| 81-100 | Critical | Block transaction |

### Velocity Checks

Configure rate limits per user:

```
Dashboard → Fraud → Velocity Settings
```

**Default Limits:**
- Max 5 verifications per minute per user
- Max 50 verifications per hour per user
- Max 200 verifications per day per user

### Blocklist Management

**Add to Blocklist:**
```
Dashboard → Fraud → Blocklist → Add User
```

Enter user UUID to block. Blocked users will receive 403 Forbidden on all verification attempts.

**Remove from Blocklist:**
```
Dashboard → Fraud → Blocklist → [User] → Remove
```

---

## Customer Management

### Customer List

View all users who have verified through your integration:

```
Dashboard → Customers
```

**Available Data:**
- User UUID
- First verification date
- Last verification date
- Total verifications
- Success rate
- Risk score

**Search & Filter:**
- Search by UUID or alias
- Filter by risk level
- Filter by date range
- Sort by activity, risk, date

### Customer Details

Click a customer to see:

**Profile:**
- UUID
- Alias (if set)
- Enrolled factors
- Device info (anonymized)

**Verification History:**
- Date/time of each verification
- Factors used
- Result (success/failure)
- Transaction amount (if provided)

### Customer Actions

| Action | Description |
|--------|-------------|
| **View History** | Full verification timeline |
| **Export Data** | Download customer data (GDPR) |
| **Block** | Add to blocklist |
| **Unblock** | Remove from blocklist |

---

## Reports

### Generate Reports

```
Dashboard → Reports → Generate
```

**Report Types:**
- **Verification Summary** - Overview of all verifications
- **Fraud Analysis** - Detailed fraud metrics
- **Customer Activity** - User-by-user breakdown
- **Revenue Report** - Transaction values verified

**Export Formats:**
- **PDF** - For presentations, compliance
- **CSV** - For spreadsheets, analysis
- **XLSX** - For Excel with formatting

### Schedule Reports

Automate regular reports:

```
Dashboard → Reports → Schedule New
```

**Frequency Options:**
- Daily (sent 6 AM)
- Weekly (sent Monday 6 AM)
- Monthly (sent 1st of month)

**Delivery:**
- Email (to your merchant email)
- Webhook (POST to your endpoint)

### Compliance Reports

For SOC 2, PCI DSS, GDPR compliance:

```
Dashboard → Reports → Compliance
```

Generates audit-ready documentation including:
- Access logs
- Data processing records
- Security incident summary

---

## Settings

### API Keys

```
Dashboard → Settings → API Keys
```

**Key Types:**
| Type | Prefix | Capabilities |
|------|--------|--------------|
| **Test** | `mk_test_` | Sandbox only |
| **Live** | `mk_live_` | Production |
| **Read-Only** | `mk_readonly_` | Analytics only |

**Actions:**
- **Create Key** - Generate new API key
- **Rename Key** - Change display name
- **Revoke Key** - Immediately disable (cannot undo)
- **View Usage** - See key's request history

### Webhooks

```
Dashboard → Settings → Webhooks
```

Configure real-time notifications to your server.

**Events Available:**
- `verification.success` - Verification completed
- `verification.failed` - Verification failed
- `fraud.flagged` - Suspicious activity detected
- `quota.warning` - Approaching quota limit (80%)
- `quota.exceeded` - Quota exceeded

**Setup:**
1. Add webhook URL (must be HTTPS)
2. Select events to receive
3. Save and test
4. Verify signature (see Webhook Guide)

### Branding (Pro+ Only)

```
Dashboard → Settings → Branding
```

Customize the verification experience:

- **Logo** - Your logo on verification screens
- **Colors** - Primary/secondary brand colors
- **Custom Domain** - verify.yourdomain.com (Enterprise)

### Notifications

```
Dashboard → Settings → Notifications
```

Configure email alerts:

| Alert | Default | Description |
|-------|---------|-------------|
| **Quota Warning** | 80% | Approaching limit |
| **Quota Exceeded** | 100% | Limit reached |
| **Fraud Alert** | Enabled | Critical risk detected |
| **Weekly Summary** | Enabled | Performance digest |

---

## Billing

### Current Plan

```
Dashboard → Billing
```

View your subscription:
- Current tier
- Quota used / limit
- Renewal date
- Payment method

### Usage Tracking

Monitor your consumption:

| Metric | Description |
|--------|-------------|
| **Verifications Used** | Count this billing period |
| **Quota Remaining** | Verifications until limit |
| **Overage** | Extra verifications (if any) |
| **Estimated Bill** | Projected cost this period |

### Invoices

```
Dashboard → Billing → Invoices
```

Download past invoices (PDF) for accounting.

### Upgrade/Downgrade

```
Dashboard → Billing → Change Plan
```

- **Upgrade** - Immediate effect, prorated charge
- **Downgrade** - Effective next billing cycle

---

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `G` then `A` | Go to Analytics |
| `G` then `F` | Go to Fraud |
| `G` then `C` | Go to Customers |
| `G` then `R` | Go to Reports |
| `G` then `S` | Go to Settings |
| `/` | Search |
| `?` | Show all shortcuts |

---

## Mobile Access

The dashboard is fully responsive. Access from any device:

- **Tablet** - Full functionality
- **Phone** - Key metrics and alerts
- **Native Apps** - Coming soon (iOS/Android)

---

## API Access

All dashboard data is available via API:

```bash
# Analytics Overview
curl https://api.notap.io/api/merchant/analytics/overview \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Customer List
curl https://api.notap.io/api/merchant/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Generate Report
curl https://api.notap.io/api/merchant/reports/generate?type=summary&format=csv \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Full API Reference:** [backend/docs/MERCHANT_DASHBOARD_API.md](../../backend/docs/MERCHANT_DASHBOARD_API.md)

---

## Troubleshooting

### "No Data Available"

- **New Account** - Data appears after first verification
- **Wrong Time Range** - Try "30d" instead of "24h"
- **Sandbox vs Production** - Check correct environment

### "Session Expired"

- Your JWT token expired (7 days)
- Solution: Log out and log back in

### "Access Denied"

- Verification status may be "pending"
- Contact support if status is "rejected"

### Slow Dashboard

- Try a different browser
- Clear cache and cookies
- Check internet connection

---

## Support

Need help? Contact us:

- **Documentation:** You're reading it!
- **Email:** support@notap.io
- **Chat:** Dashboard → Help (Pro+ plans)
- **Phone:** Your account manager (Business+ plans)

---

**Next:** [Integration Quickstart](../03-developer-guides/MERCHANT_INTEGRATION_QUICKSTART.md) to add NoTap to your app.
