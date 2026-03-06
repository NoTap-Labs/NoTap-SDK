# NoTap — Extended Features

This document covers features that extend the core authentication platform: consumer pricing tiers and blockchain-based identity.

These features are optional and not required to integrate NoTap into a payment or access flow.

---

## Consumer Pricing

End users access NoTap through a freemium model. The core service is free.

| Tier | Price | Includes |
|------|-------|---------|
| **Free** | $0 forever | Unlimited authentications, all standard factors |
| **Plus** | $2.99/month | Cloud backup, advanced factors (Voice, Face), priority support |

**Free tier covers 99% of users.** The Plus tier is for users who want cloud backup of their enrollment (so they can re-enroll quickly on a new device) or who want to use advanced biometric factors.

For merchant pricing, see the [main README](../../README.md#pricing).

---

## Blockchain Identity (Name Services)

NoTap supports blockchain name services as human-readable identifiers.

Instead of a UUID like `a1b2c3d4-5678-90ab-cdef-1234567890ab`, users can authenticate with a name like `alice.notap.sol` or `alice.eth`.

This is an optional identity layer. The default identifier formats (alias and UUID) work without any blockchain integration.

### Supported Name Services

| Service | Example | Status |
|---------|---------|--------|
| **Solana Name Service** | `alice.sol`, `alice.notap.sol` | ✅ Production |
| **Ethereum Name Service** | `alice.eth` | ✅ Production |
| **Unstoppable Domains** | `alice.crypto`, `alice.nft`, `alice.wallet` | ✅ Production |
| **BASE Name Service** | `alice.base.eth` | ✅ Production |

### How It Works

At verification, the user provides their blockchain name. NoTap resolves it to a UUID using the appropriate name service registry, then runs the standard authentication flow.

```
User: "alice.notap.sol"
         ↓
NoTap resolves .sol name → UUID: a1b2c3d4-...
         ↓
Standard authentication flow begins
```

No blockchain transaction occurs during authentication. Name resolution is a read-only lookup.

### Identity Formats Summary

| Format | Example | Best For |
|--------|---------|---------|
| **Alias** | `tiger-4829` | Easy to say at a counter or type on any device |
| **UUID** | `a1b2c3d4-5678-...` | Maximum privacy, programmatic use |
| **Blockchain name** | `alice.notap.sol` | Users who already have a Web3 identity |

All three formats resolve to the same enrolled profile and authentication flow.

---

## Future Extensions

The following capabilities are on the product roadmap and will extend the same device-independent authentication model:

- **AI agent authentication** — Verifying identity for autonomous agents initiating payments
- **Decentralized identity (DID)** — Integration with W3C DID standards and self-sovereign identity
- **On-chain audit trail** — Optional immutable verification log (hashed UUIDs, not raw data)
- **ZK-SNARK proofs** — Mathematically provable privacy (architecture ready, production Q3 2026)

These are extensions of the core platform. The core works independently of all of them.
