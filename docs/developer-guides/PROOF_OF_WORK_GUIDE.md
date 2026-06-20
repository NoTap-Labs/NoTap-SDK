# Proof-of-Work Developer Guide

**Last Updated**: 2026-06-20

## Quick Start

```javascript
// 1. Import
const { PoWService } = require('./services/powService');
const { optionalPoW } = require('./middleware/proofOfWork');

// 2. Instantiate (with existing redisClient)
const powService = new PoWService(redisClient, {
  challengeTTL: 300,    // challenges expire in 5 min
  solutionTTL: 86400,   // solved hashes stored 24h (replay protection)
  attemptWindow: 300    // velocity window of 5 min
});

// 3. Wire into Express
app.use('/v1/auth/user',
  userRateLimiter(redisClient),  // rate limiter runs FIRST
  optionalPoW(powService),       // PoW middleware
  replayProtection,
  regularAuthRouter
);
```

## Modes

### Advisory (default, backward compatible)

`POW_REQUIRED=false` (default). The middleware sets response headers but never blocks:

```
Response Headers:
  X-PoW-Status: skipped         # low velocity, no challenge needed
  X-PoW-Status: advisory        # high velocity, challenge issued
  X-PoW-Challenge-Id: uuid      # if advisory
  X-PoW-Challenge: hex(32)      # challenge bytes
  X-PoW-Difficulty: 20          # required leading zero bits
  X-PoW-Expires: 1712345678     # epoch ms
```

Clients **may** solve the challenge and include the solution in the next request — the server verifies it and sets `X-PoW-Status: verified`. But clients that ignore the challenge still succeed.

### Enforced

`POW_REQUIRED=true` or route prefix in `POW_STRICT_ROUTES`. The middleware blocks requests without a valid PoW with HTTP 429:

```json
{
  "success": false,
  "error": "Proof of work required. Solve the challenge and retry with X-PoW-Nonce header.",
  "code": "POW_REQUIRED",
  "pow_challenge": {
    "challenge_id": "uuid",
    "challenge": "hex(32)",
    "difficulty": 20,
    "expires_at": 1712345678
  }
}
```

## Client-Side Solving

### Kotlin (SDK)

```kotlin
import com.zeropay.sdk.security.ProofOfWorkSolver
import com.zeropay.sdk.security.CryptoUtils

// Suspend version (use for JS target — Web Crypto is async-only)
val nonce = ProofOfWorkSolver.solveChallenge(
    challenge = challengeHex.hexToByteArray(),
    difficulty = 20,
    hashFn = { CryptoUtils.sha256Suspend(it) } // JS: must use suspend variant
)

// Sync version (JVM only)
val nonce = ProofOfWorkSolver.solveChallengeSync(
    challenge = challengeHex.hexToByteArray(),
    difficulty = 20
)

// Send back in request
request.headers["X-PoW-Challenge-Id"] = challengeId
request.headers["X-PoW-Nonce"] = nonce.toString()
```

### JavaScript (Node.js)

```javascript
const crypto = require('crypto');
const { checkLeadingZeroBits } = require('./services/powService');

function solveChallenge(challengeHex, difficulty) {
  const challenge = Buffer.from(challengeHex, 'hex');
  let nonce = 0;
  while (nonce < 1_000_000_000) {
    const nonceBuf = Buffer.alloc(8);
    nonceBuf.writeBigUInt64BE(BigInt(nonce), 0);
    const solution = Buffer.concat([challenge, nonceBuf]);
    const hash = crypto.createHash('sha256').update(solution).digest();
    if (checkLeadingZeroBits(hash, difficulty)) return nonce;
    nonce++;
  }
  throw new Error('Could not solve PoW challenge');
}
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `POW_REQUIRED` | `false` | Master switch: block requests without valid PoW |
| `POW_ALWAYS_CHALLENGE` | `false` | Issue challenge on every request (advisory header) |
| `POW_STRICT_ROUTES` | `''` | Comma-separated prefixes that always enforce PoW (e.g., `/v1/auth/admin`) |
| `POW_CHALLENGE_TTL` | `300` | Challenge expiry in seconds |
| `POW_SOLUTION_TTL` | `86400` | Replay-protection TTL in seconds |
| `POW_ATTEMPT_WINDOW` | `300` | Velocity tracking window in seconds |

## Adding PoW to a New Route

1. Find the route in `backend/server.js`
2. Insert `optionalPoW(powService)` between the rate limiter and the route handler:

```javascript
app.use('/v1/auth/developer',
  userRateLimiter(redisClient),
  optionalPoW(powService),  // ← add here
  developerAuthRouter
);
```

3. If this route should ALWAYS require PoW (even when `POW_REQUIRED=false`), add its prefix to `POW_STRICT_ROUTES`.

## Testing

```bash
# Unit tests
cd backend
npx mocha tests/unit/powService.test.js --exit --timeout 10000

# Middleware tests
npx mocha tests/unit/proofOfWork.middleware.test.js --exit --timeout 10000

# Both
npx mocha tests/unit/powService.test.js tests/unit/proofOfWork.middleware.test.js --exit --timeout 10000
```

## Deployment Checklist

Before enabling enforcement:

- [ ] SDK clients updated to solve PoW challenges
- [ ] `POW_ALWAYS_CHALLENGE=true` deployed first — monitor client behavior via `X-PoW-Status` headers
- [ ] `POW_STRICT_ROUTES` populated with admin/merchant-only endpoints
- [ ] `POW_REQUIRED=true` enabled last, after all clients have baked in solver support
