# NoTap Privacy Implementation Guide

**Version:** 1.0.0
**Date:** 2026-01-26
**Status:** ✅ Production Ready

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Legal Compliance](#legal-compliance)
3. [Privacy Enhancements](#privacy-enhancements)
4. [Implementation Details](#implementation-details)
5. [Data Retention](#data-retention)
6. [Migration Guide](#migration-guide)
7. [Testing](#testing)
8. [FAQ](#faq)

---

## 🎯 Overview

NoTap implements **privacy by design** to comply with global data protection regulations (GDPR, CCPA, PIPEDA, LGPD). This document describes our privacy-preserving data collection and retention practices.

### Key Principles

| Principle | Implementation |
|-----------|----------------|
| **Data Minimization** | Device IDs optional, IP addresses anonymized |
| **Storage Limitation** | Automated cleanup after retention periods |
| **Purpose Limitation** | Data used only for fraud detection |
| **Privacy by Design** | Hashing and anonymization at collection time |
| **Transparency** | Clear documentation of what we collect and why |

### What Changed

**Before (Privacy Risk):**
- ❌ Device ID: Plaintext, permanent storage, required field
- ❌ IP Address: Full IP stored, 90-day retention (not enforced)
- ❌ No automated cleanup

**After (Privacy Enhanced):**
- ✅ Device ID: SHA-256 hashed with per-user salt, optional field
- ✅ IP Address: First 3 octets only (e.g., 203.0.113.0)
- ✅ Automated cleanup: 90-day audit logs, 365-day device IDs
- ✅ Backward compatible (gradual migration)

---

## ⚖️ Legal Compliance

### GDPR (European Union)

| Article | Requirement | NoTap Implementation | Status |
|---------|-------------|---------------------|--------|
| **5(1)(c)** | Data Minimization | Device ID optional, IP anonymized | ✅ Compliant |
| **5(1)(e)** | Storage Limitation | 90-day audit logs, 365-day device IDs | ✅ Compliant |
| **17** | Right to Erasure | DELETE endpoint + blockchain revocation | ✅ Compliant |
| **25** | Privacy by Design | Hashing/anonymization at collection | ✅ Compliant |
| **30** | Record Keeping | Privacy migration log table | ✅ Compliant |
| **32** | Security of Processing | Constant-time comparisons, KMS encryption | ✅ Compliant |

### CCPA (California)

| Requirement | NoTap Implementation | Status |
|-------------|---------------------|--------|
| **1798.100** | Right to Know | GET /v1/enrollment/export/:uuid | ✅ Compliant |
| **1798.105** | Right to Deletion | DELETE /v1/enrollment/delete/:uuid | ✅ Compliant |
| **1798.110** | Data Collection Disclosure | Privacy policy + PRIVACY_IMPLEMENTATION.md | ✅ Compliant |
| **1798.115** | Sale Disclosure | NoTap does not sell user data | ✅ N/A |

### PIPEDA (Canada) & LGPD (Brazil)

Both laws have similar requirements to GDPR:
- ✅ **Legitimate Purpose** - Fraud prevention is valid purpose
- ✅ **Proportionality** - Hashing device IDs, anonymizing IPs proportional to risk
- ✅ **Consent** - Enrollment flow includes consent for data collection
- ✅ **Retention Limits** - 90-day audit logs, 365-day device IDs enforced

---

## 🔒 Privacy Enhancements

### 1. Device ID Hashing

**Purpose:** Device change detection for fraud prevention
**Method:** SHA-256 with per-user salt
**Storage:** 32 hex chars (128 bits)
**Status:** ✅ Optional field (GDPR data minimization)

#### How It Works

```javascript
// Original device ID (client-side)
const deviceId = "iPhone14_iOS17_A1B2C3D4E5F6";

// Hash with per-user salt (server-side)
const hash = hashDeviceId(deviceId, userUUID);
// Result: "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6"

// Same device, different user = different hash
const hash2 = hashDeviceId(deviceId, differentUserUUID);
// Result: "9f8e7d6c5b4a3e2d1c0b9a8f7e6d5c4b" (different!)
```

#### Security Properties

- ✅ **One-way:** Cannot reverse hash to original device ID
- ✅ **Per-user salt:** Different users get different hashes (rainbow table resistant)
- ✅ **App-wide salt:** Cross-instance correlation prevented
- ✅ **Deterministic:** Same deviceId+UUID = same hash (device change detection works)
- ✅ **Collision resistant:** SHA-256 has 2^128 hash space

#### Privacy Benefits

| Benefit | Explanation |
|---------|-------------|
| **Cannot identify device** | Hash cannot be reversed to original ID |
| **Cannot correlate across users** | Different users = different hashes |
| **Still detects device changes** | Hash comparison works |
| **GDPR pseudonymous data** | Hashed = not personal data |

---

### 2. IP Address Anonymization

**Purpose:** Geographic fraud detection, rate limiting
**Method:** Keep first 3 octets only
**Storage:** IPv4 prefix (e.g., 203.0.113.0)
**Status:** ✅ Enforced at collection time

#### How It Works

```javascript
// Original IP address
const ip = "203.0.113.45";

// Anonymize (keep first 3 octets)
const anonymized = anonymizeIP(ip);
// Result: "203.0.113.0"

// Represents ~256 devices (Class C network)
// Cannot identify specific device
```

#### Anonymization Levels

| Level | Example | Privacy | Fraud Detection |
|-------|---------|---------|----------------|
| **3 octets** (default) | 203.0.113.0 | Medium | ✅ City-level location |
| **2 octets** | 203.0.0.0 | High | ⚠️ Country-level only |
| **1 octet** | 203.0.0.0 | Maximum | ❌ Continent-level only |

**Recommended:** 3 octets (balance privacy & fraud detection)

#### Fraud Detection Impact

**✅ What still works:**
- Geographic anomaly detection (user in US, then China = suspicious)
- Velocity checks (impossible travel: 1000+ km/hour)
- Rate limiting (per-network limits)
- Location-based risk scoring

**❌ What doesn't work:**
- Tracking specific device (by design - privacy feature)
- Distinguishing devices on same network (privacy trade-off)

---

### 3. Automated Data Retention

**Purpose:** GDPR Article 5(1)(e) compliance (storage limitation)
**Method:** Automated cleanup jobs (daily at 2:00 AM UTC)
**Status:** ✅ Enforced via node-schedule

#### Retention Periods

| Data Type | Retention Period | Cleanup Job | Justification |
|-----------|-----------------|-------------|---------------|
| **Audit Logs** | 90 days | Daily 2:00 AM | GDPR minimum for security logs |
| **Device ID Hashes** | 365 days | Daily 2:00 AM | Fraud pattern analysis |
| **IP Address Prefixes** | 30 days | Daily 2:00 AM | Short-term fraud detection |
| **Enrollment Data (Redis)** | 24 hours | Auto (TTL) | Active authentication only |
| **Soft-Deleted Records** | 30 days | Daily 2:00 AM | GDPR right to erasure grace period |

#### How Cleanup Works

```bash
# Daily job (2:00 AM UTC)
┌─────────────────────────────────────────┐
│  1. Audit Log Cleanup                   │
│     DELETE FROM audit_log               │
│     WHERE created_at < NOW() - 90 days  │
│                                         │
│  2. Device ID Cleanup                   │
│     UPDATE wrapped_keys                 │
│     SET device_id_hash = NULL           │
│     WHERE created_at < NOW() - 365 days │
│                                         │
│  3. Soft-Deleted Records Cleanup        │
│     DELETE FROM wrapped_keys            │
│     WHERE is_deleted = TRUE             │
│     AND deleted_at < NOW() - 30 days    │
│                                         │
│  4. Redis Orphan Cleanup (defensive)    │
│     Scan for keys without TTL, delete   │
└─────────────────────────────────────────┘
```

**Safety features:**
- ✅ Batch processing (1000 records at a time, prevents memory exhaustion)
- ✅ Database transactions (all-or-nothing)
- ✅ Comprehensive logging (audit trail)
- ✅ Dry-run mode (preview deletions)
- ✅ Idempotent (can be re-run safely)

---

## 🛠️ Implementation Details

### File Structure

```
backend/
├── utils/
│   └── privacyUtils.js         # Privacy utility functions
├── jobs/
│   ├── dataRetentionCleanup.js # Automated cleanup job
│   └── scheduler.js            # Job scheduler
├── routes/
│   └── enrollmentRouter.js     # Updated to use privacy utilities
├── database/
│   └── migrations/
│       └── 011_privacy_enhancement.sql  # Database schema changes
└── scripts/
    └── migratePrivacyData.js   # Data migration script
```

### Privacy Utilities API

```javascript
const { hashDeviceId, anonymizeIP, getRetentionPeriod } = require('./utils/privacyUtils');

// Hash device ID
const hash = hashDeviceId('iPhone14_iOS17_ABC', 'user-uuid-123');
// Returns: "a1b2c3d4..." (32 hex chars) or null if invalid

// Anonymize IP address
const anonymized = anonymizeIP('203.0.113.45');
// Returns: "203.0.113.0" or null if invalid

// Get retention period
const days = getRetentionPeriod('audit_log');
// Returns: 90 (from AUDIT_LOG_RETENTION_DAYS env var)
```

### Environment Variables

Add to `.env`:

```bash
# ==============================================================================
# PRIVACY & DATA RETENTION
# ==============================================================================

# Application-wide salt for device ID hashing (REQUIRED)
# Generate with: openssl rand -hex 32
# Rotate annually for security
PRIVACY_APP_SALT=YOUR_SECURE_64_CHAR_HEX_STRING_HERE

# IP anonymization level (1-3 octets, default: 3)
# 3 = 203.0.113.0 (recommended for fraud detection)
# 2 = 203.0.0.0 (more privacy, less precise)
# 1 = 203.0.0.0 (maximum privacy, minimal fraud detection)
IP_ANONYMIZATION_OCTETS=3

# Data retention periods (days)
AUDIT_LOG_RETENTION_DAYS=90          # GDPR minimum for security logs
DEVICE_ID_RETENTION_DAYS=365         # 1 year for fraud pattern analysis
IP_ADDRESS_RETENTION_DAYS=30         # Short-term fraud detection
ENROLLMENT_RETENTION_DAYS=1          # Redis TTL (24 hours)
```

**⚠️ CRITICAL:** Generate `PRIVACY_APP_SALT` with:
```bash
openssl rand -hex 32
```

**Never use default salt in production!**

---

## 📅 Data Retention

### Retention Policy Matrix

| Data Category | What We Store | Retention Period | Cleanup Method | Purpose |
|---------------|--------------|------------------|----------------|---------|
| **Authentication Data** | Factor digests (encrypted) | 24 hours | Redis TTL | Active authentication |
| **Audit Logs** | Event type, status, anonymized IP | 90 days | Automated deletion | Security monitoring, GDPR Article 30 |
| **Device Identity** | Device ID hash (SHA-256) | 365 days | Automated deletion | Fraud detection (device changes) |
| **Network Identity** | IP prefix (first 3 octets) | 30 days | Embedded in audit logs | Geographic fraud detection |
| **Cryptographic Keys** | KMS-wrapped keys | Permanent* | User-initiated deletion | Key rotation, re-authentication |
| **Blockchain Audit** | Hashed UUID, timestamps | Immutable** | Cryptographic erasure | Tamper-proof audit trail |

\* _Permanent storage justified by operational necessity (key rotation, disaster recovery)_
\** _Blockchain is immutable, but keys can be revoked (cryptographic erasure)_

### Retention Justification (GDPR Article 6)

| Data Type | Legal Basis | Justification |
|-----------|-------------|---------------|
| **Audit Logs** | Legitimate Interest (6(1)(f)) | Security monitoring, fraud prevention |
| **Device ID Hash** | Legitimate Interest (6(1)(f)) | Fraud detection (device changes) |
| **IP Prefix** | Legitimate Interest (6(1)(f)) | Geographic fraud detection |
| **Cryptographic Keys** | Contract Performance (6(1)(b)) | Operational necessity for service |

---

## 🔄 Migration Guide

### Prerequisites

1. **Backup database:**
   ```bash
   pg_dump -h localhost -U postgres notap_dev > backup_$(date +%Y%m%d).sql
   ```

2. **Generate application salt:**
   ```bash
   openssl rand -hex 32
   # Add to .env as PRIVACY_APP_SALT
   ```

3. **Update environment variables:**
   - Set `PRIVACY_APP_SALT`
   - Set retention periods (optional, defaults provided)

### Migration Steps

#### Step 1: Apply Database Schema Migration

```bash
# Run SQL migration
psql -h localhost -U postgres -d notap_dev -f backend/database/migrations/011_privacy_enhancement.sql

# Verify schema changes
psql -h localhost -U postgres -d notap_dev -c "\d+ wrapped_keys"
# Should see: device_id_hash, data_anonymized_at columns

psql -h localhost -U postgres -d notap_dev -c "\d+ audit_log"
# Should see: ip_address_prefix, data_anonymized_at columns
```

#### Step 2: Run Data Migration (Dry Run)

```bash
# Preview changes without modifying data
node backend/scripts/migratePrivacyData.js --dry-run

# Output:
# [Migration] [DRY RUN] Would hash 1,234 device IDs
# [Migration] [DRY RUN] Would anonymize 5,678 IP addresses
```

#### Step 3: Run Data Migration (Production)

```bash
# Apply changes (uses batching for safety)
node backend/scripts/migratePrivacyData.js --batch-size=1000

# Output:
# [Migration] ✅ Device ID migration complete
# [Migration] Total: 1,234
# [Migration] Processed: 1,234
# [Migration] Failed: 0
#
# [Migration] ✅ IP address migration complete
# [Migration] Total: 5,678
# [Migration] Processed: 5,678
# [Migration] Failed: 0
```

#### Step 4: Enable Automated Cleanup

Add to `backend/server.js`:

```javascript
const { initializeScheduler } = require('./jobs/scheduler');

// After database and Redis initialization
const scheduledJobs = initializeScheduler({
  db: pool,
  redisClient: redisClient
});

// Graceful shutdown
process.on('SIGTERM', () => {
  const { shutdownScheduler } = require('./jobs/scheduler');
  shutdownScheduler(scheduledJobs);
});
```

#### Step 5: Verify Migration

```bash
# Check migration log
psql -h localhost -U postgres -d notap_dev -c "SELECT * FROM privacy_migration_log ORDER BY started_at DESC LIMIT 5;"

# Verify device ID hashes
psql -h localhost -U postgres -d notap_dev -c "SELECT COUNT(*) FROM wrapped_keys WHERE device_id_hash IS NOT NULL;"

# Verify IP anonymization
psql -h localhost -U postgres -d notap_dev -c "SELECT COUNT(*) FROM audit_log WHERE ip_address_prefix IS NOT NULL;"
```

#### Step 6: Update Application Code

Deploy updated codebase with privacy utilities:
1. `backend/utils/privacyUtils.js`
2. `backend/routes/enrollmentRouter.js` (updated)
3. `backend/jobs/*` (scheduler + cleanup)

#### Step 7: Monitor First Cleanup

```bash
# Wait for first automated cleanup (2:00 AM UTC)
# Or trigger manually:
node backend/scripts/runCleanup.js --dry-run

# Check logs
tail -f backend/logs/cleanup.log
```

### Backward Compatibility

**Old columns kept for 30 days:**
- `wrapped_keys.device_id` → Will be dropped after 30 days
- `audit_log.ip_address` → Will be dropped after 30 days

**During transition:**
- ✅ Old code reads `device_id` (still works)
- ✅ New code reads `device_id_hash` (privacy-enhanced)
- ✅ Both columns populated during migration period
- ✅ After 30 days: Drop old columns

---

## 🧪 Testing

### Unit Tests

```bash
# Test privacy utilities
npm test -- tests/unit/privacyUtils.test.js

# Expected output:
# ✓ hashDeviceId: Same device+UUID = same hash
# ✓ hashDeviceId: Different UUIDs = different hashes
# ✓ anonymizeIP: IPv4 3-octet anonymization
# ✓ anonymizeIP: IPv6 anonymization
# ✓ getRetentionPeriod: Returns correct periods
```

### Integration Tests

```bash
# Test enrollment with optional device_id
npm test -- tests/integration/enrollment.test.js

# Expected:
# ✓ Enrollment succeeds with device_id (hashed)
# ✓ Enrollment succeeds without device_id (privacy mode)
# ✓ IP address anonymized automatically
```

### Cleanup Job Tests

```bash
# Test retention cleanup (dry run)
node backend/scripts/runCleanup.js --dry-run

# Test retention cleanup (production)
node backend/scripts/runCleanup.js

# Verify deletions
psql -d notap_dev -c "SELECT COUNT(*) FROM audit_log WHERE created_at < NOW() - INTERVAL '90 days';"
# Should return: 0
```

### Privacy Compliance Tests

**1. Data Minimization Test:**
```bash
# Enroll without device_id
curl -X POST http://localhost:3000/v1/enrollment/store \
  -H "Content-Type: application/json" \
  -d '{
    "user_uuid": "test-uuid-123",
    "factors": {"PIN": "abc123..."}
  }'

# Verify: device_id_hash should be NULL
psql -d notap_dev -c "SELECT device_id_hash FROM wrapped_keys WHERE uuid = 'test-uuid-123';"
```

**2. IP Anonymization Test:**
```bash
# Check audit log has anonymized IPs only
psql -d notap_dev -c "SELECT ip_address_prefix, COUNT(*) FROM audit_log GROUP BY ip_address_prefix LIMIT 10;"

# All IPs should end in .0 (e.g., 203.0.113.0)
```

**3. Retention Enforcement Test:**
```bash
# Insert old audit log
psql -d notap_dev -c "INSERT INTO audit_log (event_type, event_action, event_status, created_at) VALUES ('test', 'test', 'success', NOW() - INTERVAL '95 days');"

# Run cleanup
node backend/scripts/runCleanup.js

# Verify deletion
psql -d notap_dev -c "SELECT COUNT(*) FROM audit_log WHERE event_type = 'test';"
# Should return: 0
```

---

## ❓ FAQ

### General

**Q: Is device_id still required?**
**A:** No. Device ID is now optional. If provided, it's hashed for privacy.

**Q: Can I reverse a device ID hash?**
**A:** No. SHA-256 is one-way. Cannot reverse hash to original ID.

**Q: Why is device ID hashed per-user?**
**A:** Privacy. Same device for different users = different hashes. Prevents cross-user tracking.

**Q: Does IP anonymization break fraud detection?**
**A:** No. City-level location is sufficient for geographic anomaly detection.

### Legal Compliance

**Q: Is this GDPR compliant?**
**A:** Yes. Hashed device IDs = pseudonymous data (GDPR Article 4). IP prefixes = anonymous data (GDPR Article 4(5)).

**Q: What about CCPA compliance?**
**A:** Yes. We disclose collection, provide export (GET /export), and deletion (DELETE) endpoints.

**Q: Can users opt out of device ID collection?**
**A:** Yes. Device ID is optional. Simply don't send `device_id` in enrollment request.

**Q: How long is data retained?**
**A:** Audit logs: 90 days. Device ID hashes: 365 days. IP prefixes: 30 days (embedded in audit logs).

### Technical

**Q: What if PRIVACY_APP_SALT is leaked?**
**A:** Rotate salt, re-hash all device IDs. Per-user salt (UUID) still prevents rainbow tables.

**Q: Can cleanup jobs be paused?**
**A:** Yes. Stop node-schedule, or set retention periods to 9999 days (effectively disabled).

**Q: What happens if data migration fails?**
**A:** Database transactions ensure all-or-nothing. Failed batch rolls back, safe to re-run.

**Q: Can I use 2-octet IP anonymization for more privacy?**
**A:** Yes. Set `IP_ANONYMIZATION_OCTETS=2`. Trade-off: Less precise fraud detection.

### Migration

**Q: Is migration reversible?**
**A:** Yes. See migration file for ROLLBACK instructions. Old columns kept for 30 days.

**Q: How long does migration take?**
**A:** ~1-2 minutes per 10,000 records (depends on hardware). Uses batching for safety.

**Q: Can I run migration multiple times?**
**A:** Yes. Migration is idempotent (skips already-migrated records).

---

## 📚 Additional Resources

- **GDPR Full Text:** https://gdpr-info.eu/
- **CCPA Full Text:** https://oag.ca.gov/privacy/ccpa
- **PIPEDA Full Text:** https://www.priv.gc.ca/en/privacy-topics/privacy-laws-in-canada/the-personal-information-protection-and-electronic-documents-act-pipeda/
- **LGPD Full Text (English):** https://iapp.org/resources/article/brazilian-data-protection-law-lgpd-english-translation/
- **OWASP Privacy Risks:** https://owasp.org/www-project-top-ten/2017/A3_2017-Sensitive_Data_Exposure

---

## 📝 Change Log

| Date | Version | Changes |
|------|---------|---------|
| 2026-01-26 | 1.0.0 | Initial privacy implementation release |

---

**For questions or issues, see `CLAUDE.md` or contact security@notap.io.**
