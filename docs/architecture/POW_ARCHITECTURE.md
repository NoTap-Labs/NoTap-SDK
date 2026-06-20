# Proof-of-Work Anti-Hammering Architecture

**Last Updated**: 2026-06-20

## Overview

Adaptive Proof-of-Work (PoW) protects ZeroPay's auth endpoints from distributed brute-force attacks, credential stuffing, and DoS. Unlike traditional rate limiting (which blocks per-IP), PoW imposes a computational cost **per attempt** — stopping botnets where each request originates from a different IP.

## Architecture

```
Client                    Server (middleware layer)                Redis
  |                            |                                     |
  |-- POST /v1/auth/user ----->|                                     |
  |                            |-- identityFromReq()                 |
  |                            |-- readPowEnv() -- env vars          |
  |                            |                                     |
  |  [No PoW headers]          |-- recordAttempt(identity)           |
  |                            |-- getAdaptiveDifficulty(identity)   |
  |                            |                                     |
  |<--- X-PoW-Status: skipped  |  (difficulty <= 16, low velocity)   |
  |                            |                                     |
  |-- POST (with headers) ---->|                                     |
  |  X-PoW-Challenge-Id: x    |-- verifySolution(id, nonce) ------> |
  |  X-PoW-Nonce: 42          |   GET pow:challenge:x               |
  |                            |   DEL pow:challenge:x               |
  |                            |   SHA-256(challenge + nonce)        |
  |                            |   checkLeadingZeroBits(hash, diff)  |
  |                            |   SETEX pow:solution:hash 86400 '1' |
  |<--- X-PoW-Status: verified |                                     |
  |                            |                                     |
  |-- POST (velocity high) --> |                                     |
  |                            |-- recordAttempt(identity)           |
  |                            |-- getAdaptiveDifficulty -> 20       |
  |                            |-- generateChallenge(20)             |
  |                            |   SETEX pow:challenge:y 300 '{...}' |
  |<--- X-PoW-Challenge-Id: y  |                                     |
  |     X-PoW-Difficulty: 20  |                                     |
  |     X-PoW-Status: advisory |                                     |
```

## Layers

### 1. Middleware Layer (`backend/middleware/proofOfWork.js`)

Sits between the rate limiter and route handlers. Reads dynamic env vars per-request (not cached at import). Three modes:

| Mode | Trigger | Behavior |
|------|---------|----------|
| **Skipped** | No PoW headers + `difficulty <= 16` | Pass-through. Header: `X-PoW-Status: skipped` |
| **Advisory** | No PoW headers + `difficulty > 16` | Issues challenge but does NOT block. Header: `X-PoW-Status: advisory` |
| **Enforced** | `POW_REQUIRED=true` or strict route match | Blocks with 429 until valid PoW presented. Header: `X-PoW-Status: challenge` / `X-PoW-Status: verified` |
| **Always** | `POW_ALWAYS_CHALLENGE=true` | Issues challenge on every request regardless of velocity |

### 2. Service Layer (`backend/services/powService.js`)

All Redis interaction is isolated here. Key operations:

| Operation | Redis Command | Key pattern | Purpose |
|-----------|--------------|-------------|---------|
| `generateChallenge` | `SETEX pow:challenge:{id} 300 '{json}'` | `<prefix>{uuid}` | Store challenge for client to solve |
| `verifySolution` | `GET` + `DEL` challenge, `SETEX pow:solution:{hash}` | `<prefix>{uuid}` | One-time use + replay protection |
| `recordAttempt` | Lua `EVAL` (ZADD, ZREMRANGEBYSCORE, EXPIRE, ZCARD) | `<prefix>{identity}` | Track attempt velocity per identity |
| `getAdaptiveDifficulty` | Lua `EVAL` + ZCARD | `<prefix>{identity}` | Escalate difficulty based on attempt count |

### 3. Identities (`identityFromReq`)

All endpoints compute a **hashed identity** from available request signals. Identities never contain raw PII — emails are SHA-256 hashed, IPs are anonymized:

```
identity = SHA-256(anonymizeIP(req.ip) + ":" + SHA-256(email) + ":" + user_uuid)
```

Missing signals are omitted (not replaced with empty string). If no signals exist, identity is `null` and PoW uses default difficulty 16.

### 4. Adaptive Difficulty

| Attempts in window | Difficulty | Leading zero bits | Expected hashes | ~Time (50K hash/ms) |
|-------------------|-----------|-------------------|-----------------|-------------------|
| 0-2 (normal) | 16 (EASY) | 16 bits | 65,536 | ~1.3s |
| 3-9 (elevated) | 20 (MEDIUM) | 20 bits | 1,048,576 | ~21s |
| 10-19 (suspicious) | 24 (HARD) | 24 bits | 16,777,216 | ~5.6min |
| 20+ (attack) | 28 (EXTREME) | 28 bits | 268,435,456 | ~89min |

## Nonce Encoding

The SDK uses **big-endian 8-byte encoding** (`longToBytes`) for nonces. The server must match this:

```
solution = challenge_bytes || big_endian_8_bytes(nonce)
hash = SHA-256(solution)
```

## Security Properties

| Property | Mechanism |
|----------|-----------|
| **Replay prevention** | Challenge deleted after first `verifySolution` call; solution hash stored with TTL |
| **Race condition mitigation** | `isReusedSolution` check before storing solution hash |
| **PII-free tracking** | Identities are SHA-256 hashes of anonymized signals |
| **Automatic data cleanup** | All Redis keys have TTL: challenges 5min, solution hashes 24h, attempt windows 5min |
| **Non-breaking defaults** | `POW_REQUIRED=false` — zero impact on existing clients |
| **Error safety** | All async operations wrapped in try/catch → `next(error)` |

## Key Files

| File | Purpose |
|------|---------|
| `backend/services/powService.js` | Core PoW service (challenges, verification, velocity tracking) |
| `backend/middleware/proofOfWork.js` | Express middleware layer |
| `backend/server.js` (lines 159-173) | PoW service instantiation and wiring |
| `sdk/src/commonMain/kotlin/.../ProofOfWorkSolver.kt` | SDK client-side solver (suspend + sync) |
| `sdk/src/commonMain/kotlin/.../SecurityEnhancements.kt` | Legacy ProofOfWork stub (reference) |
| `backend/tests/unit/powService.test.js` | 30 unit tests |
| `backend/tests/unit/proofOfWork.middleware.test.js` | 8 middleware tests |
