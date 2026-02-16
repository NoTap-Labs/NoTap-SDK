# NoTap Seal V2: Advanced Non-Repudiation & Blockchain Anchoring

**Version:** 2.0.0
**Date:** 2026-02-15
**Status:** Production Ready (Merged)

---

## 1. Overview

**NoTap Seal V2** significantly upgrades the original cryptographic evidence system to provide **Compelling Evidence 3.0 (CE 3.0)** compliance for chargeback defense. It introduces three major architectural enhancements:

1.  **Data Enrichment:** Adds "Entropy Scores" and "Account Health" metrics to mathematically prove authentication strength.
2.  **Strategic "Push":** Automatically pushes evidence to PSPs/Merchants via webhooks immediately after verification.
3.  **Scalable Blockchain Anchoring:** Uses Merkle Tree batching to anchor thousands of seals to the Solana blockchain in a single transaction, providing immutable timestamping without prohibitive gas costs.

---

## 2. Architecture

### 2.1 The "Sign-then-Batch-then-Anchor" Flow

```mermaid
graph TD
    User[User] -->|Verifies Factors| Backend
    Backend -->|Generates| ZKProof[ZK-SNARK Proof]
    Backend -->|Assembles| Seal[Unsigned Seal JSON]
    Seal -->|Enrichment| Entropy[Calculate Entropy & Count]
    Entropy -->|Signing (KMS)| SignedSeal[Signed Seal]
    SignedSeal -->|Store| DB[(PostgreSQL)]
    
    subgraph "Real-Time Push"
    SignedSeal -->|Dispatch| Webhook[Webhook Service]
    Webhook -->|POST verification.succeeded| PSP[PSP / Merchant]
    end
    
    subgraph "Async Blockchain Anchoring"
    SignedSeal -->|Push Hash| RedisQueue[Redis Queue]
    RedisQueue -->|Pop Batch (N=100)| Batcher[Anchor Batcher Job]
    Batcher -->|Build| Merkle[Merkle Tree]
    Merkle -->|Root Hash| Anchor[Blockchain Anchor]
    Anchor -->|Tx| Solana[Solana Blockchain]
    Anchor -->|Proof| DB
    end
```

### 2.2 Component Breakdown

| Component | Responsibility | New in V2? |
| :--- | :--- | :--- |
| **Seal Assembler** | Collects session data, calculates **Entropy**, enforces PSD3 compliance. | ✅ Updated |
| **PDF Service** | Generates **Visa CE 3.0** compliant PDFs with Device/IP summaries. | ✅ Updated |
| **Webhook Service** | Dispatches `verification.succeeded` events with evidence links. | ✅ Updated |
| **Anchor Batcher** | Background job that builds Merkle Trees from pending seals. | ✅ **NEW** |
| **Merkle Tree** | Cryptographic structure to prove inclusion of a seal in a batch. | ✅ **NEW** |

---

## 3. Data Models

### 3.1 Seal JSON V1.1 Updates

The Seal JSON schema has been bumped to `1.1` to include security metrics.

```json
{
  "version": "1.1",
  "authentication": {
    "user_uuid_hash": "...",
    "factor_count_verified": 3,
    "factor_count_enrolled": 12,        // NEW: Contextualizes user behavior
    "entropy_score": 44,                // NEW: Security strength in bits
    "psd3_compliant": true
  },
  "blockchain_data": {                  // NEW: Populated after anchoring
    "chain": "solana",
    "network": "mainnet-beta",
    "tx_signature": "...",
    "merkle_root": "...",
    "merkle_proof": [ ... ]
  }
}
```

### 3.2 Entropy Calculation

Entropy is calculated based on the inherent strength of factors used:

| Factor Type | Estimated Entropy (Bits) |
| :--- | :--- |
| **NFC** | 30 (Hardware possession) |
| **Biometric** | 24 (Face/Fingerprint/Voice) |
| **Words** | 22 (Knowledge from large set) |
| **Behavioral** | 20 (Rhythm/Draw/Tap) |
| **Pattern** | 16 |
| **PIN** | 13 |

*Example:* A user verifying with **Face + PIN + NFC** achieves a score of `24 + 13 + 30 = 67 bits` (Military Grade).

---

## 4. Compelling Evidence 3.0 (PDF)

The PDF output has been restructured to align with Visa's dispute guidelines.

### New Sections
1.  **CE 3.0 Summary:**
    *   **Customer Device ID:** Hash of device fingerprint.
    *   **IP Address / Geolocation:** Verified IP at time of auth.
    *   **Linkage:** Statement of match with previous undisputed transactions.
2.  **Security Metrics:**
    *   **Account Health:** "12 factors enrolled (Power User)" - Proves user investment.
    *   **Strength Rating:** "VERY HIGH (Military Grade)".

---

## 5. Webhook Integration

Merchants/PSPs can subscribe to `verification.succeeded` to receive evidence automatically.

**Payload:**
```json
{
  "event": "verification.succeeded",
  "timestamp": "2026-02-15T12:00:00Z",
  "data": {
    "session_id": "sess_123...",
    "user_uuid": "...",
    "seal_id": "seal_abc...",
    "seal_url": "https://api.notap.io/v1/seal/seal_abc.../verify",
    "compelling_evidence": {
      "pdf_download_url": "https://api.notap.io/v1/seal/seal_abc.../verify/pdf",
      "json_data_url": "https://api.notap.io/v1/seal/seal_abc.../verify"
    }
  }
}
```

---

## 6. Configuration

Features are fully toggleable via environment variables.

| Variable | Default | Description |
| :--- | :--- | :--- |
| `NOTAP_SEAL_ENABLED` | `false` | Master toggle for seal generation. |
| `BLOCKCHAIN_ANCHOR_ENABLED` | `false` | Master toggle for blockchain anchoring. |
| `ANCHOR_BATCH_SIZE` | `100` | Number of seals to batch per transaction. |
| `ANCHOR_BATCH_INTERVAL_CRON` | `*/10 * * * *` | How often the batcher runs (every 10m). |

---

## 7. Implementation Details

### Database Schema Changes
*   **Table:** `notap_seals`
*   **New Columns:**
    *   `factor_count_enrolled` (INT)
    *   `entropy_score` (INT)
    *   `blockchain_data` (JSONB)

### Code Locations
*   **Assembler:** `backend/services/sealAssemblerService.js`
*   **PDF Generation:** `backend/services/sealPdfService.js`
*   **Batching Logic:** `backend/services/blockchain/AnchorBatcher.js`
*   **Cron Job:** `backend/services/jobs/anchorBatcherJob.js`
*   **Merkle Logic:** `backend/services/blockchain/MerkleTree.js`

---

**© 2026 NoTap. All rights reserved.**
