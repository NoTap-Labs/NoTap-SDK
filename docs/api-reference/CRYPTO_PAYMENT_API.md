---
hidden: true
---

# Crypto Payment API

Device-free blockchain payment endpoints using relayer architecture.

**Base URL:** `/v1/crypto`

***

## Endpoints Overview

| Method | Endpoint                   | Description                   | Auth Required |
| ------ | -------------------------- | ----------------------------- | ------------- |
| POST   | `/relayer/enroll`          | User enrolls relayer approval | No            |
| PUT    | `/relayer/limits`          | Update spending limits        | Yes           |
| DELETE | `/relayer/revoke`          | Revoke relayer approval       | Yes           |
| GET    | `/relayer/status/:uuid`    | Get approval status           | Yes           |
| POST   | `/merchant/wallet/connect` | Merchant connects wallet      | Yes           |
| GET    | `/merchant/wallet/status`  | Get merchant wallet status    | Yes           |
| POST   | `/charge/create`           | Create payment charge         | Yes           |
| GET    | `/charge/:id`              | Get charge status             | Yes           |
| POST   | `/charge/:id/pay`          | Pay a charge                  | Yes           |
| POST   | `/payment/execute`         | Execute direct payment        | Yes           |
| GET    | `/payment/:id`             | Get payment status            | Yes           |
| GET    | `/spending/:uuid`          | Get spending history          | Yes           |
| GET    | `/config/chains`           | Get supported chains          | No            |
| GET    | `/config/tokens`           | Get supported tokens          | No            |

***

## Relayer Endpoints

### POST `/relayer/enroll`

Enroll user for relayer-based payments.

**Rate Limit:** 5 requests per hour

**Request:**

```json
{
  "uuid": "abc-123-def-456",
  "walletAddress": "0x...",
  "spendingLimitDaily": 100.00,
  "spendingLimitPerTx": 50.00,
  "allowedChains": ["solana", "ethereum"],
  "allowedTokens": ["USDC", "SOL"]
}
```

**Response (201):**

```json
{
  "success": true,
  "enrollmentId": "enroll_abc123",
  "status": "pending_verification"
}
```

***

### GET `/relayer/status/:uuid`

Get relayer approval status for a user.

**Response (200):**

```json
{
  "success": true,
  "status": "active",
  "spendingLimitDaily": 100.00,
  "spendingLimitPerTx": 50.00,
  "spentToday": 25.50,
  "allowedChains": ["solana"],
  "lastPayment": "2025-12-27T10:30:00Z"
}
```

***

## Charge Endpoints

### POST `/charge/create`

Create a payment charge (merchant-initiated).

**Rate Limit:** 60 requests per minute

**Request:**

```json
{
  "merchantId": "merchant_abc123",
  "amountUsd": 49.99,
  "currency": "USDC",
  "description": "Order #12345",
  "metadata": {
    "orderId": "12345",
    "customerEmail": "user@example.com"
  },
  "expiresIn": 3600
}
```

**Response (201):**

```json
{
  "success": true,
  "chargeId": "charge_xyz789",
  "amountUsd": 49.99,
  "currency": "USDC",
  "status": "pending",
  "expiresAt": "2025-12-27T11:30:00Z",
  "paymentUrl": "https://pay.notap.io/charge/xyz789"
}
```

***

### POST `/charge/:id/pay`

Pay an existing charge.

**Rate Limit:** 10 requests per minute

**Request:**

```json
{
  "uuid": "abc-123-def-456",
  "chain": "solana",
  "verifiedFactors": ["PIN", "FINGERPRINT"],
  "verificationSessionId": "sess_abc123"
}
```

**Response (200):**

```json
{
  "success": true,
  "paymentId": "pay_abc123",
  "status": "completed",
  "txHash": "5abc123...",
  "chain": "solana",
  "amountUsd": 49.99,
  "amountToken": 49.99,
  "token": "USDC"
}
```

***

## Configuration Endpoints

### GET `/config/chains`

Get list of supported blockchain networks.

**Response (200):**

```json
{
  "success": true,
  "chains": [
    {
      "chain": "solana",
      "display_name": "Solana",
      "enabled": true,
      "explorer_url": "https://solscan.io"
    },
    {
      "chain": "ethereum",
      "display_name": "Ethereum",
      "enabled": true,
      "explorer_url": "https://etherscan.io"
    }
  ]
}
```

***

### GET `/config/tokens`

Get supported tokens for a chain.

**Query Parameters:**

* `chain` (optional): Chain name (default: "solana")

**Response (200):**

```json
{
  "success": true,
  "tokens": [
    {
      "token_symbol": "SOL",
      "token_name": "Solana",
      "decimals": 9,
      "is_native": true,
      "is_stablecoin": false,
      "enabled": true,
      "icon_url": "https://..."
    },
    {
      "token_symbol": "USDC",
      "token_name": "USD Coin",
      "decimals": 6,
      "is_native": false,
      "is_stablecoin": true,
      "enabled": true,
      "icon_url": "https://..."
    }
  ]
}
```

***

## Error Responses

### 400 Bad Request

```json
{
  "success": false,
  "error": "Missing required fields: uuid, walletAddress"
}
```

### 401 Unauthorized

```json
{
  "success": false,
  "error": "Authentication required"
}
```

### 404 Not Found

```json
{
  "success": false,
  "error": "Charge not found"
}
```

### 429 Too Many Requests

```json
{
  "success": false,
  "error": "Too many payment attempts"
}
```

***

## Rate Limits

| Endpoint           | Limit | Window   |
| ------------------ | ----- | -------- |
| `/relayer/enroll`  | 5     | 1 hour   |
| `/charge/:id/pay`  | 10    | 1 minute |
| `/payment/execute` | 10    | 1 minute |
| Other endpoints    | 60    | 1 minute |

***

**Last Updated:** 2025-12-27
