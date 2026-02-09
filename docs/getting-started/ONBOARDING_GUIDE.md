# Developer Onboarding Guide

**Purpose**: Help new developers understand the NoTap codebase and become productive quickly
**Version**: 1.0.0
**Date**: 2026-02-09
**Target Audience**: New developers joining the team

---

## 🚀 Quick Start (First 2 Hours)

### 1. Environment Setup (30 minutes)
```bash
# 1. Clone repository
git clone https://github.com/NoTap-Labs/zero-pay-sdk.git
cd zeropay-android

# 2. Set up development environment
# Backend
cd backend && npm run dev
# Android (in separate terminal)
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew build
```

### 2. Understand the Codebase (1 hour)
- **Read**: `documentation/04-architecture/ARCHITECTURE.md` - System overview
- **Read**: `documentation/01-getting-started/QUICKSTART.md` - Development setup
- **Explore**: Main modules (sdk/, backend/, online-web/)

### 3. First Code Change (30 minutes)
- **Pick**: A simple bug fix or small enhancement
- **Read**: `CLAUDE.md` - Development patterns and rules
- **Run**: `./scripts/agent @all` - Pre-push quality checks

---

## 🏗️ System Architecture Overview

### Core Modules (What They Do)
```
┌─────────────────────────────────────────────────────────────┐
│                  NoTap System Architecture                │
├─────────────────────────────────────────────────────────────┤
│  SDK (KMP)           │  Backend (Node.js)    │  Web (Kotlin/JS) │
│  - 15 factors        │  - 43 API routes     │  - 10 canvases     │
│  - Crypto, ZK proofs │  - Redis + PostgreSQL│  - Enrollment flow │
│  - Platform clients   │  - Security middleware│  - Verification flow │
└─────────────────────────────────────────────────────────────┘
```

### Key Technologies
- **Kotlin Multiplatform**: Shared logic across Android/iOS/Web
- **Node.js**: API server with Express, Redis, PostgreSQL
- **Jetpack Compose**: Android UI
- **Kotlin/JS**: Web frontend with kotlinx-html
- **Zero-Knowledge Proofs**: ZK-SNARKs for privacy
- **Blockchain**: Solana integration for name services

---

## 📚 Essential Documentation (Read in Order)

### Phase 1: First Day
1. **[README.md](../README.md)** - Project overview and mission
2. **[ARCHITECTURE.md](04-architecture/ARCHITECTURE.md)** - System design
3. **[QUICKSTART.md](01-getting-started/QUICKSTART.md)** - Local development
4. **[CLAUDE.md](../CLAUDE.md)** - Development rules and patterns

### Phase 2: First Week
5. **[SECURITY_PATTERNS_REFERENCE.md](05-security/SECURITY_PATTERNS_REFERENCE.md)** - Security practices
6. **[TEST_ARCHITECTURE.md](07-testing/TEST_ARCHITECTURE.md)** - Testing approach
7. **[AGENT_COMMAND_REFERENCE.md](10-internal/AGENT_COMMAND_REFERENCE.md)** - Code quality tools

### Phase 3: As Needed
8. **[PRIVACY_IMPLEMENTATION.md](05-security/PRIVACY_IMPLEMENTATION.md)** - Privacy features
9. **[ZK_SNARK_IMPLEMENTATION_GUIDE.md](03-developer-guides/ZK_SNARK_IMPLEMENTATION_GUIDE.md)** - If working on crypto
10. **[DEPLOYMENT_READINESS.md](06-deployment/DEPLOYMENT_READINESS.md)** - If deploying to production

---

## 🎯 Development Workflow

### Daily Workflow
```bash
# 1. Sync latest changes
git pull origin master

# 2. Start services
cd backend && npm run dev &

# 3. Work on your task
# (code changes)

# 4. Run quality checks
./scripts/agent @all

# 5. Test your changes
npm test && ./gradlew test

# 6. Commit and push
git add . && git commit -m "feat: your change"
git push origin master
```

### Code Quality Checks
- **Pre-push**: Automatic via `.git/hooks/pre-push`
- **Manual**: `./scripts/agent @all` (27 security+architecture checks)
- **CI/CD**: GitHub Actions on every push

---

## 🔧 Development Commands Reference

### Backend (Node.js)
```bash
cd backend
npm run dev          # Development with hot reload
npm test             # Run all tests
npm run lint          # Code style check
npm run test:e2e      # End-to-end tests
```

### Android SDK (Kotlin)
```bash
./gradlew build       # Build all modules
./gradlew :sdk:test   # Run SDK tests
./gradlew :online-web:jsBrowserDevelopmentRun  # Web dev server
```

### Web Module (Kotlin/JS)
```bash
./gradlew :online-web:compileKotlinJs    # Compile web module
./gradlew :online-web:jsBrowserDevelopmentRun  # Start dev server
# Access at http://localhost:8080
```

---

## 🔒 Security Development Guidelines

### MUST-FOLLOW Rules
1. **Never use `Math.random()`** - Use `CryptoUtils` or `crypto.randomBytes()`
2. **Constant-time comparisons** - For any security-sensitive data
3. **Memory wiping** - Clear sensitive data after use
4. **KMP separation** - No Android imports in `commonMain`
5. **No hardcoded secrets** - Use environment variables

### Security Testing
```bash
# Run security-focused tests
cd backend && npm run test:security

# Run penetration tests
cd pentest && python3 tests/01-frida-hooking.js
```

---

## 🧪 Testing Strategy

### Test Types
1. **Unit Tests** - Individual functions/classes
2. **Integration Tests** - API endpoint testing
3. **E2E Tests** - Full user flows with Bugster
4. **Security Tests** - Penetration testing suite
5. **Performance Tests** - Load testing with k6/Artillery

### Running Tests
```bash
# Backend (all tests)
cd backend && npm test

# SDK tests
./gradlew test

# Web tests (Bugster E2E)
bugster run --verbose --headless

# Security penetration tests
cd pentest && python3 scripts/run-all-tests.sh
```

---

## 🔍 Debugging Common Issues

### Backend Won't Start
```bash
# Check Redis/PostgreSQL
docker compose up -d postgres redis

# Check ports
docker compose ps

# Check logs
docker compose logs postgres redis
```

### Android Build Fails
```bash
# Check JAVA_HOME
echo $JAVA_HOME

# Clean build
./gradlew clean build

# Check Android SDK
echo $ANDROID_HOME
```

### Web Module Issues
```bash
# Check compilation
./gradlew :online-web:compileKotlinJs

# Check dev server
./gradlew :online-web:jsBrowserDevelopmentRun
# Open browser to http://localhost:8080
```

---

## 📱 Module-Specific Development

### SDK Development (Kotlin Multiplatform)
- **Location**: `sdk/src/commonMain/kotlin/`
- **Key Files**: Factor processors, crypto utilities, API clients
- **Testing**: `sdk/src/test/`

### Backend Development (Node.js)
- **Location**: `backend/src/`
- **Key Files**: Routes, services, middleware
- **Testing**: `backend/tests/`

### Web Development (Kotlin/JS)
- **Location**: `online-web/src/jsMain/kotlin/`
- **Key Files**: Factor canvases, flows, API integration
- **Testing**: Browser-based testing with Bugster

---

## 🚨 Common Pitfalls to Avoid

### 1. Breaking Changes
- Always check for existing usages before modifying APIs
- Update all tests when changing function signatures
- Use semantic versioning for breaking changes

### 2. Security Mistakes
- Never commit secrets or API keys
- Always use constant-time operations for comparisons
- Validate all user inputs

### 3. Testing Mistakes
- Don't skip tests when refactoring
- Test both success and failure paths
- Test edge cases and error conditions

### 4. Documentation Mistakes
- Update docs when adding new features
- Keep examples current with actual code
- Document breaking changes clearly

---

## 🤝 Getting Help

### When You're Stuck
1. **Check existing documentation** - Search relevant topics first
2. **Look at similar code** - Find patterns in existing implementations
3. **Ask in team channels** - Don't struggle silently
4. **Create minimal reproduction** - For bug reports

### Team Communication
- **Technical questions**: Ask in development channels
- **Security concerns**: Use private channels for sensitive topics
- **Documentation feedback**: Create GitHub issues or PR suggestions

---

## 📈 Learning Path (First Month)

### Week 1: Foundations
- Understand system architecture
- Set up development environment
- Make first code change
- Learn security patterns

### Week 2: Module Deep Dive
- Focus on your primary module (SDK/Backend/Web)
- Read relevant architecture docs
- Study existing patterns
- Contribute to module-specific tasks

### Week 3: Cross-Module Understanding
- Learn how modules interact
- Study data flows
- Understand deployment pipeline
- Work on integration features

### Week 4: Advanced Topics
- Zero-knowledge proofs (if crypto-focused)
- Security testing and penetration testing
- Performance optimization
- Production deployment considerations

---

## 📋 Checklists

### Before Submitting Code
- [ ] Code compiles without errors
- [ ] All tests pass
- [ ] Security patterns followed
- [ ] Documentation updated
- [ ] Pre-push agent checks pass

### Before Deploying to Production
- [ ] All security tests pass
- [ ] Performance benchmarks met
- [ ] Monitoring configured
- [ ] Rollback plan ready
- [ ] Team notified

---

## 🎯 Success Metrics

### Productivity Indicators
- **Time to first contribution**: < 4 hours
- **Time to first PR merged**: < 1 week
- **Code review cycle time**: < 2 days
- **Test coverage**: > 80% for new code

### Quality Indicators
- **Zero critical security issues**
- **Zero breaking changes without documentation**
- **All tests passing in CI/CD**
- **Code follows established patterns**

---

## 🔗 Resources and Links

### Essential Links
- **Repository**: https://github.com/NoTap-Labs/zero-pay-sdk
- **Documentation**: `documentation/` (this folder)
- **Team Communication**: [Your team's chat platform]
- **Issue Tracking**: GitHub Issues

### Key Documentation Files
- **System**: `documentation/04-architecture/ARCHITECTURE.md`
- **Security**: `documentation/05-security/SECURITY_PATTERNS_REFERENCE.md`
- **Testing**: `documentation/07-testing/TEST_ARCHITECTURE.md`
- **Deployment**: `documentation/06-deployment/CI_CD_WORKFLOW_MAINTENANCE.md`

### Tools and Commands
- **Agent System**: `./scripts/agent @all`
- **Docker Services**: `docker compose up -d postgres redis`
- **Local Development**: See `documentation/01-getting-started/LOCAL_DEVELOPMENT_SETUP.md`

---

## 🚀 Welcome to the Team!

We're excited to have you join NoTap! This system represents cutting-edge work in privacy-preserving authentication and zero-knowledge proofs. 

**Your first week**: Focus on understanding and small wins
**Your first month**: Become productive in your primary module  
**Long-term goal**: Master the system and contribute to major features

**Remember**: It's okay to ask questions and make mistakes. We're here to help you succeed!

---

**Last Updated**: 2026-02-09
**Next Review**: When major architectural changes occur
**Maintainers**: Development team leads