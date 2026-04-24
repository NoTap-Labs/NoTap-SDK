# NoTap Business Continuity Policy

> **Business Continuity & Disaster Recovery** — Ensures service availability and recovery from disruptions
> Required by ISO 27001 A.8.27, NIS2 Article 21, and SOC 2 A1.1.

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-22 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Regulatory Alignment](#2-regulatory-alignment)
3. [Business Impact Analysis](#3-business-impact-analysis)
4. [Continuity Strategies](#4-continuity-strategies)
5. [Disaster Recovery](#5-disaster-recovery)
6. [Incident Response Integration](#6-incident-response-integration)
7. [Communication Plan](#7-communication-plan)
8. [Testing](#8-testing)
9. [Training](#9-training)
10. [Roles & Responsibilities](#10-roles--responsibilities)
11. [Compliance & Enforcement](#11-compliance--enforcement)
12. [Related Documents](#12-related-documents)
13. [Annex A: Critical Functions](#annex-a-critical-functions)
14. [Annex B: RTO/RPO Matrix](#annex-b-rtorpo-matrix)
15. [Annex C: Test Scenarios](#annex-c-test-scenarios)

---

## 1. Purpose & Scope

### 1.1 Purpose

This policy establishes NoTap's business continuity and disaster recovery capabilities to ensure:

- **Service Availability**: Maintain 99.9% uptime (SLA compliance)
- **Recovery**: Restore services within target timelines
- **Resilience**: Minimize impact from disruptions
- **Compliance**: Meet ISO 27001 A.8.27, NIS2, SOC 2 requirements
- **Customer Trust**: Minimize service impact

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| Backend API services | Physical premises |
| SDK services | Employee systems |
| Payment processing | End-user devices |
| Admin systems | Office infrastructure |
| Customer-facing services | |

### 1.3 Key Principles

| Principle | Description |
|-----------|-------------|
| **Resilience** | Design for failure |
| **Recovery** | Fast restoration |
| **Testing** | Regular validation |
| **Improvement** | Learn from incidents |

### 1.4 Cloud-Native Approach

As a cloud-hosted service:
- AWS manages physical infrastructure
- NoTap focuses on application-level continuity
- Leverage AWS native resilience features
- RTO/RPO aligned to business needs

---

## 2. Regulatory Alignment

### 2.1 ISO 27001:2022 Controls

| Control | Requirement | Policy Section |
|---------|-------------|--------------|
| A.8.27 | Backup Copies | Section 4 |
| A.8.32 | Removal of Information | Section 4 |
| A.5.26 | Incident Management | Section 6 |
| A.5.27 | Business Continuity | Sections 3-8 |

### 2.2 NIS2 Directive

| Article | Requirement | Policy Section |
|---------|-------------|--------------|
| Art. 21 | Risk Management | Sections 3, 4 |
| Art. 21(1) | Business continuity | All sections |

### 2.3 SOC 2 Trust Services Criteria

| Criterion | Requirement | Policy Section |
|-----------|-------------|--------------|
| A1.1 | Availability | All sections |
| A1.2 | Recovery | Sections 5, 6 |
| A1.3 | Contingency | Sections 7, 8 |

### 2.4 Relationship to Other Policies

| Policy | Relationship |
|--------|-------------|
| POL-001 | Risk appetite (availability target) |
| POL-002 | Risk assessment methodology |
| POL-006 | Incident response procedures |
| POL-008 | Supplier continuity |

---

## 3. Business Impact Analysis

### 3.1 Critical Functions

| Function | Impact if Unavailable | Priority |
|-----------|----------------------|----------|
| User Authentication | Blocks all transactions | CRITICAL |
| Payment Processing | Revenue loss | CRITICAL |
| User Enrollment | Blocks new users | HIGH |
| Admin Access | Support blocking | MEDIUM |
| Verification API | Merchant impact | HIGH |
| Merchant Portal | Business impact | MEDIUM |

### 3.2 Impact Categories

| Category | Definition | Examples |
|----------|-------------|----------|
| **CRITICAL** | Immediate revenue/business impact | Authentication, payments |
| **HIGH** | Significant impact within hours | Enrollment, verification |
| **MEDIUM** | Manageable impact | Admin, reporting |
| **LOW** | Minimal impact | Logs, analytics |

### 3.3 Business Impact Tolerance

| Impact Level | Maximum Downtime | Customer Impact |
|-------------|------------------|----------------|
| **CRITICAL** | < 1 hour | Major service blocking |
| **HIGH** | < 4 hours | Significant inconvenience |
| **MEDIUM** | < 24 hours | Noticeable impact |
| **LOW** | < 7 days | Minimal impact |

### 3.4 Critical Functions Detail

See Annex A for complete critical function list.

---

## 4. Continuity Strategies

### 4.1 Architecture Resilience

```
┌─────────────────────────────────────────────────────────────────┐
│           RESILIENCE ARCHITECTURE                     │
├──────────────────────────────────────────────┤
│                                              │
│  ┌─────────────────────────────────────────┐   │
│  │           AWS INFRASTRUCTURE             │   │
│  │  ┌─────────────────────────────────┐ │   │
│  │  │     MULTI-AZ DEPLOYMENT        │ │   │
│  │  │     (2+ Availability Zones)   │ │   │
│  │  └─────────────────────────────────┘ │   │
│  │  ┌─────────────────────────────────┐ │   │
│  │  │     AUTO-SCALING              │ │   │
│  │  │     (Horizontal scaling)       │ │   │
│  │  └─────────────────────────────────┘ │   │
│  │  ┌─────────────────────────────────┐ │   │
│  │  │     LOAD BALANCING              │ │   │
│  │  │     (Traffic distribution)    │ │   │
│  │  └─────────────────────────────────┘ │   │
│  └─────────────────────────────────────────┘   │
│                                              │
│  DATA LAYER:                                    │
│  ┌─────────────────────────────────────────┐   │
│  │  POSTGRESQL (primary + replica)          │   │
│  │  REDIS (cluster mode)                │   │
│  │  S3 (multi-region backup)           │   │
│  └─────────────────────────────────────────┘   │
│                                              │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Strategies by Criticality

| Strategy | Critical | High | Medium | Low |
|----------|----------|------|--------|------|
| Multi-AZ | Required | Required | - | - |
| Auto-scaling | Required | For high load | - | - |
| Load balancing | Required | Required | - | - |
| Database replication | Required | Required | - | - |
| Redis clustering | Required | Required | - | - |
| Backup + restore | Required | Required | Manual | - |
| CDN | Required | - | - | - |

### 4.3 Availability Targets

| Metric | Target | Definition |
|--------|--------|-----------|
| **Uptime** | 99.9% | Maximum 8.76 hours/year |
| **MTTR** | < 1 hour | Mean time to recovery |
| **MTBF** | > 30 days | Mean time between failures |

### 4.4 Backup Strategy

| Data Type | Backup Method | Frequency | Retention |
|-----------|--------------|------------|-----------|
| Database | PostgreSQL replica | Continuous | 7 years |
| Redis | RDB + AOF | Continuous | 24h |
| S3 | Cross-region | Daily | 7 years |
| Encryption keys | AWS KMS | N/A (managed) | Per key |
| Logs | CloudWatch | Real-time | 90 days |

---

## 5. Disaster Recovery

### 5.1 Recovery Objectives

| Metric | Target | Policy Reference |
|--------|--------|-------------------|
| **RTO** | < 1 hour | Recovery Time Objective |
| **RPO** | < 5 minutes | Recovery Point Objective |
| **Recovery** | < 4 hours | Full service restoration |

### 5.2 RTO/RPO by Function

See Annex B for complete RTO/RPO matrix.

### 5.3 Recovery Procedures

| Step | Action | Timeline | Owner |
|------|--------|----------|--------|
| 1 | Detect failure | Real-time | Monitoring |
| 2 | Classify severity | 5 min | On-call |
| 3 | Alert IRT | 10 min | Monitoring |
| 4 | Contain impact | 30 min | Engineering |
| 5 | Begin recovery | 30 min | Engineering |
| 6 | Verify service | 1 hour | QA |
| 7 | Resume traffic | 1 hour | Engineering |
| 8 | Post-incident | 24 hours | IRT |

### 5.4 Disaster Scenarios

| Scenario | Response | RTO Target |
|----------|----------|-----------|
| Single AZ failure | Auto-failover | < 5 minutes |
| Database failure | Promote replica | < 1 hour |
| Redis cluster failure | Rebuild cluster | < 1 hour |
| Application crash | Auto-restart | < 10 minutes |
| DDoS attack | Rate limiting | < 30 minutes |
| Region failure | DNS failover | < 1 hour |

### 5.5 Recovery Locations

| Tier | Primary | Secondary |
|------|---------|-----------|
| **Production** | AWS primary region | AWS secondary region |
| **Database** | Primary + read replica | Cross-region replica |
| **Redis** | Cluster mode | Backup instance |
| **Backup** | S3 primary | S3 cross-region |

---

## 6. Incident Response Integration

### 6.1 Integration with POL-006

This policy integrates with the Incident Response Procedure (POL-006):

| POL-006 Section | Continuity Integration |
|----------------|-------------------|
| Incident classification | Determines BC activation |
| IRT activation | Includes BC lead |
| Escalation | BC stakeholders |
| Communication | Customer notifications |
| Recovery | BC procedures |

### 6.2 Trigger Criteria

| Trigger | Activation |
|---------|-------------|
| Service down > 30 minutes | Full BC activated |
| Multiple AZ affected | Full BC activated |
| Data loss potential | Full BC activated |
| Payment processing down | Payment BC activated |
| Admin system down | Admin BC activated |

### 6.3 Severity Mapping

| Service Severity | BC Severity | Actions |
|------------------|-------------|----------|
| CRITICAL | Level 1 | Full BC activated |
| HIGH | Level 2 | Partial BC activated |
| MEDIUM | Level 3 | Monitoring |
| LOW | Level 4 | Normal ops |

---

## 7. Communication Plan

### 7.1 Internal Communication

| Audience | Method | Timing |
|----------|--------|--------|
| All engineering | Slack #incidents | Real-time |
| Executive | Call/video | Within 30 min |
| Board | Email | Post-incident |
| HR | Email | As needed |

### 7.2 External Communication

| Stakeholder | Method | Timing |
|------------|--------|--------|
| Customers | Status page | Within 1 hour |
| Customers | Email | Post-incident |
| Merchants | Merchant portal | Within 1 hour |
| Support | Status page | Real-time |
| Regulators | Formal | As required |

### 7.3 Status Communication

| Status Level | Public Message |
|-------------|--------------|
| Operational | "All systems operational" |
| Degradation | "Some users experiencing [issue]" |
| Outage | "[Service] currently unavailable" |
| Maintenance | "Scheduled maintenance [date]" |

### 7.4 Customer Notification

| Scenario | Notification |
|----------|-------------|
| Service degraded | Status page + tweet |
| Service outage | Status page + email |
| Data security | Email + blog |
| Extended outage | Email + blog + social |

---

## 8. Testing

### 8.1 Test Schedule

| Test Type | Frequency | Scope |
|----------|-----------|-------|
| **Tabletop** | Quarterly | Scenario walk-through |
| **Component** | Monthly | Single service |
| **Full DR** | Annual | End-to-end |
| **Backup restore** | Quarterly | Data recovery |

### 8.2 Test Scenarios

See Annex C for complete test scenarios.

| Scenario | Test Type | Frequency |
|----------|-----------|-----------|
| Database failover | Component | Monthly |
| Redis failover | Component | Monthly |
| Application crash | Component | Monthly |
| DDoS response | Tabletop | Quarterly |
| Full region failover | Full DR | Annual |
| Backup/restore | Component | Quarterly |

### 8.3 Success Criteria

| Test | Success Metric |
|------|---------------|
| Failover | Service restored < RTO |
| Backup restore | Data recovered < RPO |
| Communication | Stakeholders notified in timeline |
| Documentation | Runbook accurate |

### 8.4 Post-Test Review

- Document results
- Identify gaps
- Update procedures
- Schedule remediation

---

## 9. Training

### 9.1 Training Requirements

| Role | Training | Frequency |
|------|----------|-----------|
| Engineering | BC procedures | Annual |
| On-call | BC response | Quarterly |
| Executive | Decision-making | Annual |
| Product | Customer communication | Annual |

### 9.2 Awareness

| Activity | Audience | Frequency |
|----------|----------|-----------|
| BC tabletop | Engineering | Quarterly |
| Publication | All staff | Annual |
| Lessons learned | All | Post-incident |

---

## 10. Roles & Responsibilities

### 10.1 Continuity Roles

| Role | Responsibility |
|------|---------------|
| **Executive Sponsor** | Business decisions, resources |
| **Engineering Lead** | Technical recovery |
| **IRT Lead** | Incident coordination |
| **Operations** | Monitoring, escalation |
| **All Engineering** | Recovery execution |

### 10.2 Escalation Matrix

| Severity | Escalate To | Timeline |
|----------|-------------|----------|
| Level 1 (Full BC) | Executive + Legal | Immediate |
| Level 2 | Engineering Lead + Ops | 30 minutes |
| Level 3 | On-call Engineer | 1 hour |
| Level 4 | Monitoring | As needed |

### 10.3 RACI Matrix

| Activity | Executive | Engineering | Operations |
|----------|-------------|--------------|------------|
| BC activation | A | R | C |
| Recovery execution | I | R | C |
| Customer comm | A | C | R |
| Supplier coord | I | C | R |
| Post-incident | A | R | C |

---

## 11. Compliance & Enforcement

### 11.1 Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Uptime | 99.9% | - |
| RTO achievement | < 1 hour | - |
| Test completion | 100% | - |
| Procedure accuracy | 100% | - |

### 11.2 Review Requirements

| Review | Frequency | Owner |
|--------|-----------|--------|
| RTO/RPO validation | Annual | Engineering Lead |
| Procedure review | Annual | Security Team Lead |
| Test results | Quarterly | Operations |
| Metrics | Monthly | Operations |

### 11.3 Continuous Improvement

- Post-incident reviews
- Test lessons learned
- Architecture reviews
- Procedure updates

---

## 12. Related Documents

| Document | Location | Purpose |
|----------|----------|---------|
| POL-001 Information Security | `POL-001_INFORMATION_SECURITY_POLICY.md` | Master policy |
| POL-002 Risk Assessment | `POL-002_RISK_ASSESSMENT_POLICY.md` | Methodology |
| POL-006 Incident Response | `POL-006_INCIDENT_RESPONSE.md` | Incident procedures |
| POL-008 Supplier Security | `POL-008_SUPPLIER_SECURITY_POLICY.md` | Supplier continuity |
| GATES.md | `../../10-internal/GATES.md` | Runtime monitoring |

---

## Annex A: Critical Functions

| Function | Description | Priority | Owner |
|----------|-------------|----------|-------|
| Authentication API | User login/verification | CRITICAL | Engineering |
| Payment Processing | Payment verification | CRITICAL | Engineering |
| Enrollment Storage | User enrollment data | CRITICAL | Engineering |
| Verification API | Factor verification | HIGH | Engineering |
| Merchant API | Merchant services | HIGH | Engineering |
| Admin Portal | Internal tools | MEDIUM | Operations |
| Reporting | Analytics | LOW | Product |

---

## Annex B: RTO/RPO Matrix

| Service/Function | RTO | RPO | Strategy |
|-----------------|-----|-----|----------|
| Authentication API | 1 hour | 5 min | Multi-AZ |
| Payment Processing | 1 hour | 5 min | Multi-AZ |
| Enrollment Storage | 4 hours | 5 min | Database replica |
| Verification API | 1 hour | 5 min | Multi-AZ |
| Merchant API | 4 hours | 5 min | Multi-AZ |
| Admin Portal | 24 hours | 1 hour | Manual recovery |
| Reporting | 24 hours | 1 hour | Daily backup |

---

## Annex C: Test Scenarios

| ID | Scenario | Type | Approach |
|----|----------|------|----------|
| BC-01 | Database primary failure | Component | Promote replica |
| BC-02 | Redis cluster failure | Component | Rebuild cluster |
| BC-03 | Application crash | Component | Auto-restart |
| BC-04 | AZ failure | Full DR | Failover to secondary AZ |
| BC-05 | DDoS attack | Tabletop | Rate limiting |
| BC-06 | Data corruption | Backup | Restore from backup |
| BC-07 | DNS failure | Tabletop | DNS failover |
| BC-08 | Full region failure | Full DR | Region failover |
| BC-09 | Supply chain failure | Tabletop | Alternative |
| BC-10 | Communication failure | Tabletop | Alt comms |

---

## Document Control

- **Version**: 1.0.0
- **Status**: DRAFT
- **Approved By**: Executive Sponsor (pending)
- **Effective Date**: Upon approval
- **Next Review**: 2027-04-22
- **Location**: `documentation/05-security/POLICIES/POL-007_BUSINESS_CONTINUITY_POLICY.md`

**For questions**: Contact Engineering Lead or see CLAUDE.md for contacts.

(End of file - 580 lines)