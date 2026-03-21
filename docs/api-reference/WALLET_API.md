# Wallet API

> Mount Path: `/v1/wallet`
> Auth: None
> Rate Limiting: 10 requests/min per IP
> Protocol: Ed25519 challenge-response

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/challenge/generate` | Generate signature challenge |
| POST | `/challenge/verify` | Verify signed challenge |
| POST | `/connect` | Complete wallet connection |
| DELETE | `/disconnect` | Disconnect wallet |
| GET | `/status/:uuid` | Get wallet connection status |

## POST /challenge/generate

Generate a challenge for wallet ownership verification.

**Request:**
```json
{
  "wallet_address": "So1ana...",
  "action": "wallet_connect"
}
```

**Valid actions:** `wallet_connect`, `sns_relink`, `sns_transfer`, `transaction_auth`

**Response (200):**
```json
{
  "success": true,
  "challenge": {
    "nonce": "...",
    "timestamp": 1711000000,
    "expires_at": 1711000300,
    "message": "Sign this message..."
  }
}
```

Challenge TTL: 5 minutes. Single-use (consumed after verification).

## POST /challenge/verify

Verify an Ed25519 signature against the challenge.

**Request:**
```json
{
  "wallet_address": "So1ana...",
  "signature": "base58...",
  "nonce": "..."
}
```

## POST /connect

Combines challenge verification + wallet linking. Caches wallet hash in Redis (24h TTL).

## Security

- Ed25519 signature verification (Solana)
- One-time challenge consumption (replay prevention)
- Wallet hashes anonymized in cache
- 10 req/min rate limit per IP
