# NoTap Web Frontend Deployment Guide

## Overview

This document explains how to deploy the NoTap web frontend (enrollment, verification, management portal) to make it accessible to users for testing.

## Live URLs

- **Enrollment**: https://app.notap.io/enroll
- **Verification**: https://app.notap.io/verify
- **Management**: https://app.notap.io/manage
- **Developer Portal**: https://app.notap.io/developer
- **Backend API**: https://api.notap.io

## Architecture

```
Frontend: https://app.notap.io (Railway - static site)
Backend:  https://api.notap.io (Railway - Node.js API)
```

The web frontend is a **static site** (HTML/CSS/JavaScript compiled from Kotlin/JS) that calls the backend API.

## Deployment Method: Railway with Docker

### Prerequisites

1. Railway CLI installed: `npm install -g @railway/cli`
2. Railway account and project access
3. Production bundle built: `gradlew :online-web:jsBrowserProductionWebpack`

### Key Files

**railway.json** (CRITICAL - see notes below):
```json
{
  "$schema": "https://railway.com/railway.schema.json",
  "build": {
    "dockerfilePath": "Dockerfile"
  },
  "deploy": {
    "startCommand": "sh -c \"serve -s /app/public -l tcp://0.0.0.0:${PORT}\""
  }
}
```

**Dockerfile**:
```dockerfile
FROM node:20-alpine
WORKDIR /app
RUN npm install -g serve@14.2.1
COPY build/dist/js/productionExecutable /app/public
RUN addgroup -g 1001 -S nodejs && adduser -S notap -u 1001 && chown -R notap:nodejs /app
USER notap
CMD ["serve", "-s", "/app/public"]
```

### Deployment Steps

1. **Build production bundle:**
   ```bash
   cd /path/to/zeropay-android
   ./gradlew :online-web:jsBrowserProductionWebpack
   ```

2. **Link to Railway service:**
   ```bash
   cd online-web
   npx railway link
   # Select: web-frontend
   ```

3. **Set PORT to match service port:**
   ```bash
   # Check service port in Railway dashboard: Settings → Networking
   npx railway variables --set "PORT=8080"
   ```

4. **Deploy:**
   ```bash
   npx railway up
   ```

### Troubleshooting 502 Errors

If you get 502 "Application failed to respond":

1. **Check port match:**
   - Railway service port (Settings → Networking): typically 8080
   - App listening port (deploy logs): should match
   - Fix: `npx railway variables --set "PORT=8080"`

2. **Check variable expansion:**
   - Logs show literal `$PORT`? Variable not expanding
   - Fix: Use double quotes in startCommand: `"sh -c \"serve ... ${PORT}\""`
   - Single quotes prevent expansion!

3. **Check host binding:**
   - Logs show `localhost:8080`? Only accessible from inside container
   - Fix: Use `tcp://0.0.0.0:${PORT}` for 0.0.0.0 binding

4. **Check file encoding (WSL/Windows):**
   - Error: "invalid character '\x00'" = null bytes in file
   - Fix: Recreate file with heredoc:
     ```bash
     cat > railway.json << 'EOF'
     { ... }
     EOF
     ```

### Correct Deploy Logs

After successful deployment, logs should show:
```
Starting Container
 UPDATE  The latest version of `serve` is 14.2.5
 INFO  Accepting connections at http://0.0.0.0:8080
```

Key indicators:
- ✅ `0.0.0.0:8080` (correct host and port)
- ❌ `localhost:8080` (wrong host - 502 error)
- ❌ `0.0.0.0:$PORT` with deprecation warning (variable not expanded)

## Production Bundle

The production bundle must be committed to git for Railway deployment:

**Location:** `online-web/build/dist/js/productionExecutable/`

**Files:**
- `index.html` (~47 KB)
- `online-web.js` (~387 KB)
- `wallet.css` (~7.7 KB)

**Gitignore exception** (in root `.gitignore`):
```gitignore
build/
!online-web/build/
online-web/build/*
!online-web/build/dist/
online-web/build/dist/*
!online-web/build/dist/js/
online-web/build/dist/js/*
!online-web/build/dist/js/productionExecutable/
```

## Testing the Deployment

**Quick HTTP test:**
```bash
curl -sI https://app.notap.io
# Should return: HTTP/2 200
```

**Page content test:**
```bash
curl -s https://app.notap.io | grep -o '<title>.*</title>'
# Should return: <title>ZeroPay - Secure Payment Authentication</title>
```

**All routes (SPA - all return same HTML):**
```bash
for route in / /enroll /verify /manage /developer; do
  echo -n "$route: "
  curl -sI "https://app.notap.io$route" | head -1
done
```

## Bugster E2E Tests

Web UI tests are in `.bugster/tests/`:

- `enrollment_flow.yaml` - Full enrollment with PIN and Pattern
- `verification_flow.yaml` - Payment verification with enrolled factors
- `management_portal.yaml` - Account management and factor viewing

Run with:
```bash
bugster run --verbose
```

## Related Documentation

- **Lesson 35**: Kotlinx-html DSL patterns (LESSONS_LEARNED.md)
- **Lesson 36**: Railway deployment patterns (LESSONS_LEARNED.md)
- **Backend Deployment**: See root `railway.json`
