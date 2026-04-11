# Screen Security Compliance Document

**Last Updated:** 2026-04-11
**Status:** IN PROGRESS
**Feature:** Screen Security (Screen Sharing, Remote Access, Tapjacking Protection)

---

## 1. Overview

### Feature Description
Screen security module provides protection against visual interception attacks during authentication:
- Screen sharing/recording detection
- Remote access app detection (AnyDesk, TeamViewer, VNC)
- Tapjacking/overlay attack prevention
- Accessibility service attack detection
- Factor filtering based on security context

### Business Justification
Prevents attackers from observing factor challenges during authentication:
- Screen sharing during verification
- Recording attacks on sensitive factors (Pattern, Emoji, ImageTap)
- Remote access tools (AnyDesk, TeamViewer)
- Malicious overlays that capture taps

### Scope
Full implementation (scope b) - All screen security features:
- Screenshot blocking (FLAG_SECURE)
- Screen recording blocking
- Remote access app detection
- Tapjacking/overlay detection
- Accessibility service detection
- Factor filtering in DEGRADE mode

---

## 2. Data Handling Matrix

### Data Collected

| Data Point | Source | Format | Retention |
|------------|--------|--------|-----------|
| **Screen State** | Android MediaProjection API | Boolean | Session only |
| **Remote Access Apps** | PackageManager query | List<String> | None (query only) |
| **Overlay Permission** | Settings.canDrawOverlays() | Boolean | None (query only) |
| **Accessibility State** | AccessibilityManager | Boolean | None (query only) |
| **Detection Timestamp** | System.currentTimeMillis() | Long | 24h |
| **Threat Log** | Redis | Encrypted JSON | 24h TTL |

### Data Storage

| Data | Where | Encrypted? | Anonymized? | TTL |
|------|-------|------------|------------|-----|
| Threat Alert | Redis | AES-256-GCM | Hash | 24h |
| Package Hash | In-memory only | N/A | SHA-256 | None |
| Settings State | In-memory only | N/A | N/A | Session |

### Data Exits System

| Data | To Whom | What Fields | Minimized? |
|------|---------|-------------|------------|
| Merchant Alert | Merchant webhook | threat_type, severity, timestamp | ✅ Yes |
| Audit Log | Internal | event, timestamp, user_hash | ✅ Yes |
| No PII exits | N/A | N/A | N/A |

### What is NOT Collected

| Excluded Data | Reason |
|---------------|--------|
| Screen content | Privacy, not needed |
| Remote session content | Privacy, not needed |
| Overlay app credentials | Not relevant |
| User input during detection | Privacy |

### Legal Basis

| Data | GDPR Art. 6 Basis |
|------|------------------|
| Threat detection | 1(f) - Legitimate interest |
| Alert storage | 1(f) - Security logging |
| Merchant notification | 1(f) - Fraud prevention |

---

## 3. Security & Compliance Matrix

### Endpoints

| Endpoint | Auth Method | Rate Limit | Replay Protection | Audit Log |
|----------|-------------|------------|-------------------|-----------|
| `/screen/threats` | Bearer token | 10/min | Nonce + timestamp | ✅ |
| `/screen/status` | Bearer token | 30/min | Nonce | ✅ |

### Authentication
- Bearer token required for all endpoints
- CSRF protection via state parameter
- Session validity: 15 minutes

### Storage Security

| Storage | Encryption | Memory Wipe | TTL |
|---------|------------|------------|-----|
| Redis alerts | AES-256-GCM | N/A | 24h |
| Package names | In-memory hash | Buffer.wipe() | Session |
| Detection state | N/A | Clear on session end | Session |

### Logging

**Requirement:** Use structured logger - NO PII

```kotlin
// ✅ CORRECT
logger.info("Screen threat detected", "threat" to "REMOTE_ACCESS")

// ❌ WRONG - Leaks PII
logger.info("User $userName has screen sharing active")
```

**Auto-redaction:** Logger automatically redacts:
- Device IDs → hashed
- IP addresses → anonymized
- Package names → hashed

### Privacy

| Data | Hashing Method |
|------|----------------|
| Package names | SHA-256 |
| Device IDs | privacyUtils.hashDeviceId() |
| User IDs | Not logged |

### Biometrics

**N/A** - This feature does NOT collect biometric data.

### Secrets Handling

```kotlin
// Buffer-based handling for sensitive data
fun detectThreats() {
    val packageList = mutableListOf<String>()
    try {
        // Use packageList
    } finally {
        packageList.clear()  // Wipe after use
    }
}
```

### Constant-Time Comparisons

All security comparisons use constant-time functions:
- `ConstantTime.equals()` for threat matching
- No early returns on mismatch

### External URLs

**N/A** - Screen security does NOT make external URL calls.

### Retention

| Data | Grace Period | Cleanup |
|------|--------------|---------|
| Threat alerts | 24h | dataRetentionCleanup.js |
| Session state | Session end | Auto-clear |
| Detection logs | 24h | Auto-expire TTL |

---

## 4. Risk Assessment

### What if this data leaks?

| Scenario | Blast Radius | Regulatory Impact | Mitigation |
|----------|--------------|-------------------|------------|
| Detection logic leak | Low | None | Logic is public Android APIs |
| Alert data leak | Medium | GDPR notification | Encrypted + hashed |
| False positive | Low | User complaint | Allow override + warn |

### What if endpoint is abused?

| Attack | Likelihood | Impact | Mitigation |
|--------|------------|--------|----------|
| DDoS on /status | Low | DoS | Rate limit 30/min |
| False threat reports | Medium | Alert spam | Verify with multiple signals |
| Bypass detection | Low | Security bypass | Log + alert merchant |

### What if secrets compromised?

| Secret | Rotation Plan | Revocation |
|--------|---------------|------------|
| Session key | 15-min TTL auto-rotate | Blacklist available |

---

## 5. Implementation Reference

### Core Files

| File | Purpose |
|------|----------|
| `sdk/src/commonMain/.../config/ScreenSecurityConfig.kt` | Configuration |
| `sdk/src/androidMain/.../security/ScreenSecurityProvider.kt` | Android detection |
| `sdk/src/jsMain/.../security/WebScreenSecurityProvider.kt` | Web detection |

### Configuration

```kotlin
ScreenSecurityConfig.Config(
    screenshotMode = ProtectionMode.DEGRADE,
    screenRecordingMode = ProtectionMode.DEGRADE,
    remoteAccessMode = ProtectionMode.DEGRADE,
    tapjackingMode = ProtectionMode.BLOCK,
    accessibilityMode = ProtectionMode.DEGRADE,
    warnDurationMs = 3000L,
    allowUserOverride = true,
    merchantAlertEnabled = true,
    failFactorFilterEnabled = true
)
```

### Integration Points

- **SecurityPolicy:** Screen threats evaluated alongside device threats
- **FactorConfig:** Visual factors filtered in DEGRADE mode
- **Factor filtering:** Uses `ScreenSecurityConfig.filterFactorsForSecurity()`

---

## 6. Integration with SecurityPolicy

### Threat Evaluation

```kotlin
// SecurityPolicy evaluates screen threats
fun evaluate(screenThreats: Set<ScreenThreat>): SecurityDecision {
    val actions = screenThreats.map { ScreenSecurityConfig.getModeForThreat(it) }
    
    return when {
        actions.any { it == ProtectionMode.BLOCK } -> 
            SecurityDecision(BLOCK_PERMANENT, threats, ...)
        actions.any { it == ProtectionMode.DEGRADE } ->
            SecurityDecision(DEGRADE, threats, ...)
        actions.any { it == ProtectionMode.DETECT_ONLY } ->
            SecurityDecision(WARN, threats, ...)
        else -> SecurityDecision(ALLOW, emptyList(), ...)
    }
}
```

### Response Actions

| Mode | Action |
|------|--------|
| DISABLED | No checking |
| DETECT_ONLY | Log only, continue |
| BLOCK | Block authentication |
| DEGRADE | Filter visual factors |

---

## 7. Constants and Thresholds

| Constant | Value | Rationale |
|----------|-------|----------|
| WARN_DURATION_MS | 3000 | 3 second visible warning |
| MAX_RATE_LIMIT | 30/min | Prevent DoS |
| ALERT_TTL | 24h | GDPR storage limitation |
| PACKAGE_LIST_CHECK | On enrollment + periodic | Security |

---

## 8. Audit Trail

All screen security events logged:
- `SCREEN_THREAT_DETECTED`
- `SCREEN_THREAT_BLOCKED`
- `SCREEN_FACTOR_FILTERED`
- `SCREEN_ALERT_SENT`

---

## 9. Approval

| Role | Name | Date |
|------|------|------|
| Security Review | |  |
| Privacy Review | |  |
| Product Approval | |  |

---

*This document accompanies the screen security implementation and is committed alongside the code.*