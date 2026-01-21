# Railway Deployment Configuration

**CRITICAL:** This document explains the Railway deployment configuration to prevent recurring deployment failures.

**Last Updated:** 2025-12-28

---

## 🌐 Production API URL

> ⚠️ **CRITICAL:** Always use the correct API URL!

| Environment | URL | Notes |
|-------------|-----|-------|
| **Production** | `https://api.notap.io` | ✅ USE THIS |
| ~~Railway Internal~~ | ~~`*.railway.app`~~ | ❌ DO NOT USE |

**Example API Call:**
```bash
curl https://api.notap.io/health
curl https://api.notap.io/v1/auth/developer/register -X POST -H "Content-Type: application/json" -d '{...}'
```

---

## ⚠️ Common Deployment Issues

### Issue #1: "Can't find root directory"

**Symptom:** Deployment fails with "root directory not found" error

**Cause:** Missing `rootDirectory` in `railway.json`

**Fix:** Ensure `railway.json` has the correct configuration (see below)

**Prevention:** NEVER delete or modify `railway.json` without checking this documentation

---

## 📋 Required Configuration Files

### 1. `railway.json` (PROJECT ROOT)

**Location:** `/zeropay-android/railway.json`

**CRITICAL CONFIGURATION:**

```json
{
  "$schema": "https://railway.com/railway.schema.json",
  "build": {
    "builder": "DOCKERFILE",
    "buildCommand": "npm install && node scripts/setupDatabase.js",
    "dockerfilePath": "backend/Dockerfile",
    "buildEnvironment": "V3",
    "rootDirectory": "backend"  ← CRITICAL: Must be "backend"
  },
  "deploy": {
    "runtime": "V2",
    "numReplicas": 1,
    "startCommand": "npm start",
    "sleepApplication": false,
    "useLegacyStacker": false,
    "multiRegionConfig": {
      "us-east4-eqdc4a": {
        "numReplicas": 1
      }
    },
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
```

**Key Fields Explained:**

| Field | Value | Purpose |
|-------|-------|---------|
| `rootDirectory` | `"backend"` | **CRITICAL** - Tells Railway where the Node.js app is located |
| `builder` | `"DOCKERFILE"` | Use Docker for builds |
| `dockerfilePath` | `"backend/Dockerfile"` | Path to Dockerfile from project root |
| `buildCommand` | `"npm install && node scripts/setupDatabase.js"` | Install deps + setup DB |
| `startCommand` | `"npm start"` | Command to start server (runs from backend/) |
| `restartPolicyType` | `"ON_FAILURE"` | Auto-restart on crashes |
| `restartPolicyMaxRetries` | `10` | Max restart attempts |

---

### 2. `backend/Dockerfile`

**Location:** `/zeropay-android/backend/Dockerfile`

```dockerfile
FROM node:20-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy application code
COPY . .

# Expose port
EXPOSE 3000

# Start application
CMD ["npm", "start"]
```

---

### 3. `backend/package.json`

**Required scripts:**

```json
{
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js",
    "test": "mocha tests/**/*.test.js"
  }
}
```

---

## 🚀 Deployment Process

### Method 1: Git Push (Recommended)

```bash
# 1. Commit changes
git add .
git commit -m "Your commit message"

# 2. Push to master
git push origin master

# 3. Railway automatically deploys
# Watch logs: railway logs
```

### Method 2: Manual Deploy (CLI)

```bash
# From backend directory
cd backend
npx railway up --detach

# Watch deployment
npx railway logs
```

### Method 3: Redeploy (Without Code Changes)

```bash
# Trigger redeploy of latest commit
railway redeploy
```

---

## 🔍 Troubleshooting Deployments

### Check Deployment Status

```bash
# Check service status
railway status

# View recent logs
railway logs

# View build logs (in Railway dashboard)
# https://railway.com/project/YOUR_PROJECT_ID
```

### Common Errors and Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| "Can't find root directory" | Missing/incorrect `rootDirectory` in railway.json | Add `"rootDirectory": "backend"` |
| "Dockerfile not found" | Wrong `dockerfilePath` | Ensure `"dockerfilePath": "backend/Dockerfile"` |
| "npm: command not found" | Wrong base image | Use `node:20-alpine` in Dockerfile |
| "Module not found" | Dependencies not installed | Check `buildCommand` includes `npm install` |
| SyntaxError on startup | Code syntax error | Fix error and redeploy |
| Port binding error | Wrong PORT env var | Railway auto-sets PORT, use `process.env.PORT` |

### Emergency Rollback

```bash
# View recent deployments
railway deployments

# Rollback to previous deployment
railway rollback <deployment-id>
```

---

## 🔐 Environment Variables

**CRITICAL:** These must be set in Railway dashboard:

### Required Variables

```bash
# Database
DATABASE_URL=postgresql://...  # Auto-set by Railway Postgres plugin

# Redis
REDIS_HOST=...
REDIS_PORT=6379
REDIS_PASSWORD=...
REDIS_TLS_ENABLED=true

# Security
JWT_SECRET=<32-byte-hex>
ADMIN_API_KEY=<32-byte-hex>
MANAGEMENT_TOKEN_SECRET=<32-byte-hex>
WEBHOOK_SECRET=<32-byte-hex>

# Node.js
NODE_ENV=production
PORT=3000  # Auto-set by Railway
```

### Optional Variables

```bash
# Admin
ADMIN_PASSWORD_EXPIRY_DAYS=90
ADMIN_FAILED_LOGIN_LOCK_MINUTES=30

# Features
SNS_ENABLED=true
CRYPTO_PAYMENTS_ENABLED=true

# Monitoring
LOG_LEVEL=info
```

**Setting Variables:**

```bash
# Via CLI
railway variables set JWT_SECRET=your_secret_here

# Or in Railway Dashboard:
# Project → Environment → Variables
```

---

## 📊 Monitoring Deployment

### Health Checks

```bash
# Test if server is running
curl https://your-app.railway.app/health

# Test authentication endpoint
curl -X POST https://your-app.railway.app/v1/auth/developer/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test1234","fullName":"Test","company":"Test"}'
```

### View Logs

```bash
# Real-time logs
railway logs --follow

# Last 100 lines
railway logs | tail -100

# Filter for errors
railway logs | grep "ERROR\|❌"
```

---

## 🔄 CI/CD Pipeline

**Current Setup:** Push to master → Railway auto-deploys

**Build Steps:**
1. Railway detects push to master
2. Reads `railway.json` configuration
3. Changes to `backend/` directory (rootDirectory)
4. Builds Docker image using `backend/Dockerfile`
5. Runs `buildCommand`: `npm install && node scripts/setupDatabase.js`
6. Starts container with `startCommand`: `npm start`
7. Health checks on port specified by `PORT` env var

**Build Time:** ~2-3 minutes  
**Zero-Downtime:** Yes (new container starts before old one stops)

---

## 📝 Deployment Checklist

Before deploying:

- [ ] Code compiles and tests pass locally
- [ ] `railway.json` has `rootDirectory: "backend"`
- [ ] Environment variables are set in Railway
- [ ] Database migrations are ready (if needed)
- [ ] No hardcoded secrets in code
- [ ] `.env.example` is updated
- [ ] API documentation is updated
- [ ] Endpoint count is updated in `API_ENDPOINTS_INVENTORY.md`

After deploying:

- [ ] Check Railway logs for errors
- [ ] Test critical endpoints
- [ ] Run database migrations (if needed)
- [ ] Monitor for crashes/restarts
- [ ] Update deployment status documentation

---

## 🛠️ Maintenance

### Database Migrations

```bash
# Run migrations after deployment
railway run bash run-migrations.sh

# Or individual migration
railway run psql $DATABASE_URL -f database/migrations/XXX_migration_name.sql
```

### Restart Service

```bash
# Graceful restart
railway restart

# Force restart (if unresponsive)
railway redeploy
```

### Scale Replicas

```bash
# Update railway.json
{
  "deploy": {
    "numReplicas": 2  // Scale to 2 instances
  }
}

# Push changes
git add railway.json
git commit -m "Scale to 2 replicas"
git push origin master
```

---

## 📚 Related Documentation

- [API Endpoints Inventory](./API_ENDPOINTS_INVENTORY.md) - Complete endpoint catalog
- [User Authentication API](./USER_AUTHENTICATION_API.md) - Auth endpoints documentation
- [Deployment Status](./DEPLOYMENT_STATUS.md) - Current deployment info

---

## 🚨 Critical Reminders

1. **NEVER remove `rootDirectory: "backend"` from railway.json**
2. **NEVER commit `.env` files (use `.env.example` only)**
3. **ALWAYS test locally before deploying**
4. **ALWAYS run migrations after schema changes**
5. **ALWAYS update documentation when adding endpoints**

---

**Last Updated:** 2025-12-27  
**Next Review:** When deployment process changes
