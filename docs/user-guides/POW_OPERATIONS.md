# Proof-of-Work Operations Guide

**Last Updated**: 2026-06-20

## Gradual Rollout Plan

Phase 1: Bake-in (weeks 1-2)
```bash
# Deploy with defaults — zero user impact
POW_REQUIRED=false
POW_ALWAYS_CHALLENGE=false
POW_STRICT_ROUTES=
```
All requests return `X-PoW-Status: skipped`. Clients start receiving the headers but can ignore them.

Phase 2: Advisory (weeks 3-4)
```bash
POW_ALWAYS_CHALLENGE=true
```
Every request gets a challenge header. Clients **should** start solving and attaching solutions. Monitor:
- `X-PoW-Status: verified` rate (solved vs unsolved)
- Latency impact (client-side solving time)
- Error rates

Phase 3: Enforcement (week 5+)
```bash
POW_REQUIRED=true
```
Only enable after `verified` rate exceeds 95%. For emergency rollback, set `POW_REQUIRED=false` — takes effect immediately (no restart needed, env vars read per-request).

## Monitoring

### Logs
The middleware generates no log lines of its own. Use structured logging around it:

```javascript
// In route handler after PoW middleware:
logger.debug('PoW status', {
  powVerified: req.powVerified,
  powChallenge: !!req.powChallenge,
  route: req.baseUrl + req.path
});
```

### Metrics to Track

| Metric | What it tells you |
|--------|------------------|
| `X-PoW-Status: verified` rate | Client adoption of PoW solving |
| `X-PoW-Status: advisory` rate | Users hitting velocity thresholds |
| `X-PoW-Status: skipped` rate | Normal low-velocity traffic |
| HTTP 429 rate (enforced mode) | Blocked requests without PoW |
| Attempt velocity per identity | Brute-force detection |

### Redis Monitoring

```bash
# Count active challenges
redis-cli --scan --pattern 'pow:challenge:*' | wc -l

# Count stored solutions
redis-cli --scan --pattern 'pow:solution:*' | wc -l

# Check identity attempt counts
redis-cli --scan --pattern 'pow:attempt:*' | head -20 | xargs -I{} redis-cli ZCARD {}

# Memory usage
redis-cli --bigkeys | grep pow:
```

## Tuning

### Difficulty Thresholds

Adjust `getAdaptiveDifficulty` thresholds in `powService.js`:

```javascript
// Current thresholds:
< 3  → 16  (EASY)     ~1.3s to solve
< 10 → 20  (MEDIUM)   ~21s  to solve
< 20 → 24  (HARD)     ~5.6min to solve
≥ 20 → 28  (EXTREME)  ~89min to solve
```

For lower-latency apps, shift thresholds down:
```javascript
< 5  → 16  (EASY)
< 15 → 18  (MEDIUM)   ~5s to solve
< 30 → 20  (HARD)     ~21s to solve
≥ 30 → 22  (HARDER)   ~84s to solve
```

### Window Size

`POW_ATTEMPT_WINDOW` controls how fast difficulty escalates:
- 300s (default): attacks escalate within minutes, but legitimate bursts (page refresh spam) also escalate
- 60s: faster escalation, fewer false positives for slow users
- 3600s: slower escalation, better for low-traffic APIs

### Challenge TTL

`POW_CHALLENGE_TTL` must balance:
- Too short: client runs out of time to solve
- Too long: stale challenges occupy Redis memory

Default 300s is sufficient for difficulty 16-20. At difficulty 28, increase to 600s+.

## Troubleshooting

### "All requests return X-PoW-Status: advisory"

Check `recordAttempt` is being called correctly. The identity hash must be consistent. Verify Redis connectivity: `redis-cli ping`.

### "Clients cannot solve within TTL"

Difficulty is too high for the TTL. Either:
- Increase `POW_CHALLENGE_TTL`
- Lower `POW_ATTEMPT_WINDOW` (slower difficulty escalation)
- Lower the attempt count thresholds

### "HTTP 429 even though client sends valid PoW"

Check:
1. The nonce encoding matches: server uses `writeBigUInt64BE`, client must use matching `longToBytes`
2. The challenge hasn't expired (check `Challenge-Expires` header)
3. The solution hasn't been reused (each challenge is one-time use)

### "Redis memory growing"

PoW keys auto-expire via TTL. If memory grows unexpectedly:
```bash
redis-cli info memory | grep used_memory_human
redis-cli --scan --count 10000 --pattern 'pow:*' | wc -l
```
Run `cleanExpiredChallenges` manually if needed.

## Security Considerations

| Risk | Mitigation |
|------|-----------|
| **Challenge exhaustion** | `POW_CHALLENGE_TTL` limits per-key memory; rate limiter runs before PoW |
| **Replay attack** | Solution hash stored with TTL; challenge deleted after first use |
| **Race condition** | `isReusedSolution` check before solution hash storage |
| **PII in Redis** | All identities are SHA-256 hashes; emails are hashed before storage |
| **DoS via BigInt crash** | Nonce validated (`Number.isInteger` + `>= 0`) before `BigInt()` conversion |
| **Header injection** | Express header parsing limits input size (8KB default) |

## Recovery

### Emergency rollback
```bash
# Set POW_REQUIRED=false and clear POW_STRICT_ROUTES
# Takes effect immediately — no restart needed
```

### Clear Redis state
```bash
redis-cli --scan --pattern 'pow:*' | xargs redis-cli DEL
```
