# NoTap Billing - User Guide

**Status:** Production Ready (Pricing Model v2.1: Pay per Verification)
**Last Updated:** Jan 31, 2026

---

## 1. Overview

NoTap uses a **transactional billing model**. As a merchant, you pay for **value received**: successful verifications.

*   **You do NOT pay for enrollments.** You can onboard as many users as you want.
*   **You do NOT pay for failed attempts.** If we block a fraudster, it's on the house.
*   **You DO pay for successful trust.** When a user authenticates (`/verify` returns `success: true`), it counts against your monthly quota.

---

## 2. Managing Your Subscription

Go to the **Merchant Dashboard** > **Billing**.

### Quotas & Overages
*   **Quota:** Your plan includes a set number of verifications (e.g., Starter = 10,000).
*   **Overage:** If you exceed your quota, your service **continues uninterrupted**. You will be billed for the extra verifications at the end of the month (e.g., $0.005 per extra verification on Starter).
*   **Sandbox:** The Sandbox plan is hard-capped at 500 verifications. If you hit the limit, the API will return `429 Too Many Requests` until the next month. Upgrade to Starter to remove this cap.

### Upgrading
You can upgrade your plan at any time.
*   **Instant Effect:** Your quota increases immediately.
*   **Proration:** You will be charged the difference for the remainder of the month.

---

## 3. Monitoring Usage

The **Usage Dashboard** provides real-time insights:
*   **Verifications:** Total successful authentications this billing cycle.
*   **Success Rate:** Percentage of attempts that succeeded.
*   **Cost Estimate:** Projected bill based on current usage trends.

---

## 4. Consumer Billing

For end-users (Consumers), billing is handled via the mobile app.
*   **Free:** Standard features.
*   **Plus:** Subscription managed via Google Play / App Store.

---

## 5. Billing Support

For billing inquiries, invoices, or enterprise custom quotes, please contact `billing@notap.io`.