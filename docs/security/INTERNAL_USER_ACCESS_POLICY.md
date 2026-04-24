# NoTap Internal User Access Policy

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-19 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Role Definitions](#2-role-definitions)
3. [Role Hierarchy](#3-role-hierarchy)
4. [Permission Matrix](#4-permission-matrix)
5. [Access Control Rules](#5-access-control-rules)
6. [Approval Workflows](#6-approval-workflows)
7. [Audit & Monitoring](#7-audit--monitoring)
8. [Offboarding](#8-offboarding)

---

## 1. Purpose & Scope

### 1.1 Purpose

This policy defines internal user access controls for NoTap to ensure:

- **Least Privilege**: Users access only data necessary for their role (ISO 27001 A.9.1)
- **Separation of Duties**: No single user has complete control (ISO 27001 A.9.1.2)
- **Auditability**: All access is logged (GDPR Art. 30)
- **Reversibility**: Access can be revoked immediately

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| Internal admin users | End users (enrollment) |
| Support staff | Merchant users |
| Developers | API consumers |
| Security team | External auditors |

### 1.3 Regulatory Alignment

| Control | Requirement | Policy Section |
|---------|-------------|--------------|
| ISO 27001 A.9.1 | Access Control Policy | This entire document |
| ISO 27001 A.9.2 | User Registration | Section 2 |
| ISO 27001 A.9.3 | Privilege Management | Section 4 |
| ISO 27001 A.9.4 | System Access Removal | Section 8 |
| GDPR Art. 30 | Records of Processing | Section 7 |

---

## 2. Role Definitions

### 2.1 Super Admin

| Attribute | Value |
|-----------|-------|
| **Role ID** | `super_admin` |
| **Database Column** | `role = 'super_admin'` |
| **Count** | Maximum 3 |
| **MFA Required** | Yes (NoTap factors) |
| **Device Trust** | Yes (registered devices only) |

**Responsibility:**
- Organization governance
- User role assignment
- Policy enforcement
- Security incident response
- NoTap Seal approval (dual-approval required)

**Why They Exist:**
- Ultimate organizational authority
- Cannot be locked out of system
- Required for catastrophic recovery

**Access Level:**
- All data (excluding RESTRICTED plaintext)
- All admin functions
- All audit logs
- User data export with approval
- Account deletion (dual-approval)

### 2.2 Support Admin

| Attribute | Value |
|-----------|-------|
| **Role ID** | `support_admin` |
| **Database Column** | `role = 'support'` |
| **Count** | No maximum (business need) |
| **MFA Required** | Yes |
| **Device Trust** | Yes |

**Responsibility:**
- User support tickets
- Account recovery
- Verification status checks
- User data lookup for support

**Why They Exist:**
- Enable user support without engineering
- Reduce engineering burden
- Maintain compliance through auditing

**Access Level:**
- User PII (view only, no export)
- All user data except RESTRICTED
- Support ticketing system
- No account deletion
- No role assignment
- No policy changes

### 2.3 Developer

| Attribute | Value |
|-----------|-------|
| **Role ID** | `developer` |
| **Database Column** | `role = 'developer'` |
| **Count** | As needed |
| **MFA Required** | Yes |
| **Device Trust** | Optional |

**Responsibility:**
- API key management
- Webhook configuration
- Developer portal administration
- SDK/API documentation

**Why They Exist:**
- Build integrations with NoTap
- Manage their API keys and webhooks
- Access developer analytics

**Access Level:**
- Own developer account
- Own API keys
- Own webhooks
- Own project analytics
- No other user data

### 2.4 Billing Admin

| Attribute | Value |
|-----------|-------|
| **Role ID** | `billing_admin` |
| **Database Column** | `role = 'billing'` |
| **Count** | Limited |
| **MFA Required** | Yes |
| **Device Trust** | Yes |

**Responsibility:**
- Payment processing
- Invoice generation
- Subscription management
- Revenue analytics

**Why They Exist:**
- Separate billing from system admin
- PCI-DSS compliance
- Financial oversight

**Access Level:**
- Payment methods (last 4 only)
- Subscription data
- Revenue reports
- No user credentials
- No authentication data

### 2.5 Security Analyst

| Attribute | Value |
|-----------|-------|
| **Role ID** | `security_analyst` |
| **Database Column** | `role = 'security'` |
| **Count** | Limited |
| **MFA Required** | Yes |
| **Device Trust** | Yes |

**Responsibility:**
- Security event monitoring
- Threat investigation
- Compliance reporting
- Vulnerability management

**Why They Exist:**
- Dedicated security oversight
- Regulatory compliance
- Incident response capability

**Access Level:**
- Security events (full)
- Audit logs (full)
- No PII modification
- No account modification

---

## 3. Role Hierarchy

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    ROLE HIERARCHY                      │
├────────────────────────────────────────────────────────────┤
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
│                                                    │
└────────────────────────────────────────────────────────────┘
```

### 3.1 Delegation Rules

| From Role | Can Delegate To | Requires Approval |
|-----------|----------------|--------------------|
| SUPER_ADMIN | SUPPORT_ADMIN | No |
| SUPER_ADMIN | BILLING_ADMIN | No |
| SUPER_ADMIN | SECURITY_ANALYST | No |
| SUPPORT_ADMIN | None | N/A |
| BILLING_ADMIN | None | N/A |
| SECURITY_ANALYST | None | N/A |

---

## 4. Permission Matrix

### 4.1 User Management Permissions

| Permission | SUPER_ADMIN | SUPPORT_ADMIN | SECURITY | BILLING |
|-------------|-------------|----------------|----------|--------|
| user.create | ✅ | ❌ | ❌ | ❌ |
| user.read.any | ✅ | ✅ | ❌ | ❌ |
| user.read.self | ✅ | ✅ | ❌ | ❌ |
| user.update.any | ✅ | ⚠️ Limited | ❌ | ❌ |
| user.update.self | ✅ | ✅ | ❌ | ❌ |
| user.delete | ✅ Dual | ❌ | ❌ | ❌ |
| user.export | ✅ With Audit | ❌ | ❌ | ❌ |
| user.password.reset | ✅ | ✅ | ❌ | ❌ |

### 4.2 Admin Management Permissions

| Permission | SUPER_ADMIN | SUPPORT_ADMIN | SECURITY |
|-------------|-------------|----------------|----------|
| admin.create | ✅ | ❌ | ❌ |
| admin.read | ✅ | ✅ | ✅ |
| admin.update | ✅ | ❌ | ❌ |
| admin.delete | ✅ Dual | ❌ | ❌ |
| admin.role.assign | ✅ | ❌ | ❌ |
| admin.permissions.grant | ✅ | ❌ | ❌ |

### 4.3 Data Access Permissions

| Data Type | SUPER_ADMIN | SUPPORT_ADMIN | DEVELOPER | SECURITY |
|-----------|-------------|----------------|------------|-----------|
| User PII | ✅ Full | ✅ View Only | ❌ | ❌ |
| Auth Data | ✅ View | ❌ | ❌ | ✅ View |
| Audit Logs | ✅ Full | ❌ | ❌ | ✅ Full |
| Payment Data | ✅ View | ❌ | ❌ | ❌ |
| API Keys | ✅ Full | ❌ | Own Only | ❌ |
| Webhooks | ✅ Full | ❌ | Own Only | ❌ |

### 4.4 Sensitive Action Permissions

| Action | SUPER_ADMIN | SUPPORT_ADMIN | Notes |
|--------|-------------|---------------|---------|
| User data export | ✅ + Audit | ❌ | Requires dual-approval |
| Account deletion | ✅ + Dual | ❌ | Requires second SUPER_ADMIN |
| Role assignment | ✅ | ❌ | Requires device trust |
| Policy change | ✅ + Audit | ❌ | Requires audit log |
| API key rotation | ✅ | ❌ | Can rotate own only |
| Secret deletion | ✅ + Audit | ❌ | Requires confirmation |

---

## 5. Access Control Rules

### 5.1 Authentication Requirements

| Role | Password Policy | MFA Required | Device Trust |
|------|------------------|-------------|--------------|
| SUPER_ADMIN | 16+ chars | ✅ (2+ factors) | ✅ Required |
| SUPPORT_ADMIN | 12+ chars | ✅ | ✅ Required |
| DEVELOPER | 12+ chars | ✅ | Optional |
| BILLING_ADMIN | 12+ chars | ✅ | ✅ Required |
| SECURITY_ANALYST | 12+ chars | ✅ | ✅ Required |

### 5.2 Session Management

| Role | Session Timeout | Max Concurrent | Re-auth for |
|------|------------------|---------------|-------------|
| SUPER_ADMIN | 30 minutes | 1 | Any data access |
| SUPPORT_ADMIN | 1 hour | 2 | PII access |
| DEVELOPER | 8 hours | 3 | API key change |
| BILLING_ADMIN | 1 hour | 2 | Payment access |
| SECURITY_ANALYST | 2 hours | 2 | Audit log export |

### 5.3 IP Access Control

| Role | Allowed Networks | Fail Open | Notes |
|------|-----------------|----------|-------|
| SUPER_ADMIN | Corporate only | ❌ No | Corporate VPN required |
| SUPPORT_ADMIN | Corporate + Trusted | ❌ No | Known locations |
| DEVELOPER | Any | ⚠️ Yes | Rate limited |
| BILLING_ADMIN | Corporate only | ❌ No | PCI-DSS compliance |
| SECURITY_ANALYST | Corporate + VPN | ⚠️ Yes | 24/7 monitoring |

---

## 6. Approval Workflows

### 6.1 Dual-Approval Required Actions

| Action | First Approver | Second Approver | Timeout |
|--------|----------------|-----------------|-------------|
| User account deletion | SUPPORT_ADMIN | SUPER_ADMIN | 24h |
| Bulk data export | SUPER_ADMIN | SUPER_ADMIN (different) | 1h |
| Role assignment | SUPPORT_ADMIN | SUPER_ADMIN | 24h |
| Policy change | SUPER_ADMIN | SUPER_ADMIN (different) | 24h |
| Secret rotation | SUPER_ADMIN | SUPER_ADMIN | 1h |

### 6.2 Approval Request Flow

```
┌─────────────────────────────────────────────────────────────┐
│                  APPROVAL WORKFLOW                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Action Triggered                                           │
│         │                                                   │
│         ▼                                                   │
│  ┌──────────────┐                                          │
│  │Dual-approval │─────────── NO ────▶ Block Action         │
│  │Required?    │                                         │
│  └──────────────┘                                         │
│         │                                                   │
│        YES                                                  │
│         │                                                   │
│         ▼                                                   │
│  ┌──────────────────────┐                                  │
│  │Submit Request      │                                  │
│  │(Requester)         │                                  │
│  └──────────────────────┘                                  │
│         │                                                   │
│         ▼                                                   │
│  ┌──────────────────────┐                                  │
│  │Approver 1 Review     │── REJECTED ──▶ Notify + Log  │
│  │(Role-based)         │                                  │
│  └──────────────────────┘                                  │
│         │                                                   │
│        APPROVED                                               │
│         │                                                   │
│         ▼                                                   │
│  ┌──────────────────────┐                                  │
│  │Approver 2 Review     │── REJECTED ──▶ Notify + Log  │
│  │(SUPER_ADMIN)         │                                  │
│  └──────────────────────┘                                  │
│         │                                                   │
│        APPROVED                                               │
│         │                                                   │
│         ▼                                                   │
│  Execute Action + Audit Log                                   │
│         │                                                   │
│         ▼                                                   │
│  Notify Requester + Security Team                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.3 Emergency Access

In case of emergency (security incident, outage), SUPER_ADMIN can bypass dual-approval with:

1. **Justification**: Written reason required
2. **Time-limited**: Auto-expires in 4 hours
3. **Automatic Audit**: Full action logged
4. **Post-incident Review**: Required within 24 hours

---

## 7. Audit & Monitoring

### 7.1 Required Audit Events

| Event | Log Level | Retention |
|-------|---------|-----------|
| Login success | INFO | 7 years |
| Login failure | WARNING | 7 years |
| Password change | INFO | 7 years |
| Role change | INFO | 7 years |
| Permission grant | INFO | 7 years |
| Data export | WARNING | 7 years |
| Account deletion | INFO | 7 years |
| Policy change | INFO | 7 years |
| Sensitive action | WARNING | 7 years |

### 7.2 Audit Log Fields

```json
{
  "timestamp": "ISO-8601",
  "actor": {
    "admin_id": "uuid",
    "role": "super_admin",
    "ip_address": "anonymized"
  },
  "action": "user.delete",
  "target": {
    "user_id": "uuid",
    "reason": "user request"
  },
  "result": "success",
  "dual_approval": {
    "approver_admin_id": "uuid",
    "approved_at": "ISO-8601"
  },
  "emergency_access": false,
  "justification": "User requested deletion"
}
```

### 7.3 Real-time Alerts

| Alert | Condition | Recipients |
|-------|-----------|-----------|
| Multiple failed logins | 5 in 10 minutes | SECURITY_ANALYST |
| Role change | Any | SUPER_ADMIN, SECURITY_ANALYST |
| Data export | >100 records | SUPER_ADMIN |
| Bulk delete | Any | SUPER_ADMIN |
| Emergency access | When triggered | SUPER_ADMIN |

---

## 8. Offboarding

### 8.1 Offboarding Checklist

| Task | When | Owner |
|------|------|-------|
| Disable account | Immediate | SUPER_ADMIN |
| Revoke API keys | Immediate | SUPER_ADMIN |
| Remove from systems | Immediate | IT |
| Revoke VPN access | Same day | IT |
| Return hardware | Same day | IT |
| Forward emails | Same day | IT |
| Archive data | 24 hours | IT |
| Conduct exit interview | Optional | HR |

### 8.2 Data Retention on Offboarding

| Data Type | Retention | Access After |
|----------|-----------|--------------|
| Audit logs | 7 years | SUPER_ADMIN only |
| Configuration | Indefinite | Read-only |
| Created content | Indefinite | Owner transfer |
| Personal data | 30 days | DELETE |

### 8.3 Immediate Revocation Triggers

Account must be disabled IMMEDIATELY if:

- Employee termination
- Security incident suspected
- Policy violation
- Unauthorized access detected
- Credentials compromised

---

## Appendix A: Quick Reference

### Quick Role Selection Guide

| Need a user to... | Role to assign |
|-------------------|----------------|
| Manage all users | SUPER_ADMIN |
| Handle support tickets | SUPPORT_ADMIN |
| View security events | SECURITY_ANALYST |
| Manage billing | BILLING_ADMIN |
| Build integrations | DEVELOPER |

### Emergency Contacts

| Security Incident | Contact |
|-------------------|----------|
| Out of band verification | security@notap.io |
| Account compromise | emergency@notap.io |
| Data breach | dpo@notap.io |

---

## Appendix B: Compliance Mapping

| ISO 27001 Control | Policy Section |
|-------------------|---------------|
| A.9.1 Business Requirements | Section 1 |
| A.9.2 User Registration | Section 2 |
| A.9.3 Privilege Management | Section 4 |
| A.9.4 Access Removal | Section 8 |
| A.9.5 Network Access | Section 5 |
| A.9.6 System Access | Section 5 |
| A.9.7 Monitoring | Section 7 |