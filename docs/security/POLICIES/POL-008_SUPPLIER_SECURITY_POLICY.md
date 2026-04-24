# NoTap Supplier Security Policy

> **Supplier Security Policy** — Third-party vendor security requirements and management
> Required by ISO 27001 A.5.19-21, NIS2 Article 24, and GDPR Article 28.

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-22 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Regulatory Alignment](#2-regulatory-alignment)
3. [Supplier Categorization](#3-supplier-categorization)
4. [Assessment Process](#4-assessment-process)
5. [Data Processing Agreements](#5-data-processing-agreements)
6. [Security Requirements](#6-security-requirements)
7. [Ongoing Monitoring](#7-ongoing-monitoring)
8. [Offboarding](#8-offboarding)
9. [Incident Response](#9-incident-response)
10. [Roles & Responsibilities](#10-roles--responsibilities)
11. [Compliance & Enforcement](#11-compliance--enforcement)
12. [Related Documents](#12-related-documents)
13. [Annex A: Supplier Categories](#annex-a-supplier-categories)
14. [Annex B: Security Requirements Matrix](#annex-b-security-requirements-matrix)
15. [Annex C: DPA Template](#annex-c-dpa-template)

---

## 1. Purpose & Scope

### 1.1 Purpose

This policy establishes NoTap's approach to managing third-party supplier security to ensure:

- **Supply Chain Security**: All vendors meet security requirements (NIS2 Art. 24)
- **Data Protection**: Personal data processed only under DPA (GDPR Art. 28)
- **Risk Management**: Supplier risks assessed and managed
- **Continuous Monitoring**: Ongoing validation of compliance
- **Incident Response**: Vendor incident procedures

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| Cloud service providers | Public APIs (no data) |
| Payment processors | Open source libraries |
| Email service providers | Physical premises |
| Analytics providers | Public websites (no PII) |
| Support tool vendors | |

### 1.3 Types of Suppliers

| Type | Data Access | Risk Level |
|------|-----------|----------|
| **Processor** | Personal data | CRITICAL |
| **Sub-processor** | Aggregated data | HIGH |
| **Service Provider** | Limited access | MEDIUM |
| **Tool Vendor** | No access | LOW |

### 1.4 Key Relationships

| Policy | Relationship |
|--------|-------------|
| POL-001 | Overall security commitments |
| POL-002 | Supplier risk assessment |
| POL-003 | Processor list (ROPA) |
| POL-006 | Vendor incident notifications |

---

## 2. Regulatory Alignment

### 2.1 ISO 27001:2022 Controls

| Control | Requirement | Policy Section |
|---------|-------------|--------------|
| A.5.19 | Supplier Relationships | Sections 3, 4 |
| A.5.20 | Supplier Agreements | Section 5 |
| A.5.21 | Supply Chain Information | Section 6 |
| A.8.8 | Exchange of Information | Section 6, 7 |

### 2.2 NIS2 Directive

| Article | Requirement | Policy Section |
|---------|-------------|--------------|
| Art. 24 | Supply Chain Security | Sections 3, 4, 6 |
| Art. 24(1) | Risk management | Section 4 |
| Art. 24(2) | Technical measures | Section 6 |

### 2.3 GDPR Requirements

| Article | Requirement | Policy Section |
|---------|-------------|--------------|
| Art. 28 | Processor agreements | Section 5 |
| Art. 28(3) | Required DPA terms | Section 5, Annex C |
| Art. 28(4) | Sub-processor restrictions | Section 5.4 |
| Art. 28(5) | Third country transfers | Section 5.5 |

### 2.4 SOC 2 Trust Services Criteria

| Criterion | Requirement | Policy Section |
|-----------|-------------|--------------|
| CC9.1 | Third-party providers | Sections 3-6 |
| CC9.2 | Third-party agreements | Section 5 |
| CC9.3 | Monitoring | Section 7 |

---

## 3. Supplier Categorization

### 3.1 Categories

| Category | Definition | Examples | Review Frequency |
|----------|-------------|----------|---------------|
| **Critical** | Core services, PII access | Cloud hosting, payments | Annual + quarterly |
| **Important** | Business functions | Email, analytics | Annual |
| **Standard** | Utility services | Monitoring tools | Biennial |
| **Low** | No data access | Public tools | As needed |

### 3.2 Category Criteria

| Criteria | Weight |
|---------|--------|
| Data access type (PII, financials) | 40% |
| Service criticality | 30% |
|替代availability | 20% |
| Security certifications | 10% |

### 3.3 Supplier Classification Matrix

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              SUPPLIER CLASSIFICATION                        │
├───────────────────────────────────────────────────────┤
│                                                       │
│   Data Access           │ Impact                  │             │
│   ───────────        │ ────────────          │           │
│   PII + Financial  │ → CRITICAL         │              │
│   PII only        │ → IMPORTANT       │              │
│   Business data    │ → IMPORTANT       │              │
│   No data        │ → STANDARD       │              │
│                                                       │
│   Service Impact        │                              │                  │
│   ───────────        │ ─────────────      │              │
│   Core (payments)   │ → CRITICAL     │              │
│   Required         │ → IMPORTANT   │              │
│   Optional        │ → STANDARD   │              │
│                                                       │
└─────────────────────────────────────────────────────────┘
```

### 3.4 Initial Categories

Based on current architecture (general categories, to be updated):

| Category | Suppliers |
|----------|-----------|
| **Critical** | Cloud hosting provider, payment processors |
| **Important** | Email service, analytics provider |
| **Standard** | Monitoring tools, logging services |
| **Low** | Development tools, documentation |

---

## 4. Assessment Process

### 4.1 Initial Assessment

| Step | Activity | Timeline | Owner |
|------|----------|----------|--------|
| 1 | Identify supplier | Week 0 | Requesting team |
| 2 | Complete questionnaire | Week 1 | Security Team |
| 3 | Review certifications | Week 2 | Security Team |
| 4 | Risk assessment | Week 2 | Security Team |
| 5 | Legal review (if needed) | Week 3 | Legal |
| 6 | Approval | Week 4 | Security Team Lead |
| 7 | DPA (if needed) | Week 4-6 | Legal |

### 4.2 Assessment Criteria

| Criteria | Required Evidence |
|-----------|---------------|
| **Security certifications** | SOC 2 Type II, ISO 27001, PCI DSS |
| **Data handling** | Data flow diagram, processing scope |
| **Incident response** | Notification procedure |
| **Encryption** | Encryption standards used |
| **Access control** | RBAC, MFA requirements |
| **Retention** | Data retention/deletion policy |
| **Breach notification** | Notification timeline |
| **Sub-processing** | Sub-processor list |

### 4.3 Minimum Requirements by Category

| Requirement | Critical | Important | Standard | Low |
|-------------|----------|-----------|----------|-----|
| SOC 2 Type II | Required | Required | Preferred | - |
| ISO 27001 | Required | Preferred | - | - |
| Encryption (AES-256) | Required | Required | - | - |
| DPA | Required | Required | - | - |
| 72h breach notice | Required | Required | - | - |
| MFA | Required | Preferred | - | - |

### 4.4 Risk Assessment Integration

All supplier risk assessments follow POL-002 methodology:

- Inherent risk based on data access
- Controls considered from assessment
- Residual risk calculated
- Treatment plan documented
- Acceptance by Security Team Lead

---

## 5. Data Processing Agreements

### 5.1 When DPA Required

A Data Processing Agreement (DPA) is required when supplier:

- Processes personal data on our behalf
- Has access to personal data
- Is a processor under GDPR Art. 4(8)
- Falls under Critical or Important category

### 5.2 DPA Required Terms (GDPR Art. 28(3))

| Required Term | Description |
|--------------|-------------|
| Subject matter | What data processed |
| Duration | Processing period |
| Nature/purpose | Why processed |
| Categories | Types of data |
| Data subjects | Whose data |
| Obligations | Security measures |
| Confidentiality | Staff obligations |
| Security | Technical + organizational |
| Sub-processing | Conditions for sub-processor |
| Data return | Return/deletion procedure |
| Audits | Audit rights |
| Transfers | Cross-border requirements |

### 5.3 DPA Template

See Annex C for complete DPA template.

### 5.4 Sub-Processor Management

| Requirement | Policy |
|--------------|--------|
| **Notification** | 30 days advance notice |
| **Approval** | NoTap written approval |
| **Same terms** | Sub-processor same obligations |
| **Objection** | Right to object |
| **Downstream** | Flow down requirements |

### 5.5 International Transfers

| Transfer Mechanism | When Used |
|------------------|----------|
| EU Adequacy | EU-based supplier |
| SCCs | Non-adequate countries |
| BCRs | Intra-group |
| Derogation | Specific, documented |

---

## 6. Security Requirements

### 6.1 Technical Requirements

| Requirement | Critical | Important | Standard |
|-------------|----------|-----------|----------|
| Encryption at rest | AES-256 | AES-256 | - |
| Encryption in transit | TLS 1.3 | TLS 1.2+ | TLS 1.2 |
| Access control | RBAC | RBAC | - |
| MFA | Required | Recommended | - |
| Logging | Full audit | Essential | Basic |
| Vulnerability mgmt | Required | Required | - |

### 6.2 Organizational Requirements

| Requirement | Critical | Important | Standard |
|-------------|----------|-----------|----------|
| Security team | Named | Named | - |
| Incident contacts | 24/7 | Business hours | Email |
| SLA | Documented | Documented | - |
| Insurance | Required | Recommended | - |
| Notifications | 24h | 72h | Reasonable |

### 6.3 Contractual Requirements

| Requirement | Description |
|--------------|-------------|
| **Security requirements** | Documented in DPA |
| **Incident notification** | Within 24-72 hours |
| **Audit rights** | Annual audit (with notice) |
| **Compliance** | SOC 2 Type II |
| **Breach liability** | Defined in contract |
| **Termination** | 90 days notice |

### 6.4 Minimum Security Controls

All Critical and Important suppliers must have:

```
REQUIRED SECURITY CONTROLS
━━━━━━━━━━━━━━━━━━━━━━━━━

□ Encryption at rest (AES-256-GCM)
□ Encryption in transit (TLS 1.2+)
□ Multi-factor authentication
□ Role-based access control
□ Comprehensive logging
□ 24/7 security contacts
□ Annual penetration testing
□ Vulnerability management program
□ Named security team
□ Incident response plan
□ Data retention/deletion policy
□ Business continuity plan
```

---

## 7. Ongoing Monitoring

### 7.1 Monitoring Schedule

| Category | Review Frequency | Assessment | Certification |
|----------|---------------|-------------|------------|
| **Critical** | Quarterly | Annual | Annual |
| **Important** | Annual | Annual | Annual |
| **Standard** | Biennial | As needed | As needed |
| **Low** | As needed | As needed | - |

### 7.2 Continuous Monitoring

| Activity | Frequency | Owner |
|----------|-----------|--------|
| SLA compliance | Monthly | Operations |
| Security news | Ongoing | Security Team |
| Incident alerts | Real-time | Security Team |
| Certification renewal | Annual | Legal |
| Risk re-assessment | Annual | Security Team |

### 7.3 Certification Validation

| Certification | Validation | Alert |
|--------------|------------|-------|
| SOC 2 Type II | Annual | Expiration - 30 days |
| ISO 27001 | Annual | Expiration - 60 days |
| PCI DSS | Annual | Expiration - 30 days |

### 7.4 Key Performance Indicators

| KRI | Threshold | Alert |
|-----|-----------|-------|
| SLA breaches | > 2/month | Operations |
| Renewal overdue | Any | Security Team |
| New critical supplier | Any without DPA | Security Team |
| Security incident | Any | Security Team |

---

## 8. Offboarding

### 8.1 Offboarding Triggers

| Trigger | Action | Timeline |
|---------|--------|----------|
| Contract end | Data return/deletion | 30 days |
| Supplier breach | Terminate | Immediate |
| Security concern | Suspend + review | Immediate |
| Business change | Reassess need | Annual |
| Poor performance | Improvement plan | 30 days |

### 8.2 Data Return Requirements

| Data Type | Return Method | Timeline |
|-----------|--------------|----------|
| Personal data | Encrypted export | 30 days |
| Encrypted data | Keys provided | 30 days |
| Backups | Deletion confirmed | 60 days |
| Logs | Export | 30 days |

### 8.3 Offboarding Checklist

| Task | Owner | Timeline |
|------|-------|----------|
| Notify supplier | Legal | 90 days |
| Disable access | IT | 30 days |
| Export data | Security Team | 30 days |
| Verify deletion | Security Team | 60 days |
| Update ROPA | DPO | 60 days |
| Document completion | Security Team | 90 days |

### 8.4 Post-Offboarding Review

- Confirm all data returned/deleted
- Review any incidents during relationship
- Update supplier risk register
- Document lessons learned

---

## 9. Incident Response

### 9.1 Supplier Incident Requirements

| Incident Type | Notification Timeline |
|---------------|----------------------|
| **Data breach** | Within 24 hours |
| **Security incident** | Within 24 hours |
| **Service degradation** | Within 72 hours |
| **Configuration change** | Within 7 days |

### 9.2 Notification Requirements

When supplier experiences security incident:

1. **Initial notification** (within timeline)
   - Description of incident
   - Data potentially affected
   - Remediation actions

2. **Detailed report** (within 7 days)
   - Root cause
   - Impact assessment
   - Remediation plan

3. **Final report** (within 30 days)
   - Complete investigation
   - Lessons learned
   - Process improvements

### 9.3 NoTap Incident Response Integration

| POL-006 Section | Supplier Integration |
|----------------|---------------------|
| Detection | Supplier monitoring |
| Classification | Include supplier impact |
| Containment | Coordinate with supplier |
| Recovery | Supplier restoration |
| Notification | 24h supplier notification |

### 9.4 Escalation

| Incident Severity | Escalate To |
|------------------|------------|
| Critical | Executive + Legal |
| High | Security Team Lead + Legal |
| Medium | Security Team |
| Low | Security Analyst |

---

## 10. Roles & Responsibilities

### 10.1 Supplier Security Roles

| Role | Responsibility |
|------|----------------|
| **Executive Sponsor** | Supplier business decisions |
| **Security Team Lead** | Policy, assessments, approval |
| **Legal** | DPA negotiation, contracts |
| **Vendor Manager** | Day-to-day relationship |
| **All Staff** | Report supplier issues |

### 10.2 RACI Matrix

| Activity | Executive | Security Lead | Legal | Vendor Manager |
|----------|-----------|---------------|------|---------------|
| Policy approval | A | R | C | I |
| Supplier assessment | I | R | C | C |
| DPA negotiation | I | C | R | C |
| Contract approval | A | C | R | C |
| Ongoing monitoring | I | R | I | R |
| Incident response | I | R | C | R |

---

## 11. Compliance & Enforcement

### 11.1 Compliance Monitoring

| Check | Frequency | Owner |
|-------|-----------|--------|
| Supplier list accuracy | Quarterly | Vendor Manager |
| Certification status | Monthly | Security Team |
| DPA currency | Monthly | Legal |
| Risk re-assessment | Annual | Security Team |
| Policy review | Annual | Security Team Lead |

### 11.2 Non-Compliance Consequences

| Violation | Consequence |
|-----------|-------------|
| Expired certification | Suspend new data |
| DPA breach | Contract review |
| Security incident | Review + remediation |
| Unauthorized sub-processor | Immediate suspension |

### 11.3 Audit Requirements

| Audit Type | Frequency | Scope |
|-----------|-----------|-------|
| Supplier audit | Annual | Critical + Important |
| Self-assessment | Annual | All suppliers |
| External audit | Every 3 years | Selected suppliers |

---

## 12. Related Documents

| Document | Location | Purpose |
|----------|----------|---------|
| POL-001 Information Security | `POL-001_INFORMATION_SECURITY_POLICY.md` | Master policy |
| POL-002 Risk Assessment | `POL-002_RISK_ASSESSMENT_POLICY.md` | Assessment methodology |
| POL-003 Data Protection | `POL-003_DATA_PROTECTION_POLICY.md` | Processor list |
| POL-006 Incident Response | `POL-006_INCIDENT_RESPONSE.md` | Incident procedures |
| REGULATORY_COMPLIANCE_MANUAL | `../REGULATORY_COMPLIANCE_MANUAL.md` | Vendor details |

---

## Annex A: Supplier Categories

| Category | Definition | Review | Examples |
|----------|-----------|--------|----------|
| **Critical** | Core service, PII/financial access | Cloud, payments | Annual + quarterly |
| **Important** | Business function | Email, analytics | Annual |
| **Standard** | Utility service | Monitoring | Biennial |
| **Low** | No data access | Public tools | As needed |

---

## Annex B: Security Requirements Matrix

| Requirement | Critical | Important | Standard | Low |
|-------------|----------|-----------|----------|------|
| SOC 2 Type II | Required | Required | - | - |
| ISO 27001 | Required | Preferred | - | - |
| Encryption at rest | AES-256 | AES-256 | - | - |
| Encryption in transit | TLS 1.3 | TLS 1.2+ | TLS 1.2 | - |
| MFA | Required | Recommended | - | - |
| DPA | Required | Required | - | - |
| 24h breach notice | Required | - | - | - |
| 72h breach notice | - | Required | - | - |
| Annual audit | Required | Annual | - | - |
| Named security team | Required | Required | - | - |

---

## Annex C: DPA Template

```
┌─────────────────────────────────────────────────────────────────┐
│              DATA PROCESSING AGREEMENT                           │
├─────────────────────────────────────────────────────────────┤
│                                                          │
│ PARTY A: NoTap                                           │
│ PARTY B: [Supplier Name]                                  │
│                                                          │
│ 1. SUBJECT MATTER                                       │
│    Personal data processing for [service description]       │
│                                                          │
│ 2. DURATION                                           │
│    Effective from [date] until [termination date]        │
│                                                          │
│ 3. NATURE AND PURPOSE                                   │
│    [Description of processing activities]              │
│                                                          │
│ 4. CATEGORIES OF DATA                                  │
│    □ Identity (name, email, phone)                     │
│    □ Financial (payment data)                         │
│    □ Technical (IP, device)                        │
│    □ Behavioral (usage patterns)                   │
│                                                          │
│ 5. DATA SUBJECTS                                     │
│    □ End users            □ Merchants                  │
│    □ Employees                                          │
│                                                          │
│ 6. OBLIGATIONS OF PROCESSOR                           │
│    □ Process only on instructions                   │
│    □ Ensure confidentiality                          │
│    □ Security measures per Art. 32                 │
│    □ Sub-processor only with consent              │
│    □ Assist with data subject rights               │
│    □ Delete/return at termination           │
│    □ Audit rights                          │
│                                                          │
│ 7. SECURITY MEASURES                                  │
│    □ Encryption at rest (AES-256)                        │
│    □ Encryption in transit (TLS 1.2+)                 │
│    □ Access control (RBAC)                  │
│    □ MFA for admin access                    │
│    □ Logging and monitoring                     │
│    □ Vulnerability management              │
│    □ Incident response plan                 │
│                                                          │
│ 8. SUB-PROCESSORS                                   │
│    □ None without written consent                  │
│    □ 30 days notice for new sub-processor        │
│    □ Same obligations flow downstream          │
│                                                          │
│ 9. DATA TRANSFER                                    │
│    □ [Specify mechanism: adequacy/SCCs/BCRs]        │
│                                                          │
│ 10. BREACH NOTIFICATION                             │
│     □ Within [24/48/72] hours                    │
│     □ To: security@notap.io                     │
│                                                          │
│ 11. TERMINATION                                  │
│     □ 90 days notice                              │
│     □ Data return within 30 days               │
│     □ Deletion confirmation                    │
│                                                          │
│ SIGNATURES:                                         │
│                                                          │
│ NoTap: _________________ Date: __________               │
│ Supplier: _________________ Date: __________          │
│                                                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Document Control

- **Version**: 1.0.0
- **Status**: DRAFT
- **Approved By**: Executive Sponsor (pending)
- **Effective Date**: Upon approval
- **Next Review**: 2027-04-22
- **Location**: `documentation/05-security/POLICIES/POL-008_SUPPLIER_SECURITY_POLICY.md`

**For questions**: Contact Security Team Lead or see CLAUDE.md for security contacts.

(End of file - 592 lines)