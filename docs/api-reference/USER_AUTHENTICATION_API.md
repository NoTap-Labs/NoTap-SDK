# NoTap User Authentication API

This document describes the JWT-based authentication API for all NoTap user types.

**Last Updated:** 2025-12-27

---

## Overview: 4 User Types

| User Type | Base Path | Token Type | MFA Required | OAuth Supported |
|-----------|-----------|------------|--------------|-----------------|
| **Developer** | `/v1/auth/developer` | `type: 'developer'` | No | Yes (Google, GitHub) |
| **Regular User** | `/v1/auth/user` | `type: 'user'` | Optional | Yes (Google, GitHub) |
| **Merchant** | `/v1/auth/merchant` | `type: 'merchant'` | Optional | Yes (Google, GitHub) |
| **Admin** | `/v1/auth/admin` | `type: 'admin'` | **Yes (NoTap)** | No |

---

## Common Response Format

### Success Response
```json
{
  "success": true,
  "user": {
    "id": "user-id-uuid",
    "email": "user@example.com",
    "fullName": "John Doe"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": "7d"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error message description"
}
```

---

## 1. Developer User Authentication

**Base Path:** `/v1/auth/developer`

**Database Table:** `developers`

**JWT Payload:**
```json
{
  "developerId": "dev-uuid",
  "email": "dev@example.com",
  "name": "Developer Name",
  "type": "developer",
  "iat": 1703123456,
  "exp": 1703727456
}
```

### POST `/v1/auth/developer/register`

Register a new developer account.

**Request:**
```json
{
  "email": "dev@example.com",
  "password": "SecurePass123",
  "fullName": "John Developer",
  "company": "Acme Inc"
}
```

**Validation:**
- Email: Valid email format
- Password: Minimum 8 characters
- Full name: Required
- Company: Optional

**Response (201):**
```json
{
  "success": true,
  "developer": {
    "id": "dev-abc-123",
    "email": "dev@example.com",
    "fullName": "John Developer",
    "company": "Acme Inc",
    "createdAt": "2025-12-27T12:00:00Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d",
  "message": "Developer account created successfully"
}
```

### POST `/v1/auth/developer/login`

Login with email and password.

**Request:**
```json
{
  "email": "dev@example.com",
  "password": "SecurePass123"
}
```

**Response (200):**
```json
{
  "success": true,
  "developer": {
    "id": "dev-abc-123",
    "email": "dev@example.com",
    "fullName": "John Developer"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d"
}
```

### GET `/v1/auth/developer/profile`

Get developer profile (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200):**
```json
{
  "success": true,
  "developer": {
    "id": "dev-abc-123",
    "email": "dev@example.com",
    "fullName": "John Developer",
    "company": "Acme Inc",
    "apiKeysCount": 3,
    "createdAt": "2025-12-27T12:00:00Z",
    "lastLoginAt": "2025-12-27T14:30:00Z"
  }
}
```

### POST `/v1/auth/developer/validate`

Validate JWT token (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200):**
```json
{
  "success": true,
  "valid": true,
  "developer": {
    "id": "dev-abc-123",
    "email": "dev@example.com",
    "fullName": "John Developer"
  }
}
```

---

## 2. Regular User Authentication

**Base Path:** `/v1/auth/user`

**Database Table:** `regular_users`

**JWT Payload:**
```json
{
  "userId": "user-uuid",
  "email": "user@example.com",
  "name": "User Name",
  "type": "user",
  "iat": 1703123456,
  "exp": 1703727456
}
```

### POST `/v1/auth/user/register`

Register a new regular user account.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "MyPassword123",
  "fullName": "Jane User",
  "phoneNumber": "+1234567890"
}
```

**Validation:**
- Email: Valid email format
- Password: Minimum 8 characters
- Full name: Required
- Phone number: Optional

**Response (201):**
```json
{
  "success": true,
  "user": {
    "id": "user-xyz-789",
    "email": "user@example.com",
    "fullName": "Jane User",
    "createdAt": "2025-12-27T12:00:00Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d",
  "message": "Account created successfully. Please verify your email."
}
```

### POST `/v1/auth/user/login`

Login with email and password.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "MyPassword123"
}
```

**Response (200):**
```json
{
  "success": true,
  "user": {
    "id": "user-xyz-789",
    "email": "user@example.com",
    "fullName": "Jane User"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d"
}
```

### POST `/v1/auth/user/verify-email`

Verify email address with token.

**Request:**
```json
{
  "token": "email-verification-token-hex"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Email verified successfully"
}
```

### POST `/v1/auth/user/request-reset`

Request password reset email.

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Password reset email sent"
}
```

### POST `/v1/auth/user/reset-password`

Reset password with token.

**Request:**
```json
{
  "token": "password-reset-token-hex",
  "newPassword": "NewSecurePass123"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Password reset successfully"
}
```

### POST `/v1/auth/user/oauth/callback`

OAuth login/registration (Google, GitHub).

**Request:**
```json
{
  "provider": "google",
  "oauthId": "oauth-provider-user-id",
  "email": "user@gmail.com",
  "fullName": "Jane Doe"
}
```

**Supported Providers:** `google`, `github`

**Response (200):**
```json
{
  "success": true,
  "user": {
    "id": "user-xyz-789",
    "email": "user@gmail.com",
    "fullName": "Jane Doe"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d",
  "isNewUser": true
}
```

### POST `/v1/auth/user/notap/link`

Link NoTap enrollment to user account (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request:**
```json
{
  "notapUuid": "notap-enrollment-uuid"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "NoTap account linked successfully"
}
```

### POST `/v1/auth/user/notap/login`

Login with NoTap passwordless authentication.

**Request:**
```json
{
  "notapUuid": "notap-enrollment-uuid",
  "verificationResult": {
    "success": true,
    "uuid": "notap-enrollment-uuid",
    "verified": true
  }
}
```

**Response (200):**
```json
{
  "success": true,
  "user": {
    "id": "user-xyz-789",
    "email": "user@example.com",
    "fullName": "Jane User"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d"
}
```

### GET `/v1/auth/user/profile`

Get user profile (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200):**
```json
{
  "success": true,
  "user": {
    "id": "user-xyz-789",
    "email": "user@example.com",
    "fullName": "Jane User",
    "phoneNumber": "+1234567890",
    "emailVerified": true,
    "notapEnabled": true,
    "createdAt": "2025-12-27T12:00:00Z",
    "lastLoginAt": "2025-12-27T14:30:00Z"
  }
}
```

### PUT `/v1/auth/user/profile`

Update user profile (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request:**
```json
{
  "fullName": "Jane Updated User",
  "phoneNumber": "+9876543210"
}
```

**Allowed Fields:** `fullName`, `phoneNumber`

**Response (200):**
```json
{
  "success": true,
  "user": {
    "id": "user-xyz-789",
    "email": "user@example.com",
    "fullName": "Jane Updated User",
    "phoneNumber": "+9876543210"
  },
  "message": "Profile updated successfully"
}
```

### POST `/v1/auth/user/validate`

Validate JWT token (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200):**
```json
{
  "success": true,
  "valid": true,
  "user": {
    "id": "user-xyz-789",
    "email": "user@example.com",
    "fullName": "Jane User"
  }
}
```

### POST `/v1/auth/user/logout`

Logout user (client-side token deletion).

**Response (200):**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## 3. Merchant User Authentication

**Base Path:** `/v1/auth/merchant`

**Database Table:** `merchant_users`

**JWT Payload:**
```json
{
  "merchantId": "merchant-uuid",
  "email": "merchant@business.com",
  "businessName": "Acme Business LLC",
  "type": "merchant",
  "iat": 1703123456,
  "exp": 1703727456
}
```

### POST `/v1/auth/merchant/register`

Register a new merchant account.

**Request:**
```json
{
  "email": "merchant@business.com",
  "password": "SecureMerchant123",
  "fullName": "Bob Merchant",
  "businessName": "Acme Business LLC",
  "businessType": "retail"
}
```

**Validation:**
- Email: Valid email format
- Password: Minimum 8 characters
- Full name: Required
- Business name: Required
- Business type: Optional (retail, restaurant, ecommerce, etc.)

**Response (201):**
```json
{
  "success": true,
  "merchant": {
    "id": "merchant-abc-456",
    "email": "merchant@business.com",
    "businessName": "Acme Business LLC",
    "verificationStatus": "pending",
    "createdAt": "2025-12-27T12:00:00Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d",
  "message": "Merchant account created. Please verify your email and submit business documentation."
}
```

### POST `/v1/auth/merchant/login`

Login with email and password.

**Request:**
```json
{
  "email": "merchant@business.com",
  "password": "SecureMerchant123"
}
```

**Response (200):**
```json
{
  "success": true,
  "merchant": {
    "id": "merchant-abc-456",
    "email": "merchant@business.com",
    "businessName": "Acme Business LLC",
    "verificationStatus": "verified"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d"
}
```

### GET `/v1/auth/merchant/profile`

Get merchant profile (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200):**
```json
{
  "success": true,
  "merchant": {
    "id": "merchant-abc-456",
    "email": "merchant@business.com",
    "businessName": "Acme Business LLC",
    "businessType": "retail",
    "businessDescription": "Leading retail store",
    "ownerFullName": "Bob Merchant",
    "businessPhone": "+1234567890",
    "verificationStatus": "verified",
    "defaultCurrency": "USD",
    "acceptsCrypto": true,
    "acceptsFiat": true,
    "createdAt": "2025-12-27T12:00:00Z",
    "lastLoginAt": "2025-12-27T14:30:00Z"
  }
}
```

### PUT `/v1/auth/merchant/profile`

Update merchant profile (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request:**
```json
{
  "businessName": "Acme Business LLC Updated",
  "businessDescription": "Updated description",
  "businessPhone": "+9876543210",
  "defaultCurrency": "EUR"
}
```

**Allowed Fields:** `businessName`, `businessType`, `businessDescription`, `businessPhone`, `ownerFullName`, `defaultCurrency`

**Response (200):**
```json
{
  "success": true,
  "merchant": {
    "id": "merchant-abc-456",
    "email": "merchant@business.com",
    "businessName": "Acme Business LLC Updated",
    "businessType": "retail"
  },
  "message": "Profile updated successfully"
}
```

### POST `/v1/auth/merchant/validate`

Validate JWT token (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200):**
```json
{
  "success": true,
  "valid": true,
  "merchant": {
    "id": "merchant-abc-456",
    "email": "merchant@business.com",
    "businessName": "Acme Business LLC"
  }
}
```

### POST `/v1/auth/merchant/logout`

Logout merchant (client-side token deletion).

**Response (200):**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## 4. Admin User Authentication

**Base Path:** `/v1/auth/admin`

**Database Table:** `admin_users`

**JWT Payload:**
```json
{
  "adminId": "admin-uuid",
  "email": "admin@notap.io",
  "fullName": "Alice Admin",
  "role": "admin",
  "type": "admin",
  "iat": 1703123456,
  "exp": 1703727456
}
```

**Security Requirements:**
- **MFA Required:** NoTap enrollment mandatory for all admin accounts
- **Password Minimum:** 12 characters (stricter than other user types)
- **No OAuth:** Only email/password + MFA authentication
- **Failed Login Tracking:** Account locks after 5 failed attempts for 30 minutes
- **Password Expiration:** 90 days (enforced via database trigger)

### POST `/v1/auth/admin/register`

Register a new admin account (MFA required).

**Request:**
```json
{
  "email": "admin@notap.io",
  "password": "VerySecureAdmin1234",
  "fullName": "Alice Admin",
  "notapUuid": "notap-enrollment-uuid",
  "role": "admin"
}
```

**Validation:**
- Email: Valid email format
- Password: **Minimum 12 characters** (stricter than other user types)
- Full name: Required
- NoTap UUID: **Required** (MFA enforcement)
- Role: Optional (default: 'admin', options: 'admin', 'superadmin', 'auditor')

**Response (201):**
```json
{
  "success": true,
  "admin": {
    "id": "admin-def-789",
    "email": "admin@notap.io",
    "fullName": "Alice Admin",
    "role": "admin",
    "mfaEnabled": true,
    "createdAt": "2025-12-27T12:00:00Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d",
  "message": "Admin account created with MFA enabled"
}
```

### POST `/v1/auth/admin/login`

Login with email, password, and MFA verification.

**Request:**
```json
{
  "email": "admin@notap.io",
  "password": "VerySecureAdmin1234",
  "mfaVerification": {
    "success": true,
    "uuid": "notap-enrollment-uuid",
    "verified": true
  }
}
```

**Security:**
- Password must be correct
- MFA verification must succeed
- Account must not be locked (failed attempts)
- Account must be active and not suspended

**Response (200):**
```json
{
  "success": true,
  "admin": {
    "id": "admin-def-789",
    "email": "admin@notap.io",
    "fullName": "Alice Admin",
    "role": "admin"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": "7d"
}
```

**Error Response (401) - Failed Login:**
```json
{
  "success": false,
  "error": "Invalid credentials"
}
```

**Error Response (401) - Account Locked:**
```json
{
  "success": false,
  "error": "Account is temporarily locked due to failed login attempts"
}
```

**Error Response (400) - MFA Required:**
```json
{
  "success": false,
  "error": "MFA verification required for admin access"
}
```

### GET `/v1/auth/admin/profile`

Get admin profile (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200):**
```json
{
  "success": true,
  "admin": {
    "id": "admin-def-789",
    "email": "admin@notap.io",
    "fullName": "Alice Admin",
    "jobTitle": "System Administrator",
    "department": "IT Security",
    "role": "admin",
    "mfaEnabled": true,
    "lastPasswordChange": "2025-10-15T08:00:00Z",
    "createdAt": "2025-01-01T12:00:00Z",
    "lastLoginAt": "2025-12-27T14:30:00Z"
  }
}
```

### POST `/v1/auth/admin/validate`

Validate JWT token (authenticated).

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200):**
```json
{
  "success": true,
  "valid": true,
  "admin": {
    "id": "admin-def-789",
    "email": "admin@notap.io",
    "fullName": "Alice Admin",
    "role": "admin"
  }
}
```

---

## Token Type Separation (Security)

**JWT tokens are user-type specific and cannot be used interchangeably.**

### Middleware Validation

Each endpoint validates the `type` field in the JWT payload:

- Developer endpoints require `type: 'developer'`
- Regular user endpoints require `type: 'user'`
- Merchant endpoints require `type: 'merchant'`
- Admin endpoints require `type: 'admin'`

### Example: Invalid Token Type Error

**Request:**
```bash
# Attempting to access merchant endpoint with regular user token
curl -X GET https://api.notap.io/v1/auth/merchant/profile \
  -H "Authorization: Bearer <user-jwt-token>"
```

**Response (403):**
```json
{
  "success": false,
  "error": "Invalid token type. Merchant token required."
}
```

---

## Error Codes

| Status | Error | Description |
|--------|-------|-------------|
| 400 | Validation error | Missing or invalid request fields |
| 401 | Invalid credentials | Email/password incorrect |
| 401 | Authentication required | No token provided |
| 401 | Token expired | JWT token has expired |
| 403 | Invalid token type | Token type doesn't match endpoint |
| 403 | Account suspended | User account is suspended |
| 403 | MFA required | Admin login requires MFA |
| 404 | User not found | User doesn't exist |
| 409 | Email already registered | Duplicate email |
| 500 | Server error | Internal server error |

---

## Environment Variables

```bash
# JWT Configuration
JWT_SECRET=<generate with: openssl rand -hex 32>
JWT_EXPIRY=7d  # Token lifetime (default: 7 days)

# Password Reset
PASSWORD_RESET_EXPIRY_HOURS=1  # Reset token expiry (default: 1 hour)

# Email Verification
EMAIL_VERIFICATION_EXPIRY_HOURS=24  # Email token expiry (default: 24 hours)

# Admin Security
ADMIN_PASSWORD_EXPIRY_DAYS=90  # Password expiration (default: 90 days)
ADMIN_FAILED_LOGIN_LOCK_MINUTES=30  # Account lock duration (default: 30 minutes)
```

---

## Rate Limiting

All authentication endpoints are rate-limited using `userRateLimiter`:

- **Limit:** 10 requests per minute per IP
- **Window:** 60 seconds (rolling)
- **Storage:** Redis

**Rate Limit Headers:**
```
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 7
X-RateLimit-Reset: 1703123500
```

**Rate Limit Exceeded Response (429):**
```json
{
  "success": false,
  "error": "Too many requests. Please try again later."
}
```

---

## Testing

### Run Comprehensive Test Suite

```bash
bash /tmp/test-all-user-types.sh
```

### Test Individual User Types

**Regular User:**
```bash
# Register
curl -X POST https://api.notap.io/v1/auth/user/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234","fullName":"Test User"}'

# Login
curl -X POST https://api.notap.io/v1/auth/user/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234"}'

# Get Profile
curl -X GET https://api.notap.io/v1/auth/user/profile \
  -H "Authorization: Bearer <jwt-token>"
```

**Merchant User:**
```bash
# Register
curl -X POST https://api.notap.io/v1/auth/merchant/register \
  -H "Content-Type: application/json" \
  -d '{"email":"merchant@business.com","password":"Merchant123","fullName":"Bob Merchant","businessName":"Acme LLC","businessType":"retail"}'

# Login
curl -X POST https://api.notap.io/v1/auth/merchant/login \
  -H "Content-Type: application/json" \
  -d '{"email":"merchant@business.com","password":"Merchant123"}'
```

**Admin User:**
```bash
# Register (requires NoTap UUID)
curl -X POST https://api.notap.io/v1/auth/admin/register \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@notap.io","password":"AdminSecure1234","fullName":"Alice Admin","notapUuid":"notap-uuid","role":"admin"}'

# Login (requires MFA)
curl -X POST https://api.notap.io/v1/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@notap.io","password":"AdminSecure1234","mfaVerification":{"success":true,"uuid":"notap-uuid","verified":true}}'
```

---

## Database Migrations Required

Before using these endpoints, run migrations 014-017:

```bash
# In Windows Command Prompt (NOT WSL)
cd C:\Users\USUARIO\StudioProjects\zero-pay-sdk\zeropay-android\backend

# Run all migrations
railway run bash run-migrations.sh
```

**Migrations:**
- 014: Create `regular_users` table
- 015: Create `merchant_users` table
- 016: Create `admin_users` table
- 017: Add `severity` column to `audit_log` table

---

## Security Best Practices

1. **Password Storage:** All passwords hashed with bcrypt (12 rounds)
2. **Token Storage:** JWTs signed with HMAC-SHA256
3. **Failed Login Tracking:** Admin accounts lock after 5 failed attempts
4. **MFA Enforcement:** Admin accounts require NoTap enrollment
5. **Audit Logging:** All authentication events logged with severity levels
6. **Rate Limiting:** 10 requests/minute per IP
7. **Token Type Separation:** Tokens cannot be used across user types
8. **Constant-Time Comparison:** Password verification uses constant-time operations

---

**Last Updated:** 2025-12-27
