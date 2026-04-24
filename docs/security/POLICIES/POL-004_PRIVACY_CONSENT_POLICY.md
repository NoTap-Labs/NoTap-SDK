# NoTap Privacy & Consent Policy

> **Privacy and Consent Management** — Governs consent collection, management, and withdrawal
> Required by GDPR Articles 6-22, BIPA, CCPA, COPPA, and ISO 27001 control A.8.11.

| Version | Date | Author | Status |
|---------|------|--------|--------|
| 1.0.0 | 2026-04-22 | Claude | DRAFT |

---

## Table of Contents

1. [Purpose & Scope](#1-purpose--scope)
2. [Definitions](#2-definitions)
3. [Privacy Principles](#3-privacy-principles)
4. [Consent Framework](#4-consent-framework)
5. [Jurisdiction-Specific Requirements](#5-jurisdiction-specific-requirements)
6. [Children's Privacy](#6-childrens-privacy)
7. [Marketing & Communications](#5-marketing--communications)
8. [Cookie & Tracking](#6-cookie--tracking)
9. [Consent Management Implementation](#9-consent-management-implementation)
10. [Data Subject Rights](#10-data-subject-rights)
11. [Roles & Responsibilities](#11-roles--responsibilities)
12. [Compliance & Enforcement](#12-compliance--enforcement)
13. [Annex A: Consent Types Matrix](#annex-a-consent-types-matrix)
14. [Annex B: Jurisdiction Requirements](#annex-b-jurisdiction-requirements)
15. [Annex C: Consent Wording Templates](#annex-c-consent-wording-templates)

---

## 1. Purpose & Scope

### 1.1 Purpose

This policy establishes NoTap's approach to privacy and consent management to ensure:

- **Lawful Processing**: Legal basis established for all processing activities
- **Informed Consent**: Data subjects understand and agree to data use
- **Right to Withdraw**: Easy consent withdrawal without detriment
- **Jurisdictional Compliance**: Meet requirements for all applicable regimes
- **Privacy by Design**: Privacy built into all systems

### 1.2 Scope

| Included | Excluded |
|----------|----------|
| User enrollment data | Employee HR data (separate) |
| Authentication factor data | Vendor data |
| Payment processing data | Public data |
| Marketing communications | |
| Device tracking | |

### 1.3 Regulatory Alignment

| Regulation | Requirement | Policy Section |
|------------|-------------|----------------|
| GDPR Art. 6-22 | Lawful processing, consent | Sections 3, 4 |
| GDPR Art. 9 | Special category data | Section 4.4 |
| BIPA | Biometric consent | Section 5.2 |
| CCPA | Opt-out rights | Section 5.3 |
| COPPA | Children's privacy | Section 6 |
| ePrivacy | Cookie consent | Section 8 |

---

## 2. Definitions

| Term | Definition |
|------|-------------|
| **Consent** | Freely given, specific, informed, unambiguous agreement (GDPR Art. 4(11)) |
| **Explicit Consent** | Clear affirmative act for special category data (GDPR Art. 9(2)(a)) |
| **Opt-in** | Pre-checked box prohibited, affirmative action required |
| **Opt-out** | Default allowed, user can decline |
| **Withdrawal** | As easy to withdraw as to give consent |
| **Legitimate Interest** | Processing necessary for business purposes (GDPR Art. 6(1)(f)) |
| **Personal Data** | Any information identifying individual |
| **Special Category** | Biometric, health, political, etc. (GDPR Art. 9) |
| **Jurisdiction** | Geographic or regulatory region |

---

## 3. Privacy Principles

### 3.1 GDPR Article 5 Principles

| Principle | Requirement | NoTap Implementation |
|-----------|-------------|---------------------|
| **Lawfulness** | Processing must have legal basis | Legal basis matrix (POL-003) |
| **Fairness** | Transparent processing | Privacy disclosure |
| **Transparency** | Clear language, accessible | Privacy policy, notices |
| **Purpose Limitation** | Use only for stated purpose | ROPA activity scope |
| **Data Minimization** | Collect only necessary | Essential data only |
| **Accuracy** | Keep accurate | Self-service correction |
| **Storage Limitation** | Delete when not needed | TTL enforcement |
| **Integrity** | Protect from unauthorized | Encryption, access control |
| **Accountability** | Demonstrate compliance | Audit logging |

### 3.2 Privacy by Design Implementation

| Requirement | Implementation | Technology |
|--------------|----------------|------------|
| **Proactive** | Risk assessments (POL-002) | DPIA process |
| **Privacy as default** | Opt-in for sensitive | Enrollment wizard |
| **Privacy embedded** | Data minimization | Field validation |
| **Full functionality** | No trade-offs | Same-user experience |
| **End-to-end security** | Lifecycle protection | Encryption |
| **Visibility** | Transparent | Privacy disclosure |
| **User respect** | Consent control | Dashboard settings |

### 3.3 Data Minimization Requirements

For each data field, verify:

- [ ] Necessary for purpose
- [ ] No less intrusive alternative
- [ ] Limited to minimum needed
- [ ] Retention period justified
- [ ] Deletion plan in place

---

## 4. Consent Framework

### 4.1 Consent Types

| Consent Type | Legal Basis | Use Case | Mechanism |
|-------------|-------------|----------|-----------|
| **Explicit** | GDPR Art. 9(2)(a) | Biometric authentication | Written/electronic signature |
| **Contract** | GDPR Art. 6(1)(b) | Authentication, payments | Terms acceptance |
| **Legitimate Interest** | GDPR Art. 6(1)(f) | Fraud prevention | LIA documented |
| **Legal Obligation** | GDPR Art. 6(1)(c) | Tax records | Legal requirement |
| **Marketing** | GDPR Art. 6(1)(a) | Communications | Opt-in checkbox |
| **Cookies** | ePrivacy | Analytics | Cookie banner |

### 4.2 Valid Consent Requirements

For consent to be valid under GDPR:

1. **Freely Given**
   - No coercion or undue pressure
   - Genuine choice offered
   - Equal weight to given/withdrawn

2. **Specific**
   - Granular options available
   - Purpose clearly stated
   - Not bundled without choice

3. **Informed**
   - Purpose explained
   - Data use described
   - Third parties identified
   - Rights explained

4. **Unambiguous**
   - Clear affirmative action
   - No pre-ticked boxes
   - Statement or clear action

### 4.3 Consent Collection Process

```
┌─────────────────────────────────────────────────────────────────┐
│              CONSENT COLLECTION PROCESS                      │
├─────────────────────────────────────────────────────┤
│                                                      │
│  Step 1: Context Assessment                         │
│  - Identify processing type                       │
│  - Determine legal basis                       │
│  - Identify jurisdiction                       │
│                                                      │
│  Step 2: Consent Design                        │
│  - Draft consent wording                      │
│  - Select mechanism                        │
│  - Plan withdrawal process                  │
│                                                      │
│  Step 3: Implementation                      │
│  - Build UI/flow                          │
│  - Test user experience                     │
│  - Verify withdrawal works                │
│                                                      │
│  Step 4: Documentation                     │
│  - Log consent records                    │
│  - Update ROPA                            │
│  - Train staff                            │
│                                                      │
│  Step 5: Review                           │
│  - Regular audits                         │
│  - Update as needed                       │
│  - Document changes                     │
│                                                      │
└─────────────────────────────────────────────────────┘
```

### 4.4 Special Category Data (GDPR Art. 9)

For biometric data (NoTap use case):

| Requirement | Implementation |
|--------------|---------------|
| **Explicit Consent** | Written/electronic consent required |
| **BIPA (Illinois)** | 6 disclosures + electronic signature |
| **Consent Scope** | Specific biometric type only |
| **Purpose Limitation** | Authentication only |
| **Retention** | Active + 30 days post-deactivation |
| **Deletion** | Upon consent withdrawal |

---

## 5. Jurisdiction-Specific Requirements

### 5.1 GDPR (EU/EEA/UK)

| Requirement | Implementation |
|--------------|---------------|
| Legal basis | Art. 6(1) - must establish |
| Consent for processing | Art. 7 - affirmative, documented |
| Children's data | 16 (13 UK) - parental consent |
| International transfers | Art. 44-49 - adequacy/SCCs/BCRs |
| Rights | Art. 15-22 - must honor |
| Breach notification | Art. 33 - 72 hours |

### 5.2 BIPA (Illinois, USA)

| Requirement | Implementation |
|--------------|---------------|
| **Written Policy** | 740 ILCS 14/10 - must provide |
| **Consent Required** | 740 ILCS 14/15 - before collection |
| **6 Disclosures** | 1. Data collected, 2. Purpose, 3. retention, 4. sharing, 5. right to delete, 6. |
| **Electronic Signature** | Must be verifiable |
| **Retention** | 3 years after last interaction |
| **Destruction** | 3 years after last interaction |

### 5.3 CCPA/CPRA (California)

| Requirement | Implementation |
|--------------|---------------|
| **Right to Opt-Out** | "Do Not Sell" link required |
| **Sensitive Data** | Limit use with opt-out |
| **GPC Support** | Honor Global Privacy Control |
| **Dark Patterns** | Prohibited |
| **Rewards** | Cannot deny service for exercise |

### 5.4 PIPEDA (Canada)

| Purpose | Implementation |
|---------|----------------|
| **Consent** | Meaningful, opt-in |
| **Accountability** | Designated accountable |
| **Limited Collection** | Necessary only |
| **Access** | Must provide |

### 5.5 Jurisdiction Matrix

| Jurisdiction | Primary Law | Consent Type | Special Requirements |
|--------------|-------------|---------------|---------------------|
| **EU/UK** | GDPR | Opt-in for sensitive | DPIA for high risk |
| **California** | CCPA/CPRA | Opt-out default | "Do Not Sell" link |
| **Illinois** | BIPA | Opt-in (biometric) | 6 disclosures + signature |
| **Canada** | PIPEDA | Meaningful opt-in | Accountability |
| **Brazil** | LGPD | Opt-in | DPO required |
| **Texas** | TDPSA | Opt-out + GPC | Honor GPC |

---

## 6. Children's Privacy

### 6.1 Age Thresholds by Jurisdiction

| Jurisdiction | Children's Age | Consent Required |
|--------------|----------------|-----------------|
| **EU (GDPR)** | Under 16* | Parental consent (*13 UK) |
| **California** | Under 13 | Parental consent |
| **Illinois** | Under 13 | Parental consent |
| **Texas** | Under 13 | Parental consent |
| **Canada (PIPEDA)** | Under 13 | Parental consent |

### 6.2 NoTap Approach

NoTap **does not knowingly** collect data from children under 13:

- **Age verification**: Prompted during enrollment
- **Block underage**: System blocks enrollment
- **No directed content**: Not targeted to children
- **School verification**: Checkbox for age

If underage user identified:
1. Do not process data
2. Request parental consent
3. If no consent, delete data

### 6.3 COPPA Compliance (US)

| COPPA Requirement | NoTap Implementation |
|------------------|---------------------|
| Verifiable consent | Parental consent required |
| Direct contact with child | Not permitted |
| Parental notice | Clear, complete |
| Parental access | Email/text verification |
| Retain consent | 3 years |

---

## 7. Marketing & Communications

### 7.1 Marketing Consent Requirements

| Marketing Type | GDPR | CCPA | Implementation |
|----------------|-----|-----|---------------|
| **Email** | Opt-in required | Opt-out required | "Unsubscribe" link |
| **SMS** | Opt-in required | Opt-out required | "STOP" response |
| **Push** | Opt-in required | Opt-out required | Device prompt |
| **Analytics** | Opt-in required (non-essential) | Opt-out available | Cookie banner |
| **Third-party sharing** | Opt-in required | Opt-out required | Clear disclosure |

### 7.2 Marketing Opt-Out Process

```
┌─────────────────────────────────────────────────────────────────┐
│              MARKETING OPT-OUT PROCESS                    │
├─────────────────────────────────────────────────────┤
│                                                      │
│  Mechanism 1: Email Link                            │
│  - "Unsubscribe" in every email                     │
│  - One-click unsubscription                        │
│  - Confirmation email                           │
│                                                      │
│  Mechanism 2: Account Settings                    │
│  - Marketing preferences dashboard              │
│  - Granular control                             │
│  - Immediate effect                           │
│                                                      │
│  Mechanism 3: Contact DPO                     │
│  - dpo@notap.io                               │
│  - 10 business days response                   │
│                                                      │
│  TIMELINE: Process within 10 business days        │
│  CONFIRMATION: Send confirmation email            │
│  EXCEPTION: No service denial for opt-out          │
│                                                      │
└─────────────────────────────────────────────────────────────────┘
```

### 7.3 Prohibited Practices

- **No Dark Patterns**: No manipulative UI
- **No Service Denial**: Can't deny service for not consenting
- **No Bundling**: Can't bundle with other consents
- **No Pre-ticked Boxes**: No default opt-ins

---

## 8. Cookie & Tracking

### 8.1 Cookie Categories

| Category | Examples | Consent Required | Retention |
|----------|----------|---------------|-----------|
| **Strictly Necessary** | Session, security | No | Session |
| **Functional** | Language preference | No | 1 year |
| **Analytics** | Usage statistics | Yes - opt-in | 1 year |
| **Marketing** | Ad targeting | Yes - opt-in | 1 year |

### 8.2 Cookie Banner Requirements

| Jurisdiction | Consent Type | Mechanism |
|--------------|-------------|-----------|
| **EU** | Opt-in required | Cookie banner |
| **California** | Opt-out available | "Do Not Sell" link |
| **Texas** | Opt-out + honor GPC | Banner + GPC signal |

### 8.3 Cookie Consent Implementation

- **First visit**: Banner explaining cookies
- **Necessary cookies**: No consent needed
- **Other cookies**: Opt-in via banner
- **Granular**: Specific cookie type consent
- **Withdrawal**: Re-prompt via footer link

---

## 9. Consent Management Implementation

### 9.1 Consent Storage

| Field | Storage | Retention |
|-------|---------|----------|
| Consent type | User record | Active + retention |
| Consent date | Consent log | Active + 3 years |
| Consent version | Version reference | Legal evidence |
| Withdrawal date | Consent log | Active + 3 years |
| IP address | Audit log | Security |

### 9.2 Consent Versioning

Each consent version must have:
- Unique version identifier
- Effective date
- Wording (for legal evidence)
- Previous version (for comparison)

### 9.3Consent Withdrawal Process

User can withdraw consent via:
- Account settings dashboard
- Email to dpo@notap.io
- Live chat support

Upon withdrawal:
1. Stop processing immediately
2. Confirm via email
3. Update user preferences
4. Document in audit log

---

## 10. Data Subject Rights

### 10.1 Rights by Jurisdiction

| Right | GDPR Art. | CCPA | BIPA |
|-------|----------|------|------|
| Access | Art. 15 | Know | Notice |
| Rectification | 16 | Correct | - |
| Erasure | 17 | Delete | Delete |
| Restrict processing | 18 | Limit | - |
| Portability | 20 | - | - |
| Object | 21 | - | - |
| Opt-out | - | Opt-out | - |

### 10.2 Rights Response Requirements

| Right | Response Timeline | Format | Verification |
|--------|----------------|-----------|--------|
| GDPR | 30 days | Free of charge | Identity verification |
| CCPA | 45 days | Free | Identity verification |
| BIPA | 30 days | Written confirmation | Identity verification |

### 10.3 Automated Decision-Making

| Requirement | Implementation |
|--------------|---------------|
| **Not Fully Automated** | Human review available |
| **Significant Effects** | Right to explanation |
| **Challenge** | Request human review anytime |

---

## 11. Roles & Responsibilities

| Role | Responsibility |
|------|----------------|
| **DPO** | Consent oversight, policy enforcement |
| **Privacy Team** | Request handling, compliance |
| **Security Team** | Technical implementation |
| **All Staff** | Report consent issues |

---

## 12. Compliance & Enforcement

### 12.1 Compliance Monitoring

| Check | Frequency | Owner |
|-------|-----------|-------|
| Consent accuracy | Quarterly | Privacy Team |
| Request handling | Monthly | DPO |
| Jurisdiction updates | As needed | Legal |
| Policy review | Annual | DPO |

### 12.2 Non-Compliance Consequences

| Violation | Consequence |
|-----------|-------------|
| Unlawful processing | GDPR Art. 83 - Up to 4% revenue |
| BIPA violation | $1,000/violation (negligent) |
| BIPA violation | $5,000/violation (intentional) |
| CCPA violation | $2,500/violation |
| COPPA violation | FTC enforcement |

### 12.3 Training Requirements

| Role | Training | Frequency |
|------|----------|-----------|
| All Staff | Privacy fundamentals | Annual |
| Developers | Consent implementation | Annual |
| Support | Request handling | Annual |
| DPO | Advanced privacy | Annual |

---

## Annex A: Consent Types Matrix

| Processing Type | Legal Basis | Consent Method | Withdrawal | Retention |
|----------------|-------------|---------------|------------|-----------|
| Authentication (factors) | Art. 6(1)(b) | Terms acceptance | N/A - contract | Active+30d |
| Biometric (voice) | Art. 9(2)(a) | Explicit consent | Yes - account | Active+30d |
| Biometric (face) | Art. 9(2)(a) | Explicit consent | Yes - account | Active+30d |
| Biometric (fingerprint) | Art. 9(2)(a) | Explicit consent | Yes - account | Active+30d |
| Device fingerprint | Art. 6(1)(f) | Legitimate interest | Yes - settings | 2 years |
| Marketing email | Art. 6(1)(a) | Opt-in | Yes - unsubscribe | Consent+30d |
| Analytics | Art. 6(1)(a) | Cookie consent | Yes - banner | 1 year |
| Third-party sharing | Art. 6(1)(a) | Opt-in | Yes | Consent date |

---

## Annex B: Jurisdiction Requirements

| Jurisdiction | Opt-in/Opt-out | Children | Breach | DPO |
|--------------|---------------|----------|--------|------|
| **EU** | Opt-in (sensitive) | 16 (13 UK) | 72h notice | Required |
| **UK** | Opt-in (sensitive) | 13 | 72h notice | Required |
| **California** | Opt-out | 13 | None | Recommended |
| **Illinois** | Opt-in (biometric) | 13 | None | - |
| **Texas** | Opt-out | 13 | None | - |
| **Canada** | Meaningful opt-in | 13 | None | - |

---

## Annex C: Consent Wording Templates

### C.1 Biometric Consent (BIPA)

```
NoTap Biometric Authentication Consent

PURPOSE: We use your [voice/fingerprint/face] to verify your identity 
during authentication as an alternative to PIN or password.

DATA COLLECTED: Biometric template (encoded mathematical representation)

RETENTION: Your biometric data is retained while your account is active 
and for 30 days after account deactivation.

SHARING: Your biometric data is not shared with any third parties.

YOUR RIGHTS: You may withdraw this consent at any time by disabling 
biometric authentication in your account settings or contacting dpo@notap.io.
Account deletion will automatically delete your biometric data.

By enabling biometric authentication, I acknowledge that I have read 
and agree to this consent.
[ ] I agree
```

### C.2 Marketing Consent

```
Stay Connected (Optional)

I'd like to receive:
[ ] Product updates and feature announcements
[ ] Security tips and best practices
[ ] Early access to new features

We respect your privacy. You can unsubscribe anytime.
Our full Privacy Policy: notap.io/privacy
```

---

## Document Control

- **Version**: 1.0.0
- **Status**: DRAFT
- **Approved By**: DPO (pending), Executive Sponsor (pending)
- **Effective Date**: Upon approval
- **Next Review**: 2027-04-22
- **Location**: `documentation/05-security/POLICIES/POL-004_PRIVACY_CONSENT_POLICY.md`

**For questions**: Contact DPO or see CLAUDE.md.

(End of file - 608 lines)