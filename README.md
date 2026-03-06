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

**Device-Independent Authentication Layer for Payments and Access**

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![SCA-Ready Architecture](https://img.shields.io/badge/SCA-Ready%20Architecture-green.svg)](https://www.ecb.europa.eu)
[![Android SDK](https://img.shields.io/badge/Android-Production-brightgreen.svg)](https://docs.notap.io)
[![Web SDK](https://img.shields.io/badge/Web-Production-brightgreen.svg)](https://docs.notap.io)

</div>

---

NoTap is a device-independent fallback authentication layer. When phone-based authentication fails — lost device, dead battery, no signal — NoTap allows users to verify their identity from any available interface, and PSPs to recover the transaction.

---

## The Problem

Today, digital identity is tightly bound to personal devices.

Authentication methods like SMS OTP, authenticator apps, and device biometrics all assume the user has access to their phone. When the device is unavailable — lost, out of battery, not present, or blocked — authentication fails.

For PSPs and merchants, this means:

- Legitimate transactions are declined
- Customers abandon purchases
- Support costs increase
- Fraud exploits gaps in device-dependent flows

**Authentication failure is not only a security issue. It is a revenue problem.**

| Problem | Annual Cost | Source |
|---------|-------------|--------|
| False declines (legitimate transactions blocked) | $443 billion | Javelin Strategy & Research |
| Cart abandonment due to authentication friction | $18 billion | Baymard Institute |
| Payment fraud | $28 billion | Nilson Report 2024 |
| IT helpdesk costs from device lockouts | $4.2 billion | Gartner |

---

## The NoTap Approach

NoTap is a fallback and recovery layer, not a replacement for existing authentication.

When a user's primary device-based authentication fails, NoTap activates. It verifies identity using memory-based identity factors the user enrolled in advance — factors they can reproduce from any available interface: a store terminal, a kiosk, a browser, or another device.

The transaction continues. The PSP never sees a failure.

```
┌─────────────────────────────────────────────────┐
│                 Payment Attempt                  │
└─────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────┐
│         PSP Authentication Step                  │
│   SMS OTP / device biometric / push notification │
│                                                  │
│   ✗  Device unavailable — authentication fails  │
└─────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────┐
│         NoTap Fallback Verification              │
│                                                  │
│  User ID:    tiger-4829                          │
│  Factor 1:   PIN          ✓ Verified             │
│  Factor 2:   Pattern      ✓ Verified             │
│                                                  │
│  ✅ Identity confirmed — 14 seconds              │
└─────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────┐
│     PSP continues transaction                    │
│     Process payment → Transaction complete       │
└─────────────────────────────────────────────────┘
```

**NoTap integrates as a secondary verification layer within existing payment and access flows. It does not process payments. It verifies identity.**

Users enroll once (5 minutes, on their own device). They configure multiple authentication factors — a PIN, a drawn pattern, a tap rhythm, a color sequence, an emoji sequence, or others. These are converted into cryptographic fingerprints. The raw values never leave the device.

After enrollment, they authenticate from any device. The system selects 2–3 factors per transaction based on risk:

| Transaction | Factors | Time |
|-------------|---------|------|
| Lower-risk transactions | 2 factors | ~10 seconds |
| Higher-risk transactions | 3 factors | ~25 seconds |
| Flagged / unusual | 3 factors | ~25 seconds |

---

## What Makes NoTap Different

Every existing authentication method verifies the device. NoTap verifies the person.

| Method | Fails When |
|--------|------------|
| SMS OTP | Phone unavailable, no signal, SIM swapped |
| Authenticator app | Phone unavailable, switched, or stolen |
| Device biometrics | Customer is at a different device |
| Push notification | No signal, dead battery, wrong device |
| **NoTap** | **Does not depend on the user's device** |

Because NoTap factors are memory-based and reproducible from any interface, authentication continues through scenarios where every other method fails.

Additional properties:

- **Bot-resistant** — Behavioral timing patterns make automation significantly harder
- **SIM-swap proof** — No phone number dependency, no SIM swap attack surface
- **No replay attacks** — Each session uses a nonce and timestamp; factor grids randomize per transaction
- **24-hour key rotation** — Daily digest rotation limits breach exposure to a single day

---

## Business Impact

Organizations using NoTap can:

- **Recover failed transactions** that would otherwise be lost to device unavailability
- **Reduce false declines** by providing higher-confidence identity verification
- **Reduce fraud** by eliminating the attack vectors that exploit device-based authentication
- **Designed for SCA compliance** — knowledge, behavioral, and biometric factors across 3 categories satisfy PSD3 requirements without 3D Secure friction
- **Reduce support costs** from authentication-related access issues

NoTap does not replace your PSP. It sits in front of it. When authentication succeeds, your existing payment flow handles everything else.

---

## Integration

### PSP Integrations

NoTap integrates with major payment providers including Stripe, Adyen, Square, Tilopay, and MercadoPago.

PSP sessions can be created in parallel with authentication to reduce checkout latency.

### SDKs

| Platform | Status |
|----------|--------|
| **Android** | ✅ Production |
| **Web** | ✅ Production |
| **iOS** | 🚧 Q1 2026 |

### Quick Start

```kotlin
val noTap = NoTap(context, NoTapConfig(
    baseUrl = "https://api.notap.io",
    apiKey = "your_api_key"
))

noTap.verification.verify(
    noTapId = "tiger-4829",
    amount = 49.99,
    onSuccess = { result -> processPayment(result.paymentToken) },
    onFailure = { error -> handleAuthFailure(error) }
)
```

Full API reference: [docs.notap.io/api](https://docs.notap.io/api)

---

## Compliance

| Standard | Status |
|----------|--------|
| **PSD3 SCA** | ✅ Designed for compliance — knowledge, behavioral, and biometric categories |
| **GDPR** | ✅ Compliant — 24h TTL, right to erasure, no raw biometric storage |
| **OWASP Top 10** | ✅ Mitigated |
| **NIST Cryptography** | ✅ SHA-256, PBKDF2, AES-256-GCM |
| **SOC 2 Type II** | 🚧 Q2 2026 |
| **HIPAA** | 🚧 Q3 2026 |

---

## Pricing

Pricing depends on verification volume. Enrollments are free and unlimited.

See [notap.io/pricing](https://notap.io/pricing) for full tier details, or [Extended Features](documentation/02-user-guides/EXTENDED_FEATURES.md) for consumer pricing.

---

## Product Scope

### Core Platform

Device-independent authentication fallback for payments and access control. This is what NoTap is today and what every integration is built on.

The core platform works independently of blockchain, AI, or agentic payment systems. It requires only an API connection and one of the available SDKs.

### Future Extensions

The same authentication infrastructure will extend to support:

- **AI agent authentication** — Identity verification for autonomous agents initiating payments
- **Decentralized identity** — Integration with self-sovereign identity systems
- **Blockchain name services** — Human-readable identifiers (.sol, .eth, .crypto) resolved to NoTap IDs
- **Autonomous payment agents** — Authentication flows for agent-to-agent commerce

These are extensions of the core primitive. They do not change how the core platform works.

---

## Summary

NoTap is authentication infrastructure for payments and access. It fills the gap that exists when device-based authentication fails — which happens more often, and costs more, than most organizations measure.

**One sentence:** NoTap is a device-independent fallback authentication layer — when device-based authentication fails, it verifies identity from any available interface so PSPs can recover the transaction.

---

## Resources

- **Website:** [https://notap.io](https://notap.io)
- **Documentation:** [https://docs.notap.io](https://docs.notap.io)
- **Developer Portal:** [https://developer.notap.io](https://developer.notap.io)
- **API Reference:** [https://docs.notap.io/api](https://docs.notap.io/api)
- **General:** hello@notap.io
- **Technical Support:** support@notap.io
- **Partnerships:** partnership@notap.io

---

<div align="center">
<p>Licensed under <a href="LICENSE">Apache License 2.0</a></p>
<p>Made with ❤️ by the NoTap Team</p>
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
