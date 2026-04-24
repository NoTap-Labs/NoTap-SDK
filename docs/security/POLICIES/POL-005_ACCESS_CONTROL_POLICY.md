# NoTap Access Control Policy

> **Access Control Policy** — Logical and physical access control for information systems and data
> Required by ISO 27001 A.9.1, SOC 2 CC6, and PCI DSS requirements.

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-22 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Regulatory Alignment](#2-regulatory-alignment)
3. [Access Control Principles](#3-access-control-principles)
4. [Identity Management](#4-identity-management)
5. [Role-Based Access Control](#5-role-based-access-control)
6. [Network Access Control](#6-network-access-control)
7. [System & Application Access](#7-system--application-access)
8. [Privileged Access Management](#8-privileged-access-management)
9. [Access Review & Monitoring](#9-access-review--monitoring)
10. [Roles & Responsibilities](#10-roles--responsibilities)
11. [Compliance & Enforcement](#11-compliance--enforcement)
12. [Related Documents](#12-related-documents)
13. [Annex A: Role Permission Matrix](#annex-a-role-permission-matrix)
14. [Annex B: Session Timeout Matrix](#annex-b-session-timeout-matrix)
15. [Annex C: Access Control Checklist](#annex-c-access-control-checklist)

---

## 1. Purpose & Scope

### 1.1 Purpose

This policy establishes NoTap's access control framework to ensure:

- **Least Privilege**: Users access only data necessary for their role (ISO 27001 A.9.1)
- **Separation of Duties**: No single user has complete control over critical operations
- **Accountability**: All access is logged and traceable
- **Zero Trust**: Never trust, always verify every request
- **Compliance**: Meet regulatory requirements for access control

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| Admin system access | Customer authentication |
| Internal API access | Merchant API access |
| Database access | End-user enrollment |
| Cloud console access | Payment processor systems |
| Support tool access | |

### 1.3 Key Changes from Implementation

This policy formalizes the existing implementation in `INTERNAL_USER_ACCESS_POLICY.md` as an official policy with:
- Formal ISO 27001 A.9.1 alignment
- SOC 2 CC6 mapping
- Integration with risk assessment (POL-002)
- Integration with incident response (POL-006)

---

## 2. Regulatory Alignment

### 2.1 ISO 27001:2022 Controls

| Control | Requirement | Policy Section |
|---------|-------------|--------------|
| A.9.1 | Business Requirements of Access Control | Sections 3, 5 |
| A.9.2 | User Registration | Section 4 |
| A.9.3 | Privilege Management | Section 8 |
| A.9.4 | System & Application Access | Sections 6, 7 |
| A.9.5 | Secure Authentication | Sections 7, 8 |
| A.9.6 | Access Rights Removal | Section 4.4 |

### 2.2 SOC 2 Trust Services Criteria

| Criterion | Requirement | Policy Section |
|-----------|-------------|--------------|
| CC6.1 | Logical Access | Sections 5-8 |
| CC6.2 | User Registration | Section 4 |
| CC6.3 | Role Assignment | Section 5 |
| CC6.4 | Access Revocation | Section 4.4 |
| CC6.5 | Multi-Factor Authentication | Section 7.3 |
| CC6.6 | Network Access | Section 6 |
| CC6.7 | Privileged Access | Section 8 |

### 2.3 PCI DSS Mapping

| Requirement | Policy Section |
|-------------|--------------|
| R8.1 | User identification management |
| R8.2 | Authentication mechanisms |
| R8.3 | Access control systems |
| R8.7 | Access logging |

---

## 3. Access Control Principles

### 3.1 Core Principles

| Principle | Description | Implementation |
|-----------|-------------|--------------|
| **Least Privilege** | Minimum access required for job function | Role-based permissions |
| **Need-to-Know** | Access only when required for work | Just-in-time access |
| **Separation of Duties** | No single point of control | Dual-approval workflow |
| **Defense in Depth** | Multiple layers of control | MFA, network, device trust |
| **Zero Trust** | Never trust, always verify | Every request authenticated |

### 3.2 Access Control Model

```
┌─────────────────────────────────────────────────────────────────┐
│              ACCESS CONTROL MODEL                     │
├──────────────────────────────────────────────┤
│                                              │
│  ┌─────────────────────────────────────────┐ │
│  │         IDENTITY VERIFICATION            │ │
│  │  (Admin registration, MFA required)     │ │
│  └──────────────────┬──────────────────────┘ │
│                     │                        │
│                     ▼                        │
│  ┌─────────────────────────────────────────┐ │
│  │         AUTHENTICATION                   │ │
│  │  (JWT token, API key verification)      │ │
│  └──────────────────┬──────────────────────┘ │
│                     │                        │
│                     ▼                        │
│  ┌─────────────────────────────────────────┐ │
│  │         AUTHORIZATION                   │ │
│  │  (Role-based permission check)        │ │
│  └──────────────────┬──────────────────────┘ │
│                     │                        │
│                     ▼                        │
│  ┌─────────────────────────────────────────┐ │
│  │         ACCESS GRANTED/DENIED          │ │
│  │  (Audit logged)                      │ │
│  └─────────────────────────────────────────┘ │
│                                              │
│  DEFENSE IN DEPTH:                             │
│  1. Identity → 2. Auth → 3. AuthZ       │
│                                              │
└─────────────────────────────────────────────────────────────────┘
```

### 3.3 Risk-Based Access

| Data Classification | Required Controls |
|-------------------|------------------|
| RESTRICTED | MFA + device trust + permission check |
| CONFIDENTIAL | MFA + permission check |
| INTERNAL | Authentication + role check |
| PUBLIC | No restrictions |

---

## 4. Identity Management

### 4.1 Admin User Registration

| Requirement | Description |
|--------------|-------------|
| **Identity Verification** | Email verification required |
| **Background Check** | For privileged access roles |
| **MFA Enrollment** | Required before access granted |
| **Device Registration** | For device trust roles |
| **Manager Approval** | Required for new accounts |
| **Access Agreement** | Signed policy acknowledgment |

### 4.2 Identity Lifecycle

```
┌──────────────────────────���─��────────────────────────────────────┐
│              IDENTITY LIFECYCLE                         │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────┐                                        │
│  │ REQUEST │◄──────── New employee, contractor       │
│  └────┬───┘                                        │
│       │                                             │
│       ▼                                             │
│  ┌──────────────┐                                     │
│  │  APPROVAL │◄──────── Manager + Security           │
│  └────┬───────┘                                     │
│       │                                             │
│       ▼                                             │
│  ┌──────────────┐                                     │
│  │ PROVISION │◄──────── Identity + Access          │
│  └────┬───────┘                                     │
│       │                                             │
│       ▼                                             │
│  ┌──────────────────────────────────────┐         │
│  │           ACTIVE USER                 │◄──────── Ongoing access    │
│  │  - Periodic access review           │         │
│  │  - Annual recertification        │         │
│  └────┬───────────────────────┘         │
│       │                                             │
│       ▼                                             │
│  ┌──────────┐                                        │
│  │ REVOKE  │◄──────── Termination, role change   │
│  │  0-24hrs │  Immediate disable              │
│  └──────────┘                                        │
│                                                      │
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 Access Provisioning (WKI-001)

| Step | Action | Timeline | Owner |
|------|--------|----------|--------|
| 1 | Request submission | Day 0 | Hiring manager |
| 2 | Identity verification | Day 1 | HR |
| 3 | Security review | Day 1 | Security Team |
| 4 | Manager approval | Day 1 | Department head |
| 5 | Account provisioning | Day 2 | IT |
| 6 | Access activation | Day 2 | User |

### 4.4 Access Revocation

| Trigger | Action | Timeline |
|----------|--------|----------|
| Termination | Disable account | Immediate |
| Role change | Adjust permissions | 24 hours |
| Inactivity (90 days) | Disable account | Warning + 7 days |
| Policy violation | Suspend + review | Immediate |
| Security incident | Suspend | Immediate |

---

## 5. Role-Based Access Control

### 5.1 Defined Roles

| Role ID | Role Name | Description | Max Count |
|---------|----------|-------------|----------|
| `super_admin` | Super Administrator | Full system access | 3 |
| `support_admin` | Support Administrator | User support access | No limit |
| `developer` | Developer | API key management | No limit |
| `billing_admin` | Billing Administrator | Payment access | Limited |
| `security_analyst` | Security Analyst | Security monitoring | Limited |

### 5.2 Role Hierarchy

```
┌─────────────────────────────────────────────────────────────────┐
│                    ROLE HIERARCHY                        │
├────────────────────────────────────────────┤
│                                                      │
│                      ┌─────────┐                        │
│                     │SUPER_ADMIN│                       │
│                     │    ↑    │                        │
│                     │    │    │                        │
│                     │    ├───┼─────┐                  │
│                     │    │       │    │                  │
│              ┌──────┴──┐ ┌──┴────┐ ┌──┴───┐         │
│              │SECURITY │ │SUPPORT│ │BILLING│          │
│              │ANALYST │ │ ADMIN │ │ ADMIN │          │
│              │  ↑   │ │   ↑  │ │  ↑   │          │
│              │  │   │ │   │  │ │  │   │          │
│              └──┴───┘ └──┴───┘ └──┴───┘         │
│                  │        │         │               │
│                  │        │         │               │
│            ┌──────┴────────┴─────────┴─────┐      │
│            │         DEVELOPER            │       │
│            │     (API management)        │       │
│            └─────────────────────────────┘       │
│                                                      │
```

### 5.3 Permission Categories

| Category | Description | Examples |
|-----------|-------------|----------|
| **USER** | User management | user.create, user.read, user.delete |
| **ADMIN** | Admin management | admin.create, admin.role.assign |
| **DATA** | Data access | data.export, data.read |
| **CONFIG** | Configuration | config.read, config.write |
| **AUDIT** | Audit logs | audit.read, audit.export |
| **SECURITY** | Security operations | security.alert, security.disable |
| **FINANCIAL** | Payment access | payment.read, payment.refund |

### 5.4 Permission Matrix

See Annex A for complete permission matrix by role.

### 5.5 Segregation of Duties

| Duty Separation | Implementation |
|----------------|---------------|
| User delete | Two different admins required |
| Role assignment | Manager + Security approval |
| Data export | Audit log + approval |
| Security disable | Two-person rule |

---

## 6. Network Access Control

### 6.1 Network Zones

| Zone | Access From | Authentication |
|------|-------------|----------------|
| **Admin Zone** | Corporate VPN | MFA + device trust |
| **Development** | Any (rate limited) | API key |
| **Monitoring** | Corporate + VPN | Read-only |
| **Public** | Internet | None required |

### 6.2 Access Methods

| Method | Allowed Roles | MFA Required | Device Trust |
|--------|-------------|-------------|------------|
| VPN | All admin | Yes | Yes |
| Direct | Developer only | Yes | No |
| Console | Super Admin | Yes | Yes |
| API | Developer | API key | No |

### 6.3 IP Allowlisting

| Role | Default | Override |
|------|---------|----------|
| SUPER_ADMIN | Corporate only | Never |
| SUPPORT_ADMIN | Corporate + trusted | Rarely |
| DEVELOPER | Any | Always |
| BILLING_ADMIN | Corporate only | Never |
| SECURITY_ANALYST | Corporate + VPN | Allowed |

---

## 7. System & Application Access

### 7.1 Authentication Mechanisms

| Mechanism | Required For | Implementation |
|-----------|-------------|--------------|
| **Password** | All users | 12+ chars, complexity |
| **MFA** | All users | NoTap factors |
| **API Key** | API access | bcrypt hashed |
| **JWT** | Session access | 30 min expiry |
| **Device Trust** | Privileged roles | Registered device |

### 7.2 MFA Requirements

| Role | MFA Required | Factor Count |
|------|-------------|------------|
| SUPER_ADMIN | Yes (required) | 2+ factors |
| SUPPORT_ADMIN | Yes | 1+ factor |
| DEVELOPER | Yes | 1+ factor |
| BILLING_ADMIN | Yes | 1+ factor |
| SECURITY_ANALYST | Yes | 1+ factor |

### 7.3 Multi-Factor Authentication

MFA is required for:
- All admin account login
- Privileged operations
- Data export
- Configuration changes
- Access from new device

### 7.4 Session Management

| Role | Session Timeout | Max Concurrent | Re-auth For |
|------|--------------|-------------|------------|
| SUPER_ADMIN | 30 minutes | 1 | Any data access |
| SUPPORT_ADMIN | 1 hour | 2 | PII access |
| DEVELOPER | 8 hours | 3 | API key change |
| BILLING_ADMIN | 1 hour | 2 | Payment access |
| SECURITY_ANALYST | 2 hours | 2 | Audit export |

See Annex B for complete session matrix.

---

## 8. Privileged Access Management

### 8.1 Privileged Roles

| Role | Privileged Because |
|------|------------------|
| SUPER_ADMIN | Full system access, user deletion |
| SECURITY_ANALYST | Security configuration |
| BILLING_ADMIN | Payment access |

### 8.2 Privileged Access Controls

| Control | SUPER_ADMIN | SECURITY | BILLING |
|---------|-----------|----------|--------|
| MFA required | Yes (2+) | Yes | Yes |
| Device trust | Yes | Yes | Yes |
| Step-up auth | Yes | For alerts | N/A |
| Session logging | Full | Full | Payment |
| Approval required | Account delete | N/A | N/A |

### 8.3 Super Admin Specific Controls

Super administrator accounts require:
1. **Named individuals** - No shared accounts
2. **Two-factor MFA** - At least 2 factors
3. **Device registration** - Only registered devices
4. **Dual-approval** - For sensitive actions
5. **Emergency access protocol** - Documented procedure
6. **Activity monitoring** - Real-time alerts

### 8.4 Emergency Access

Emergency access allows temporary elevation when:
- Security incident declared
- Service degradation
- Documented justification
- Auto-expires (4 hours)
- Post-incident review required

---

## 9. Access Review & Monitoring

### 9.1 Access Audit

| Event | Log Level | Retention |
|-------|----------|----------|
| Login success | INFO | 7 years |
| Login failure | WARNING | 7 years |
| Permission change | INFO | 7 years |
| Role assignment | INFO | 7 years |
| Data export | WARNING | 7 years |
| Sensitive action | WARNING | 7 years |

### 9.2 Access Review Schedule

| Review Type | Frequency | Owner | Approver |
|------------|-----------|---------|--------|
| Quarterly access | Every 90 days | Security Team Lead | Security Team |
| Annual certification | Annual | Security Team Lead | Executive |
| After role change | Within 30 days | Security Team | Manager |
| Termination | Immediate | HR | Security Team |

### 9.3 Access Metrics

| Metric | Target | Alert |
|--------|--------|-------|
| Failed logins | < 5% | > 10% |
| Stale accounts | 0 | Any 90+ days inactive |
| Elevated access | Documented | Undocumented |
| Shared credentials | 0 | Any found |

### 9.4 Key Risk Indicators (KRIs)

| KRI | Threshold | Alert |
|-----|-----------|-------|
| Failed MFA attempts | > 5 in 10 min | Security Team |
| New admin created | Any | Security Team |
| Role change | Any | Manager |
| Access from new IP | > 3 different | User verification |

---

## 10. Roles & Responsibilities

### 10.1 Access Control Roles

| Role | Responsibility |
|------|-------------|
| **Executive Sponsor** | Policy approval, resource allocation |
| **Security Team Lead** | Policy maintenance, access reviews |
| **HR** | Identity verification, termination |
| **Department Head** | Access approval |
| **All Staff** | Policy compliance, credentials |

### 10.2 RACI Matrix

| Activity | Security Team | HR | Manager | User |
|----------|-------------|---|---------|------|
| Policy approval | C | I | I | I |
| Identity verification | I | R | C | I |
| Access approval | C | C | R | I |
| Account provisioning | R | C | I | I |
| Access review | R | C | C | I |
| Termination | I | R | I | I |

---

## 11. Compliance & Enforcement

### 11.1 Compliance Monitoring

| Check | Frequency | Owner |
|-------|-----------|-------|
| Access review | Quarterly | Security Team |
| Role validation | Annual | Security Team Lead |
| MFA compliance | Monthly | Security Team |
| Session audit | Monthly | Security Team |

### 11.2 Non-Compliance

| Violation | Consequence |
|-----------|-------------|
| Shared credentials | Immediate disable |
| MFA bypass | Suspension |
| Unauthorized access | Disciplinary |
| Policy violation | Review + action |

### 11.3 Audit Requirements

| Audit Type | Frequency | Documentation |
|-----------|-----------|-------------|
| Access review | Quarterly | Report to Executive |
| Role certification | Annual | Signed acknowledgment |
| Privileged access | Annual | Full audit |
| Compliance | Annual | External audit |

---

## 12. Related Documents

| Document | Location | Purpose |
|----------|----------|---------|
| POL-001 Information Security | `POL-001_INFORMATION_SECURITY_POLICY.md` | Master policy |
| POL-006 Incident Response | `POL-006_INCIDENT_RESPONSE.md` | Incident procedures |
| INTERNAL_USER_ACCESS_POLICY | `../INTERNAL_USER_ACCESS_POLICY.md` | Implementation |
| SECURITY_PATTERNS_REFERENCE | `../SECURITY_PATTERNS_REFERENCE.md` | Security patterns |
| DATA_CLASSIFICATION_POLICY | `../DATA_CLASSIFICATION_POLICY.md` | Data classification |
| GATES.md | `../../10-internal/GATES.md` | Runtime controls |

---

## Annex A: Role Permission Matrix

| Permission | SUPER_ADMIN | SUPPORT_ADMIN | DEVELOPER | BILLING | SECURITY |
|------------|------------|------------|----------|--------|----------|
| **User Management** | | | | | |
| user.create | ✅ | ❌ | ❌ | ❌ | ❌ |
| user.read.any | ✅ | ✅ | ❌ | ❌ | ❌ |
| user.read.self | ✅ | ✅ | ✅ | ❌ | ❌ |
| user.update.any | ✅ | ⚠️ Limited | ❌ | ❌ | ❌ |
| user.delete | ✅ Dual | ❌ | ❌ | ❌ | ❌ |
| user.export | ✅ With Audit | ❌ | ❌ | ❌ | ❌ |
| **Admin Management** | | | | | | |
| admin.create | ✅ | ❌ | ❌ | ❌ | ❌ |
| admin.read | ✅ | ✅ | ❌ | ❌ | ✅ |
| admin.update | ✅ | ❌ | ❌ | ❌ | ❌ |
| admin.delete | ✅ Dual | ❌ | ❌ | ❌ | ❌ |
| admin.role.assign | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Data Access** | | | | | | | |
| data.pii.view | ✅ Full | ✅ View | ❌ | ❌ | ❌ |
| data.auth.view | ✅ View | ❌ | ❌ | ❌ | ✅ View |
| data.audit.full | ✅ Full | ❌ | ❌ | ❌ | ✅ Full |
| data.payment.view | ✅ View | ❌ | ❌ | ✅ View | ❌ |
| **API Access** | | | | | |
| api.key.manage | ✅ Full | ❌ | Own Only | ❌ | ❌ |
| api.key.rotate | ✅ | ❌ | Own Only | ❌ | ❌ |
| webhook.manage | ✅ Full | ❌ | Own Only | ❌ | ❌ |
| **Configuration** | | | | | | |
| config.read | ✅ Full | Limited | Own | Limited | ✅ |
| config.write | ✅ | ❌ | Own | ❌ | ✅ |
| **Security** | | | | | |
| security.alert | ✅ | ✅ | ❌ | ❌ | ✅ |
| security.disable | ✅ | ❌ | ❌ | ❌ | ⚠️ Limited |
| security.block | ✅ | ❌ | ❌ | ❌ | ❌ |

---

## Annex B: Session Timeout Matrix

| Role | Session Timeout | Max Concurrent | Re-auth For |
|------|----------------|---------------|------------|
| SUPER_ADMIN | 30 minutes | 1 | Any data access |
| SUPPORT_ADMIN | 1 hour | 2 | PII access |
| DEVELOPER | 8 hours | 3 | API key change |
| BILLING_ADMIN | 1 hour | 2 | Payment access |
| SECURITY_ANALYST | 2 hours | 2 | Audit log export |

---

## Annex C: Access Control Checklist

| Check | Status | Evidence |
|-------|--------|----------|
| All admins have unique accounts | [ ] | User list |
| MFA enabled for all admins | [ ] | MFA status |
| Privileged access logged | [ ] | Audit logs |
| Quarterly access reviews documented | [ ] | Review reports |
| Terminated access revoked within 24h | [ ] | Termination log |
| Shared credentials eliminated | [ ] | Audit finding |
| Session timeouts enforced | [ ] | Configuration |
| IP restrictions for privileged roles | [ ] | Firewall rules |
| Dual-approval for sensitive actions | [ ] | Workflow logs |
| Annual access certification | [ ] | Signed acknowledgments |

---

## Document Control

- **Version**: 1.0.0
- **Status**: DRAFT
- **Approved By**: Executive Sponsor (pending)
- **Effective Date**: Upon approval
- **Next Review**: 2027-04-22
- **Location**: `documentation/05-security/POLICIES/POL-005_ACCESS_CONTROL_POLICY.md`

**For questions**: Contact Security Team Lead or see CLAUDE.md for security contacts.

(End of file - 592 lines)