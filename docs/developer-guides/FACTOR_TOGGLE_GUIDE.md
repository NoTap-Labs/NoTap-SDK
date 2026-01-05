# Factor Toggle System - Usage Guide

Complete guide for using the Factor Toggle System to enable/disable authentication factors for incremental testing and development.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Configuration Methods](#configuration-methods)
4. [Testing Workflow](#testing-workflow)
5. [Test Annotations](#test-annotations)
6. [Common Scenarios](#common-scenarios)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

The Factor Toggle System allows you to:

- ✅ **Enable/disable individual factors** - Test 5 factors, fix issues, then enable more
- ✅ **Automatic test skipping** - Tests for disabled factors skip automatically (no failures)
- ✅ **Build-time configuration** - Set defaults in `gradle.properties`
- ✅ **Runtime override** - Change configuration dynamically in code
- ✅ **Zero breaking changes** - All factors enabled by default

### Why Use This?

**Before:**
```
❌ Enable all 15 factors → 50 test failures → Can't isolate issues
❌ Comment out tests manually → Forget to uncomment later
❌ Disable entire test classes → Lose test coverage tracking
```

**After:**
```
✅ Enable 5 factors → Fix 10 specific issues → Enable 5 more factors
✅ Tests auto-skip for disabled factors → Clean test output
✅ Track which factors are production-ready → Gradual rollout
```

---

## 🚀 Quick Start

### Method 1: Runtime Override (Recommended for Testing)

```kotlin
// In your test setup or main application
FactorConfig.override {
    enableOnly(
        Factor.PIN,
        Factor.PATTERN_MICRO,
        Factor.EMOJI,
        Factor.FINGERPRINT,
        Factor.RHYTHM_TAP
    )
}

// Now only these 5 factors are available
// All other factor tests will be skipped automatically
```

### Method 2: Gradle Properties (Build-Time)

Edit `gradle.properties`:

```properties
# Disable problematic factors
zeropay.factor.voice=false
zeropay.factor.nfc=false
zeropay.factor.balance=false

# All others remain enabled (default = true)
```

### Method 3: Test Preset

```kotlin
// Use predefined presets for common scenarios
FactorTestPresets.enableBasicFactors()  // PIN, COLOUR, EMOJI, PATTERN, RHYTHM_TAP
FactorTestPresets.enableKnowledgeFactors()  // PIN, COLOUR, EMOJI, WORDS
FactorTestPresets.enableBiometricFactors()  // FINGERPRINT, FACE
```

---

## ⚙️ Configuration Methods

### 1. Runtime Configuration (Dynamic)

**Enable Only Specific Factors:**
```kotlin
FactorConfig.override {
    enableOnly(Factor.PIN, Factor.EMOJI, Factor.FINGERPRINT)
}
// All other factors disabled
```

**Enable/Disable Individual Factors:**
```kotlin
FactorConfig.override {
    enable(Factor.VOICE, Factor.NFC)  // Enable these
    disable(Factor.BALANCE, Factor.MOUSE_DRAW)  // Disable these
}
```

**Enable/Disable by Category:**
```kotlin
FactorConfig.override {
    enableCategory(Factor.Category.KNOWLEDGE)  // Enable all knowledge factors
    disableCategory(Factor.Category.POSSESSION)  // Disable all possession factors
}
```

**Enable All (Default):**
```kotlin
FactorConfig.clearOverride()
// Reverts to default: all factors enabled
```

### 2. Build-Time Configuration (Static)

Edit `gradle.properties`:

```properties
# Knowledge Factors
zeropay.factor.pin=true
zeropay.factor.colour=false  # Disable colour
zeropay.factor.emoji=true
zeropay.factor.words=false  # Disable words

# Behavioral Factors
zeropay.factor.pattern_micro=true
zeropay.factor.pattern_normal=false  # Disable pattern normal
zeropay.factor.voice=false  # Disable voice
zeropay.factor.rhythm_tap=true

# Biometric Factors
zeropay.factor.fingerprint=true
zeropay.factor.face=false  # Disable face
```

### 3. Checking Factor Status

```kotlin
// Check if specific factor is enabled
if (FactorConfig.isEnabled(Factor.PIN)) {
    println("PIN is enabled")
}

// Get all enabled factors
val enabled = FactorConfig.getEnabledFactors()
println("Enabled factors: ${enabled.joinToString { it.displayName }}")

// Get enabled factors by category
val knowledgeFactors = FactorConfig.getEnabledFactorsByCategory(Factor.Category.KNOWLEDGE)

// Get statistics
val stats = FactorConfig.getStats()
println(stats)
// Output:
// Factor Configuration:
//   Total: 15 factors
//   Enabled: 8 (53%)
//   Disabled: 7
//   By Category:
//     - Something You Know: 2 enabled
//     - Something You Have: 0 enabled
//     - Something You Are: 6 enabled
//   PSD3 Compliant: ✅ Yes
```

---

## 🧪 Testing Workflow

### Incremental Testing Strategy

**Week 1: Basic Factors (5 factors)**
```kotlin
@BeforeAll
fun setup() {
    FactorTestPresets.enableBasicFactors()
    // PIN, COLOUR, EMOJI, PATTERN_MICRO, RHYTHM_TAP
}
```
- Run tests: `./gradlew :sdk:test`
- Fix 10-15 test failures
- Commit fixes

**Week 2: Add Behavioral Factors (8 factors)**
```kotlin
@BeforeAll
fun setup() {
    FactorTestPresets.enableBehavioralFactors()
    // PATTERN_MICRO, PATTERN_NORMAL, MOUSE_DRAW, STYLUS_DRAW,
    // VOICE, IMAGE_TAP, BALANCE, RHYTHM_TAP
}
```
- Run tests again
- Fix additional issues
- Commit fixes

**Week 3: Add Biometric Factors (10 factors)**
```kotlin
@BeforeAll
fun setup() {
    FactorConfig.override {
        enable(Factor.FINGERPRINT, Factor.FACE)  // Add biometrics to existing
    }
}
```
- Run tests
- Fix biometric-specific issues
- Commit fixes

**Week 4: Enable All (15 factors)**
```kotlin
@BeforeAll
fun setup() {
    FactorConfig.clearOverride()  // All factors enabled
}
```
- Final integration testing
- All tests should pass

---

## 🏷️ Test Annotations

### 1. Single Factor Test

```kotlin
@EnabledIfFactorEnabled(Factor.PIN)
@Test
fun `PIN validation should reject weak PINs`() {
    // This test ONLY runs if Factor.PIN is enabled
    // If disabled, test is SKIPPED (not failed)
}
```

### 2. Multiple Factors (ALL must be enabled)

```kotlin
@EnabledIfAllFactorsEnabled(factors = [Factor.PIN, Factor.FINGERPRINT])
@Test
fun `PIN + Fingerprint 2FA should work`() {
    // Runs ONLY if BOTH factors are enabled
    // If either is disabled, test is SKIPPED
}
```

### 3. Multiple Factors (ANY can be enabled)

```kotlin
@EnabledIfAnyFactorEnabled(factors = [Factor.PATTERN_MICRO, Factor.PATTERN_NORMAL])
@Test
fun `Pattern factors should support timing analysis`() {
    // Runs if EITHER pattern factor is enabled
    // Only skipped if BOTH are disabled
}
```

### 4. Entire Test Class

```kotlin
@EnabledIfFactorEnabled(Factor.VOICE)
class VoiceFactorTest {
    // ALL tests in this class only run if Factor.VOICE is enabled

    @Test
    fun `voice enrollment should work`() { ... }

    @Test
    fun `voice verification should work`() { ... }
}
```

### 5. Manual Check (No Annotation)

```kotlin
class MyTest : FactorTestBase() {
    @Test
    fun `custom factor test`() {
        // Programmatic check - throws AssumptionViolatedException if disabled
        assumeFactorEnabled(Factor.NFC)

        // Test logic here (only runs if NFC enabled)
    }
}
```

---

## 📚 Common Scenarios

### Scenario 1: Testing PIN Factor Only

```kotlin
// In test class or @BeforeAll
FactorConfig.override {
    enableOnly(Factor.PIN)
}

// Run tests
./gradlew :sdk:test --tests "*PinFactorTest*"
```

**Result:** Only PIN tests run, all other factor tests skipped

---

### Scenario 2: Disable Problematic Factors Temporarily

```kotlin
// Voice and NFC have compilation issues, disable for now
FactorConfig.override {
    disable(Factor.VOICE, Factor.NFC, Factor.BALANCE)
}

// Or in gradle.properties:
zeropay.factor.voice=false
zeropay.factor.nfc=false
zeropay.factor.balance=false

// All tests run except Voice, NFC, and Balance tests
```

**Result:** Clean build, can fix issues for disabled factors later

---

### Scenario 3: Test Only Android-Specific Factors

```kotlin
FactorConfig.override {
    enableOnly(
        Factor.FINGERPRINT,  // Android BiometricPrompt
        Factor.FACE,  // Android BiometricPrompt
        Factor.NFC,  // Android NFC
        Factor.BALANCE  // Android accelerometer
    )
}
```

**Result:** Only Android hardware factors tested

---

### Scenario 4: Test Minimum PSD3 SCA Compliance

```kotlin
FactorTestPresets.enableMinimumPSD3()
// Enables: PIN (Knowledge) + FINGERPRINT (Inherence)
// Meets PSD3 requirement: 2 factors from 2 categories

val stats = FactorConfig.getStats()
println(stats.meetsPSD3Requirements)  // true
```

---

### Scenario 5: Gradual Production Rollout

```kotlin
// Phase 1: Launch with core factors only
FactorConfig.override {
    enableOnly(
        Factor.PIN,
        Factor.PATTERN_MICRO,
        Factor.FINGERPRINT,
        Factor.FACE
    )
}

// Phase 2: Add fun factors (2 weeks later)
FactorConfig.override {
    enable(Factor.EMOJI, Factor.COLOUR, Factor.RHYTHM_TAP)
}

// Phase 3: Add advanced factors (1 month later)
FactorConfig.override {
    enable(Factor.VOICE, Factor.NFC, Factor.STYLUS_DRAW)
}

// Phase 4: Full rollout (2 months later)
FactorConfig.clearOverride()  // All factors enabled
```

---

## 🛠️ Troubleshooting

### Issue: Tests Still Failing for Disabled Factors

**Cause:** Test not using conditional execution

**Fix:** Add annotation or extend `FactorTestBase`

```kotlin
// Before (fails when factor disabled)
@Test
fun testVoice() { ... }

// After (skips when factor disabled)
@EnabledIfFactorEnabled(Factor.VOICE)
@Test
fun testVoice() { ... }
```

---

### Issue: Factor Shown as Available but Tests Skip

**Cause:** Runtime override not cleared from previous test

**Fix:** Clear override in `@BeforeEach` or `@AfterEach`

```kotlin
@AfterEach
fun cleanup() {
    FactorConfig.clearOverride()  // Reset to defaults
}
```

---

### Issue: Build-Time Configuration Not Working

**Cause:** Gradle properties not synced

**Fix:** Re-sync gradle

```bash
./gradlew clean
./gradlew --refresh-dependencies
```

---

### Issue: Want to Test Only Enabled Factors in UI

**Cause:** UI showing all factors regardless of config

**Fix:** Use `FactorRegistry.availableFactors()` which respects `FactorConfig`

```kotlin
// ✅ CORRECT - Respects FactorConfig
val availableFactors = FactorRegistry.availableFactors(context)

// ❌ WRONG - Shows all factors
val allFactors = Factor.values()
```

---

## 📊 Statistics and Monitoring

### Check Current Configuration

```kotlin
val stats = FactorConfig.getStats()
println("""
    Total Factors: ${stats.totalFactors}
    Enabled: ${stats.enabledFactors} (${stats.enabledPercentage}%)
    Disabled: ${stats.disabledFactors}

    Enabled by Category:
    - Knowledge: ${stats.enabledByCategory[Factor.Category.KNOWLEDGE]}
    - Possession: ${stats.enabledByCategory[Factor.Category.POSSESSION]}
    - Inherence: ${stats.enabledByCategory[Factor.Category.INHERENCE]}

    PSD3 Compliant: ${stats.meetsPSD3Requirements}
""".trimIndent())
```

### Export Configuration for Documentation

```kotlin
val status = FactorConfig.getStatusSummary()
status.forEach { (factor, enabled) ->
    println("${factor.displayName}: ${if (enabled) "✅ Enabled" else "❌ Disabled"}")
}
```

---

## 🎓 Best Practices

1. **Start Small** - Enable 5 factors first, fix issues, then expand
2. **Use Presets** - Leverage `FactorTestPresets` for common configurations
3. **Clear Overrides** - Always clear overrides in `@AfterEach` to avoid test pollution
4. **Annotate Tests** - Use `@EnabledIfFactorEnabled` for automatic skipping
5. **Document Disabled Factors** - Comment why factors are disabled in gradle.properties
6. **Track Progress** - Use `getStats()` to monitor which factors are production-ready
7. **Test Incrementally** - Don't enable all 15 factors at once

---

## 📝 Summary

**The Factor Toggle System gives you:**

✅ **Incremental Testing** - Test 5 factors at a time without breaking the build
✅ **Clean Test Output** - Disabled factors skip tests (not fail them)
✅ **Flexible Configuration** - Runtime, build-time, or test preset control
✅ **Gradual Rollout** - Launch with core factors, add more over time
✅ **Zero Breaking Changes** - All factors enabled by default

**Quick Reference:**

```kotlin
// Enable only 5 factors for testing
FactorConfig.override { enableOnly(Factor.PIN, Factor.EMOJI, Factor.PATTERN_MICRO, Factor.FINGERPRINT, Factor.RHYTHM_TAP) }

// Disable problematic factors
FactorConfig.override { disable(Factor.VOICE, Factor.NFC, Factor.BALANCE) }

// Use preset
FactorTestPresets.enableBasicFactors()

// Reset to all enabled
FactorConfig.clearOverride()

// Annotate test
@EnabledIfFactorEnabled(Factor.PIN)
@Test fun testPIN() { ... }
```

---

**Questions?** See `sdk/src/commonMain/kotlin/com/zeropay/sdk/config/FactorConfig.kt` for complete API documentation.
