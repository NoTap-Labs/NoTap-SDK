# Fraud Analysis: NoTap vs. Payment Fraud Landscape

**Last Updated:** 2026-05-27  
**Purpose:** Comprehensive analysis of how NoTap's architecture mitigates payment fraud across the full fraud taxonomy, identifying gaps and opportunities.

---

## Table of Contents

1. [2026 Fraud Landscape Summary](#1-2026-fraud-landscape-summary)
2. [Fraud Type Taxonomy & NoTap Mitigation](#2-fraud-type-taxonomy--notap-mitigation)
3. [SIM Swap & SMS OTP Vulnerabilities](#3-sim-swap--sms-otp-vulnerabilities)
4. [Chargeback / Friendly Fraud](#4-chargeback--friendly-fraud)
5. [Account Takeover (ATO)](#5-account-takeover-ato)
6. [Phishing & Social Engineering](#6-phishing--social-engineering)
7. [Card-Not-Present (CNP) Fraud](#7-card-not-present-cnp-fraud)
8. [First-Party Fraud](#8-first-party-fraud)
9. [Synthetic Identity Fraud](#9-synthetic-identity-fraud)
10. [Policy Abuse & Refund Fraud](#10-policy-abuse--refund-fraud)
11. [NoTap Seal: Cryptographic Non-Repudiation](#11-notap-seal-cryptographic-non-repudiation)
12. [Agentic Commerce / AI Bot Fraud](#12-agentic-commerce--ai-bot-fraud)
13. [Adversary-in-the-Middle (AiTM) MFA Bypass](#13-adversary-in-the-middle-aitm-mfa-bypass)
14. [Cross-Domain Fraud Vectors](#14-cross-domain-fraud-vectors)
15. [Current Fraud Detection in SDK (7 Strategies)](#15-current-fraud-detection-in-sdk-7-strategies)
16. [Gaps & Recommendations](#16-gaps--recommendations)

---

## 1. 2026 Fraud Landscape Summary

### Macro Trends (Adyen 2026, MRC 2026, Sift Q1 2026, LexisNexis 2026)

| Metric | Value | Source |
|--------|-------|--------|
| Global card fraud losses | $28B+ annually | Nilson Report |
| Chargeback volume projected (2028) | 324M transactions | Mastercard/Datos Insights |
| Friendly fraud share of chargebacks | 70% | Sift FIBR |
| First-party fraud as % of all fraud | 38-42% (growing) | LexisNexis, GR4VY |
| eCommerce chargeback rate increase (YoY) | 222% | SQ Magazine |
| Global eCommerce fraud losses (2029 projection) | $107B+ annually | Sift |
| Account takeover attack rate | ~0.95-1.3% of logins | Sift, LexisNexis |
| Synthetic identity fraud increase (YoY) | 8x | LexisNexis |
| Agentic traffic growth (Jan-Dec 2025) | 450% | LexisNexis |
| Phishing share of successful attacks | 90% | Various |
| SMS OTP deprecation regulators (2026) | UAE, India, Philippines + NIST/CISA | Multiple |

### Key Shift: Third-Party → First-Party Fraud

```
2020: 55% third-party card fraud, 25% first-party fraud
2026: 32% third-party card fraud, 42% first-party fraud
Source: GR4VY Payment Fraud Report 2026
```

### The Authentication Paradox

93% of consumers accept extra verification at checkout to reduce fraud. Yet 2FA adoption across websites averages just 2.93-3.79% (Sift 2025 data). This represents a massive untapped opportunity for NoTap.

---

## 2. Fraud Type Taxonomy & NoTap Mitigation

### Legend
| Icon | Meaning |
|------|---------|
| ✅ | Strong inherent mitigation |
| ⚠️ | Partial / requires configuration |
| ❌ | Not directly addressed |
| 🔮 | Potential / future opportunity |

| # | Fraud Type | NoTap Mitigation | Mechanism | Rating |
|---|-----------|-----------------|-----------|--------|
| 1 | **SIM Swap** | ✅ Full | Device-free MFA: No SMS dependency. 15 factors, none phone-number-bound. | ✅ **Eliminates root cause** |
| 2 | **SMS OTP Interception** | ✅ Full | No OTP codes. All factors are locally generated digests. | ✅ **Eliminates attack surface** |
| 3 | **Chargeback / Friendly Fraud** | ✅ Strong | NoTap Seal: ZK-SNARK + ECDSA-P256 signature = cryptographic proof of auth. Merchant wins disputes. | ✅ **Industry-first solution** |
| 4 | **Account Takeover** | ✅ Strong | 3-15 factor requirement. Rate limiting (5/hr/user). Behavioral profiling. Device fingerprinting. | ✅ **Multi-layer defense** |
| 5 | **Credential Stuffing** | ✅ Full | No passwords stored. Each factor requires unique input. Rate limiting kills automation. | ✅ **By design** |
| 6 | **Phishing (credential capture)** | ✅ Strong | Factors are device-independent, not typed like passwords. Behavioral factors can't be phished. | ✅ **Factor diversity** |
| 7 | **Adversary-in-the-Middle (AiTM)** | ⚠️ Partial | Origin-bound: digests computed locally. But unlike FIDO2, no cryptographic origin binding on the proof layer. ZK proofs add privacy but not AiTM resistance at protocol level. | ⚠️ **See §13** |
| 8 | **MFA Fatigue** | ✅ Strong | No push notifications. Users actively enter factors. No "approve" button to spam. | ✅ **By design** |
| 9 | **Card-Not-Present (CNP)** | ✅ Strong | Replaces CVV/3DS with multi-factor auth. PSP integration for parallel session creation. | ✅ **Drop-in replacement** |
| 10 | **First-Party Fraud** | ✅ Strong | NoTap Seal provides cryptographic proof of user's active participation. Cannot claim "didn't authorize." | ✅ **Unique advantage** |
| 11 | **Synthetic Identity** | ⚠️ Partial | NoTap verifies identity through factors, not documents. Strong for existing users; enrollment verification depends on integration KYC. | ⚠️ **Integration-dependent** |
| 12 | **Policy Abuse / Refund Fraud** | ❌ Not addressed | NoTap authenticates, doesn't set policies. But NoTap Seal could prove refund requests are legitimate. | 🔮 **Seal extension** |
| 13 | **Card Testing / Enumeration** | ✅ Strong | Rate limiting (5/hr/user). Unique per-user factor requirements. Inconsistent failure messages blocked. | ✅ **Built-in** |
| 14 | **Brute Force** | ✅ Strong | 5 attempts/hr/user. Factor entropy: 42-240 bits per transaction. | ✅ **Crypto-strong** |
| 15 | **Session Hijacking** | ⚠️ Partial | 15-min session timeout. Nonce validation. Session locking. But no device token binding. | ⚠️ **See §13** |
| 16 | **Man-in-the-Middle** | ✅ Strong | TLS 1.3, end-to-end encrypted digest flow. Digests are single-use bound to nonce. | ✅ **Crypto-level** |
| 17 | **Replay Attack** | ✅ Strong | Nonce validation (single-use), timestamp validation (5-min window), session locking. Pen-tested verified. | ✅ **Verified** |
| 18 | **Insider Threat** | ✅ Strong | ZK proofs: server never sees factor values. KMS-wrapped keys. No plaintext secrets on backend. | ✅ **Zero-knowledge architecture** |
| 19 | **Physical Device Theft** | ✅ Strong | Device-independent: factors not stored on device. Enrollment in KeyStore (30d expiry). Remote wipe. | ✅ **Device-free design** |
| 20 | **Social Engineering** | ⚠️ Partial | Factor diversity prevents single-channel compromise. Behavioral factors resist social engineering. User education remains necessary. | ⚠️ **Human factor** |
| 21 | **Agentic Bot Fraud** | ✅ Strong | Behavioral detection (typing speed, completion time). 500ms minimum factor time = bot detection. Rate limiting. | ✅ **Configurable** |
| 22 | **Promotion/Gift Card Fraud** | ❌ Not addressed | NoTap provides auth, not promotion logic. Could authenticate high-value promo redemptions. | 🔮 **Integration** |
| 23 | **Triangulation Fraud** | ⚠️ Partial | NoTap verifies the user, not the transaction context. Merchant-side controls needed. | ⚠️ **Merchant-side** |
| 24 | **Business Email Compromise** | ⚠️ Partial | NoTap could replace email-based payment approval with multi-factor. Requires BEC-specific flow. | 🔮 **Extension** |
| 25 | **SS7 Protocol Attack** | ✅ Full | No SMS in the auth flow. SS7 interception irrelevant. | ✅ **By design** |
| 26 | **OTP Pumping / AIT** | ✅ Full | No OTPs. No SMS costs to inflate. | ✅ **By design** |

---

## 3. SIM Swap & SMS OTP Vulnerabilities

### The Problem

SIM swap fraud increased **342% in 2025** (Phone Check 2026). Attackers port a victim's number to their SIM for **$10-100 per target** (dark web pricing). Once they control the number:

1. All SMS OTPs route to attacker's phone
2. Password resets for banking, email, crypto
3. Average loss: **$12,400 per incident**

**Regulators in 2026 actively deprecating SMS OTP:**
- UAE Central Bank (March 2026): Directive to move away from SMS OTP
- RBI (India): Mandating phishing-resistant auth
- NIST SP 800-63-4: SMS is "restricted" authenticator
- CISA: Formal guidance to stop relying on SMS

### How NoTap Eliminates SIM Swap

NoTap is **SMS-free by design** — not as a migration path, but as a core architectural decision:

| SMS OTP Weakness | NoTap Solution |
|-----------------|----------------|
| Phone-number-bound | UUID/alias/SNS-bound |
| Carrier-dependent | Carrier-independent |
| 5-30s delivery delay | Instant (local digest computation) |
| 12-15% delivery failure | No delivery needed |
| SIM swap intercepts codes | No codes to intercept |
| SS7 interception possible | No SMS in flow |
| Single-channel (SMS) | 15 diverse factor types |
| Phishable (enter OTP code) | No reusable values to phish |

### Risk-Based Factor Selection Handles Residual Risk

Even if a user's device is fully compromised, NoTap's **risk-based factor selection** kicks in:
- Low risk ($0-30): 2 factors
- Medium risk ($30+): 3 factors  
- Fraud detected: 3 factors + escalation
- Behavioral factors (MouseDraw, RhythmTap, StylusDraw) **cannot be phished or transferred**

---

## 4. Chargeback / Friendly Fraud

### Scale of the Problem

| Metric | Value |
|--------|-------|
| Global chargeback costs projected | $125B |
| U.S. merchants annual chargeback loss | $15B |
| Friendly fraud share | 70% of all chargebacks |
| Chargeback growth (2025→2028) | 24% |
| eCommerce chargeback rate increase (YoY) | 222% |
| Merchants win only | 17.4% of fraud-coded chargebacks |
| Per-incident cost | ~$250 |

### How NoTap Wins Chargebacks: The NoTap Seal

The **NoTap Seal** (V1 implemented) is the industry's first cryptographic non-repudiation package for authentication:

```
┌─────────────────────────────────────────────────────────────────┐
│                    NoTap Seal Structure                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  {                                                               │
│    "seal_id": "notap-seal-abc123",                               │
│    "version": "1.0.0",                                           │
│    "timestamp": 1699999999000,                                    │
│    "session": {                                                   │
│      "session_id": "sess-...",                                    │
│      "user_uuid_hash": "sha256(uuid)",                            │
│      "merchant_id": "merchant-123",                               │
│      "amount_cents_hash": "sha256(amount)",                       │
│      "factor_count": 3,                                           │
│      "factor_types_hash": "sha256(PIN,MOUSE,RHYTHM)",             │
│      "verification_type": "PAYMENT"                               │
│    },                                                             │
│    "proof": {                                                     │
│      "pi_a": [...],                 // Groth16 proof              │
│      "pi_b": [...],                                               │
│      "pi_c": [...],                                               │
│      "public_signals": [...],       // ZK public outputs          │
│      "proof_id": "proof-1"                                        │
│    },                                                             │
│    "signature": "ECDSA-P256..."     // Merchant-signable          │
│    "blockchain_tx": "Solana signature..."                         │
│  }                                                                │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

**Why this defeats chargebacks:**

1. **Cryptographic proof of user participation**: ZK-SNARK proves multi-factor authentication occurred
2. **Non-repudiation**: User signed with their factors (something they know/are/do)
3. **Tamper-evident**: ECDSA-P256 signature + Solana blockchain anchor
4. **Privacy-preserving**: Proof reveals nothing about *which* factors were used
5. **Merchant-countersignable**: Seal can carry merchant's signature for dual accountability

### Current Status
- ✅ Seal assembly service (`sealAssemblerService.js`)
- ✅ Seal signing service (`sealSigningService.js`) with KMS integration
- ✅ ZK-SNARK proof generation (`zkProofService.js`)
- ✅ Blockchain anchor (`BlockchainAnchor.js`, `LocalAnchor.js`)
- ✅ PDF generation for evidence packages (`sealPdfService.js`)
- ✅ 29 security hardening fixes applied (CRITICAL: memory wiping, IDOR, key exposure)
- ✅ 114 tests for the seal system

### How Merchants Use Seals in Disputes

```
1. Customer disputes charge (friendly fraud)
2. Merchant downloads NoTap Seal PDF
3. PDF contains:
   - Timestamp of authenticated session
   - ZK proof of multi-factor completion
   - Blockchain transaction ID (immutable)
   - Merchant transaction context
4. Merchant submits to acquirer/bank
5. Bank verifies cryptographic proof
6. Dispute ruled in merchant's favor
```

---

## 5. Account Takeover (ATO)

### Scale

- ATO attack rate: 0.95-1.3% of logins (Sift FIBR Q1 2026)
- eCommerce ATO attempt rate surged **64% YoY** (LexisNexis)
- Login attack rate jumped **216%** (LexisNexis)
- 93% of consumers accept extra verification when it reduces fraud risk

### NoTap's Multi-Layer ATO Defense

| Layer | Mechanism | Effect |
|-------|-----------|--------|
| **Knowledge** | PIN, Pattern, Words, Colour, Emoji | Something attacker doesn't know |
| **Biometric** | Voice, Face, Fingerprint | Can't be stolen remotely |
| **Behavioral** | MouseDraw, RhythmTap, StylusDraw, ImageTap | Can't be replicated from logs |
| **Possession** | NFC | Requires physical tag |
| **Context** | Balance (accelerometer gait) | Can't be simulated |
| **Rate Limiting** | 5 attempts/hr/user | Kills brute force |
| **Escalation** | Failed factor → add another factor | Attacker must know MORE |
| **Session Control** | 15-min window, single-use nonce | Window too small for slow attacks |

### ATO Attack Chain vs. NoTap

```
Attack Chain:
Credential Leak → Password Found → Login → ATO

NoTap Blocks At:
❌ No passwords to leak (factors are local digests)
❌ Each factor requires UNIQUE input every time
❌ Rate limiting after 5 failures
❌ Escalation: fail one factor → must do MORE
❌ ZK proofs prevent even server-side ATO
```

---

## 6. Phishing & Social Engineering

### 2026 Threat Landscape

- Phishing constitutes **90%** of successful cyber attacks
- Social engineering causes **74%** of cybersecurity breaches
- SMS scams increased **61% YoY**
- AiTM phishing kits (Evilginx, Tycoon 2FA, Starkiller) industrialized

### NoTap's Structural Advantages

| Phishing Vector | Why NoTap Resists |
|----------------|-------------------|
| Credential harvesting | No reusable credentials. Digests are one-time. |
| Fake login page | Factors are device-processed locally. No typed passwords. |
| Voice phishing (vishing) | Behavioral factors can't be described over phone. |
| SMS phishing (smishing) | No SMS in auth flow. No codes to forward. |
| Spear-phishing | Factor diversity: 15 types across 5 categories. |
| MFA fatigue | No push notifications. Active factor entry required. |

### Behavioral Factors: The Phishing-Proof Layer

Factors like **MouseDraw, StylusDraw, RhythmTap, and ImageTap** have unique properties:

1. **Cannot be described**: You can't tell someone your mouse movement pattern
2. **Cannot be screenshot**: A screenshot of a drawing canvas doesn't recreate the timing
3. **Cannot be keylogged**: No keystrokes to capture
4. **Cannot be relayed**: AiTM proxy can't forward behavioral input in real-time
5. **User-specific**: Same person draws the same way each time (behavioral biometric)

---

## 7. Card-Not-Present (CNP) Fraud

### Scale

- CNP fraud losses: **$28B globally**
- 63% of merchant transactions are digital (CNP)
- 3D Secure adoption reduces liability but adds friction
- Dynamic 3DS reduces auth rates by 50-70% while maintaining fraud protection

### NoTap vs. 3D Secure

| Dimension | 3D Secure (3DS) | NoTap |
|-----------|-----------------|-------|
| Auth factors | Typically 1 (SMS OTP or app) | 2-6 factors (configurable) |
| Device binding | Phone-bound | Device-free (any device) |
| Cryptographic proof | Not standard | ZK-SNARK + ECDSA-P256 Seal |
| User experience | Redirect, OTP entry | Canvas-based, inline |
| Friction | High (30-50% abandonment at high amounts) | Low (<10s for 2 factors) |
| Liability shift | Yes (for authenticated transactions) | Yes + cryptographic evidence |
| Phishing resistance | Low (SMS OTP) | High (diverse factors) |
| Agentic commerce | Not designed for it | MCP server already built |

### PSP Parallel Session Integration

NoTap creates PSP checkout sessions **in parallel** with authentication:
- Saves 200-300ms (28% faster checkout)
- Supported PSPs: Stripe, Tilopay, Adyen, MercadoPago, Square
- Non-blocking: auth succeeds even if PSP session fails
- Backward compatible: optional `psp_config` parameter

---

## 8. First-Party Fraud

### Definition & Scale

First-party fraud: **legitimate customer** makes a purchase, receives goods/services, then fraudulently disputes the charge.

- **42% of all fraud** in 2026 (up from 25% in 2020)
- **$100B annually** in friendly fraud losses globally
- **65%** stems from customer confusion (unclear billing descriptors)
- **85%** of merchants now use chargeback mitigation tools
- Only **17.4%** of chargebacks are won by merchants

### Why Traditional Tools Fail

```javascript
// Traditional fraud detection can't catch first-party fraud:
if (cardIsValid && AVSMatch && CVVMatch) {
    approve(); // All checks pass — customer is real!
}
// The customer is the fraudster — and the system trusts them.
```

### NoTap's Solution: Cryptographic Non-Repudiation

The **NoTap Seal** proves that:
1. The **specific user** was authenticated
2. They completed **multiple factors** (volitional participation)
3. The authentication happened at a **specific time** (blockchain-anchored)
4. The transaction **context** was known (amount, merchant, timestamp)

**Legal argument**: "The user completed 3 authentication factors. Each factor requires active, conscious participation. A PIN entry + mouse drawing + rhythm tapping cannot be automated, delegated, or performed unknowingly."

---

## 9. Synthetic Identity Fraud

### Scale & Trend

- **8x increase YoY** globally (LexisNexis 2026)
- **11% of all fraud** now involves synthetic identities
- **48.3% of fraud in LATAM** is synthetic identity
- $20B+ in losses annually

### How Synthetic Identity Works

```
1. Fraudster combines real SSN + fake name/birthdate
2. Builds credit profile over 12-24 months
3. Applies for credit → appears legitimate
4. "Busts out" — maxes out credit, disappears
```

### NoTap's Mitigation Role

| Stage | Traditional Weakness | NoTap Strength |
|-------|---------------------|----------------|
| Account creation | Document verification can be faked | Factors require genuine human interaction |
| Credit building | Automated scripts simulate activity | Behavioral factors detect automation |
| Bust-out | Single-factor auth bypassed | Multi-factor required for high-value |
| Chargeback | No proof of user participation | Seal provides cryptographic evidence |

**Limitation**: NoTap doesn't replace initial KYC. Synthetic identity detection at enrollment must be handled by the integrating system. However, NoTap's behavioral factors during enrollment **do** provide bot-detection signals.

---

## 10. Policy Abuse & Refund Fraud

### Scale

- **39.8%** of businesses affected (Adyen 2026)
- **57%** of merchants report increase in refund/policy abuse (MRC 2026)
- Types: serial returns, wardrobing, free trial cycling, loyalty harvesting

### NoTap Opportunity (Not Yet Implemented)

Policy abuse is outside NoTap's current scope, but the **NoTap Seal** could be extended:

```javascript
// Future: Authenticate refund requests
POST /v1/refund
{
    "user_uuid": "...",
    "transaction_id": "TXN-...",
    "reason": "Item defective",
    "notap_auth": {
        "seal_id": "notap-seal-...",
        "factor_count": 2,
        "timestamp": 1699999999000
    }
}
```

**Benefit**: Proves the actual user requested the refund — reduces refund fraud without adding friction to legitimate returns.

---

## 11. NoTap Seal: Cryptographic Non-Repudiation

### Current Implementation

| Component | File | Status |
|-----------|------|--------|
| Seal assembly | `backend/services/sealAssemblerService.js` | ✅ Complete |
| Seal signing (KMS) | `backend/crypto/sealSigningService.js` | ✅ Complete (29 security fixes) |
| ZK proof service | `backend/services/zkProofService.js` | ✅ Complete |
| Seal storage | `backend/services/sealStorageService.js` | ✅ Complete |
| Seal PDF generation | `backend/services/sealPdfService.js` | ✅ Complete |
| Blockchain anchor | `backend/services/BlockchainAnchor.js` | ✅ Complete |
| Local anchor fallback | `backend/services/LocalAnchor.js` | ✅ Complete |
| Router | `backend/routes/sealRouter.js` | ✅ Complete |
| Security tests | `backend/tests/unit/sealSecurity.test.js` | ✅ 49 tests |
| Blockchain tests | `backend/tests/unit/blockchainAnchor.test.js` | ✅ |
| signing tests | `backend/tests/unit/sealSigning.test.js` | ✅ |

### Seal Generation Flow

```
Verification Success
    ↓
zkProofService.generateProof()
    ├── Creates Groth16 proof (pi_a, pi_b, pi_c)
    ├── Generates public signals (factor count, timestamp)
    └── Returns proof result
    ↓
sealAssemblerService.assembleSeal()
    ├── Combines session data + proof result + transaction context
    ├── Creates unsigned seal JSON
    └── Returns unsigned seal
    ↓
sealSigningService.sign()
    ├── Canonicalizes JSON (deep sort keys)
    ├── SHA-256 hash of canonical JSON
    ├── ECDSA-P256 signature (KMS or Local)
    ├── Memory wipe all hash buffers
    └── Returns signed seal
    ↓
BlockchainAnchor.anchorSeal()
    ├── Optional: write to Solana
    └── Return transaction signature
    ↓
sealStorageService.store()
    ├── Store in PostgreSQL
    └── Return seal_id
    ↓
sealPdfService.generate()
    ├── Create human-readable PDF
    └── Available for download
```

---

## 12. Agentic Commerce / AI Bot Fraud

### 2026 Threat

- **450% increase** in agentic traffic (Jan-Dec 2025, LexisNexis)
- AI bots mimicking human cursor movements, typing patterns
- **59% rise** in malicious bot attacks
- Agentic commerce: AI agents making purchases on behalf of users

### NoTap's Built-In Defenses

| Bot Technique | NoTap Detection |
|--------------|-----------------|
| Fast factor completion (<500ms) | ✅ `MIN_FACTOR_COMPLETION_MS` flag |
| Perfect typing speed | ✅ Behavioral deviation scoring |
| Same pattern every time | ✅ Historical profile comparison |
| Automated replays | ✅ Nonce + timestamp validation |
| Session reuse | ✅ Single-use sessions |
| Credential stuffing | ✅ Rate limiting (5/hr/user) |

### MCP Server for Authorized Agent Commerce

**Already implemented** (v3.21.0): MCP server + A2A Agent Card for agentic payment authorization:

```javascript
// Agent registers with NoTap
POST /v1/agent/register
{
    "agent_name": "my-shopping-agent",
    "capabilities": ["payment:initiate", "payment:authorize"],
    "max_transaction_amount": 100.00,
    "callback_url": "https://my-agent.com/webhook"
}

// Agent initiates payment
POST /v1/agent/payment
{
    "agent_id": "agent-123",
    "user_uuid": "user-456",
    "amount": 75.00,
    "merchant_id": "merchant-789",
    "context": { "order_id": "order-999" }
}

// User completes factors to authorize
// Agent receives callback on success/failure
```

**Key**: The user still must complete MFA. The agent can *request* payments but cannot *authorize* them without the user's factor input. This prevents agent compromise from leading to unauthorized payments.

---

## 13. Adversary-in-the-Middle (AiTM) MFA Bypass

### The 2026 Reality

AiTM phishing kits (Evilginx, Tycoon 2FA, Bluekit, Starkiller) have **industrialized MFA bypass**:

```
Victim → AiTM Proxy → Real Site
   ↓                        ↓
Credentials relayed    MFA challenge
   ↓                        ↓
MFA response relayed   Session cookie
   ↓                        ↓
Attacker steals cookie ←——┘
```

**All shared-secret MFA fails** against AiTM:
- ❌ SMS OTP
- ❌ TOTP (Google Authenticator)
- ❌ Push notifications (even with number matching)
- ❌ Email magic links

**Only FIDO2/WebAuthn passkeys resist** (origin-bound cryptography).

### NoTap's AiTM Resistance

**Current**: NoTap is partially resistant but does **not** have FIDO2-level protocol origin binding.

| AiTM Attack Stage | NoTap Response |
|-------------------|---------------|
| Proxy relays login page | Factors are COMPUTED locally, not typed. PIN is entered on canvas, not in form field. |
| Attacker captures factor input | Digests are single-use + session-bound. Replay attack blocked by nonce. |
| Attacker steals session cookie | 15-min timeout, session locking, nonce validation. |
| Proxy relays MFA flow in real-time | Behavioral factors (MouseDraw, RhythmTap) require genuine human interaction — hard to relay. |
| ZK proof generation | Proof bound to session — not replayable on attacker's device. |

**Key Difference from Traditional MFA**:
```
Traditional MFA: Enter OTP code from SMS/app → code is a shared secret → can be relayed
NoTap: User draws on canvas → local SHA-256 digest → digest is NOT the factor value → cannot be relayed
```

**However**, if the AiTM proxy serves a fake canvas and captures the user's raw factor input (e.g., the actual PIN digits or pattern coordinates), this is a concern for knowledge factors. **Behavioral factors resist this** because:
- MouseDraw coordinates alone don't reproduce the timing dynamics
- RhythmTap intervals are normalized with enrollment metadata
- These factors are **behavioral biometrics**, not secrets

### Recommendation for AiTM Hardening

Consider adding **origin binding** to the ZK proof circuit:

```kotlin
// Future: Include merchant origin in digest computation
fun computeDigest(factorInput, merchantOrigin) {
    val boundInput = factorInput + merchantOrigin.encodeToByteArray()
    SHA-256(boundInput)
}
```

This would make the digest cryptographically bound to the specific merchant domain, defeating AiTM relay at the protocol level — similar to FIDO2's origin binding.

---

## 14. Cross-Domain Fraud Vectors

### Fraud That NoTap Can Address Beyond Payments

| Domain | Fraud Type | NoTap Application |
|--------|-----------|-------------------|
| **Healthcare** | Insurance fraud, prescription fraud | Authenticate provider-patient relationship before claims |
| **Government** | Benefits fraud, identity theft | Multi-factor identity verification for benefits access |
| **Gaming** | Account sharing, RMT fraud | Factor verification for high-value item trades |
| **IoT** | Device spoofing, unauthorized access | NFC + behavioral auth for device admin |
| **Education** | Exam fraud, diploma mills | Proctored factor completion + behavioral biometrics |
| **Legal** | E-signature forgery | NoTap Seal as cryptographic e-signature with MFA |
| **Insurance** | Claims fraud | Authenticate claimant identity + Seal for claim authorization |
| **Real Estate** | Title fraud, wire fraud | Multi-factor for wire transfer authorization (already a use case) |
| **Cryptocurrency** | Wallet drain, SIM swap | Device-free MFA for exchange withdrawals |
| **Enterprise SSO** | Credential reuse, shadow IT | NoTap as phishing-resistant auth for corporate apps |

### Wire Fraud Prevention (Real Estate / High-Value)

**$2.9B lost annually** to Business Email Compromise / wire fraud. NoTap could provide:

```
1. Email with payment instructions
2. User logs into NoTap portal
3. Completes 3+ factors (including behavioral)
4. NoTap Seal generated for the transaction
5. Payment instruction cryptographically signed
6. Receiving bank verifies signature
```

---

## 15. Current Fraud Detection in SDK (7 Strategies)

### Implementation: `merchant/src/commonMain/kotlin/com/zeropay/merchant/fraud/FraudDetector.kt`

The SDK ships with **FraudDetectorComplete** — a production fraud detection engine with 7 strategies:

| # | Strategy | Risk Score | Mechanism |
|---|----------|-----------|-----------|
| 1 | **Velocity** | 0-65 | Per-minute/hour/day attempt thresholds |
| 2 | **Geolocation** | 0-90 | Haversine distance → impossible travel detection |
| 3 | **Device Fingerprint** | 0-150 | New device, blacklisted, multi-user, velocity |
| 4 | **Behavioral** | 0-85 | Completion time, typing speed, historical deviation |
| 5 | **IP Reputation** | 0-155 | Blacklisted IPs, velocity, shared IP, proxy detection |
| 6 | **Time-of-Day** | 0-15 | Historical hour deviation (UTC, needs timezone fix) |
| 7 | **Transaction Amount** | 0-65 | vs. historical average, rapid high-value detection |

### Risk Scoring

| Score | Level | Action |
|-------|-------|--------|
| 0-30 | LOW | Normal verification (2 factors) |
| 31-70 | MEDIUM | Challenge with extra factor (3 factors) |
| 71-100 | HIGH | Block transaction |

### Risk-Based Factor Count Applied in VerificationManager

```kotlin
// merchant/src/commonMain/kotlin/com/zeropay/merchant/config/MerchantConfig.kt
fun getRequiredFactorCount(riskLevel, transactionAmount): Int {
    if (riskLevel == HIGH || amount >= HIGH_RISK_THRESHOLD) return 3
    if (riskLevel == MEDIUM || amount >= MEDIUM_RISK_THRESHOLD) return 2
    return 2  // minimum
}
```

### Current Gaps in the SDK Fraud Detector

| Gap | Impact | Priority |
|-----|--------|----------|
| **Timezone handling** | Time-of-day strategy uses UTC → false positives across timezones | MEDIUM |
| **In-memory state only** | Stats lost on restart. No Redis/DB persistence. | HIGH |
| **No ML/adaptive thresholds** | Static thresholds may not adapt to user patterns | MEDIUM |
| **Proxy detection is minimal** | Just checks private IP ranges | LOW |
| **No cross-user network analysis** | Can't detect coordinated fraud rings | MEDIUM |
| **No behavioral baseline per factor type** | Single profile for all factors | LOW |

---

## 16. Gaps & Recommendations

### Critical (Immediate)

| # | Gap | Recommendation |
|---|-----|---------------|
| C1 | **No FIDO2/WebAuthn integration** | AiTM phishing is the #1 MFA bypass in 2026. NoTap should support passkeys as a verification factor or as a transport. |
| C2 | **Fraud detector data is in-memory only** | Migrate fraud state to Redis/DB for persistence across restarts and distributed deployment. |

### High (Short-term)

| # | Gap | Recommendation |
|---|-----|---------------|
| H1 | **Timezone handling in fraud detection** | Add timezone to Location data class; convert to local time for time-of-day analysis. |
| H2 | **Seal adoption by merchants** | Build automated seal submission to acquirers/networks (Visa Claim Resolution, Mastercard) for chargeback defense. |
| H3 | **Cross-user device/IP network analysis** | Detect fraud rings sharing devices/IPs using graph analysis. |
| H4 | **Adaptive fraud thresholds** | Instead of static thresholds, learn per-user baseline and adjust dynamically. |

### Medium (Medium-term)

| # | Gap | Recommendation |
|---|-----|---------------|
| M1 | **Machine learning integration** | Add ML-based anomaly detection using factor completion patterns, transaction sequences, and behavioral profiles. |
| M2 | **Real-time fraud signal sharing** | Implement PSD3/PSR fraud signal sharing between PSPs (legal basis now clear in EU). |
| M3 | **Agent commerce audit trail** | Extend seal to include agent_id for agent-initiated transactions. |
| M4 | **Continuous authentication** | After initial verification, periodically re-challenge for long sessions (e.g., high-value checkout flows). |

### Low (Long-term)

| # | Gap | Recommendation |
|---|-----|---------------|
| L1 | **Quantum-resistant algorithms** | Plan migration to lattice-based crypto for ZK proofs. |
| L2 | **Decentralized identity (DID)** | Integrate with W3C DIDs for cross-platform identity. |
| L3 | **Biometric liveness detection** | Add liveness checks for Voice/Face factors. |
| L4 | **Synthetic identity detection** | Add enrollment-time signals for synthetic identity detection. |
| L5 | **Refund/policy abuse authentication** | Extend NoTap Seal to prove refund requests are user-authorized. |

---

## Summary: NoTap's Fraud Mitigation Scorecard

| Category | Coverage | Key Strength |
|----------|----------|-------------|
| **SMS/OTP/SIM Swap** | ✅✅✅ | Eliminated at architectural level |
| **Chargebacks** | ✅✅✅ | NoTap Seal = cryptographic evidence |
| **Account Takeover** | ✅✅✅ | 15 factors + rate limiting + escalation |
| **Phishing** | ✅✅ | Diversity + behavioral factors |
| **Brute Force** | ✅✅✅ | Rate limiting + crypto entropy |
| **Replay** | ✅✅✅ | Nonce + timestamp + session lock (verified) |
| **Insider Threat** | ✅✅✅ | Zero-knowledge architecture |
| **AiTM MFA Bypass** | ⚠️✅ | Partially resistant; needs FIDO2 integration |
| **Agentic Commerce** | ✅✅ | MCP server + A2A framework |
| **First-Party Fraud** | ✅✅✅ | Seal = non-repudiation |
| **Synthetic Identity** | ⚠️ | Integration-dependent |
| **Policy Abuse** | ❌ | Not addressed (extendable) |

**Overall**: NoTap's architecture inherently eliminates several major fraud categories (SIM swap, SMS OTP interception, credential stuffing) that plague traditional auth systems. The **NoTap Seal** is a unique differentiator for chargeback defense — the #1 growing fraud cost for merchants. The main gap in 2026 is **FIDO2/WebAuthn integration** for AiTM phishing resistance, which would bring it to parity with the strongest available authentication.
