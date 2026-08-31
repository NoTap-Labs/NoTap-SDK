# Security Policy

## 🔒 Security at NoTap

Security is a top priority. NoTap is built around a specific, honest security
claim — see the [README](README.md)'s "What NoTap is not" section — and we'd
rather under-claim here than overstate what's actually shipped.

---

## 🐛 Reporting a Vulnerability

If you discover a security issue, please report it responsibly rather than
opening a public issue.

**📧 Email:** security@notap.io

**Please include:**

- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix, if you have one

### What to expect

1. **Acknowledgment** within 24 hours
2. **Investigation** with updates within 72 hours
3. **Resolution**, with a disclosure timeline coordinated with you
4. **Credit** in the advisory, unless you'd rather stay anonymous

### Responsible disclosure

We ask for reasonable time to fix a vulnerability before public disclosure —
typically 7-14 days for critical issues, up to 90 days for low severity.

---

## 🛡️ What's actually implemented

- **Hashing:** SHA-256 for authentication factor digests
- **Key derivation:** PBKDF2-HMAC-SHA256, 600,000 iterations
- **Encryption:** AES-256-GCM
- **Transport:** TLS for all network communication
- **Key rotation:** Daily HKDF-based digest rotation
- **Constant-time comparison** for all factor/digest checks
- **Replay protection:** per-session nonce + HMAC challenge-response
  (a captured response can't be replayed into a different session)
- **Rate limiting** with escalating penalties on abuse
- **No raw biometric storage** — only cryptographic digests are ever stored
- **24-hour TTL** on authentication data, with a documented right-to-erasure path

We do not currently claim a specific compliance certification (SOC 2, FIPS
140-2, or similar). The system is *designed* with PSD3 SCA categories and
GDPR data-minimization principles in mind; that is a design intent, not a
completed audit or certification.

---

## 🔐 Practices we recommend to integrators

**API keys**
- Never commit keys to version control
- Rotate regularly; use separate keys per environment

**Transport**
- HTTPS only, everywhere, always

**Storage**
- Platform-native secure storage (Keychain / Android Keystore)
- Never store a raw authentication factor — only its digest

**Input validation**
- Validate and bound every input before it reaches the SDK

---

## 📞 Contact

- **Security issues:** security@notap.io
- **General security questions:** [GitHub Discussions](https://github.com/NoTap-Labs/NoTap-SDK/discussions)

---

**Thank you for helping keep NoTap secure.** 🙏
