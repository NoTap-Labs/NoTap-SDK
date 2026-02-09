# ZeroPay Web Module - Internal Development Guide

**Version:** 1.0.0 (internal build)
**Last Updated:** 2026-02-09
**Target:** Internal developers working on the web module

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Web Module Architecture](#web-module-architecture)
3. [Building the Web Module](#building-the-web-module)
4. [Development Server](#development-server)
5. [Factor Canvases](#factor-canvases)
6. [API Integration](#api-integration)
7. [Testing](#testing)
8. [Production Build](#production-build)

---

## Prerequisites

| Requirement | Version | Purpose |
|-------------|---------|---------|
| **Java JDK** | 17+ | Kotlin compilation |
| **Node.js** | 18+ | Backend API (required) |
| **Docker** | Latest | PostgreSQL + Redis |
| **Gradle** | 8.0+ | Build system |
| **Chrome/Edge** | Latest | Development browser |

**Verify setup:**
```bash
java -version                    # OpenJDK 17+
node -v                         # v18+
./gradlew --version              # 8.x
```

---

## Web Module Architecture

### Module Structure
```
online-web/
├── src/
│   ├── jsMain/
│   │   ├── kotlin/              # Kotlin/JS source code
│   │   │   └── com/zeropay/web/
│   │   │       ├── enrollment/  # Enrollment flow
│   │   │       ├── verification/ # Verification flow  
│   │   │       ├── management/  # Management portal
│   │   │       ├── developer/   # Developer portal
│   │   │       └── factor/      # Factor canvases
│   │   └── resources/          # HTML/CSS/JSON resources
│   └── jsTest/                 # Tests
└── build.gradle.kts             # Build configuration
```

### Key Dependencies
```kotlin
// From build.gradle.kts
dependencies {
    // Core SDK (shared with Android)
    implementation(project(":sdk"))
    
    // Kotlin/JS essentials
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js:1.9.22")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // HTTP client
    implementation("io.ktor:ktor-client-js:2.3.7")
    
    // HTML generation
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.9.1")
    
    // DateTime support
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
}
```

---

## Building the Web Module

### 1. Setup Backend First
```bash
# Backend must be running for web module to work
cd backend
npm run dev
# Backend runs on http://localhost:3000
```

### 2. Build Web Module
```bash
# Return to root directory
cd ..

# Clean build
./gradlew :online-web:clean

# Compile Kotlin/JS
./gradlew :online-web:compileKotlinJs

# Build with dependencies
./gradlew :online-web:build
```

### 3. Development Server
```bash
# Start development server with hot reload
./gradlew :online-web:jsBrowserDevelopmentRun
```

**Access at:** `http://localhost:8080`

---

## Development Server

### What Gets Served
- **Enrollment Flow:** `http://localhost:8080/enrollment`
- **Verification Flow:** `http://localhost:8080/verification`
- **Management Portal:** `http://localhost:8080/management`
- **Developer Portal:** `http://localhost:8080/developer`

### Development Features
- **Hot reload:** Changes auto-compile and refresh
- **Source maps:** Debug original Kotlin code
- **Console logging:** Full stack traces available

### Troubleshooting Dev Server
```bash
# Check if port 8080 is available
lsof -i :8080

# Kill existing process
kill -9 <PID>

# Clear build cache
./gradlew :online-web:clean
```

---

## Factor Canvases

The web module provides browser-based implementations of all 15 authentication factors:

### Knowledge Factors
```kotlin
// PIN Canvas
PINFactor.render(root, onComplete)

// Pattern Canvas  
PatternFactor.render(root, onComplete)

// Words Canvas
WordsFactor.render(root, onComplete)

// Colour Canvas
ColourFactor.render(root, onComplete)

// Emoji Canvas
EmojiFactor.render(root, onComplete)
```

### Biometric Factors
```kotlin
// Voice Canvas (requires microphone)
VoiceFactor.render(root, onComplete)

// Face Canvas (requires camera)
FaceFactor.render(root, onComplete)

// Fingerprint Canvas (requires WebAuthn)
FingerprintFactor.render(root, onComplete)
```

### Behavioral Factors
```kotlin
// Rhythm Tap Canvas
RhythmTapFactor.render(root, onComplete)

// Mouse Draw Canvas
MouseDrawFactor.render(root, onComplete)

// Stylus Draw Canvas
StylusDrawFactor.render(root, onComplete)

// Image Tap Canvas
ImageTapFactor.render(root, onComplete)
```

### Other Factors
```kotlin
// NFC Canvas (Web NFC API)
NFCFactor.render(root, onComplete)

// Balance Canvas (DeviceMotion API)
BalanceFactor.render(root, onComplete)
```

### Canvas Implementation Pattern
```kotlin
object FactorCanvas {
    fun render(root: HTMLDivElement, onComplete: (ByteArray) -> Unit) {
        root.append {
            div(classes = "factor-container") {
                h2 { +"Factor Title" }
                
                // Interactive elements
                button {
                    onClick = {
                        // Process input
                        val digest = processor.process(input)
                        onComplete(digest)
                    }
                    +"Submit"
                }
            }
        }
    }
}
```

---

## API Integration

The web module communicates with the backend API using Ktor client:

### API Client Setup
```kotlin
// In online-web/src/jsMain/kotlin/com/zeropay/web/api/
class ApiClient(private val baseUrl: String = "http://localhost:3000") {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    
    suspend fun storeEnrollment(data: EnrollmentData): ApiResponse {
        return client.post("$baseUrl/v1/enrollment/store") {
            setBody(data)
            header("Content-Type", "application/json")
        }.body()
    }
}
```

### Available Endpoints
```kotlin
// Enrollment
POST /v1/enrollment/store
GET  /v1/enrollment/retrieve/:uuid

// Verification
POST /v1/verification/initiate
POST /v1/verification/verify

// Names (blockchain)
GET  /v1/names/resolve/:name
POST /v1/names/associate

// Sandbox (testing)
GET /v1/sandbox/tokens
POST /v1/sandbox/enrollments/create
```

### Error Handling
```kotlin
try {
    val response = apiClient.storeEnrollment(enrollmentData)
    if (response.success) {
        // Success flow
        navigateToSuccess()
    } else {
        // Show error
        showError(response.error)
    }
} catch (e: Exception) {
    // Network or parsing error
    showNetworkError()
}
```

---

## Testing

### Run Web Tests
```bash
# Kotlin/JS tests
./gradlew :online-web:jsTest

# Browser tests (Chrome Headless)
./gradlew :online-web:jsBrowserTest
```

### Test Structure
```kotlin
// online-web/src/jsTest/kotlin/com/zeropay/web/
class FactorCanvasTest {
    @Test
    fun `PIN canvas should validate input`() {
        // Test PIN validation logic
        val processor = PINProcessor()
        val result = processor.process("1234")
        assertTrue(result.isNotEmpty())
    }
}
```

### Integration Testing
```bash
# Start backend first
cd backend && npm run dev

# In another terminal, run integration tests
./gradlew :online-web:jsBrowserDevelopmentRun
# Manual testing via browser at localhost:8080
```

---

## Production Build

### Build for Production
```bash
# Production webpack build
./gradlew :online-web:jsBrowserProductionWebpack

# Output location
ls -la online-web/build/dist/js/productionExecutable/
```

### Production Deployment
```bash
# Build production artifacts
./gradlew :online-web:jsBrowserProductionWebpack

# Deploy to Railway (or other static hosting)
cd online-web/build/dist/js/productionExecutable/
# Deploy contents to static web server
```

### Production Features
- **Optimized bundle:** Minified and tree-shaken
- **Code splitting:** Lazy loaded components
- **Source maps:** Separate .map files for debugging
- **Asset optimization:** Compressed JavaScript and CSS

---

## Key Configuration Files

| File | Purpose |
|------|---------|
| `online-web/build.gradle.kts` | Module build configuration |
| `online-web/src/jsMain/resources/index.html` | Main HTML entry point |
| `online-web/src/jsMain/resources/serve.json` | Static server configuration |
| `backend/.env.local` | Backend API configuration |

---

## Security Considerations

### Web Platform Limitations
- **No direct KeyStore access:** Uses browser's SecureStorage APIs
- **Reduced crypto capabilities:** Web Crypto API subset
- **Microphone/Camera permissions:** Required for biometric factors
- **HTTPS required:** For production deployment

### Implemented Security
- **Constant-time comparisons:** For digest verification
- **Secure random generation:** Using Web Crypto API
- **Input sanitization:** Against XSS attacks
- **CORS configuration:** Proper backend headers

---

## Development Workflow

### Daily Development
```bash
# 1. Start backend
cd backend && npm run dev &

# 2. Start web dev server
cd ../ && ./gradlew :online-web:jsBrowserDevelopmentRun

# 3. Make changes to Kotlin code
# 4. Browser auto-refreshes with new build

# 5. Run tests
./gradlew :online-web:jsTest
```

### Adding New Factor Canvas
```kotlin
// 1. Create processor in sdk/src/commonMain/kotlin/com/zeropay/sdk/factors/processors/
class NewFactorProcessor : FactorProcessor {
    override fun process(input: String): ByteArray {
        // Implementation
    }
}

// 2. Create canvas in online-web/src/jsMain/kotlin/com/zeropay/web/factor/
object NewFactorCanvas {
    fun render(root: HTMLDivElement, onComplete: (ByteArray) -> Unit) {
        // UI implementation using kotlinx-html
    }
}

// 3. Register in enrollment/verification flows
```

### Debugging Tips
```bash
# Check browser console for errors
# Use Chrome DevTools Sources tab for Kotlin source maps
# Check network tab for API calls
# Verify backend is running on localhost:3000
```

---

## Browser Compatibility

| Browser | Minimum Version | Status |
|---------|----------------|--------|
| **Chrome** | 90 | ✅ Full support |
| **Firefox** | 88 | ✅ Full support |
| **Safari** | 14 | ✅ Full support |
| **Edge** | 90 | ✅ Full support |

### Required APIs
- **Web Crypto API:** For secure hashing
- **Canvas API:** For drawing factors
- **Web Audio API:** For voice factor
- **LocalStorage:** For session management
- **WebAuthn:** For fingerprint factor

---

## Troubleshooting

### Common Issues

**"Cannot resolve project ':sdk'"**
```bash
# Ensure you're in root directory
pwd  # Should be zeropay-android
./gradlew :sdk:build  # Test SDK builds first
```

**"Backend connection failed"**
```bash
# Verify backend is running
curl http://localhost:3000/health

# Check CORS headers in backend logs
```

**"Module not found" errors**
```bash
./gradlew :online-web:clean
./gradlew :online-web:compileKotlinJs
```

**"Microphone not available"**
- Use HTTPS in production
- Grant microphone permission
- Check browser privacy settings

---

## Performance Optimization

### Build Optimizations
```kotlin
// In build.gradle.kts
js(IR) {
    browser {
        commonWebpackConfig {
            cssSupport { enabled.set(true) }
            // Code splitting enabled
        }
    }
    binaries.executable()
}
```

### Runtime Optimizations
- **Lazy loading:** Factor canvases loaded on demand
- **Debounced input:** For performance factors
- **Memory management:** Cleanup event listeners
- **Bundle analysis:** Check webpack-bundle-analyzer

---

**End of Web Module Development Guide**