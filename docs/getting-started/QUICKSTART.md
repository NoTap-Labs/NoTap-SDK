# ZeroPay SDK - Internal Development Quick Start

**Version:** 2.0.0 (internal build)
**Last Updated:** 2026-02-09
**Target:** Internal developers working on the codebase

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Building the SDK](#building-the-sdk)
4. [Running the Backend](#running-the-backend)
5. [Testing](#testing)
6. [Module Architecture](#module-architecture)
7. [API Endpoints](#api-endpoints)
8. [Development Workflow](#development-workflow)

---

## Prerequisites

| Requirement | Version | Purpose |
|-------------|---------|---------|
| **Java JDK** | 17+ | Android SDK compilation |
| **Android SDK** | Latest | Android development |
| **Node.js** | 18+ | Backend API server |
| **Docker** | Latest | PostgreSQL + Redis containers |
| **Gradle** | 8.0+ | Build system |
| **Git** | Latest | Version control |

**Verify setup:**
```bash
java -version                    # OpenJDK 17+
node -v                         # v18+
npm -v                          # 9+
docker --version                 # 24.x
./gradlew --version              # 8.x
```

---

## Local Development Setup

### 1. Clone Repository
```bash
git clone https://github.com/keikworld/zero-pay-sdk.git
cd zeropay-android
```

### 2. Backend Setup (5 minutes)
```bash
cd backend

# Create environment file
cp .env.local.example .env.local

# Install dependencies
npm install

# Start PostgreSQL + Redis
docker compose up -d postgres redis

# Wait 30 seconds for services to start
sleep 30

# Run database migrations
npm run db:migrate

# Start backend server
npm run dev
```

**Backend will run on:** `http://localhost:3000`

### 3. Android Setup
```bash
# Return to root directory
cd ..

# Set ANDROID_HOME environment variable
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Create local.properties if needed
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

---

## Building the SDK

### Build All Modules
```bash
./gradlew clean build --console=plain
```

### Build Specific Modules
```bash
# Core SDK (Kotlin Multiplatform)
./gradlew :sdk:build

# Android Enrollment Module
./gradlew :enrollment:build

# Android Merchant Module  
./gradlew :merchant:build

# Web Module (Kotlin/JS)
./gradlew :online-web:compileKotlinJs
```

### Run Web Development Server
```bash
./gradlew :online-web:jsBrowserDevelopmentRun
# Access at: http://localhost:8080
```

---

## Running the Backend

### Development Mode (Hot Reload)
```bash
cd backend
npm run dev
```

### Production Mode
```bash
cd backend
npm start
```

### Test Backend Health
```bash
curl http://localhost:3000/health
```

**Expected response:**
```json
{
  "status": "healthy",
  "timestamp": "2026-02-09T01:00:00.000Z",
  "services": {
    "redis": "connected",
    "postgres": "connected"
  }
}
```

---

## Testing

### Backend Tests
```bash
cd backend

# All tests
npm test

# Unit tests only
npm run test:unit

# Integration tests
npm run test:integration

# E2E tests
npm run test:e2e
```

### SDK Tests
```bash
# All SDK tests
./gradlew :sdk:test

# Android tests
./gradlew :enrollment:test
./gradlew :merchant:test
```

### Web Tests (Kotlin/JS)
```bash
./gradlew :online-web:jsBrowserTest
```

---

## Module Architecture

### Core Modules

| Module | Purpose | Platform | Key Files |
|--------|---------|----------|-----------|
| **sdk** | Core factors, crypto, networking | KMP (Android + Web) | `sdk/src/commonMain/kotlin/` |
| **enrollment** | 5-step enrollment wizard | Android | `enrollment/src/androidMain/kotlin/` |
| **merchant** | 4-screen verification flow | Android | `merchant/src/androidMain/kotlin/` |
| **online-web** | Web enrollment + verification | Kotlin/JS | `online-web/src/jsMain/kotlin/` |
| **backend** | Node.js API server | Node.js | `backend/` |

### Available Authentication Factors (15 total)

| Category | Factors |
|----------|---------|
| **Knowledge** | PIN, Pattern, Words, Colour, Emoji |
| **Biometric** | Face, Fingerprint, Voice |
| **Behavioral** | RhythmTap, MouseDraw, StylusDraw, ImageTap |
| **Possession** | NFC |
| **Location** | Balance |

**Minimum required:** 3 factors (6+ recommended for maximum security)

---

## API Endpoints

All endpoints require backend running at `http://localhost:3000`

### Authentication Routes
```bash
# Enrollment
POST /v1/enrollment/store
GET  /v1/enrollment/retrieve/:uuid
PUT  /v1/enrollment/update
DELETE /v1/enrollment/delete/:uuid

# Verification  
POST /v1/verification/initiate
POST /v1/verification/verify

# Names (blockchain)
GET  /v1/names/resolve/:name
POST /v1/names/associate
```

### Admin Routes (require ADMIN_API_KEY)
```bash
# User management
GET    /v1/admin/users
POST   /v1/admin/users
PUT    /v1/admin/users/:uuid
DELETE /v1/admin/users/:uuid

# Configuration
GET    /v1/admin/config
PUT    /v1/admin/config
```

### Sandbox Routes (testing)
```bash
# Test tokens
GET /v1/sandbox/tokens

# Mock enrollments
POST /v1/sandbox/enrollments/create

# Mock name resolution
GET /v1/sandbox/names/resolve/:name
```

### API Key Format
- **Sandbox:** Generated by backend `TEST_API_KEYS` in `backend/config/sandboxConfig.js`
- **Production:** Set via `ADMIN_API_KEY` environment variable
- **Format:** 32-byte hex string (e.g., `3eecf593219c6603e800ebd8d3a5bee7252df43ad17aa3650045174a239a8544`)

---

## Development Workflow

### Daily Workflow
```bash
# 1. Sync with main branch
git pull origin master

# 2. Start backend services
cd backend && docker compose up -d && npm run dev

# 3. Build your target module
./gradlew :sdk:build  # or :enrollment:build

# 4. Run tests
npm test && ./gradlew :sdk:test

# 5. Make changes and iterate
```

### Code Quality Checks
```bash
# Run agent system (all 27 checks)
./scripts/agent @all

# Individual agents
./scripts/agent @sentry      # Pre-push checks (9 checks)
./scripts/agent @architect   # Architecture patterns (12 checks) 
./scripts/agent @compliance  # GDPR/PSD3 compliance (6 checks)
```

### Building for Production
```bash
# Clean production build
./gradlew clean build

# Web production build
./gradlew :online-web:jsBrowserProductionWebpack

# Android release builds
./gradlew :enrollment:assembleRelease
./gradlew :merchant:assembleRelease
```

---

## Key Configuration Files

| File | Purpose |
|------|---------|
| `backend/.env.local` | Local development environment |
| `backend/docker-compose.yml` | PostgreSQL + Redis services |
| `build.gradle.kts` (root) | Project-wide build configuration |
| `sdk/build.gradle.kts` | KMP SDK configuration |
| `local.properties` | Android SDK path (auto-generated) |
| `gradle.properties` | Gradle optimization settings |

---

## Security Notes

### Local Development Only
- **DO NOT** commit `.env.local` files
- **USE** provided test credentials in `backend/config/sandboxConfig.js`
- **NEVER** use production secrets in local development

### API Security
- All state-changing endpoints require replay protection
- Rate limiting enforced per IP and UUID
- Double encryption (AES-256-GCM + KMS) for enrollment data
- Memory wiping for sensitive data

---

## Troubleshooting

### Common Issues

**"SDK location not found"**
```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

**"PostgreSQL connection refused"**
```bash
docker compose up -d postgres
docker compose logs postgres
```

**"Redis connection failed"**
```bash
docker compose up -d redis
docker exec -it notap-redis redis-cli ping
```

**"Gradle build failed"**
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

---

## Documentation Structure

For detailed information:
- **Architecture:** `documentation/ARCHITECTURE.md`
- **Security:** `documentation/SECURITY_AUDIT.md`  
- **Testing:** `documentation/07-testing/TEST_ARCHITECTURE.md`
- **Deployment:** `documentation/06-deployment/`
- **API Reference:** `backend/docs/`

---

## Support & Resources

### Internal Resources
- **Code repository:** `https://github.com/keikworld/zero-pay-sdk`
- **Issue tracking:** GitHub Issues in repository
- **Build status:** GitHub Actions (automatic on push)

### Development Commands Reference
```bash
# === SETUP ===
cd backend && ./scripts/local-setup.sh

# === DEVELOPMENT ===  
npm run dev                    # Backend with hot reload
./gradlew :sdk:build          # Build SDK

# === TESTING ===
npm test                       # Backend tests
./gradlew test                 # All Gradle tests

# === CODE QUALITY ===
./scripts/agent @all          # All quality checks
```

---

**End of Internal Development Quick Start**