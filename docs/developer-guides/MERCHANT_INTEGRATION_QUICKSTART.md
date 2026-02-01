# Merchant Integration Quickstart

**Add NoTap authentication to your application in 15 minutes.**

---

## Prerequisites

- NoTap merchant account ([Register here](https://dashboard.notap.io/register))
- API key (sandbox: `mk_test_*`, production: `mk_live_*`)
- HTTPS endpoint for webhooks (optional)

---

## Integration Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Your App  │────▶│  NoTap API  │────▶│  User Auth  │
│  (Backend)  │◀────│             │◀────│  (Factors)  │
└─────────────┘     └─────────────┘     └─────────────┘
      │                    │
      │   1. Initiate      │
      │─────────────────▶  │
      │                    │
      │   2. Session ID    │
      │◀─────────────────  │
      │                    │
      │   3. Submit Factor │
      │─────────────────▶  │
      │                    │
      │   4. Result        │
      │◀─────────────────  │
```

---

## Quick Start (Node.js)

### Install SDK (Optional)

```bash
npm install @notap/sdk
```

Or use raw HTTP requests (examples below).

### Step 1: Initiate Verification

When a user needs to authenticate (e.g., checkout, login):

```javascript
const axios = require('axios');

const NOTAP_API = 'https://api.notap.io';
const API_KEY = 'mk_test_your_api_key'; // Use mk_live_* in production

async function initiateVerification(userUuid, amount) {
  const response = await axios.post(
    `${NOTAP_API}/v1/verification/initiate`,
    {
      user_uuid: userUuid,        // User's NoTap UUID or alias
      transaction_amount: amount, // Optional: for risk assessment
      risk_level: 'LOW'           // LOW, MEDIUM, HIGH
    },
    {
      headers: {
        'Authorization': `Bearer ${API_KEY}`,
        'Content-Type': 'application/json'
      }
    }
  );

  return response.data;
  // Returns: { session_id, required_factors: ['PIN', 'PATTERN'], expires_at }
}
```

### Step 2: Display Factor Challenge

Based on `required_factors`, show the appropriate UI:

```javascript
// Example: PIN factor
function renderPinChallenge(sessionId) {
  return `
    <div class="notap-challenge">
      <h2>Enter your PIN</h2>
      <input type="password" id="pin" maxlength="6" />
      <button onclick="submitPin('${sessionId}')">Verify</button>
    </div>
  `;
}
```

### Step 3: Submit Factor

When user completes a factor:

```javascript
async function submitFactor(sessionId, factorType, factorData) {
  const response = await axios.post(
    `${NOTAP_API}/v1/verification/submit-factor`,
    {
      session_id: sessionId,
      factor_type: factorType,  // 'PIN', 'PATTERN', 'EMOJI', etc.
      factor_data: factorData   // Factor-specific data
    },
    {
      headers: {
        'Authorization': `Bearer ${API_KEY}`,
        'Content-Type': 'application/json'
      }
    }
  );

  return response.data;
  // Returns: { success: true, verified: true } or { success: true, next_factor: 'PATTERN' }
}
```

### Step 4: Handle Result

```javascript
async function verifyUser(userUuid, pin, pattern) {
  // 1. Start verification
  const session = await initiateVerification(userUuid, 99.99);

  // 2. Submit required factors
  for (const factor of session.required_factors) {
    let factorData;

    if (factor === 'PIN') {
      factorData = { pin_digest: hashPin(pin) };
    } else if (factor === 'PATTERN') {
      factorData = { pattern_digest: hashPattern(pattern) };
    }

    const result = await submitFactor(session.session_id, factor, factorData);

    if (result.verified) {
      return { success: true, message: 'User verified!' };
    }

    if (result.error) {
      return { success: false, error: result.error };
    }
  }
}
```

---

## Quick Start (Python)

```python
import requests
import hashlib

NOTAP_API = 'https://api.notap.io'
API_KEY = 'mk_test_your_api_key'

def initiate_verification(user_uuid: str, amount: float) -> dict:
    response = requests.post(
        f'{NOTAP_API}/v1/verification/initiate',
        json={
            'user_uuid': user_uuid,
            'transaction_amount': amount,
            'risk_level': 'LOW'
        },
        headers={
            'Authorization': f'Bearer {API_KEY}',
            'Content-Type': 'application/json'
        }
    )
    return response.json()

def submit_factor(session_id: str, factor_type: str, factor_data: dict) -> dict:
    response = requests.post(
        f'{NOTAP_API}/v1/verification/submit-factor',
        json={
            'session_id': session_id,
            'factor_type': factor_type,
            'factor_data': factor_data
        },
        headers={
            'Authorization': f'Bearer {API_KEY}',
            'Content-Type': 'application/json'
        }
    )
    return response.json()

# Usage
session = initiate_verification('user-uuid-123', 49.99)
result = submit_factor(session['session_id'], 'PIN', {'pin_digest': 'hashed_pin'})
print(f"Verified: {result.get('verified', False)}")
```

---

## Quick Start (cURL)

```bash
# 1. Initiate verification
curl -X POST https://api.notap.io/v1/verification/initiate \
  -H "Authorization: Bearer mk_test_your_api_key" \
  -H "Content-Type: application/json" \
  -d '{
    "user_uuid": "00000000-0000-4000-a000-000000000002",
    "transaction_amount": 49.99,
    "risk_level": "LOW"
  }'

# Response:
# {
#   "success": true,
#   "session_id": "sess_abc123",
#   "required_factors": ["PIN", "PATTERN"],
#   "expires_at": "2026-01-31T12:00:00Z"
# }

# 2. Submit PIN factor
curl -X POST https://api.notap.io/v1/verification/submit-factor \
  -H "Authorization: Bearer mk_test_your_api_key" \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "sess_abc123",
    "factor_type": "PIN",
    "factor_data": {
      "pin_digest": "sha256_hash_of_pin"
    }
  }'

# 3. Submit PATTERN factor (if required)
curl -X POST https://api.notap.io/v1/verification/submit-factor \
  -H "Authorization: Bearer mk_test_your_api_key" \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "sess_abc123",
    "factor_type": "PATTERN",
    "factor_data": {
      "pattern_digest": "sha256_hash_of_pattern"
    }
  }'
```

---

## Factor Data Formats

### PIN (4-6 digits)

```javascript
const factorData = {
  pin_digest: sha256(pin)  // SHA-256 hash of PIN string
};
```

### Pattern (3x3 or 4x4 grid)

```javascript
const factorData = {
  pattern_digest: sha256(pattern.join(',')),  // e.g., "0,1,2,5,8,7,6"
  grid_size: 3  // 3 for 3x3, 4 for 4x4
};
```

### Emoji (sequence of emoji IDs)

```javascript
const factorData = {
  emoji_digest: sha256(emojiIds.join(',')),  // e.g., "smile,heart,star,moon"
  count: 4
};
```

### Color (sequence of colors)

```javascript
const factorData = {
  color_digest: sha256(colors.join(',')),  // e.g., "red,blue,green,yellow"
  count: 4
};
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | Success | Continue flow |
| 400 | Invalid request | Check request body |
| 401 | Unauthorized | Check API key |
| 403 | Forbidden | User blocked or quota exceeded |
| 404 | Not found | Invalid session/user |
| 429 | Rate limited | Slow down requests |
| 500 | Server error | Retry with backoff |

### Error Response Format

```json
{
  "success": false,
  "error": "FACTOR_MISMATCH",
  "message": "PIN does not match enrollment",
  "attempts_remaining": 2
}
```

### Common Errors

| Error Code | Description | Solution |
|------------|-------------|----------|
| `FACTOR_MISMATCH` | Wrong factor data | User entered incorrect PIN/pattern |
| `SESSION_EXPIRED` | Session timed out | Start new verification |
| `USER_NOT_FOUND` | Unknown user UUID | User needs to enroll first |
| `USER_BLOCKED` | User on blocklist | Contact support |
| `QUOTA_EXCEEDED` | Out of verifications | Upgrade plan |

---

## Sandbox Testing

### Test Users

| UUID | PIN | Pattern | Description |
|------|-----|---------|-------------|
| `00000000-0000-4000-a000-000000000002` | 1234 | 0,1,2,5,8 | Primary test user |
| `test-user-001` | 1111 | 0,4,8 | Secondary test |

### Test Scenarios

```javascript
// Success scenario
const successUser = '00000000-0000-4000-a000-000000000002';
const successPin = sha256('1234');

// Failure scenario (wrong PIN)
const failPin = sha256('9999');  // Will return FACTOR_MISMATCH

// Blocked user scenario
const blockedUser = 'blocked-test-user';  // Returns USER_BLOCKED
```

---

## Production Checklist

Before going live:

- [ ] Replace `mk_test_*` with `mk_live_*` API key
- [ ] Update API URL if using regional endpoint
- [ ] Implement proper error handling
- [ ] Set up webhooks for async notifications
- [ ] Test all factor types your users might have
- [ ] Add retry logic with exponential backoff
- [ ] Implement logging for debugging
- [ ] Review rate limits for your plan

---

## SDK Libraries

### Official SDKs

| Language | Package | Install |
|----------|---------|---------|
| **Node.js** | `@notap/sdk` | `npm install @notap/sdk` |
| **Python** | `notap-sdk` | `pip install notap-sdk` |
| **Java** | `io.notap:sdk` | Maven/Gradle |
| **Go** | `github.com/notap/sdk-go` | `go get` |

### Community SDKs

- Ruby: `gem install notap`
- PHP: `composer require notap/sdk`
- .NET: `dotnet add package NoTap.SDK`

---

## Next Steps

1. **[Webhook Integration](MERCHANT_WEBHOOK_GUIDE.md)** - Real-time event notifications
2. **[API Reference](../../backend/docs/MERCHANT_DASHBOARD_API.md)** - Full endpoint documentation
3. **[Dashboard Guide](../02-user-guides/MERCHANT_DASHBOARD_USER_GUIDE.md)** - Monitor your integration

---

## Support

- **Sandbox Issues:** Check API key prefix (`mk_test_`)
- **Integration Help:** support@notap.io
- **API Status:** [status.notap.io](https://status.notap.io)
