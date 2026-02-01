# NoTap Partner Integration Guide (PSP Command Center)

**For Payment Service Providers (PSPs), Aggregators, and Platforms.**

---

## 1. Overview

The **Partner Command Center** allows you to programmatically onboard, manage, and bill your sub-merchants. Instead of asking your merchants to sign up for NoTap manually, you can provision their accounts via API, manage their keys, and receive a single aggregated invoice.

### Key Features
*   **Headless Onboarding:** Create merchant accounts via API (`POST /merchants`).
*   **Masquerading:** Execute API calls *on behalf of* your merchants using your Master Key.
*   **Aggregated Billing:** Receive one monthly invoice for all your merchants' usage.
*   **Global Webhooks:** Receive verification events for *all* your merchants at one URL.

---

## 2. Authentication

You will be issued a **Partner Master Key** (`pk_live_...`). This key grants you administrative access to all merchants linked to your Partner ID.

### The "On-Behalf-Of" Header
To perform actions for a specific merchant (e.g., verifying a user), you must include the `X-Merchant-ID` header.

```http
Authorization: Bearer pk_live_abc123...
X-Merchant-ID: 7a8b9c... (Target Sub-Merchant UUID)
```

**Security Note:**
*   You do **not** need the merchant's private API key (`sk_live_...`).
*   Your Master Key (`pk_live_...`) verifies your identity.
*   The `X-Merchant-ID` ensures the action is scoped to the correct sub-merchant.

---

## 3. Onboarding Flow (Headless)

When a merchant signs up on *your* platform, you should automatically create a NoTap account for them.

### Step 1: Create Merchant
**Endpoint:** `POST /v1/partners/merchants`

**Request:**
```json
{
  "name": "Coffee Shop #1",
  "email": "admin@coffee1.com",
  "webhook_url": "https://api.your-platform.com/webhooks/notap"
}
```

**Response:**
```json
{
  "success": true,
  "merchant_id": "7a8b9c-...",
  "api_key": "sk_live_xyz...",  // (Optional) Give to merchant if they need direct access
  "dashboard_url": "https://dashboard.notap.io/login?partner_sso=..."
}
```

### Step 2: Store Credentials
Save the `merchant_id` in your database. You will need this for the `X-Merchant-ID` header.

---

## 4. Verification Flow (Masquerade)

When a user pays at one of your merchants, you initiate the verification using your Master Key.

### Initiate Verification
**Endpoint:** `POST /v1/verification/initiate`

**Headers:**
```http
Authorization: Bearer pk_live_YOUR_MASTER_KEY
X-Merchant-ID: SUB_MERCHANT_UUID
```

**Body:**
```json
{
  "user_uuid": "alice.notap.sol",
  "transaction_amount": 15.00
}
```

**Result:**
The API behaves exactly as if the merchant had called it directly. The usage is recorded against the merchant's quota but billed to your Partner Aggregated Invoice.

---

## 5. Billing & Invoices

> **Detailed Pricing:** See [Partner Pricing Model](../08-business/PARTNER_PRICING_MODEL.md) for tiers and margins.

*   **Aggregated Invoice:** On the 1st of every month, you receive a CSV breakdown of usage by `merchant_id`.
*   **Pricing:** You are charged the wholesale rate (e.g., $0.05/auth).
*   **Reselling:** You may charge your merchants whatever you wish (e.g., $0.10/auth).

### Webhooks
Configure a global webhook to receive events for ALL your merchants.

**Endpoint:** `POST /v1/partners/webhooks`
```json
{
  "url": "https://api.your-platform.com/webhooks/global",
  "secret": "your_signing_secret"
}
```

**Payload Example:**
```json
{
  "event": "verification.success",
  "merchant_id": "7a8b9c-...",
  "timestamp": "2026-02-01T12:00:00Z",
  "data": { ... }
}
```
