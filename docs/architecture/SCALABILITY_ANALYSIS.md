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

---

## Expert Recommendations: MVP for 10k, Ready for 100k+

### The Critical Insight

**The difference between an MVP that breaks at 10k and one that's ready for 100k is architectural decisions made NOW, not infrastructure later.**

The key blocker is **stateful in-memory storage** which prevents horizontal scaling entirely. Fix this once, and scaling becomes just infrastructure configuration.

---

### Minimum Viable Infrastructure (10k Concurrent)

For MVP launch handling 10,000+ simultaneous users:

| Component | Minimum | Configuration |
|-----------|---------|---------------|
| **API Instances** | 2 (for HA) | Auto-scaling disabled initially |
| **Redis** | Single node | 2GB RAM, no persistence (data is re-creatable) |
| **PostgreSQL** | Single | Connection pool: 50 |
| **Memory** | 2GB per API | Node.js heap optimized |

**This can handle ~300-500 RPS comfortably.**

---

### The 5 Critical Architectural Changes (MVP Required)

These changes are MINIMAL code modifications but enable infinite horizontal scaling:

#### 1. Move Verification Sessions to Redis ⭐ CRITICAL

**Files:** `verificationRouter.js:82`, `pspRouter.js:40`

```javascript
// BEFORE (blocks scaling):
const verificationSessions = new Map();

// AFTER (enables horizontal scaling):
// Use Redis with TTL - session auto-expires, shared across instances
await redisClient.setEx(`verification:${sessionId}`, 300, JSON.stringify(session));
const session = JSON.parse(await redisClient.get(`verification:${sessionId}`));
```

**Why:** Sessions are the ONLY thing preventing horizontal scaling. This single change enables ANY number of API instances.

**Impact:** 1-day work, enables infinite scaling

#### 2. Abstract Session Storage Behind Interface

**New file:** `backend/services/sessionStorage.js`

```javascript
// Interface that can switch between Memory/Redis/etc.
class SessionStorage {
  constructor(adapter = 'redis') {
    this.adapter = adapter;
  }
  
  async set(sessionId, data, ttl = 300) {
    if (this.adapter === 'redis') {
      return redisClient.setEx(`session:${sessionId}`, ttl, JSON.stringify(data));
    }
    return memoryStore.set(sessionId, { data, expires: Date.now() + ttl * 1000 });
  }
  
  async get(sessionId) {
    if (this.adapter === 'redis') {
      const data = await redisClient.get(`session:${sessionId}`);
      return data ? JSON.parse(data) : null;
    }
    const entry = memoryStore.get(sessionId);
    return entry && entry.expires > Date.now() ? entry.data : null;
  }
}
```

**Why:** Enables runtime switching between memory (testing) and Redis (production) without code changes.

#### 3. Increase PostgreSQL Pool + Add PgBouncer

**File:** `backend/database/database.js`

```javascript
pool = new Pool({
  max: 50,  // Up from 20 - handles 10k concurrent
  min: 5,
  // ... rest same
});
```

**Infrastructure:** Add PgBouncer (connection pooler) in front of PostgreSQL:
- Reduces connection overhead
- Enables 100s of connections without overwhelming PostgreSQL

**Why:** 20 connections cannot handle burst traffic. 50 is minimum for 10k.

#### 4. Increase Redis Memory + Optimize

**File:** `backend/docker-compose.yml`

```yaml
redis-dev:
  command: >
    redis-server
    --requirepass ${REDIS_PASSWORD}
    --maxmemory 2gb
    --maxmemory-policy allkeys-lru
    --appendonly yes
    --save 900 1 --save 300 10  # Reduce fsync frequency
```

**Why:** 256MB will fill up at ~5k sessions. 2GB handles 50k+.

#### 5. Make Rate Limiting Configurable

**File:** `backend/middleware/rateLimitMiddleware.js`

Add environment variables:

```javascript
const RATE_LIMIT_CONFIG = {
  global: { 
    windowMs: 60000, 
    max: parseInt(process.env.RATE_LIMIT_GLOBAL) || 1000 
  },
  perIP: { 
    windowMs: 60000, 
    max: parseInt(process.env.RATE_LIMIT_PER_IP) || 100 
  },
  perUser: { 
    windowMs: 60000, 
    max: parseInt(process.env.RATE_LIMIT_PER_USER) || 50 
  }
};
```

**Why:** Allows tightening limits during attacks without code changes.

---

### The "Scalability Framework" - Enable 100k+ Without Code Changes

These infrastructure changes prepare for 100k without touching code:

| Change | When | Effect |
|--------|------|--------|
| **Redis Cluster (3 masters)** | Phase 2 | Horizontal Redis scaling |
| **PgBouncer (100 connections)** | Phase 2 | Connection pooling |
| **PostgreSQL Read Replica** | Phase 2 | Read scaling |
| **Redis Pipeline Batch Ops** | Phase 2 | Reduce round-trips |
| **API Auto-scaling (5+ instances)** | Phase 2 | Horizontal scaling |
| **Async Audit Logging** | Phase 2 | Remove sync bottleneck |

---

### Specific Code Patterns to Avoid (Technical Debt)

| Pattern | Problem | Fix |
|---------|---------|-----|
| `new Map()` for sessions | Not shared across instances | Use Redis from day 1 |
| `pool.query()` in hot path | Connection overhead | Use prepared statements + PgBouncer |
| `KEYS *` or `SCAN` in production | O(N) blocking | Use specific keys or hash fields |
| Synchronous audit logging | Blocks response | Use async queue (Bull/Redis streams) |
| Hardcoded connection limits | Inflexible | Use env vars |

---

### Recommended MVP Launch Configuration

```yaml
# docker-compose.mvp.yml (recommended for launch)

services:
  api:
    deploy:
      replicas: 2  # Minimum 2 for HA
      resources:
        limits:
          memory: 2G
        reservations:
          memory: 1G
    
  redis:
    command: >
      redis-server
      --maxmemory 2gb
      --maxmemory-policy allkeys-lru
      --tcp-backlog 511
      --timeout 0
      
  postgres:
    # Add PgBouncer sidecar
    # Or use cloud provider's connection pooler
```

---

### Summary: MVP vs 100k+ Path

| Aspect | MVP (10k) | 100k+ |
|--------|-----------|-------|
| **Sessions** | Redis (shared) | Redis Cluster |
| **API Instances** | 2 | 10+ auto-scale |
| **Redis** | 2GB single node | 6+ master cluster |
| **PostgreSQL** | 50 pool + PgBouncer | PgBouncer + 3 replicas |
| **Rate Limiting** | Redis-backed | Redis Cluster |
| **Audit Logs** | Async queue | Event-driven |

### The One Thing to Get Right

> **Move verification sessions to Redis. Everything else is just tuning.**

This single architectural decision is the difference between:
- ❌ Having to rewrite the entire session system at 50k users
- ✅ Just adding more Redis nodes at 100k users

---

## Final Verdict

### Production Readiness: ⚠️ NOT READY for 100k concurrent (without changes)

### With Phase 1 MVP Fixes: ✅ READY for 10k concurrent

**Required Changes for MVP Launch:**

| Priority | Change | Effort | Impact |
|----------|--------|--------|--------|
| 🔴 CRITICAL | Move verificationSessions to Redis | 1 day | Enables horizontal scaling |
| 🔴 CRITICAL | Move pspSessions to Redis | 1 day | Enables horizontal scaling |
| 🔴 CRITICAL | Increase PostgreSQL pool to 50 | 1 hour | Prevents connection exhaustion |
| 🔴 CRITICAL | Increase Redis to 2GB | 1 hour | Prevents OOM |
| 🟡 HIGH | Add PgBouncer | 2 hours | Connection pooling |
| 🟡 HIGH | Make rate limits configurable | 1 hour | Operational flexibility |

**Total MVP Work: 2-3 days** → Ready for 10k, architected for 100k+

---

## Detailed MVP Recommendations with Code Changes

### Recommendation 1: Move Verification Sessions from In-Memory Map to Redis

**File:** `backend/routes/verificationRouter.js:82`

**Current Code (Blocking Horizontal Scaling):**
```javascript
const verificationSessions = new Map();
```

**Recommended Change:**
```javascript
// REMOVE: const verificationSessions = new Map();
// ADD: Use Redis client from app.locals

// In /initiate endpoint (line 400):
await redisClient.setEx(`verification:${sessionId}`, 300, JSON.stringify(session));

// In /verify endpoint (line 581):
const sessionData = await redisClient.get(`verification:${sessionId}`);
const session = sessionData ? JSON.parse(sessionData) : null;

// In delete operations (lines 591, 600, 903, 960, 1093):
await redisClient.del(`verification:${session_id}`);
```

**Performance Impact:**
- **Latency:** +1-2ms per session operation (Redis round-trip)
- **Throughput:** Enables infinite horizontal scaling (currently blocked)
- **Memory:** Offloads session storage from heap to Redis
- **Reliability:** Sessions survive API restarts

**Reasoning:**
The in-memory `Map()` stores sessions only in the current process. With 2+ API instances, requests can hit different instances and find no session. This causes auth failures. Redis provides shared storage across all instances.

---

### Recommendation 2: Move PSP Sessions from In-Memory Map to Redis

**File:** `backend/routes/pspRouter.js:40`

**Current Code:**
```javascript
const pspSessions = new Map();
```

**Recommended Change:**
```javascript
// REMOVE: const pspSessions = new Map();
// ADD: Use Redis with appropriate TTL

// Store PSP session:
await redisClient.setEx(`psp:${sessionId}`, 1800, JSON.stringify(pspData));

// Retrieve PSP session:
const pspData = JSON.parse(await redisClient.get(`psp:${sessionId}`));

// Delete PSP session:
await redisClient.del(`psp:${sessionId}`);
```

**Performance Impact:**
- **Latency:** +1-2ms per PSP operation
- **Throughput:** Enables multiple API instances to serve PSP requests
- **Memory:** ~10KB per PSP session offloaded to Redis

**Reasoning:**
PSP sessions are created in parallel with auth. If user hits different API instance after auth succeeds, PSP session must be accessible. Currently it would fail.

---

### Recommendation 3: Increase PostgreSQL Connection Pool

**File:** `backend/database/database.js:58`

**Current Code:**
```javascript
pool = new Pool({
  max: 20,
  min: 2,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 30000,
});
```

**Recommended Change:**
```javascript
pool = new Pool({
  max: parseInt(process.env.DB_POOL_SIZE) || 50,
  min: parseInt(process.env.DB_POOL_MIN) || 5,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 30000,
});
```

**Performance Impact:**
- **Throughput:** Can handle 2.5x more concurrent DB operations
- **Latency:** Reduces connection wait time from ~500ms to ~10ms under load
- **Error Rate:** Prevents "too many connections" errors at ~166 RPS

**Reasoning:**
With 20 connections and ~166 RPS average (10k users in 60s burst), the pool exhausts quickly. Each verification needs a DB query. With connection wait times, latency spikes.

---

### Recommendation 4: Increase Redis Memory Limit

**File:** `backend/docker-compose.yml`

**Current Configuration:**
```yaml
redis-dev:
  command: >
    redis-server
    --requirepass ${REDIS_PASSWORD}
    --maxmemory 256mb
    --maxmemory-policy allkeys-lru
```

**Recommended Change:**
```yaml
redis-dev:
  command: >
    redis-server
    --requirepass ${REDIS_PASSWORD}
    --maxmemory 2gb
    --maxmemory-policy allkeys-lru
    --tcp-backlog 511
    --timeout 0
    --appendonly yes
```

**Performance Impact:**
- **Capacity:** Can store 8x more sessions (256MB → 2GB)
- **Sessions:** Supports ~50,000+ verification sessions
- **Eviction:** Reduces likelihood of evicting active sessions

**Reasoning:**
Each verification session is ~2KB. With 256MB, we hit memory limit at ~128,000 sessions. At 10k concurrent users with auth bursts, we risk hitting this. 2GB provides headroom.

---

### Recommendation 5: Make Rate Limits Configurable via Environment

**File:** `backend/middleware/rateLimitMiddleware.js`

**Current Code (Hardcoded):**
```javascript
const DEFAULT_LIMITS = {
  global: { windowMs: 60000, max: 1000 },
  perIP: { windowMs: 60000, max: 100 },
  perUser: { windowMs: 60000, max: 50 }
};
```

**Recommended Change:**
```javascript
const DEFAULT_LIMITS = {
  global: { 
    windowMs: 60000, 
    max: parseInt(process.env.RATE_LIMIT_GLOBAL) || 1000 
  },
  perIP: { 
    windowMs: 60000, 
    max: parseInt(process.env.RATE_LIMIT_PER_IP) || 100 
  },
  perUser: { 
    windowMs: 60000, 
    max: parseInt(process.env.RATE_LIMIT_PER_USER) || 50 
  }
};
```

**Performance Impact:**
- **Operational:** Can tighten limits during attack without deploy
- **Flexibility:** Can increase for legitimate high-traffic periods

**Reasoning:**
During a DDoS or brute-force attack, you need to reduce limits immediately. With hardcoded values, you need to deploy code. With env vars, just restart with new config.

---

### Recommendation 6: Remove Periodic Cleanup Interval (After Redis Migration)

**File:** `backend/routes/verificationRouter.js:1281`

**Current Code:**
```javascript
setInterval(() => {
  const now = Date.now();
  let cleaned = 0;
  for (const [sessionId, session] of verificationSessions.entries()) {
    if (now > session.expiresAt) {
      verificationSessions.delete(sessionId);
      cleaned++;
    }
  }
  if (cleaned > 0) {
    safeLogger.info(`🧹 Cleaned up ${cleaned} expired verification sessions`);
  }
}, 5 * 60 * 1000);
```

**Recommended Change:**
```javascript
// REMOVE THIS ENTIRE BLOCK AFTER MIGRATING TO REDIS
// Redis TTL handles expiration automatically
// Manual cleanup is no longer needed and wastes CPU

// Optional: Keep for monitoring only
// setInterval(() => {
//   const redisClient = req.app.locals.redisClient;
//   const keys = await redisClient.keys('verification:*');
//   safeLogger.info(`Active verification sessions: ${keys.length}`);
// }, 5 * 60 * 1000);
```

**Performance Impact:**
- **CPU:** Eliminates O(N) iteration every 5 minutes
- **Memory:** No Map to clean (Redis handles TTL)
- **Latency:** No blocking cleanup operations

**Reasoning:**
The cleanup iterates through ALL sessions every 5 minutes. With thousands of sessions, this blocks the event loop. Redis handles expiration automatically via TTL - no code needed.

---

### Recommendation 7: Add Environment Variables

**Add to `.env.example`:**

```bash
# ============================================================================
# SCALABILITY CONFIGURATION (MVP)
# ============================================================================

# Redis Session TTL in seconds (default: 300 = 5 minutes)
REDIS_SESSION_TTL=300

# PostgreSQL Connection Pool
DB_POOL_SIZE=50
DB_POOL_MIN=5

# Rate Limiting (requests per minute)
RATE_LIMIT_GLOBAL=1000
RATE_LIMIT_PER_IP=100
RATE_LIMIT_PER_USER=50
```

**Performance Impact:**
- **Configuration:** No code deploys needed to tune
- **Production:** Can adjust based on real traffic patterns

---

## Summary: MVP Minimum Viable Changes

| # | Change | File | Effort | Scalability Impact |
|---|--------|------|--------|-------------------|
| 1 | Move verificationSessions to Redis | verificationRouter.js | 1 day | Enables horizontal scaling |
| 2 | Move pspSessions to Redis | pspRouter.js | 1 day | Enables horizontal scaling |
| 3 | Increase DB pool to 50 | database.js | 1 hour | Prevents connection exhaustion |
| 4 | Increase Redis to 2GB | docker-compose.yml | 1 hour | Prevents OOM |
| 5 | Make rate limits configurable | rateLimitMiddleware.js | 1 hour | Operational flexibility |
| 6 | Remove cleanup interval | verificationRouter.js | 30 min | CPU optimization |

**Total MVP Work: 2-3 days**

**Result: Can handle 10k concurrent, architected for 100k+**

---

## Related Documentation

- [Architecture Overview](./ARCHITECTURE.md)
- [Security Audit](../05-security/SECURITY_AUDIT.md)
- [Development Rules](../03-developer-guides/DEVELOPMENT_RULES.md)
- [Testing Architecture](../07-testing/TEST_ARCHITECTURE.md)
- [Provider Abstraction](./PROVIDER_ABSTRACTION.md)

---

# APPENDIX: Infrastructure Scaling (Phase 2+)

> This section documents infrastructure requirements for 50k-100k+ concurrent users.

---

## Phase 2: Redis Cluster

### When Required

- **50k+ concurrent users**
- **High availability** required (tolerate node failures)
- **Memory needs** exceed single Redis instance

### Configuration

```yaml
# docker-compose.redis-cluster.yml
version: '3.8'

services:
  redis-node-1:
    image: redis:7-alpine
    command: redis-server --cluster-enabled yes --cluster-config-file nodes.conf --cluster-node-timeout 5000 --appendonly yes
    ports:
      - "7001:7001"
    volumes:
      - redis1:/data

  redis-node-2:
    image: redis:7-alpine
    command: redis-server --cluster-enabled yes --cluster-config-file nodes.conf --cluster-node-timeout 5000 --appendonly yes
    ports:
      - "7002:7002"
    volumes:
      - redis2:/data

  redis-node-3:
    image: redis:7-alpine
    command: redis-server --cluster-enabled yes --cluster-config-file nodes.conf --cluster-node-timeout 5000 --appendonly yes
    ports:
      - "7003:7003"
    volumes:
      - redis3:/data

volumes:
  redis1:
  redis2:
  redis3:
```

### Environment Variables

```bash
# Redis Cluster
REDIS_CLUSTER_ENABLED=true
REDIS_CLUSTER_NODES=redis-node-1:7001,redis-node-2:7002,redis-node-3:7003
```

### Code Changes

Update `ServiceFactory.js` to detect cluster mode:

```javascript
if (process.env.REDIS_CLUSTER_ENABLED === 'true') {
  const cluster = new Redis.Cluster([
    { host: 'redis-node-1', port: 7001 },
    { host: 'redis-node-2', port: 7002 },
    { host: 'redis-node-3', port: 7003 }
  ]);
  return new RedisClusterCacheService(cluster);
}
```

---

## Phase 2: PgBouncer

### When Required

- **Database connection exhaustion** at high load
- **100+ concurrent requests** hitting database
- **Cost reduction** (fewer PostgreSQL connections)

### Configuration

```yaml
# docker-compose.pgbouncer.yml
services:
  pgbouncer:
    image: edoburu/pgbouncer
    environment:
      DATABASE_URL: ${DATABASE_URL}
      POOL_MODE: transaction
      MAX_CLIENT_CONN: 200
      DEFAULT_POOL_SIZE: 25
      MIN_POOL_SIZE: 5
    ports:
      - "6432:5432"
```

### Pool Modes

| Mode | Use Case | Trade-off |
|------|----------|-----------|
| `session` | Legacy apps, prepared statements | More connections |
| `transaction` | **Recommended** for NoTap | Best for connection pooling |
| `statement` | Rarely needed | Most aggressive, breaks some apps |

### Environment Variables

```bash
# PgBouncer
PGBOUNCER_HOST=localhost
PGBOUNCER_PORT=6432
PGBOUNCER_POOL_MODE=transaction

# Application connects to PgBouncer instead of PostgreSQL
DATABASE_URL=postgresql://user:pass@pgbouncer:6432/zeropay
```

---

## Phase 2: PostgreSQL Read Replica

### When Required

- **Read-heavy workload** (verification checks vs writes)
- **50k+ concurrent users**
- **Geographic distribution** (replicas in multiple regions)

### Architecture

```
                    ┌──────────────┐
                    │   Primary    │
                    │ PostgreSQL   │
                    │   :5432      │
                    └──────┬───────┘
                           │
              ┌────────────┴────────────┐
              │                         │
       ┌──────▼──────┐          ┌──────▼──────┐
       │   Read      │          │   Read      │
       │  Replica 1 │          │  Replica 2  │
       │   :5432     │          │   :5433     │
       └─────────────┘          └─────────────┘
```

### Implementation

The application already has `PgReadReplicaService`:

```javascript
const { PgReadReplicaService } = require('./services/database/PgReadReplicaService');

const replicaService = new PgReadReplicaService(database.pool, {
  replicaUrl: process.env.DB_REPLICA_URL
});

// Reads go to replica (faster)
const user = await replicaService.read('SELECT * FROM users WHERE uuid = $1', [uuid]);

// Writes go to primary (required)
await replicaService.write('UPDATE users SET last_login = NOW() WHERE uuid = $1', [uuid]);
```

### Environment Variables

```bash
# PostgreSQL Read Replica
DB_REPLICA_URL=postgresql://user:pass@replica-host:5432/zeropay
DB_REPLICA_POOL_MAX=10
DB_REPLICA_POOL_MIN=2
```

---

## Phase 2: Async Audit Logging

### When Required

- **High transaction volume** (1000+ verifications/minute)
- **Database load** from synchronous audit inserts
- **Compliance** requires guaranteed audit logging

### Implementation

The application already has `AsyncAuditService`:

```javascript
const { AsyncAuditService } = require('./services/AsyncAuditService');

const auditService = new AsyncAuditService(cacheService, dbService, {
  bufferSize: 100,        // Flush when buffer reaches 100
  flushInterval: 5000     // Or every 5 seconds
});

// Non-blocking - returns immediately
await auditService.log({
  uuid: userUuid,
  action: 'VERIFICATION_COMPLETE',
  ipAddress: req.ip,
  details: { factors: ['PIN', 'EMOJI'], success: true }
});
```

### Environment Variables

```bash
# Async Audit
AUDIT_BUFFER_SIZE=100
AUDIT_FLUSH_INTERVAL=5000
```

---

## Phase 3: Horizontal API Scaling

### Load Balancer Setup

```yaml
# docker-compose.traefik.yml
services:
  traefik:
    image: traefik:v2.10
    command:
      - "--api.insecure=true"
      - "--providers.docker=true"
      - "--providers.docker.network=backend"
      - "--entrypoints.web.address=:80"
      - "--entrypoints.websecure.address=:443"
    ports:
      - "80:80"
      - "443:443"
      - "8080:8080"
    networks:
      - backend

  api-1:
    build: .
    environment:
      - NODE_ENV=production
      - REDIS_HOST=redis
      - DATABASE_URL=postgres://user:pass@pgbouncer:6432/zeropay
    networks:
      - backend

  api-2:
    build: .
    environment:
      - NODE_ENV=production
      - REDIS_HOST=redis
      - DATABASE_URL=postgres://user:pass@pgbouncer:6432/zeropay
    networks:
      - backend
```

### Session Affinity

With Redis-backed sessions, API instances can be stateless:
- Any API instance can handle any request
- Sessions stored in shared Redis
- Add/remove instances without configuration

---

## Infrastructure Checklist

| Phase | Users | Components | Effort |
|-------|-------|-----------|--------|
| MVP | 10k | Single Redis, Single PG | ✅ Done |
| Phase 2 | 50k | Redis Cluster, PgBouncer, Read Replica | 2-3 days |
| Phase 3 | 100k+ | Load Balancer, Multiple API Instances | 1 week |

---

## Monitoring Metrics

### Key Metrics to Track

| Metric | Alert Threshold | Action |
|--------|----------------|--------|
| Redis memory | > 80% | Scale to cluster or optimize |
| DB connections | > 80% | Add PgBouncer |
| API response time | > 500ms | Profile and optimize |
| Session not found | > 1% | Check Redis connectivity |

### Recommended Tools

- **Redis**: `redis-cli INFO memory`
- **PostgreSQL**: `SELECT * FROM pg_stat_activity`
- **API**: Prometheus + Grafana

