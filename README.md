<div align="center">

# 🔐 NoTap

**Get back into your account from any screen, with nothing but your memory.**

No phone. No code. No email. No support ticket.

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Status: Pre-release](https://img.shields.io/badge/status-pre--release-orange.svg)](#-status)
[![Android](https://img.shields.io/badge/Android-in%20development-yellow.svg)](#-status)
[![Web](https://img.shields.io/badge/Web-testable%20now-brightgreen.svg)](#-status)

</div>

---

NoTap is a device-free authentication layer for the moment a passkey or a
phone-based method can't help you: a new laptop, a kiosk, a borrowed machine,
a phone that's lost, dead, or out of signal. On your own device, a passkey
(Touch ID, Windows Hello) already gives you everything you need — NoTap is
the lane that runs when there's no device credential to fall back on.

Read more on the [NoTap website](https://notap.io) — including our current
security-boundary claims and how we're validating this with people who live
the recovery problem.

## 🧠 How it works

You enroll once, on a device you trust, choosing a set of memorable factors —
a PIN, a drawn pattern, a color sequence, an emoji sequence, a tap rhythm, and
others. These are converted into cryptographic digests; the raw values never
leave your device unencrypted.

To get back in later, from any screen, you're challenged on a rotating subset
of your enrolled factors — never the same fixed set forever, and never your
whole enrolled set at once, so a single observed session can't reproduce
everything you know.

## ✅ What NoTap is — and isn't

| NoTap **is** | NoTap **is not** |
|---|---|
| Recovery and step-up authentication | A replacement for a passkey on your own device |
| A way to avoid phone/email/OTP/support-agent recovery | Protection against a malware-controlled terminal |
| A verified-identity signal an app can act on | A card vault, payment processor, or PSP |
| Useful after device loss, or on any unfamiliar device | PSD3/SCA compliant today (that's a design intent, not a certification) |

If the screen you're typing on already has a keylogger or a screen recorder,
nothing typed or tapped on it is safe — no authentication scheme fixes that.
NoTap's claim is narrower and real: it removes the *account-recovery* attack
surface (SMS, email resets, support-desk social engineering) that this class
of breach has actually exploited.

**Optional payment handoff.** NoTap does not process payments, store card
details, or replace a payment provider. After it verifies a user, an
application may pass that verified session to its own existing payment
provider to resume or start a checkout — useful when *authentication*, not
payment processing, is the missing step.

### Why it's worth trying

| For users | For businesses |
|---|---|
| No dependency on a phone during a recovery event | Fewer costly manual/agent-assisted recovery cases |
| No email reset link, no OTP to copy | A recovery path independent of SMS delivery or telecom providers |
| No support call — less friction, less social-engineering exposure | A second lane alongside passkeys, not a replacement for them |
| Memorable factors, not one long password to forget | Payment-provider handoff is optional infrastructure, not the core product |

## 🧪 Help us validate NoTap

We're validating this with people who live the recovery problem — product
teams, security teams, developers, and researchers who want to test whether
device-independent recovery is useful, understandable, and safe.

| Who | How you can help |
|---|---|
| 🎨 **Design partners** | Help us test real recovery journeys inside your application. |
| 🛡️ **Security & identity reviewers** | Review the threat model, challenge our design, and probe our implementation assumptions. |
| 🧑‍💻 **Open-source contributors** | Help improve factor usability, accessibility, SDKs, testing, and documentation. |

We're asking for feedback, pilot conversations, and technical review — not
asking anyone to move payments or replace an existing identity stack.

- 💬 **Start here:** [GitHub Discussions](https://github.com/NoTap-Labs/NoTap-SDK/discussions)
- 🌐 **Or reach the team directly:** [notap.io](https://notap.io) → *Help validate NoTap*

## 📊 Status

| Platform | Status |
|---|---|
| 🤖 **Android SDK** | 🚧 In active development |
| 🌐 **Web SDK** | ✅ Testable now — enrollment and verification work end-to-end |
| 🍎 **iOS** | 📋 Not yet started |

This project is pre-release. Treat any specific compliance, certification, or
production-readiness claim you don't see here as **not yet true** — we'd
rather under-claim than publish something we can't stand behind.

## 📚 Documentation

Public documentation is being rebuilt deliberately, file by file, rather than
mirrored wholesale from internal docs — see [`docs/`](docs/) for what's
available so far.

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Found a security issue? Please report
it per [SECURITY.md](SECURITY.md) rather than a public issue.

## 📄 License

[Apache License 2.0](LICENSE).

<div align="center">

·

*Built for the screen you didn't expect to need.*

</div>
