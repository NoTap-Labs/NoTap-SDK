# Backend Service Map

> Complete inventory of backend routers, services, middleware, jobs, crypto modules, and utilities.
> Use this as a developer reference for navigating the backend codebase.

**Last updated:** 2026-03-21 | **Backend version:** v3.22.0

---

## Routers (48 files)

Routes are mounted in `server.js`. Grouped by domain.

### Core User Flows

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `enrollmentRouter.js` | `/v1/enrollment` | User enrollment (5-step wizard, factor digests) |
| `verificationRouter.js` | `/v1/verification` | Payment verification (factor matching, PSP sessions) |
| `managementRouter.js` | `/v1/management` | User profile management (view/delete enrollment) |
| `managementDevicesRouter.js` | `/v1/management/devices` | Device management for enrolled users |
| `recoveryRouter.js` | `/v1/recovery` | Account recovery flows |

### Authentication

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `regularAuthRouter.js` | `/v1/auth/user` | End-user authentication (login/register/refresh) |
| `merchantAuthRouter.js` | `/v1/auth/merchant` | Merchant authentication |
| `developerAuthRouter.js` | `/v1/auth/developer` | Developer portal authentication |
| `adminAuthRouter.js` | `/v1/auth/admin` | Admin panel authentication |
| `ssoRouter.js` | `/v1/sso` | SSO integration (OAuth/OIDC) |
| `sessionKeyRouter.js` | — | Session key management |

### Admin Panel

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `adminRouter.js` | `/v1/admin` | Core admin operations |
| `adminUserManagementRouter.js` | `/v1/admin` | Admin user CRUD operations |
| `adminManagementRouter.js` | `/v1/admin/admins` | Admin self-management (factory: `createAdminManagementRouter`) |
| `adminTeamRouter.js` | `/v1/admin/teams` | Admin team RBAC (factory: `createAdminTeamRouter`) |
| `adminConfigurationRouter.js` | `/v1/admin` | System configuration management |
| `adminAnalyticsRouter.js` | `/v1/admin/analytics` | Analytics dashboard data |
| `adminAuditRouter.js` | `/v1/admin/audit` | Audit log viewer |
| `adminSecurityMonitoringRouter.js` | `/v1/admin/security` | Security monitoring dashboard |
| `adminBillingRouter.js` | `/v1/admin/billing` | Admin billing management |
| `adminAliasRouter.js` | `/v1/admin/alias` | Alias management for admin |
| `adminUserRouter.js` | `/api/admin-users` | Legacy admin user endpoints |

### Merchant

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `merchantRouter.js` | `/api/merchant` | Merchant API (enrollment, verification via API keys) |
| `merchantDashboardRouter.js` | `/api/merchant/dashboard` | Merchant dashboard data |
| `merchantBillingRouter.js` | `/v1/merchant/billing` | Merchant billing/usage |

### Developer Portal

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `developerPortalRouter.js` | `/v1/developer` | Developer portal (API keys, docs, sandbox) |
| `webhooksRouter.js` | `/v1/developer/webhooks-mgmt` | Webhook management for developers |

### Payments & Billing

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `paymentRouter.js` | — | Payment processing coordination |
| `cryptoPaymentRouter.js` | `/v1/crypto` | Cryptocurrency payment flows |
| `pspRouter.js` | `/v1/psp` | PSP integration endpoints |
| `unifiedBillingRouter.js` | `/v1/billing` | Unified billing across user types |
| `consumerBillingRouter.js` | `/v1/consumer/billing` | Consumer billing endpoints |
| `stripeWebhookHandler.js` | — | Stripe webhook processor |
| `unifiedWebhookHandler.js` | `/v1/webhooks` | Unified webhook ingestion |

### Blockchain & Web3

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `blockchainRouter.js` | `/v1/blockchain` | Blockchain identity operations |
| `walletRouter.js` | `/v1/wallet` | Wallet signature verification |
| `snsRouter.js` | `/v1/sns` | Solana Name Service integration |
| `namesRouter.js` | `/v1/names` | Human-readable name resolution |
| `didRouter.js` | — | Decentralized identifier (DID) operations |

### Agentic Payments (v3.21.0+)

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `agentRegistrationRouter.js` | `/v1/developer/agents` | Agent registration and management |
| `agentAuthRouter.js` | `/v1/agent/auth` | Agent authentication flow (initiate/verify/cancel) |

### Security & Compliance

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `bipaConsentRouter.js` | `/v1/bipa` | BIPA biometric consent management |
| `zkProofRouter.js` | `/v1/zkproof` | Zero-knowledge proof generation/verification |
| `sealRouter.js` | `/v1/seal` | Cryptographic seal assembly and verification |

### Partners & Demo

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `partnerRouter.js` | `/v1/partners` | PSP partner management |
| `demoAnalyticsRouter.js` | `/v1/demo-analytics` | Demo/analytics endpoints |

### Development Only

| Router | Mount Path | Description |
|--------|-----------|-------------|
| `sandboxRouter.js` | `/v1/sandbox` | Sandbox testing (dev/staging only) |
| `testProfileRouter.js` | `/v1/test-profiles` | Test profile generation (dev/staging only) |

---

## Services (60+ files)

### Core Business Logic

| Service | Description |
|---------|-------------|
| `ServiceFactory.js` | Provider-agnostic factory — switches cache/DB backends via env |
| `ConfigService.js` | System configuration management |
| `ConfigurationService.js` | Feature flags and runtime configuration |
| `FeatureService.js` | Feature toggle service |

### User & Auth

| Service | Description |
|---------|-------------|
| `AdminAuthService.js` | Admin authentication (login, password, MFA) |
| `AdminSessionService.js` | Admin session management |
| `AdminUserService.js` | Admin user CRUD |
| `DeveloperUserService.js` | Developer account management |
| `MerchantUserService.js` | Merchant account management |
| `RegularUserService.js` | End-user account management |
| `UserManagementService.js` | Cross-type user operations |
| `TokenBlacklistService.js` | JWT token revocation |
| `SessionKeyManager.js` | Session key lifecycle |
| `SessionKeyRotationService.js` | Automatic session key rotation |
| `RecoveryCodeService.js` | Account recovery code generation/validation |

### Admin

| Service | Description |
|---------|-------------|
| `AdminAnalyticsService.js` | Analytics aggregation for admin dashboard |
| `MerchantAnalyticsService.js` | Merchant-specific analytics |
| `PermissionService.js` | RBAC permission management |
| `TeamService.js` | Admin team management |

### API & Usage

| Service | Description |
|---------|-------------|
| `APIKeyService.js` | API key generation, validation, rotation |
| `UsageTrackingService.js` | API usage metering and logging |

### Security & Audit

| Service | Description |
|---------|-------------|
| `AuditService.js` | Synchronous audit logging |
| `AsyncAuditService.js` | Non-blocking audit log writes |
| `SecurityMonitoringService.js` | Threat detection and security alerts |
| `jurisdictionService.js` | BIPA/GDPR jurisdiction checking |

### Payments & Billing

| Service | Description |
|---------|-------------|
| `pspSessionService.js` | Parallel PSP session creation (Stripe, Tilopay, Adyen, etc.) |
| `PaymentProvider.js` | Abstract payment provider interface |
| `PaymentProviderFactory.js` | PSP provider instantiation |
| `StripeProvider.js` | Stripe-specific implementation |
| `MoonPayProvider.js` | MoonPay crypto on-ramp provider |

### Blockchain & Web3

| Service | Description |
|---------|-------------|
| `solanaService.js` | Solana blockchain operations |
| `blockchainIntegrationService.js` | Multi-chain blockchain integration |
| `snsIntegrationService.js` | Solana Name Service integration |
| `snsValidationService.js` | SNS name validation |
| `walletSignatureService.js` | Wallet signature verification |
| `didService.js` | DID document management |
| `aliasGenerator.js` | Human-readable alias generation |

### Crypto Payments

| Service | Description |
|---------|-------------|
| `CryptoConfigService.js` | Crypto payment configuration |
| `CryptoRelayerService.js` | Crypto transaction relaying |
| `CryptoRelayerRotationService.js` | Relayer key rotation |

### Webhooks

| Service | Description |
|---------|-------------|
| `WebhookService.js` | Webhook registration and dispatch |
| `WebhookDeliveryWorker.js` | Async webhook delivery with retry |

### Agentic (v3.21.0+)

| Service | Description |
|---------|-------------|
| `AgentAuthService.js` | Agent authentication session management |
| `AgentCallbackService.js` | Agent callback delivery with retry |
| `AgentRegistrationService.js` | Agent registration and credential management |

### Seal & ZK

| Service | Description |
|---------|-------------|
| `sealAssemblerService.js` | Cryptographic seal assembly |
| `sealPdfService.js` | PDF seal generation |
| `sealStorageService.js` | Seal storage management |
| `zkProofService.js` | ZK-SNARK proof generation and verification |

### Data & Cache (subdirectories)

| Path | Description |
|------|-------------|
| `services/cache/` | Cache provider abstraction (Redis, hybrid, memory) |
| `services/database/` | Database provider abstraction (PostgreSQL, read replicas) |
| `services/blockchain/` | Blockchain provider modules |
| `services/nameService/` | Name resolution service |
| `services/crypto/` | Crypto-specific service modules |

### Testing & Sandbox

| Service | Description |
|---------|-------------|
| `SandboxDataGenerator.js` | Test data generation for sandbox mode |
| `testProfileService.js` | Test profile management |
| `ManagementDeviceService.js` | Device management operations |
| `AnalyticsService.js` | General analytics service |

---

## Middleware (31 files)

### Authentication

| Middleware | Description |
|-----------|-------------|
| `authMiddleware.js` | Core user authentication (JWT validation) |
| `adminAuth.js` | Admin authentication and session validation |
| `merchantAuth.js` | Merchant API authentication |
| `developerAuth.js` | Developer portal authentication |
| `billingAuth.js` | Billing endpoint authentication |
| `userAuth.js` | User-level authentication |
| `agentAuth.js` | Agent authentication (agentic payments) |
| `apiKeyValidator.js` | API key validation + partner masquerade |
| `requirePermission.js` | RBAC permission checking (DI via `initializePool`) |
| `testProfileMiddleware.js` | Test profile authentication bypass (dev only) |

### Rate Limiting

| Middleware | Description |
|-----------|-------------|
| `rateLimiter.js` | General rate limiting |
| `rateLimitMiddleware.js` | Rate limit middleware factory |
| `adminRateLimiter.js` | Admin-specific rate limits |
| `developerRateLimiter.js` | Developer portal rate limits |
| `managementRateLimiter.js` | Management endpoint rate limits |
| `merchantDashboardRateLimiter.js` | Merchant dashboard rate limits |
| `ssoRateLimiter.js` | SSO endpoint rate limits |
| `auditRateLimiter.js` | Audit endpoint rate limits |
| `passwordResetLimiter.js` | Password reset rate limits |
| `consumerQuotaEnforcer.js` | Consumer usage quota enforcement |
| `merchantQuotaEnforcer.js` | Merchant usage quota enforcement |

### Security

| Middleware | Description |
|-----------|-------------|
| `security.js` | Helmet, CORS, content security headers |
| `nonceValidator.js` | Request nonce validation |
| `replayProtection.js` | Replay attack prevention (nonce + timestamp) |
| `ssrfProtection.js` | SSRF URL validation |
| `oauthStateProtection.js` | OAuth state parameter CSRF protection |

### Infrastructure

| Middleware | Description |
|-----------|-------------|
| `sessionManager.js` | Session lifecycle management |
| `cacheManager.js` | Cache middleware (response caching) |
| `distributedLock.js` | Distributed locking for concurrent operations |
| `featureGate.js` | Feature flag gating |
| `adminActivityLogger.js` | Admin action audit logging (uses `req.app.locals.db`) |

---

## Jobs (2 files)

| Job | Description |
|-----|-------------|
| `dataRetentionCleanup.js` | GDPR data retention enforcement — scheduled cleanup of expired data |
| `scheduler.js` | Job scheduler for recurring tasks |

---

## Crypto (7 files)

| Module | Description |
|--------|-------------|
| `doubleLayerCrypto.js` | Double-layer encryption (PBKDF2 + KMS) |
| `encryption.js` | AES-256-GCM encryption/decryption |
| `keyDerivation.js` | PBKDF2/HKDF key derivation |
| `keyManagement.js` | Key lifecycle management |
| `kmsProvider.js` | AWS KMS integration |
| `memoryWipe.js` | Secure memory wiping (`wipeBuffer`) |
| `sealSigningService.js` | Cryptographic seal signing |

---

## Utils (6 files)

| Utility | Description |
|---------|-------------|
| `logger.js` | Structured logging with PII auto-redaction |
| `safeLogger.js` | Safe logging wrapper |
| `constantTimeCompare.js` | Constant-time comparison for secrets/digests |
| `dummyHash.js` | Dummy hash generation (timing attack prevention) |
| `privacyUtils.js` | Privacy utilities (`hashDeviceId`, `anonymizeIP`, `isValidUUID`) |
| `startupValidator.js` | Boot-time environment validation |

---

## Dependency Injection Patterns

Three DI patterns are used (all valid, each suited to different contexts):

| Pattern | Where Used | When to Use |
|---------|-----------|-------------|
| **Factory functions** | `createAdminTeamRouter(pool)`, `createAdminManagementRouter(pool)` | New routers that need explicit pool injection for unit testing |
| **`req.app.locals.db`** | Most routers (11+) | Standard approach — pool set once in server.js, available everywhere |
| **`initializePool(db)`** | `requirePermission.js` middleware | Middleware that runs outside Express request context |

The centralized pool is set in `server.js`:
```javascript
app.locals.db = database.pool;
```

**Shared middleware:** Admin API-key auth is centralized in `middleware/adminAuth.js` (`requireAdminAuth`). All admin routers import from this single source.

---

## Adding a New Router

1. Create `backend/routes/myRouter.js`
2. Mount in `server.js`: `app.use('/v1/my-route', middleware..., myRouter)`
3. Add to this service map
4. Document API endpoints in `backend/docs/`
5. Complete compliance gate (see CLAUDE.md)
