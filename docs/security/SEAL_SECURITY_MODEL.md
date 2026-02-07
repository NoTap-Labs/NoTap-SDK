# NoTap Seal - Security Model

## Overview

The NoTap Seal provides cryptographic non-repudiation for payment authentication. This document describes the threat model, key management strategy, and security guarantees.

## Threat Model

### What the Seal Proves

| Property | Mechanism | Guarantee Level |
|----------|-----------|----------------|
| **Identity** | ZK-SNARK proves factor match | Mathematical (soundness) |
| **Integrity** | ECDSA-P256 signature | Cryptographic (NIST P-256) |
| **Authenticity** | KMS-managed signing key | Hardware (HSM-backed) |
| **Non-repudiation** | Combined ZKP + signature | Legal-grade |
| **Transaction binding** | Amount + merchant in public signals | Circuit-level |
| **Freshness** | Timestamp in public signals | Replay-resistant |

### What the Seal Does NOT Prove

- That the user intended to make the payment (only that they authenticated)
- That the payment was processed successfully (seal is generated at auth time)
- That no coercion occurred (ZKP proves factor knowledge, not willingness)

### Attack Vectors and Mitigations

| Attack | Mitigation |
|--------|-----------|
| **Forge seal** | ECDSA-P256 requires private key (in KMS HSM) |
| **Tamper with amount** | Amount bound into ZK circuit public signals |
| **Replay seal** | Session ID + timestamp are unique per verification |
| **Steal signing key** | AWS KMS: key never leaves HSM boundary |
| **Modify proof** | Any change invalidates ECDSA signature |
| **Expired seal** | Expiration tracked, verification returns `seal_expired: true` |

## Key Management

### Signing Key (ECDSA-P256)

```
Algorithm:  ECC_NIST_P256 (secp256r1)
Usage:      SIGN_VERIFY
Provider:   AWS KMS (production) / Local crypto (development)
Rotation:   Manual (re-sign affected seals)
```

**IMPORTANT:** This is a SEPARATE key from the encryption KMS key:
- **Encryption key** (`KMS_KEY_ID`): `SYMMETRIC_DEFAULT` / `ENCRYPT_DECRYPT`
- **Seal signing key** (`NOTAP_SEAL_KMS_KEY_ID`): `ECC_NIST_P256` / `SIGN_VERIFY`

### Key Creation

```bash
aws kms create-key \
  --key-spec ECC_NIST_P256 \
  --key-usage SIGN_VERIFY \
  --description "NoTap Seal Signing Key (ECDSA-P256)" \
  --tags TagKey=Service,TagValue=notap-seal
```

### Public Key Distribution

The public verification key is served at:
```
GET /.well-known/notap-zk-verification.json
```

This follows RFC 8615 (Well-Known URIs) for automatic discovery.

## Signature Process

### Signing

```
1. Remove signature field from seal (set to null)
2. Deep-sort all object keys recursively
3. JSON.stringify the sorted object (deterministic)
4. SHA-256 hash the canonical JSON
5. ECDSA-P256 sign the hash with KMS
6. Attach signature to seal
```

### Verification

```
1. Extract signature from seal
2. Set signature field to null
3. Deep-sort all object keys recursively
4. JSON.stringify the sorted object
5. SHA-256 hash the canonical JSON
6. ECDSA-P256 verify with public key
```

### Deterministic Canonicalization

The seal JSON is canonicalized before signing to ensure that:
- Key ordering doesn't affect the hash
- Identical seals always produce identical signatures
- Independent verifiers can reconstruct the exact same hash

## ZK-SNARK Integration

### Circuit Public Signals (V1.1)

| # | Signal | Purpose |
|---|--------|---------|
| 1 | `userUuidHash` | Bind to user identity |
| 2 | `timestamp` | Replay protection |
| 3 | `factorCount` | PSD3 compliance proof |
| 4 | `merkleRoot` | Bind to enrollment state |
| 5 | `sessionId` | Bind to specific session |
| 6 | `amountCents` | Transaction amount binding |
| 7 | `merchantIdHash` | Merchant binding |

Signals 5-7 implement PSD3/RTS Dynamic Linking: the proof is cryptographically bound to the specific transaction.

### Privacy Guarantees

The ZK proof reveals:
- That factors matched (boolean)
- The count and category diversity

The ZK proof does NOT reveal:
- Which specific factors (PIN vs Pattern vs Emoji)
- Factor values (the actual PIN digits)
- Factor digests (the hashed values)

## Data Privacy

### What's in the Seal

| Field | Contains | PII Risk |
|-------|----------|----------|
| `user_uuid_hash` | SHA-256 of UUID | None (one-way hash) |
| `factors_verified` | Factor names | None (no values) |
| `merchant_id` | Merchant identifier | Business data only |
| `session_id` | Session identifier | Ephemeral |
| `proof` | ZK math values | None (reveals nothing) |

### What's NOT in the Seal

- Raw user UUID
- Factor digests or values
- Biometric data
- Device information
- IP addresses

## Non-Blocking Architecture

Seal generation follows the same pattern as ZK proof generation:

```javascript
try {
    const seal = await assembleSeal({ session, proofResult, transactionContext });
    seal.signature = await signSeal(seal);
    sealId = await storeSeal(seal);
} catch (sealError) {
    // Non-blocking: log and continue
    logger.error('Seal generation failed (non-critical):', sealError.message);
}
```

**Verification NEVER fails due to seal issues.** The seal is an add-on for dispute evidence.

## V2 Roadmap: Blockchain Anchoring

Future enhancement (not in V1):

```
V1 (current): Seal + ZKP + ECDSA = Integrity via signature
V2 (planned): Seal + ZKP + ECDSA + Blockchain = Integrity via immutability
```

Planned approach:
- Hash of signed seal posted to Solana (low-cost anchor)
- Full seal stored on Arweave (permanent, decentralized)
- `blockchain` field populated with tx hash, block number, explorer URL
- Enables third-party verification without trusting NoTap's database

## Security Checklist

- [ ] `NOTAP_SEAL_KMS_KEY_ID` is set (not using local signer in production)
- [ ] KMS key is `ECC_NIST_P256` / `SIGN_VERIFY` (not encryption key)
- [ ] `.well-known` endpoint serves correct public key
- [ ] Seal expiration configured (`NOTAP_SEAL_EXPIRATION_DAYS`)
- [ ] Rate limiting on seal endpoints
- [ ] No PII in seal JSON (verify `user_uuid_hash` is actually hashed)
- [ ] Seal feature is opt-in (default `false`)
