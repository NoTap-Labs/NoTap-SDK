# Security Policy

## 🔒 Security at NoTap

Security is our top priority. NoTap is designed with security-first principles to protect user authentication data and prevent unauthorized access.

***

## 🐛 Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security issue, please report it responsibly.

### How to Report

**📧 Email:** security@notap.io

**Please Include:**

* Description of the vulnerability
* Steps to reproduce the issue
* Potential impact assessment
* Suggested fix (if available)
* Your contact information

### What to Expect

1. **Acknowledgment:** We'll acknowledge your report within **24 hours**
2. **Investigation:** We'll investigate and provide updates within **72 hours**
3. **Resolution:** We'll work on a fix and coordinate disclosure timeline
4. **Credit:** We'll credit you in the security advisory (unless you prefer to remain anonymous)

### Responsible Disclosure

Please give us reasonable time to fix vulnerabilities before public disclosure. We typically aim for:

* **Critical vulnerabilities:** 7-14 days
* **High severity:** 30 days
* **Medium/Low severity:** 60-90 days

***

## 🛡️ Security Features

### Cryptographic Standards

NoTap uses industry-standard cryptography:

* **Hashing:** SHA-256 for all authentication factor digests
* **Key Derivation:** PBKDF2 with 100,000 iterations
* **Encryption:** AES-256-GCM for data encryption
* **Transport:** TLS 1.3 for all network communication
* **Key Rotation:** Daily HKDF rotation for enhanced security

### Attack Resistance

NoTap is designed to resist common attacks:

* ✅ **Timing Attacks** - Constant-time comparison for all factors
* ✅ **Replay Attacks** - Nonce validation and session expiry
* ✅ **Brute Force** - Multi-layer rate limiting and account lockout
* ✅ **Man-in-the-Middle** - TLS 1.3 and certificate pinning
* ✅ **Memory Dumps** - Automatic memory wiping of sensitive data
* ✅ **Device Tampering** - Anti-root/jailbreak detection

### Privacy Protection

* ✅ **Zero-Knowledge Proofs** - Merchants never see which factors you used
* ✅ **No Biometric Storage** - Only cryptographic hashes stored
* ✅ **24-Hour TTL** - Authentication data auto-expires daily
* ✅ **GDPR Compliant** - Privacy by design, right to erasure

***

## 🔐 Security Best Practices

### For Developers Integrating NoTap

1. **API Key Security**
   * Never commit API keys to version control
   * Use environment variables or secure vaults
   * Rotate keys regularly (every 90 days recommended)
   * Use different keys for development/staging/production
2. **HTTPS Only**
   * Always use HTTPS for all API communication
   * Never send authentication data over HTTP
   * Implement certificate pinning for production apps
3. **Input Validation**
   * Validate all user inputs before sending to NoTap SDK
   * Sanitize data to prevent injection attacks
   * Implement proper error handling
4. **Secure Storage**
   * Use platform-specific secure storage (Keychain/KeyStore)
   * Never store authentication factors in plain text
   * Clear sensitive data from memory after use

### For End Users

1. **Device Security**
   * Use device lock screen (PIN/biometric)
   * Keep your device OS updated
   * Don't use NoTap on rooted/jailbroken devices
2. **Factor Selection**
   * Choose strong, unique authentication factors
   * Don't reuse PINs/patterns from other services
   * Enroll 6+ factors from multiple categories
3. **Account Monitoring**
   * Check your NoTap authentication history regularly
   * Report suspicious activity immediately
   * Revoke access for lost/stolen devices

***

##

***

## 📜 Compliance

### Standards & Regulations

NoTap complies with:

* **PSD3 SCA** - Strong Customer Authentication (EU Payment Directive)
* **GDPR** - General Data Protection Regulation
* **OWASP Top 10** - Web application security risks mitigated
* **NIST Cryptographic Standards** - FIPS 140-2 compliant algorithms

###

***

##

***

## 📚 Security Resources

### Documentation

* [**Security Analysis**](docs/security-analysis.md) - Detailed threat model
* [**Integration Guide**](/broken/pages/PbuP36zsQV2DenLcwsGH) - Secure integration patterns
* [**API Reference**](docs/api-reference.md) - Security considerations per endpoint

### External Resources

* [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
* [NIST Cryptographic Standards](https://csrc.nist.gov/projects/cryptographic-standards-and-guidelines)
* [PSD3 Technical Standards](https://www.eba.europa.eu/regulation-and-policy/payment-services-and-electronic-money/regulatory-technical-standards-on-strong-customer-authentication-and-secure-communication-under-psd2)

***

##

***

## 📞 Contact

* **Security Issues:** security@notap.io
* **General Security Questions:** [GitHub Discussions - Security](https://github.com/keikworld/NoTap-SDK/discussions/categories/security)
* **Emergency Security Contact:** +1-XXX-XXX-XXXX (Enterprise customers only)

***

## ✅ Security Commitment

We commit to:

1. **Transparency** - Publicly disclose security issues (after fixes)
2. **Rapid Response** - Acknowledge reports within 24 hours
3. **Regular Audits** - Annual third-party security audits
4. **Continuous Improvement** - Ongoing security enhancements
5. **Community Collaboration** - Work with security researchers

**Last Updated:** December 5, 2025

***

**Thank you for helping keep NoTap secure!** 🙏
