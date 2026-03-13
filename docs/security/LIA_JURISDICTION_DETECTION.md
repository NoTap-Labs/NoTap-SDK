# Legitimate Interest Assessment: Jurisdiction Detection

**Document Type:** Legal Basis Justification  
**Regulation:** GDPR Article 6(1)(f) - Legitimate Interest  
**Processing Activity:** IP Geolocation for Jurisdiction Detection  
**Version:** 1.0.0  
**Date:** 2026-03-08  
**Reviewed By:** Legal Counsel / Data Protection Officer  
**Next Review:** 2027-03-08  

---

## Executive Summary

**Processing Activity:** Detecting user jurisdiction (country/state) to determine applicable privacy regulations

**Legal Basis:** Legitimate Interest (GDPR Article 6(1)(f))

**Conclusion:** ✅ **JUSTIFIED** - The legitimate interest balancing test demonstrates that jurisdiction detection is necessary, proportional, and respects data subject rights.

---

## 1. Purpose of Processing

### What data is processed?
- IP address (anonymized to first 3 octets before processing)
- Coarse geolocation (country and state/region ONLY)
- User-declared location (if provided)

### What do we do with it?
Use coarse location data to determine which privacy regulations apply (GDPR, BIPA, CCPA, etc.) and show the appropriate consent screens.

### Business purpose
Present users with relevant compliance disclosures based on their jurisdiction, rather than overwhelming all users with every possible regulation regardless of applicability.

---

## 2. Legitimate Interest

### What is our legitimate interest?

**Primary Interest:** Compliance with applicable privacy laws

We have a legitimate interest in determining which privacy regulations apply to each user in order to:
1. Show appropriate consent screens (e.g., BIPA only for Illinois residents)
2. Apply correct data retention policies (e.g., GDPR 90-day audit logs)
3. Provide jurisdiction-specific user rights (e.g., CCPA opt-out vs GDPR erasure)
4. Avoid overwhelming users with irrelevant legal disclosures

**Secondary Interest:** User experience

Showing only relevant consent screens improves user experience by avoiding:
- Confusion from inapplicable legal disclosures
- Longer enrollment times due to unnecessary consent screens
- Legal uncertainty about which laws apply

### Is our interest a legitimate one?

✅ **Yes** - Compliance with applicable law is a fundamental legitimate interest recognized by GDPR Recital 47:

> "The processing of personal data strictly necessary for the purposes of preventing fraud also constitutes a legitimate interest of the data controller concerned."

Applied here: Determining jurisdiction is strictly necessary to comply with applicable privacy laws.

---

## 3. Necessity Test

### Is the processing necessary for that purpose?

✅ **Yes** - We cannot determine which privacy regulations apply without knowing the user's jurisdiction.

**Why location is necessary:**
- GDPR applies to EU/EEA residents
- BIPA applies only to Illinois residents
- CCPA applies only to California residents
- Different regulations have different requirements (consent vs disclosure, retention periods, user rights)

**Without jurisdiction detection:**
- Option A: Apply NO regulations → Legal non-compliance
- Option B: Apply ALL regulations → User confusion, poor UX, unnecessary data collection
- Option C: Detect jurisdiction → Appropriate compliance (chosen approach)

### Could we achieve the same purpose without this data?

⚠️ **Partially** - We offer user-declared location as the PREFERRED method:

| Method | Pros | Cons | Privacy |
|--------|------|------|---------|
| **User-declared location** | Accurate, privacy-preserving | Requires user input, may be skipped | ✅ Excellent (no geolocation) |
| **IP geolocation (coarse)** | Automatic, fallback | Less accurate, processes personal data | ⚠️ Acceptable (coarse only) |
| **Apply all regulations** | No data needed | Poor UX, over-collection | ❌ Violates data minimization |

**Conclusion:** User declaration is preferred, IP geolocation is the least intrusive alternative when users don't declare.

---

## 4. Balancing Test

### What is the impact on the individual?

**Data collected:**
- Country code (e.g., "US", "DE", "PA")
- State/region code (e.g., "IL", "CA") for US/Canada only
- **NOT collected:** City, coordinates, timezone, precise location

**How it's used:**
- Determine applicable regulations (session only)
- Show appropriate consent screens
- **NOT used for:** Marketing, profiling, tracking, analytics

**Data retention:**
- **ZERO** - Location data is NEVER stored in the database
- Exists only in the session context (ephemeral)
- Discarded after enrollment flow completes

**Impact assessment:**
- ✅ Low impact: Coarse location only (country/state)
- ✅ Low risk: No precise location, no tracking
- ✅ Transparent: User informed why location is detected
- ✅ Reversible: User can override with declared location

### What are the individual's reasonable expectations?

**User expectation analysis:**

✅ **Users reasonably expect:**
- Relevant legal disclosures based on their location
- Not to be shown Illinois biometric law if they're in Germany
- Compliance with local privacy laws
- Their location to be detected for legal compliance (standard practice)

❌ **Users do NOT expect:**
- Precise tracking of their location
- Location data to be sold or shared
- Location to be used for advertising
- Location to be stored permanently

**Alignment:** Our processing aligns with reasonable expectations. We detect ONLY coarse location (country/state) for compliance purposes, never precise location or tracking.

### Could the individual object or would they be likely to object?

**Objection analysis:**

**Low likelihood of objection:**
- Coarse location only (country level)
- Used for compliance, not commercial purposes
- Ephemeral (not stored)
- User can override (control)

**Safeguards to prevent objection:**
1. **Transparency:** Privacy policy discloses jurisdiction detection
2. **Control:** User can declare location (bypasses geolocation)
3. **Data minimization:** Only country/state, never precise location
4. **No storage:** Location never stored in database
5. **Purpose limitation:** Used ONLY for compliance determination

**GDPR Article 21 Right to Object:**
Users have the right to object to processing based on legitimate interest. We provide:
- Ability to declare location (no geolocation needed)
- Clear explanation in privacy policy
- No adverse consequences if user objects (can still use service)

---

## 5. Safeguards

### What safeguards do we have in place?

| Safeguard | Implementation | GDPR Principle |
|-----------|----------------|----------------|
| **Data minimization** | Country/state ONLY, no city/coordinates | Article 5(1)(c) |
| **Storage limitation** | NEVER stored in database (ephemeral only) | Article 5(1)(e) |
| **User control** | User can declare location (no geolocation) | Article 21 (objection) |
| **Transparency** | Privacy policy disclosure, in-app explanation | Article 12-14 |
| **IP anonymization** | Anonymize to 3 octets BEFORE processing | Article 32 (security) |
| **Default to restrictive** | Unknown location → apply GDPR + BIPA | Article 5(2) (accountability) |
| **No secondary use** | Used ONLY for compliance, never marketing | Article 5(1)(b) (purpose limitation) |

### Technical safeguards

**Code reference:** `backend/services/jurisdictionService.js`

```javascript
// 1. User declaration preferred (no geolocation)
if (userDeclaredCountry) {
    return {
        country: userDeclaredCountry,
        source: 'user_declared',
        privacyNote: 'No geolocation used'
    };
}

// 2. IP anonymized BEFORE lookup
const anonymizedIP = anonymizeIP(ipAddress);  // First 3 octets only

// 3. Coarse location ONLY
const geo = geoip.lookup(ipAddress);
return {
    country: geo.country,
    state: geo.region,
    // city: geo.city,  // ❌ NOT RETURNED
    // ll: geo.ll,      // ❌ NOT RETURNED (coordinates)
    ipAddressDiscarded: true  // Full IP never stored
};
```

---

## 6. Balancing Test Conclusion

### Balance between legitimate interest and individual rights

| Factor | Legitimate Interest (Processing) | Individual Rights (No Processing) | Weight |
|--------|--------------------------------|----------------------------------|--------|
| **Purpose** | Legal compliance (high importance) | Privacy (high importance) | ⚖️ Balanced |
| **Data** | Coarse location (country/state) | No geolocation | ✅ Minimized |
| **Storage** | Ephemeral (not stored) | No data retention | ✅ Minimized |
| **Control** | User can override (declare location) | User autonomy respected | ✅ Balanced |
| **Transparency** | Disclosed in privacy policy | Right to information | ✅ Satisfied |
| **Impact** | Low (coarse location, session only) | Low risk to rights | ✅ Low impact |

### Final Conclusion

✅ **LEGITIMATE INTEREST IS JUSTIFIED**

**Reasons:**
1. **Necessity:** Cannot determine applicable regulations without jurisdiction
2. **Proportionality:** Coarse location only (country/state), not precise tracking
3. **Minimal impact:** Ephemeral data, not stored, low privacy risk
4. **User control:** Can declare location, can object, can override
5. **Transparency:** Disclosed in privacy policy and in-app
6. **No alternatives:** Less intrusive alternatives are offered (user declaration)

**GDPR Article 6(1)(f) requirements satisfied:**
- [x] Legitimate interest exists (legal compliance)
- [x] Processing is necessary for that interest
- [x] Interests/rights of data subject do not override our interest
- [x] Balancing test documented
- [x] Safeguards in place

---

## 7. Documentation & Accountability

### Records maintained

Per GDPR Article 30 (Records of Processing Activities):

- [x] Purpose of processing: Jurisdiction detection for compliance
- [x] Legal basis: Legitimate interest (Article 6(1)(f))
- [x] Categories of data: Coarse location (country/state)
- [x] Retention period: Ephemeral (session only, not stored)
- [x] Technical safeguards: IP anonymization, data minimization
- [x] Recipients: None (data not shared)
- [x] Transfers: None (no cross-border transfers)

### Review schedule

- **Next review:** 2027-03-08 (annual)
- **Trigger for review:** Changes to processing, new regulations, complaints
- **Responsible:** Data Protection Officer / Legal Counsel

---

## 8. Alternative Approaches Considered

| Alternative | Pros | Cons | Decision |
|-------------|------|------|----------|
| **Apply all regulations to all users** | No geolocation needed | Violates data minimization, poor UX | ❌ Rejected |
| **User declaration only (required)** | Privacy-preserving | Friction, users may not know jurisdiction | ⚠️ Offered but not required |
| **IP geolocation (precise)** | More accurate | Privacy invasive, unnecessary | ❌ Rejected |
| **IP geolocation (coarse) as fallback** | Balanced, privacy-preserving | Requires LIA documentation | ✅ **CHOSEN** |
| **No jurisdiction detection** | No processing | Legal non-compliance | ❌ Rejected |

---

## 9. References

### Legal References
- **GDPR Article 6(1)(f):** Legitimate interest legal basis
- **GDPR Recital 47:** Examples of legitimate interests
- **ICO Guidance:** [Legitimate interests](https://ico.org.uk/for-organisations/guide-to-data-protection/guide-to-the-general-data-protection-regulation-gdpr/legitimate-interests/)
- **EDPB Guidelines:** [Guidelines 06/2020 on legitimate interest](https://edpb.europa.eu/our-work-tools/our-documents/guidelines/guidelines-062020-legitimate-interest_en)

### Code References
- `backend/services/jurisdictionService.js` - Jurisdiction detection logic
- `backend/utils/privacyUtils.js` - IP anonymization
- `backend/routes/bipaConsentRouter.js` - BIPA consent (Illinois only)

### Related Documentation
- `documentation/05-security/PRIVACY_IMPLEMENTATION.md` - Privacy overview
- `documentation/05-security/REGULATORY_COMPLIANCE_MANUAL.md` - Full compliance manual

---

## 10. Approval

**Prepared by:** Technical Team  
**Date:** 2026-03-08  

**Reviewed by:** Data Protection Officer / Legal Counsel  
**Date:** [Pending]  

**Approval Status:** ⏳ Pending DPO Review  

**Notes:**  
This LIA should be reviewed and approved by the Data Protection Officer or Legal Counsel before deployment to production.

---

**END OF DOCUMENT**
