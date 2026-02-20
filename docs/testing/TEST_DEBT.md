# Test Debt Registry

**Last Updated:** 2026-02-20
**Status:** 488/496 tests passing (8 flaky timing failures)

---

## Summary

| Category | Count | Severity | Status |
|----------|-------|----------|--------|
| Timing Tests | 8-11 (flaky) | LOW | Inherently non-deterministic — pass/fail varies per run |
| ~~Emoji Tests~~ | ~~3~~ | ~~MEDIUM~~ | **FIXED** (2026-02-20) — splitEmojis bug + test data |
| ~~Processor Tests~~ | ~~2~~ | ~~MEDIUM~~ | **FIXED** (2026-02-20) — JSON→semicolon format |
| ~~ColourFactor Tests~~ | ~~2~~ | ~~MEDIUM~~ | **FIXED** (2026-02-20) — MIN_SELECTIONS=4 |

---

## Timing Tests (8-11 flaky failures)

These tests verify constant-time behavior but are inherently flaky due to:
- CPU scheduling variance
- JVM warmup effects
- Garbage collection pauses
- WSL2/NTFS overhead
- Environment-specific timing

### Affected Tests

| Test | File | Issue |
|------|------|-------|
| `testSecurity_ConstantTimeVerification` | RhythmTapFactorTest.kt | Timing variance |
| `testPinFactor_TimingAttackResistance` | ZeroPaySDKTest.kt | Timing variance |
| `testEmojiVerify_ConstantTime` | EmojiAndColourFactorTest.kt | Timing variance |
| `testColourVerify_ConstantTime` | EmojiAndColourFactorTest.kt | Timing variance |
| `testVerify_ConstantTime_TimingIndependent` | PatternFactorTest.kt | Timing variance |
| `testVerify_ConstantTime_TimingIndependent` | PinFactorTest.kt | Timing variance |
| `testIsValidPin_ConstantTime_NoEarlyReturn` | PinFactorTest.kt | Timing variance |
| `testVerify_ConstantTime_TimingIndependent` | WordsFactorTest.kt | Timing variance |
| `testVerify_ConstantTime_TimingIndependent` | MouseFactorTest.kt | Timing variance |
| `testVerify_ConstantTime_TimingIndependent` | VoiceFactorTest.kt | Timing variance |
| `constant-time comparison should *` (x2) | SecurityComprehensiveTest.kt | Timing variance |

**Note:** These fluctuate between 8-11 failures per run. The underlying constant-time code is correct — the tests are measuring JVM timing which is non-deterministic.

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

## Fixed Issues (2026-02-20)

### Emoji Tests (3 failures → 0)

**Root cause:** `EmojiProcessor.splitEmojis()` had a bug at line 268 — when it encountered a consecutive emoji codepoint, it joined it to the current emoji instead of treating it as separate. So `"😀😃😄"` was parsed as 2 emojis instead of 3.

**Fix:** Removed the `else if (isEmojiCodePoint(nextCodePoint))` branch that incorrectly joined consecutive emojis. Now only ZWJ sequences and modifiers are joined to compound emojis.

### Processor Tests (2 failures → 0)

**Root cause:** Tests passed JSON format (`[{"x":0.1,"y":0.2,"t":0}]`) but `MouseProcessor` and `StylusProcessor` expect semicolon-separated format (`0.1,0.2,0;0.15,0.25,100`).

**Fix:** Updated test data in `FactorValidationTest.kt` to use the correct semicolon format.

### ColourFactor Integration Tests (2 failures → 0)

**Root cause:** `testFullAuthenticationFlow_TwoFactors` and `testFullAuthenticationFlow_ThreeFactors` in `ZeroPaySDKTest.kt` used `listOf(0, 1)` (2 colors) but `ColourFactor.digest()` requires `MIN_SELECTIONS = 4`. These were misclassified as "timing tests" in the original TEST_DEBT.md.

**Fix:** Updated test data to `listOf(0, 1, 2, 3)`.

---

## Action Items

### Priority 1 (Next)
- [ ] Implement statistical timing test approach (run N iterations, check distribution)
- [ ] Add timing test category for optional CI skip

### Priority 2 (Technical Debt)
- [ ] Refactor timing tests to use distribution analysis
- [ ] Add integration tests that verify constant-time behavior statistically

---

## Related Files

- `sdk/src/commonMain/kotlin/com/zeropay/sdk/factors/processors/EmojiProcessor.kt` — splitEmojis fix
- `sdk/src/test/kotlin/com/zeropay/sdk/factors/FactorValidationTest.kt` — Mouse/Stylus format fix
- `sdk/src/test/kotlin/com/zeropay/sdk/ZeroPaySDKTest.kt` — ColourFactor MIN_SELECTIONS fix
