# NoTap Policy Framework Index

> **Single Source of Truth** for all formal policies, standards, and procedures.
> This index maps regulatory requirements to implementation documents.

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-22 | Claude | DRAFT |

---

## Table of Contents

1. [Framework Overview](#1-framework-overview)
2. [Policy Hierarchy](#2-policy-hierarchy)
3. [Policy Registry](#3-policy-registry)
4. [Compliance Mapping](#4-compliance-mapping)
5. [Roles & Responsibilities](#5-roles--responsibilities)
6. [Review & Maintenance](#6-review--maintenance)

---

## 1. Framework Overview

### 1.1 Purpose

This policy framework establishes the governance structure for NoTap's Information Security Management System (ISMS) to ensure:

- **Regulatory Compliance**: GDPR, CCPA, PIPEDA, BIPA, NIS2, PCI DSS
- **Certification Readiness**: ISO 27001:2022, SOC 2 Type II
- **Risk Management**: Systematic identification and treatment of security risks
- **Continuous Improvement**: Regular review and enhancement of controls

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| Backend API (Node.js) | Physical premises (cloud-hosted) |
| SDK (Kotlin Multiplatform) | Employee personal devices |
| Web frontend (Kotlin/JS) | Third-party vendor internal systems |
| Database services (PostgreSQL, Redis) | |
| AWS KMS integration | |
| All data processed for authentication | |

### 1.3 Regulatory Alignment Matrix

| Regulation | Type | Scope | Policy Sections |
|-----------|------|-------|---------------|
| **GDPR** | Data Protection (EU) | EU users, all processing | POL-001, POL-003, POL-004 |
| **CCPA/CPRA** | Data Protection (California) | CA residents | POL-001, POL-003, POL-004 |
| **PIPEDA** | Data Protection (Canada) | Canadian users | POL-001, POL-003, POL-004 |
| **BIPA** | Biometrics (Illinois) | Biometric factors | POL-001, POL-005 |
| **NIS2** | Cybersecurity (EU) | Essential entities | POL-001, POL-002, POL-006 |
| **PCI DSS v4.0** | Payment Security | Payment processing | POL-001, POL-005 |
| **ISO 27001:2022** | ISMS (International) | Entire scope | ALL |
| **SOC 2 Type II** | Trust Services (US) | Service organization | ALL |

---

## 2. Policy Hierarchy

### 2.1 Hierarchy Structure

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    POLICY FRAMEWORK HIERARCHY                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  LEVEL 1: MASTER POLICY (Strategic)                                 │
│  ┌─────────────────────────────────────────────────────────────┐     │
│  │ POL-001: Information Security Policy                      │     │
│  │ Purpose: Establish ISMS commitment and governance       │     │
│  │ Audience: All stakeholders, auditors                    │     │
│  │ Review: Annual                                          │     │
│  └─────────────────────────────────────────────────────────────┘     │
│                           │                                         │
│                           ▼                                         │
│  LEVEL 2: TOPIC-SPECIFIC POLICIES (Tactical)                        │
│  ┌─────────────────────────────────────────────────────────────┐     │
│  │ POL-002: Risk Assessment Policy                          │     │
│  │ POL-003: Data Protection Policy (ROPA + Retention)      │     │
│  │ POL-004: Privacy & Consent Policy                       │     │
│  │ POL-005: Access Control Policy                        │     │
│  │ POL-006: Incident Response Procedure                  │     │
│  │ POL-007: Business Continuity Policy                  │     │
│  │ POL-008: Supplier Security Policy                    │     │
│  └───────────────────────────────────────────────────────┘     │
│                           │                                         │
│                           ▼                                         │
│  LEVEL 3: STANDARDS & PROCEDURES (Operational)                   │
│  ┌─────────────────────────────────────────────────────────────┐     │
│  │ STD-001: Encryption Standard                            │     │
│  │ STD-002: Password Standard                              │     │
│  │ STD-003: Data Classification Standard                 │     │
│  │ STD-004: Secure Development Standard                   │     │
│  │ STD-005: Logging & Monitoring Standard               │     │
│  └─────────────────────────────────────────────────────────────┘     │
│                           │                                         │
│                           ▼                                         │
│  LEVEL 4: WORK INSTRUCTIONS (Implementation)                         │
│  ┌─────────────────────────────────────────────────────────────┐     │
│  │ WKI-001: Access Provisioning Procedure                  │     │
│  │ WKI-002: Incident Response Runbook                    │     │
│  │ WKI-003: Data Retention Cleanup Procedure              │     │
│  │ WKI-004: Change Management Procedure                   │     │
│  └─────────────────────────────────────────────────────────────┘     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Document Control

| Level | Type | Approval | Review Cycle | Change Control |
|-------|------|----------|--------------|---------------|
| Level 1 | Master Policy | Executive | Annual | Formal approval required |
| Level 2 | Topic Policy | Security Team Lead | Annual | Policy owner approval |
| Level 3 | Standard | Security Team | Bi-annual | Documented changes |
| Level 4 | Work Instruction | Team Lead | As needed | Ad-hoc updates |

---

## 3. Policy Registry

### 3.1 Level 1: Master Policy

| ID | Policy Name | Version | Status | Owner | Next Review |
|----|-------------|---------|--------|-------|-----------|
| POL-001 | Information Security Policy | 1.0.0 | DRAFT | Security Team | 2027-04-22 |

### 3.2 Level 2: Topic-Specific Policies

| ID | Policy Name | Version | Status | Owner | ISO 27001 | SOC 2 |
|----|-------------|---------|--------|-------|-----------|-----------|-------|
| POL-002 | Risk Assessment Policy | 1.0.0 | DRAFT | Security Team | Cl. 6.1.2 | CC3.1 |
| POL-003 | Data Protection Policy | 1.0.0 | DRAFT | DPO | A.8.15, A.8.16 | CC1.2 |
| POL-004 | Privacy & Consent Policy | 1.0.0 | DRAFT | DPO | CC1.2 | Privacy |
| POL-005 | Access Control Policy | 1.0.0 | DRAFT | Security Team | A.5.15-A.5.18 | CC6.1-CC6.7 |
| POL-006 | Incident Response Procedure | 1.0.0 | DRAFT | Security Team | A.5.26, A.5.27 | CC7.1-CC7.4 |
| POL-007 | Business Continuity Policy | 1.0.0 | DRAFT | Operations | A.8.27 | A1.1 |
| POL-008 | Supplier Security Policy | 1.0.0 | DRAFT | Security Team | A.5.19-A.5.21 | CC9.2 |

### 3.3 Level 3: Standards

| ID | Standard Name | Version | Status | Reference |
|----|---------------|---------|--------|-----------|
| STD-001 | Encryption Standard | - | PLANNED | SECURITY_PATTERNS_REFERENCE.md |
| STD-002 | Password Standard | - | PLANNED | INTERNAL_USER_ACCESS_POLICY.md |
| STD-003 | Data Classification Standard | - | PLANNED | DATA_CLASSIFICATION_POLICY.md |
| STD-004 | Secure Development Standard | - | PLANNED | DEVELOPMENT_RULES.md |
| STD-005 | Logging & Monitoring Standard | - | PLANNED | ARCHITECTURE.md |

### 3.4 Level 4: Work Instructions

| ID | Work Instruction | Version | Status | Reference |
|----|------------------|---------|--------|-----------|
| WKI-001 | Access Provisioning Procedure | - | PLANNED | INTERNAL_USER_ACCESS_POLICY.md |
| WKI-002 | Incident Response Runbook | - | PLANNED | PENTEST_SECURITY_AUDIT_REPORT.md |
| WKI-003 | Data Retention Cleanup Procedure | - | PLANNED | PRIVACY_IMPLEMENTATION.md |
| WKI-004 | Change Management Procedure | - | PLANNED | DEVELOPMENT_RULES.md |

### 3.5 Existing Implementation Documents

| Document | Type | Status | Integration |
|----------|------|--------|-------------|
| SECURITY_PATTERNS_REFERENCE.md | Implementation | ✅ Complete | Reference for STD-001 |
| INTERNAL_USER_ACCESS_POLICY.md | Implementation | ✅ Complete | Reference for POL-005 |
| DATA_CLASSIFICATION_POLICY.md | Implementation | ✅ Complete | Reference for STD-003 |
| PRIVACY_IMPLEMENTATION.md | Implementation | ✅ Complete | Reference for POL-003 |
| DATA_LIFECYCLE_RISK_ASSESSMENT.md | Assessment | ✅ Complete | Input to POL-002 |
| DEVELOPMENT_RULES.md | Standards | ✅ Complete | Reference for STD-004 |
| ISO_27002_GAP_ANALYSIS.md | Gap Analysis | ✅ Complete | Input to policy development |
| GATES.md | Controls | ✅ Complete | Runtime enforcement |

---

## 4. Compliance Mapping

### 4.1 ISO 27001:2022 Mandatory Policies

| Clause | Requirement | Policy | Status |
|--------|-------------|--------|--------|
| 5.1 | Information Security Policy | POL-001 | DRAFT |
| 5.2 | Leadership and Commitment | POL-001 | DRAFT |
| 5.3 | Organizational Roles | POL-001 | DRAFT |
| 6.1.2 | Risk Assessment | POL-002 | DRAFT |
| 6.1.3 | Risk Treatment | POL-002 | DRAFT |
| 7.1 | Resources | POL-001 | DRAFT |
| 7.2 | Competence | POL-001 | DRAFT |
| 7.3 | Awareness | POL-001 | DRAFT |
| 7.4 | Communication | POL-001 | DRAFT |
| 7.5 | Documented Information | POL-001 | DRAFT |
| 8.1 | Operational Planning and Control | POL-004, POL-006 | DRAFT |
| 8.2 | Incident Management | POL-006 | DRAFT |
| 8.3 | Continual Improvement | ALL | ONGOING |

### 4.2 GDPR Article Mapping

| Article | Requirement | Policy | Status |
|---------|-------------|--------|--------|
| Art. 5 | Processing Principles | POL-003, POL-004 | DRAFT |
| Art. 6 | Lawfulness of Processing | POL-004 | DRAFT |
| Art. 12 | Transparency | POL-004 | DRAFT |
| Art. 13-14 | Privacy Notice | POL-004 | DRAFT |
| Art. 15-22 | Data Subject Rights | POL-003 | DRAFT |
| Art. 25 | Privacy by Design | POL-004, STD-004 | DRAFT |
| Art. 28 | Processor Agreements | POL-008 | PLANNED |
| Art. 30 | Records of Processing | POL-003 | DRAFT |
| Art. 32 | Security Measures | POL-001, STD-001 | DRAFT |
| Art. 33-34 | Breach Notification | POL-006 | DRAFT |
| Art. 35 | DPIA | POL-002 | DRAFT |
| Art. 37 | DPO | TBD | PLANNED |

### 4.3 NIS2 Directive Mapping

| Article | Requirement | Policy | Status |
|---------|-------------|--------|--------|
| Art. 21 | Risk Management | POL-002, POL-007 | DRAFT |
| Art. 21(2) | Minimum Security Measures | POL-001, POL-005 | DRAFT |
| Art. 23 | Incident Reporting | POL-006 | DRAFT |
| Art. 24 | Supply Chain Security | POL-008 | DRAFT |

### 4.4 SOC 2 Trust Services Criteria Mapping

| TSC | Requirement | Policy | Status |
|-----|-------------|--------|--------|
| Security | CC6.1-CC6.7 | POL-005 | DRAFT |
| Availability | A1.1 | POL-007 | DRAFT |
| Processing Integrity | PI1.1 | STD-004 | PLANNED |
| Confidentiality | CC6.1 | POL-003, POL-005 | DRAFT |
| Privacy | P1-P5 | POL-004 | DRAFT |
| Supply Chain | CC9.1-CC9.3 | POL-008 | DRAFT |

---

## 5. Roles & Responsibilities

### 5.1 Policy Governance Roles

| Role | Responsibility | Document Owner |
|------|----------------|---------------|
| **Executive Sponsor** | Final approval for master policy | TBD |
| **Security Team Lead** | Topic policy oversight | TBD |
| **Policy Owner** | Maintain specific policy | Per policy |
| **DPO (Privacy Officer)** | GDPR compliance | TBD |
| **All Staff** | Read and comply | All policies |

### 5.2 Policy Development Process

```
┌─────────────────────────────────────────────────────────────┐
│                  POLICY DEVELOPMENT WORKFLOW                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. Identify Requirement                                     │
│     └─> Regulatory mandate or business need                │
│                      │                                       │
│  2. Gap Analysis                                            │
│     └─> Review ISO_27002_GAP_ANALYSIS.md                   │
│                      │                                       │
│  3. Draft Policy                                           │
│     └─> Follow policy template (see Section 7)            │
│                      │                                       │
│  4. Review & Comment                                        │
│     └─> Security team + legal (if applicable)             │
│                      │                                       │
│  5. Approval                                                │
│     └─> Policy owner + executive sponsor                    │
│                      │                                       │
│  6. Publication                                             │
│     └─> Upload to POLICIES/ folder + update this index     │
│                      │                                       │
│  7. Communication                                           │
│     └─> Notify affected teams via CLAUDE.md               │
│                      │                                       │
│  8. Training (if required)                                 │
│     └─> Document in task.md                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Review & Maintenance

### 6.1 Review Schedule

| Document Type | Review Cycle | Trigger |
|--------------|--------------|--------|
| Master Policy (POL-001) | Annual | Scheduled review |
| Topic Policies (POL-002 to POL-008) | Annual | Scheduled review |
| Standards (STD-001 to STD-005) | Bi-annual | Scheduled review |
| Work Instructions (WKI-001 to WKI-004) | As needed | Process change |
| This Index | With any policy change | Immediate update |

### 6.2 Version Control

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2026-04-22 | Claude | Initial framework + 4 policies (POL-002, POL-003, POL-004, POL-006) |

### 6.3 Change Log Protocol

When any policy is updated:
1. Update version number in document header
2. Add entry to change log in document
3. Update this index with new version and date
4. Update CLAUDE.md if policy affects development workflow
5. Notify security team

---

## 7. Policy Template Structure

All new policies MUST follow this structure:

```markdown
# [Policy Name]

| Version | Date | Author | Status |
|---------|------|--------|--------|
| X.Y.Z | YYYY-MM-DD | Name | DRAFT/APPROVED/ACTIVE |

---

## Table of Contents

1. Purpose & Scope
2. Definitions
3. Policy Statement
4. Roles & Responsibilities
5. Procedures
6. Compliance & Audit
7. Exceptions
8. Related Documents
9. Review & Maintenance
10. Change Log
```

---

## 8. Related Documents

| Document | Location | Purpose |
|----------|----------|---------|
| MASTER_INDEX (this file) | `POLICIES/` | Policy framework index |
| ISO_27002_GAP_ANALYSIS | `../` | Compliance gap analysis |
| SECURITY_PATTERNS_REFERENCE | `../` | Security implementation |
| DATA_CLASSIFICATION_POLICY | `../` | Data classification |
| INTERNAL_USER_ACCESS_POLICY | `../` | Access control |
| PRIVACY_IMPLEMENTATION | `../` | Privacy implementation |
| DEVELOPMENT_RULES | `../../03-developer-guides/` | Development standards |
| CLAUDE.md | `../../` | Agent development guide |

---

## 9. Quick Reference

### Need to find a policy?

| Scenario | Document |
|----------|----------|
| "What are our security commitments?" | POL-001 Information Security Policy |
| "How do we assess risks?" | POL-002 Risk Assessment Policy |
| "What data do we process?" | POL-003 Data Protection Policy |
| "How long do we keep data?" | POL-003 (Section 8: Retention & Deletion) |
| "How do we get consent?" | POL-004 Privacy & Consent Policy |
| "How do we handle breaches?" | POL-006 Incident Response Procedure |
| "Who can access what?" | INTERNAL_USER_ACCESS_POLICY.md |
| "What encryption do we use?" | SECURITY_PATTERNS_REFERENCE.md |

### Compliance Quick Check

| Regulation | Primary Policy | Key Controls |
|-----------|---------------|--------------|
| ISO 27001 | POL-001, POL-002 | All policies |
| GDPR | POL-003, POL-004, POL-006 | Art. 30 ROPA, Art. 33 breach |
| NIS2 | POL-002, POL-006 | Art. 21 measures, Art. 23 reporting |
| BIPA | POL-004 | Biometric consent |
| CCPA | POL-004 | Opt-out rights |
| SOC 2 | POL-001, POL-005 | CC6 access, CC7 incidents |
| PCI DSS | POL-005, STD-001 | R8 authentication, R10 logging |

---

**Document Control:**
- Location: `documentation/05-security/POLICIES/INDEX.md`
- Master Index: Yes
- Last Updated: 2026-04-22
- Next Review: 2027-04-22
- Policies Included: POL-001, POL-002, POL-003, POL-004, POL-005, POL-006, POL-007, POL-008

**For questions:** See CLAUDE.md for security contacts.