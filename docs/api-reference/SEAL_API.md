# Seal API

> Mount Path: `/v1/seal`
> Auth: Mixed (public for retrieval/verification, auth for session/merchant)
> Rate Limiting: Global

## Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/:seal_id` | Retrieve seal JSON | None (public) |
| GET | `/:seal_id/pdf` | Download PDF dispute evidence | None (public) |
| POST | `/:seal_id/verify` | Verify seal integrity | None (public) |
| GET | `/session/:session_id` | Retrieve seal by session | Management auth |
| GET | `/merchant/:merchant_id` | List merchant seals | Merchant auth |

## GET /:seal_id

Public endpoint for banks/merchants to retrieve seal data. Proof data is redacted in public responses.

**Seal ID Format:** `seal_[UUID]`

## POST /:seal_id/verify

Verify seal signature and ZK proof integrity.

**Verification checks:**
1. Signature validity (against seal signing key)
2. Proof structure (pi_a, pi_b, pi_c, public_signals)
3. Public signals consistency
4. Seal expiration status
5. Blockchain anchor verification (V2, optional)

## GET /merchant/:merchant_id

List seals for a merchant. IDOR prevention: merchant can only view own seals.

**Query params:** `limit` (default 20), `offset` (default 0)

## Design

Seal IDs are UUID capability tokens — unguessable but shareable with banks for dispute resolution. Public retrieval is by design.
