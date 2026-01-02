# API Endpoint Test Results

**Test Date:** 2025-12-28  
**API URL:** https://api.notap.io  
**Total Endpoints Tested:** 73 (sample of 322)

---

## 📊 Summary

| Status | Count | % | Meaning |
|--------|-------|---|---------|
| ✅ **Success (2xx)** | 6 | 8% | Working correctly |
| 🔐 **Auth Required (401)** | 21 | 29% | Correct - needs JWT token |
| ⚠️ **Bad Request (400)** | 25 | 34% | Correct - validation errors |
| ❌ **Not Found (404)** | 20 | 27% | Missing routes or path issues |
| 💥 **Server Error (5xx)** | 1 | 1% | Needs fixing |

**Overall Health:** 71% endpoints functioning correctly (52/73)

---

## ✅ Working Endpoints (6)

| Endpoint | Status | Notes |
|----------|--------|-------|
| GET /health | 200 | Health check |
| GET / | 200 | Root endpoint |
| POST /v1/auth/developer/logout | 200 | Logout |
| POST /v1/auth/user/logout | 200 | Logout |
| POST /v1/auth/merchant/logout | 200 | Logout |
| POST /v1/auth/admin/logout | 200 | Logout |

---

## 🔐 Auth Required (21) - CORRECT BEHAVIOR

These endpoints correctly return 401 when no JWT token is provided:

| Endpoint | User Type |
|----------|-----------|
| POST /v1/auth/developer/notap/link | Developer |
| GET /v1/auth/developer/profile | Developer |
| PUT /v1/auth/developer/profile | Developer |
| POST /v1/auth/developer/validate | Developer |
| POST /v1/auth/user/notap/link | User |
| GET /v1/auth/user/profile | User |
| PUT /v1/auth/user/profile | User |
| POST /v1/auth/user/validate | User |
| GET /v1/auth/merchant/profile | Merchant |
| PUT /v1/auth/merchant/profile | Merchant |
| POST /v1/auth/merchant/validate | Merchant |
| GET /v1/auth/admin/profile | Admin |
| POST /v1/auth/admin/validate | Admin |
| DELETE /v1/management/enrollment/:uuid | Management |
| GET /v1/management/export/:uuid | Management |
| PUT /v1/management/auto-renewal/:uuid | Management |
| DELETE /v1/management/payment/:uuid | Management |
| GET /v1/management/devices/:uuid | Management |
| POST /v1/management/devices/:uuid | Management |
| DELETE /v1/management/devices/:uuid/:deviceId | Management |
| GET /v1/admin/stats | Admin |

---

## ⚠️ Validation Errors (25) - CORRECT BEHAVIOR

These return 400 because test sent empty/incomplete payloads:

| Endpoint | Required Fields |
|----------|-----------------|
| POST /v1/auth/developer/register | email, password, fullName |
| POST /v1/auth/developer/login | email, password |
| POST /v1/auth/developer/oauth/callback | provider, code |
| POST /v1/auth/developer/notap/login | notapUuid, verificationResult |
| POST /v1/auth/developer/verify-email | token |
| POST /v1/auth/developer/request-reset | email |
| POST /v1/auth/developer/reset-password | token, newPassword |
| POST /v1/auth/user/register | email, password, fullName |
| POST /v1/auth/user/login | email, password |
| POST /v1/auth/user/oauth/callback | provider, code |
| POST /v1/auth/user/notap/login | notapUuid, verificationResult |
| POST /v1/auth/user/verify-email | token |
| POST /v1/auth/user/request-reset | email |
| POST /v1/auth/user/reset-password | token, newPassword |
| POST /v1/auth/merchant/register | email, password, fullName, businessName |
| POST /v1/auth/merchant/login | email, password |
| POST /v1/auth/admin/register | email, password, fullName, notapUuid |
| POST /v1/auth/admin/login | email, password, mfaVerification |
| PUT /v1/enrollment/renew/:uuid | factors |
| POST /v1/verification/initiate | user_uuid |
| POST /v1/verification/verify | session_id, factors |
| POST /v1/management/verify | uuid, factors |
| POST /v1/sns/transfer | from, to, name |
| POST /v1/sns/relink | name, newUuid |
| POST /v1/names/validate | name |

---

## ❌ Not Found (20) - NEEDS INVESTIGATION

| Endpoint | Possible Cause |
|----------|----------------|
| DELETE /v1/auth/developer/account | Endpoint may not exist |
| POST /v1/enrollment/initiate | Route registration issue |
| POST /v1/enrollment/submit | Route registration issue |
| GET /v1/enrollment/:uuid | Test uses literal `:uuid` |
| POST /v1/enrollment/alias | Route may not exist |
| GET /v1/enrollment/alias/:alias | Test uses literal `:alias` |
| DELETE /v1/enrollment/:uuid | Test uses literal `:uuid` |
| GET /v1/verification/session/:sessionId | Test uses literal `:sessionId` |
| POST /v1/verification/timeout | Endpoint may not exist |
| PUT /v1/management/devices/:uuid/:deviceId | Test uses literals |
| GET /v1/management/devices/:uuid/activity | Test uses literal `:uuid` |
| GET /v1/admin/health | Endpoint may not exist |
| POST /v1/sns/register | Endpoint may not exist |
| GET /v1/sns/owner/:name | Test uses literal `:name` |
| GET /v1/sns/lookup | Endpoint may not exist |
| POST /v1/names/resolve | Endpoint may not exist |
| POST /v1/names/register | Endpoint may not exist |
| GET /v1/names/stats | Endpoint may not exist |
| GET /v1/names/owner/:name | Test uses literal `:name` |
| POST /v1/names/reverse-lookup | Endpoint may not exist |

**Note:** Many 404s are false positives because test uses placeholder values like `:uuid` instead of actual UUIDs.

---

## 💥 Server Errors (1) - NEEDS FIXING

| Endpoint | Status | Error |
|----------|--------|-------|
| GET /v1/sns/resolve/:name | 500 | Server-side error |

**Action:** Check server logs for `/v1/sns/resolve` error.

---

## 🔧 Recommendations

### Immediate Actions:
1. **Fix GET /v1/sns/resolve/:name** - Server error needs investigation
2. **Verify enrollment routes** - Check if `/v1/enrollment/*` routes are registered correctly
3. **Run database migrations** - Some 404s may be due to missing tables

### Test Improvements:
1. Use actual UUIDs/values instead of placeholders
2. Send proper request bodies for POST endpoints
3. Test with authentication tokens for protected endpoints

---

## 📝 How to Retest

```bash
# Set correct API URL
export API_URL="https://api.notap.io"

# Run comprehensive test
bash /tmp/test_all_endpoints.sh

# Test specific endpoint
curl -X POST "$API_URL/v1/auth/developer/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234","fullName":"Test Dev","company":"Test"}'
```

---

**Last Updated:** 2025-12-28
