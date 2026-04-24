# NoTap Information Security Policy

> **Master Policy** — Foundation document for the Information Security Management System (ISMS)
> All other policies, standards, and procedures derive authority from this document.

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-22 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Definitions](#2-definitions)
3. [Policy Statement](#3-policy-statement)
4. [Leadership & Commitment](#4-leadership--commitment)
5. [Roles & Responsibilities](#5-roles--responsibilities)
6. [Information Security Objectives](#6-information-security-objectives)
7. [ISMS Scope & Boundaries](#7-isms-scope--boundaries)
8. [Compliance Framework](#8-compliance-framework)
9. [Risk Management](#9-risk-management)
10. [Resource Provision](#10-resource-provision)
11. [Competence & Awareness](#11-competence--awareness)
12. [Documented Information](#12-documented-information)
13. [Operational Planning & Control](#13-operational-planning--control)
14. [Incident Management](#14-incident-management)
15. [Continual Improvement](#15-continual-improvement)
16. [Related Documents](#16-related-documents)
17. [Review & Maintenance](#17-review--maintenance)
18. [Change Log](#18-change-log)

---

## 1. Purpose & Scope

### 1.1 Purpose

This Information Security Policy establishes NoTap's commitment to protecting information assets and defines the governance structure for the Information Security Management System (ISMS).

**Objectives of this Policy:**
- Establish strategic direction for information security
- Define roles and responsibilities for security governance
- Set information security objectives aligned with business goals
- Ensure compliance with applicable regulatory requirements
- Provide framework for establishing and reviewing security policies
- Commit to continual improvement of the ISMS

### 1.2 Scope

| Included in ISMS Scope | Excluded from ISMS Scope |
|------------------------|-------------------------|
| Backend API services (Node.js) | Physical premises (cloud-hosted) |
| Kotlin Multiplatform SDK | Employee personal devices |
| Web frontend (Kotlin/JS) | Third-party vendor internal systems |
| Database services (PostgreSQL, Redis) | |
| AWS KMS integration | |
| All user enrollment and verification data | |
| All merchant and payment processing data | |
| All system configuration and secrets | |

**Rationale:** This scope encompasses all systems that process, store, or transmit:
- User authentication factors
- Payment verification data
- Personal identifiable information (PII)
- Cryptographic secrets
- Audit records

### 1.3 Applicability

This policy applies to:
- All NoTap employees, contractors, and consultants
- All systems and infrastructure under NoTap control
- All third-party processors acting on behalf of NoTap
- All data processed for NoTap customers

### 1.4 Regulatory Notice

This policy supports compliance with:
- **ISO 27001:2022** — Information security management systems
- **GDPR** — General Data Protection Regulation (EU)
- **CCPA/CPRA** — California Consumer Privacy Act
- **PIPEDA** — Personal Information Protection and Electronic Documents Act (Canada)
- **BIPA** — Illinois Biometric Information Privacy Act
- **NIS2** — Network and Information Security Directive 2 (EU)
- **PCI DSS v4.0** — Payment Card Industry Data Security Standard
- **SOC 2 Type II** — Trust Services Criteria

---

## 2. Definitions

| Term | Definition |
|------|-------------|
| **ISMS** | Information Security Management System — systematic approach to managing sensitive information |
| **Information Asset** | Data or information stored, processed, or transmitted that has value to the organization |
| **Risk** | Effect of uncertainty on information security objectives |
| **Control** | Measure that modifies risk (preventive, detective, or corrective) |
| **DPO** | Data Protection Officer — responsible for GDPR compliance oversight |
| **PII** | Personal Identifiable Information — any data that can identify an individual |
| **Zero-Knowledge** | Authentication architecture where the server never sees raw factor data |
| **Digest** | SHA-256 hash of authentication factor — irreversible, non-extractable |
| **ZK Proof** | Zero-Knowledge Proof — cryptographic proof without revealing the secret |

---

## 3. Policy Statement

### 3.1 Executive Commitment

**NoTap commits to:**

1. **Protecting Information Assets** — All information assets will be protected against unauthorized access, disclosure, alteration, destruction, and disruption.

2. **Zero-Knowledge Architecture** — The authentication system is designed so that raw factor data never leaves the user's device. Only irreversible digests are transmitted and stored.

3. **Multi-Factor Security** — Minimum 3 authentication factors required for enrollment; risk-based selection (2-3 factors) for verification.

4. **Cryptographic Strength** — AES-256-GCM encryption, SHA-256 hashing, PBKDF2 with 100K+ iterations, HKDF-SHA256 for key rotation.

5. **Privacy by Design** — Data minimization, anonymization, and retention limits built into all systems per GDPR requirements.

6. **Compliance** — Meet all applicable regulatory requirements including GDPR, CCPA, BIPA, NIS2, and PCI DSS.

7. **Continual Improvement** — Regular review and enhancement of security controls based on risk assessment and incident learning.

### 3.2 Core Principles

| Principle | Implementation |
|-----------|---------------|
| **Confidentiality** | Encryption at rest and in transit; role-based access control |
| **Integrity** | Cryptographic hashes; constant-time comparisons; audit logging |
| **Availability** | Horizontal scaling; graceful degradation; Redis hybrid mode |
| **Authentication** | Multi-factor MFA; constant-time verification; anti-replay protection |
| **Non-Repudiation** | Comprehensive audit logging; immutable records |

### 3.3 Prohibited Practices

The following are explicitly prohibited:

- ❌ Storing authentication factors in plaintext
- ❌ Using MD5, SHA-1, DES, RC4, or ECB mode
- ❌ Using `Math.random()` for security purposes
- ❌ Hardcoding secrets in source code
- ❌ Using `contentEquals()` for digest comparison (timing attack vector)
- ❌ Redis `set()` without TTL (GDPR violation)
- ❌ Console.log in production code (bypasses PII redaction)
- ❌ Using `innerHTML` with dynamic data (XSS vulnerability)
- ❌ Android imports in commonMain (KMP violation)

---

## 4. Leadership & Commitment

### 4.1 Executive Sponsorship

**Executive Sponsor** (CTO/CEO) is responsible for:
- Approving the Information Security Policy
- Ensuring adequate resources for the ISMS
- Reviewing ISMS performance quarterly
- Approving major security investments
- Resolving resource conflicts

### 4.2 Management Commitment

**Security Team Lead** is responsible for:
- Maintaining and updating this policy
- Overseeing policy compliance
- Reporting security metrics to Executive Sponsor
- Managing security incidents
- Coordinating security assessments

### 4.3 Policy Authority

This policy is issued under the authority of NoTap Executive Management and supersedes any conflicting practices or procedures.

---

## 5. Roles & Responsibilities

### 5.1 Security Governance Structure

| Role | Responsibility | Reports To |
|------|----------------|-----------|
| Executive Sponsor | Strategic direction, resource allocation | Board/CEO |
| Security Team Lead | Policy oversight, incident response | Executive Sponsor |
| Security Engineers | Control implementation, monitoring | Security Team Lead |
| Developers | Secure development, compliance | Engineering Lead |
| DPO | Privacy compliance, GDPR oversight | Executive Sponsor |

### 5.2 Specific Responsibilities

| Role | Key Responsibilities |
|------|-------------------|
| **All Staff** | Read and comply with security policies; report incidents; attend training |
| **Developers** | Follow secure development standards; implement security controls; write secure code |
| **Security Team** | Monitor security; respond to incidents; maintain controls; conduct assessments |
| **DPO** | Oversee GDPR compliance; maintain ROPA; advise on privacy by design |
| **Engineering Lead** | Ensure security integrated in SDLC; resource security work |

### 5.3 Segregation of Duties

| Duty Separation | Implementation |
|----------------|----------------|
| Development vs Production | Separate access; CI/CD with approval |
| Security monitoring vs Operations | Independent monitoring team |
| Change management | Peer review required |
| Incident response | Trained responders, not developers |

---

## 6. Information Security Objectives

### 6.1 Strategic Objectives

| Objective | Target | Measurement |
|-----------|--------|------------|
| Protect user authentication data | 100% | Zero breaches of factor data |
| Maintain service availability | 99.9% | Uptime monitoring |
| Comply with GDPR | 100% | Audit findings |
| Prevent unauthorized access | 100% | No successful IDOR attacks |
| Maintain cryptographic strength | AES-256-GCM | Standards compliance |

### 6.2 Operational Objectives

| Objective | Target | Measurement |
|-----------|--------|------------|
| Encryption at rest | AES-256-GCM | All sensitive data |
| Encryption in transit | TLS 1.3 | All connections |
| Audit logging | Complete | All security events |
| Vulnerability remediation | < 30 days | Remediation tracking |
| Penetration testing | Annual | External pentest |

### 6.3 Compliance Objectives

| Regulation | Objective | Measurement |
|-----------|-----------|--------------|
| ISO 27001 | Certification readiness | Gap analysis < 10 items |
| GDPR | Full compliance | Zero Article 33 breaches |
| NIS2 | Security measures | Art. 21 compliance |
| SOC 2 | Type II readiness | Audit readiness |

---

## 7. ISMS Scope & Boundaries

### 7.1 Systems in Scope

```
┌─────────────────────────────────────────────────────────────┐
│                    ISMS SYSTEM BOUNDARY                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           NO INTERNAL SYSTEMS                     │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ │   │
│  │  │ Backend API│ │   SDK      │ │ Web UI      │ │   │
│  │  │ (Node.js)  │ │ (KMP)      │ │ (Kotlin/JS)│ │   │
│  │  └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ │   │
│  │        │             │             │             │   │
│  │  ┌─────┴─────────────┴─────────────┴──────┐   │   │
│  │  │         DATA LAYER                      │   │
│  │  │  ┌─────────────┐  ┌─────────────┐     │   │
│  │  │  │ PostgreSQL │  │   Redis     │     │   │
│  │  │  │ (KMS-wrap) │  │ (TTL+enc)  │     │   │
│  │  │  └─────────────┘  └─────────────┘     │   │
│  │  │  ┌─────────────────────────────────┐ │   │
│  │  │  │       AWS KMS (encryption)     │ │   │
│  │  │  └─────────────────────────────────┘ │   │
│  │  └───────────────────────────────────────┘   │
│  └──────────────────────────────────────────────┘   │
│                                                             │
│  ┌────────────────────────────────��────────────────────┐   │
│  │           EXTERNAL INTERFACES                       │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ │   │
│  │  │ Merchant   │ │   PSP      │ │  End User  │ │   │
│  │  │ Portal     │ │ Integration│ │ (Android)  │ │   │
│  │  └─────────────┘ └─────────────┘ └─────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 7.2 Information Assets

| Asset Category | Examples | Classification | Criticality |
|--------------|----------|----------------|------------|
| **RESTRICTED** | Passwords, API secrets, factor digests, biometrics | Highest | Critical |
| **CONFIDENTIAL** | PII, email, phone, wallet addresses | High | High |
| **INTERNAL** | UUIDs, merchant IDs, business data | Medium | Medium |
| **PUBLIC** | Status codes, API versions | Low | Low |

### 7.3 External Dependencies

| Service | Provider | Data Handled | Control |
|---------|----------|--------------|---------|
| Cloud Infrastructure | AWS | All | AWS security controls + NoTap encryption |
| Database | PostgreSQL | User data, audit logs | TLS + field encryption |
| Cache | Redis | Session data, verification state | TLS + TTL enforcement |
| Email | SMTP Provider | Transactional emails | DPA required |
| Payment PSPs | Stripe, Tilopay, etc. | Payment tokens only | Integration security |

---

## 8. Compliance Framework

### 8.1 Regulatory Compliance Matrix

| Regulation | Jurisdiction | Requirements | Policy Section |
|-----------|-------------|--------------|----------------|
| **GDPR** | European Union | Data protection, privacy rights | POL-003, POL-004 |
| **CCPA/CPRA** | California | Consumer privacy rights | POL-003, POL-004 |
| **PIPEDA** | Canada | Fair information principles | POL-003 |
| **BIPA** | Illinois | Biometric data protection | POL-004, WKI-001 |
| **NIS2** | European Union | Cybersecurity measures | POL-001, POL-006 |
| **PCI DSS** | Global | Payment card security | STD-001, POL-005 |

### 8.2 Certification Alignment

| Certification | Scope | Status |
|--------------|-------|--------|
| **ISO 27001:2022** | Entire ISMS | Gap analysis complete |
| **SOC 2 Type II** | Trust Services | Policy framework building |

### 8.3 Audit Requirements

| Audit Type | Frequency | Owner | Report To |
|------------|-----------|--------|--------|
| Internal ISMS Review | Annual | Security Team Lead | Executive Sponsor |
| External Penetration Test | Annual | External Vendor | Executive Sponsor |
| GDPR Compliance | Annual | DPO | Executive Sponsor, Regulators |
| ISO 27001 Certification | Every 3 years | External Auditor | Board |

---

## 9. Risk Management

### 9.1 Risk Assessment Approach

NoTap follows ISO 31000 risk management principles:

1. **Establish Context** — Define scope, criteria, and methodology
2. **Risk Identification** — Identify assets, threats, and vulnerabilities
3. **Risk Analysis** — Assess likelihood and impact
4. **Risk Evaluation** — Compare against risk criteria
5. **Risk Treatment** — Apply controls or accept risk
6. **Monitor and Review** — Continuous monitoring

### 9.2 Risk Appetite

| Risk Category | Appetite | Threshold |
|--------------|----------|-----------|
| Authentication compromise | Zero | Any successful attack |
| PII breach | Zero | GDPR Art. 33 notification required |
| Service availability | < 0.1% downtime | SLA breach |
| Cryptographic weakness | None | Standards non-compliance |
| Access control failure | Zero | Unauthorized access |

### 9.3 Control Framework

Controls are implemented per ISO 27001 Annex A:

| Control Area | Key Controls | Implementation |
|--------------|--------------|-----------------|
| **A.5 Organizational** | Policies, roles, segregation | This policy, INTERNAL_USER_ACCESS_POLICY |
| **A.6 People** | Screening, awareness, training | HR process, security training |
| **A.7 Physical** | Cloud-hosted (AWS responsibility) | N/A — managed by provider |
| **A.8 Technological** | Access, cryptography, operations | SECURITY_PATTERNS_REFERENCE |

---

## 10. Resource Provision

### 10.1 Security Resources

| Resource | Commitment | Measurement |
|----------|-------------|-------------|
| **Personnel** | Qualified security team | Certifications, training |
| **Technology** | Security tools and infrastructure | Security tooling budget |
| **Time** | Security reviews and assessments | Sprint allocation |
| **Training** | Annual security training | 100% completion |

### 10.2 Infrastructure Security

| Component | Requirement | Implementation |
|-----------|-------------|----------------|
| Compute | Isolation, hardening | AWS security groups, VPC |
| Data | Encryption, backup | AES-256-GCM, automated backup |
| Network | Segmentation, monitoring | TLS 1.3, CloudWatch |
| Access | MFA, least privilege | Role-based, audit logging |

---

## 11. Competence & Awareness

### 11.1 Security Training Requirements

| Role | Training | Frequency | Content |
|------|----------|-----------|---------|
| All Staff | Security Awareness | Annual | Phishing, password, reporting |
| Developers | Secure Development | Annual | OWASP, secure coding |
| Security Team | Technical Training | Bi-annual | Certifications, emerging threats |
| Management | Governance | Annual | Risk, compliance |

### 11.2 Awareness Program

| Activity | Frequency | Audience | Owner |
|----------|-----------|----------|--------|
| Phishing simulation | Quarterly | All staff | Security Team |
| Security newsletter | Monthly | All staff | Security Team |
| Training completion tracking | Continuous | All staff | HR |
| Incident lessons learned | After incidents | All staff | Security Team |

---

## 12. Documented Information

### 12.1 Mandatory ISMS Documents

| Document | Location | Owner | Review |
|----------|----------|-------|--------|
| Information Security Policy | POLICIES/POL-001 | Security Team Lead | Annual |
| Risk Assessment | POLICIES/POL-002 | Security Team Lead | Annual |
| Data Protection Policy | POLICIES/POL-003 | DPO | Annual |
| Incident Response Procedure | POLICIES/POL-006 | Security Team Lead | Annual |
| Records of Processing (ROPA) | POLICIES/POL-003 | DPO | Quarterly |
| Security Patterns Reference | SECURITY_PATTERNS_REFERENCE | Security Team | Bi-annual |
| Access Control Policy | INTERNAL_USER_ACCESS_POLICY | Security Team | Annual |

### 12.2 Document Control Requirements

All controlled documents must include:
- Version number
- Author
- Approval status
- Review date
- Change log

---

## 13. Operational Planning & Control

### 13.1 Operational Security Controls

| Control | Implementation | Verification |
|---------|----------------|--------------|
| Change management | CI/CD pipeline with peer review | Git history |
| Vulnerability management | Automated scanning + remediation | npm audit, Trivy |
| Configuration management | Infrastructure as code | Terraform state |
| Backup and recovery | Automated backup, tested quarterly | Recovery tests |
| Monitoring | CloudWatch + alerting | Dashboard, alerts |

### 13.2 Operational Procedures

| Procedure | Reference | Owner |
|----------|-----------|--------|
| Access provisioning | WKI-001 | Security Team |
| Data retention cleanup | WKI-003 | Operations |
| Incident response | WKI-002 | Security Team |
| Change management | WKI-004 | Engineering Lead |

---

## 14. Incident Management

### 14.1 Incident Classification

| Severity | Definition | Response Time |
|----------|------------|---------------|
| **Critical** | Active breach, data exfiltration | Immediate |
| **High** | Suspected breach, service compromise | 1 hour |
| **Medium** | Security event, no breach | 4 hours |
| **Low** | Policy violation, suspicious activity | 24 hours |

### 14.2 Incident Response

Full procedure documented in **POL-006 Incident Response Procedure**.

Key requirements:
- All security incidents documented in audit_log
- Breach notification within 72 hours (GDPR Art. 33)
- NIS2 reporting within 24 hours (Art. 23)
- Post-incident review within 7 days

---

## 15. Continual Improvement

### 15.1 Improvement Sources

| Source | Input to Improvement |
|--------|---------------------|
| Incident lessons learned | Control updates |
| Audit findings | Policy updates |
| Risk assessment | Control additions |
| Technology changes | Technical standards |
| Regulatory changes | Compliance updates |

### 15.2 Improvement Process

1. **Identify** — Capture improvement opportunity
2. **Assess** — Evaluate impact and feasibility
3. **Plan** — Develop implementation plan
4. **Implement** — Deploy control change
5. **Verify** — Confirm effectiveness
6. **Document** — Update policies and procedures

### 15.3 Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Policy compliance | 100% | Audit findings |
| Incident recurrence | 0 | Post-incident reviews |
| Vulnerability remediation | < 30 days | Remediation tracking |
| Training completion | 100% | LMS records |

---

## 16. Related Documents

| Document | Location | Purpose |
|----------|----------|---------|
| **Policy Index** | `POLICIES/INDEX.md` | Framework overview |
| **Security Patterns** | `../SECURITY_PATTERNS_REFERENCE.md` | Security implementation |
| **Internal Access Policy** | `../INTERNAL_USER_ACCESS_POLICY.md` | Access control |
| **Data Classification** | `../DATA_CLASSIFICATION_POLICY.md` | Data classification |
| **Privacy Implementation** | `../PRIVACY_IMPLEMENTATION.md` | Privacy implementation |
| **Risk Assessment** | `../DATA_LIFECYCLE_RISK_ASSESSMENT.md` | Risk assessment |
| **Development Rules** | `../../03-developer-guides/DEVELOPMENT_RULES.md` | Secure development |
| **ISO Gap Analysis** | `../ISO_27002_GAP_ANALYSIS.md` | Compliance gap analysis |
| **GATES** | `../../10-internal/GATES.md` | Runtime controls |

---

## 17. Review & Maintenance

### 17.1 Review Schedule

| Review Type | Frequency | Owner | Approver |
|-------------|-----------|--------|---------|
| Full policy review | Annual | Security Team Lead | Executive Sponsor |
| Gap analysis update | Annual | Security Team Lead | Security Team Lead |
| Compliance mapping | Annual | DPO | Security Team Lead |
| Related documents | As needed | Document owner | Security Team Lead |

### 17.2 Approval Authority

| Change Type | Authority | Rationale |
|--------------|-----------|------------|
| Minor corrections | Security Team Lead | Maintenance |
| Standard updates | Security Team Lead | Operational |
| Policy changes | Executive Sponsor | Governance |
| New regulations | Board + Executive | Strategic |

### 17.3 Communication

When this policy is updated:
1. Version number incremented
2. Change log updated
3. Security team notified
4. Training updated if required
5. CLAUDE.md updated if development practices change

---

## 18. Change Log

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2026-04-22 | Claude | Initial policy — comprehensive framework based on ISO 27001:2022 |

---

## Appendix A: Quick Reference

### Security Commitments

| Commitment | Standard | Implementation |
|-----------|---------|----------------|
| Encryption at rest | AES-256-GCM | All sensitive data |
| Encryption in transit | TLS 1.3 | All connections |
| Key derivation | PBKDF2 100K+ | All password hashing |
| Key rotation | HKDF-SHA256 | Daily rotation |
| Authentication | MFA (3+ factors) | Risk-based selection |
| Logging | Structured, redacted | All security events |

### Prohibited Technologies

| Technology | Reason | Replacement |
|-----------|--------|-------------|
| MD5 | Collision attacks | SHA-256 |
| SHA-1 | Collision attacks | SHA-256 |
| DES/3DES | Key size too small | AES-256 |
| RC4 | Multiple vulnerabilities | AES-256-GCM |
| ECB mode | No semantic security | GCM mode |
| Math.random() | Predictable | crypto.randomBytes() |

---

**Document Control:**
- Version: 1.0.0
- Status: DRAFT
- Approved By: Executive Sponsor (pending)
- Effective Date: Upon approval
- Next Review: 2027-04-22
- Location: `documentation/05-security/POLICIES/POL-001.md`

**For questions:** See CLAUDE.md for security contacts.