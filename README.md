# NoTap

**Get back into your account from any screen, with nothing but your memory.**
No phone. No code. No email. No support ticket.

NoTap is a device-free authentication layer for the moment a passkey or a
phone-based method can't help you: a new laptop, a kiosk, a borrowed machine,
a phone that's lost, dead, or out of signal. On your own device, a passkey
(Touch ID, Windows Hello) already gives you everything you need — NoTap is
the lane that runs when there's no device credential to fall back on.

---

## How it works

You enroll once, on a device you trust, choosing a set of memorable factors —
a PIN, a drawn pattern, a color sequence, an emoji sequence, a tap rhythm, and
others. These are converted into cryptographic digests; the raw values never
leave your device unencrypted.

To get back in later, from any screen, you're challenged on a subset of your
enrolled factors — never the same fixed set forever, and never your whole
enrolled set at once, so a single observed session can't reproduce everything
you know.

## What NoTap is not

- **Not a payment rail.** It's an authentication layer, not a payment
  processor.
- **Not biometric on every factor.** Voice, where supported, is a spoken
  password (speech-to-text), not a voiceprint.
- **Not a defense against a fully compromised device.** If the screen you're
  typing on already has a keylogger or screen recorder, nothing typed on it
  is safe — no authentication scheme can fix that. NoTap's honest claim is
  narrower and real: it removes the *account-recovery* attack surface (SMS,
  email resets, support-desk social engineering) that this class of breach
  has actually exploited.

## Status

| Platform | Status |
|---|---|
| **Android SDK** | In active development |
| **Web SDK** | In active development |
| **iOS** | Not yet started |

This project is pre-release. Treat any specific compliance, certification, or
production-readiness claim you don't see here as **not yet true** — we'd
rather under-claim than publish something we can't stand behind.

## Documentation

Public documentation is being rebuilt deliberately, file by file, rather than
mirrored wholesale from internal docs — see [`docs/`](docs/) for what's
available so far.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Report a security issue per
[SECURITY.md](SECURITY.md) rather than a public issue.

## License

[Apache License 2.0](LICENSE).
