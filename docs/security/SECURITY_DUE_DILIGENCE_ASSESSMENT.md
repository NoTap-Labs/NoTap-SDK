# SECURITY DUE DILIGENCE: NoTap/ZeroPay Authentication

## EXECUTIVE SUMMARY

**Verdict: Major gaps between marketing claims and actual implementation.**

---

## 🔴 CRITICAL FINDINGS

### 1. ZK-SNARK IS THEATER, NOT CRYPTOGRAPHY

**What they claim:** "Zero-knowledge proofs for privacy-preserving authentication"

**What's actually running:**
```javascript
// backend/services/zkProofService.js:69
console.warn('⚠️ ZK-SNARK proof generation using placeholder (trusted setup pending)');
```

The circuit exists (`circuits/factor_auth.circom`) but:
- ❌ Never compiled (no `build/` directory)
- ❌ No trusted setup (no `.zkey` file)
- ❌ Backend returns mock data: `pi_a: ['0x1', '0x2']`

**Impact:** The "privacy-preserving audit trail" and "ZK proof" security guarantees are fictional. Current implementation provides ZERO privacy protection.

---

### 2. TIMING ATTACK VULNERABILITY IN PROD CODE

**File:** `merchant/src/commonMain/kotlin/.../DigestComparator.kt:79`
```kotlin
println("Digest comparison completed in ${executionTime}ms: ${if (result) "MATCH" else "NO MATCH"}")
```

This logs whether digests match, creating a **timing oracle**. Combined with:
- Line 64: `println("Invalid digest size: ...")`
- Line 75: `println("Digest comparison exceeded timeout...")`

An attacker can use timing differences to extract valid factor digests.

---

### 3. 179 INSTANCES OF NON-CONSTANT-TIME COMPARISONS

grep shows extensive use of `.contentEquals()` which is NOT constant-time:
- `sdk/src/commonMain/kotlin/.../VoiceFactor.kt:127`
- `sdk/src/commonMain/kotlin/.../NfcFactor.kt:218`
- `sdk/src/commonMain/kotlin/.../MouseFactor.kt`
- And 175+ more locations

While they have a `ConstantTime` utility, **it's not being used everywhere it matters**.

---

### 4. PENETRATION TEST: NOT FIXED

Per `SECURITY_AUDIT.md` lines 65-117:

| Vulnerability | Status | CVSS |
|--------------|--------|------|
| SSRF | ❌ NOT FIXED (16 vulns) | 9.1 |
| Replay (nonce) | ❌ BROKEN (both valid + replay fail) | 8.1 |
| Race Condition | ✅ Fixed | 7.4 |

The SSRF allows attackers to hit internal services (localhost, 10.x, 172.16.x).

---

### 5. SINGLE POINT OF FAILURE: REDIS

No Redis clustering, sentinel, or read replica configuration found. If Redis goes down:
- All active sessions die
- All factor digests become inaccessible
- Authentication completely stops

---

### 6. UNVERIFIED SCALABILITY

**Claim:** "Handles 1,666 RPS" (from SCALABILITY_ANALYSIS.md)

**Reality:** 
- No load test results in repo
- No k6/Artillery evidence
- The "1,666 RPS" is theoretical calculation, not measured
- Async audit logging still marked as "TODO" in code

---

## ⚠️ ARCHITECTURAL CONCERNS

### Factor Entropy Is Weak

Looking at actual factor implementations:
- **PIN:** 4-12 digits = ~13-40 bits (brute-forceable)
- **Colors:** 3-6 colors from limited palette
- **Emojis:** 3-8 from set of ~50

Even with "combinatorial" selection, the search space is much smaller than claimed. The entropy tests check for "20 unique bytes in 32-byte hash" but this verifies hash quality, not input entropy.

### Comparison to Passkeys

| Property | Passkeys (WebAuthn) | NoTap |
|----------|-------------------|-------|
| Hardware anchor | ✅ Secure Enclave | ❌ None |
| Phishing resistant | ✅ Yes | ❌ No |
| Server trust | ✅ Client-verified | ❌ Server sees all |
| Replay protection | ✅ Cryptographic | ❌ Timestamp-based (broken) |

NoTap is fundamentally weaker than passkeys because the user's device is not a trust anchor.

### Recovery = Complete Re-enrollment

If user forgets factors:
- Standard email password reset works for regular auth
- **No ZeroPay factor recovery mechanism** - they must re-enroll entirely
- This means losing factors = losing account access

---

## 📊 PRIOR ART ANALYSIS

This is NOT novel. It's KBA (Knowledge-Based Authentication) with more factors:

| Concept | Prior Art |
|---------|-----------|
| Multiple knowledge factors | Banks use security questions + ATM PIN for decades |
| Behavioral biometrics | Visa Deep Authenticate, IBM Trusteer |
| Device-free auth | Matches's magic link, Apple Watch unlock |
| Combinatorial selection | FICO Score, ThreatMetrix |

A competent team could clone this in 3-6 months.

---

## 🔐 WHAT IS REAL

### Good:
- PBKDF2: 100,000 iterations ✅ (OWASP compliant)
- AES-256-GCM encryption ✅
- Memory wiping (multi-pass overwrite) ✅
- Constant-time comparison utility exists ✅
- Double encryption architecture (KMS + PBKDF2) ✅
- HKDF daily key rotation ✅

### Bad:
- ZK proofs = placeholder
- Timing attack in logging
- Non-constant-time comparisons everywhere
- SSRF unfixed
- Replay protection broken

---

## 💀 WORST CASE SCENARIO

If attacker gets Redis dump:
1. All factor digests exposed (encrypted with AES-256, but key derivation uses factors + UUID)
2. With digests + known UUID pattern, offline brute-force possible
3. If KMS compromised → full account takeover
4. ZK proofs provide ZERO protection (they're mock data)

---

## FINAL ASSESSMENT

| Category | Score |
|----------|-------|
| Novelty | 2/10 (KBA repackaged) |
| Security | 4/10 (good crypto, bad implementation) |
| Scalability | 3/10 (untested, SPOF) |
| Privacy (ZK claims) | 0/10 (theater) |
| Production Ready | No |

**This is a demo/prototype, not production infrastructure.** The "security theater" around ZK proofs and the unfixed SSRF/replay vulnerabilities are major red flags for any security-conscious deployment.
