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

<div align="center">

# NoTap

**Authentication Reimagined**

*Your identity is a master key that opens any door — but it lives in your mind, not your pocket.*

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9+-blue.svg)](https://kotlinlang.org)
[![PSD3 SCA Compliant](https://img.shields.io/badge/PSD3-SCA%20Compliant-green.svg)](https://www.ecb.europa.eu)

</div>

---

## The Master Key Idea

Imagine your identity as a **master key** — it opens any door (any device), without you having to carry it physically.

**A physical key** requires you to carry it. Lose it? You're locked out.
**Your phone** is just another physical key. Dead battery? Stolen? You're locked out.
**NoTap** puts the key in your mind. It can't be stolen, lost, or run out of battery.

```
PHYSICAL KEY                         YOUR NOTAP
────────────                         ──────────
Lose it → locked out                 Can't lose what's in your mind
Someone steals it → they have access Can't steal your memory
Only opens specific doors            Opens any door (any device)
```

**Your phone number works from any phone** — your phone, a friend's phone, a hotel phone. The number follows you, not the device.

**NoTap works the same way.** Your PIN, your pattern, your rhythm — they follow you. Use them on any device: a store's tablet, a friend's laptop, a hospital kiosk, any browser.

**Enroll once on your device. Authenticate anywhere without it.**

---

## What NoTap Is (And Isn't)

### We Are: The Bouncer at the Club

**NoTap is an authentication service.** Think of us as the bouncer at a nightclub:

| The Bouncer | NoTap |
|-------------|-------|
| Checks your ID | Verifies your identity through multiple factors |
| Confirms you're on the list | Confirms you are who you claim to be |
| Lets you in | Grants access |
| **Doesn't serve drinks** | **Doesn't process payments** |

**Our job is answering one question: "Is this person who they claim to be?"**

### We Enable: Authentication Without Your Phone

**The fundamental problem with modern authentication:**

| Method | Requires |
|--------|----------|
| SMS codes | Your phone |
| Authenticator apps | Your phone |
| Push notifications | Your phone |
| Face ID / Touch ID | Your phone |

**NoTap solution:** Authenticate on **any device** — the merchant's POS, a kiosk, a web browser, a friend's phone. Your authentication factors live in **your memory**, not a device.

### We Are NOT Competing With:

| Company/Service | What They Do | What NoTap Does |
|-----------------|--------------|-----------------|
| **Apple Pay / Google Pay** | Payment processing | We **integrate** with them |
| **Face ID / Secure Enclave** | Device biometrics | We **use** them when available |
| **Stripe / Adyen / Square** | Payment processing | We **hand off** to them |
| **Hardware biometric scanners** | Fingerprint/face readers | We **leverage** their infrastructure |

### How It Fits Together

```
┌─────────────────────────────────────────────────────────────┐
│                    USER AUTHENTICATION                       │
│                         (NoTap)                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  "Is this person who they claim to be?"                 │ │
│  │  • 3-15 authentication factors                          │ │
│  │  • Works on ANY device (no phone required!)             │ │
│  │  • Uses device biometrics when available                │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↓
                     ✅ Identity Verified
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    PAYMENT PROCESSING                        │
│              (Stripe, Apple Pay, Adyen, etc.)               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  "Process this $49.99 transaction"                      │ │
│  │  • Your chosen payment provider handles the money       │ │
│  │  • We don't touch funds, ever                          │ │
│  │  • Users keep their preferred payment methods           │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

**Users keep their preferred services:**
- Already use Apple Pay? Great — we authenticate, Apple Pay processes
- Prefer Google Wallet? No problem — we verify identity, Google handles payment
- Love Samsung Pay? Perfect — we confirm it's you, Samsung does the rest

---

## The Problem NoTap Solves

**You need to make a purchase or access something secure, but:**

- 📱 Your phone was **stolen** or the **battery died**
- 💳 You **forgot your wallet** at the hotel
- 🏃 You're at the **gym** and left everything in your locker
- 🏖️ You're at the **beach** and don't want to risk losing your phone
- 🎢 You're at a **theme park** and want to store everything safely
- 🚕 Your **credit card was declined** and you need a taxi home
- 🏥 You're a **nurse** with phone locked away (hospital rules)
- 💻 You need to pay on an **untrusted device** (public computer, friend's phone)

**Traditional solutions all fail the same way:**

| Solution | The Problem |
|----------|-------------|
| **Apple Pay / Google Pay** | Requires your phone |
| **Credit Cards** | Requires your wallet |
| **SMS 2FA** | Requires your phone |
| **Authenticator Apps** | Requires your phone |
| **Amazon One** | Only works at Amazon/Whole Foods |
| **Cash** | Requires you to carry cash |

**Every solution requires you to CARRY something.** NoTap requires only what you already have: your memory.

---

## "Wait, Is This Complicated?"

**No. Here's why NoTap is actually EASIER than traditional passwords:**

| Your Concern | The Reality |
|--------------|-------------|
| **"15 factors sounds overwhelming!"** | You **choose 3-6** factors. System asks for only **2-3 per transaction** (NOT all of them!) |
| **"Too much to remember!"** | **Diversity = easier**: 4 colors (red, blue, green, yellow) is easier than "P@ssw0rd123!" |
| **"Takes too long!"** | **10-30 seconds total**: Coffee = 2 factors (10 sec), Groceries = 3 factors (25 sec) |
| **"What if I forget one?"** | **Forgiving system**: Wrong factor? System asks for different ones. Not locked out! |
| **"I'm bad at memorizing!"** | **Biometrics available**: Fingerprint/face = zero memorization. Pattern/rhythm = muscle memory. |

### Real Example

**You enroll 6 factors** (one-time, 10 minutes):
PIN + Pattern + Emoji + Rhythm + Fingerprint + Colors

**Daily usage** (every transaction):

| Coffee ($4) | Lunch ($15) | Groceries ($75) |
|-------------|-------------|-----------------|
| PIN + Fingerprint | Pattern + Colors | Rhythm + Emoji + Fingerprint |
| **10 seconds** | **15 seconds** | **25 seconds** |

**You NEVER complete all 6 factors in one transaction.** The system picks 2-3 based on purchase amount.

---

## How It Works

### Step 1: Enroll Once (5 minutes, on your device)

Pick 3+ things only you know or do (6+ recommended):

| Factor | What It Is | Example |
|--------|------------|---------|
| **PIN** | 4-12 digits you choose | `4829` |
| **Pattern** | Shape you draw | Like an unlock pattern, but timed |
| **Colors** | Sequence of colors | Red → Blue → Green → Yellow |
| **Emoji** | Sequence of emoji | 🌙 → 🎸 → 🍕 → 🚀 |
| **Rhythm** | Tap pattern you create | *tap-tap---tap-tap-tap* |
| **Words** | 4 words you pick | ocean-tiger-melody-spark |

**Optional:** Add fingerprint or face for extra security when your phone IS available.

These get turned into math (cryptographic digests). The actual values never leave your phone. We only store the "puzzle" — not the answer.

### Step 2: Get Your NoTap Name

Choose how you want to be identified:

| Type | Example | Best For |
|------|---------|----------|
| **Alias** | `tiger-4829` | Easy to remember, say out loud |
| **Blockchain Name** | `alice.notap.sol` | Professional, like an email |
| **UUID** | `a1b2c3d4-5678-90ab...` | Maximum security |

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

## Real Stories: When This Matters

### 🔋 The Dead Phone

> *You're at the airport. Flight boards in 20 minutes. Phone battery: 0%. You need coffee.*
>
> **Before NoTap:** Beg strangers for a charger. Miss your flight.
>
> **With NoTap:** Walk to the cafe. Say "tiger-4829". Enter your PIN on their tablet. Tap your rhythm. Coffee paid. **15 seconds.**

### 🏥 The Locked-Out Nurse

> *Your phone is in your locker (hospital rules). A patient needs medication from the secure cabinet. Now.*
>
> **Before NoTap:** Run to locker. Unlock phone. Get 2FA code. Run back. Critical minutes lost.
>
> **With NoTap:** Walk to cabinet. Type your NoTap ID. Draw your pattern. Cabinet unlocks. **Patient gets medication.**

### 🏖️ The Beach Day

> *Phone locked in the car (smart). Kids want ice cream.*
>
> **Before NoTap:** Walk back to car. Get phone. Walk back. Ice cream melted.
>
> **With NoTap:** Walk to vendor. Use their device. Authenticate. **Ice cream for everyone.**

### 🎢 The Theme Park

> *You're at a theme park with the family. You want to store everything in a locker — phone, wallet, everything — and just enjoy the rides worry-free.*
>
> **Before NoTap:** Keep your phone in a zippered pocket. Worry about it on every roller coaster.
>
> **With NoTap:** Lock everything away. Buy lunch, souvenirs, whatever — **completely hands-free.** Enjoy the day.

### 🚕 The Declined Card

> *It's 2 AM. You're stranded. Your credit card was declined. You need a taxi home.*
>
> **Before NoTap:** Call a friend to send money. Wait 30 minutes. Feel helpless.
>
> **With NoTap:** Open the taxi's payment tablet. Authenticate with your backup payment method. **Get home safe.**

### 🏃 The Post-Workout Smoothie

> *Finished your workout. Wallet and phone locked in the locker. Just want a smoothie.*
>
> **Before NoTap:** Go back to locker. Get dressed. Get phone. Come back. The moment's gone.
>
> **With NoTap:** Walk to the counter. Use their tablet. PIN + pattern. **Smoothie paid. Stay in the zone.**

---

## Two Operating Modes

| Mode | What Happens | Use Case |
|------|--------------|----------|
| **Authentication Only** | NoTap verifies identity → Grant access | Building entry, server login, secure cabinet |
| **Auth + Payment** | NoTap verifies identity → PSP processes payment | Store checkout, restaurant bill, online purchase |

### Authentication Mode Use Cases

- **Enterprise:** Building entry, computer login, secure rooms, time clocks
- **Healthcare:** HIPAA-compliant device-free login, prescription verification, lab equipment access
- **Finance:** ATM without card, wire transfer approval, vault access
- **Education:** Campus access, exam authentication, dorm entry

---

## Security (Without the Jargon)

**What we store:** A math puzzle that only your factors can solve.

**What we DON'T store:** Your actual PIN, pattern, colors, or words.

**How it works:**
1. You enter your PIN on any device
2. That device turns it into a math result (a "digest")
3. We check if the math result matches — without ever seeing your PIN

It's like a teacher checking if your math homework answer is correct, without seeing your work.

### Security Features

| Feature | What It Does |
|---------|--------------|
| **Double Encryption** | PBKDF2 + AWS KMS |
| **24-Hour Expiry** | Factors auto-refresh daily |
| **Factor Shuffling** | Different factors asked each time |
| **Risk-Based Auth** | $5 coffee = 2 factors, $100 purchase = 3 factors |
| **Zero-Knowledge Proofs** | Merchant never sees which factors you used |
| **Constant-Time Operations** | Prevents timing attacks |

### If Your Phone Is Stolen

**Traditional auth:** Attacker has your 2FA forever until you notice.

**NoTap:**
- Your authentication factors live in your memory, not your phone
- Stored digests expire in 24 hours with daily rotation
- After 24h: Attacker is **completely locked out**

---

## Available Factors (15 Total)

You choose 3+ from these:

### Knowledge (Something You Know)
- **PIN** — 4-12 digits
- **Colors** — Sequence of 3-6 colors
- **Emoji** — Sequence of 3-8 emojis
- **Words** — 4 memorable words

### Inherence (Something You Are)
- **Pattern** — Visual unlock pattern with timing
- **Rhythm Tap** — Your unique tapping pattern
- **Voice** — Spoken passphrase
- **Image Tap** — Tap specific points on an image
- **Mouse/Stylus Draw** — Your signature style
- **Balance** — Device tilt pattern
- **Fingerprint** — Via device sensor
- **Face** — Via device camera

### Possession (Something You Have)
- **NFC** — Tap your NFC tag/card

---

## Blockchain Name Support

Use human-readable names instead of UUIDs:

| Service | Example |
|---------|---------|
| **Solana Name Service** | `alice.notap.sol` (free during enrollment) |
| **Ethereum Name Service** | `alice.eth` (bring your own) |
| **Unstoppable Domains** | `alice.crypto`, `.nft`, `.wallet` |
| **BASE Name Service** | `alice.base.eth` |

**Merchant asks:** "What's your NoTap ID?"
**You can say:** `tiger-4829` or `alice.notap.sol` — both work.

---

## For Developers

### Quick Start

```kotlin
// Android
val noTap = NoTap(context, NoTapConfig(
    baseUrl = "https://api.notap.io",
    enableBiometrics = true
))

// Start enrollment
noTap.enrollment.enroll(
    factors = listOf(Factor.PIN, Factor.PATTERN, Factor.EMOJI),
    paymentProvider = PaymentProvider.STRIPE,
    onSuccess = { noTapId -> println("NoTap ID: $noTapId") }
)

// Merchant verification
noTap.verification.verify(
    noTapId = "tiger-4829",
    amount = 49.99,
    onSuccess = { result -> processPayment(result.paymentToken) }
)
```

### SDKs Available

| Platform | Status |
|----------|--------|
| **Android** | ✅ Production |
| **Web** | ✅ Production |
| **iOS** | 🚧 Q1 2026 |

### Supported PSPs

Stripe, Adyen, Square, Tilopay, MercadoPago — with parallel session creation (28% faster checkout).

### Developer Portal

- **API Keys:** Generate sandbox + production keys
- **Webhooks:** enrollment.completed, verification.succeeded, etc.
- **Analytics:** Usage stats, success rates, response times
- **Sandbox:** Test mode with fake payments

---

## For Merchants

### Why Integrate NoTap?

| Problem | NoTap Solution |
|---------|----------------|
| 30% of transactions fail during auth | Backup when primary payment fails |
| $443B in falsely declined transactions | Reduce false declines |
| PSD3 SCA compliance (mandatory 2026) | 15 factors vs. 2 minimum required |

### Integration Options

- **E-Commerce Plugins:** Shopify, WooCommerce ($9.99/month)
- **Direct API:** RESTful + SDKs
- **White-Label:** Full customization available

### Pricing

- **$0.15-0.50 per verification** (or subscription)
- **No setup fees** for standard integrations

---

## Who This Is For

### For People
- **Athletes & gym-goers** — Leave phone in locker, still buy post-workout
- **Beach/pool lovers** — Don't risk your phone near water
- **Travelers** — Phone stolen abroad? Still get home
- **Parents** — Phone with kids? Still buy groceries

### For Businesses
- **Hospitals** — Staff authenticate without phones in sterile areas
- **Warehouses** — Workers with gloves authenticate on terminals
- **Call centers** — Agents log in without personal devices
- **Any business** — No more badge replacements, no more lockouts

---

## Technical Architecture

> **Note:** NoTap is the public brand. Code uses `zeropay` for API stability (like Meta/Facebook).

### Kotlin Multiplatform (95% code reuse)

```
zeropay-android/
├── sdk/                    # Core SDK (KMP)
│   ├── commonMain/        # Platform-agnostic
│   ├── androidMain/       # Android-specific
│   └── jsMain/            # Web-specific
├── enrollment/            # User enrollment module
├── merchant/              # Merchant verification module
├── online-web/            # Web SDK (Kotlin/JS)
└── backend/               # Node.js API server
```

### Data Storage

| Storage | Purpose | Encryption |
|---------|---------|------------|
| **Device KeyStore** | NoTap ID + cached digests | Hardware-backed |
| **Redis** | Encrypted digests | AES-256-GCM + TLS |
| **PostgreSQL** | KMS-wrapped keys + names | AWS KMS |
| **Solana** | Audit trail (hashed UUIDs) | Public blockchain |

**Privacy Guarantee:** Only cryptographic hashes stored. Never raw biometric data, PINs, or patterns.

---

## Compliance

| Standard | Status |
|----------|--------|
| **PSD3 SCA** | ✅ Compliant (15 factors across 3 categories) |
| **GDPR** | ✅ Compliant (24h TTL, right to erasure) |
| **OWASP Top 10** | ✅ Mitigated |
| **NIST Crypto** | ✅ Compliant (SHA-256, PBKDF2, AES-256-GCM) |

---

## The Vision

> *I see a future where people live freer: shop at the beach risk-free, travel worry-free, and businesses attract customers who used to abandon carts. NoTap isn't just authentication; it's pure freedom.*

**What this unlocks:**

| For People | For Businesses |
|------------|----------------|
| Shop at the beach risk-free | Capture abandoned transactions |
| Travel without worry | Attract customers who left wallets behind |
| Leave phone in locker at gym | Reduce fraud with stronger auth |
| Never be stranded with dead battery | PSD3 compliance built-in |

**NoTap enables new consumer behaviors and new ways of doing business.**

> *And you — how do you see the future? Can you imagine the potential this has in the markets you know?*

---

## Documentation

- **[API Reference](https://docs.notap.io/api)**
- **[Integration Guides](https://docs.notap.io/integrations)**
- **[Developer Portal](https://developer.notap.io)**
- **[Investment Analysis](documentation/08-business/INVESTMENT_ANALYSIS_REVISED.md)**

---

## Support & Community

- **Website:** [https://notap.io](https://notap.io)
- **Documentation:** [https://docs.notap.io](https://docs.notap.io)
- **General Inquiries:** hello@notap.io
- **Technical Support:** support@notap.io
- **Partnerships:** partnership@notap.io
- **X/Twitter:** [@NoTapAuth](https://x.com/NoTapAuth)
- **Solana Name Service:** notap.sol

---

## License

Licensed under Apache License 2.0 - see [LICENSE](LICENSE).

**Commercial Use:** Permitted with attribution. Contact us for white-label licensing.

---

<div align="center">
  <p><strong>Made with ❤️ by the NoTap Team</strong></p>
  <p>
    <a href="https://notap.io">Website</a> •
    <a href="https://docs.notap.io">Documentation</a> •
    <a href="https://github.com/keikworld/zero-pay-sdk">GitHub</a>
  </p>
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
