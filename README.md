# NoTap SDK - Passwordless, Device-Free Authentication

**NoTap** is a revolutionary passwordless, device-free payment authentication platform powered by zero-knowledge proofs and multi-factor authentication.

## 🌟 Why NoTap?

- **🔐 Passwordless:** No passwords to remember or forget
- **📱 Device-Free:** No phone? No problem! Authenticate on any terminal
- **🛡️ Ultra-Secure:** Zero-knowledge proofs + multi-factor authentication
- **⚡ Fast:** Sub-second authentication
- **🌐 Universal:** Works on POS terminals, web, mobile
- **🔒 Privacy-First:** Your factors never leave your device

---

## 🔗 Links

- 🌐 **Website:** [notap.xyz](https://notap.xyz)
- 📚 **Documentation:** [docs.notap.xyz](https://docs.notap.xyz)
- 💬 **Discord:** [Join Community](https://discord.gg/notap)
- 🐦 **Twitter:** [@NoTapAuth](https://twitter.com/NoTapAuth)
- 📧 **Support:** support@notap.xyz

---



  <h1>notap</h1>
  <p><strong>Authentication Reimagined</strong></p>
  <p><em>Your identity follows you. Not your device.</em></p>
</div>

---

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9+-blue.svg)](https://kotlinlang.org)
[![PSD3 SCA Compliant](https://img.shields.io/badge/PSD3-SCA%20Compliant-green.svg)](https://www.ecb.europa.eu)

---

## 💡 The Simple Idea

**Your phone number works from any phone** — your phone, a friend's phone, a hotel phone, a payphone. The number follows you, not the device.

**NoTap works the same way for authentication.** Your PIN, your pattern, your rhythm — they live in your memory. Use them on any device: a store's tablet, a friend's laptop, a hospital kiosk, any browser.

```
YOUR PHONE NUMBER                    YOUR NOTAP
─────────────────                    ──────────
Works from your phone         ✓     Works from your phone
Works from friend's phone     ✓     Works from friend's phone
Works from hotel phone        ✓     Works from store's tablet
Works from any phone          ✓     Works from any device
```

**Enroll once on your device. Authenticate anywhere without it.**

---

## 📖 Real Stories: When This Matters

### 🔋 The Dead Phone

> *You're at the airport. Flight boards in 20 minutes. Phone battery: 0%. You need coffee.*
>
> **Before NoTap:** Beg strangers for a charger. Miss your flight.
>
> **With NoTap:** Walk to the cafe. Say "tiger-4829" (your NoTap name). Enter your 4-digit PIN on their tablet. Tap your rhythm. Coffee paid. 15 seconds.

### 🏥 The Locked-Out Nurse

> *You're a nurse. Phone is in your locker (hospital rules). A patient needs medication from the secure cabinet. Now.*
>
> **Before NoTap:** Run to locker. Unlock phone. Get 2FA code. Run back. Critical minutes lost.
>
> **With NoTap:** Walk to cabinet. Type your NoTap ID. Draw your pattern on the screen. Cabinet unlocks. Patient gets medication.

### 💻 The 2 AM Emergency

> *Your server crashes. You're at a friend's house. Your 2FA app? On your dead phone at home.*
>
> **Before NoTap:** Drive 30 minutes home. Wake up the family. Lose $50K in downtime.
>
> **With NoTap:** Borrow friend's laptop. Type your NoTap name. Enter your PIN. Draw your pattern. Get emergency AWS access. Fix server in 5 minutes.

### 🏃 The Gym Run

> *Finished your workout. Wallet and phone locked in the locker. Just want a smoothie.*
>
> **Before NoTap:** Go back to locker. Get dressed. Get phone. Come back. The moment's gone.
>
> **With NoTap:** Walk to counter. Use their tablet. PIN + pattern. Smoothie paid. Stay in the zone.

### 🏖️ The Beach Day

> *You're at the beach. Phone is locked in the car (smart). Kids want ice cream.*
>
> **Before NoTap:** Walk back to car. Get phone. Walk back to beach. Ice cream melted by now.
>
> **With NoTap:** Walk to vendor. Use their device. Authenticate. Ice cream for everyone.

---

## ✨ The Magic: How It Works

### Step 1: Enroll Once (5 minutes, on your phone)

Pick 3-6 things only you know or do:

| Factor | What It Is | Example |
|--------|------------|---------|
| **PIN** | 4-6 digits you choose | `4829` |
| **Pattern** | Shape you draw | Like an unlock pattern, but timed |
| **Colors** | Sequence of colors | Red → Blue → Green → Yellow |
| **Emoji** | Sequence of emoji | 🌙 → 🎸 → 🍕 → 🚀 |
| **Rhythm** | Tap pattern you create | *tap-tap---tap-tap-tap* |
| **Words** | 4 words you pick | ocean-tiger-melody-spark |

These get turned into math (cryptographic digests). The actual values never leave your phone. We only store the "puzzle" — not the answer.

**Optional:** Add your fingerprint or face for extra security when your phone IS available.

### Step 2: Get Your NoTap Name

Choose how you want to be identified:

- **Simple:** `tiger-4829` (auto-generated, easy to remember)
- **Professional:** `alice.notap.sol` (blockchain name, like an email)
- **Technical:** `a1b2c3d4-5678-90ab...` (UUID, most secure)

### Step 3: Use It Anywhere

Walk up to any device. Enter your name. Complete 2-3 of your factors. Done.

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   Welcome to NoTap                                          │
│                                                             │
│   Enter your NoTap ID: [tiger-4829          ]              │
│                                                             │
│   ─────────────────────────────────────────────────────     │
│                                                             │
│   Enter PIN:  [• • • •]                     ✓ Verified     │
│                                                             │
│   Draw Pattern: [        ]                  ✓ Verified     │
│                 [   ───  ]                                  │
│                 [     │  ]                                  │
│                                                             │
│   ─────────────────────────────────────────────────────     │
│                                                             │
│   ✅ Identity Verified                                      │
│   Processing payment of $4.99...                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**That's it.** No phone. No card. No wallet. Just you.

---

## 🚀 What This Unlocks

Once you can authenticate anywhere, you can do anything that requires "proving it's you":

| Use Case | How It Works |
|----------|--------------|
| **Pay at stores** | Authenticate → Payment processes through your linked Stripe/Apple Pay/etc. |
| **Enter buildings** | Authenticate → Door unlocks (no badge needed) |
| **Access servers** | Authenticate → Get temporary credentials (no 2FA app needed) |
| **Open secure cabinets** | Authenticate → Lock releases (hospital, pharmacy, lab) |
| **Log into computers** | Authenticate → Session starts (no password manager needed) |
| **Approve transactions** | Authenticate → Wire transfer approved (no SMS code needed) |

**NoTap is the authentication layer.** What you DO after authenticating is up to you.

---

## 🤔 "Wait, Is This Complicated?"

**Nope. Here's the truth:**

| Your Worry | The Reality |
|------------|-------------|
| *"15 factors sounds like a lot!"* | You pick 3-6. System only asks for 2-3 per use. |
| *"Too much to remember!"* | A 4-color sequence is easier than `P@ssw0rd123!` |
| *"Takes forever!"* | 10-15 seconds. Faster than fumbling for your phone. |
| *"What if I forget one?"* | System asks for a different factor. You're not locked out. |
| *"I'm bad at memory!"* | Patterns and rhythms are muscle memory. Like riding a bike. |

**Think of it this way:** You remember your phone number, your PIN, your home address, your mother's birthday. NoTap just uses what you already know how to do.

---

## 🔐 The Security (Without the Jargon)

**What we store:** A math puzzle that only your factors can solve.

**What we DON'T store:** Your actual PIN, pattern, colors, or words.

**How it works:**
1. You enter your PIN on any device
2. That device turns it into a math result (called a "digest")
3. We check if the math result matches — without ever seeing your PIN

It's like a teacher checking if your math homework answer is correct, without seeing your work.

**Extra protection:**
- Your factors expire and refresh automatically every 24 hours
- Even if someone stole our database, they couldn't reverse the math to get your factors
- When your phone IS available, add fingerprint/face for maximum security

---

## 🎯 Who This Is For

### For People (Consumers)

- **Athletes & gym-goers** — Leave phone in locker, still buy post-workout
- **Beach/pool lovers** — Don't risk your phone near water
- **Travelers** — Phone stolen abroad? Still get home
- **Parents** — Phone with kids? Still buy groceries
- **Anyone** — Battery dead? Life continues

### For Businesses (B2B)

- **Hospitals** — Staff authenticate without phones in sterile areas
- **Warehouses** — Workers with gloves authenticate on fixed terminals
- **Schools** — Students access without losing ID cards
- **Offices** — No more badge replacements, no more lockouts
- **Call centers** — Agents log in without personal devices

### For Developers

- **API-first** — Simple REST endpoints
- **SDK available** — Android (production), iOS (coming), Web (production)
- **PSP integration** — Stripe, Adyen, Square, Tilopay built-in
- **Self-service** — Developer portal with API keys, webhooks, sandbox

---

## ⚡ Quick Technical Overview

### What NoTap Is

**An authentication service.** We answer one question: *"Is this person who they claim to be?"*

Once verified, we hand off to whatever comes next:
- **Payments:** We tell Stripe/Adyen/Apple Pay "this person is verified, process the payment"
- **Access control:** We tell the door/system "this person is verified, grant access"
- **We never touch money.** We never store passwords. We just verify identity.

### What NoTap Is NOT

| We're NOT | We integrate WITH |
|-----------|-------------------|
| A payment processor | Stripe, Adyen, Square, Apple Pay, Google Pay |
| A biometric scanner manufacturer | Apple's Secure Enclave, Android's TEE |
| A password manager | We replace passwords, not store them |
| Competing with Apple/Google Pay | We complement them — authenticate with NoTap, pay with them |

### Two Modes

| Mode | What Happens | Example |
|------|--------------|---------|
| **Authentication Only** | Verify identity → Grant access | Building entry, server login, secure cabinet |
| **Auth + Payment** | Verify identity → Hand off to PSP → Payment processes | Store checkout, restaurant bill, online purchase |

---

## 🆕 Beyond Payments: Pure Authentication (NEW)

**NoTap now supports TWO modes:**

### 💳 Payment Mode (Traditional)
- **Use Case:** Making purchases without your phone/wallet
- **Flow:** Enroll → Link payment provider → Verify → Payment processed
- **Examples:** Pay at gym, restaurant, store - hands-free

### 🔐 Authentication Mode (NEW - v2.0)
- **Use Case:** Access control & API authentication WITHOUT payments
- **Flow:** Enroll → Link to service → Verify → Access granted (no payment)
- **Why This Matters:** Most authentication is NOT payment-related

**Authentication Mode Use Cases:**

#### 🏢 Enterprise Access Control
- **Building entry** → No badge needed, authenticate at door
- **Computer login** → Device-free MFA for workstations
- **Secure room access** → Labs, data centers, executive offices
- **Time clock** → Clock in/out without badge
- **Benefit:** Employees don't need badges, phones, or tokens

#### 🌐 API & Developer Authentication
- **API key replacement** → NoTap ID + factors = secure API auth
- **Webhook verification** → Authenticate webhook sources
- **CI/CD pipeline access** → Secure deployment authentication
- **Admin panel login** → No password, no 2FA app
- **Benefit:** Zero secrets to store, rotate, or leak

#### 🏥 Healthcare & Compliance
- **EMR system access** → HIPAA-compliant device-free login
- **Prescription verification** → Authenticate without phone
- **Lab equipment access** → Cleanroom-safe (no devices)
- **Patient check-in** → Hands-free hospital kiosks
- **Benefit:** Compliance-ready, no phone contamination

#### 🏦 Banking & Finance
- **ATM without card** → Authenticate with factors only
- **Wire transfer approval** → Multi-factor without SMS
- **Trading platform access** → Secure trader authentication
- **Vault access** → Physical + digital factor verification
- **Benefit:** Zero SIM-swap vulnerability

#### 🎓 Education
- **Campus access** → No student ID card needed
- **Exam authentication** → Verify student identity
- **Dorm room entry** → Authenticate at any door
- **Library checkout** → No card required
- **Benefit:** Students don't lose/forget IDs

#### 🚗 Transportation & Logistics
- **Vehicle access** → Authenticate to unlock company vehicles
- **Warehouse entry** → Hands-free with gloves/equipment
- **Delivery confirmation** → Driver authentication
- **Parking gate access** → No fob needed
- **Benefit:** Equipment-free authentication

**Technical Benefits:**
- ✅ **Resource-based risk:** PUBLIC/PRIVATE/SENSITIVE/CRITICAL
- ✅ **Permission validation:** Scoped access tokens (OAuth-style)
- ✅ **Session management:** JWT + session tokens
- ✅ **No payment processing:** Pure authentication, zero payment overhead
- ✅ **Backward compatible:** Existing payment mode unchanged

**Architecture:**
```
AUTHENTICATION MODE:
User → Enroll factors → Link to merchant/service
     → Authenticate → Access token generated
     → Access granted (no payment)

PAYMENT MODE:
User → Enroll factors → Link payment provider
     → Authenticate → Payment processed
```

**Market Opportunity:**
- **Global MFA market:** $18.2B by 2028 (Mordor Intelligence)
- **Problem:** 100% of MFA solutions are device-bound
- **NoTap advantage:** ONLY device-free authentication platform
- **B2B enterprise:** Replace badges, tokens, passwords, SMS codes
- **B2C consumer:** Authenticate without phone (gym, work, travel)

---

## 🧑‍💻 Developer Portal: Self-Service Integration

**NEW in v2.0:** Complete developer self-service platform for seamless NoTap integration.

### Features

#### 🔑 **API Key Management**
- **Generate keys instantly** → No approval wait time
- **Multiple environments** → Sandbox, staging, production keys
- **Scoped permissions** → Read-only, write, admin
- **Key rotation** → Regenerate keys without downtime
- **Usage tracking** → Real-time API call monitoring

#### 🪝 **Webhook Configuration**
- **Event subscriptions:**
  - `enrollment.completed` → User enrolled successfully
  - `verification.succeeded` → Authentication passed
  - `verification.failed` → Authentication failed
  - `payment.processed` → Payment completed
  - `session.expired` → Session timed out
- **Delivery monitoring** → See webhook delivery status
- **Retry logic** → Automatic retries with exponential backoff
- **Signature verification** → HMAC-SHA256 webhook signatures
- **Test webhooks** → Send test events to verify integration

#### 📊 **Analytics Dashboard**
- **Usage statistics:**
  - Total enrollments
  - Total verifications
  - Success/failure rates
  - Average authentication time
  - Geographic distribution
- **Real-time monitoring** → Live API call logs
- **Error tracking** → Failed requests with stack traces
- **Performance metrics** → Response times, latency

#### 🧪 **Sandbox Testing Environment**
- **Test mode** → Fake payments, no real charges
- **Test users** → Pre-configured test accounts
- **Mock responses** → Simulate success/failure scenarios
- **Factor testing** → Test all 15 authentication factors
- **Zero risk** → No impact on production data

#### 🔐 **Security Features**
- **JWT authentication** → Secure API access
- **IP whitelisting** → Restrict API access by IP
- **Rate limiting** → Prevent abuse (configurable limits)
- **Audit logs** → Complete activity history
- **Step-up authentication** → Sensitive actions require re-auth

### Developer Portal Access

**Quick Start:**
1. Visit `https://developer.notap.io`
2. Sign up with email/password or OAuth (Google, GitHub)
3. Create your first project
4. Generate API keys (sandbox + production)
5. Copy integration code snippets
6. Start testing in 5 minutes

**Pricing:**
- **Free Tier:** 1,000 verifications/month, 1 project
- **Starter:** $49/month → 10,000 verifications, 3 projects
- **Professional:** $199/month → 50,000 verifications, unlimited projects
- **Enterprise:** Custom pricing → Volume discounts, SLA, support

---

## 👤 Management Portal: Self-Service Account Management

**NEW in v2.0:** Complete user self-service portal for managing NoTap accounts.

### Features

#### 📋 **Account Overview**
- **NoTap ID display** → UUID, alias, blockchain name
- **Enrollment date** → When you registered
- **Last authentication** → Most recent verification
- **Account status** → Active, suspended, locked
- **Statistics:**
  - Total authentications
  - Success rate
  - Most used factors
  - Recent activity timeline

#### 🔐 **Factor Management**
- **View enrolled factors** → See which factors you've set up
- **Remove factors** → Delete unused authentication factors
- **Update factors** → Change PIN, pattern, etc.
- **Add factors** → Enroll new authentication methods
- **Factor usage stats** → See which factors you use most
- **Test factors** → Verify factors work before using at merchant

#### 📱 **Device Management**
- **View devices** → See all devices you've enrolled from
- **Revoke devices** → Remove compromised devices
- **Device trust levels** → Primary, secondary, trusted
- **Last seen** → When each device was last used

#### 🔒 **Security Settings**
- **Change password** → Update account password
- **Enable 2FA** → Add extra security layer
- **View security log** → Audit trail of account changes
- **Failed login attempts** → See suspicious activity
- **Step-up authentication** → Require extra auth for sensitive actions

#### 🌐 **Blockchain Name Management**
- **View linked names** → See all blockchain names
- **Add names** → Link ENS, Unstoppable, BASE names
- **Remove names** → Unlink blockchain names
- **Primary name** → Set default name for verification
- **Verify ownership** → Re-verify wallet signatures

#### 🗑️ **GDPR Compliance**
- **Data export** → Download all your data (JSON format)
- **Account deletion** → Permanently delete your account
- **Data retention policy** → See what data we store
- **Privacy settings** → Control data sharing preferences
- **Right to be forgotten** → Complete data removal

#### ⚡ **Quick Actions**
- **Generate QR code** → Share your NoTap ID
- **Print receipt** → Physical card with UUID/alias
- **Share link** → Send NoTap ID via SMS/email
- **Reset factors** → Emergency factor reset (with step-up auth)

### Management Portal Access

**How to Access:**
1. Visit `https://manage.notap.io`
2. Authenticate using your NoTap factors
3. View and manage your account
4. Make changes in real-time

**Security:**
- **Step-up authentication** → Sensitive actions (delete account, remove factors) require re-verification
- **Session timeout** → 15 minutes of inactivity logs you out
- **Device fingerprinting** → Detect suspicious login locations
- **Email notifications** → Alerts for account changes

---

## 🏆 Why NoTap vs. Alternatives?

| Scenario | Cash/Card | Apple Pay | Amazon One | **NoTap** |
|----------|-----------|-----------|------------|-----------|
| **MFA Type** | ⚠️ Static (CVV) | ❌ Device-bound (SMS/biometric) | ⚠️ Biometric (palm) | ✅ **Device-free (any device)** |
| **Phone stolen** | ❌ Need card | ❌ Need phone | ❌ Amazon only | ✅ **Works anywhere** |
| **Wallet forgotten** | ❌ Can't pay | ⚠️ If you have phone | ❌ Venue-locked | ✅ **Any merchant** |
| **Battery dead** | ❌ Need card | ❌ Need phone | ❌ Limited venues | ✅ **Use merchant's device** |
| **Phone in locker** | ❌ Need card | ❌ Can't get SMS code | ❌ Venue-locked | ✅ **Authenticate on any device** |
| **Zero-trust device** | ⚠️ Risk skimming | ⚠️ Risk phone hack | ❌ Not portable | ✅ **No card exposed** |
| **Backup payment** | ⚠️ Carry extra cards | ⚠️ Need device sync | ❌ Not a payment system | ✅ **Always available** |
| **Empty-handed** | ❌ Must carry items | ❌ Need phone | ⚠️ Palm only, Amazon only | ✅ **Truly hands-free** |
| **Privacy** | ⚠️ Card saved on device | ⚠️ Apple/Google know purchases | ⚠️ Amazon tracks everything | ✅ **Zero trace after payment** |

**NoTap is the ONLY device-free, portable, merchant-agnostic payment authentication solution.**

**Key differentiator:** Traditional MFA is **device-bound** (your phone). NoTap is **device-free** (any device).

---

## 🛡️ How NoTap Works (Technical Overview)

### Authentication Factors (15 Available)

NoTap uses **multi-factor authentication** - you choose 3+ factors from 15 available (6+ recommended for maximum security):

#### 🧠 Knowledge Factors
- **PIN** (4-12 digits)
- **Pattern** (visual unlock pattern)
- **Colors** (sequence of 3-6 colors)
- **Emoji** (sequence of 3-8 emojis)
- **Words** (memorable word sequence)

#### 👤 Biometric Factors
- **Fingerprint** (via device sensor)
- **Face Recognition** (via device camera)
- **Voice** (spoken passphrase)

#### 🎯 Behavioral Factors
- **Rhythm Tap** (your unique tapping pattern)
- **Mouse/Stylus Draw** (your signature style)
- **Image Tap** (tap sequence on an image)
- **Balance Gesture** (device tilt pattern)

#### 📡 Possession Factors
- **NFC** (tap your NFC tag/card)

### 🧘 Cognitive Load & User Experience

**"Wait, 15 factors sounds overwhelming!" - It's not. Here's why:**

#### Enrollment (One-Time, 5-10 Minutes)
- **You choose only 3-15 factors** - Pick what fits your memory style
- **Minimum 3, recommended 6+** - More factors = better security, but start small if needed
- **Mix factor types** - Combine easy (PIN, colors) + secure (biometrics, rhythm)
- **~2 minutes per factor** - PIN takes 30 seconds, pattern takes 1-2 minutes to practice
- **You're in control** - Skip factors you find difficult (no face camera? skip Face Recognition)

#### Daily Use (10-30 Seconds per Payment)
- ✅ **NOT stressful** - System asks for only **2-3 factors** per transaction (NOT all of them!)
- ✅ **Random selection** - You won't use all enrolled factors every time
- ✅ **Fast factors** - PIN = 5 seconds, Pattern = 8 seconds, Colors = 10 seconds
- ✅ **Biometrics available** - Fingerprint/Face ID when device supports it (instant)
- ✅ **Forgiving system** - One mistake doesn't lock you out (see Dynamic Factor Escalation below)

#### Real Example: 6 Enrolled Factors

**During enrollment (one-time):**
```
You enroll: PIN + Pattern + Emoji + Rhythm + Fingerprint + Colors
Total time: ~10 minutes (one-time setup)
```

**Daily usage (every transaction):**
```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ Coffee ($4)     │  │ Lunch ($15)     │  │ Groceries ($75) │
│                 │  │                 │  │                 │
│ System asks:    │  │ System asks:    │  │ System asks:    │
│ ✓ PIN           │  │ ✓ Pattern       │  │ ✓ Rhythm        │
│ ✓ Fingerprint   │  │ ✓ Colors        │  │ ✓ Emoji         │
│                 │  │                 │  │ ✓ Fingerprint   │
│ ⏱ 10 seconds    │  │ ⏱ 15 seconds    │  │ ⏱ 25 seconds    │
└─────────────────┘  └─────────────────┘  └─────────────────┘

You NEVER complete all 6 factors in one transaction!
The system picks 2-3 based on purchase amount and risk level.
```

#### Cognitive Load Comparison

| Authentication Type | What You Remember | What You Do Per Transaction | Time |
|---------------------|-------------------|----------------------------|------|
| **Traditional Password** | 1 complex string (12-20 chars, symbols, uppercase) | Type entire password every time | ~15 seconds |
| **SMS 2FA** | Password + have phone nearby | Type password + wait for SMS + type code | ~45 seconds |
| **NoTap (6 factors)** | 6 simple patterns (PIN, colors, emoji, etc.) | Complete only 2-3 factors (random selection) | **10-25 seconds** |

**Why NoTap is actually EASIER:**
- ✅ **Diversity reduces mental load** - Remembering 4 colors is easier than 16 random characters
- ✅ **Not all at once** - You only use 2-3 factors per transaction (80% of time = only 2)
- ✅ **Visual/tactile memory** - Pattern/rhythm tap use muscle memory (not conscious recall)
- ✅ **Biometrics available** - Fingerprint/face = zero mental effort
- ✅ **Forgiving** - Forgot one factor? System asks for different ones next time

#### Time Investment Breakdown

**80% of your daily purchases** (coffee, snacks, transit - under $30):
- System asks: **2 factors only**
- Time: **10-15 seconds**
- Faster than entering a credit card + PIN

**20% of purchases** (groceries, clothing - over $30):
- System asks: **3 factors**
- Time: **20-30 seconds**
- Same speed as chip card + PIN + SMS 2FA

**No phone needed, no wallet needed, no cards to carry - just your memory!**

### Security Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    ENROLLMENT (One-Time)                     │
├─────────────────────────────────────────────────────────────┤
│ 1. User selects 3+ factors (6+ recommended)                │
│ 2. (Optional) Register SNS name (alice.notap.sol)          │
│ 3. Each factor → SHA-256 digest (on your device)           │
│ 4. Digests encrypted with your key (PBKDF2 + AES-256-GCM)  │
│ 5. Backend wraps key with AWS KMS                           │
│ 6. Encrypted data stored (24h TTL, GDPR compliant)         │
│ 7. (Blockchain) Enrollment commitment stored on Solana     │
│ 8. You receive NoTap ID (UUID, alias, or SNS name)         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              VERIFICATION (Every Purchase)                   │
├─────────────────────────────────────────────────────────────┤
│ 1. You walk into merchant (no phone, no wallet)            │
│ 2. Merchant enters your NoTap ID (UUID/alias/SNS name)     │
│ 3. Backend resolves SNS name → UUID (if SNS used)          │
│ 4. Backend calculates risk (LOW/MEDIUM/HIGH)               │
│ 5. System selects 2-3 factors based on risk + amount       │
│ 6. You complete factors on merchant's device               │
│ 7. NoTap verifies each factor (constant-time)              │
│    ├─ Factor fails? → Escalate (add 1 factor, restart)    │
│    └─ All pass? → Generate ZK-proof (privacy-preserving)  │
│ 8. (Blockchain) Authentication logged on Solana            │
│ 9. Payment authorized → Merchant processes via Stripe/etc. │
└─────────────────────────────────────────────────────────────┘
```

### Security Features

- ✅ **Double-Layer Encryption** (PBKDF2 + AWS KMS)
- ✅ **Zero-Knowledge Proofs** (ZK-SNARKs for privacy)
- ✅ **Constant-Time Operations** (prevents timing attacks)
- ✅ **Memory Wiping** (sensitive data cleared after use)
- ✅ **Anti-Tampering** (detects rooted/jailbroken devices, debug mode)
- ✅ **Rate Limiting** (prevents brute-force attacks)
- ✅ **TLS 1.3** (encrypted data in transit)
- ✅ **24-Hour TTL + Auto-Renewal** (GDPR compliant, phone theft protection)
- ✅ **No Biometric Storage** (only cryptographic hashes)
- ✅ **Factor Shuffling** (anti-shoulder-surfing protection)
- ✅ **Risk-Based Authentication** (2-3 factors depending on transaction risk)
- ✅ **Dynamic Factor Escalation** (adaptive security on authentication failure)

### 🔄 Automatic Daily Key Rotation (Phone Theft Protection)

**Problem:** If your phone is stolen, attackers could extract authentication data.

**NoTap Solution:** **Automatic daily digest rotation** - your authentication credentials expire and renew **every 24 hours** with zero user action.

**How it works:**

1. **During Enrollment:**
   - Your device generates a **master key** (stays on your device forever, hardware-encrypted)
   - Day 0 digest is derived from master key → sent to backend (24h expiration)

2. **Every 20 Hours (Automatic Background Process):**
   - Your device derives a **new daily digest** using HKDF cryptographic rotation
   - Backend updates Redis with fresh digest (new 24h expiration starts)
   - Old digest becomes **useless** immediately ✅

3. **If Phone is Stolen:**
   - Attacker gets current day's digest (if they extract it from backend)
   - Digest expires in **≤24 hours**
   - Attacker **cannot** derive tomorrow's digest (needs master key from your device)
   - After 24h: **Completely locked out** ✅

**Security Guarantees:**
- ✅ **Forward Secrecy:** Day N digest cannot derive Day N+1 digest
- ✅ **Limited Damage:** Maximum 24-hour compromise window
- ✅ **Zero Server Knowledge:** Master keys never leave your device
- ✅ **Hardware-Protected:** Master keys stored in TEE/StrongBox (Android KeyStore)
- ✅ **30-Day Re-Enrollment:** Forced fresh enrollment every 30 days for maximum security

**Technical Details:**
- Uses HKDF (HMAC-based Key Derivation Function) from RFC 5869
- Runs as Android WorkManager background job (battery-efficient)
- Constraints: Network required, battery not low
- Retries: Up to 3 attempts with exponential backoff
- No database writes (only Redis cache updated)

**This is enterprise-grade key rotation** - similar to Kerberos tickets, OAuth refresh tokens, and TLS session keys!

### 🔀 Factor Shuffling (Anti-Shoulder-Surfing)

NoTap randomly selects factors per transaction from your enrolled set (2-3 based on risk):

**Enrollment:**
- You set up 3+ factors (6+ recommended, e.g., PIN, pattern, emoji, rhythm, colors, words)

**Verification (randomized every time):**
- **Today:** System asks for PIN + emoji sequence
- **Tomorrow:** System asks for pattern + rhythm tap
- **Next time:** System asks for colors + words

**Security benefits:**
- ✅ **Merchant employee sees your PIN?** They still need your other enrolled factors to authenticate
- ✅ **Someone watches over your shoulder?** They only see 2 out of your enrolled factors
- ✅ **Replay attack?** Different factors required next transaction
- ✅ **Stolen factor?** One compromised factor ≠ account compromised

**This makes NoTap MORE secure than static passwords/PINs.**

### ⚖️ Risk-Based Authentication (Dynamic Factor Count)

NoTap intelligently adjusts the number of factors based on transaction amount and fraud detection:

| Scenario | Amount | Risk Level | Factors Required | Example |
|----------|--------|------------|------------------|---------|
| **Low-Risk Purchase** | < $30 | LOW | **2 factors** | Coffee, snacks, transit |
| **Medium Purchase** | $30-$99 | LOW/MEDIUM | **3 factors** | Restaurant, groceries |
| **High-Value Purchase** | ≥ $100 | LOW/HIGH | **3 factors** | Electronics, clothing |
| **Fraud Detected** | Any | HIGH | **3 factors** | Suspicious activity |

**User experience:**
- ✅ **80% of transactions**: Only 2 quick factors (~10-15 seconds)
- ✅ **Higher-value purchases**: 3 factors for added security (~20-30 seconds)
- ✅ **Adaptive to risk**: System adjusts based on fraud patterns, not just amount
- ✅ **Fast factors available**: PIN, pattern, colors = 5 seconds each

### 🔺 Dynamic Factor Escalation (Adaptive Security)

If you fail one factor, NoTap **escalates the challenge** instead of locking you out:

**How it works:**

1. **Initial Challenge** (Low-risk $5 coffee)
   - System: "Complete 2 factors: PIN + Pattern"
   - You enter PIN → ❌ **INCORRECT**

2. **Escalation** (Add 1 factor, remove failed)
   - System: "PIN failed. Complete 2 factors: **Pattern + Emoji**"
   - PIN removed, Emoji added (no retry of failed factor)
   - Challenge resets, you start fresh

3. **Second Chance**
   - You complete Pattern + Emoji → ✅ **SUCCESS**
   - Payment authorized!

4. **Rate Limiting** (After 2 full challenge failures)
   - Two failed challenges → 15-minute cooldown
   - Prevents brute-force while allowing honest mistakes

**Benefits:**
- ✅ **Honest mistakes don't lock you out** → One wrong factor doesn't mean failure
- ✅ **Adaptive security** → Failed factor = slightly harder challenge
- ✅ **No factor repetition** → Failed factor removed from retry
- ✅ **Brute-force protection** → 2 challenge failures = rate limit

**Example Flow:**
```
Transaction: $5 coffee (2 factors required)
├─ Attempt 1: PIN fails → Escalate to Pattern + Emoji
├─ Attempt 2: Pattern + Emoji succeed → ✅ Payment authorized
└─ Total time: ~20 seconds (includes retry)
```

**This makes NoTap more forgiving than traditional authentication while maintaining security.**

---

## 🆔 User Identification: Three Ways to Find You

**NEW in v2.0:** NoTap now supports **3 identification methods** - making it easier than ever to authenticate!

### 1️⃣ **UUID (Traditional)**
```
a1b2c3d4-5678-90ab-cdef-1234567890ab
```
- ✅ **Original method** - Always works
- ✅ **Most secure** - Cryptographically random
- ❌ **Hard to remember** - 36 characters

### 2️⃣ **Alias (Memorable ID)** ⭐ NEW!
```
tiger-4829
```
- ✅ **Memorable** - Word + number format (e.g., `ocean-7342`, `dragon-1856`)
- ✅ **Shorter** - Only 8-15 characters vs 36-char UUID
- ✅ **Easier to type** - Fits on receipts/cards/QR codes
- ✅ **Multi-language** - English seeded, Spanish/Portuguese/French ready
- ✅ **Scalable** - 10M combinations (1,000 words × 10,000 numbers)
- ✅ **Auto-generated** - Deterministic from UUID (same UUID = same alias)

### 3️⃣ **Blockchain Name (Human-Readable)** ⭐ NEW!
```
alice.eth (Ethereum)
alice.notap.sol (Solana)
alice.crypto (Unstoppable)
alice.base.eth (BASE L2)
```
- ✅ **Memorable** - Like an email address
- ✅ **Professional** - Branded identity
- ✅ **Universal** - Works across all NoTap merchants
- ✅ **Optional** - Free during enrollment (SNS) or bring your own
- ✅ **Multi-Chain** - Supports 4 blockchain name services

### How It Works at Point of Sale

**Merchant asks:** "What's your NoTap ID?"

**You can provide ANY of these:**
1. Full UUID → `a1b2c3d4-5678-90ab-cdef-1234567890ab`
2. Memorable alias → `tiger-4829`
3. **Blockchain name → `alice.eth`, `alice.notap.sol`, `alice.crypto`, `alice.base.eth`** ← Most professional!

**Backend automatically resolves all formats to your account!**

### Blockchain Name Benefits

#### 🎯 **For Users:**
- **Easy to remember:** Just like an email address
- **Professional identity:** `john.eth` or `john.notap.sol` instead of random UUID
- **Write it down:** Give it to merchants verbally or on paper
- **QR code backup:** Your name can be printed on business cards
- **Portable:** Works at ANY NoTap merchant worldwide
- **Multi-chain:** Use existing ENS, Unstoppable, or BASE names

#### 🏪 **For Merchants:**
- **Reduced errors:** Users type names instead of 36-char UUIDs
- **Faster checkout:** "alice.eth" vs copying UUID from phone
- **Better UX:** Professional, memorable identifiers
- **Loyalty programs:** Easier to track than UUID

#### 🌐 **Supported Blockchain Name Services:**
- **Solana Name Service (.sol, .notap.sol)** - Primary integration, free subdomains
- **Ethereum Name Service (.eth)** - Most popular blockchain names
- **Unstoppable Domains (.crypto, .nft, .wallet, .dao, .x, .bitcoin, .blockchain, .zil, .888)** - Multi-chain support
- **BASE Name Service (.base.eth)** - Layer 2 Ethereum names

### Registering Your Blockchain Name

**During enrollment (optional step):**

1. **Choose your blockchain name service:**
   - **Register new** `name.notap.sol` (free, instant) ✨
   - **Bring existing** ENS name (`.eth`)
   - **Bring existing** Unstoppable domain (`.crypto`, `.nft`, etc.)
   - **Bring existing** BASE name (`.base.eth`)

2. **For new Solana names** (name.notap.sol):
   - Check availability → Type preferred name (e.g., "alice")
   - ✅ **Available:** "alice.notap.sol is available! ✨"
   - ❌ **Taken:** See smart suggestions: "alice1", "alice_notap", "alice2025"

3. **For existing blockchain names:**
   - Enter your full name (e.g., "alice.eth")
   - Verify ownership via wallet signature
   - Auto-link to your NoTap account

4. **Real-time validation:**
   - Auto-detects name service by TLD (.eth, .sol, .crypto, etc.)
   - Name length: 1-63 characters
   - Allowed: lowercase letters, numbers, hyphens

5. **Skip if not interested:**
   - Blockchain names are **100% optional**
   - You can always use UUID or alias
   - Add name later via Management Portal

### Technical Details

**Multi-Chain Name Service Integration:**
- **Solana Name Service:** Bonfida (@bonfida/spl-name-service)
  - Parent domain: `notap.sol`
  - Free subdomains for users
  - Instant registration
- **Ethereum Name Service:** viem library (viem@^2.0.0)
  - Supports `.eth` names
  - Auto-resolves via Ethereum mainnet
- **Unstoppable Domains:** @unstoppabledomains/resolution@^9.3.3
  - Supports 13 TLDs (.crypto, .nft, .wallet, .dao, .x, .bitcoin, .blockchain, .zil, .888, etc.)
  - Multi-chain resolution (Polygon, Ethereum)
- **BASE Name Service:** viem library
  - Layer 2 Ethereum names (`.base.eth`)
  - Fast, low-cost resolution

**Architecture:**
- **Pluggable Provider System:** Each blockchain has dedicated provider
- **Auto-Routing:** Backend detects TLD and routes to correct provider
- **Toggle System:** Enable/disable chains independently via backend config
- **Fallback:** If blockchain resolution fails, use UUID/alias
- **Storage:** PostgreSQL with indexed blockchain_name column
- **Privacy:** Blockchain names are public (like domain names)

**API Endpoints:**
```bash
# Multi-chain name resolution
GET  /v1/names/resolve/:name           # Auto-detect chain, resolve name → UUID
GET  /v1/names/reverse/:uuid           # Reverse lookup UUID → name
GET  /v1/names/check-availability/:name # Check SNS availability

# Legacy SNS endpoints (still supported)
GET  /v1/sns/check-availability/:name
GET  /v1/sns/resolve/:snsName
GET  /v1/sns/reverse/:uuid
```

**SDK Support:**
```kotlin
// Multi-chain name resolution (auto-detects chain)
val uuid = nameServiceClient.resolveName("alice.eth")
val uuid2 = nameServiceClient.resolveName("alice.notap.sol")
val uuid3 = nameServiceClient.resolveName("alice.crypto")

// Check SNS availability
val result = nameServiceClient.checkAvailability("alice")
if (result.available) {
    println("alice.notap.sol is available!")
}

// Reverse lookup
val name = nameServiceClient.reverseLookup(userUuid)

// Get supported chains
val chains = nameServiceClient.getSupportedChains()
```

---

## ⛓️ Blockchain & Decentralized Identity

**NEW in v2.0:** NoTap now includes **comprehensive blockchain integration** for enhanced security, privacy, and interoperability.

### Why Blockchain?

Traditional authentication stores everything in centralized databases. **NoTap leverages blockchain for:**

1. **Immutable Audit Trail** → Every authentication logged on-chain
2. **Decentralized Identity (DID)** → W3C-compliant identity standards
3. **Zero-Knowledge Proofs** → Privacy-preserving verification
4. **Smart Contract Security** → Tamper-proof enrollment commitments
5. **Human-Readable Names** → Solana Name Service (SNS) integration

### 🔗 Blockchain Features

#### 1. **W3C Decentralized Identifiers (DIDs)**

Every NoTap user gets a **standards-compliant DID** that works across systems:

**Supported DID Methods:**
```
did:sol:alice.notap              # SNS name (most user-friendly)
did:sol:9xQeWvG816bUx9...        # Solana wallet address
did:web:notap.io:users:alice    # Web-based DID
did:key:z6Mkf...                 # Self-describing key
```

**DID Document Example:**
```json
{
  "@context": ["https://www.w3.org/ns/did/v1"],
  "id": "did:sol:alice.notap",
  "controller": "did:sol:alice.notap",
  "verificationMethod": [{
    "id": "did:sol:alice.notap#notap-auth-key",
    "type": "Ed25519VerificationKey2020",
    "controller": "did:sol:alice.notap",
    "publicKeyMultibase": "z6Mkf..."
  }],
  "authentication": ["did:sol:alice.notap#notap-auth-key"],
  "service": [{
    "id": "did:sol:alice.notap#enrollment",
    "type": "NoTapEnrollmentService",
    "serviceEndpoint": {
      "url": "https://api.notap.io/v1/enrollment/...",
      "zkProofSupported": true,
      "factorCount": 6
    }
  }]
}
```

**Benefits:**
- ✅ **Universal Identity:** Works with any DID-compatible system
- ✅ **Verifiable Credentials:** Export authentication proofs as VCs
- ✅ **Interoperability:** Compatible with Web3Auth, Civic, Lit Protocol
- ✅ **Future-Proof:** Standards-based (W3C DID Core 1.0)

#### 2. **Solana Smart Contracts**

NoTap deploys **two Rust/Anchor smart contracts** on Solana:

**Enrollment Program** (440 LOC):
```rust
pub fn create_enrollment(
    user_uuid_hash: [u8; 32],
    merkle_root: [u8; 32],
    factor_count: u8,
    category_count: u8,
    expires_at: i64
)
```
- Stores enrollment commitments on-chain
- Immutable factor count and expiration
- Supports auto-renewal (HKDF-based)
- GDPR-compliant revocation

**Audit Program** (330 LOC):
```rust
pub fn record_auth_event(
    user_uuid_hash: [u8; 32],
    merchant_id: [u8; 32],
    success: bool,
    timestamp: i64,
    factors_used: u8
)
```
- Immutable authentication log
- Fraud detection and reputation tracking
- Batch aggregation via Merkle trees
- PSD3 compliance audit trail

**Benefits:**
- ✅ **Tamper-Proof:** Can't modify past enrollments/authentications
- ✅ **Transparency:** Anyone can verify authentication history
- ✅ **Decentralized:** No single point of failure
- ✅ **Cost-Effective:** Solana's low fees (~$0.00025 per transaction)

#### 3. **Zero-Knowledge Proofs (ZK-SNARKs)**

NoTap uses **Groth16 ZK-SNARKs** for privacy-preserving verification:

**What Gets Proven (without revealing details):**
- ✅ User authenticated successfully
- ✅ Required number of factors completed
- ✅ Factors match enrolled digests

**What Stays Private:**
- 🔒 Which specific factors were used
- 🔒 Factor values (PIN, pattern, etc.)
- 🔒 User UUID (hashed in proof)

**Circuit Design:**
```
Inputs (public):
  - Merkle root of enrolled digests
  - Required factor count
  - Timestamp

Inputs (private):
  - User UUID
  - Factor digests
  - Factor types
  - Merkle proof

Output (public):
  - Verification proof (192 bytes)
  - Valid/Invalid (boolean)
```

**Benefits:**
- ✅ **Privacy:** Merchant never sees which factors you used
- ✅ **Compliance:** Prove authentication without revealing data
- ✅ **Audit Trail:** Immutable proofs stored on-chain
- ✅ **Efficiency:** 192-byte proof vs full factor data

**Status:** ⏳ Ready for production (requires trusted setup ceremony)

#### 4. **Solana Name Service (SNS) Integration**

**Human-readable blockchain names** powered by Bonfida:

**How It Works:**
1. Register `alice.notap.sol` during enrollment
2. Backend creates subdomain under `notap.sol` parent
3. Name stored on Solana blockchain
4. Resolves to your NoTap UUID automatically

**On-Chain Data:**
```
Domain: alice.notap.sol
Owner: (Your Solana wallet or NoTap-managed)
Target: UUID a1b2c3d4-5678-90ab-cdef-1234567890ab
Parent: notap.sol
```

**Benefits:**
- ✅ **Decentralized:** Your name lives on Solana, not just NoTap servers
- ✅ **Transferable:** Can transfer ownership to another wallet
- ✅ **Verifiable:** Anyone can verify name → UUID mapping on-chain
- ✅ **Branded:** Professional `name.notap.sol` identity

### 🏗️ Blockchain Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   BLOCKCHAIN LAYER (Solana)                  │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │  Enrollment     │  │  Audit          │  │  SNS        │ │
│  │  Program        │  │  Program        │  │  Registry   │ │
│  │  (Rust/Anchor)  │  │  (Rust/Anchor)  │  │  (Bonfida)  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
│         │                      │                    │         │
│         │ Store commitments    │ Log auth events    │ Resolve │
│         │                      │                    │ names   │
└─────────┼──────────────────────┼────────────────────┼─────────┘
          │                      │                    │
          ▼                      ▼                    ▼
┌─────────────────────────────────────────────────────────────┐
│                   BACKEND API (Node.js)                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  • blockchainIntegrationService.js (600 LOC)                │
│  • didService.js (561 LOC)                                  │
│  • snsIntegrationService.js (1,126 LOC)                     │
│  • zkProofService.js (ZK-SNARK generation)                  │
│                                                               │
│  REST APIs:                                                  │
│  • /v1/did/document/:identifier  (DID resolution)           │
│  • /v1/sns/check-availability/:name                         │
│  • /v1/sns/resolve/:name                                    │
│  • /v1/blockchain/enrollment/:uuid                          │
│  • /v1/blockchain/audit/:uuid                               │
│                                                               │
└─────────────────────────────────────────────────────────────┘
          │                      │                    │
          ▼                      ▼                    ▼
┌─────────────────────────────────────────────────────────────┐
│                   CLIENT SDKS (Kotlin/Swift/JS)              │
├─────────────────────────────────────────────────────────────┤
│  • SNSClient.kt (4 methods, 21 unit tests)                  │
│  • EnrollmentClient.kt (SNS registration step)              │
│  • VerificationClient.kt (SNS name resolution)              │
└─────────────────────────────────────────────────────────────┘
```

### 📊 Blockchain Integration Status

| Component | Status | LOC | Files |
|-----------|--------|-----|-------|
| **Smart Contracts** | ✅ Complete | 770 | 2 Rust programs |
| **DID Service** | ✅ Complete | 561 | didService.js |
| **SNS Integration** | ✅ Complete | 1,126 | snsIntegrationService.js |
| **Blockchain Service** | ✅ Complete | 600 | blockchainIntegrationService.js |
| **ZK-SNARK Circuit** | ⏳ Ready | 475 | setup-automated.sh |
| **SDK Client (SNS)** | ✅ Complete | 192 + 550 tests | SNSClient.kt + tests |
| **UI Components** | ✅ Complete | 469 | SNSRegistrationStep.kt |
| **Documentation** | ✅ Complete | ~3,000 | 6 markdown files |
| **TOTAL** | **~85% Complete** | **~8,200 LOC** | **20+ files** |

**Remaining Work:**
1. ⏳ **Deploy smart contracts** to Solana devnet/mainnet
2. ⏳ **Run trusted setup ceremony** for ZK-SNARK production keys
3. ⏳ **Register parent domain** `notap.sol` with Bonfida
4. ⏳ **Enable SNS** in backend (set `SNS_ENABLED=true`)

**All code is ready - just needs deployment configuration!**

### 🚀 Enabling Blockchain Features

```bash
# 1. Deploy smart contracts to Solana devnet
cd programs
./deploy.sh --network devnet

# 2. Run ZK-SNARK trusted setup (10 minutes)
cd circuits
./setup-automated.sh --mode full

# 3. Enable SNS in backend
cd backend
echo "SNS_ENABLED=true" >> .env
echo "PARENT_DOMAIN=notap.sol" >> .env
echo "SOLANA_RPC_URL=https://api.devnet.solana.com" >> .env

# 4. Run database migration
psql $DATABASE_URL < database/migrations/005_add_sns_subdomain_support.sql

# 5. Restart backend
npm run dev
```

**Test SNS Registration:**
```bash
# Check availability
curl https://api.notap.io/v1/sns/check-availability/alice

# Response:
{
  "success": true,
  "data": {
    "name": "alice",
    "fullName": "alice.notap.sol",
    "available": true,
    "suggestions": []
  }
}
```

---

## 🚀 For Merchants: Why Integrate NoTap?

### Reduce Cart Abandonment
- **30% of transactions fail** during authentication (Source: Forter)
- **$443B in falsely declined transactions** annually
- **NoTap provides backup** when primary payment fails

### Attract New Customers
- **Emergency payment capability** → "We accept NoTap" signage
- **Fitness/lifestyle customers** → Gym-goers, beach visitors, joggers
- **Tourist-friendly** → Travelers without phones/wallets

### PSD3 SCA Compliance (Europe)
- **Mandatory by 2026** for all EU payment processors
- **NoTap exceeds requirements** (15 factors vs. 2 minimum)
- **Reduce compliance burden** with plug-and-play solution

### Integration Options

**🔌 E-Commerce Plugins:**
- Shopify ($9.99/month)
- WooCommerce ($9.99/month)
- Magento (custom pricing)

**💻 Direct API Integration:**
- Stripe, Adyen, Square, PayPal
- RESTful API + SDKs (iOS, Android, Web)
- Full white-label available

**💰 Pricing:**
- **$0.15-0.50 per verification** (or merchant-paid subscription)
- **No setup fees** for standard integrations
- **Revenue share available** for payment processors

---

## 💻 For Developers: Quick Start

### Installation

```bash
# Android
dependencies {
    implementation("com.zeropay:sdk:1.0.0")
    implementation("com.zeropay:enrollment:1.0.0")  // Optional
    implementation("com.zeropay:merchant:1.0.0")    // Optional
}

# iOS (Coming Soon)
pod 'NoTapSDK', '~> 1.0'

# Web (JavaScript/TypeScript)
npm install @notap/sdk
```

### Basic Usage (User Enrollment)

```kotlin
// Android
val noTap = NoTap(context, NoTapConfig(
    baseUrl = "https://api.notap.io",
    enableBiometrics = true
))

// Start enrollment
noTap.enrollment.enroll(
    factors = listOf(Factor.PIN, Factor.PATTERN, Factor.EMOJI,
                     Factor.RHYTHM_TAP, Factor.COLORS, Factor.WORDS),
    paymentProvider = PaymentProvider.STRIPE,
    onSuccess = { noTapId ->
        println("Enrolled! Your NoTap ID: $noTapId")
    },
    onError = { error ->
        println("Enrollment failed: ${error.message}")
    }
)
```

### Basic Usage (Merchant Verification)

```kotlin
// Merchant app
noTap.verification.verify(
    noTapId = "user-notap-id",
    amount = 49.99,
    currency = "USD",
    onSuccess = { result ->
        if (result.verified) {
            processPayment(result.paymentToken)
        }
    },
    onError = { error ->
        showError(error.message)
    }
)
```

### Web Integration

```javascript
// JavaScript/TypeScript
import NoTap from '@notap/sdk';

const noTap = new NoTap({
  apiKey: 'your-api-key',
  environment: 'production'
});

// Enroll user
await noTap.enroll({
  factors: ['pin', 'pattern', 'emoji', 'rhythm', 'colors', 'words'],
  paymentProvider: 'stripe',
  onSuccess: (noTapId) => console.log(`NoTap ID: ${noTapId}`)
});

// Verify payment
const result = await noTap.verify({
  noTapId: 'user-notap-id',
  amount: 49.99,
  currency: 'USD'
});

if (result.verified) {
  processPayment(result.paymentToken);
}
```

**📚 Full Documentation:** [https://docs.notap.io](https://docs.notap.io)

---

## 📊 Market & Traction

### Market Size
- **$58B Total Addressable Market** (emergency payments, zero-trust devices, lifestyle)
- **280M lost/stolen cards annually** worldwide
- **68% of users** experience dead phone battery requiring purchase
- **500M fitness wearable users** globally (potential empty-handed use case)

### Regulatory Drivers
- **PSD3 SCA (2026):** Mandatory strong customer authentication in EU
- **eIDAS 2.0 (2026):** Digital identity wallets required
- **GDPR Compliance:** NoTap designed with privacy-first architecture

### Current Status
- ✅ **Android SDK:** Production-ready
- 🚧 **iOS SDK:** In development (Q1 2026)
- ✅ **Web SDK:** Production-ready (enrollment + verification flows complete)
- ✅ **Backend API:** Production-ready (Node.js + Redis + PostgreSQL)
- ✅ **Developer Portal:** Production-ready (self-service integration)
- ✅ **Management Portal:** Production-ready (self-service account management)
- ✅ **Multi-Chain Names:** Production-ready (ENS, Unstoppable, BASE, SNS)
- ✅ **E2E Testing:** Bugster integration complete
- 🚧 **Partnerships:** Tilopay integration in progress (LATAM market)

---

## 🔧 Technical Architecture

> **🏷️ Note:** NoTap is the public brand name. Code/packages use `zeropay` for API stability (like Meta/Facebook, Twitter/X).

### Kotlin Multiplatform (KMP) Structure

NoTap is built with **Kotlin Multiplatform** for 95%+ code reuse across platforms:

```
zeropay-android/
├── sdk/                    # Core SDK (KMP)
│   ├── commonMain/        # Platform-agnostic (crypto, factors, network)
│   ├── androidMain/       # Android-specific (UI, KeyStore, biometrics)
│   └── iosMain/           # iOS-specific (planned)
│
├── enrollment/            # User enrollment module
├── merchant/              # Merchant verification module
├── online-web/            # Web SDK (Kotlin/JS)
└── backend/               # Node.js API server
```

**Key Technologies:**
- **Kotlin Multiplatform** (95% code reuse)
- **Jetpack Compose** (Android UI)
- **SwiftUI** (iOS UI - planned)
- **Kotlin/JS** (Web)
- **Redis** (TLS-encrypted caching, 24h TTL)
- **PostgreSQL** (KMS-wrapped key storage + SNS names)
- **AWS KMS** (key wrapping)
- **Solana Blockchain** (smart contracts, DID, SNS, ZK-SNARKs)
- **Rust/Anchor** (smart contract development)
- **Bonfida SNS** (human-readable blockchain names)

### Data Storage Strategy

| Storage | Purpose | Data Stored | Encryption | TTL |
|---------|---------|-------------|------------|-----|
| **Device KeyStore** | Primary | NoTap ID + cached digests | Hardware-backed | Permanent |
| **Backend Redis** | Secondary cache | Encrypted digests | AES-256-GCM + TLS | 24 hours |
| **Backend PostgreSQL** | Key storage + SNS | KMS-wrapped keys + SNS names | AWS KMS | Permanent |
| **Solana Blockchain** | Audit trail | Enrollment commitments, auth events, SNS names | Public (hashed UUIDs) | Immutable |

**Privacy Guarantee:** Only cryptographic hashes (SHA-256 digests) are stored. **Never** raw biometric data, PINs, or patterns. Blockchain stores hashed UUIDs only (not identifiable).

---

## 🔒 Security & Compliance

### Security Audit Results (November 2025)

**✅ Comprehensive security audit completed:**
- **15 critical vulnerabilities found and fixed** (100% remediation)
- **Timing attack prevention** across all 12 authentication factors
- **Secure random number generation** (replaced Math.random())
- **Deadlock prevention** (fixed concurrency bug)
- **600+ lines of code** hardened for security

### Compliance Standards

| Standard | Status | Notes |
|----------|--------|-------|
| **PSD3 SCA** | ✅ Compliant | 15 factors across 5 categories (exceeds requirement) |
| **GDPR** | ✅ Compliant | 24h TTL, right to erasure, consent tracking |
| **OWASP Top 10** | ✅ Mitigated | SQL injection, XSS, CSRF, timing attacks |
| **NIST Crypto** | ✅ Compliant | SHA-256, PBKDF2, AES-256-GCM, SecureRandom |
| **SOC 2 Type II** | 🚧 In Progress | Backend infrastructure audit Q1 2026 |

### Threat Model

NoTap protects against:
- ✅ **Timing Attacks** (constant-time comparison)
- ✅ **Replay Attacks** (nonce validation, session expiry)
- ✅ **Man-in-the-Middle** (TLS 1.3, certificate pinning)
- ✅ **Brute-Force** (rate limiting, account lockout)
- ✅ **Memory Dumps** (memory wiping, secure storage)
- ✅ **Device Tampering** (anti-root/jailbreak detection, debug mode detection)
- ✅ **Merchant Device Hijacking** (session binding, time-limited auth, geographic binding)

---

## 📱 Platform Support

| Platform | Status | SDK Available | UI Framework |
|----------|--------|---------------|--------------|
| **Android** | ✅ Production | Yes | Jetpack Compose |
| **iOS** | 🚧 Q1 2026 | In Development | SwiftUI |
| **Web** | ✅ Production | Yes | Kotlin/JS + HTML5 |
| **Desktop** | ⏳ Planned | Q2 2026 | Compose Multiplatform |

**Minimum Requirements:**
- **Android:** 7.0 (API 24) or higher
- **iOS:** 14.0 or higher (planned)
- **Web:** Chrome 90+, Firefox 88+, Safari 14+

---

## 🤝 Partnerships & Integrations

### Payment Processors
- ✅ **Stripe** (plugin ready)
- ✅ **Adyen** (enterprise integration)
- 🚧 **Tilopay** (LATAM partnership in progress)
- ⏳ **Square, PayPal, Braintree** (planned)

### E-Commerce Platforms
- 🚧 **Shopify** (app store submission Q1 2026)
- 🚧 **WooCommerce** (plugin marketplace Q1 2026)
- ⏳ **Magento, PrestaShop, BigCommerce** (planned)

### Blockchain Wallets
- ✅ **Phantom** (Solana wallet integration)
- ⏳ **MetaMask** (Ethereum - planned)
- ⏳ **Coinbase Wallet** (planned)

---

## 🗺️ Roadmap

### Q4 2025 (Completed)
- [x] Android SDK production release
- [x] Backend API production release
- [x] Security audit (26 vulnerabilities fixed)
- [x] Web SDK production release (enrollment + verification)
- [x] Developer Portal (self-service integration)
- [x] Management Portal (self-service account management)
- [x] Multi-chain name service (ENS, Unstoppable, BASE, SNS)
- [x] Alias system (memorable IDs)
- [x] Bugster E2E testing integration
- [x] Billing infrastructure (B2B/B2C pricing)
- [x] ZK-SNARK circuit implementation (awaiting trusted setup)

### Q1 2026 (Current)
- [ ] iOS SDK release
- [ ] ZK-SNARK trusted setup ceremony
- [ ] SOC 2 Type II certification
- [ ] Tilopay partnership (LATAM)
- [ ] Shopify/WooCommerce plugins
- [ ] 10 merchant pilots (gyms, cafes, airports)
- [ ] 1,000 user enrollments

### Q2 2026
- [ ] ZK-SNARK full implementation
- [ ] Biometric liveness detection
- [ ] Multi-device synchronization
- [ ] Ethereum/MetaMask integration

### Q3-Q4 2026
- [ ] Desktop SDK (Compose Multiplatform)
- [ ] Payment provider OAuth (full implementation)
- [ ] Hardware security module (HSM) integration
- [ ] Series A fundraising

---

## 📚 Documentation

### Core Documentation
- **[Investment Analysis](documentation/08-business/INVESTMENT_ANALYSIS_REVISED.md)** - Market research, competitive analysis, financials
- **[Implementation Plan](documentation/10-internal/ZEROPAY_IMPLEMENTATION_PLAN_v2.md)** - 30-day development roadmap
- **[Development Guide](documentation/03-developer-guides/development_guide.md)** - Architecture, patterns, best practices
- **[API Documentation](https://docs.notap.io/api)** - REST API reference
- **[Integration Guides](https://docs.notap.io/integrations)** - Platform-specific guides

### Blockchain & SNS Documentation
- **[Blockchain Integration Guide](documentation/03-developer-guides/BLOCKCHAIN_INTEGRATION_GUIDE.md)** - Smart contracts, DID, ZK-SNARKs
- **[DID & SNS Guide](documentation/03-developer-guides/DID_SNS_INTEGRATION_GUIDE.md)** - Decentralized identity, human-readable names
- **[SNS Implementation](documentation/11-legacy/SNS_SUBDOMAIN_IMPLEMENTATION.md)** - Technical details of SNS integration
- **[Blockchain Progress Report](documentation/09-analysis/BLOCKCHAIN_PROGRESS_REPORT.md)** - Implementation status
- **[SNS Code Verification](documentation/07-testing/SNS_CODE_VERIFICATION.md)** - Pattern verification and test coverage

### Alias System Documentation
- **[Alias System Guide](documentation/04-architecture/ALIAS_SYSTEM.md)** - Complete alias architecture, word dictionaries, multi-language support
- **[Alias Implementation Summary](documentation/09-analysis/ALIAS_IMPLEMENTATION_SUMMARY.md)** - Quick reference for deployment and testing

### Multi-Chain Name Service Documentation
- **[Multi-Chain Quick Start](documentation/01-getting-started/MULTICHAIN_NAME_SERVICE_QUICKSTART.md)** - Quick start guide for ENS, Unstoppable, BASE, SNS
- **[Multi-Chain Implementation](documentation/04-architecture/MULTICHAIN_NAME_SERVICE_IMPLEMENTATION.md)** - Architecture and technical details
- **[Multi-Chain Next Steps](documentation/10-internal/MULTICHAIN_NEXT_STEPS.md)** - Implementation roadmap

### Developer Portal Documentation
- **[Developer Portal API](backend/docs/DEVELOPER_PORTAL_API.md)** - Complete API reference for developer portal
- **Developer Portal User Guide** - Coming soon

### Web Platform Documentation
- **[Web Verification Flow Architecture](online-web/VERIFICATION_FLOW_ARCHITECTURE.md)** - Complete web verification architecture
- **Web Enrollment Guide** - Coming soon

### Testing Documentation
- **[Bugster Integration Guide](documentation/03-developer-guides/BUGSTER_INTEGRATION.md)** - E2E testing setup and configuration
- **[Bugster Quick Start](documentation/01-getting-started/BUGSTER_QUICKSTART.md)** - Getting started with Bugster tests

---

## 🧪 Testing

### Test Types

NoTap uses **comprehensive testing strategy** across unit, integration, and end-to-end tests:

#### Unit Tests (SDK & Backend)
```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :sdk:test
./gradlew :enrollment:test
./gradlew :merchant:test

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Backend unit tests
cd backend && npm test
```

#### Integration Tests (API)
```bash
# Backend API tests
cd backend && npm run test:integration

# Test coverage
npm run test:coverage
```

#### End-to-End Tests (Bugster) ⭐ NEW!

**NEW in v2.0:** AI-powered browser-based E2E testing with Bugster.

**Features:**
- **Real browser automation** → Tests actual user flows in Chrome/Firefox
- **AI-powered destructive testing** → Bugster AI agent finds edge cases
- **6 backend test specs:**
  - Enrollment flow (5-step wizard)
  - Verification flow (UUID/alias/blockchain name input)
  - Wallet management
  - ZK-SNARK proof generation
  - GDPR compliance (data export, deletion)
  - Admin audit logs
- **10 web UI test specs** → All factor canvases (PIN, pattern, emoji, color, etc.)
- **GitHub Actions integration** → Automated testing on every PR
- **Visual regression testing** → Screenshot comparison

**Running Bugster Tests:**
```bash
# Install Bugster CLI
npm install -g @bugster/cli

# Backend API E2E tests
bugster run --config bugster.config.yaml

# Web UI E2E tests
bugster run --config bugster.config.web.yaml

# AI destructive testing (finds edge cases)
bugster agent --mode destructive --duration 30m

# Run specific test suite
bugster run --config bugster.config.yaml --suite enrollment
```

**GitHub Actions Workflow:**
```yaml
# Automatically runs on every PR
- Backend E2E tests (5-10 minutes)
- Web UI E2E tests (10-15 minutes)
- Destructive testing (30 minutes, nightly)
```

**Test Coverage:**
- ✅ **38 unit test files** across SDK, enrollment, backend
- ✅ **Unit tests** for all 15 authentication factors
- ✅ **Integration tests** for enrollment/verification flows
- ✅ **Security tests** (timing attacks, constant-time, memory wiping)
- ✅ **6 Bugster backend specs** (real API flows)
- ✅ **10 Bugster web UI specs** (real browser testing)
- ✅ **AI destructive testing** (edge case discovery)

**Total Test Coverage:** ~85% code coverage (unit + integration + E2E)

---

## 🤝 Contributing

We welcome contributions! NoTap is built with:
- **Kotlin Multiplatform** (cross-platform code sharing)
- **Jetpack Compose** (Android UI)
- **Ktor** (networking)
- **Redis** (caching)
- **PostgreSQL** (persistent storage)

**Before contributing:**
1. Read [CONTRIBUTING.md](./CONTRIBUTING.md)
2. Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
3. Ensure **constant-time operations** for crypto comparisons
4. Always **wipe sensitive data** from memory
5. Write tests for new features

**Security Issues:** Report privately to security@notap.io

---

## 📞 Support & Community

- **🌐 Website:** [https://notap.io](https://notap.io)
- **📖 Documentation:** [https://docs.notap.io](https://docs.notap.io)
- **💬 Discussions:** [GitHub Discussions](https://github.com/keikworld/zero-pay-sdk/discussions)
- **🐛 Issues:** [GitHub Issues](https://github.com/keikworld/zero-pay-sdk/issues)
- **📧 General Inquiries:** hello@notap.io
- **🛟 Technical Support:** support@notap.io
- **🤝 Partnerships:** partnership@notap.io
- **⚖️ Appeals:** appeals@notap.io
- **📜 Code of Conduct:** conduct@notap.io
- **🐦 X/Twitter:** [@NoTapAuth](https://x.com/NoTapAuth)
- **💼 LinkedIn:** [NoTap](https://linkedin.com/company/notap)
- **🆔 Solana Name Service:** notap.sol

> **💡 Brave Browser Users:** Type `notap.io` or `notap.sol` in the address bar - both take you to our website!

---

## ⚖️ License

Licensed under the Apache License 2.0 - see [LICENSE](LICENSE) file.

**Commercial Use:** Permitted with attribution. Contact us for white-label licensing.

---

## 🙏 Acknowledgments

Built with love using:
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) - Cross-platform development
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI
- [Ktor](https://ktor.io/) - Asynchronous HTTP networking
- [Redis](https://redis.io/) - High-performance caching
- [PostgreSQL](https://www.postgresql.org/) - Reliable persistent storage
- [Solana](https://solana.com/) - Blockchain integration

**Special thanks** to our early adopters, security auditors, and open-source contributors.

---

<div align="center">
  <p><strong>Made with ❤️ by the NoTap Team</strong></p>
  <p>
    <a href="https://notap.io">Website</a> •
    <a href="https://docs.notap.io">Documentation</a> •
    <a href="https://github.com/keikworld/zero-pay-sdk">GitHub</a> •
    <a href="mailto:hello@notap.io">Contact</a>
  </p>
  <p><em>💡 Brave users: notap.sol works too!</em></p>
</div>


---

## 📚 Documentation

Comprehensive guides and references available:

### Getting Started
- [Quick Start Guide](docs/getting-started/) - Get up and running in 5 minutes
- [Installation](docs/getting-started/) - Detailed installation instructions
- [First Authentication](docs/getting-started/) - Your first NoTap integration

### Integration Guides
- [Android Integration](docs/developer-guides/) - Native Android SDK integration
- [iOS Integration](docs/developer-guides/) - Native iOS SDK integration
- [Web Integration](docs/web-integration/) - JavaScript/Web integration
- [Backend API](docs/api-reference/) - RESTful API documentation

### Architecture & Security
- [Architecture Overview](docs/architecture/) - System architecture and design
- [Security Best Practices](docs/security/) - Security guidelines and threat model
- [Authentication Flow](docs/architecture/) - How NoTap authentication works

### Testing
- [Testing Guide](docs/testing/) - How to test your integration
- [E2E Testing](docs/testing/) - End-to-end testing with Bugster

---

## 🚀 Quick Start

### 1. Install the SDK

**Android (Gradle):**
```gradle
dependencies {
    implementation 'xyz.notap:sdk:1.0.0'
}
```

**iOS (CocoaPods):**
```ruby
pod 'NoTapSDK', '~> 1.0.0'
```

**Web (NPM):**
```bash
npm install @notap/sdk
```

### 2. Initialize NoTap

**Android:**
```kotlin
val noTap = NoTapClient(
    apiKey = "ntpk_live_...",
    environment = Environment.PRODUCTION
)
```

**iOS:**
```swift
let noTap = NoTapClient(
    apiKey: "ntpk_live_...",
    environment: .production
)
```

**Web:**
```javascript
const noTap = new NoTapClient({
    apiKey: 'ntpk_live_...',
    environment: 'production'
});
```

### 3. Authenticate a User

```kotlin
// Initiate authentication
val session = noTap.verification.initiate(
    userIdentifier = "alice.notap.sol", // UUID, Alias, or SNS name
    transactionAmount = 49.99
)

// Present factors to user and verify
val result = noTap.verification.verify(
    sessionId = session.id,
    factors = userProvidedFactors
)

if (result.success) {
    // ✅ User authenticated!
    processPayment(result.authToken)
}
```

**That's it!** See our [Developer Guides](docs/developer-guides/) for complete integration tutorials.

---

## 🎯 Use Cases

### 🛒 Point of Sale (POS)
- **Device-free payments:** Customer left phone at home? No problem!
- **Faster checkout:** No fumbling with phones or cards
- **Reduced fraud:** Multi-factor authentication with ZK proofs

### 💻 E-Commerce
- **Passwordless login:** No more password resets
- **One-click checkout:** Authenticate with your chosen factors
- **Cross-device:** Start on phone, finish on desktop

### 🏦 Banking & Finance
- **High-security transactions:** Multi-factor + zero-knowledge proofs
- **Regulatory compliance:** PSD3-ready authentication
- **Fraud prevention:** Behavioral biometrics + knowledge factors

### 🏢 Enterprise
- **SSO Integration:** Works with existing identity providers
- **Admin controls:** Manage users and permissions
- **Audit trails:** Complete authentication history

---

## 🔐 Security

NoTap is built with security at its core:

- **🔐 Zero-Knowledge Proofs:** Prove you know your factors without revealing them
- **🔒 End-to-End Encryption:** Factors encrypted on device, never sent in plain text
- **⏱️ Constant-Time Operations:** Protection against timing attacks
- **🛡️ PSD3 Compliant:** Multi-category authentication (knowledge, biometric, possession)
- **🔑 Hardware Security:** Android KeyStore, iOS Keychain integration
- **📊 Security Audits:** Regular third-party security audits

**See:** [Security Documentation](docs/security/) for complete security architecture.

---

## 🌐 Supported Platforms

| Platform | Status | Minimum Version |
|----------|--------|-----------------|
| **Android** | ✅ Production Ready | Android 8.0 (API 26) |
| **iOS** | ✅ Production Ready | iOS 14.0+ |
| **Web** | ✅ Production Ready | Modern browsers (ES6+) |
| **Backend API** | ✅ Production Ready | REST API |

---

## 🤝 Contributing

We welcome contributions from the community!

### How to Contribute

1. **Found a bug?** [Open an issue](https://github.com/NoTap-Labs/NoTap-SDK/issues)
2. **Have a feature request?** [Start a discussion](https://github.com/NoTap-Labs/NoTap-SDK/discussions)
3. **Want to improve docs?** Submit a pull request!

### Documentation Contributions

This repository contains **public documentation only**. Documentation is automatically synced from our development repository.

To contribute:
- **Documentation improvements:** Submit PRs directly to this repo
- **Code changes:** Contact us at dev@notap.xyz for contributor access

See our [Contributing Guide](CONTRIBUTING.md) for detailed guidelines.

---

## 💬 Community & Support

### Get Help

- 📧 **Email:** support@notap.xyz
- 💬 **Discord:** [Join our community](https://discord.gg/notap)
- 📖 **Documentation:** [docs.notap.xyz](https://docs.notap.xyz)
- 🐛 **Bug Reports:** [GitHub Issues](https://github.com/NoTap-Labs/NoTap-SDK/issues)

### Stay Updated

- 🐦 **Twitter:** [@NoTapAuth](https://twitter.com/NoTapAuth)
- 📝 **Blog:** [blog.notap.xyz](https://blog.notap.xyz)
- 📬 **Newsletter:** [Subscribe](https://notap.xyz/newsletter)

---

## 📄 License

Copyright © 2025 NoTap Labs. All rights reserved.

This documentation is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).

For SDK licensing, contact: licensing@notap.xyz

---

## 🏷️ About the Name

**NoTap** is our public brand name. Internally, the codebase uses "zeropay" - this is intentional and follows industry standards (like Meta/Facebook, Google/Alphabet). This enables us to rebrand without breaking existing integrations.

**For developers:** Use package names like `xyz.notap.sdk` in your apps, even though internal packages may reference `zeropay`.

---

<div align="center">

**Made with ❤️ by the NoTap Labs team**

[Website](https://notap.xyz) • [Docs](https://docs.notap.xyz) • [Discord](https://discord.gg/notap) • [Twitter](https://twitter.com/NoTapAuth)

</div>
