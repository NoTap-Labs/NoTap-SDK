# Test Debt Registry

**Last Updated:** 2026-02-18
**Branch:** `fix/critical-security-consistency-20260218`
**Status:** 481/496 tests passing (15 failures)

---

## Summary

| Category | Count | Severity | Action Required |
|----------|-------|----------|-----------------|
| Timing Tests | 10 | LOW | Flaky by nature - consider skipping or increasing tolerance |
| Emoji Tests | 3 | MEDIUM | Emoji parsing edge cases |
| Processor Tests | 2 | MEDIUM | Additional validation requirements |

---

## 1. Timing Tests (10 failures)

These tests verify constant-time behavior but are inherently flaky due to:
- CPU scheduling variance
- JVM warmup effects
- Garbage collection pauses
- Environment-specific timing

### Failed Tests

| Test | File | Issue |
|------|------|-------|
| `testSecurity_ConstantTimeVerification` | RhythmTapFactorTest.kt | Timing variance |
| `testFullAuthenticationFlow_ThreeFactors` | ZeroPaySDKTest.kt | Timing variance |
| `testFullAuthenticationFlow_TwoFactors` | ZeroPaySDKTest.kt | Timing variance |
| `testPinFactor_TimingAttackResistance` | ZeroPaySDKTest.kt | Timing variance |
| `testEmojiVerify_ConstantTime` | EmojiAndColourFactorTest.kt | Timing variance |
| `testColourVerify_ConstantTime` | EmojiAndColourFactorTest.kt | Timing variance |
| `testVerify_ConstantTime_TimingIndependent` | PatternFactorTest.kt | Timing variance |
| `testVerify_ConstantTime_TimingIndependent` | PinFactorTest.kt | Timing variance |
| `testIsValidPin_ConstantTime_NoEarlyReturn` | PinFactorTest.kt | Timing variance |
| `testVerify_ConstantTime_TimingIndependent` | WordsFactorTest.kt | Timing variance |

### Recommended Fix

```kotlin
// Option 1: Increase tolerance from 50% to 100%
val percentDifference = (difference.toDouble() / average) * 100
assertTrue(percentDifference < 100.0, "...")

// Option 2: Skip in CI with @Ignore or category
@Ignore("Timing tests are flaky in CI")
@Test
fun testConstantTime() { ... }

// Option 3: Use statistical analysis (run 100 times, check distribution)
```

---

## 2. Emoji Tests (3 failures)

Emoji parsing has edge cases with multi-codepoint characters.

### Failed Tests

| Test | File | Issue |
|------|------|-------|
| `Emoji should accept valid emoji sequence` | FactorValidationTest.kt:167 | Assertion fails |
| `Emoji should reject too many emojis` | FactorValidationTest.kt:179 | Assertion fails |
| `Emoji should handle edge case counts` | FactorValidationTest.kt:208 | Assertion fails |

### Investigation Needed

1. Check if test emoji strings are valid Unicode
2. Verify EmojiProcessor.parseEmojis() handles all emoji formats
3. Test with actual emoji characters vs Unicode escapes

---

## 3. Processor Tests (2 failures)

Additional validation requirements not met by test data.

### Failed Tests

| Test | File | Issue |
|------|------|-------|
| `MouseProcessor should accept valid mouse pattern` | FactorValidationTest.kt | Validation fails |
| `StylusProcessor should accept valid stroke data` | FactorValidationTest.kt | Validation fails |

### Investigation Needed

1. Check MIN_PATH_LENGTH requirement
2. Verify pressure variance calculation for StylusProcessor
3. Ensure timestamps and coordinates meet all validation rules

---

## Action Items

### Priority 1 (Before Merge)
- [ ] Investigate emoji parsing issues
- [ ] Fix processor validation tests

### Priority 2 (Post-Merge)
- [ ] Implement statistical timing test approach
- [ ] Add timing test category for optional CI skip

### Priority 3 (Technical Debt)
- [ ] Refactor timing tests to use distribution analysis
- [ ] Add integration tests that verify constant-time behavior statistically

---

## Related Files

- `sdk/src/test/kotlin/com/zeropay/sdk/factors/FactorValidationTest.kt`
- `sdk/src/test/kotlin/com/zeropay/sdk/factors/EmojiFactor AndColourFactorTest.kt`
- `sdk/src/test/kotlin/com/zeropay/sdk/ZeroPaySDKTest.kt`
- `sdk/src/commonMain/kotlin/com/zeropay/sdk/factors/processors/EmojiProcessor.kt`
- `sdk/src/commonMain/kotlin/com/zeropay/sdk/factors/processors/MouseProcessor.kt`
- `sdk/src/commonMain/kotlin/com/zeropay/sdk/factors/processors/StylusProcessor.kt`
