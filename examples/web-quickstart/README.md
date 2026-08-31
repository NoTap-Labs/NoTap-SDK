# Web quickstart (preview)

The Web SDK is the platform we've actually proven working end-to-end — real
browser, real CSP, real cryptography, real backend (see the main
[README](../../README.md#-status)). A public npm package isn't published yet,
so this is illustrative: what integration is designed to look like, not a
package you can install today.

```javascript
import { NoTap } from '@notap/web-sdk'; // not yet published — illustrative

const noTap = new NoTap({
  apiKey: 'your_api_key',
});

// Enrollment — a person chooses several memorable factors once
const enrollment = await noTap.enrollment.start({
  factors: ['PIN', 'PATTERN', 'EMOJI', 'WORDS'],
});

// Later, from any screen: recovery / step-up verification
const session = await noTap.verification.initiate({
  userIdentifier: 'their-notap-id', // UUID, alias, or linked name
});

// The SDK walks the person through whichever factors this session
// challenges them on — never their whole enrolled set at once.
const result = await noTap.verification.complete(session);

if (result.verified) {
  // Restore access, enroll a new passkey, approve an action —
  // your application decides what happens next.
}
```

## What's real right now

- Enrollment and verification work end-to-end against a real backend, with
  real WASM-based OPAQUE cryptography (RFC 9807) — the server never stores
  anything password-equivalent.
- The shape of the API above is the intended design; exact method names may
  still change before a public package ships.

## Want to try it before the package is public?

That's exactly what the [Help us validate NoTap](../../README.md#-help-us-validate-notap)
section is for.
