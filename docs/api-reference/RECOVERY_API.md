# Recovery API

> Mount Path: `/v1/recovery`
> Auth: Mixed (public for verify, management auth for status/regenerate)
> Rate Limiting: Global + 3 attempts/hour per IP for `/verify`
> Replay Protection: Yes

## Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/verify` | Verify recovery code | None (code is auth) |
| GET | `/status/:uuid` | Check remaining codes | Management auth |
| POST | `/regenerate` | Generate new codes | Management auth (WRITE) |

## POST /verify

Verify a recovery code and receive a re-enrollment token.

**Request:**
```json
{
  "uuid": "abc-123-def",
  "recovery_code": "ABCD-EFGH-JKLM-NPQR"
}
```

**Response (200):**
```json
{
  "success": true,
  "reenrollment_token": "...",
  "remaining_codes": 7,
  "grace_period_days": 7
}
```

**Rate Limit:** 3 attempts per hour per IP.

## Recovery Flow

1. User cannot access factors
2. Provides UUID + recovery code
3. Receives 1-hour re-enrollment token
4. 7-day grace period (reduced factor threshold)

## Security

- Codes are single-use (consumed after verification)
- bcrypt + KMS wrapped (never plaintext)
- 8 codes per batch
- Codes not retrievable after initial generation
- Re-enrollment tokens: 1-hour TTL, single-use
