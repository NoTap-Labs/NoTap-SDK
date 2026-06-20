# Security Status Registry

**SINGLE SOURCE OF TRUTH** for all security vulnerability status tracking.

---

## Purpose

This file is the authoritative source for security vulnerability status across the ZeroPay/NoTap codebase. All other security documentation should reference this file for current status.

**Rule:** When in doubt about security status, check THIS FILE FIRST.

---

## Metadata

| Property | Value |
|----------|-------|
| **Last Updated** | 2026-06-17 |
| **Owner** | Security Team |
| **Review Frequency** | Weekly or after any security fix |
| **Location** | `documentation/05-security/SECURITY_STATUS_REGISTRY.md` |

---

## Vulnerability Status Matrix

| ID | Category | Vulnerability | Status | CVSS | Last Tested | Fixed By | Notes |
|----|----------|---------------|--------|------|-------------|----------|-------|
| V001 | Backend | SSRF (16 vulns) | ✅ FIXED | 9.1 | 2026-01-23 | Handler validation | Re-tested with 140 payloads, 0 vulnerabilities |
| V002 | Backend | Replay Attack (nonce) | ✅ FIXED | 8.1 | 2026-01-23 | Dynamic Redis client | Verified: replay returns 403 |
| V003 | Backend | Race Condition | ✅ FIXED | 7.4 | 2026-01-19 | Distributed locks | 0/10 concurrent succeeded post-fix |
| V004 | SDK | Timing Leak (DigestComparator println) | ✅ FIXED | - | 2026-02-16 | Removed println statements | All timing-sensitive logging removed |
| V005 | SDK | Enrollment contentEquals (PatternFactor) | ✅ FIXED | - | 2026-02-16 | Replaced with ConstantTime.equals | Was at enrollment/src/androidMain/.../PatternFactor.kt:54 |
| V006 | SDK | Enrollment contentEquals (EmojiFactor) | ✅ FIXED | - | 2026-02-16 | Replaced with ConstantTime.equals | Was at enrollment/src/androidMain/.../EmojiFactor.kt:53 |
| V007 | SDK | Color Factor Entropy (6 colors) | ✅ FIXED | - | 2026-02-16 | Increased to 12 colors, 4-6 selections | Was 7.8-12.9 bits, now 14.3-21.5 bits |
| V008 | Architecture | Redis SPOF | ✅ FIXED | - | 2026-02-16 | HybridCacheService with PostgreSQL fallback | Uses cacheService abstraction, auto-failover |
| V009 | Backend | ZK-SNARK Placeholder | 🔴 OPEN | - | - | - | All proofs mock data, marked experimental |
| V010 | Architecture | No Factor Recovery | 🔴 OPEN | - | - | - | Forgotten factors = re-enrollment required |
| V011 | Scalability | Unverified Performance | 🔴 OPEN | - | - | - | No load testing performed |
| V012 | Documentation | Stale Status Markers | ✅ FIXED | - | 2026-02-16 | Created this registry | Reconciled contradictory docs |
| V013 | Architecture | Direct Redis Access (no abstraction) | ✅ FIXED | - | 2026-02-16 | All routes now use cacheService | Fixed 7 locations in verificationRouter, enrollmentRouter |
| V014 | Backend | X-Forwarded-For Spoofing (11 locations) | ✅ FIXED | 9.0 | 2026-06-17 | req.ip for all rate limiters/services | server.js has trust proxy: 1; req.ip is correct client IP |
| V015 | Backend | MFA Factor Name Timing/Enumeration | ✅ FIXED | 8.5 | 2026-06-17 | Constant-time factor validation | Buffer.alloc for unknown names + secureCompare |
| V016 | Backend | Lockout Timing Oracle (3 services) | ✅ FIXED | 7.4 | 2026-06-17 | Lockout after bcrypt | Eliminated 200-500ms timing difference |
| V017 | Backend | NoTap Login Timing & Enumeration | ✅ FIXED | 6.5 | 2026-06-17 | getDummyHash baseline + generic messages | Also added lockout tracking to NoTap paths |
| V018 | Backend | Admin Error Disclosure | ✅ FIXED | 5.3 | 2026-06-17 | Unified "Invalid credentials" | Locked/inactive/suspended/general all same message |
| V019 | Backend | Session Replay on /verify | ✅ FIXED | 5.3 | 2026-06-17 | session.status === 'verified' guard | Duplicate submissions blocked |

---

## Status Definitions

| Status | Meaning | Action Required |
|--------|---------|-----------------|
| ✅ FIXED | Verified by test or code review | Update CHANGELOG.md, close ticket |
| ⚠️ MONITORING | Fixed but awaiting re-test | Schedule re-test |
| 🔴 OPEN | Known, not yet fixed | Prioritize in sprint |
| 🟡 IN PROGRESS | Fix under development | Update regularly |
| ❌ WONT FIX | Accepted risk or not applicable | Document rationale |

---

## How to Update This File

### When Finding a NEW Vulnerability

1. Add new row to the Vulnerability Status Matrix
2. Set status to 🔴 OPEN
3. Add to task.md for tracking
4. Create security ticket

### When FIXING a Vulnerability

1. **CRITICAL:** Update this file FIRST (before merging code)
2. Change status from 🔴 OPEN to ✅ FIXED
3. Add "Last Tested" date
4. Add "Fixed By" with brief description
5. Add reference to commit/ticket
6. Update all referencing documents (see below)

### After Updating This File

Run this command to find all documents that reference the fixed vulnerability:
```bash
grep -r "V00[1-9]" documentation/
```

Then update each referencing document to point to this registry.

---

## Canonical References

| Document | What It Contains | When to Update |
|----------|------------------|----------------|
| `SECURITY_STATUS_REGISTRY.md` | **SINGLE SOURCE OF TRUTH** | Immediately on any change |
| `SECURITY_AUDIT.md` | Historical audit findings | Add links to registry, don't duplicate status |
| `COMPLIANCE_AUDIT_STATUS.md` | Compliance test results | Update after re-tests |
| `CHANGELOG.md` | Release notes | Add entry when deploying fixes |
| `planning.md` | Roadmap status | Update when priority changes |
| `task.md` | Active tasks | Update when starting/completing work |

---

## Anti-Patterns (DO NOT DO)

- ❌ Adding new "FIXED" sections without updating original "NOT FIXED" entries
- ❌ Leaving stale status markers ("NOT FIXED", "STILL VULNERABLE") when fixed
- ❌ Having contradictory status across documents (check this file first)
- ❌ Updating CHANGELOG.md without updating this registry
- ❌ Marking as "FIXED" without verification/test evidence

---

## Quick Commands

### Check for stale status markers
```bash
grep -rn "NOT FIXED\|STILL VULNERABLE\|BROKEN" documentation/05-security/
```

### Check all docs reference registry
```bash
grep -rn "SECURITY_STATUS_REGISTRY" documentation/
```

### Verify a specific vulnerability is fixed
```bash
grep "V00X" documentation/05-security/SECURITY_STATUS_REGISTRY.md
```

---

## History

| Date | Change | Author |
|------|--------|--------|
| 2026-06-17 | Fixed 9 auth vulns (X-Forwarded-For, MFA, lockout timing, NoTap, admin errors, session replay) | Security Remediation |
| 2026-02-16 | Created registry, fixed 7 vulnerabilities | Security Remediation |
| 2026-01-23 | SSRF/Replay fixes verified | Pentest |
| 2026-01-19 | Race condition fixed | Security Team |

---

*This file is the single source of truth. All security status claims should be verified against this document.*
