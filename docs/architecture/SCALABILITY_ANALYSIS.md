# ZeroPay Scalability & Concurrency Architecture Review

> **Target:** 100,000+ Concurrent Authentication Sessions
> **Status:** Analysis Complete
> **Date:** 2026-02-12

---

## Executive Summary

| Aspect | Current State | 100k Concurrent Target | Verdict |
|--------|--------------|------------------------|---------|
| **Redis** | Single-node, 256MB | Needs clustering | ⚠️ CRITICAL |
| **PostgreSQL** | Pool: 20 connections | Needs PgBouncer + read replicas | ⚠️ CRITICAL |
| **Sessions** | In-memory Map | Must move to Redis | ⚠️ CRITICAL |
| **Rate Limiting** | O(N) SCAN iteration | Needs optimized counters | ⚠️ CRITICAL |
| **API Layer** | Single instance | Horizontal scaling ready | ✅ GOOD |

---

## 1. System Architecture Mapping

### Authentication Flow (per verification)

```
User Request
    ↓
Rate Limiting (Redis) - 3-5 Redis ops
    ↓
Replay Protection (Redis) - 1-2 Redis ops
    ↓
[INITIATE]
  ├─ Redis: GET enrollment:{uuid} (encrypted digests) ✅
  ├─ Select random factors
  ├─ Create in-memory session (Map) ⚠️
  └─ PSP parallel (optional) → Redis
    ↓
[VERIFY]
  ├─ Redis: GET enrollment:{uuid} 
  ├─ Constant-time digest comparison (CPU)
  ├─ PostgreSQL: GET wrapped_key (1 query)
  ├─ Double decryption (CPU: PBKDF2 + AES)
  ├─ ZK proof generation (CPU intensive)
  └─ Session token → Redis
```

### Operations Per Authentication

| Metric | Value |
|--------|-------|
| **PostgreSQL queries** | 2 per verification (1 SELECT + 1 audit INSERT) |
| **Redis operations** | 5-8 per verification |
| **CPU crypto ops** | PBKDF2 (100K iterations) + AES-256-GCM |

---

## 2. Target Load Modeling

### Conservative Estimate: 100k concurrent in 60-second burst

| Metric | Value |
|--------|-------|
| **Peak RPS** | 1,666 requests/second |
| **Redis ops/sec** | ~8,000-13,000 ops/sec |
| **PostgreSQL writes/sec** | ~1,666 audit logs/sec |
| **CPU (crypto)** | High (PBKDF2, AES-256-GCM) |

### Memory Requirements

| Component | Calculation | Required |
|-----------|-------------|----------|
| **Redis sessions** | 100k × ~2KB | 200MB minimum |
| **Rate limit keys** | ~100k keys × 60s TTL | 100-200MB |
| **Enrollment data** | ~500 bytes × active users | 50-100MB |
| **Total Redis** | | **2-4GB recommended** (vs 256MB current) |

---

## 3. Redis Scalability Analysis

### Current Configuration Issues

| Issue | Impact | Severity |
|-------|--------|----------|
| **Single-node Redis** | No horizontal scaling, SPOF | 🔴 CRITICAL |
| **256MB memory limit** | Will saturate at ~50k sessions | 🔴 CRITICAL |
| **allkeys-lru eviction** | May evict active sessions | 🟡 HIGH |
| **SCAN for rate limits** | O(N) - problematic at scale | 🟡 HIGH |
| **No pipeline usage** | Round-trip latency per op | 🟡 MEDIUM |

### Redis Operations Breakdown

```javascript
// Rate limiting uses:
// - Global: FIXED window (1 key)
// - Per-IP: SLIDING window (1 key, ZREMRANGEBYSCORE, ZCARD, ZADD)
// - Per-user: TOKEN BUCKET (1 key, HMGET, HMSET, EXPIRE)

// Total: 3-5 Redis round-trips per request
// At 1,666 RPS = 5,000-8,000 Redis ops/sec
```

### Redis Scaling Roadmap

| Phase | Target | Configuration |
|-------|--------|---------------|
| **Phase 1** | 10k concurrent | Single node, 2GB RAM, pipeline optimization |
| **Phase 2** | 50k concurrent | Redis Cluster (3 masters) |
| **Phase 3** | 100k+ | Redis Cluster (6+ masters) + read replicas |

---

## 4. PostgreSQL Scalability Analysis

### Current Configuration

```javascript
// database.js:50-75
pool = new Pool({
  max: 20,              // ← CRITICAL BOTTLENECK
  min: 2,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 30000,
});
```

### Issues Identified

| Issue | Impact | Severity |
|-------|--------|----------|
| **max: 20 connections** | Cannot handle 1,666 writes/sec | 🔴 CRITICAL |
| **Audit log per operation** | Synchronous write bottleneck | 🔴 CRITICAL |
| **No connection pooling** | Every request = new connection | 🟡 HIGH |
| **Wrapped key queries** | Single row lookups OK, but not scalable | 🟡 MEDIUM |
| **No read replicas** | All reads hit primary | 🟡 MEDIUM |

### Query Analysis

```sql
-- Per verification:
SELECT * FROM wrapped_keys WHERE uuid = $1;  -- 1 query, indexed (PK)

-- Per audit log:
INSERT INTO audit_log (uuid, action, ip_address, details) VALUES (...);  -- 1 write
```

### PostgreSQL Scaling Roadmap

| Phase | Target | Configuration |
|-------|--------|---------------|
| **Phase 1** | 10k concurrent | PgBouncer (100 connections) |
| **Phase 2** | 50k concurrent | PgBouncer + 1 read replica |
| **Phase 3** | 100k+ | PgBouncer + 3 read replicas + partition audit_log |

---

## 5. Connection & Pooling Analysis

### Current State

| Component | Current | Required (100k) |
|-----------|---------|-----------------|
| **PostgreSQL pool** | 20 | 100-200 (via PgBouncer) |
| **Redis clients** | 1 | Connection pool or cluster |
| **API instances** | 1 | 10+ (horizontal) |

### Critical Problem

```
100k concurrent users
÷ 20 PostgreSQL connections
= 5,000 requests per connection per second

This is IMPOSSIBLE - connections will exhaust immediately.
```

---

## 6. State Management Analysis

### CRITICAL ISSUE: In-Memory Sessions

```javascript
// verificationRouter.js:82
const verificationSessions = new Map();  // ← CRITICAL SCALABILITY BUG
```

**Problems:**
1. Sessions stored in single process memory
2. Not shared across API instances
3. Lost on instance restart
4. Cannot scale horizontally

**Required Fix:** Move to Redis with TTL

```javascript
// Should be:
await redisClient.setEx(`verification:${sessionId}`, 300, JSON.stringify(session));
```

---

## 7. Horizontal Scalability Analysis

### Current Architecture

- Single Express server instance
- In-memory session storage (blocks horizontal scaling)
- Single Redis node (blocks scaling)
- Single PostgreSQL (read/write)

### Scalability Readiness

| Component | Status |
|-----------|--------|
| API layer | ✅ Ready (stateless except sessions) |
| Rate limiting | ✅ Ready (Redis-backed) |
| Session tokens | ✅ Ready (Redis-backed when fixed) |
| Verification sessions | ❌ Blocks scaling |
| Redis node | ❌ Blocks scaling |
| PostgreSQL | ❌ Blocks scaling |

---

## 8. Bottleneck Identification

### Ranked Bottlenecks

| # | Bottleneck | Location | Severity | Impact |
|---|------------|----------|----------|--------|
| 1 | **In-memory sessions** | verificationRouter.js:82 | 🔴 CRITICAL | Blocks horizontal scaling entirely |
| 2 | **Single Redis node** | docker-compose.yml | 🔴 CRITICAL | No fault tolerance, memory limit |
| 3 | **PostgreSQL pool = 20** | database.js:58 | 🔴 CRITICAL | Connection exhaustion at scale |
| 4 | **Sync audit logging** | database.js:351 | 🔴 CRITICAL | 1,666 writes/sec will block |
| 5 | **Rate limit SCAN** | rateLimitMiddleware.js:721 | 🟡 HIGH | O(N) iteration problematic |
| 6 | **No PgBouncer** | Infrastructure | 🟡 HIGH | Connection overhead |
| 7 | **CPU crypto ops** | Double decryption | 🟡 HIGH | PBKDF2 is CPU-intensive |

---

## 9. Load Testing Recommendations

### Required Test Scenarios

| Phase | Target RPS | Purpose |
|-------|------------|---------|
| Phase 1 | 1,000 | Baseline |
| Phase 2 | 2,000 | Expected peak |
| Phase 3 | 5,000 | Stress test |

### Key Metrics to Capture

- P50, P95, P99 latency
- Redis CPU and memory usage
- PostgreSQL connections in use
- Lock wait times
- Error rate

### Recommended Tools

- **k6** (recommended)
- Locust
- Artillery

---

## 10. Failure Mode Analysis

| Failure Scenario | Impact | Mitigation |
|-----------------|--------|------------|
| **Redis crash** | All auth fails | Redis Cluster (Sentinel) |
| **PostgreSQL crash** | Auth succeeds but can't store wrapped keys | Read replicas + async writes |
| **Single API instance** | Total outage | Horizontal scaling |
| **Burst 10x traffic** | Rate limits trigger, 429 responses | Graceful degradation |
| **Replay storm** | DB write amplification | Aggressive rate limiting |

---

## 11. Scaling Roadmap

### Phase 1: 0 → 10,000 Concurrent

| Task | Priority | Effort |
|------|----------|--------|
| Move verification sessions to Redis | 🔴 CRITICAL | 1 day |
| Increase PostgreSQL pool to 50 | 🔴 CRITICAL | 1 hour |
| Increase Redis memory to 2GB | 🔴 CRITICAL | 1 hour |
| Add async audit logging (buffered) | 🟡 HIGH | 2 days |
| Implement connection pooling for Redis | 🟡 HIGH | 1 day |

### Phase 2: 10,000 → 50,000 Concurrent

| Task | Priority | Effort |
|------|----------|--------|
| Deploy Redis Cluster (3 masters) | 🔴 CRITICAL | 1 week |
| Add PgBouncer (100 connections) | 🔴 CRITICAL | 2 days |
| Optimize rate limit queries | 🟡 HIGH | 2 days |
| Add 1 PostgreSQL read replica | 🟡 HIGH | 2 days |
| Implement API auto-scaling (3+ instances) | 🟡 HIGH | 1 week |

### Phase 3: 50,000 → 100,000+ Concurrent

| Task | Priority | Effort |
|------|----------|--------|
| Redis Cluster (6+ masters) | 🔴 CRITICAL | 1 week |
| PgBouncer + read replicas (3) | 🔴 CRITICAL | 1 week |
| Partition audit_log table | 🟡 HIGH | 1 week |
| Move to event-driven architecture | 🟡 HIGH | 2 weeks |
| Consider separate rate-limit service | 🟡 MEDIUM | 2 weeks |

---

## 12. Required Metrics

### Monitoring Dashboard Thresholds

| Metric | Warning | Critical |
|--------|---------|----------|
| **RPS** | - | > 1,500 |
| **Concurrent sessions** | > 30,000 | > 50,000 |
| **Redis memory** | > 60% | > 80% |
| **Redis CPU** | > 50% | > 70% |
| **PostgreSQL connections** | > 12/20 | > 18/20 |
| **PostgreSQL query latency** | > 50ms | > 100ms |
| **Auth latency P99** | > 300ms | > 500ms |
| **Error rate** | > 0.5% | > 1% |

---

## 13. Expert Requirements

| Role | When Needed |
|------|-------------|
| **Redis Cluster Architect** | Phase 2 |
| **PostgreSQL Tuning Specialist** | Phase 2 |
| **DevOps Reliability Engineer** | All phases |
| **Load Testing Engineer** | Phase 1 |

---

## Final Verdict

### Production Readiness: ⚠️ NOT READY for 100k concurrent

**Critical Issues (Must Fix Before Production):**

1. **In-memory verification sessions** - Blocks ALL horizontal scaling
2. **Single Redis node** - Memory limit + no fault tolerance
3. **PostgreSQL pool (20)** - Will exhaust at 1,666 RPS
4. **Synchronous audit logging** - 1,666 writes/sec = bottleneck

### Timeline Estimate

| Phase | Duration |
|-------|----------|
| Phase 1 (Quick Wins) | 1 week |
| Phase 2 (Clustering) | 2-3 weeks |
| Phase 3 (Full Scale) | 4-6 weeks |

### Conditional Pass

- **10k concurrent**: With Phase 1 fixes ✅
- **50k concurrent**: With Phase 2 fixes ✅
- **100k+ concurrent**: With Phase 3 fixes ✅

---

## Related Documentation

- [Architecture Overview](./ARCHITECTURE.md)
- [Security Audit](../05-security/SECURITY_AUDIT.md)
- [Development Rules](../03-developer-guides/DEVELOPMENT_RULES.md)
- [Testing Architecture](../07-testing/TEST_ARCHITECTURE.md)
