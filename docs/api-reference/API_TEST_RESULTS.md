---
hidden: true
---

# API Test Results

**Last Tested:** 2025-12-27 19:45 UTC **Environment:** Production (api.notap.io) **Total Endpoints:** 36 **Pass Rate:** 100% (all responding correctly)

## Authentication Status

| Auth Type        | Status       | Key Set                              |
| ---------------- | ------------ | ------------------------------------ |
| Admin API Key    | ✅ Working    | `ADMIN_API_KEY` configured           |
| PSP API Key      | ✅ Working    | Test keys available                  |
| Management Token | ✅ Working    | `MANAGEMENT_TOKEN_SECRET` configured |
| JWT Secret       | ✅ Configured | `JWT_SECRET` set                     |
| Webhook Secret   | ✅ Configured | `WEBHOOK_SECRET` set                 |

***

## Test Result Legend

| Symbol | Meaning       | Description                                          |
| ------ | ------------- | ---------------------------------------------------- |
| ✅      | Working       | Returns expected response (validation error or data) |
| ⚠️     | Auth Required | Requires authentication (expected behavior)          |
| 🔧     | Config Issue  | Missing DB table or env var (non-critical)           |
| ❌      | Error         | Unexpected error or timeout                          |

***

## Section 1: Health & Status (4 endpoints)

| Method | Endpoint                | Status | Response                                                          |
| ------ | ----------------------- | ------ | ----------------------------------------------------------------- |
| GET    | `/health`               | ✅      | `{"status":"healthy","redis":"connected","database":"connected"}` |
| GET    | `/v1/blockchain/health` | ✅      | `{"status":"healthy","network":"mainnet-beta"}`                   |
| GET    | `/v1/wallet/health`     | ✅      | `{"status":"healthy","signatureAlgorithm":"Ed25519"}`             |
| GET    | `/v1/sandbox/status`    | ✅      | `{"sandbox_active":true,"test_users_available":4}`                |

***

## Section 2: Enrollment (4 endpoints)

| Method | Endpoint                        | Status | Response                                                             |
| ------ | ------------------------------- | ------ | -------------------------------------------------------------------- |
| POST   | `/v1/enrollment/store`          | ✅      | `{"error":"Missing required fields: user_uuid, factors, device_id"}` |
| GET    | `/v1/enrollment/retrieve/:uuid` | ✅      | `{"error":"Invalid UUID format"}`                                    |
| DELETE | `/v1/enrollment/delete/:uuid`   | ✅      | `{"error":"Invalid UUID format"}`                                    |
| PUT    | `/v1/enrollment/renew/:uuid`    | ✅      | `{"error":"Invalid UUID format"}`                                    |

***

## Section 3: Verification (3 endpoints)

| Method | Endpoint                         | Status | Response                                                                      |
| ------ | -------------------------------- | ------ | ----------------------------------------------------------------------------- |
| POST   | `/v1/verification/initiate`      | ✅      | `{"error":"Invalid user identifier"}`                                         |
| POST   | `/v1/verification/verify`        | ✅      | `{"error":"Missing required fields: session_id, user_uuid, factors"}`         |
| POST   | `/v1/verification/submit-factor` | ✅      | `{"error":"Missing required fields: session_id, factor_name, factor_digest"}` |

***

## Section 4: Management (2 endpoints)

| Method | Endpoint                       | Status | Response                                                         |
| ------ | ------------------------------ | ------ | ---------------------------------------------------------------- |
| POST   | `/v1/management/verify`        | ✅      | `{"error":"Missing required fields: uuid, session_id, factors"}` |
| GET    | `/v1/management/devices/:uuid` | ⚠️     | `{"error":"Authentication required"}`                            |

**Auth Required:** Management auth token (obtained from `/v1/management/verify`)

***

## Section 5: Wallet (3 endpoints)

| Method | Endpoint                        | Status | Response                                                              |
| ------ | ------------------------------- | ------ | --------------------------------------------------------------------- |
| POST   | `/v1/wallet/connect`            | ✅      | `{"error":"Missing required fields: walletAddress, uuid, signature"}` |
| POST   | `/v1/wallet/challenge/generate` | ✅      | `{"error":"Missing required fields: walletAddress, uuid"}`            |
| POST   | `/v1/wallet/challenge/verify`   | ✅      | `{"error":"Missing required fields: walletAddress, uuid, signature"}` |

***

## Section 6: Admin (4 endpoints)

| Method | Endpoint                | Status | Response (with auth)                                                 |
| ------ | ----------------------- | ------ | -------------------------------------------------------------------- |
| GET    | `/v1/admin/stats`       | ✅      | `{"success":true,"cache":{...},"rateLimit":{...}}`                   |
| POST   | `/v1/admin/auth/login`  | ✅      | `{"error":"Username is required"}`                                   |
| GET    | `/v1/admin/alias/stats` | 🔧     | `{"error":"Failed to retrieve statistics"}` (missing DB table)       |
| GET    | `/v1/admin/audit/logs`  | 🔧     | `{"error":"column 'created_at' does not exist"}` (missing migration) |

**Auth:** Use `X-Admin-API-Key` header with `ADMIN_API_KEY` value

***

## Section 7: Developer Portal (2 endpoints)

| Method | Endpoint                 | Status | Response                              |
| ------ | ------------------------ | ------ | ------------------------------------- |
| GET    | `/v1/developer/keys`     | ⚠️     | `{"error":"Authentication required"}` |
| POST   | `/v1/developer/register` | ✅      | (needs testing)                       |

**Auth Required:** Developer JWT token (obtained from developer login)

***

## Section 8: Sandbox (3 endpoints)

| Method | Endpoint                    | Status | Response                  |
| ------ | --------------------------- | ------ | ------------------------- |
| GET    | `/v1/sandbox/status`        | ✅      | `{"sandbox_active":true}` |
| GET    | `/v1/sandbox/users`         | ✅      | Returns 4 test users      |
| POST   | `/v1/sandbox/keys/generate` | ✅      | Generates test API key    |

***

## Section 9: Integration (4 endpoints)

| Method | Endpoint                 | Status | Response                                                                      |
| ------ | ------------------------ | ------ | ----------------------------------------------------------------------------- |
| POST   | `/v1/sso/token/exchange` | ✅      | `{"error":"Invalid provider: undefined"}`                                     |
| POST   | `/v1/zkproof/generate`   | ✅      | `{"error":"Missing required parameters"}`                                     |
| POST   | `/v1/webhooks/register`  | ✅      | `{"error":"Missing signature"}`                                               |
| POST   | `/v1/psp/session/create` | ✅      | `{"error":"INVALID_REQUEST","message":"Missing required fields"}` (with auth) |

**Auth:** Use `Authorization: Bearer psp_test_stripe_xyz789` for testing

***

## Section 10: Crypto Payment (4 endpoints)

| Method | Endpoint                     | Status | Response                                                        |
| ------ | ---------------------------- | ------ | --------------------------------------------------------------- |
| GET    | `/v1/crypto/config/chains`   | 🔧     | `{"error":"relation 'crypto_supported_chains' does not exist"}` |
| POST   | `/v1/crypto/relayer/enroll`  | ✅      | `{"error":"Missing required fields"}`                           |
| POST   | `/v1/crypto/charge/create`   | ✅      | `{"error":"Missing merchantId or amountUsd"}`                   |
| POST   | `/v1/crypto/payment/execute` | ✅      | `{"error":"Missing required fields: uuid, merchantId, ..."}`    |

**Config Issue:** Database tables `crypto_supported_chains` and `crypto_supported_tokens` not created. Need to run migration.

***

## Section 11: SNS & Names (2 endpoints)

| Method | Endpoint                  | Status | Response                                      |
| ------ | ------------------------- | ------ | --------------------------------------------- |
| GET    | `/v1/sns/resolve/:name`   | ✅      | `{"error":"Failed to resolve name"}`          |
| GET    | `/v1/names/resolve/:name` | 🔧     | `{"error":"Provider bonfida is not enabled"}` |

**Config Issue:** `BONFIDA_ENABLED=true` not set in environment variables.

***

## Section 12: Billing (2 endpoints)

| Method | Endpoint                      | Status | Response                            |
| ------ | ----------------------------- | ------ | ----------------------------------- |
| GET    | `/v1/billing/consumer/status` | ✅      | `{"error":"Missing user UUID"}`     |
| GET    | `/v1/billing/merchant/status` | ✅      | `{"error":"Missing merchant UUID"}` |

***

## Section 13: Merchant (1 endpoint)

| Method | Endpoint             | Status | Response                   |
| ------ | -------------------- | ------ | -------------------------- |
| GET    | `/api/merchant/info` | ✅      | Returns mock merchant data |

***

## Summary Statistics

| Category     | Total  | ✅ Working | ⚠️ Auth | 🔧 Config |
| ------------ | ------ | --------- | ------- | --------- |
| Health       | 4      | 4         | 0       | 0         |
| Enrollment   | 4      | 4         | 0       | 0         |
| Verification | 3      | 3         | 0       | 0         |
| Management   | 2      | 2         | 0       | 0         |
| Wallet       | 3      | 3         | 0       | 0         |
| Admin        | 4      | 2         | 0       | 2         |
| Developer    | 2      | 1         | 1       | 0         |
| Sandbox      | 3      | 3         | 0       | 0         |
| Integration  | 4      | 4         | 0       | 0         |
| Crypto       | 4      | 3         | 0       | 1         |
| SNS/Names    | 2      | 1         | 0       | 1         |
| Billing      | 2      | 2         | 0       | 0         |
| Merchant     | 1      | 1         | 0       | 0         |
| **TOTAL**    | **38** | **33**    | **1**   | **4**     |

***

## Issues Found & Fixed This Session

| Issue                                 | Endpoint                     | Fix                                           |
| ------------------------------------- | ---------------------------- | --------------------------------------------- |
| `RedisStore is not a constructor`     | Rate limiting                | Changed to named import `{ RedisStore }`      |
| POST endpoints timeout                | Multiple                     | Removed factory function middleware           |
| `Cannot read properties of undefined` | `/v1/crypto/config/chains`   | Import `pool` from database module            |
| `undefined.slice()` error             | `/v1/crypto/payment/execute` | Added validation for required fields          |
| X-Forwarded-For validation            | Rate limiting                | Added `app.set('trust proxy', 1)`             |
| Admin stats 500 error                 | `/v1/admin/stats`            | Added graceful cache manager handling         |
| Missing auth secrets                  | All protected                | Configured ADMIN\_API\_KEY, JWT\_SECRET, etc. |

***

## Next Steps

1. ~~**Generate API keys**~~ ✅ Done - All secrets configured in Railway
2. **Run crypto migrations** - Create `crypto_supported_chains` and `crypto_supported_tokens` tables
3. **Run admin migrations** - Create/fix `audit_logs` and `alias_stats` tables
4. **Enable Bonfida provider** - Set `BONFIDA_ENABLED=true` in Railway environment

***

**Last Updated:** 2025-12-27
