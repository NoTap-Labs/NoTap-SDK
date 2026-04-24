# NoTap Incident Response Procedure

> **Incident Management** — Procedures for detecting, responding to, and recovering from security incidents
> Required by GDPR Article 33, NIS2 Article 23, and ISO 27001 controls A.5.26-27.

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-22 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Definitions](#2-definitions)
3. [Incident Classification](#3-incident-classification)
4. [Detection & Reporting](#4-detection--reporting)
5. [Initial Assessment](#5-initial-assessment)
6. [Containment](#6-containment)
7. [Eradication & Recovery](#7-eradication--recovery)
8. [Post-Incident](#8-post-incident)
9. [Breach Notification](#9-breach-notification)
10. [Roles & Responsibilities](#10-roles--responsibilities)
11. [Communication Plan](#11-communication-plan)
12. [Training & Testing](#12-training--testing)
13. [Annex A: Contact List](#annex-a-contact-list)
14. [Annex B: Incident Report Template](#annex-b-incident-report-template)
15. [Annex C: Notification Templates](#annex-c-notification-templates)

---

## 1. Purpose & Scope

### 1.1 Purpose

This procedure establishes NoTap's incident response capabilities to ensure:

- **Rapid Detection**: Minimize time from incident to detection
- **Effective Response**: Minimize damage and recovery time
- **Regulatory Compliance**: Meet notification requirements
- **Business Continuity**: Maintain service availability
- **Learning**: Improve controls post-incident

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| Security incidents | Physical security |
| Data breaches | Employee misconduct |
| Service disruptions | Vendor incidents |
| Cyber attacks | Physical premises |

### 1.3 Regulatory Alignment

| Regulation | Requirement | Procedure Section |
|------------|-------------|----------------|
| GDPR Art. 33 | 72-hour notification | Section 9 |
| NIS2 Art. 23 | 24-hour early warning, 72-hour notification | Section 9 |
| ISO 27001 A.5.26 | Incident management | Sections 4-8 |
| ISO 27001 A.5.27 | Learning from incidents | Section 8 |

### 1.4 Relationship to Other Policies

| Policy | Relationship |
|--------|-------------|
| POL-001 | Master security policy |
| POL-002 | Risk assessment informs controls |
| GATES.md | Runtime detection controls |
| SECURITY_AUDIT.md | Security findings |

---

## 2. Definitions

| Term | Definition |
|------|-------------|
| **Incident** | Unwanted or unexpected event that may compromise security |
| **Security Incident** | Incident affecting confidentiality, integrity, availability |
| **Data Breach** | Breach of personal data (GDPR Art. 4(12)) |
| **Personal Data Breach** | Breach leading to accidental unlawful destruction, loss, alteration, unauthorized disclosure |
| **Incident Response Team (IRT)** | Designated responders |
| **Severity** | Impact level on business/regulatory |
| **Root Cause** | Primary cause of incident |
| **Near Miss** | Incident that could have caused harm |

---

## 3. Incident Classification

### 3.1 Severity Levels

| Level | Definition | Examples | Response Time |
|-------|-------------|-----------|--------------|
| **CRITICAL** | Active breach, data exfiltration, service down | Immediate |
| **HIGH** | Suspected breach, full system compromise | 1 hour |
| **MEDIUM** | Security event, limited impact | 4 hours |
| **LOW** | Policy violation, suspicious activity | 24 hours |

### 3.2 Incident Categories

| Category | Description | Examples |
|----------|-------------|----------|
| **CONFIDENTIALITY** | Unauthorized access to data | Data breach, credential theft |
| **INTEGRITY** | Data tampering | Code injection, data modification |
| **AVAILABILITY** | Service disruption | DDoS, ransomware |
| **AUTHENTICATION** | Identity compromise | Account takeover |
| **FINANCIAL** | Fraud or financial loss | Payment fraud |
| **REPUTATIONAL** | Brand damage | Public disclosure |

### 3.3 Data Breach Assessment

| Risk Level | Criteria | Notification |
|-------------|----------|--------------|
| **High Risk** | Rights/freedoms of individuals likely | 72h (GDPR) |
| **Low Risk** | Unlikely to result in risk | No regulatory notification |
| **No Risk** | Pseudonymized, encrypted | No notification |

---

## 4. Detection & Reporting

### 4.1 Detection Sources

| Source | Detection Mechanism | Alert |
|--------|-------------------|-------|
| **IDS/IPS** | Network monitoring | Real-time |
| **WAF** | Application monitoring | Real-time |
| **SIEM** | Log correlation | Real-time |
| **CloudWatch** | AWS monitoring | Real-time |
| **User Reports** | security@notap.io | Manual |
| **Third-Party** | Vendor alerts | Manual |

### 4.2 Incident Reporting

```
┌─────────────────────────────────────────────────────────────────┐
│              INCIDENT REPORTING PROCESS                    │
├─────────────────────────────────────────────────────┤
│                                                      │
│  Step 1: Detection                                │
│  - Automated alert                                │
│  - User report                                   │
│  - Vendor notification                          │
│                                                      │
│  Step 2: Immediate Actions                       │
│  - Do NOT delete evidence                       │
│  - Document timestamp                          │
│  - Preserve logs                               │
│                                                      │
│  Step 3: Report to Security Team               │
│  - Email: security@notap.io                    │
│  - Include: What, When, How discovered         │
│  - Do NOT speculate on cause                 │
│                                                      │
│  Step 4: Initial Triage                        │
│  - Severity assignment                        │
│  - IRT notification                           │
│  - Start incident log                        │
│                                                      │
│  TIMELINE: Report within 15 minutes            │
│                                                      │
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 What to Report

| Required | Example |
|----------|----------|
| What happened | "Suspicious login activity detected" |
| When | "2026-04-22 14:32 UTC" |
| Systems affected | "auth-api-01, Redis cluster" |
| Initial impact | "Unknown - under investigation" |
| Reporter | "John Doe, Security Analyst" |

### 4.4 What NOT to Do

- ❌ Don't delete logs or files
- ❌ Don't reboot affected systems
- ❌ Don't discuss externally
- ❌ Don't speculate on cause
- ❌ Don't notify authorities without IRT Lead approval

---

## 5. Initial Assessment

### 5.1 Assessment Checklist

| Task | Complete |
|------|----------|
| Log incident start time | [ ] |
| Document initial findings | [ ] |
| Assign severity level | [ ] |
| Identify affected systems | [ ] |
| Identify data at risk | [ ] |
| Notify IRT (if CRITICAL/HIGH) | [ ] |
| Preserve evidence | [ ] |
| Start incident log | [ ] |

### 5.2 Decision Matrix

| Severity | IRT Activation | Executive Notification | Regulatory Prep |
|----------|-----------------|------------------------|-----------------|
| **CRITICAL** | Immediate | Immediate | Yes |
| **HIGH** | Within 1 hour | Within 2 hours | Yes |
| **MEDIUM** | Within 4 hours | Next business day | No |
| **LOW** | Within 24 hours | Weekly report | No |

### 5.3 Evidence Preservation

| Evidence Type | Preservation Method |
|--------------|--------------------|
| **System logs** | Export, hash, store securely |
| **Network captures** | Full packet capture |
| **Memory dumps** | Forensic image |
| **Disk images** | Write-blocker + hash |
| **Application logs** | Export |
| **Configuration** | Screen capture |

---

## 6. Containment

### 6.1 Containment Strategies

| Strategy | Use Case | Implementation |
|----------|----------|---------------|
| **Short-term** | Active threat | Network isolation, account disable |
| **Long-term** | Known vector | Firewall rules, patches |
| **Compensating** | No immediate fix | Enhanced monitoring |

### 6.2 Containment Checklist

| Action | Complete |
|--------|----------|
| Isolate affected systems | [ ] |
| Disable compromised accounts | [ ] |
| Block malicious IPs/domains | [ ] |
| Revoke API keys | [ ] |
| Update firewall rules | [ ] |
| Enhance monitoring | [ ] |
| Document containment actions | [ ] |

### 6.3 Containment Decision Tree

```
┌─────────────────────────────────────────────────────────────────┐
│              CONTAINMENT DECISION TREE                    │
├─────────────────────────────────────────────────────┤
│                                                      │
│  Is the incident still active?                       │
│         │                                          │
│      YES │ NO                                       │
│         │                                          │
│         ▼                                         │
│  Can we isolate without business impact?          │
│         │                                          │
│      YES │ NO                                       │
│         │                                          │
│         ▼                                         │
│  Is data exfiltration suspected?                   │
│         │                                          │
│      YES │ NO                                       │
│         │                                          │
│         ▼                                         │
│  Consider:                                      │
│  • Short-term containment                        │
│  • Enhanced monitoring                         │
│  • Compensating controls                       │
│                                                      │
│  EXEC APPROVAL: Required for CRITICAL             │
│                                                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Eradication & Recovery

### 7.1 Eradication

| Task | Complete |
|------|----------|
| Remove malware | [ ] |
| Patch vulnerability | [ ] |
| Reset compromised credentials | [ ] |
| Remove unauthorized access | [ ] |
| Verify eradication | [ ] |

### 7.2 Recovery

| Task | Complete |
|------|----------|
| Restore from clean backups | [ ] |
| Verify system integrity | [ ] |
| Verify data integrity | [ ] |
| Resume services | [ ] |
| Monitor for recurrence | [ ] |

### 7.3 Recovery Checklist

```
┌─────────────────────────────────────────────────────────────────┐
│              RECOVERY CHECKLIST                        │
├─────────────────────────────────────────────────────┤
│                                                      │
│  □ All systems restored from verified clean backups         │
│  □ Vulnerability patched/mitigated                  │
│  □ All credentials rotated (if compromised)           │
│  □ All sessions invalidated                     │
│  □ MFA re-enabled (if disabled during incident)       │
│  □ Monitoring enhanced                           │
│  □ Service health confirmed                      │
│  □ Stakeholder notification sent               │
│                                                      │
│  VERIFICATION: Security sign-off before        │
│  resuming full operations                       │
│                                                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 8. Post-Incident

### 8.1 Post-Incident Review

| Task | Timeline | Owner |
|------|----------|--------|
| Document timeline | Within 7 days | IRT Lead |
| Root cause analysis | Within 14 days | Security Team |
| Identify control gaps | Within 14 days | Security Team |
| Develop recommendations | Within 30 days | Security Team |
| Implement fixes | Per roadmap | Engineering |
| Update procedures | Within 30 days | IRT Lead |

### 8.2 Lessons Learned Report

Contents:
1. Executive summary
2. Incident timeline
3. Root cause
4. Impact assessment
5. Response effectiveness
6. Control gaps identified
7. Remediation plan
8. Recommendations

### 8.3 Metrics

| Metric | Target |
|--------|--------|
| Detection time | < 1 hour |
| Response initiation | < 1 hour |
| Containment | < 4 hours |
| Recovery | < 24 hours |
| Post-incident review | < 7 days |

---

## 9. Breach Notification

### 9.1 Notification Requirements

| Regulation | Notification | Timeline | Authority |
|------------|--------------|----------|-----------|
| GDPR Art. 33 | Supervisor | 72 hours | Lead DPA |
| GDPR Art. 34 | Data subjects | "Without undue delay" | Direct |
| NIS2 Art. 23 | Authority | 24 hours (early), 72 hours | NIS2 Authority |
| CCPA | Affected users | "Most expedient" | California AG |

### 9.2 Notification Decision Tree

```
┌─────────────────────────────────────────────────────────────────┐
│           BREACH NOTIFICATION DECISION                    │
├─────────────────────────────────────────────────────┤
│                                                      │
│  Step 1: Assess risk to rights/freedoms             │
│                                                      │
│  Likely risk to individuals?                     │
│         │                                          │
│      YES │ NO                                       │
│         │                                          │
│         ▼                                         │
│  Step 2: Notify supervisory authority             │
│  (GDPR 72h / NIS2 24h)                       │
│         │                                          │
│         ▼                                         │
│  Step 3: Notify data subjects (if high risk)    │
│  (GDPR Art. 34 / CCPA)                       │
│                                                      │
│  NOTIFICATION CONTENT:                             │
│  • Nature of breach                               │
│  • Categories/records affected                  │
│  • Likely consequences                        │
│  • Measures taken/proposed                    │
│  • DPO contact                                │
│                                                      │
└─────────────────────────────────────────────────────────────────┘
```

### 9.3 Notification Content Requirements

| Element | GDPR Art. 33 | GDPR Art. 34 | NIS2 Art. 23 |
|----------|-------------|---------------|--------------|
| Description of breach | Required | Not required | Required |
| Categories affected | Required | "Where possible" | Required |
| Records affected | "Where possible" | "Where possible" | Required |
| Consequences | "Likely consequences" | "Likely consequences" | Required |
| Measures taken | Required | "Measures taken" | Required |
| DPO contact | Required | Required | Required |
| Timeline | 72 hours | "Without undue delay" | 24h / 72h |

### 9.4 Exception: Encryption

If breach involves **encrypted data** with:
- Strong encryption (AES-256)
- Keys not compromised
- Appropriately implemented

Then:
- Risk to rights/freedoms unlikely
- No supervisory notification required (GDPR Art. 33(5))
- Document in risk assessment

---

## 10. Roles & Responsibilities

### 10.1 Incident Response Team

| Role | Responsibility | Backup |
|------|----------------|--------|
| **IRT Lead** | Overall response, decisions, notifications | Security Team Lead |
| **Security Analyst** | Detection, analysis, containment | Security Team |
| **System Engineer** | Technical containment, recovery | DevOps Lead |
| **Communications** | Internal/external comms | Marketing Lead |
| **Legal** | Regulatory notification, counsel | External counsel |
| **DPO** | Privacy aspects, breach assessment | Privacy Officer |

### 10.2 Escalation Matrix

| Severity | Escalate To | Timeline |
|----------|-------------|----------|
| CRITICAL | IRT Lead + Executive + Legal | Immediate |
| HIGH | IRT Lead + Legal | 1 hour |
| MEDIUM | IRT Lead | 4 hours |
| LOW | Security Team | 24 hours |

### 10.3 RACI

| Activity | IRT Lead | Security | Legal | DPO | Executive |
|----------|----------|----------|-------|-----|-----------|
| Detection | I | R | I | I | I |
| Analysis | A | R | C | C | I |
| Containment | A | R | C | I | I |
| Recovery | A | R | I | I | I |
| Notification | A | C | R | R | I |
| Post-incident | R | R | C | C | A |

---

## 11. Communication Plan

### 11.1 Internal Communication

| Audience | Method | Timing |
|----------|--------|--------|
| All Staff | Email | As needed |
| Executive | Update call | CRITICAL: Immediate |
| Technical | Slack #security-incidents | Real-time |
| Board | Briefing | Post-incident |

### 11.2 External Communication

| Stakeholder | Method | Approval |
|-------------|--------|----------|
| Customers | Email, status page | IRT Lead |
| Media | Press statement | Executive + Legal |
| Regulators | Formal notification | Legal |
| Law enforcement | Via Legal | Executive |

### 11.3 Communication Templates

| Scenario | Template |
|----------|----------|
| Internal | "Security incident in progress [ID]. Impact: [X]. Status: [Containment/Recovery]. Updates: [Link]" |
| Customer (no data) | "We detected suspicious activity. No data compromised. [Actions taken]. Learn more: [Link]" |
| Customer (data) | "We experienced a security incident affecting [data]. [What happened, What we're doing, What you can do]" |
| Regulator | Formal letter via Legal |

---

## 12. Training & Testing

### 12.1 Training Requirements

| Role | Training | Frequency |
|------|----------|-----------|
| IRT Members | Full IRT training | Annual |
| All Staff | Security awareness | Annual |
| Engineers | Incident technical response | Annual |
| Executives | Breach decision-making | Annual |

### 12.2 Testing Requirements

| Test Type | Frequency | Scope |
|----------|-----------|-------|
| Tabletop exercise | Quarterly | Scenario walk-through |
| Functional test | Annual | Full procedure |
| Technical exercise | Annual | Detection + response |
| Full simulation | Annual | End-to-end |

### 12.3 Test Scenarios

1. **Ransomware attack**: Detection, containment, recovery
2. **Data breach**: Detection, assessment, notification
3. **Account takeover**: Detection, containment, recovery
4. **DDoS**: Detection, mitigation, communication

---

## Annex A: Contact List

| Role | Contact | Backup |
|------|---------|---------|
| IRT Lead | +1 (555) 010-0001 | +1 (555) 010-0002 |
| Security Team | security@notap.io | security-oncall@notap.io |
| Legal Counsel | legal@notap.io | external-counsel@notap.io |
| DPO | dpo@notap.io | privacy@notap.io |
| Executive | ceo@notap.io | cto@notap.io |
| AWS Support | aws-support@notap.io | - |
| Regulator (GDPR) | [Lead DPA] | - |
| Regulator (NIS2) | [Authority] | - |

---

## Annex B: Incident Report Template

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    INCIDENT REPORT                          │
├────────────────────────────────────────────────────┤
│ Incident ID: [INC-YYYY-###]                              │
│ Date Discovered: [YYYY-MM-DD HH:MM UTC]                  │
│ Date Resolved: [YYYY-MM-DD HH:MM UTC]                  │
│ Reporter: [Name]                                       │
└──────────────────────────────────────────────────────┘

1. INCIDENT SUMMARY
   ─────────────────────────────────────
   Type: [Category]
   Severity: [CRITICAL/HIGH/MEDIUM/LOW]
   Status: [CLOSED]

2. TIMELINE
   ─────────────────────────────────────
   | Time | Event |
   |------|--------|
   |      |        |

3. DESCRIPTION
   ─────────────────────────────────────
   What happened:
   
   How discovered:
   
   Systems affected:

4. IMPACT
   ─────────────────────────────────────
   Data at risk: [Yes/No]
   Users affected: [Number]
   Service impact: [Description]
   Financial impact: [Est. $]

5. RESPONSE
   ─────────────────────────────────────
   Actions taken:
   
   Containment method:
   
   Recovery method:

6. ROOT CAUSE
   ─────────────────────────────────────
   Primary cause:
   
   Contributing factors:

7. LESSONS LEARNED
   ─────────────────────────────────────
   What worked well:
   
   What could improve:
   
   Recommendations:

8. SIGN-OFF
   ─────────────────────────────────────
   IRT Lead: __________________ Date: __________
   Security Team Lead: __________________ Date: __________
```

---

## Annex C: Notification Templates

### C.1 Supervisory Authority (GDPR)

```
To: [Lead DPA]
Date: [YYYY-MM-DD]

NOTICE OF PERSONAL DATA BREACH
(Article 33, GDPR)

1. Nature of the breach:
[Description]

2. Categories and approximate number of data subjects:
[Categories, estimated number]

3. Categories and approximate number of records:
[Categories, estimated number]

4. Likely consequences:
[Description of likely consequences]

5. Measures taken or proposed:
[Description of measures]

6. DPO contact details:
[Name, email, phone]

We will provide additional information as it becomes available.
```

### C.2 Data Subjects (GDPR Art. 34)

```
Subject: Security Incident Notification

Dear [Customer],

We are writing to inform you of a security incident that may have affected your personal information.

WHAT HAPPENED
[Description]

WHAT INFORMATION WAS INVOLVED
[Categories of data]

WHAT WE ARE DOING
[Description of response]

WHAT YOU CAN DO
[Recommended actions]

We take the security of your data seriously and apologize for any concern this may cause.

Questions? Contact dpo@notap.io
```

---

## Document Control

- **Version**: 1.0.0
- **Status**: DRAFT
- **Approved By**: IRT Lead (pending), Executive Sponsor (pending)
- **Effective Date**: Upon approval
- **Next Review**: 2027-04-22
- **Location**: `documentation/05-security/POLICIES/POL-006_INCIDENT_RESPONSE.md`

**For questions**: Contact IRT Lead or see CLAUDE.md.

(End of file - 580 lines)