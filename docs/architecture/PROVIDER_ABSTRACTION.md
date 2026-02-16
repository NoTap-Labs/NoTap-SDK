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
- [DEVELOPMENT_RULES.md](../03-developer-guides/DEVELOPMENT_RULES.md) - Section 12: Scalability Rules
