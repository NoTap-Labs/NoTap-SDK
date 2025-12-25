# Test Creation Patterns - Always Read Before Writing Tests

**Date:** 2025-12-24
**Status:** Mandatory Reference Document
**Purpose:** Prevent test/code mismatches by following production code patterns

---

## 🚨 Critical Rule: NEVER Write Tests Without Reading Production Code First

**Pattern Observed Across ALL Modules:**
1. Production code evolves (new parameters, validation rules, security fixes)
2. Tests are written/updated without reading current production code
3. Tests fail with compilation errors or assertion failures
4. Hours wasted debugging obvious mismatches

**Solution:** Always read the actual production code before writing or updating any test.

---

## 📋 Mandatory Pre-Test Checklist

Before writing ANY test, complete this checklist:

### Step 1: Read Production Code
```bash
# Find the production class/function
grep -r "class ClassName\|data class ClassName" */src/*/kotlin
grep -r "function functionName\|const functionName" */src/**/*.js

# Read the ENTIRE file
Read <file_path>

# Note:
# - Constructor parameters (names, types, defaults, order)
# - Validation rules (regex patterns, range checks, required fields)
# - Return types and structures
# - Error handling patterns
```

### Step 2: Check for Recent Changes
```bash
# Check git history for recent changes to the class
git log --oneline -10 -- path/to/ProductionFile.kt

# Read the latest commit affecting this file
git show <commit-hash>:path/to/ProductionFile.kt
```

### Step 3: Verify Dependencies
```bash
# Check what the production code imports/depends on
grep "^import\|^require\|^const.*= require" ProductionFile.*

# Ensure test mocks/stubs match actual dependencies
```

### Step 4: Document Your Findings
```markdown
# Test Plan for ClassName

## Production Code Analysis (Date: YYYY-MM-DD)

**Constructor Signature:**
```typescript
constructor(
  param1: Type1,              // Required
  param2: Type2 = default,    // Optional with default
  param3?: Type3              // Optional nullable
)
```

**Validation Rules:**
- param1: Regex `/pattern/`, length > 0
- param2: Range [0, 100], default = 50
- param3: Must be HTTPS URL if provided

**Dependencies:**
- ExternalService (mocked in tests)
- ValidationUtils (use actual implementation)

**Test Cases Needed:**
1. Valid construction with all parameters
2. Valid construction with only required parameters
3. Invalid param1 (fails regex)
4. Invalid param2 (out of range)
5. etc.
```

---

## 🔍 Common Mismatch Patterns & Fixes

### Pattern 1: Constructor Parameter Mismatch

**❌ WRONG - Guessing parameters:**
```kotlin
// Test assumes old signature
PSPConfig.production(
    apiKey = "key",
    pspId = "id",
    metadata = mapOf("key" to "value")  // ❌ Parameter doesn't exist in companion function
)
```

**✅ CORRECT - Read production code first:**
```kotlin
// Production code (PSPConfig.kt):
// companion object {
//     fun production(apiKey: String, pspId: String): PSPConfig
// }

// Test matches actual signature
PSPConfig.production(
    apiKey = "key",
    pspId = "id"
)

// If metadata is needed, use main constructor:
PSPConfig(
    apiKey = "key",
    pspId = "id",
    environment = PSPEnvironment.PRODUCTION,
    metadata = mapOf("key" to "value")
)
```

**Files Where This Occurred:**
- `psp-sdk/src/test/kotlin/com/zeropay/psp/PSPConfigTest.kt:156`

---

### Pattern 2: Constructor Argument Order/Type Mismatch

**❌ WRONG - Positional arguments without reading signature:**
```kotlin
// Test assumes (errorCode: String, message: String)
ApiErrorResponse("ERROR_CODE", "Error message")  // ❌ First param is Boolean!
```

**✅ CORRECT - Read production signature:**
```kotlin
// Production code (PSPModels.kt):
// data class ApiErrorResponse(
//     val success: Boolean = false,
//     val error: String,
//     val message: String,
//     val details: Map<String, String>? = null
// )

// Test uses named parameters to avoid order issues
ApiErrorResponse(
    error = "ERROR_CODE",
    message = "Error message"
)
```

**Files Where This Occurred:**
- `psp-sdk/src/test/kotlin/com/zeropay/psp/PSPModelsTest.kt:233, 254`

---

### Pattern 3: Validation Rule Mismatch

**❌ WRONG - Not matching actual validation:**
```kotlin
// Test uses production environment with test API key
PSPConfig(
    apiKey = "psp_test_test_abc123",  // test key
    pspId = "test",
    environment = PSPEnvironment.PRODUCTION  // ❌ Mismatch!
)
```

**✅ CORRECT - Read validation logic:**
```kotlin
// Production code (PSPConfig.kt):
// val keyEnv = apiKey.split("_")[1]  // "test" or "live"
// val expectedEnv = when (environment) {
//     PSPEnvironment.SANDBOX -> "test"
//     PSPEnvironment.PRODUCTION -> "live"
// }
// require(keyEnv == expectedEnv) { ... }

// Test matches validation rules
PSPConfig(
    apiKey = "psp_test_test_abc123",   // test key
    pspId = "test",
    environment = PSPEnvironment.SANDBOX  // ✅ Matches "test" in key
)
```

**Files Where This Occurred:**
- `psp-sdk/src/test/kotlin/com/zeropay/psp/PSPConfigTest.kt:102, 114`

---

### Pattern 4: Hardcoded Timestamps (CRITICAL)

**❌ WRONG - Using old fixed timestamps:**
```kotlin
class SessionManagerTest {
    private val now = 1700000000000L  // ❌ November 14, 2023 - 2 years old!
    private val oneHour = 3600000L

    fun createTestSession(): VerificationSession {
        return VerificationSession(
            createdAt = now,
            expiresAt = now + oneHour  // Will be expired immediately!
        )
    }
}
```

**Production Impact:**
```kotlin
// Production code calls System.currentTimeMillis() internally
fun getSession(id: String): VerificationSession? {
    cleanExpiredSessions()  // Calls getCurrentTimeMillis() -> Dec 24, 2025
    return sessions[id]     // Session created in 2023 is expired!
}

// Test expectation:
assertEquals(session, manager.getSession(id))  // ❌ FAILS - session is null (expired)
```

**✅ CORRECT - Use current time:**
```kotlin
class SessionManagerTest {
    private val now = System.currentTimeMillis()  // ✅ Current time
    private val oneHour = 3600000L

    fun createTestSession(): VerificationSession {
        return VerificationSession(
            createdAt = now,
            expiresAt = now + oneHour  // Expires 1 hour from now
        )
    }
}
```

**Files Where This Occurred:**
- `psp-sdk/src/test/kotlin/com/zeropay/psp/VerificationSessionTest.kt:7, 189`

**Why This Happens:**
- Developer copies test from old code
- Timestamp was valid when originally written
- Time passes, production code uses `System.currentTimeMillis()`
- Test sessions are now ancient history and immediately expire

---

### Pattern 5: Security Boundary Changes

**❌ WRONG - Not matching security fixes:**
```kotlin
// Old code used `>`
fun isExpired(): Boolean {
    return System.currentTimeMillis() > expiresAt
}

// Test assumes session is valid AT exact boundary
@Test
fun `session at exact expiration boundary should be valid`() {
    val session = createSession(expiresAt = System.currentTimeMillis())
    assertFalse(session.isExpired())  // ❌ FAILS after security fix!
}
```

**✅ CORRECT - Read current security logic:**
```kotlin
// Production code after security audit (>=, not >)
fun isExpired(): Boolean {
    return System.currentTimeMillis() >= expiresAt  // ✅ Secure boundary
}

// Test matches new behavior
@Test
fun `session at exact expiration boundary should be expired`() {
    val session = createSession(expiresAt = System.currentTimeMillis())
    assertTrue(session.isExpired())  // ✅ Matches >= boundary
}
```

**Files Where This Occurred:**
- All modules after 2025-12-23 security audit
- Changed `>` to `>=` in 4 files (SDK, Merchant, Enrollment, PSP-SDK)

---

### Pattern 6: Missing Test Data Updates

**❌ WRONG - Test data doesn't match production requirements:**
```kotlin
// Production requires minimum 3 factors (updated 2025-11-05)
@Test
fun `should enroll user successfully`() {
    val factors = listOf("PIN", "EMOJI")  // ❌ Only 2 factors!
    val result = enrollUser(factors)
    assertTrue(result.success)  // ❌ FAILS - needs 3 minimum
}
```

**✅ CORRECT - Read current requirements:**
```kotlin
// Production code (EnrollmentManager.kt):
// require(factors.size >= 3) { "Minimum 3 factors required" }

@Test
fun `should enroll user successfully`() {
    val factors = listOf("PIN", "EMOJI", "PATTERN")  // ✅ 3 factors
    val result = enrollUser(factors)
    assertTrue(result.success)
}
```

**Files Where This Occurred:**
- `merchant/src/commonTest/kotlin/.../*Test.kt` (23 tests fixed)

---

## 📝 Test Creation Template

Use this template for ALL new tests:

```kotlin/typescript
/**
 * Tests for [ClassName]
 *
 * Production Code: path/to/ProductionFile.kt
 * Last Verified: YYYY-MM-DD
 *
 * Constructor Signature (from production):
 * - param1: Type (required, validation: ...)
 * - param2: Type (optional, default: ...)
 *
 * Key Validation Rules:
 * - Rule 1: ...
 * - Rule 2: ...
 */

class ClassNameTest {

    // ==================== TEST DATA SETUP ====================

    // Use current time, not hardcoded timestamps
    private val now = System.currentTimeMillis()  // ✅ Not 1700000000000L

    // Match actual validation patterns from production
    private val validApiKey = "psp_test_merchant_abc123"  // ✅ Matches regex

    /**
     * Helper to create valid test instance matching production constructor
     *
     * Production signature (verified YYYY-MM-DD):
     * ClassName(param1: Type, param2: Type = default, ...)
     */
    private fun createValidInstance(
        param1: Type = validDefault1,
        param2: Type = validDefault2
    ): ClassName {
        return ClassName(
            param1 = param1,
            param2 = param2
            // Use named parameters to prevent order issues
        )
    }

    // ==================== CONSTRUCTION TESTS ====================

    @Test
    fun `should create instance with valid parameters`() {
        // Read production code to know what "valid" means!
        val instance = createValidInstance()
        assertNotNull(instance)
    }

    @Test
    fun `should reject invalid param1`() {
        // Read production validation to know what's invalid!
        assertFailsWith<IllegalArgumentException> {
            createValidInstance(param1 = "invalid-value")
        }
    }

    // ==================== BEHAVIOR TESTS ====================

    @Test
    fun `should perform expected behavior`() {
        // Read production implementation to know expected behavior!
        val instance = createValidInstance()
        val result = instance.someMethod()

        // Assert based on ACTUAL production behavior, not assumptions
        assertEquals(expectedValue, result)
    }
}
```

---

## 🔄 Test Maintenance Checklist

**When production code changes:**

1. **Search for ALL tests using changed code:**
   ```bash
   grep -r "ClassName\|functionName" */test/
   ```

2. **Update EVERY test found:**
   - Fix parameter names/types
   - Fix validation expectations
   - Fix return type assertions
   - Fix import statements

3. **Verify tests compile:**
   ```bash
   gradlew :module:compileDebugUnitTestKotlinAndroid
   npm test -- --listTests
   ```

4. **Run tests to ensure they pass:**
   ```bash
   gradlew :module:test
   npm test
   ```

5. **Commit code + test updates together:**
   ```bash
   git add ProductionFile.kt ProductionFileTest.kt
   git commit -m "feat: Update feature X (includes test updates)"
   ```

**NEVER:**
- ❌ Change production code without updating tests
- ❌ Commit code that breaks test compilation
- ❌ Leave tests with outdated signatures
- ❌ Assume tests "probably still work"

**ALWAYS:**
- ✅ Update tests in SAME commit as code changes
- ✅ Search for ALL usages before changing APIs
- ✅ Verify test compilation before committing
- ✅ Run full test suite after major changes

---

## 🎯 Real-World Examples from This Codebase

### Example 1: Merchant Tests (23 failures fixed)

**Problem:** Tests used 2 factors, production required 3 minimum

**Investigation:**
```bash
# Read production code
Read merchant/src/commonMain/kotlin/.../VerificationManager.kt

# Found:
require(factors.size >= 3) { "Minimum 3 factors required" }
```

**Fix:** Updated ALL 23 tests to use 3+ factors

---

### Example 2: PSP-SDK Session Tests (4 failures fixed)

**Problem:** All sessions immediately expired

**Investigation:**
```kotlin
// Test code
private val now = 1700000000000L  // Nov 14, 2023

// Production code
fun getSession(id: String) {
    cleanExpiredSessions()  // Uses System.currentTimeMillis() = Dec 24, 2025
    // ... sessions from 2023 are expired!
}
```

**Fix:** Changed to `System.currentTimeMillis()`

---

### Example 3: Security Boundary (multiple modules)

**Problem:** Tests assumed `>` boundary, production changed to `>=`

**Investigation:**
```bash
# Security audit fixed timing attack
git show 0d49136:psp-sdk/src/commonMain/kotlin/.../VerificationSession.kt

# Changed:
- return currentTime > expiresAt
+ return currentTime >= expiresAt
```

**Fix:** Updated test expectations to match `>=` behavior

---

## 📚 Required Reading Before Writing Tests

**MANDATORY - Read these in order:**

1. **This document** - Test creation patterns
2. **Production code file** - The ACTUAL code you're testing
3. **Recent git history** - `git log -10 -- ProductionFile.*`
4. **Related test files** - See how similar tests are written
5. **LESSONS_LEARNED.md** - Common pitfalls to avoid

**Recommended:**
- `TEST_ARCHITECTURE.md` - Framework-specific patterns (Mocha vs Jest vs JUnit)
- `SECURITY_PATTERNS_REFERENCE.md` - Security testing patterns
- Recent pull requests touching the same code

---

## ✅ Pre-Commit Test Validation

Before committing, verify:

```bash
# 1. Tests compile
gradlew :module:compileDebugUnitTestKotlinAndroid --no-daemon
npm test -- --listTests

# 2. Tests pass
gradlew :module:test --no-daemon
npm test

# 3. Tests match production code
# - Read both files side by side
# - Verify parameter names match
# - Verify validation rules match
# - Verify test data is current (no old timestamps!)

# 4. Coverage is adequate
# - New code has tests
# - Changed code has updated tests
# - Edge cases are covered
```

---

## 🚫 Anti-Patterns to Avoid

| Anti-Pattern | Why It's Wrong | Correct Approach |
|--------------|----------------|------------------|
| Copy-paste old tests | Old tests have outdated assumptions | Write new tests after reading current code |
| Hardcode 2023 timestamps | Production uses current time | Use `System.currentTimeMillis()` |
| Guess constructor params | Wrong types/names/order | Read production class definition |
| Assume validation rules | Rules change over time | Read actual validation logic |
| Skip test compilation | Syntax errors block CI/CD | Always verify tests compile |
| Test in isolation | Miss integration issues | Run full test suite |

---

## 📊 Success Metrics

**Good Test Suite Indicators:**
- ✅ 95%+ pass rate
- ✅ Zero compilation errors
- ✅ Tests updated in same commit as code changes
- ✅ Test data matches current requirements
- ✅ No hardcoded old timestamps

**Bad Test Suite Indicators:**
- ❌ <80% pass rate
- ❌ Compilation errors
- ❌ Tests lag behind code changes
- ❌ Test data from years ago
- ❌ Tests fail on timing boundaries

---

## 🔧 Quick Fix Guide

**When tests fail after code changes:**

1. **Read error message** - What specifically failed?
2. **Read production code** - What's the CURRENT implementation?
3. **Compare test vs production** - Where's the mismatch?
4. **Fix test to match production** - Not the other way around!
5. **Run tests again** - Verify fix works
6. **Commit together** - Code + test updates in one commit

**Common Quick Fixes:**
```kotlin
// Fix 1: Add missing environment parameter
- PSPConfig(apiKey = "psp_test_...", pspId = "id")
+ PSPConfig(apiKey = "psp_test_...", pspId = "id", environment = PSPEnvironment.SANDBOX)

// Fix 2: Use named parameters
- ApiErrorResponse("CODE", "message")
+ ApiErrorResponse(error = "CODE", message = "message")

// Fix 3: Use current time
- private val now = 1700000000000L
+ private val now = System.currentTimeMillis()

// Fix 4: Add required parameters
- createSession(factors = listOf("PIN", "EMOJI"))
+ createSession(factors = listOf("PIN", "EMOJI", "PATTERN"))
```

---

## 📝 Lesson Summary

**Core Principle:** Tests must ALWAYS match current production code, not historical assumptions.

**Workflow:**
1. Read production code FIRST
2. Understand current behavior
3. Write tests matching ACTUAL behavior
4. Update tests EVERY time code changes
5. Commit code + tests together

**Remember:** 10 minutes reading production code saves hours debugging test failures.

---

**Last Updated:** 2025-12-24
**Applies To:** All modules (SDK, Merchant, Enrollment, PSP-SDK, PSP-SDK-Web, Online-Web)
**Status:** Mandatory for all test creation and updates
