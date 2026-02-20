# Provider Abstraction Layer

> **Purpose:** Enable plug-and-play switching between infrastructure providers without code changes.

---

## Overview

The provider abstraction layer allows the application to switch between different cache and database providers by changing environment variables. This enables:

- **Vendor independence:** Switch providers without code changes
- **Horizontal scaling:** Sessions in shared Redis, not in-memory Maps
- **Easy testing:** Use memory adapter without Redis
- **Future-proofing:** Add new providers without refactoring

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Business Logic Layer                        │
│         (verificationRouter, enrollmentRouter, etc.)            │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                  Provider Interface Layer                        │
│  ┌─────────────────┐           ┌─────────────────────────────┐   │
│  │  ICacheService  │           │   IDatabaseService         │   │
│  │  (interface)    │           │   (interface)             │   │
│  └────────┬────────┘           └──────────────┬────────────┘   │
└───────────┼──────────────────────────────────────┼──────────────┘
            │                                      │
┌───────────▼──────────────────────────────────────▼──────────────┐
│                   Adapter Layer (Pluggable)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐   │
│  │RedisCache    │  │MemoryCache  │  │PgDatabase         │   │
│  │Service       │  │Service      │  │Service            │   │
│  │(default)     │  │(testing)   │  │(default)          │   │
│  └──────────────┘  └──────────────┘  └────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Environment Variables

### Cache Provider Switching

| Variable | Values | Default | Description |
|----------|--------|---------|-------------|
| `CACHE_PROVIDER` | `redis`, `memory`, `keydb`, `memcached` | `redis` | Cache provider |

### Database Provider Switching

| Variable | Values | Default | Description |
|----------|--------|---------|-------------|
| `DB_PROVIDER` | `postgresql`, `mysql`, `cockroachdb` | `postgresql` | Database provider |

### Provider-Specific Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `REDIS_URL` | - | Redis connection URL |
| `DATABASE_URL` | - | PostgreSQL connection URL |
| `DB_REPLICA_URL` | - | Read replica URL (Phase 2) |

---

## Usage

### Using Cache Service in Routes

```javascript
// ❌ FORBIDDEN - Direct Redis, hard to swap
const redisClient = req.app.locals.redisClient;
await redisClient.setEx(`session:${id}`, 300, data);

// ✅ REQUIRED - Use abstraction layer
const cacheService = req.app.locals.cacheService;
await cacheService.setSession(id, data, 300);
```

### Using Database Service

```javascript
// ❌ FORBIDDEN - Direct pg Pool
const result = await req.app.locals.db.query('SELECT * FROM users');

// ✅ REQUIRED - Use abstraction layer
const dbService = req.app.locals.dbService;
const result = await dbService.query('SELECT * FROM users');
```

### Switching Providers

```bash
# Use Redis (production)
CACHE_PROVIDER=redis

# Use Memory (testing without Redis)
CACHE_PROVIDER=memory

# Use PostgreSQL (default)
DB_PROVIDER=postgresql
```

---

## Available Adapters

### Cache Adapters

| Adapter | Provider | Status | Use Case |
|---------|----------|--------|----------|
| `RedisCacheService` | Redis | ✅ Stable | Production |
| `MemoryCacheService` | Memory | ✅ Stable | Testing, dev |
| `KeyDBCacheService` | KeyDB | 🔜 Future | High availability |
| `MemcachedCacheService` | Memcached | 🔜 Future | Simple caching |

### Database Adapters

| Adapter | Provider | Status | Use Case |
|---------|----------|--------|----------|
| `PgDatabaseService` | PostgreSQL | ✅ Stable | Production |
| `MySQLDatabaseService` | MySQL | 🔜 Future | Alternative DB |
| `CockroachDatabaseService` | CockroachDB | 🔜 Future | Distributed DB |

---

## Adding a New Provider

### Step 1: Create Adapter

```javascript
// backend/services/cache/KeyDBCacheService.js
const { ICacheService } = require('./ICacheService');

class KeyDBCacheService extends ICacheService {
  constructor(keydbClient, options = {}) {
    super(options);
    this.client = keydbClient;
  }

  async setSession(sessionId, data, ttlSeconds) {
    // Implement using KeyDB client
  }

  async getSession(sessionId) {
    // Implement using KeyDB client
  }

  // ... implement all ICacheService methods
}

module.exports = { KeyDBCacheService };
```

### Step 2: Add to ServiceFactory

```javascript
// backend/services/ServiceFactory.js
const { KeyDBCacheService } = require('./cache/KeyDBCacheService');

// In createCacheService():
case 'keydb':
  return new KeyDBCacheService(keydbClient);
```

### Step 3: Add Environment Variable

```bash
# In .env.example
# KeyDB (future)
# KEYDB_HOST=localhost
# KEYDB_PORT=6379
```

### Step 4: Test

```bash
# Test with new provider
CACHE_PROVIDER=keydb npm run dev

# Test switching
CACHE_PROVIDER=memory npm test
```

---

## Provider Switching Matrix

| Switch To | Cache Config | Database Config | Testing Effort |
|-----------|--------------|-----------------|----------------|
| **KeyDB** | `CACHE_PROVIDER=keydb` | N/A | 1 hour |
| **Memcached** | `CACHE_PROVIDER=memcached` | N/A | 1 hour |
| **MySQL** | N/A | `DB_PROVIDER=mysql` | 2 hours |
| **CockroachDB** | N/A | `DB_PROVIDER=cockroachdb` | 2 hours |
| **Memory** | `CACHE_PROVIDER=memory` | N/A | 0 (already works) |

---

## Phase 2 Services

### PgReadReplicaService

For read/write splitting at scale:

```javascript
const { PgReadReplicaService } = require('./services/database/PgReadReplicaService');

const replicaService = new PgReadReplicaService(database.pool, {
  replicaUrl: process.env.DB_REPLICA_URL
});

// Read queries go to replica
const users = await replicaService.read('SELECT * FROM users WHERE active = true');

// Write queries go to primary
await replicaService.write('INSERT INTO users (id, email) VALUES ($1, $2)', [id, email]);
```

### AsyncAuditService

For buffered audit logging:

```javascript
const { AsyncAuditService } = require('./services/AsyncAuditService');

const auditService = new AsyncAuditService(cacheService, dbService, {
  bufferSize: 100,
  flushInterval: 5000
});

// Non-blocking audit log
await auditService.log({
  uuid: userUuid,
  action: 'LOGIN',
  ipAddress: req.ip,
  details: { method: 'password' }
});
```

---

## Testing with Different Providers

### Unit Tests (Memory Adapter)

```javascript
// Tests always use memory adapter
const cacheService = new MemoryCacheService();
await cacheService.setSession('test-1', { userId: '123' }, 300);
```

### Integration Tests (Configure per Environment)

```bash
# Use memory for fast tests
CACHE_PROVIDER=memory npm test

# Use Redis for full integration
CACHE_PROVIDER=redis npm run test:integration
```

---

## Key Files

| File | Purpose |
|------|---------|
| `backend/services/ServiceFactory.js` | Provider creation factory |
| `backend/services/cache/ICacheService.js` | Cache interface definition |
| `backend/services/cache/RedisCacheService.js` | Redis adapter |
| `backend/services/cache/MemoryCacheService.js` | Memory adapter |
| `backend/services/database/IDatabaseService.js` | Database interface |
| `backend/services/database/PgDatabaseService.js` | PostgreSQL adapter |
| `backend/services/database/PgReadReplicaService.js` | Read replica (Phase 2) |
| `backend/services/AsyncAuditService.js` | Buffered audit (Phase 2) |

---

## Scaling Checklist

Before every commit, verify:

- [ ] New sessions use `cacheService.setSession()` not `Map`
- [ ] No direct `redisClient` calls in routes
- [ ] Database queries go through `dbService`
- [ ] Connection pools are configurable via env vars
- [ ] New features support provider switching

---

## Related Documentation

- [SCALABILITY_ANALYSIS.md](./SCALABILITY_ANALYSIS.md) - Performance analysis and recommendations
- [REDIS_HYBRID_ARCHITECTURE.md](./REDIS_HYBRID_ARCHITECTURE.md) - Redis Cluster + PostgreSQL fallback
- [DEVELOPMENT_RULES.md](../03-developer-guides/DEVELOPMENT_RULES.md) - Section 12: Scalability Rules

---

# Redis Hybrid Architecture Guide

> **Purpose:** High-availability Redis with horizontal scaling and automatic failover.

---

## Overview

The hybrid Redis architecture provides:
- **Horizontal Scaling**: Redis Cluster for 100k+ users
- **Automatic Failover**: Sentinel-based automatic failover
- **Graceful Degradation**: PostgreSQL fallback when Redis is unavailable
- **Pluggable Design**: Add new providers without code changes

---

## Architecture Comparison

| Configuration | Users | Failover | Complexity | Use Case |
|--------------|-------|----------|------------|----------|
| Single Redis | 10k | ❌ | Low | Dev/Test |
| Redis + Sentinel | 50k | ✅ | Medium | Small Prod |
| Redis Cluster | 100k+ | ⚠️ | High | Large Prod |
| **Cluster + Sentinel** | **100k+** | ✅ | High | **Production** |

---

## Hybrid Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    API Request                                   │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Cache Service Layer                            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  HybridCacheService (Primary)                           │    │
│  │  1. Try Redis Cluster (fast)                           │    │
│  │  2. If Redis fails → Try PostgreSQL fallback           │    │
│  │  3. Re-populate Redis when recovered                   │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
    ┌──────────┐    ┌──────────────┐  ┌──────────┐
    │  Redis   │    │ PostgreSQL   │  │  Cache   │
    │ Cluster  │───▶│  Fallback    │◀─│  Miss    │
    │(Primary) │    │  (Secondary) │  │          │
    └──────────┘    └──────────────┘  └──────────┘
```

---

## Environment Variables

### Redis Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `REDIS_CLUSTER_ENABLED` | `false` | Enable Redis Cluster mode |
| `REDIS_CLUSTER_NODES` | - | Comma-separated cluster nodes |
| `REDIS_SENTINEL_ENABLED` | `false` | Enable Sentinel failover |
| `REDIS_SENTINEL_MASTER` | `mymaster` | Sentinel master name |
| `REDIS_SENTINEL_NODES` | - | Comma-separated sentinel nodes |

### Fallback Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_FALLBACK_ENABLED` | `true` | Enable PostgreSQL fallback |
| `POSTGRES_FALLBACK_CACHE` | `true` | Cache to PostgreSQL on Redis miss |
| `FALLBACK_RETRY_ATTEMPTS` | `3` | Retry Redis before fallback |

---

## Usage

### Using Hybrid Cache Service

```javascript
// ✅ RECOMMENDED - Uses hybrid with fallback
const cacheService = req.app.locals.cacheService;

// Automatic: Try Redis first, fallback to PostgreSQL
const session = await cacheService.getSession(sessionId);

// Automatic: Write to both Redis and PostgreSQL
await cacheService.setSession(sessionId, data, 300);
```

### Direct Redis (When Needed)

```javascript
// For operations that MUST use Redis (e.g., rate limiting)
const redisClient = req.app.locals.redisClient;
await redisClient.incr(`rate:${ip}`);
```

---

## Implementation

### ServiceFactory Configuration

```javascript
// In ServiceFactory.js
static createCacheService(redisClient = null, options = {}) {
  const provider = process.env.CACHE_PROVIDER || 'redis';
  
  switch (provider) {
    case 'redis':
      // Check for cluster or sentinel mode
      if (process.env.REDIS_CLUSTER_ENABLED === 'true') {
        return new RedisClusterCacheService(redisClient, options);
      }
      if (process.env.REDIS_SENTINEL_ENABLED === 'true') {
        return new RedisSentinelCacheService(redisClient, options);
      }
      return new RedisCacheService(redisClient);
    
    case 'redis-hybrid':
      // Hybrid: Redis + PostgreSQL fallback
      return new HybridCacheService(redisClient, options);
    
    case 'memory':
      return new MemoryCacheService();
  }
}
```

### HybridCacheService Pattern

```javascript
class HybridCacheService {
  constructor(redisClient, options = {}) {
    this.redis = redisClient;
    this.dbService = options.dbService; // PostgreSQL service
    this.retryAttempts = options.retryAttempts || 3;
    this.fallbackEnabled = options.fallbackEnabled !== false;
  }

  async getSession(sessionId) {
    // Try Redis first
    for (let attempt = 1; attempt <= this.retryAttempts; attempt++) {
      try {
        const cached = await this.redis.get(`session:${sessionId}`);
        if (cached) return JSON.parse(cached);
        break; // Key doesn't exist, not an error
      } catch (redisError) {
        logger.warn(`Redis attempt ${attempt} failed: ${redisError.message}`);
        if (attempt === this.retryAttempts) {
          // All retries failed, trigger fallback
          break;
        }
        await this.sleep(100 * attempt); // Exponential backoff
      }
    }

    // Fallback to PostgreSQL
    if (this.fallbackEnabled && this.dbService) {
      logger.info('Redis unavailable, falling back to PostgreSQL');
      try {
        const session = await this.dbService.getSession(sessionId);
        // Re-populate Redis for next time (best effort)
        if (session) {
          this.redis.setEx(`session:${sessionId}`, 300, JSON.stringify(session))
            .catch(() => {}); // Best effort
        }
        return session;
      } catch (dbError) {
        logger.error('PostgreSQL fallback also failed', dbError);
        throw new Error('Session unavailable');
      }
    }

    return null;
  }

  async setSession(sessionId, data, ttlSeconds) {
    // Always try Redis first
    try {
      await this.redis.setEx(`session:${sessionId}`, ttlSeconds, JSON.stringify(data));
    } catch (error) {
      logger.warn('Redis set failed', error.message);
    }

    // Optionally persist to PostgreSQL
    if (this.fallbackEnabled && this.dbService) {
      try {
        await this.dbService.upsertSession(sessionId, data, ttlSeconds);
      } catch (error) {
        logger.warn('PostgreSQL fallback write failed', error.message);
      }
    }
  }

  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}
```

---

## Redis Cluster Setup

### Docker Compose (Development)

```yaml
# docker-compose.yml
redis-cluster:
  image: redis:7-alpine
  command: redis-server --cluster-enabled yes --cluster-config-file nodes.conf
  ports:
    - "7000-7005:7000-7005"
  volumes:
    - redis-cluster-data:/data

volumes:
  redis-cluster-data:
```

### Production Topology

```
┌─────────────────────────────────────────────────────┐
│                    Load Balancer                       │
└──────────────────────┬──────────────────────────────┘
                       │
    ┌─────────────────┼─────────────────┐
    ▼                 ▼                 ▼
┌────────┐       ┌────────┐       ┌────────┐
│ API 1  │       │ API 2  │       │ API 3  │
└────┬───┘       └────┬───┘       └────┬───┘
     │                │                 │
     └────────────────┼────────────────┘
                      ▼
         ┌────────────────────────┐
         │   Redis Cluster (3+)    │
         │  (with Sentinel for      │
         │   automatic failover)    │
         └────────────────────────┘
```

---

## Compliance Considerations

Using PostgreSQL as fallback is **NOT** a compliance violation:

| Requirement | How We Handle It |
|-------------|-----------------|
| **Data Minimization** | Only store encrypted digests (same as Redis) |
| **Encryption** | Data is AES-256 encrypted before storage |
| **Retention** | PostgreSQL respects same TTL/retention logic |
| **Anonymization** | Cache keys use SHA-256(uuid), not raw IDs |

---

## Testing

### Test Failover

```bash
# Simulate Redis failure
REDIS_URL=invalid npm run dev

# Should fallback to PostgreSQL
# Check logs for "Redis unavailable, falling back to PostgreSQL"
```

### Test Cluster

```bash
# Enable cluster mode
REDIS_CLUSTER_ENABLED=true
REDIS_CLUSTER_NODES=redis-1:7000,redis-2:7000,redis-3:7000
npm run dev
```

---

## Monitoring

### Key Metrics

| Metric | Alert If |
|--------|----------|
| Redis hit rate | < 95% |
| Fallback to PostgreSQL | > 5% of requests |
| Redis latency | > 10ms |
| PostgreSQL fallback latency | > 100ms |

---

## Key Files

| File | Purpose |
|------|---------|
| `backend/services/cache/HybridCacheService.js` | Hybrid Redis + PostgreSQL |
| `backend/services/cache/RedisClusterCacheService.js` | Redis Cluster adapter |
| `backend/services/cache/RedisSentinelCacheService.js` | Redis Sentinel adapter |
| `backend/services/ServiceFactory.js` | Provider factory |
