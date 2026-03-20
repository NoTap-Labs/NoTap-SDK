# Agent Integration — Compliance, Data Handling & Risk Assessment

**Feature:** Agentic Payment Framework Integration (MCP Server + A2A Agent Card + Async Auth Flow)
**Status:** Pre-Implementation Compliance Review
**Date:** 2026-03-16
**Version:** v3.21.0 (planned)
**Review Required By:** Compliance Team, DPO

---

## 1. Feature Summary

NoTap is adding an **agent integration layer** that allows AI agents (ChatGPT, Claude, Perplexity, etc.) and PSP/merchant systems to discover and use NoTap authentication via open protocols (MCP, A2A).

### What This Feature IS

- A framework for external agents to **initiate authentication** on behalf of users
- A discovery mechanism (MCP tools, A2A Agent Card) so agents find NoTap automatically
- A webhook system so agents get notified when users complete authentication
- An agent registration system so developers can manage agent credentials and scopes

### What This Feature IS NOT

- NOT a payment processor — NoTap never touches money, cards, or bank accounts
- NOT an agentic payment system — agents bring their own payment rails
- NOT a user data aggregator — agents receive only auth results (session_id, seal_id, boolean)
- NOT bypassing any existing security, privacy, or regulatory requirements

### Regulatory Scope

NoTap remains an **authentication-only service**. This feature does not change our regulatory classification:
- We do NOT fall under PSD2/PSD3 payment processing requirements
- We DO comply with GDPR (data minimization, retention, right to erasure)
- We DO comply with BIPA (biometric consent for Illinois residents)
- We DO comply with CCPA, PIPEDA, LGPD (via jurisdiction detection)

---

## 2. Data Handling Matrix

### 2.1 Data That ENTERS NoTap (From Agents)

| # | Data Point | Source | Format | Validation | Purpose |
|---|-----------|--------|--------|------------|---------|
| D1 | Agent API Key | HTTP `Authorization: Bearer` header | 64 hex chars | SHA-256 hash comparison (constant-time) | Authenticate agent request |
| D2 | User Identifier | Request body `user_identifier` | UUID / alias / SNS name | Format validation, enrollment existence check | Identify which user to authenticate |
| D3 | Merchant ID | Request body `merchant_id` | UUID | Format validation, delegation scope check | Associate auth with merchant |
| D4 | Transaction Amount | Request body `transaction_amount` | Numeric (0-999999.99) | Range validation, delegation limit check | Risk-based factor selection |
| D5 | Callback URL | Request body `callback_url` | HTTPS URL | **SSRF protection middleware** (blocks private IPs, metadata endpoints, dangerous ports) | Where to send auth completion webhook |
| D6 | Callback Secret | Request body `callback_secret` | String (32-128 chars) | Length validation | HMAC signing key for webhook payloads |
| D7 | Context Metadata | Request body `context` | JSON object (max 1KB) | **Input sanitization** (strip `<>`, `javascript:`, SQL keywords) | Agent-provided transaction context |
| D8 | Nonce | HTTP `X-Nonce` header | UUID v4 | Format + uniqueness check (Redis, 5-min TTL) | Replay protection |
| D9 | Timestamp | HTTP `X-Timestamp` header | Unix ms / ISO 8601 | 5-minute window, 30s future tolerance | Replay protection |
| D10 | IP Address | TCP connection | IPv4/IPv6 | Format validation | Rate limiting, fraud detection |

### 2.2 Data That NoTap STORES

| # | Data Point | Storage | Encrypted? | Anonymized? | Retention | Cleanup Method |
|---|-----------|---------|-----------|-------------|-----------|----------------|
| S1 | Agent Registration (agent_id, name, type, config) | PostgreSQL `agent_registrations` | No (non-sensitive config) | No | Until deactivated + 30 days | Soft delete → hard delete via `dataRetentionCleanup.js` |
| S2 | Agent API Key Hash | PostgreSQL `api_keys` (via APIKeyService) | One-way SHA-256 hash | Yes (irreversible) | Until key revoked + 30 days | Existing key cleanup |
| S3 | Agent API Key Prefix | PostgreSQL `api_keys` | No (first 8 chars only) | Partial | Same as S2 | Same as S2 |
| S4 | Delegation Config (allowed_ops, merchants, max_amount) | PostgreSQL `agent_registrations` | No (policy config) | No | Same as S1 | Same as S1 |
| S5 | Auth Session (session_id, factors, status) | Redis | No (ephemeral) | No | **5 minutes** (Redis TTL) | Auto-expire |
| S6 | Callback URL (per-session) | Redis (within session) | No (needed for HTTP call) | No | **5 minutes** (Redis TTL) | Auto-expire |
| S7 | Callback Secret (per-session) | Redis (within session) | **Yes — AES-256-GCM** via `encryption.js` | No | **5 minutes** (Redis TTL) | Auto-expire + memory wipe after HMAC |
| S8 | Default Callback URL | PostgreSQL `agent_registrations` | No | No | Same as S1 | Same as S1 |
| S9 | User Identifier Hash (usage log) | PostgreSQL `agent_usage_log` | Salted SHA-256 (`PRIVACY_APP_SALT`) | **Yes (irreversible)** | **30 days** | `dataRetentionCleanup.js` daily 2:00 AM UTC |
| S10 | IP Address Prefix (usage log) | PostgreSQL `agent_usage_log` | No | **Yes (3 octets only)** via `anonymizeIP()` | **30 days** | `dataRetentionCleanup.js` daily 2:00 AM UTC |
| S11 | Operation + Result (usage log) | PostgreSQL `agent_usage_log` | No | No | **30 days** | `dataRetentionCleanup.js` |
| S12 | Callback Delivery Log | PostgreSQL `agent_callback_deliveries` | No | No | **30 days** | `dataRetentionCleanup.js` |
| S13 | Nonce (replay protection) | Redis `nonce:{uuid}` | No | No | **5 minutes** (Redis TTL) | Auto-expire |
| S14 | Audit Events | PostgreSQL `audit_log` | No (secrets auto-redacted by `AuditService`) | IP anonymized | **90 days** | `dataRetentionCleanup.js` |

### 2.3 Data That LEAVES NoTap (To Agents via Callback)

| # | Data Point | When | Format | Contains PII? |
|---|-----------|------|--------|---------------|
| O1 | Event Type | Auth completion | String: `agent.auth.completed` / `agent.auth.failed` | No |
| O2 | Session ID | Auth completion | UUID (already known to agent) | No |
| O3 | Seal ID | Auth success only | UUID reference to ZK proof | No |
| O4 | Authenticated | Auth completion | Boolean: `true` / `false` | No |
| O5 | Timestamp | Auth completion | ISO 8601 | No |
| O6 | HMAC Signature | Every callback | `X-NoTap-Signature` header (hex) | No |

### 2.4 Data That NoTap Explicitly Does NOT Collect or Expose

| Data | Why Not | Enforcement |
|------|---------|-------------|
| Factor inputs (PIN digits, emoji selections, patterns) | Agents never participate in factor challenge — user interacts directly with NoTap | No API endpoint exposes this |
| Factor digests (SHA-256 hashes of inputs) | Internal to verification service only | Not included in any agent-facing response schema |
| Biometric data (face, fingerprint, voice) | Never leaves verification session | Not accessible via any agent endpoint |
| Payment information (card numbers, bank accounts, crypto wallets) | NoTap is auth-only | No payment fields in any schema |
| User PII (name, email, phone, address) | Not needed for auth — UUIDs and aliases only | `verify-compliance.sh` Check 11 blocks required PII fields |
| User location (GPS, precise coordinates) | Jurisdiction check uses coarse data (country/state), ephemeral | `jurisdictionService.js` discards after check |
| Full IP address | Truncated to 3 octets before any storage | `privacyUtils.anonymizeIP()` enforced |
| Factor count or factor names | Default response is `{enrolled: boolean}` only | Extended data requires explicit `agent:read_extended` permission |
| Callback response bodies from agents | Not stored (only HTTP status code + attempt count) | Schema excludes response_body field |

---

## 3. Legal Basis Matrix (GDPR Article 6)

| Data Point | Legal Basis | Justification | Data Subject Rights |
|-----------|-------------|---------------|---------------------|
| S1: Agent Registration | Art. 6(1)(b) — Contract | Developer registers agent as part of service contract | Right to erasure (Art. 17), portability (Art. 20) |
| S2: API Key Hash | Art. 6(1)(b) — Contract | Required for authenticating agent requests | Right to erasure (revoke key) |
| S4: Delegation Config | Art. 6(1)(b) — Contract | Developer configures agent permissions | Right to rectification (Art. 16), erasure (Art. 17) |
| S5-S7: Auth Session | Art. 6(1)(b) — Contract | Required to complete authentication flow | Auto-deletes in 5 minutes |
| S9: User ID Hash | Art. 6(1)(f) — Legitimate Interest | Usage analytics, billing, fraud detection | Right to object (Art. 21) — hash is irreversible, no individual identification possible |
| S10: IP Prefix | Art. 6(1)(f) — Legitimate Interest | Rate limiting, fraud detection | Right to object — anonymized (3 octets), no individual identification |
| S11: Operation + Result | Art. 6(1)(f) — Legitimate Interest | Service quality, billing | Right to erasure (30-day auto-delete) |
| S14: Audit Events | Art. 6(1)(c) — Legal Obligation | GDPR Art. 30 requires processing activity records | Immutable (write-once), 90-day retention |

### Legitimate Interest Assessment (Art. 6(1)(f)) — S9, S10, S11

| Criterion | Assessment |
|-----------|------------|
| **Legitimate interest** | Preventing fraud, ensuring service quality, accurate billing |
| **Necessity** | Cannot detect abuse without usage patterns; cannot bill without operation counts |
| **Balancing test** | Data is anonymized (salted hash, truncated IP) — individual identification is impossible. User privacy impact is minimal. Interest outweighs risk. |
| **Safeguards** | 30-day retention, anonymization, no cross-referencing with PII |

---

## 4. Regulatory Compliance Matrix

### 4.1 GDPR Compliance

| GDPR Article | Requirement | How We Comply | Enforcement |
|-------------|-------------|---------------|-------------|
| Art. 5(1)(a) — Lawfulness | Legal basis for each data point | Documented in Section 3 above | DPO review |
| Art. 5(1)(b) — Purpose Limitation | No repurposing collected data | Agent data used only for auth + billing | `verify-compliance.sh` Check 10 blocks analytics/tracking |
| Art. 5(1)(c) — Data Minimization | Collect only what's necessary | Enrollment info returns only `{enrolled: boolean}` by default | Code review, extended data requires explicit permission |
| Art. 5(1)(d) — Accuracy | Keep data accurate | Agent config updatable via PUT endpoint | Developer self-service |
| Art. 5(1)(e) — Storage Limitation | Retention limits on all data | 5-min sessions, 30-day usage logs, 90-day audit | Redis TTL + `dataRetentionCleanup.js` |
| Art. 5(2) — Accountability | Document compliance | This document | DPO + compliance team review |
| Art. 6 — Legal Basis | Each processing activity has basis | Matrix in Section 3 | DPO review |
| Art. 13/14 — Transparency | Inform data subjects | Developer docs describe data collected | MCP README + API docs |
| Art. 17 — Right to Erasure | Delete on request | Agent deactivation deletes registration + revokes keys | DELETE endpoint + cleanup job |
| Art. 20 — Portability | Export data in machine-readable format | GET /v1/developer/agents/:id returns full config as JSON | Existing endpoint |
| Art. 25 — Privacy by Design | Anonymization, minimization baked in | Salted hashes, IP truncation, encrypted secrets | `privacyUtils.js`, `encryption.js` |
| Art. 30 — Records of Processing | Maintain processing activity records | Audit log (immutable, 90-day retention) | `AuditService.js` |
| Art. 32 — Security of Processing | Appropriate technical measures | AES-256-GCM encryption, HMAC signatures, rate limiting, SSRF protection | Middleware chain on all endpoints |
| Art. 33/34 — Breach Notification | Notify within 72 hours | Audit trail enables rapid breach identification | `AuditService.getIPHistory()` for forensics |

### 4.2 BIPA Compliance (Illinois)

| BIPA Section | Requirement | How We Comply | Enforcement |
|-------------|-------------|---------------|-------------|
| 740 ILCS 14/15(a) — Retention | Written policy for destruction | 24h factor data TTL, 3-year biometric consent record | Redis TTL, `dataRetentionCleanup.js` |
| 740 ILCS 14/15(b) — Consent | Written consent before biometric collection | `AgentAuthService.initiateAgentAuth()` checks `jurisdictionService` → requires valid `bipa_consents` record for IL users before allowing biometric factors | **Hard block — no override, no bypass** |
| 740 ILCS 14/15(c) — No Sale | Cannot sell/trade biometric data | NoTap never sells any data | Privacy policy, no data broker integrations |
| 740 ILCS 14/15(d) — Safeguards | Reasonable security measures | AES-256-GCM, constant-time comparison, memory wipe | Existing security infrastructure |
| 740 ILCS 14/15(e) — Transparency | Inform of collection | BIPA consent step with 6 disclosure cards | Existing `BIPAConsentStep.kt` + `bipaConsentRouter.js` |

**Critical Control:** If an agent initiates auth for an Illinois user with biometric factors (FACE, FINGERPRINT, VOICE) and no BIPA consent record exists, the request is **rejected with HTTP 451 (Unavailable for Legal Reasons)** and a link to the consent enrollment flow. Agents CANNOT bypass this.

### 4.3 CCPA Compliance (California)

| CCPA Right | How We Comply |
|-----------|---------------|
| Right to Know | Developer docs + this compliance document |
| Right to Delete | Agent deactivation + 30-day hard delete |
| Right to Opt-Out of Sale | We never sell data |
| Right to Non-Discrimination | No service degradation for privacy choices |

### 4.4 PIPEDA Compliance (Canada)

| PIPEDA Principle | How We Comply |
|-----------------|---------------|
| Accountability | DPO oversight, this compliance document |
| Identifying Purposes | Data handling matrix (Section 2) |
| Consent | Implied consent for contractual necessity (agent registration) |
| Limiting Collection | Data minimization (Section 2.4) |
| Limiting Use | Purpose limitation (auth + billing only) |
| Accuracy | Developer self-service update endpoints |
| Safeguards | Encryption, rate limiting, SSRF protection |
| Openness | Public API documentation |
| Individual Access | GET endpoints for agent config + usage |
| Challenging Compliance | partners@notap.io for complaints |

---

## 5. Security Controls Matrix

### 5.1 Endpoint Security

| Endpoint | Auth | Rate Limit | Replay Protection | SSRF Check | Audit Log | Input Sanitization |
|----------|------|-----------|-------------------|-----------|-----------|-------------------|
| POST /v1/developer/agents | Developer JWT | 10/min per developer | Nonce + Timestamp | callback_url_default | agent.registered | Auto (security.js) |
| GET /v1/developer/agents | Developer JWT | 50/min per developer | N/A (GET) | N/A | N/A (read-only) | Auto |
| PUT /v1/developer/agents/:id | Developer JWT | 10/min per developer | Nonce + Timestamp | callback_url_default | agent.updated | Auto |
| DELETE /v1/developer/agents/:id | Developer JWT | 5/min per developer | Nonce + Timestamp | N/A | agent.deleted | Auto |
| POST /v1/agent/auth/initiate | Agent API Key | 20/min per agent | Nonce + Timestamp | callback_url | agent.auth.initiated | Auto |
| GET /v1/agent/auth/status/:id | Agent API Key | 60/min per agent | N/A (GET) | N/A | N/A (read-only) | Auto |
| POST /v1/agent/auth/cancel/:id | Agent API Key | 10/min per agent | Nonce + Timestamp | N/A | agent.auth.cancelled | Auto |
| GET /v1/agent/enrollment/info/:id | Agent API Key | 30/min per agent | N/A (GET) | N/A | N/A (read-only) | Auto |
| GET /.well-known/agent.json | None (public) | Global rate limit | N/A | N/A | N/A | N/A (static file) |

### 5.2 Data-at-Rest Security

| Data | Storage | Encryption | Access Control |
|------|---------|-----------|----------------|
| Agent registrations | PostgreSQL | Disk encryption (infrastructure) | Developer ID ownership check |
| API key hashes | PostgreSQL | One-way SHA-256 (irreversible) | Never exposed via API |
| Callback secrets (session) | Redis | AES-256-GCM (application-level) | Session TTL (5 min), memory wipe after use |
| Auth sessions | Redis | Not encrypted (ephemeral, 5-min TTL) | Session ID knowledge required |
| Usage logs | PostgreSQL | Disk encryption (infrastructure) | Developer ID ownership check |
| Audit logs | PostgreSQL | Disk encryption (infrastructure) | Admin-only access, immutable |

### 5.3 Data-in-Transit Security

| Channel | Protocol | Authentication | Integrity |
|---------|----------|---------------|-----------|
| Agent → NoTap API | HTTPS (TLS 1.3) | API key in Bearer header | TLS + nonce replay protection |
| NoTap → Agent Callback | HTTPS (TLS 1.3) | HMAC-SHA256 signature | X-NoTap-Signature header |
| User → NoTap Auth UI | HTTPS (TLS 1.3) | Session ID | TLS + factor challenge |

### 5.4 Existing Middleware Applied to All Agent Routes

| Middleware | File | What It Does |
|-----------|------|-------------|
| Security headers | `security.js` | X-Frame-Options, X-Content-Type-Options, CSP, HSTS |
| Input sanitization | `security.js` | Strips `<>`, `javascript:`, event handlers from all inputs |
| SQL injection detection | `security.js` | Blocks SQL keywords in body/query params |
| Rate limiting (global) | `rateLimitMiddleware.js` | 1000 req/min global, 100/min per IP |
| Rate limiting (per-key) | `apiKeyValidator.js` | 100 req/min per API key |
| Replay protection | `replayProtection.js` | UUID v4 nonce + 5-min timestamp window |
| SSRF protection | `ssrfProtection.js` | Blocks private IPs, cloud metadata, dangerous ports |
| Audit auto-redaction | `AuditService.js` | Strips password, token, secret, key, digest from logs |

---

## 6. Risk Assessment

### 6.1 Threat Model

| Threat | Likelihood | Impact | Mitigation | Residual Risk |
|--------|-----------|--------|------------|---------------|
| **Stolen agent API key** | Medium | High — attacker can initiate auth sessions | Per-agent rate limits (20/min), delegation scope limits (merchant, amount), key rotation endpoint, usage monitoring | Low — attacker can only initiate (never complete) auth, no access to factor inputs |
| **SSRF via callback_url** | Medium | Critical — internal service compromise | SSRF middleware blocks private IPs, metadata endpoints, dangerous ports; HTTPS required in production | Low — validated against comprehensive blocklist |
| **Callback forgery (impersonation)** | Medium | High — agent acts on fake auth result | HMAC-SHA256 signature on every callback, per-session secret | Low — requires secret knowledge |
| **Replay attack on callback** | Low | Medium — duplicate payment trigger | Session ID is single-use, callback includes unique timestamp | Very Low |
| **Enumeration via enrollment info** | Medium | Medium — discover who is enrolled | Default returns only `{enrolled: boolean}`, no factor count | Low — binary signal only |
| **BIPA consent bypass via agent** | Low | Critical — $5,000/violation regulatory fine | Hard block in `AgentAuthService` — checks `jurisdictionService` + `bipa_consents` table before allowing biometric factors. No override flag. | Very Low — architectural enforcement |
| **Agent data breach** | Low | Medium — agent configs exposed | Hashed API keys, no PII in agent tables, encrypted callback secrets | Low — minimal sensitive data stored |
| **DDoS via agent auth initiation** | Medium | Medium — service degradation | Multi-layer rate limiting (global, IP, key, agent-specific) | Low — multiple defense layers |
| **Man-in-the-middle on callback** | Low | High — intercepted auth results | HTTPS required, HMAC signature for integrity | Very Low |
| **Cross-agent session access** | Low | High — agent reads another agent's session | Session ownership enforced via `secureCompare(session.agentDeveloperId, req.agent.developerId)` | Very Low — constant-time comparison |

### 6.2 Blast Radius Analysis

| Scenario | Affected Scope | Contained By |
|----------|---------------|-------------|
| Single agent key compromised | Only that agent's allowed merchants + amount limit | Delegation scope, rate limits, key revocation |
| Agent registration DB table leaked | Agent names, types, configs (no keys, no PII) | Keys are hashed, no user data in table |
| Callback delivery log leaked | Session IDs, HTTP status codes (no payloads, no PII) | Minimal data stored, 30-day retention |
| Redis instance compromised | Active sessions (5-min window), encrypted callback secrets | Short TTL limits exposure window, secrets AES-encrypted |
| Full backend compromise | Everything | Beyond scope of this feature — existing security posture applies |

### 6.3 Regulatory Fine Exposure

| Regulation | Maximum Fine | Our Exposure | Rationale |
|-----------|-------------|-------------|-----------|
| GDPR | 4% of annual global turnover or €20M | **Minimal** | Data minimization enforced, retention limits set, right to erasure implemented |
| BIPA | $5,000 per intentional violation, $1,000 per negligent | **Minimal** | Hard block on biometric factors without consent — architectural, not policy |
| CCPA | $7,500 per intentional violation | **Minimal** | We don't sell data, deletion implemented |
| PIPEDA | $100,000 CAD per violation | **Minimal** | Consent model, data minimization, safeguards in place |

---

## 7. Compliance Verification Checklist

### Pre-Implementation (This Document)

- [x] Data handling matrix complete (Section 2)
- [x] Legal basis for each data point identified (Section 3)
- [x] Regulatory compliance mapped (Section 4)
- [x] Security controls mapped to endpoints (Section 5)
- [x] Risk assessment with mitigations (Section 6)
- [ ] DPO review and sign-off
- [ ] Compliance team review and sign-off

### During Implementation

- [ ] All POST/PUT/DELETE endpoints have replay protection (X-Nonce + X-Timestamp)
- [ ] All endpoints accepting URLs have SSRF middleware
- [ ] Callback secrets encrypted with AES-256-GCM before Redis storage
- [ ] Callback secrets memory-wiped after HMAC computation
- [ ] User identifiers hashed with `PRIVACY_APP_SALT` before usage log storage
- [ ] IP addresses truncated to 3 octets via `anonymizeIP()` before storage
- [ ] BIPA check in `AgentAuthService.initiateAgentAuth()` — rejects biometric factors for IL users without consent
- [ ] Audit events logged for all state-changing agent operations
- [ ] Agent name validation: alphanumeric + spaces + hyphens, max 100 chars
- [ ] Agent context metadata: max 1KB, sanitized
- [ ] `dataRetentionCleanup.js` updated with: agent_usage_log (30d), agent_callback_deliveries (30d)
- [ ] All new env vars added to: .env.example, .env.test, .env.test.example, .env.local, .env.local.example, ci-cd.yml
- [ ] `verify-compliance.sh` passes all 18 checks with new code

### Post-Implementation

- [ ] Tests cover: SSRF rejection, BIPA block, session scoping, rate limiting, replay protection
- [ ] 80%+ code coverage on new files
- [ ] Pre-push agent (`./scripts/agent @all`) passes with 0 blocking violations
- [ ] Penetration test scenarios added for agent endpoints
- [ ] task.md + planning.md updated

---

## 8. Data Flow Diagram

```
WHAT ENTERS NOTAP (from agents):
  ┌────────────────────────────────────────────────────────────────┐
  │ Agent API Key ───► Hash comparison (constant-time), never stored raw │
  │ User Identifier ──► Hash with PRIVACY_APP_SALT before logging       │
  │ Transaction Amt ──► Stored in usage log (30-day retention)          │
  │ Callback URL ─────► SSRF-validated, stored in session (5-min TTL)  │
  │ Callback Secret ──► AES-256-GCM encrypted in Redis (5-min TTL)    │
  │ IP Address ───────► Truncated to 3 octets before any storage       │
  │ Nonce ────────────► UUID v4, single-use, 5-min Redis TTL           │
  │ Timestamp ────────► Validated (5-min window), not stored            │
  └────────────────────────────────────────────────────────────────┘

WHAT LEAVES NOTAP (to agents via callback):
  ┌────────────────────────────────────────────────────────────────┐
  │ Event Type ──────► "agent.auth.completed" (string literal)         │
  │ Session ID ──────► UUID (already known to agent from initiation)   │
  │ Seal ID ─────────► UUID reference to ZK proof (if auth succeeded)  │
  │ Authenticated ───► Boolean: true / false                           │
  │ Timestamp ───────► ISO 8601 of completion time                     │
  │ HMAC Signature ──► X-NoTap-Signature header (integrity check)     │
  └────────────────────────────────────────────────────────────────┘

WHAT NEVER LEAVES NOTAP:
  ┌────────────────────────────────────────────────────────────────┐
  │ ✗ Factor inputs (PIN, emoji, pattern, etc.)                        │
  │ ✗ Factor digests (SHA-256 hashes)                                  │
  │ ✗ Biometric data (face, fingerprint, voice)                        │
  │ ✗ User PII (name, email, phone, address)                          │
  │ ✗ Payment details (card numbers, bank accounts)                    │
  │ ✗ Full IP addresses                                                │
  │ ✗ User location (GPS, precise coordinates)                         │
  │ ✗ Factor count or names (unless extended permission)               │
  └────────────────────────────────────────────────────────────────┘
```

---

## 9. Retention Schedule Summary

| Data Category | Storage | Retention | Deletion Method | Legal Basis for Retention |
|--------------|---------|-----------|-----------------|--------------------------|
| Agent registration | PostgreSQL | Active + 30 days after deactivation | Soft delete → hard delete (cleanup job) | Contract |
| Auth session | Redis | 5 minutes | Redis TTL (auto-expire) | Contract |
| Callback secret | Redis (encrypted) | 5 minutes | Redis TTL + memory wipe | Contract |
| Usage log | PostgreSQL | 30 days | Batch delete (cleanup job, daily 2 AM UTC) | Legitimate interest |
| Callback delivery log | PostgreSQL | 30 days | Batch delete (cleanup job, daily 2 AM UTC) | Legitimate interest |
| Audit log | PostgreSQL | 90 days + 7 days grace | Batch delete (cleanup job, daily 2 AM UTC) | Legal obligation (GDPR Art. 30) |
| Nonce | Redis | 5 minutes | Redis TTL (auto-expire) | Security (replay protection) |

---

## 10. Approval & Sign-Off

| Role | Name | Date | Status |
|------|------|------|--------|
| Author | Engineering | 2026-03-16 | Complete |
| DPO | | | Pending |
| Compliance | | | Pending |
| Security | | | Pending |
| Legal | | | Pending |

---

**Document Version:** 1.0
**Next Review:** After implementation, before deployment to production
