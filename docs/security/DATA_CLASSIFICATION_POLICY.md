# NoTap Data Classification Policy

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-19 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Classification Levels](#2-classification-levels)
3. [Data Categories & Treatment](#3-data-categories--treatment)
4. [Risk Assessment Matrices](#4-risk-assessment-matrices)
5. [Role-Based Access Control](#5-role-based-access-control)
6. [Treatment Requirements](#6-treatment-requirements)
7. [Compliance & Audit](#7-compliance--audit)

---

## 1. Purpose & Scope

### 1.1 Purpose

This policy establishes the data classification framework for NoTap to ensure:

- **Data Minimization**: Only collect and retain necessary data (GDPR Art. 5(1)(c))
- **Purpose Limitation**: Use data only for stated purposes (GDPR Art. 5(1)(b))
- **Security**: Protect data according to sensitivity (GDPR Art. 32)
- **Compliance**: Meet regulatory requirements (GDPR, CCPA, PIPEDA, BIPA, PCI-DSS)

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| User enrollment data | Employee personal devices |
| Authentication factors | Marketing materials |
| Transaction data | Public documentation |
| API logs & audit trails | Third-party vendor data |
| Internal admin data | |

### 1.3 Regulatory Alignment

| Regulation | Alignment |
|------------|-----------|
| GDPR Art. 5(1)(c) | Data Minimization - Classification ensures minimal collection |
| GDPR Art. 5(1)(e) | Storage Limitation - Retention periods by classification |
| GDPR Art. 32 | Security Measures - Treatment based on classification |
| ISO 27002 A.8.11 | Data Masking - Field-level masking implementation |
| ISO 27002 A.8.12 | DLP - Leakage prevention via classification |

---

## 2. Classification Levels

### 2.1 Level Definitions

| Level | Definition | Examples | Harm if Disclosed |
|-------|------------|----------|----------------|
| **RESTRICTED** | Would cause severe harm if disclosed | Passwords, cryptographic keys, raw biometrics | Account compromise, identity theft, regulatory penalty |
| **CONFIDENTIAL** | Would cause harm or penalty if disclosed | PII, financial data, auth tokens | Privacy violation, fraud, regulatory fine |
| **INTERNAL** | Business-sensitive but not PII | IDs, business names, counts | Business intelligence leak, competitive harm |
| **PUBLIC** | Intended for public disclosure | Status codes, API version | None |

### 2.2 Visual Representation

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        DATA CLASSIFICATION PYRAMID                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                           ▲ PUBLIC ▲                                       │
│                          ┌───────────┐                                      │
│                         │  status  │                                         │
│                         │  code    │                                         │
│                         │  version │                                         │
│                          └──���────────┘                                      │
│                                                                             │
│                      ┌───────────────┐                                     │
│                     │  INTERNAL    │                                        │
│                    │  merchant_id  │                                        │
│                    │  uuid        │                                         │
│                    │  created_at   │                                        │
│                     └───────────────┘                                     │
│                                                                             │
│                  ┌─────────────────────┐                                  │
│                 │   CONFIDENTIAL     │                                       │
│                │   email          │                                         │
│                │   phone         │                                         │
│                │   wallet_addr   │                                         │
│                │   token        │                                         │
│                 └─────────────────────┘                                  │
│                                                                             │
│              ┌─────────────────────────────┐                                    │
│             │      RESTRICTED        │                                      │
│            │   password_hash       │                                       │
│            │   api_key          │                                       │
│            │   factor_digest    │                                       │
│            │   pin            │                                       │
│            │   biometric_data  │                                       │
│             └─────────────────────────────┘                                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Data Categories & Treatment

### 3.1 RESTRICTED Data

| Field | Rationale | Legal Basis | Treatment |
|-------|----------|------------|-----------|
| `password` | Authentication | GDPR Art. 4(1) | Never store plaintext |
| `password_hash` | Authentication | GDPR Art. 4(1) | bcrypt, never expose |
| `api_secret` | Authentication | OWASP | Mask in all responses |
| `api_key` | Authentication | OWASP | Mask in all responses |
| `factor_digest` | Authentication | GDPR Art. 4(1) | Mask in all responses |
| `biometric_data` | Biometric | BIPA | Never store, process only |
| `pin` | Authentication | GDPR Art. 4(1) | Never store plaintext |
| `recovery_codes` | Authentication | GDPR Art. 4(1) | One-time use, hash |

### 3.2 CONFIDENTIAL Data

| Field | Rationale | Legal Basis | Treatment | Mask Pattern |
|-------|----------|------------|-----------|-------------|
| `email` | PII | GDPR Art. 4(1) | Mask external | `j***@domain.com` |
| `phone` | PII | GDPR Art. 4(1) | Mask external | `+1*** **** 2345` |
| `wallet_address` | PII | GDPR Art. 4(1) | Mask external | `0x12...abc` |
| `device_id` | PII | GDPR Art. 4(1) | Hash + mask | Hash only |
| `ip_address` | PII | GDPR Art. 4(1) | Anonymize | `203.0.113.0` |
| `access_token` | Auth | GDPR Art. 4(1) | Mask all | Bearer token |
| `full_name` | PII | GDPR Art. 4(1) | Mask external | `J*** S***` |

### 3.3 INTERNAL Data

| Field | Rationale | Legal Basis | Treatment |
|-------|----------|------------|-----------|
| `merchant_id` | Business | Legitimate Interest | Mask external API |
| `user_uuid` | Business | Legitimate Interest | Mask external API |
| `business_name` | Business | Legitimate Interest | Mask external |
| `created_at` | Business | Legitimate Interest | Show, don't expose |
| `verification_status` | Business | Legitimate Interest | Show to owner |

### 3.4 PUBLIC Data

| Field | Treatment |
|-------|-----------|
| `status` | No masking |
| `success` | No masking |
| `error_code` | No masking |
| `factor` | No masking |
| `api_version` | No masking |

---

## 4. Risk Assessment Matrices

### 4.1 Data Processing Risk Matrix

| Data Type | Sensitivity | Threat Level | Impact | Likelihood | Risk Score |
|----------|-------------|--------------|--------|-----------|------------|
| Password hashes | CRITICAL | High | Severe | Low | **HIGH** |
| API keys | CRITICAL | High | Severe | Medium | **CRITICAL** |
| Biometric templates | CRITICAL | High | Severe | Low | **CRITICAL** |
| Email addresses | HIGH | Medium | Moderate | High | **HIGH** |
| Phone numbers | HIGH | Medium | Moderate | Medium | **MEDIUM** |
| Wallet addresses | HIGH | Medium | Moderate | Medium | **MEDIUM** |
| Device IDs | HIGH | Medium | Moderate | Medium | **MEDIUM** |
| Authentication tokens | HIGH | High | Moderate | High | **HIGH** |
| UUIDs | MEDIUM | Low | Low | High | **LOW** |
| Transaction counts | MEDIUM | Low | Low | Medium | **LOW** |

### 4.2 User Role Risk Grid

| Role | RESTRICTED Access | CONFIDENTIAL Access | INTERNAL Access | Export Risk | Delete Risk |
|------|-----------------|------------------|-----------------|-------------|------------|
| **SUPER_ADMIN** | ❌ Never | ✅ Full | ✅ Full | ⚠️ Audit | ✅ Audit |
| **SUPPORT_ADMIN** | ❌ Never | ✅ View-only | ✅ Full | ❌ No | ❌ No |
| **MERCHANT** | ❌ Never | ❌ No | ✅ Own only | ❌ No | ❌ No |
| **USER** | ❌ Never | ❌ No | ✅ Own only | ❌ No | ✅ Own only |
| **PUBLIC** | ❌ Never | ❌ No | ❌ No | ❌ No | ❌ No |

### 4.3 Regulatory Risk Assessment Grid

| Data Category | GDPR | CCPA | PIPEDA | BIPA | PCI-DSS |
|--------------|------|------|-------|-------|-------|---------|
| Passwords | ✅ PII | ✅ | ✅ | N/A | N/A |
| Email | ✅ PII | ✅ | ✅ | N/A | N/A |
| Phone | ✅ PII | ✅ | ✅ | N/A | N/A |
| Biometrics | ✅ Special Cat | N/A | ⚠️ | ✅ | N/A |
| Payment info | ✅ | ✅ | ✅ | N/A | ✅ |
| Device ID | ✅ PII | ✅ | ✅ | N/A | N/A |
| IP Address | ✅ PII | ✅ | ✅ | N/A | N/A |

### 4.4 Compliance Agent Check Grid

| Classification | Mask Required | Audit Required | Encryption Required | Retention Max |
|----------------|---------------|---------------|---------------------|---------------|
| **RESTRICTED** | Always | ✅ Full | ✅ AES-256-GCM | 7 years |
| **CONFIDENTIAL** | External only | ✅ Full | ✅ AES-256-GCM | 1 year |
| **INTERNAL** | External only | ✅ Basic | Optional | 2 years |
| **PUBLIC** | None | None | None | Indefinite |

---

## 5. Role-Based Access Control

### 5.1 Role Definitions

| Role | Description | Use Case | Access Level |
|------|-------------|---------|------------|
| `SYSTEM` | Automated processes | Background jobs, webhooks | Full (no masking) |
| `SUPER_ADMIN` | Organization admins | Full system administration | Full (no RESTRICTED) |
| `SUPPORT_ADMIN` | Support staff | User support | View all (no exports) |
| `MERCHANT` | Business merchants | Transaction verification | Own merchant data only |
| `USER` | End users | Own enrollment | Own enrollment only |
| `PUBLIC` | Unauthenticated | Public APIs | Public data only |

### 5.2 Access Matrix

```
┌──────��─��─────────────────────────────────────────────────────────────────────────┐
│                     ROLE ACCESS MATRIX                            │
├──────────────────┬─────────┬────────────┬─────────┬─────────┬───────┤
│ Data Level       │ SYSTEM  │ SUPER_AD  │ SUPPORT │ MERCHANT│ USER  │
├──────────────────┼─────────┼────────────┼─────────┼─────────┼───────┤
│ RESTRICTED      │ ✅ Full │ ❌ Never   │ ❌ Never│ ❌ Never│ ❌    │
│ - password      │ View    │            │         │         │       │
│ - api_key      │         │            │         │         │       │
│ - factor_digest│         │            │         │         │       │
├──────────────────┼─────────┼────────────┼─────────┼─────────┼───────┤
│ CONFIDENTIAL    │ ✅ Full │ ✅ Full    │ ✅ View │ ❌ Never│ ❌    │
│ - email         │ View    │            │         │         │       │
│ - phone        │         │            │         │         │       │
│ - wallet       │         │            │         │         │       │
├──────────────────┼─────────┼────────────┼─────────┼─────────┼───────┤
│ INTERNAL       │ ✅ Full │ ✅ Full    │ ✅ View │ ✅ Own  │ ✅Own │
│ - uuid         │ Show    │            │         │         │       │
│ - merchant_id │         │            │         │         │       │
├──────────────────┼─────────┼────────────┼─────────┼─────────┼───────┤
│ PUBLIC         │ ✅ Full │ ✅ Full    │ ✅ Full │ ✅ Full │ ✅Full│
│ - status       │ Show    │            │         │         │       │
│ - factor_type │         │            │         │         │       │
└──────────────────┴─────────┴────────────┴─────────┴─────────┴───────┘
```

### 5.3 Why These Roles Exist

| Role | Rationale |
|------|-----------|
| SYSTEM | Background jobs need full access to process data; audited separately |
| SUPER_ADMIN | Organization leadership needs full view for governance; dual-approval for sensitive actions |
| SUPPORT_ADMIN | User support requires viewing PII but NOT exporting; audit logs for compliance |
| MERCHANT | Merchants only need their own data; prevents cross-merchant data access |
| USER | Users only see their own enrollment; prevents unauthorized access |
| PUBLIC | Unauthenticated users see only public status information |

---

## 6. Treatment Requirements

### 6.1 Masking Requirements

| Classification | Display | Logs | API Response | Export |
|----------------|---------|------|-------------|--------|
| RESTRICTED | Never | `[REDACTED]` | `[REDACTED]` | Never |
| CONFIDENTIAL | Masked | `[REDACTED]` | Masked* | Never |
| INTERNAL | Show | Full | Masked** | Audit only |
| PUBLIC | Full | Full | Full | Allowed |

*Mask pattern: `j***@domain.com`, `+1*** ***2345`
**UUID pattern: `abc-123-***-***-************`

### 6.2 Encryption Requirements

| Classification | At Rest | In Transit | Processing |
|----------------|---------|------------|-------------|
| RESTRICTED | AES-256-GCM | TLS 1.3 | In memory only |
| CONFIDENTIAL | AES-256-GCM | TLS 1.3 | In memory only |
| INTERNAL | Optional | TLS 1.3 | Clear |
| PUBLIC | None | TLS 1.3 | Clear |

### 6.3 Retention Requirements

| Classification | Active Storage | Audit Log | Backup |
|----------------|--------------|----------|--------|
| RESTRICTED | Never* | 7 years | 7 years |
| CONFIDENTIAL | 1 year | 7 years | 7 years |
| INTERNAL | 2 years | 3 years | 3 years |
| PUBLIC | Indefinite | 90 days | 90 days |

*Stored hashed/encrypted, not in plaintext

---

## 7. Compliance & Audit

### 7.1 Compliance Agent Checks

The compliance agent should verify:

| Check | Classification | Required Action |
|-------|---------------|----------------|
| RESTRICTED exposed | API response | FAIL - Block deployment |
| CONFIDENTIAL unmasked | External API | FAIL - Block deployment |
| INTERNAL unmasked | Public API | WARN - Review |
| Missing encryption | CONFIDENTIAL | FAIL - Block deployment |
| Missing audit log | All writes | FAIL - Block deployment |
| Retention exceeded | All data | FAIL - Alert |

### 7.2 Audit Trail Requirements

| Event | Log Required | Fields to Log |
|-------|-------------|--------------|
| Data classification change | ✅ | Old, new, reason, actor |
| Role access grant | ✅ | Role, user, scope, approver |
| Data export | ✅ | User, data type, count, destination |
| Masking bypass | ✅ | Reason, approver, expiry |
| Retention violation | ✅ Auto | Data, age, action taken |

---

## 8. Implementation

### 8.1 Code Usage

```javascript
// Import classification utilities
const { getFieldClassification, maskValue, CLASSIFICATION } = require('../utils/dataClassifier');
const { maskResponse, getUserRole, validateClassification } = require('../utils/responseMask');

// Use in API response
app.get('/api/user', requireAuth, (req, res) => {
  const user = getUserData(req.userId);
  
  // Mask based on requester role
  const masked = maskResponse(user, req);
  
  // Validate compliance
  const validation = validateClassification(masked, getUserRole(req));
  if (!validation.valid) {
    logger.warn('Classification violation:', validation.violations);
  }
  
  res.json(masked);
});
```

### 8.2 Middleware Usage

```javascript
// Apply masking to all responses
const { createMaskMiddleware } = require('../utils/responseMask');

app.use(createMaskMiddleware({
  recursive: true
}));
```

---

## 9. Policy Review

| Review Cycle | Frequency | Owner |
|--------------|-----------|-------|
| Full review | Annual | Security Team |
| Risk update | Quarterly | Security Team |
| Classification add | As needed | Data Owner |

---

## Appendix A: Field Classification Quick Reference

### RESTRICTED Fields (Never Expose)
```
password, password_hash, api_secret, api_key, factor_digest, biometric_data,
pin, recovery_codes, secret, private_key, seed, salt, iv
```

### CONFIDENTIAL Fields (Mask External)
```
email, phone, wallet_address, device_id, ip_address, full_name, access_token,
signature, credit_card, oauth_token
```

### INTERNAL Fields (Mask External)
```
merchant_id, user_uuid, business_name, created_at, verification_status,
factor_count, transaction_count
```

### PUBLIC Fields (No Masking)
```
status, success, error_code, factor, api_version
```