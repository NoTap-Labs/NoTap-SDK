# Merchant Webhook Guide

**Webhooks** send real-time HTTP notifications to your server when events occur in NoTap. Use them to trigger automated workflows, update your database, or alert your team.

---

## Quick Setup

### 1. Add Webhook URL

**Dashboard:**
```
Settings → Webhooks → Add Webhook
```

**API:**
```bash
curl -X POST https://api.notap.io/api/merchant/settings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "webhooks": [{
      "url": "https://your-server.com/webhooks/notap",
      "events": ["verification.success", "fraud.flagged"],
      "enabled": true
    }]
  }'
```

### 2. Receive Events

NoTap sends a POST request to your URL:

```javascript
// Your webhook endpoint (Express.js example)
app.post('/webhooks/notap', (req, res) => {
  const event = req.body;

  console.log('Received event:', event.type);

  // Process the event
  switch (event.type) {
    case 'verification.success':
      handleVerificationSuccess(event.data);
      break;
    case 'fraud.flagged':
      handleFraudAlert(event.data);
      break;
  }

  // Always respond 200 to acknowledge receipt
  res.status(200).json({ received: true });
});
```

### 3. Verify Signature (Important!)

Always verify webhook signatures to ensure requests are from NoTap:

```javascript
const crypto = require('crypto');

function verifyWebhookSignature(payload, signature, secret) {
  const expectedSignature = crypto
    .createHmac('sha256', secret)
    .update(JSON.stringify(payload))
    .digest('hex');

  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expectedSignature)
  );
}

app.post('/webhooks/notap', (req, res) => {
  const signature = req.headers['x-notap-signature'];
  const webhookSecret = process.env.NOTAP_WEBHOOK_SECRET;

  if (!verifyWebhookSignature(req.body, signature, webhookSecret)) {
    return res.status(401).json({ error: 'Invalid signature' });
  }

  // Process verified event...
  res.status(200).json({ received: true });
});
```

---

## Available Events

### Verification Events

| Event | Trigger | Use Case |
|-------|---------|----------|
| `verification.initiated` | Session created | Track attempts |
| `verification.success` | User verified | Update order status |
| `verification.failed` | Verification failed | Fraud review |
| `verification.expired` | Session timed out | Cleanup |

### Fraud Events

| Event | Trigger | Use Case |
|-------|---------|----------|
| `fraud.flagged` | Suspicious activity | Alert security team |
| `fraud.blocked` | User blocked | Update CRM |
| `fraud.velocity_exceeded` | Rate limit hit | Review account |

### Quota Events

| Event | Trigger | Use Case |
|-------|---------|----------|
| `quota.warning` | 80% quota used | Plan upgrade prompt |
| `quota.exceeded` | 100% quota used | Immediate action |

### Billing Events

| Event | Trigger | Use Case |
|-------|---------|----------|
| `billing.payment_success` | Payment received | Update subscription |
| `billing.payment_failed` | Payment failed | Dunning workflow |
| `billing.subscription_changed` | Plan change | Update features |

---

## Event Payload Format

### Standard Structure

```json
{
  "id": "evt_1234567890",
  "type": "verification.success",
  "created_at": "2026-01-31T12:00:00Z",
  "merchant_id": "merchant-uuid",
  "data": {
    // Event-specific data
  },
  "metadata": {
    "api_version": "2026-01-31",
    "idempotency_key": "unique-key-for-deduplication"
  }
}
```

### verification.success

```json
{
  "id": "evt_abc123",
  "type": "verification.success",
  "created_at": "2026-01-31T12:00:00Z",
  "merchant_id": "merchant-uuid",
  "data": {
    "session_id": "sess_xyz789",
    "user_uuid": "user-uuid-123",
    "user_alias": "tiger-4829",
    "factors_used": ["PIN", "PATTERN"],
    "verification_time_ms": 2340,
    "transaction_amount": 49.99,
    "risk_score": 12,
    "ip_address": "203.0.113.x",  // Anonymized
    "device_fingerprint": "fp_hashed_value"
  }
}
```

### verification.failed

```json
{
  "id": "evt_def456",
  "type": "verification.failed",
  "created_at": "2026-01-31T12:01:00Z",
  "merchant_id": "merchant-uuid",
  "data": {
    "session_id": "sess_xyz789",
    "user_uuid": "user-uuid-123",
    "failure_reason": "FACTOR_MISMATCH",
    "factor_failed": "PIN",
    "attempts_remaining": 2,
    "risk_score": 45
  }
}
```

### fraud.flagged

```json
{
  "id": "evt_ghi789",
  "type": "fraud.flagged",
  "created_at": "2026-01-31T12:02:00Z",
  "merchant_id": "merchant-uuid",
  "data": {
    "user_uuid": "user-uuid-456",
    "flag_reason": "VELOCITY_ABUSE",
    "risk_score": 85,
    "recent_attempts": 15,
    "time_window_minutes": 5,
    "recommended_action": "BLOCK"
  }
}
```

### quota.warning

```json
{
  "id": "evt_jkl012",
  "type": "quota.warning",
  "created_at": "2026-01-31T12:03:00Z",
  "merchant_id": "merchant-uuid",
  "data": {
    "quota_used": 8500,
    "quota_limit": 10000,
    "percent_used": 85,
    "days_remaining": 12,
    "projected_overage": 2000,
    "upgrade_url": "https://dashboard.notap.io/billing/upgrade"
  }
}
```

---

## Implementation Best Practices

### 1. Respond Quickly (< 5 seconds)

NoTap expects a response within 5 seconds. Process async:

```javascript
app.post('/webhooks/notap', async (req, res) => {
  // Acknowledge immediately
  res.status(200).json({ received: true });

  // Process asynchronously
  setImmediate(() => {
    processEvent(req.body);
  });
});
```

### 2. Handle Duplicates (Idempotency)

Events may be delivered multiple times. Use `idempotency_key`:

```javascript
const processedEvents = new Set();

function processEvent(event) {
  const key = event.metadata.idempotency_key;

  if (processedEvents.has(key)) {
    console.log('Duplicate event, skipping:', key);
    return;
  }

  processedEvents.add(key);
  // Process event...
}
```

### 3. Store Raw Events

Log all events for debugging and compliance:

```javascript
async function processEvent(event) {
  // Store raw event
  await db.webhookEvents.insert({
    event_id: event.id,
    event_type: event.type,
    payload: JSON.stringify(event),
    received_at: new Date()
  });

  // Then process
  // ...
}
```

### 4. Handle Retries

NoTap retries failed deliveries with exponential backoff:

| Attempt | Delay |
|---------|-------|
| 1 | Immediate |
| 2 | 1 minute |
| 3 | 5 minutes |
| 4 | 30 minutes |
| 5 | 2 hours |

After 5 failures, the webhook is disabled. Check Dashboard for status.

### 5. Use HTTPS

Webhooks must be delivered to HTTPS endpoints. No HTTP allowed.

---

## Webhook Configuration

### Dashboard Settings

```
Settings → Webhooks
```

| Setting | Description | Default |
|---------|-------------|---------|
| **URL** | Your endpoint (HTTPS required) | - |
| **Events** | Which events to send | All |
| **Enabled** | Active/inactive toggle | true |
| **Secret** | HMAC signing key | Auto-generated |

### Per-Webhook Settings

You can have multiple webhooks with different configurations:

```javascript
// Example: Separate endpoints for different event types
{
  "webhooks": [
    {
      "url": "https://api.yourapp.com/webhooks/verification",
      "events": ["verification.success", "verification.failed"],
      "enabled": true
    },
    {
      "url": "https://security.yourapp.com/webhooks/fraud",
      "events": ["fraud.flagged", "fraud.blocked"],
      "enabled": true
    },
    {
      "url": "https://billing.yourapp.com/webhooks/quota",
      "events": ["quota.warning", "quota.exceeded"],
      "enabled": true
    }
  ]
}
```

---

## Testing Webhooks

### Test Endpoint

Send a test event from Dashboard:

```
Settings → Webhooks → [Your Webhook] → Send Test
```

### Local Development

Use a tunnel service to receive webhooks locally:

```bash
# Using ngrok
ngrok http 3000

# Your webhook URL becomes:
# https://abc123.ngrok.io/webhooks/notap
```

### Webhook Simulator

Test your handler with curl:

```bash
curl -X POST https://your-server.com/webhooks/notap \
  -H "Content-Type: application/json" \
  -H "X-NoTap-Signature: test-signature" \
  -d '{
    "id": "evt_test123",
    "type": "verification.success",
    "created_at": "2026-01-31T12:00:00Z",
    "merchant_id": "test-merchant",
    "data": {
      "session_id": "sess_test",
      "user_uuid": "test-user",
      "factors_used": ["PIN"],
      "risk_score": 10
    },
    "metadata": {
      "api_version": "2026-01-31",
      "idempotency_key": "test-key-123"
    }
  }'
```

---

## Security

### Signature Verification

Every webhook includes `X-NoTap-Signature` header:

```
X-NoTap-Signature: sha256=5d41402abc4b2a76b9719d911017c592
```

Verify with your webhook secret:

```python
# Python example
import hmac
import hashlib

def verify_signature(payload: bytes, signature: str, secret: str) -> bool:
    expected = hmac.new(
        secret.encode(),
        payload,
        hashlib.sha256
    ).hexdigest()

    return hmac.compare_digest(f"sha256={expected}", signature)
```

### IP Allowlist

NoTap webhooks originate from these IPs:

```
52.1.2.3       # US-East
52.4.5.6       # US-West
18.7.8.9       # EU-West
```

Add to your firewall allowlist for extra security.

### Rotate Secrets

Regenerate your webhook secret periodically:

```
Dashboard → Settings → Webhooks → Regenerate Secret
```

Update your server with the new secret before it takes effect (24h grace period).

---

## Troubleshooting

### Webhooks Not Received

1. **Check URL is HTTPS** - HTTP not allowed
2. **Check firewall** - Allow NoTap IPs
3. **Check endpoint** - Must return 2xx status
4. **Check enabled** - Webhook may be disabled after failures

### Signature Verification Failing

1. **Check secret** - Copy exactly from Dashboard
2. **Check payload** - Use raw body, not parsed JSON
3. **Check encoding** - UTF-8 required

### Duplicate Events

- Use `idempotency_key` for deduplication
- Store processed event IDs
- Network issues may cause retries

### Events Delayed

- Check Dashboard → Webhooks → Delivery History
- Retries cause delays (up to 2 hours)
- High volume may cause queuing

---

## Event History

View delivery attempts in Dashboard:

```
Settings → Webhooks → [Webhook] → History
```

Shows:
- Event ID
- Type
- Timestamp
- HTTP status
- Response body
- Retry count

---

## Complete Example (Express.js)

```javascript
const express = require('express');
const crypto = require('crypto');
const app = express();

// Use raw body for signature verification
app.use('/webhooks/notap', express.raw({ type: 'application/json' }));

const WEBHOOK_SECRET = process.env.NOTAP_WEBHOOK_SECRET;

// Signature verification middleware
function verifySignature(req, res, next) {
  const signature = req.headers['x-notap-signature'];

  if (!signature) {
    return res.status(401).json({ error: 'Missing signature' });
  }

  const expectedSignature = 'sha256=' + crypto
    .createHmac('sha256', WEBHOOK_SECRET)
    .update(req.body)
    .digest('hex');

  if (!crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expectedSignature)
  )) {
    return res.status(401).json({ error: 'Invalid signature' });
  }

  req.body = JSON.parse(req.body);
  next();
}

// Webhook endpoint
app.post('/webhooks/notap', verifySignature, async (req, res) => {
  const event = req.body;

  // Log event
  console.log(`[NoTap Webhook] ${event.type}: ${event.id}`);

  // Acknowledge immediately
  res.status(200).json({ received: true });

  // Process async
  try {
    switch (event.type) {
      case 'verification.success':
        await handleVerificationSuccess(event.data);
        break;

      case 'verification.failed':
        await handleVerificationFailed(event.data);
        break;

      case 'fraud.flagged':
        await handleFraudAlert(event.data);
        break;

      case 'quota.warning':
        await handleQuotaWarning(event.data);
        break;

      default:
        console.log(`Unhandled event type: ${event.type}`);
    }
  } catch (error) {
    console.error('Webhook processing error:', error);
    // Don't throw - we already sent 200
  }
});

async function handleVerificationSuccess(data) {
  // Update your order/transaction status
  await db.orders.update({
    where: { session_id: data.session_id },
    data: { status: 'verified', verified_at: new Date() }
  });
}

async function handleVerificationFailed(data) {
  // Log failed attempt, maybe alert if repeated
  if (data.attempts_remaining === 0) {
    await alertSecurityTeam(data.user_uuid, 'All verification attempts failed');
  }
}

async function handleFraudAlert(data) {
  // Alert security team
  await slack.send({
    channel: '#security-alerts',
    text: `🚨 Fraud flagged: User ${data.user_uuid} - ${data.flag_reason}`
  });
}

async function handleQuotaWarning(data) {
  // Alert billing team
  await email.send({
    to: 'billing@yourcompany.com',
    subject: 'NoTap Quota Warning',
    body: `${data.percent_used}% of quota used. ${data.days_remaining} days remaining.`
  });
}

app.listen(3000, () => console.log('Webhook server running on port 3000'));
```

---

## Support

- **Webhook Issues:** Check Dashboard → Webhooks → History
- **Integration Help:** support@notap.io
- **API Status:** [status.notap.io](https://status.notap.io)
