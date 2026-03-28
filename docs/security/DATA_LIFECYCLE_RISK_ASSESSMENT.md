# Data Lifecycle Risk Assessment — Full Security Audit

**Last Updated:** 2026-03-21
**Audit Scope:** End-to-end data protection across all stages of the NoTap authentication lifecycle
**Overall Rating:** 9/10 — Strong architecture with 3 actionable items

---

## Executive Summary

NoTap uses a **zero-knowledge architecture** — nobody (not merchants, not PSPs, not admins, not even the backend) can access raw factor data at any stage. Only irreversible SHA-256 hashes (digests) are ever transmitted or stored. This assessment covers every stage of the data lifecycle: input, transit, storage, verification, device exposure, cross-tenant isolation, deletion, portability, logging, and key management.

---

## 1. Customer Input (Factor Entry)

| Stage | Protection | Status |
|-------|-----------|--------|
| Factor entered on device | SHA-256 hashed client-side before leaving device | PROTECTED |
| Digest in memory (client) | Kotlin/JS `fill(0)` after hashing | PROTECTED |
| Raw factor (PIN, pattern, etc.) | Never leaves the device — only digest transmitted | PROTECTED |

**Risk: LOW** — Raw factors never touch the wire. Only SHA-256 digests are transmitted.

---

## 2. Data In Transit

| Stage | Protection | Status |
|-------|-----------|--------|
| Client to Backend | HTTPS enforced (HSTS 1yr, includeSubDomains, preload) | PROTECTED |
| Backend to Redis | TLS 1.3 in production (port 6380, mTLS optional) | PROTECTED |
| Backend to PostgreSQL | SSL/TLS configurable (`DB_SSL_*` env vars) | PROTECTED |
| Nonce + timestamp | Replay protection (60s window, single-use nonces in Redis) | PROTECTED |

**Risk: LOW** — All transit paths use TLS. Minor gap: no HTTP-to-HTTPS redirect at Express layer (relies on reverse proxy).

---

## 3. Data At Rest (Redis)

| Data | Encryption | TTL | Status |
|------|-----------|-----|--------|
| Enrollment data (`enrollment:{uuid}`) | AES-256-GCM | 24 hours | PROTECTED |
| PSP sessions | cacheService (encrypted) | 5 minutes | PROTECTED |
| Auth tokens (`psp_auth_token:*`) | cacheService (encrypted) | 5 minutes | PROTECTED |
| Rate limit counters | Not sensitive | 1-60 minutes | N/A |

**TTL Enforcement:** `RedisCacheService.set()` throws an error if TTL is missing — GDPR Article 5(1)(e) hardcoded at the infrastructure level.

**Risk: LOW** — Everything encrypted with AES-256-GCM, mandatory TTL on all keys.

---

## 4. Data At Rest (PostgreSQL)

| Data | Protection | Status |
|------|-----------|--------|
| Encryption keys | KMS-wrapped (never plaintext) | PROTECTED |
| Device IDs | SHA-256 hashed with per-user salt | PROTECTED |
| IP addresses | Anonymized to first 3 octets | PROTECTED |
| PSP API keys | SHA-256 hashed (key_hash column) | PROTECTED |
| Merchant passwords | bcrypt hashed | PROTECTED |
| Audit logs | Action + anonymized metadata | PROTECTED |
| Factor digests | NOT stored in PostgreSQL (Redis only, 24h TTL) | N/A |

**Risk: LOW** — Double encryption (PBKDF2 + KMS wrapping). No plaintext secrets in DB.

---

## 5. During Verification (Active Use)

| Stage | Protection | Status |
|-------|-----------|--------|
| Digest comparison | `crypto.timingSafeEqual()` — constant-time | PROTECTED |
| Buffers after comparison | `wipeBuffer()` — 3-pass wipe (zeros, random, zeros) | PROTECTED |
| Factor loop enumeration | `continue` not `break` — prevents oracle attack | PROTECTED |
| Failed verification | `getDummyHash()` computed even for missing users | PROTECTED |

**Risk: LOW** — No timing attacks, no enumeration oracles, memory wiped after use.

---

## 6. On Merchant/PSP Device

| Data | What They Receive | Status |
|------|------------------|--------|
| Auth token | `auth_<64-hex>` — opaque, 5-min TTL | PROTECTED |
| Factor digests | Never returned to merchant/PSP | PROTECTED |
| User UUID | Returned in success metadata | ACCEPTABLE (needed for transaction binding) |
| ZK proof | Cryptographic proof, no user data extractable | PROTECTED |

**Risk: LOW** — Merchants/PSPs only see opaque tokens and ZK proofs, never factor data.

---

## 7. Cross-Tenant Isolation (IDOR)

| Check | Implementation | Status |
|-------|---------------|--------|
| PSP A accessing PSP B's session | `session.psp_id !== req.psp.pspId` returns 404 | PROTECTED |
| Merchant A accessing Merchant B's data | JWT contains `merchantId`, scoped queries | PROTECTED |
| PSP accessing merchant sessions | Different auth mechanisms, different endpoints | PROTECTED |
| Admin accessing user data | Admin JWT required, audit logged | PROTECTED |

**Risk: LOW** — IDOR checks on every session access. Returns 404 (not 403) to prevent information leakage.

---

## 8. Deletion / Right to Erasure (GDPR Art. 17)

| Layer | Deletion Mechanism | Status |
|-------|-------------------|--------|
| Redis enrollment | `cacheService.delete('enrollment:{uuid}')` | IMPLEMENTED |
| PostgreSQL wrapped keys | `DELETE FROM wrapped_keys WHERE uuid = $1` (transactional) | IMPLEMENTED |
| Blockchain enrollment | `blockchainIntegrationService.revokeEnrollment()` | IMPLEMENTED |
| Audit logs | 90-day retention + 7-day grace = 97 days then hard-delete | IMPLEMENTED |
| Agent data | 30-day retention, batch cleanup | IMPLEMENTED |
| BIPA consent | DELETE endpoint + 30-day hard-delete | IMPLEMENTED |

**Endpoint:** `DELETE /v1/enrollment/delete/:uuid`

**Risk: LOW** — Multi-layer deletion with audit trail. Daily cleanup job at 2 AM UTC.

---

## 9. Data Portability (GDPR Art. 20)

| Data | Exportable | Status |
|------|-----------|--------|
| Enrollment metadata | Yes — factor count, timestamps, device ID hash | IMPLEMENTED |
| Audit trail | Yes — all actions with anonymized IPs | IMPLEMENTED |
| Factor digests | No — explicitly excluded (security) | BY DESIGN |
| Format | JSON only | PARTIAL — no CSV/XML alternative |

**Endpoint:** `GET /v1/enrollment/export/:uuid`

**Risk: MEDIUM** — Functional but JSON-only. Factor digests excluded by design (correct — they're irreversible hashes, not "data" the user provided).

---

## 10. Logging and PII Exposure

| Check | Status |
|-------|--------|
| UUIDs in logs | Truncated to 8 chars: `uuid.slice(0, 8)...` |
| IPs in logs | Anonymized: `anonymizeIP(req.ip)` — first 3 octets |
| Device IDs in logs | Hashed: `hashDeviceId(deviceId, uuid)` |
| Factor digests | Never logged |
| API keys | Never logged (redacted by sensitive key list) |
| Passwords | Never logged (redacted) |
| `console.log` | Zero instances — all use structured `logger` |

**Auto-redaction keys:** password, secret, token, key, auth, digest, private, seed, salt, iv, hash

**Risk: LOW** — Comprehensive PII redaction. No raw sensitive data in logs.

---

## 11. Key and Credential Management

| Credential | Storage | Rotation | Revocation |
|-----------|---------|----------|-----------|
| PSP API keys | SHA-256 hash in PostgreSQL | `expires_at` column + `active` flag | Set `active = false` |
| Merchant JWT | In-memory, Redis blacklist | 30-min expiry, `jti` tracking | Blacklist on logout |
| Admin API key | Environment variable | Manual rotation | Server restart |
| Encryption key | AWS KMS (production) | Automatic every 90 days | Key version tracking |
| PRIVACY_APP_SALT | Environment variable | Annual (manual) | N/A |

**Risk: MEDIUM** — PSP key audit trail is missing (no log of create/revoke/rotate events).

---

## 12. Access Control Matrix

```
                    Customer   Merchant   PSP        Admin
Factor digests      NEVER      NEVER      NEVER      NEVER
Raw factors         Device     NEVER      NEVER      NEVER
Auth token          -          Opaque     Opaque     -
ZK proof            -          Yes        -          Yes
Enrollment meta     Export     -          -          Audit
Audit trail         Export     -          -          Full
User UUID           Own        Txn-scoped Txn-scoped All
Device ID           Hashed     -          -          Hashed
IP address          Anonymized -          -          Anonymized
Other users' data   NEVER      NEVER      NEVER      Scoped
PSP sessions        -          -          Own only   All
Merchant sessions   -          Own only   -          All
```

---

## 13. GDPR Article Compliance

| Article | Requirement | Status |
|---------|-------------|--------|
| 5(1)(a) Lawfulness | Legal basis for processing | PARTIAL — BIPA documented, general consent implicit |
| 5(1)(b) Purpose Limitation | Data only for stated purpose | COMPLIANT |
| 5(1)(c) Data Minimization | Collect only necessary data | COMPLIANT |
| 5(1)(e) Storage Limitation | Delete when no longer needed | COMPLIANT — mandatory TTL |
| 15 Right of Access | Provide copy of data | COMPLIANT |
| 17 Right to Erasure | Delete upon request | COMPLIANT |
| 18 Right to Restrict | Restrict processing | NOT IMPLEMENTED |
| 20 Data Portability | Standard format export | PARTIAL — JSON only |
| 32 Security | Technical measures | COMPLIANT |
| 33 Breach Notification | 72h notification | NOT IMPLEMENTED in code |

---

## 14. Risk Summary

| Stage | Risk | Rationale |
|-------|------|-----------|
| Customer input | LOW | SHA-256 on-device, raw factors never leave |
| In transit | LOW | TLS everywhere, replay protection |
| At rest (Redis) | LOW | AES-256-GCM, mandatory TTL |
| At rest (PostgreSQL) | LOW | KMS-wrapped keys, no plaintext |
| During verification | LOW | Constant-time, memory wipe, no oracle |
| On merchant device | LOW | Opaque tokens only |
| Cross-tenant (IDOR) | LOW | psp_id/merchantId scoping on every access |
| Deletion (erasure) | LOW | Multi-layer delete + daily cleanup |
| Portability (export) | MEDIUM | JSON-only, no CSV/XML |
| Logging | LOW | Auto-redaction, no PII |
| Key management | MEDIUM | PSP key audit trail missing |
| GDPR Art. 18 | MEDIUM | No "restrict processing" flag |

---

## 15. Actionable Items

### Item 1: PSP API Key Audit Table (MEDIUM)

**Problem:** No audit trail for PSP key lifecycle events (creation, revocation, rotation).
**Impact:** Cannot trace who created/revoked a key or when, complicating incident response.
**Fix:** Create `psp_api_key_audit` table + log events in pspRouter and admin endpoints.

### Item 2: PRIVACY_APP_SALT Production Enforcement (MEDIUM)

**Problem:** `PRIVACY_APP_SALT` uses a verbose warning string as default. If deployed without changing it, all device ID hashes and signature hashes use a weak, publicly-known salt.
**Impact:** Reduces brute-force resistance of anonymized identifiers.
**Fix:** Remove default value, add startup validation to fail-fast if not set in production.

### Item 3: Data Portability Format (LOW)

**Problem:** `GET /v1/enrollment/export/:uuid` returns JSON only. GDPR Art. 20 recommends "structured, commonly used, machine-readable format."
**Impact:** JSON qualifies as machine-readable, but CSV/XML alternatives improve interoperability.
**Fix:** Add `Accept` header support for CSV export alongside JSON.

---

## Appendix: Encryption Algorithms In Use

| Purpose | Algorithm | Key Size | Standard |
|---------|-----------|----------|----------|
| Symmetric encryption | AES-256-GCM | 256 bits | NIST SP 800-38D |
| Hashing | SHA-256 | 256 bits | FIPS 180-4 |
| Key derivation | PBKDF2 (100K+ iterations) | 256 bits | NIST SP 800-132 |
| Key rotation | HKDF-SHA256 | 256 bits | RFC 5869 |
| Password hashing | bcrypt | Configurable rounds | OpenBSD |
| Random generation | crypto.randomBytes (CSPRNG) | N/A | Node.js crypto |

### Forbidden (Never Use)

- MD5, SHA-1, DES, RC4, ECB mode
- `Math.random()` for any security purpose
- `contentEquals()` for digest comparison
- Hardcoded secrets

---

**Audit performed by:** Claude Code
**Date:** 2026-03-21
**Next review:** Quarterly or after significant architecture changes
