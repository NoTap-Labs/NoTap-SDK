# NoTap Troubleshooting Guide

**Purpose**: Resolve common development issues quickly
**Version**: 1.0.0
**Date**: 2026-02-09

---

## 🚨 Quick Diagnosis Flow

### Step 1: Identify the Problem Area
```
┌─────────────────────────────────────────┐
│          What's not working?         │
├─────────────────────────────────────────┤
│ □ Backend won't start               │
│ □ Android build fails               │  
│ □ Web module compilation errors      │
│ □ Tests are failing                 │
│ □ Authentication not working         │
│ □ Performance issues                 │
└─────────────────────────────────────────┘
```

### Step 2: Check Common Causes
1. **Environment variables** - Missing or incorrect
2. **Dependencies** - Outdated or conflicting
3. **Services** - Database, Redis not running
4. **Configuration** - Wrong ports, invalid paths
5. **Permissions** - File access, network issues

### Step 3: Use This Guide
- Find your section below
- Try solutions in order (easiest first)
- Check logs for specific error messages
- Contact team if still stuck

---

## 🔧 Environment Setup Issues

### Backend Won't Start

#### Problem: `npm run dev` fails
```bash
# 1. Check Node.js version
node --version  # Should be 18+

# 2. Check environment file
ls -la backend/.env  # Should exist and be readable

# 3. Check dependencies
cd backend && npm install

# 4. Check Redis/PostgreSQL
docker compose ps  # Should show running services

# 5. Check ports (Windows WSL specific)
netstat -tulpn | grep :3000
```

#### Common Solutions
- **Missing Redis**: `docker compose up -d redis`
- **Port conflict**: Change PORT in .env
- **Permission denied**: `chmod +x scripts/*.sh`
- **Module not found**: `npm install` to install dependencies

#### Backend Logs
```bash
# Check startup logs
cd backend && npm run dev 2>&1 | head -20

# Check specific error
tail -f logs/app.log
```

### Android Build Issues

#### Problem: Gradle build fails
```bash
# 1. Check Java version
java -version  # Should be 17+

# 2. Check ANDROID_HOME
echo $ANDROID_HOME

# 3. Clean build
./gradlew clean

# 4. Check dependencies
./gradlew dependencies
```

#### Common Solutions
- **JAVA_HOME not set**:
  ```bash
  export JAVA_HOME=$HOME/Android/Sdk
  export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
  ```
- **Android SDK missing**: Install Android Studio or SDK tools
- **Gradle daemon issues**: `./gradlew --stop` then build
- **Outdated dependencies**: `./gradlew build --refresh-dependencies`

#### Build Variations
```bash
# Build specific module
./gradlew :sdk:build
./gradlew :enrollment:assembleDebug

# Build with info
./gradlew build --info

# Skip tests
./gradlew build -x test
```

### Web Module Issues

#### Problem: Kotlin/JS compilation fails
```bash
# 1. Check Node.js (required for Kotlin/JS)
node --version

# 2. Clean build
./gradlew :online-web:clean

# 3. Check dependencies
./gradlew :online-web:dependencies
```

#### Common Solutions
- **Missing Node.js**: Install Node.js 18+ from nodejs.org
- **Browser development server**: Access at http://localhost:8080
- **JavaScript interop**: Check console for runtime errors
- **Module dependencies**: Run `npm install` in project root

---

## 🗄️ Database & Redis Issues

### Database Connection Problems

#### PostgreSQL Issues
```bash
# 1. Check if PostgreSQL is running
docker compose ps

# 2. Check PostgreSQL logs
docker compose logs postgres

# 3. Test connection
docker exec -it notap-postgres psql -U notap -d notap_dev -c "SELECT 1;"

# 4. Check network
docker network ls
```

#### Common Solutions
- **Connection refused**: Start PostgreSQL service
- **Authentication failed**: Check POSTGRES_PASSWORD in .env
- **Database not found**: Run migrations: `npm run db:migrate`
- **Permission denied**: Check user permissions in Docker

#### Redis Issues
```bash
# 1. Test Redis connection
docker exec -it notap-redis redis-cli -a notap_redis_dev_2024 ping

# 2. Check Redis logs
docker compose logs redis

# 3. Check Redis configuration
redis-cli -a notap_redis_dev_2024 CONFIG GET maxmemory
```

#### Common Solutions
- **Redis not running**: `docker compose up -d redis`
- **Wrong password**: Check REDIS_PASSWORD in .env
- **Memory issues**: Increase maxmemory in redis.conf
- **TLS errors**: Use dev mode (port 6379) vs prod mode (6380)

---

## 🧪 Testing Issues

### Backend Tests Failing

#### Problem: `npm test` fails
```bash
# 1. Check test database setup
npm run test:setup

# 2. Run specific test file
npx mocha tests/unit/crypto.test.js --timeout 5000

# 3. Run with verbose output
npm test -- --verbose
```

#### Common Solutions
- **Test database**: Clear with `npm run test:clean`
- **Redis conflicts**: Stop Redis during tests or use test instance
- **Timeout errors**: Increase timeout with `--timeout 10000`
- **Environment variables**: Check .env.test matches .env.example

### Android Tests Failing

#### Problem: Gradle tests fail
```bash
# 1. Run specific test class
./gradlew :sdk:test --tests "*CryptoUtilsTest"

# 2. Run with info
./gradlew test --info

# 3. Run with debugging
./gradlew test --debug-jvm
```

#### Common Solutions
- **Emulator issues**: Start Android emulator first
- **Missing test dependencies**: Run `./gradlew testClasses`
- **Kotlin version**: Ensure Kotlin version matches project
- **Memory issues**: Increase Gradle heap: `./gradlew -Dorg.gradle.jvmargs=-Xmx4g test`

---

## 🔐 Authentication Issues

### Enrollment Problems

#### Problem: Enrollment fails
```bash
# 1. Check backend health
curl http://localhost:3000/health

# 2. Check enrollment endpoint
curl -X POST http://localhost:3000/v1/enrollment/store \
  -H "Content-Type: application/json" \
  -d '{"user_uuid": "test", "factors": {}}'

# 3. Check Redis data
docker exec -it notap-redis redis-cli -a notap_redis_dev_2024 keys "*"
```

#### Common Solutions
- **Factor validation**: Check factor format requirements
- **Missing required fields**: Ensure all required factors present
- **Nonce validation**: Check X-Nonce and X-Timestamp headers
- **Session timeout**: Re-enrollment may be needed after 24 hours

### Verification Problems

#### Problem: Verification fails
```bash
# 1. Check verification initiation
curl -X POST http://localhost:3000/v1/verification/initiate \
  -H "Content-Type: application/json" \
  -d '{"user_uuid": "test"}'

# 2. Check factor challenges
curl -X GET http://localhost:3000/v1/verification/session/123

# 3. Check Redis session data
docker exec -it notap-redis redis-cli -a notap_redis_dev_2024 get "session:123"
```

#### Common Solutions
- **Invalid session**: Session may have expired (24 hour TTL)
- **Factor mismatch**: Ensure using enrolled factors
- **Transaction binding**: Check amount and merchant match
- **Rate limiting**: Too many attempts, wait for cooldown

---

## 🚀 Performance Issues

### Slow Response Times

#### Backend Performance
```bash
# 1. Check response times
curl -w "@{time_total}\n" http://localhost:3000/health -o /dev/null

# 2. Check system resources
docker stats  # CPU, memory usage

# 3. Check database performance
docker exec -it notap-postgres psql -U notap -d notap_dev -c "SELECT * FROM pg_stat_activity;"
```

#### Common Solutions
- **High CPU usage**: Check for infinite loops, optimize algorithms
- **Memory leaks**: Look for unclosed database connections
- **Slow queries**: Run `EXPLAIN ANALYZE` on slow queries
- **Redis optimization**: Use SCAN instead of KEYS

### Memory Issues

#### Backend Memory Usage
```bash
# 1. Check Node.js memory usage
docker stats --no-stream | grep notap-backend

# 2. Check for memory leaks
node --inspect app.js &  # Node.js inspector
# Visit chrome://inspect
```

#### Common Solutions
- **Memory leaks**: Check for unclosed database connections
- **Large objects**: Implement streaming for large datasets
- **Garbage collection**: Add `node --max-old-space-size` if needed
- **Buffer management**: Ensure buffers are properly cleared

---

## 🔍 Debugging Techniques

### Backend Debugging

#### Node.js Debugging
```bash
# 1. Start with debug flags
NODE_ENV=development node --inspect-brk app.js

# 2. Or use Chrome DevTools
node --inspect app.js
# Open Chrome DevTools -> Node icon

# 3. Add debug logs
console.log('Debug point 1:', { data });
console.trace('Execution trace');
```

#### Database Debugging
```bash
# 1. Enable query logging
docker exec -it notap-postgres psql -U notap -d notap_dev -c "ALTER SYSTEM SET log_min_duration_statement TO 0;"

# 2. Monitor slow queries
docker exec -it notap-postgres psql -U notap -d notap_dev -c "SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 5;"
```

### Android Debugging

#### Android Studio Debugging
```kotlin
// Add debug logs
Log.d("NoTap", "Debug message: $data")

// Add breakpoints
// Use Android Studio debugger
// Set breakpoints in Android Studio
```

#### Gradle Debugging
```bash
# Run with debug info
./gradlew build --info --stacktrace

# Run specific task with debugging
./gradlew :sdk:test --debug --tests "*CryptoTest"
```

### Web Debugging

#### Browser Debugging
```kotlin
// Add debug logging in Kotlin/JS
console.log("Debug: $data")

// Check browser console
// Open Developer Tools (F12)
// Check Network tab for API calls
// Check Console tab for JavaScript errors
```

---

## 🌐 Network & Connectivity Issues

### Local Network Problems

#### Backend Not Accessible
```bash
# 1. Check if backend is running
curl http://localhost:3000/health

# 2. Check if port is bound
netstat -tulpn | grep :3000

# 3. Check firewall
sudo ufw status  # Ubuntu
sudo firewall-cmd --list-all  # CentOS/RHEL
```

#### Docker Network Issues
```bash
# 1. Check Docker network
docker network ls
docker network inspect no-cash-notap

# 2. Restart Docker services
docker compose down
docker compose up -d

# 3. Rebuild if needed
docker compose build --no-cache
```

### API Connection Issues

#### CORS Problems
```bash
# 1. Test CORS preflight
curl -H "Origin: http://localhost:8080" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS http://localhost:3000/v1/enrollment/store

# 2. Check CORS configuration
grep -r "cors" backend/middleware/
```

#### Common Solutions
- **CORS errors**: Update CORS_ORIGIN in .env
- **CORS preflight**: Check Access-Control-Allow-Headers
- **Network timeouts**: Increase timeout in API clients
- **DNS issues**: Use IP addresses instead of hostnames

---

## 📱 Platform-Specific Issues

### Windows/WSL Issues

#### File Path Problems
```bash
# 1. Use Windows paths for Gradle
cmd.exe /c "gradlew.bat build --no-daemon"

# 2. Check file permissions
ls -la /mnt/c/Users/  # Should show your files

# 3. Check environment variables
env | grep -E "JAVA_HOME|ANDROID_HOME"
```

#### Common Solutions
- **Path separators**: Use Windows paths in cmd.exe calls
- **File permissions**: Run `chmod +x` scripts
- **Environment variables**: Set in Windows system, not just WSL
- **Docker issues**: Use Windows Docker Desktop

### macOS Issues

#### Build Permission Issues
```bash
# 1. Fix permissions
chmod +x gradlew

# 2. Clear Gradle cache
./gradlew clean

# 3. Use system Java if needed
export JAVA_HOME=$(/usr/libexec/java_home -v 2.0)

# 4. Check Xcode command line tools
xcode-select --install
```

---

## 🔧 Development Tool Issues

### IDE/Editor Problems

#### IntelliJ IDEA Issues
```bash
# 1. Invalidate caches
File -> Invalidate Caches / Restart

# 2. Clear Gradle caches
./gradlew clean

# 3. Check project structure
# File -> Project Structure -> Reload
```

#### VS Code Issues
```bash
# 1. Check extensions
code --list-extensions

# 2. Check workspace settings
code .vscode/settings.json

# 3. Check for syntax errors
```

### Docker Issues
```bash
# 1. Restart Docker daemon
sudo systemctl restart docker

# 2. Clear Docker cache
docker system prune -f

# 3. Rebuild images
docker compose build --no-cache
```

---

## 🆘 Getting Help

### When to Ask for Help
- **After trying all solutions** in relevant section
- **When error messages are unclear** or not documented
- **When experiencing new issues** not covered here
- **Before making significant changes** that might break things

### How to Ask for Help Effectively

#### Create Good Bug Reports
```markdown
## Issue Description
- What were you trying to do?
- What happened instead?
- What did you expect to happen?

## Environment
- OS: Windows/macOS/Linux
- Node.js version: 
- Docker version:
- Browser: Chrome/Firefox/Safari

## Steps to Reproduce
1. Command/action: 
2. Expected result:
3. Actual result:

## Error Messages
- Paste full error output
- Include stack traces if available
```

#### Where to Ask
- **Technical questions**: Team development channel
- **Bug reports**: GitHub Issues with template
- **Security concerns**: Private channel to security team
- **Documentation feedback**: Create issue or PR

### Community Resources
- **GitHub Discussions**: https://github.com/NoTap-Labs/zero-pay-sdk/discussions
- **Stack Overflow**: Tag questions with `notap` or `zeropay`
- **Documentation**: Check `documentation/` directory first
- **Search existing issues**: Check if already reported

---

## 📋 Common Error Messages

### Backend Error Messages
```
Error: listen EADDRINUSE :::3000
→ Solution: Kill process on port 3000 or change PORT

Error: connect ECONNREFUSED 127.0.0.1:5432
→ Solution: Start PostgreSQL service

Error: Redis connection failed
→ Solution: Start Redis service, check REDIS_PASSWORD

JWT_SECRET must be at least 32 characters
→ Solution: Update JWT_SECRET in .env

Timed out waiting for database
→ Solution: Check PostgreSQL health, network connectivity
```

### Android Error Messages
```
Could not resolve com.android.tools.build:gradle:X.X.X
→ Solution: Update Gradle version in gradle/wrapper/gradle-wrapper.properties

Failed to install Gradle distribution
→ Solution: Check network, use Gradle wrapper

SDK location not configured
→ Solution: Set ANDROID_HOME environment variable

Failed to find target with hash string
→ Solution: Run `./gradlew clean`

Compile error: "Kotlin version mismatch"
→ Solution: Update Kotlin plugin version
```

### Web Module Error Messages
```
TypeError: Cannot read property 'length' of null
→ Solution: Check for null values before accessing properties

ReferenceError: variable is not defined
→ Solution: Check variable scope in JavaScript interop

Module not found: @kotlinx-html
→ Solution: Add kotlinx-html dependency

Build failed: JavaScript stack overflow
→ Solution: Check for infinite loops, recursion limits
```

---

## 🔧 Performance Tuning

### Backend Optimization
```javascript
// 1. Enable compression
app.use(compression());

// 2. Add caching
app.use('/api/data', cacheMiddleware);

// 3. Optimize database queries
// Use indexes, limit results

// 4. Monitor performance
const startTime = Date.now();
// ... code ...
const duration = Date.now() - startTime;
console.log(`Request took ${duration}ms`);
```

### Android Optimization
```kotlin
// 1. Use ViewBinding
// Avoid findViewById calls

// 2. Optimize layouts
// Use ConstraintLayout, avoid nested layouts

// 3. Use proper threading
// Coroutines for background tasks

// 4. Optimize images
// Use WebP format, appropriate sizing
```

---

**Last Updated**: 2026-02-09  
**Next Review**: As new issues are discovered  
**Maintainers**: Development team leads

For issues not covered here, please create a GitHub issue with detailed information about your environment and steps to reproduce.