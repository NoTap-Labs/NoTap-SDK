# NoTap Seal - Developer Guide

## What is a NoTap Seal?

A **NoTap Seal** is a cryptographic non-repudiation proof package generated after each successful NoTap authentication. It bundles:

1. **ZK-SNARK Proof** - Mathematical proof that the user authenticated with the correct factors
2. **ECDSA-P256 Signature** - Digital signature proving the proof originated from NoTap
3. **Transaction Context** - Amount, merchant, timestamp bound into the proof
4. **PSD3 Compliance Data** - Factor categories proving regulatory compliance

**Primary use case:** Chargeback defense. When a cardholder disputes a transaction, the merchant or PSP can present the NoTap Seal as irrefutable proof that the actual account holder authenticated.

## Quick Start

### 1. Enable NoTap Seal

Set in your environment:

```bash
NOTAP_SEAL_ENABLED=true
NOTAP_SEAL_KMS_KEY_ID=your-kms-key-id   # Optional: uses local signing if not set
NOTAP_SEAL_VERIFY_BASE_URL=https://api.notap.io
NOTAP_SEAL_EXPIRATION_DAYS=365
```

### 2. Initiate Verification (check seal availability)

```bash
POST /v1/verification/initiate
{
  "user_uuid": "alice.notap.sol",
  "transaction_amount": 49.99,
  "merchant_id": "merch_456"
}
```

Response includes `seal_available: true` when seals are enabled.

### 3. Complete Verification (seal auto-generated)

After successful factor verification, the response includes seal data:

```json
{
  "success": true,
  "auth_token": "abc123...",
  "session_id": "sess_xyz",
  "proof_id": "proof-42",
  "seal_id": "seal_12345678-1234-4123-8123-123456789abc",
  "seal_url": "https://api.notap.io/v1/seal/seal_12345678.../verify",
  "message": "Authentication successful"
}
```

### 4. Retrieve the Seal

```bash
GET /v1/seal/seal_12345678-1234-4123-8123-123456789abc
```

### 5. Verify the Seal

```bash
POST /v1/seal/seal_12345678-1234-4123-8123-123456789abc/verify
```

Returns:

```json
{
  "valid": true,
  "checks": {
    "signature_valid": true,
    "proof_structure_valid": true,
    "signals_consistent": true,
    "seal_expired": false
  }
}
```

### 6. Download PDF Evidence

```bash
GET /v1/seal/seal_12345678-1234-4123-8123-123456789abc/pdf
```

Returns a branded PDF suitable for bank/PSP dispute submission.

## API Reference

### Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `GET` | `/v1/seal/:seal_id` | Retrieve seal JSON | Public |
| `GET` | `/v1/seal/:seal_id/pdf` | Download PDF evidence | Public |
| `POST` | `/v1/seal/:seal_id/verify` | Verify seal integrity | Public |
| `GET` | `/v1/seal/session/:session_id` | Get seal by session | Public |
| `GET` | `/v1/seal/merchant/:merchant_id` | List merchant seals | Merchant |

### Seal JSON Structure

```json
{
  "version": "1.0",
  "seal_id": "seal_<uuid>",

  "transaction": {
    "id": "order_789",
    "amount": 49.99,
    "amount_cents": 4999,
    "currency": "USD",
    "merchant_id": "merch_456",
    "merchant_name": "Coffee Shop",
    "timestamp": "2026-02-07T12:00:00.000Z"
  },

  "authentication": {
    "user_uuid_hash": "0xabcdef...",
    "session_id": "sess_xyz",
    "factors_verified": ["PIN", "FACE", "RHYTHM_TAP"],
    "factor_categories": ["behavioral", "biometric", "knowledge"],
    "factor_count": 3,
    "psd3_compliant": true
  },

  "proof": {
    "protocol": "groth16",
    "curve": "bn128",
    "pi_a": ["0x...", "0x..."],
    "pi_b": [["0x...", "0x..."], ["0x...", "0x..."]],
    "pi_c": ["0x...", "0x..."],
    "public_signals": [
      "userUuidHash", "timestamp", "factorCount", "merkleRoot",
      "sessionIdHash", "amountCents", "merchantIdHash"
    ],
    "verification_key_hash": "abc123..."
  },

  "signature": {
    "algorithm": "ECDSA_SHA_256",
    "signer": "aws-kms",
    "value": "base64-encoded-signature",
    "certificate_url": "https://api.notap.io/.well-known/notap-zk-verification.json"
  },

  "verification": {
    "verify_url": "https://api.notap.io/v1/seal/{seal_id}/verify",
    "verification_key_url": "https://api.notap.io/.well-known/notap-zk-verification.json"
  },

  "blockchain": null,

  "metadata": {
    "created_at": "2026-02-07T12:00:00.000Z",
    "expires_at": "2027-02-07T12:00:00.000Z",
    "generator": "notap-seal-v1"
  }
}
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `NOTAP_SEAL_ENABLED` | `false` | Master toggle for seal generation |
| `NOTAP_SEAL_KMS_KEY_ID` | (empty) | AWS KMS key ID for ECDSA-P256 signing |
| `NOTAP_SEAL_VERIFY_BASE_URL` | `https://api.notap.io` | Base URL for verification endpoints |
| `NOTAP_SEAL_EXPIRATION_DAYS` | `365` | Days until seal expires (0 = never) |

## Chargeback Defense Workflow

```
1. User buys coffee for $4.99 at Coffee Shop
2. NoTap authenticates user (PIN + Face + Rhythm)
3. Seal generated: ZKP + ECDSA signature + transaction binding
4. 30 days later: User disputes charge ("I didn't buy that")
5. Coffee Shop retrieves seal from NoTap API
6. Coffee Shop submits seal PDF to acquiring bank
7. Bank verifies:
   a. ECDSA signature matches NoTap's public key
   b. ZK proof is structurally valid
   c. Transaction amount matches ($4.99)
   d. 3 factors verified from 3 categories (PSD3 compliant)
8. Bank rules in merchant's favor (non-repudiation proven)
```

## Integration Examples

### Node.js

```javascript
const axios = require('axios');

// After successful verification, retrieve the seal
const sealId = verificationResponse.seal_id;
const seal = await axios.get(`https://api.notap.io/v1/seal/${sealId}`);

// Verify independently
const verification = await axios.post(
  `https://api.notap.io/v1/seal/${sealId}/verify`
);

console.log(verification.data.valid); // true
console.log(verification.data.checks.signature_valid); // true
```

### Download PDF for dispute

```javascript
const response = await axios.get(
  `https://api.notap.io/v1/seal/${sealId}/pdf`,
  { responseType: 'arraybuffer' }
);

fs.writeFileSync('dispute-evidence.pdf', response.data);
```

## FAQ

**Q: Does the seal contain any user PII?**
A: No. The seal contains a SHA-256 hash of the user UUID, not the UUID itself. Factor names are included (e.g., "PIN", "FACE") but not the actual factor values or digests.

**Q: What happens if seal generation fails?**
A: The verification still succeeds. Seal generation is non-blocking — if it fails, the response simply won't include `seal_id` and `seal_url`.

**Q: How long are seals valid?**
A: Configurable via `NOTAP_SEAL_EXPIRATION_DAYS` (default: 365 days). This covers typical chargeback dispute windows of 120 days with margin.

**Q: Can a seal be forged?**
A: No. The ECDSA-P256 signature is computed over the entire seal JSON. Any modification (even one byte) invalidates the signature. The signing key is stored in AWS KMS hardware and never leaves the HSM.

**Q: What is the blockchain field for?**
A: V2 feature. In a future release, seals will be optionally anchored to a blockchain for additional immutability guarantees. V1 relies on ECDSA signature integrity.
