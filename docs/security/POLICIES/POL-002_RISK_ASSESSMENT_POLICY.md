# NoTap Risk Assessment Policy

> **Risk Management Policy** — Framework for identifying, analyzing, and treating information security risks
> Establishes the methodology and criteria for risk assessments required by ISO 27001, GDPR, and NIS2.

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-22 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Definitions](#2-definitions)
3. [Risk Management Framework](#3-risk-management-framework)
4. [Risk Assessment Methodology](#4-risk-assessment-methodology)
5. [Risk Appetite & Criteria](#5-risk-appetite--criteria)
6. [Risk Treatment](#6-risk-treatment)
7. [Regulatory Requirements](#7-regulatory-requirements)
8. [Roles & Responsibilities](#7-roles--responsibilities)
9. [Monitoring & Review](#8-monitoring--review)
10. [Continual Improvement](#9-continual-improvement)
11. [Related Documents](#10-related-documents)
12. [Annex A: Risk Register Template](#annex-a-risk-register-template)
13. [Annex B: DPIA Template](#annex-b-dpia-template)

---

## 1. Purpose & Scope

### 1.1 Purpose

This policy establishes NoTap's systematic approach to identifying, analyzing, and treating information security risks to ensure:

- **Systematic Risk Management**: Consistent methodology across all information assets
- **Regulatory Compliance**: Meet GDPR Art. 35 (DPIA), NIS2 Art. 21 requirements
- **Informed Decision-Making**: Risk information available for management decisions
- **Resource Allocation**: Prioritize security investments based on risk prioritization
- **Continual Improvement**: Regular assessment and treatment of emerging risks

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| All information assets | Third-party vendor internal systems |
| All business processes | Physical premises (cloud-managed) |
| All system configurations | Employee personal devices |
| All customer data processing | |
| All authentication factor handling | |

### 1.3 Regulatory Alignment

| Regulation | Requirement | Policy Section |
|------------|-------------|----------------|
| ISO 27001 Cl. 6.1.2 | Risk Assessment | Sections 3, 4, 5 |
| ISO 27001 Cl. 6.1.3 | Risk Treatment | Section 6 |
| GDPR Art. 35 | Data Protection Impact Assessment | Annex B |
| NIS2 Art. 21 | Risk Management | Sections 3, 4, 7 |

### 1.4 Key Differences from POL-001

| Aspect | POL-001 | POL-002 |
|--------|--------|---------|
| Level | Strategic (what we commit to) | Operational (how we do it) |
| Focus | Governance structure | Risk assessment methodology |
| Audience | Executives, auditors | Security team, assessors |
| Review | Annual | Quarterly + as needed |

---

## 2. Definitions

| Term | Definition |
|------|-------------|
| **Risk** | Effect of uncertainty on objectives (ISO 31000) |
| **Risk Assessment** | Systematic process of identifying, analyzing, and evaluating risk |
| **Risk Analysis** | Process to understand risk nature and determine risk level |
| **Risk Evaluation** | Process to compare risk analysis results against criteria |
| **Risk Treatment** | Process to modify risk (avoid, modify, transfer, accept) |
| **Residual Risk** | Risk remaining after treatment |
| **Risk Appetite** | Amount and type of risk an organization is willing to accept |
| **Threat** | Potential cause of an unwanted incident |
| **Vulnerability** | Weakness that can be exploited by a threat |
| **DPIA** | Data Protection Impact Assessment (GDPR Art. 35) |
| **Inherent Risk** | Risk before controls are applied |
| **Residual Risk** | Risk after controls are applied |

---

## 3. Risk Management Framework

### 3.1 Framework Overview

```
┌──────────────────��──────────────────────────────────────────────────────────┐
│                   RISK MANAGEMENT PROCESS                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐                                           │
│  │   1. SCOPE  │◄───────── Define assessment boundary        │
│  └──────┬───────┘                                           │
│         │                                                    │
│         ▼                                                    │
│  ┌──────────────┐                                           │
│  │ 2. IDENTIFY │◄──────── Identify assets, threats,          │
│  │   ASSETS    │          vulnerabilities                      │
│  └──────┬───────┘                                           │
│         │                                                    │
│         ▼                                                    │
│  ┌──────────────┐                                           │
│  │ 3. ANALYZE  │◄──────── Assess likelihood and              │
│  │   RISK      │          impact                             │
│  └──────┬───────┘                                           │
│         │                                                    │
│         ▼                                                    │
│  ┌──────────────┐                                           │
│  │ 4. EVALUATE │◄──────── Compare against risk               │
│  │   RISK      │          criteria                          │
│  └──────┬───────┘                                           │
│         │                                                    │
│         ▼                                                    │
│  ┌──────────────┐                                           │
│  │ 5. TREAT    │◄──────── Apply controls,                  │
│  │   RISK      │          transfer, accept                   │
│  └──────┬───────┘                                           │
│         │                                                    │
│         ▼                                                    │
│  ┌──────────────┐                                           │
│  │ 6. MONITOR  │◄──────── Continual review,                 │
│  │   & REVIEW  │          update                           │
│  └──────────────┘                                           │
│                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Risk Assessment Types

| Type | Trigger | Frequency | Owner |
|------|---------|-----------|-------|
| **Annual Enterprise** | Annual review cycle | Yearly | Security Team Lead |
| **Quarterly Update** | Changed risk landscape | Quarterly | Security Team |
| **Ad Hoc Assessment** | Significant change | As needed | Security Team |
| **DPIA (GDPR Art. 35)** | New processing, high risk | Before processing | DPO |
| **NIS2 Assessment** | Essential entity requirements | Annual | Security Team Lead |

### 3.3 Assessment Boundary

Each risk assessment must define:

1. **Information Assets in Scope**
   - Systems, data stores, interfaces
   - Physical assets (if any)
   - Personnel with access

2. **Business Processes Covered**
   - Authentication flows
   - Enrollment processes
   - Payment processing
   - Administrative functions

3. **External Dependencies**
   - Cloud providers (AWS)
   - Third-party services
   - Payment processors

4. **Time Period**
   - Assessment date range
   - Validity period

---

## 4. Risk Assessment Methodology

### 4.1 Risk Calculation Formula

```
Risk Score = Likelihood × Impact
```

Where:
- **Likelihood**: Probability of threat exploiting vulnerability (1-5 scale)
- **Impact**: Consequence if risk materializes (1-5 scale)

### 4.2 Likelihood Scale

| Level | Score | Description | Annual Frequency |
|-------|-------|-------------|-------------------|
| **Very Unlikely** | 1 | Less than 1% probability | < 1 incident/yr |
| **Unlikely** | 2 | 1-10% probability | 1-2 incidents/yr |
| **Possible** | 3 | 10-30% probability | 3-5 incidents/yr |
| **Likely** | 4 | 30-70% probability | 6-10 incidents/yr |
| **Very Likely** | 5 | > 70% probability | > 10 incidents/yr |

### 4.3 Impact Scale

| Level | Score | Description | Example |
|-------|-------|-------------|----------|
| **Negligible** | 1 | Minimal impact, easily recovered | Minor spam, temporary annoyance |
| **Minor** | 2 | Limited impact, quick recovery | Single user inconvenience, < 1hr outage |
| **Moderate** | 3 | Noticeable impact, managed recovery | Department affected, 1-4hr outage |
| **Major** | 4 | Significant impact, major recovery | Business impact, 4-24hr outage |
| **Severe** | 5 | Catastrophic impact, unrecoverable | Data breach, regulatory fine, business failure |

### 4.4 Risk Matrix

| Impact \ Likelihood | 1 | 2 | 3 | 4 | 5 |
|--------------------|---|---|---|---|---|
| **5 (Severe)** | MEDIUM | HIGH | CRITICAL | CRITICAL | CRITICAL |
| **4 (Major)** | MEDIUM | MEDIUM | HIGH | CRITICAL | CRITICAL |
| **3 (Moderate)** | LOW | MEDIUM | MEDIUM | HIGH | CRITICAL |
| **2 (Minor)** | LOW | LOW | MEDIUM | MEDIUM | HIGH |
| **1 (Negligible)** | LOW | LOW | LOW | MEDIUM | MEDIUM |

### 4.5 Risk Categories

| Category | Description | Examples |
|----------|-------------|----------|
| **Confidentiality** | Unauthorized disclosure | Data breach, PII exposure |
| **Integrity** | Unauthorized modification | Data tampering, code injection |
| **Availability** | Service disruption | DDoS, system failure |
| **Authentication** | Identity compromise | Credential theft, impersonation |
| **Financial** | Monetary loss | Fraud, regulatory fines |
| **Reputational** | Brand damage | Public breach disclosure |

### 4.6 Information Asset Classification for Risk

| Asset Class | Examples | Base Score Impact |
|-------------|----------|--------------------|
| **CRITICAL** | Factor digests, encryption keys, biometrics | 5 |
| **HIGH** | PII, payment data, authentication tokens | 4 |
| **MEDIUM** | Business data, merchant configurations | 3 |
| **LOW** | Public information, logs | 1-2 |

---

## 5. Risk Appetite & Criteria

### 5.1 Risk Appetite Statement

NoTap accepts **ZERO** risk appetite for:

- **Authentication Compromise**: Any successful attack on authentication factors
- **PII Breach**: AnyUnauthorized access to personal data requiring GDPR Art. 33 notification
- **Cryptographic Weakness**: Any non-compliance with cryptographic standards
- **Critical Vulnerabilities**: Any unpatched critical/high vulnerability over 30 days

NoTap accepts **LOW** risk appetite for:

- **Service Availability**: < 0.1% unplanned downtime (SLA breach)
- **Access Control Failure**: Any unauthorized access to internal systems

NoTap accepts **MEDIUM** risk appetite for:

- **Non-Critical Vulnerabilities**: Medium/low vulnerabilities with compensating controls
- **Third-Party Risks**: Vendors with SOC 2 Type II or equivalent

### 5.2 Risk Criteria Matrix

| Risk Level | Treatment Required | Approval Authority | Timeline |
|-----------|-------------------|---------------------|----------|
| **CRITICAL** | Immediate treatment | Security Team Lead | 24 hours |
| **HIGH** | Urgent treatment | Security Team Lead | 7 days |
| **MEDIUM** | Planned treatment | Security Team | 30 days |
| **LOW** | Accept or monitor | Security Analyst | Next review cycle |

### 5.3 Risk Tolerance Thresholds

| Category | Tolerance Threshold | Maximum Acceptable |
|----------|-------------------|--------------------|
| Authentication compromise | Zero | Any successful attack |
| PII breach | Zero | GDPR Art. 33 notification required |
| Service availability | 99.9% | 8.76 hours/year downtime |
| Unauthorized access | Zero | Any unauthorized access |
| Critical vuln (CVSS 9-10) | Zero | Unpatched > 24 hours |
| High vuln (CVSS 7-8.9) | Zero | Unpatched > 7 days |

---

## 6. Risk Treatment

### 6.1 Treatment Options

| Option | Description | When to Use |
|--------|-------------|-------------|
| **Avoid** | Eliminate the risk entirely | Risk exceeds appetite, no control needed |
| **Modify** | Apply controls to reduce likelihood/impact | Feasible and cost-effective |
| **Transfer** | Shift risk to third party | Insurance, outsourcing, contracts |
| **Accept** | Acknowledge and monitor | Risk within appetite, cost > benefit |

### 6.2 Control Selection Framework

When **Modifying** risk, select controls from:

| Control Category | Priority | Examples |
|------------------|----------|----------|
| **Preventive** | 1st | Encryption, access control, MFA |
| **Detective** | 2nd | Logging, monitoring, IDS |
| **Corrective** | 3rd | Incident response, backup, recovery |
| **Compensating** | Alternative | Temporary controls while permanent implemented |

### 6.3 Control Mapping to ISO 27001

| Risk Type | ISO 27001 Controls | Implementation |
|-----------|-------------------|-----------------|
| Confidentiality | A.8.1, A.8.2, A.8.8 | Encryption, access control |
| Integrity | A.8.5, A.8.6, A.8.7 | Input validation, checksums |
| Availability | A.8.24, A.8.27, A.8.32 | Backup, redundancy |
| Authentication | A.8.2, A.8.5 | MFA, authentication |
| Financial | A.8.9, A.8.15 | Transaction logging, PCI controls |

### 6.4 Residual Risk Acceptance

Residual risk may be accepted only when:

1. **Treatment not feasible**: No effective treatment available
2. **Cost exceeds benefit**: Treatment cost disproportionate to risk
3. **Risk within appetite**: Risk level within accepted thresholds
4. **Approved**: Written approval from appropriate authority
5. **Documented**: Risk register entry with justification
6. **Monitored**: Regular review of accepted risks

Residual risk acceptance form required:

```
Risk ID: [RISK-XXX]
Title: [Risk description]
Inherent Level: [CRITICAL/HIGH/MEDIUM/LOW]
Treatment Options Considered:
  1. [Option and why rejected]
  2. [Option and why rejected]
Residual Risk Level: [CRITICAL/HIGH/MEDIUM/LOW]
Justification: [Why acceptance is acceptable]
Approval: [Authority, Date]
Review Date: [Next review date]
```

---

## 7. Regulatory Requirements

### 7.1 GDPR Article 35 (DPIA) Requirements

A Data Protection Impact Assessment (DPIA) is required when processing:

| Criteria | NoTap Trigger |
|----------|---------------|
| Systematic monitoring | Device fingerprinting, behavioral analytics |
| Large scale processing | 100,000+ users |
| Special category data | Biometric data (BIPA factors) |
| New technology | Blockchain verification, voice authentication |
| Profiling | Risk-based factor selection |

**DPIA Required for NoTap:**
- Biometric factor processing (voice, face, fingerprint)
- Device fingerprinting and behavioral analysis
- Risk-based authentication selection
- Blockchain verification storage

### 7.2 DPIA Process

```
┌─────────────────────────────────────────────────────────────────┐
│                    DPIA PROCESS                               │
├───────────────────────────────────────────────────────────────┤
│                                                          │
│  Step 1: Consultation                                      │
│  - DPO involvement                                         │
│  - Data subjects input (if needed)                         │
│                                                          │
│  Step 2: Describe Processing                              │
│  - Data flows and processing operations                    │
│  - Necessity and proportionality                           │
│                                                          │
│  Step 3: Assess Necessity                                 │
│  - Legal basis verification                                │
│  - Minimum data collection                                │
│                                                          │
│  Step 4: Identify Risks                                    │
│  - Inherent risk analysis                                 │
│  - Rights and freedoms impact                              │
│                                                          │
│  Step 5: Propose Measures                                 │
│  - Technical controls                                    │
│  - Safeguards and guarantees                             │
│                                                          │
│  Step 6: Residual Risk                                   │
│  - Risk after measures                                   │
│  - Acceptance decision                                   │
│                                                          │
│  Step 7: Review and Approval                             │
│  - DPO sign-off                                           │
│  - Management approval                                   │
│                                                          │
└─────────────────────────────────────────────────────────────────┘
```

### 7.3 NIS2 Article 21 Requirements

NIS2 requires cybersecurity risk management for:

| Requirement | NoTap Implementation |
|-------------|---------------------|
| Risk analysis | Annual enterprise risk assessment |
| Incident handling | POL-006 Incident Response Procedure |
| Business continuity | POL-007 Business Continuity |
| Supply chain security | POL-008 Supplier Security |
| Security in acquisitions | Change management process |
| Vulnerability handling | Vulnerability management (WKI) |

### 7.4 Documentation Requirements

| Document | Retention | Access |
|----------|-----------|--------|
| Risk Assessment Report | 7 years | Security Team, Auditors |
| DPIA Documentation | 7 years | DPO, Auditors |
| Risk Register | Current + 3 years | Security Team |
| Treatment Plans | 7 years | Security Team, Management |
| Acceptance Approvals | 7 years | Security Team, Legal |

---

## 8. Roles & Responsibilities

### 8.1 Risk Management Roles

| Role | Responsibility | Reports To |
|------|----------------|-------------|
| **Executive Sponsor** | Risk appetite approval, resource allocation | Board |
| **Security Team Lead** | Policy oversight, enterprise assessment | Executive Sponsor |
| **DPO** | DPIA oversight, privacy risk assessment | Executive Sponsor |
| **Security Engineer** | Technical risk assessments, vulnerability analysis | Security Team Lead |
| **All Staff** | Report risks, participate in assessments | Security Team Lead |

### 8.2 Risk Assessment Responsibilities

| Activity | Owner | Approver | Frequency |
|----------|-------|-----------|-----------|
| Enterprise Risk Assessment | Security Team Lead | Executive Sponsor | Annual |
| Quarterly Risk Update | Security Engineer | Security Team Lead | Quarterly |
| DPIA | DPO | Executive Sponsor | Before new processing |
| Vulnerability Assessment | Security Engineer | Security Team Lead | Monthly + ad hoc |
| Third-Party Risk Assessment | Security Team | Security Team Lead | Annual |

### 8.3 RACI Matrix

| Activity | Security Team Lead | DPO | Security Engineer | All Staff |
|----------|-------------------|-----|-----------------|-----------|
| Enterprise Assessment | A | C | R | I |
| DPIA | C | R/A | C | I |
| Risk Register Maintenance | R | C | R | I |
| Risk Reporting | R | C | C | I |
| Risk Identification | I | C | R | R |

*R = Responsible, A = Accountable, C = Consulted, I = Informed*

---

## 9. Monitoring & Review

### 9.1 Review Triggers

| Trigger | Type | Timeline |
|---------|------|----------|
| Annual calendar review | Scheduled | Annual (Q4) |
| Significant business change | Ad hoc | Before change |
| Major security incident | Post-incident | Within 7 days |
| Regulatory change | Ad hoc | Within 30 days |
| New system/process launch | Ad hoc | Before launch |

### 9.2 Key Risk Indicators (KRIs)

| KRI | Threshold | Alert |
|-----|-----------|-------|
| Open Critical Risks | > 0 | Immediate |
| Open High Risks | > 3 | Weekly |
| Overdue Treatments | Any | Weekly |
| Failed Controls | > 5 | Monthly |
| Residual Risk Acceptances | > 10 | Quarterly |

### 9.3 Reporting Requirements

| Report | Audience | Frequency | Format |
|--------|----------|-----------|--------|
| Executive Summary | Executive Sponsor | Quarterly | Dashboard |
| Risk Register | Security Team | Monthly | Spreadsheet |
| DPIA Results | DPO, Executive | As needed | Formal report |
| Compliance Status | Auditors | Annual | Evidence package |
| Incident Learning | All Security | Post-incident | Lessons learned doc |

---

## 10. Continual Improvement

### 10.1 Improvement Sources

| Source | Input |
|--------|-------|
| Incident investigations | Control gaps identified |
| Audit findings | Compliance gaps |
| Regulatory changes | New requirements |
| Technology changes | New risks |
| Third-party assessments | External perspective |

### 10.2 Maturity Levels

| Level | Description | Indicators |
|-------|-------------|------------|
| **Initial (1)** | Ad hoc, reactive | No formal process |
| **Developing (2)** | Basic process established | Annual assessment |
| **Defined (3)** | Documented, consistently applied | Quarterly reviews |
| **Managed (4)** | Measured and monitored | KRIs, dashboards |
| **Optimizing (5)** | Continually improving | Predictive analytics |

**Current Target**: Level 3 (Defined)

### 10.3 Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Annual assessment completion | 100% | - |
| DPIA before new processing | 100% | - |
| Critical risk treatment timeline | 24 hours | - |
| Treatment completion rate | 95% | - |
| Risk register accuracy | 100% | - |

---

## 11. Related Documents

| Document | Location | Purpose |
|----------|----------|---------|
| POL-001 Information Security Policy | `POL-001_INFORMATION_SECURITY_POLICY.md` | Master policy |
| DATA_LIFECYCLE_RISK_ASSESSMENT.md | `../` | Existing risk assessment |
| POL-006 Incident Response | `POL-006_INCIDENT_RESPONSE.md` | Incident management |
| PRIVACY_IMPLEMENTATION.md | `../PRIVACY_IMPLEMENTATION.md` | Privacy risk |
| ISO_27002_GAP_ANALYSIS.md | `../ISO_27002_GAP_ANALYSIS.md` | Control gap analysis |

---

## Annex A: Risk Register Template

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         RISK REGISTER                             │
├──────────────────────────────────────────────────────────────────────┤
│ Date Created: [YYYY-MM-DD]    Assessment Owner: [Name]            │
│ Last Updated: [YYYY-MM-DD]    Next Review: [YYYY-MM-DD]           │
└──────────────────────────────────────────────────────────────────────┘

| ID | Title | Description | Category | Inherent | Likelihood | Impact | Residual | Treatment | Owner | Status |
|----|-------|-------------|-----------|----------|-----------|--------|-----------|------------|--------|--------

INSTRUCTIONS:
- ID: RISK-[###] sequential number
- Title: Brief risk title
- Description: Detailed risk description with asset and threat
- Category: Confidentiality/Integrity/Availability/Authentication/Financial
- Inherent: CRITICAL/HIGH/MEDIUM/LOW (before controls)
- Likelihood: 1-5 scale
- Impact: 1-5 scale
- Residual: CRITICAL/HIGH/MEDIUM/LOW (after controls)
- Treatment: AVOID/MODIFY/TRANSFER/ACCEPT
- Owner: Responsible person
- Status: OPEN/IN_PROGRESS/CLOSED/ACCEPTED
```

---

## Annex B: DPIA Template

```
┌──────────────────────────────────────────────────────────────────────────────┐
│              DATA PROTECTION IMPACT ASSESSMENT (DPIA)                      │
├──────────────────────────────────────────────────────────────────────────────┤
│ Processing Activity: [Name]                                                │
│ Date: [YYYY-MM-DD]        DPIA Owner: [Name]              DPO Review: [Date]│
└──────────────────────────────────────────────────────────────────────────────┘

1. PROCESSING DESCRIPTION
   ──────────────────────────────────────────────
   Data Categories:
   
   Data Subjects:
   
   Processing Operations:
   
   Data Flows:
   
   Volume (estimated records):

2. NECESSITY AND PROPORTIONALITY
   ──────────────────────────────────────────────
   Legal Basis: [GDPR Art. 6(1)] 
   
   Necessity:
   
   Proportionality:
   
   Data Minimization Measures:

3. RISK TO DATA SUBJECTS
   ──────────────────────────────────────────────
   | Risk | Severity | Likelihood | Overall Risk | Mitigation |
   |------|----------|------------|---------------|------------|
   |     |          |            |               |            |

4. MEASURES AND SAFEGUARDS
   ──────────────────────────────────────────────
   Technical Controls:
   
   Organizational Controls:
   
   Safeguards:

5. RESIDUAL RISK ASSESSMENT
   ──────────────────────────────────────────────
   Residual Risk Level: [CRITICAL/HIGH/MEDIUM/LOW]
   
   Acceptable: [YES/NO - with justification]

6. SIGN-OFF
   ──────────────────────────────────────────────
   DPIA Owner: __________________ Date: __________
   DPO Review: __________________ Date: __________
   Management Approval: __________________ Date: __________
```

---

## Document Control

- **Version**: 1.0.0
- **Status**: DRAFT
- **Approved By**: Executive Sponsor (pending)
- **Effective Date**: Upon approval
- **Next Review**: 2027-04-22
- **Location**: `documentation/05-security/POLICIES/POL-002_RISK_ASSESSMENT_POLICY.md`

**For questions**: Contact Security Team Lead or see CLAUDE.md for security contacts.

(End of file - 608 lines)