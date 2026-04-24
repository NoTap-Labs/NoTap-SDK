# NoTap Data Protection Policy

> **Records of Processing Activities (ROPA)** — Comprehensive data inventory and processing documentation
> Required by GDPR Article 30, CCPA data inventory requirements, and ISO 27001 controls A.8.15-16.

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-22 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Definitions](#2-definitions)
3. [Data Inventory](#3-data-inventory)
4. [Processing Activities](#4-processing-activities)
5. [Legal Basis Matrix](#5-legal-basis-matrix)
6. [Data Subject Rights](#6-data-subject-rights)
7. [International Transfers](#7-international-transfers)
8. [Retention & Deletion](#8-retention--deletion)
9. [Security Measures](#9-security-measures)
10. [Roles & Responsibilities](#10-roles--responsibilities)
11. [Monitoring & Compliance](#11-monitoring--compliance)
12. [Annex A: Processing Activities Register](#annex-a-processing-activities-register)
13. [Annex B: Data Subject Categories](#annex-b-data-subject-categories)
14. [Annex C: Third-Party Processors](#annex-c-third-party-processors)

---

## 1. Purpose & Scope

### 1.1 Purpose

This policy establishes the Records of Processing Activities (ROPA) for NoTap to ensure:

- **GDPR Article 30 Compliance**: Complete records of all processing activities
- **Data Inventory Management**: Comprehensive data asset inventory
- **Regulatory Defense**: Evidence of compliance for authorities
- **Accountability**: Demonstrable compliance with data protection principles
- **Transparency**: Clear documentation for data subjects

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| All personal data processing | Employee HR data |
| All customer data | Third-party vendor data |
| All authentication data | Public information |
| All payment-related data | |
| All administrative data | |

### 1.3 Regulatory Alignment

| Regulation | Requirement | Policy Section |
|------------|-------------|----------------|
| GDPR Art. 30 | Records of Processing | Sections 3, 4, Annex A |
| GDPR Art. 5 | Data Processing Principles | Section 5 |
| GDPR Art. 32 | Security Measures | Section 9 |
| CCPA | Data Inventory | Sections 3, 4 |
| ISO 27001 A.8.15 | Production & Release | Section 8 |

### 1.4 Key Relationships

| Document | Relationship |
|----------|---------------|
| POL-001 Information Security | Master policy - security commitments |
| POL-002 Risk Assessment | Risk-based processing decisions |
| POL-004 Privacy & Consent | Legal basis for processing |
| DATA_CLASSIFICATION_POLICY | Data sensitivity classification |
| PRIVACY_IMPLEMENTATION.md | Implementation details |

---

## 2. Definitions

| Term | Definition |
|------|-------------|
| **Personal Data** | Any information relating to an identified natural person (GDPR Art. 4(1)) |
| **Processing** | Any operation performed on personal data (GDPR Art. 4(2)) |
| **Controller** | Entity determining purposes and means of processing (GDPR Art. 4(7)) |
| **Processor** | Entity processing on controller's behalf (GDPR Art. 4(8)) |
| **Data Subject** | Identified or identifiable natural person |
| **ROPA** | Records of Processing Activities (GDPR Art. 30) |
| **Legal Basis** | GDPR Article 6(1) justification for processing |
| **Special Category** | Personal data revealing race, health, biometrics, etc. (GDPR Art. 9) |
| **Third Country** | Non-EU/EEA country |
| **Adequacy Decision** | EU determination of adequate protection level |

---

## 3. Data Inventory

### 3.1 Data Categories

| Category | Data Types | Classification | Volume (Est.) |
|----------|-------------|----------------|----------------|
| **Identity Data** | Name, email, phone, address | CONFIDENTIAL | 100K+ |
| **Authentication Data** | Factor digests, biometrics | RESTRICTED | 100K+ |
| **Financial Data** | Payment tokens, wallet addresses | CONFIDENTIAL | 10K+ |
| **Device Data** | Device IDs, fingerprints | CONFIDENTIAL | 100K+ |
| **Behavioral Data** | Usage patterns, verification history | CONFIDENTIAL | 1M+ |
| **Technical Data** | IP addresses, logs | INTERNAL | 10M+ |
| **Business Data** | Merchant IDs, transaction records | INTERNAL | 500K+ |

### 3.2 Data Sources

| Source | Data Received | Purpose |
|--------|---------------|----------|
| End Users (Enrollment) | Identity, authentication factors, device info | Identity verification |
| End Users (Verification) | Biometric samples, authentication attempts | Authentication |
| Merchants | Business information, API credentials | Payment integration |
| Payment Processors | Transaction data, payment status | Payment processing |
| Device Sensors | Device fingerprint, location | Device trust |
| Internal Systems | Audit logs, usage metrics | Service improvement |

### 3.3 Data Processing Systems

| System | Data Stored | Retention | Encryption |
|--------|-------------|------------|-------------|
| **PostgreSQL (Primary)** | User accounts, merchants, transactions | Per category | AES-256-GCM + KMS |
| **Redis (Cache)** | Sessions, verification state, rate limits | 24 hours max | TLS + field encryption |
| **AWS KMS** | Encryption keys | Per customer | AWS-managed |
| **AWS S3** | Audit logs, backups | 7 years | AES-256 |

### 3.4 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        DATA FLOW ARCHITECTURE                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────┐     ┌─────────────┐     ┌──────────────────┐          │
│  │  USER   │────▶│ ENROLLMENT  │────▶│  FACTOR DIGEST    │          │
│  │ DEVICE  │     │   SERVICE   │     │    (Redis)       │          │
│  └─────────┘     └─────────────┘     └────────┬─────────┘          │
│                                             │                        │
│                                             ▼                        │
│  ┌─────────┐     ┌─────────────┐     ┌──────────────────┐          │
│  │ MERCHANT│────▶│ VERIFICATION│────▶│   AUTHENTICATION │          │
│  │  API    │     │   SERVICE   │     │    RESULT        │          │
│  └─────────┘     └─────────────┘     └────────┬─────────┘          │
│                                             │                        │
│                                             ▼                        │
│  ┌─────────┐     ┌─────────────┐     ┌──────────────────┐          │
│  │   PSP   │◀────│  PAYMENT   │◀────│   TRANSACTION    │          │
│  │        │     │  SERVICE   │     │    RECORDS       │          │
│  └─────────┘     └───────────���─┘     └────────┬─────────┘          │
│                                             │                        │
│                                             ▼                        │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │              POSTGRESQL (Encrypted)                      │       │
│  │   Users │ Merchants │ Transactions │ Audit Logs           │       │
│  └──────────────────────────────────────────────────────────┘       │
│                                                                     │
│  LEGEND: ──────▶ Data Flow    ▣ Storage    ┌──────┐ Processing      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 4. Processing Activities

### 4.1 Core Processing Activities

| Activity ID | Processing Purpose | Data Categories | Legal Basis |
|-------------|-------------------|-----------------|-------------|
| PA-001 | User Enrollment | Identity, Auth, Device | Art. 6(1)(b) - Contract |
| PA-002 | Authentication Verification | Auth, Device | Art. 6(1)(b) - Contract |
| PA-003 | Payment Processing | Financial, Identity | Art. 6(1)(b) - Contract |
| PA-004 | Account Management | Identity | Art. 6(1)(b) - Contract |
| PA-005 | Customer Support | Identity | Art. 6(1)(f) - Legitimate Interest |
| PA-006 | Fraud Prevention | Behavioral, Technical | Art. 6(1)(f) - Legitimate Interest |
| PA-007 | Service Improvement | Technical, Behavioral | Art. 6(1)(f) - Legitimate Interest |
| PA-008 | Legal Compliance | All | Art. 6(1)(c) - Legal Obligation |
| PA-009 | Biometric Authentication | Biometric (Special) | Art. 9(2)(a) - Explicit Consent |

### 4.2 Processing Activity Details

#### PA-001: User Enrollment

| Attribute | Value |
|-----------|-------|
| **Purpose** | Create and maintain user authentication enrollment |
| **Data Subjects** | End users enrolling authentication factors |
| **Data Categories** | Name, email, phone, factor digests, device fingerprints |
| **Legal Basis** | GDPR Art. 6(1)(b) - Contract performance |
| **Recipients** | NoTap internal only |
| **Retention** | Active + 30 days post-deactivation |
| **Automated Decisions** | Factor validation |
| **Profiling** | None |

#### PA-002: Authentication Verification

| Attribute | Value |
|-----------|-------|
| **Purpose** | Verify user identity for payment authentication |
| **Data Subjects** | End users verifying for transactions |
| **Data Categories** | Factor digests, verification attempts, device trust |
| **Legal Basis** | GDPR Art. 6(1)(b) - Contract performance |
| **Recipients** | PSPs for transaction verification |
| **Retention** | 30 days (usage logs), 90 days (verification history) |
| **Automated Decisions** | Factor selection, verification result |
| **Profiling** | Risk-based factor selection |

#### PA-003: Payment Processing

| Attribute | Value |
|-----------|-------|
| **Purpose** | Process payment verification requests |
| **Data Subjects** | End users and merchants |
| **Data Categories** | Payment tokens, transaction amounts, merchant IDs |
| **Legal Basis** | GDPR Art. 6(1)(b) - Contract performance |
| **Recipients** | Stripe, Tilopay, Adyen, MercadoPago |
| **Retention** | Per PSP requirements (7 years) |
| **Automated Decisions** | Payment status |
| **Profiling** | None |

#### PA-009: Biometric Authentication

| Attribute | Value |
|-----------|-------|
| **Purpose** | Enable biometric factor verification |
| **Data Subjects** | Users enrolling biometric factors (voice, face, fingerprint) |
| **Data Categories** | Biometric templates (voice print, face encoding) |
| **Legal Basis** | GDPR Art. 9(2)(a) - Explicit consent |
| **BIPA Compliance** | Illinois users: electronic consent required |
| **Retention** | Active + 30 days post-deactivation |
| **Special Category** | Biometric data (GDPR Art. 9) |

---

## 5. Legal Basis Matrix

### 5.1 GDPR Article 6(1) Legal Basis

| Legal Basis | Applicable Activities | Documentation Required |
|------------|---------------------|----------------------|
| **Art. 6(1)(a) - Consent** | Marketing, non-essential cookies | Consent record, withdrawal mechanism |
| **Art. 6(1)(b) - Contract** | Enrollment, authentication, payments | Terms of service acceptance |
| **Art. 6(1)(c) - Legal Obligation** | Tax, fraud prevention | Legal requirement reference |
| **Art. 6(1)(f) - Legitimate Interest** | Support, fraud prevention, improvement | LIA document (see POL-004) |

### 5.2 Consent Requirements by Processing

| Activity | Consent Type | Consent Mechanism | Withdrawal |
|----------|--------------|-------------------|------------|
| Biometric authentication | Explicit (Art. 9(2)(a)) | Signed electronic consent | Account deletion |
| Marketing communications | Opt-in (Art. 6(1)(a)) | Email/link click | One-click unsubscribe |
| Non-essential cookies | Opt-in (ePrivacy) | Cookie banner | Browser settings |
| Third-party sharing | Opt-in (Art. 6(1)(a)) | Clear disclosure | Contact DPO |

### 5.3 Lawful Processing Checklist

For each processing activity, verify:

- [ ] Legal basis identified and documented
- [ ] Purpose limitation observed
- [ ] Data minimization applied
- [ ] Accuracy maintained
- [ ] Storage limitation enforced
- [ ] Integrity and confidentiality protected
- [ ] Accountability demonstrated
- [ ] Data subject rights enabled

---

## 6. Data Subject Rights

### 6.1 GDPR Rights Implementation

| Right | GDPR Art. | Implementation | Response Time |
|-------|----------|----------------|---------------|
| **Right to Access** | Art. 15 | POST /v1/management/export | 30 days |
| **Right to Rectification** | Art. 16 | Profile update API | 30 days |
| **Right to Erasure** | Art. 17 | POST /v1/management/delete | 30 days |
| **Right to Restrict** | Art. 18 | Account settings | 30 days |
| **Right to Portability** | Art. 20 | Export API (JSON) | 30 days |
| **Right to Object** | Art. 21 | Contact DPO | 30 days |
| **Rights re: Automated** | Art. 22 | Human review available | Immediate |

### 6.2 CCPA Rights Implementation

| Right | CCPA | Implementation | Response Time |
|-------|-----|----------------|---------------|
| **Right to Know** | 1798.100 | Privacy disclosure + data export | 45 days |
| **Right to Delete** | 1798.105 | Deletion API | 45 days |
| **Right to Correct** | 1798.106 | Correction API | 45 days |
| **Right to Opt-Out** | 1798.120 | "Do Not Sell" link | Immediate |
| **Right to Limit** | 1798.182 | Limit sensitive data use | Immediate |

### 6.3 BIPA Rights Implementation

| Right | BIPA | Implementation |
|-------|-----|----------------|
| **Right to Notice** | 740 ILCS 14/10 | Written consent with 6 disclosures |
| **Right to Consent** | 740 ILCS 14/15 | Electronic signature |
| **Right to Delete** | 740 ILCS 14/20 | Written request, 3-year retention |

### 6.4 Data Subject Request Process

```
┌─────────────────────────────────────────────────────────────────┐
│              DATA SUBJECT REQUEST PROCESS                    │
├─────────────────────────────────────────────────────────────┤
│                                                          │
│  Step 1: Request Received                                 │
│  - Verify identity (2FA required)                         │
│  - Log request timestamp                                 │
│                                                          │
│  Step 2: Request Validation                             │
│  - Validate request type                                │
│  - Check request validity                              │
│  - Identify scope                                    │
│                                                          │
│  Step 3:Request Processing                            │
│  - Execute request (per rights matrix)               │
│  - Compile data (if applicable)                      │
│                                                          │
│  Step 4: Response                                  │
│  - Format response (JSON/PDF)                     │
│  - Verify completeness                             │
│  - Send to data subject                          │
│                                                          │
│  Step 5: Documentation                             │
│  - Log completion                                │
│  - Update ROPA                                   │
│  - Close ticket                                │
│                                                          │
│  TIMELINES: GDPR = 30 days | CCPA = 45 days         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. International Transfers

### 7.1 Transfer Mechanisms

| Transfer Type | Mechanism | Safeguards | Status |
|--------------|-----------|------------|---------|
| **Intra-group** | BCRs (pending) | Binding Corporate Rules | Pending |
| **Third Countries** | SCCs | Standard Contractual Clauses | Active |
| **Payment Processors** | Adequacy + SCCs | EU-US Data Privacy Framework | Active |
| **AWS** | AWS SCCs | AWS DPA | Active |

### 7.2 Transfer Risk Assessment

| Transfer | Risk Level | Mitigation | Review Frequency |
|----------|------------|------------|-------------------|
| AWS (US) | Low | Adequacy + DPA | Annual |
| Stripe (US) | Low | Adequacy + SCC | Annual |
| Tilopay (US) | Low | SCC | Annual |
| Adyen (NL) | Low | Intra-company | Annual |

### 7.3 Transfer Documentation

For each transfer, maintain:

- [ ] Transfer mechanism reference
- [ ] Third country adequacy status
- [ ] Safeguards in place
- [ ] Transfer impact assessment
- [ ] Review date

---

## 8. Retention & Deletion

### 8.1 Retention Periods

| Data Category | Active Retention | Archive Retention | Deletion Method |
|---------------|-----------------|------------------|----------------|
| User accounts | Active + 30 days | 30 days post-deletion | Secure erase |
| Factor digests | Active + 30 days | 30 days post-deletion | Secure erase |
| Verification logs | 90 days | None | Auto-delete |
| Payment records | 7 years | 7 years | Secure erase |
| Audit logs | 7 years | 7 years | Secure erase |
| Support tickets | 3 years | 3 years | Secure erase |
| Marketing data | Until consent withdrawn | 30 days post-withdrawal | Secure erase |

### 8.2 Deletion Procedures

| Trigger | Action | Timeline | Verification |
|--------|--------|----------|-------------|
| Account deletion request | Soft delete + data purge | 30 days | Audit log |
| Consent withdrawal | Marketing data deletion | 30 days | Confirmation |
| Contract termination | Full data deletion | 30 days | Audit log |
| Legal hold | Suspend deletion | Until released | Legal review |
| Inactivity (3 years) | Account deletion | Notice + 30 days | Email notice |

### 8.3 Secure Deletion Standards

All deletion must meet:

- **NIST 800-88**: Clear or purge based on media type
- **Verification**: Deletion certificate retained
- **Audit**: Deletion logged with timestamp
- **Documentation**: ROPA updated

---

## 9. Security Measures

### 9.1 Technical Measures (GDPR Art. 32)

| Measure | Implementation | Effectiveness |
|--------|----------------|---------------|
| **Encryption at rest** | AES-256-GCM + KMS | High |
| **Encryption in transit** | TLS 1.3 | High |
| **Access control** | Role-based, least privilege | High |
| **MFA** | NoTap factors | High |
| **Logging** | Structured, PII-redacted | Medium |
| **Testing** | Monthly vulnerability scans | Medium |
| **Incident response** | POL-006 (pending) | Medium |

### 9.2 Organizational Measures

| Measure | Implementation |
|--------|----------------|
| **Data protection policies** | POL-001, POL-003, POL-004 |
| **Training** | Annual GDPR + security training |
| **DPO** | Designated privacy officer |
| **Documentation** | ROPA this document |
| **Impact assessments** | POL-002 (DPIA required) |
| **Breach notification** | 72-hour notification capability |

### 9.3 Privacy by Design Implementation

| Requirement | Implementation |
|--------------|---------------|
| Data minimization | Collect only necessary data |
| Purpose limitation | Defined in ROPA per activity |
| Storage limitation | TTL enforcement, automated cleanup |
| Integrity | Input validation, checksums |
| Accountability | Audit logging, documentation |

---

## 10. Roles & Responsibilities

| Role | Responsibility | GDPR Art. Reference |
|------|----------------|--------------------|
| **DPO** | ROPA maintenance, data subject liaison | Art. 39 |
| **Security Team Lead** | Technical security, retention enforcement | Art. 32 |
| **Data Protection Office** | Request handling, rights fulfillment | Art. 30 |
| **All Staff** | Processing documentation, data accuracy | Art. 5 |

---

## 11. Monitoring & Compliance

### 11.1 Compliance Monitoring

| Check | Frequency | Owner | Documentation |
|--------|-----------|-------|--------------|
| ROPA accuracy | Quarterly | DPO | Update log |
| Retention enforcement | Monthly | Security | Audit report |
| Request handling | Monthly | DPO | Response metrics |
| Third-party compliance | Annual | Legal | DPA review |
| DPIA updates | As needed | DPO | Assessment |

### 11.2 Audit Requirements

| Audit Type | Frequency | Scope |
|-----------|-----------|--------|
| Internal ROPA review | Annual | Full ROPA |
| External GDPR audit | Every 3 years | Selected activities |
| Regulatory inspection | As requested | All records |

### 11.3 Non-Compliance Consequences

| Violation | Consequence |
|-----------|-------------|
| Missing ROPA entry | GDPR Art. 83(4) - Warning |
| Incorrect data | GDPR Art. 83(4) - Up to 2% revenue |
| Data breach | GDPR Art. 83(5) - Up to 4% revenue |
| Blocked subject access | GDPR Art. 83(6) - Enforcement |

---

## Annex A: Processing Activities Register

| ID | Activity | Controller | Processor | Purpose | Legal Basis | Categories | Retention | Transfers |
|----|----------|------------|------------|----------|-------------|-------------|------------|------------|
| PA-001 | User enrollment | NoTap | AWS | Account creation | Art. 6(1)(b) | Identity, Auth | Active+30d | None |
| PA-002 | Authentication | NoTap | AWS | Verification | Art. 6(1)(b) | Auth | 90 days | None |
| PA-003 | Payment | NoTap | PSPs | Transaction | Art. 6(1)(b) | Financial | 7 years | PSPs |
| PA-004 | Support | NoTap | Internal | Customer service | Art. 6(1)(f) | Identity | 3 years | None |
| PA-005 | Fraud prevention | NoTap | Internal | Fraud prevention | Art. 6(1)(c) | Technical | 7 years | None |
| PA-006 | Marketing | NoTap | Email provider | Communications | Art. 6(1)(a) | Identity | Consent+30d | Email provider |
| PA-007 | Biometric auth | NoTap | AWS | Verification | Art. 9(2)(a) | Biometric | Active+30d | None |
| PA-008 | Analytics | NoTap | Internal | Improvement | Art. 6(1)(f) | Technical | 2 years | None |

---

## Annex B: Data Subject Categories

| Category | Description | Examples | Rights Requests Est. |
|----------|-------------|-----------|---------------------|
| **End Users** | Authentication users | Enrollment, verification | 100/year |
| **Merchants** | Business customers | API keys, transactions | 20/year |
| **Support Contacts** | Help requests | Account issues | 500/year |
| **Inactive** | Dormant accounts | Re-engagement | 10/year |

---

## Annex C: Third-Party Processors

| Processor | Purpose | Data Processed | DPA | Transfer |
|----------|---------|---------------|-----|---------|
| AWS | Cloud hosting | All | AWS DPA |
| Stripe | Payment processing | Financial | SCC |
| Tilopay | Payment processing | Financial | SCC |
| Adyen | Payment processing | Financial | SCC |
| SendGrid | Email delivery | Email | DPA |

---

## Document Control

- **Version**: 1.0.0
- **Status**: DRAFT
- **Approved By**: DPO (pending), Executive Sponsor (pending)
- **Effective Date**: Upon approval
- **Next Review**: 2027-04-22
- **Location**: `documentation/05-security/POLICIES/POL-003_DATA_PROTECTION_POLICY.md`

**For questions**: Contact DPO or see CLAUDE.md for contacts.

(End of file - 616 lines)