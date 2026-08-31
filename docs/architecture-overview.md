# Architecture overview

A short, conceptual look at how NoTap works — not implementation detail, just
enough to see the shape of it. Deeper technical docs will be added here as
they're reviewed for public release (see the repo README's "Help us
validate" section).

## The two-lane model

NoTap is not a replacement for passkeys. On a device you own, a passkey
(Touch ID, Windows Hello) already gives you the best available sign-in
experience — nothing here changes that.

NoTap is the *second* lane: the one that runs when the first lane isn't
available — a new laptop, a kiosk, a borrowed machine, a phone that's lost,
dead, or out of signal.

## Enrollment: several memorable factors, not one password

During enrollment, a person chooses several factors from categories such as:

- A PIN
- A colour sequence
- An emoji sequence
- A short phrase or word sequence
- A discrete pattern (a fixed sequence of points, not a free-form drawing)
- A tap rhythm
- A set of image-tile choices
- NFC, where the device supports it

Voice, where offered, is a spoken password converted to text — not a
biometric voiceprint. Factors that can't be reproduced reliably by a real
person twice (free-form drawing, precise timing capture, face, fingerprint)
are intentionally excluded from what's enrollable.

Each factor becomes a cryptographic digest. The raw value never leaves the
device unencrypted.

## Verification: never the whole set, and never fixed forever

At verification time, NoTap doesn't ask for every enrolled factor — it asks
for a subset, chosen with a specific goal: bound what a single observer of
one login screen can learn over time.

- A **recognised device** is challenged on the same small subset, repeatedly.
  Watching it once, or a hundred times, teaches an observer only that subset
  — never the rest of what was enrolled.
- An **unfamiliar device** gets a stronger challenge — more factors than any
  single recognised device is ever shown — so a captured session from one
  device is, by construction, insufficient to authenticate anywhere else.

## What happens after verification

NoTap only answers one question: *is this the enrolled person?* What happens
next is entirely up to the application — restore account access, enroll a
new passkey, approve a sensitive action, or (optionally) hand a verified
session to an existing payment provider to resume a checkout. NoTap doesn't
process payments or store card details itself.

## What this deliberately doesn't cover

This page is a teaser, not a threat model. It doesn't cover key derivation,
challenge-response internals, or the specific cryptographic protocols in use
(the README mentions OPAQUE for the web client, for those following along).
Those write-ups are exactly the kind of technical detail we're looking for
security and identity reviewers to help us document publicly and get right —
see the main [README](../README.md#-help-us-validate-notap).
