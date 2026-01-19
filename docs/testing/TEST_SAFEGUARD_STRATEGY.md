---
hidden: true
---

# Test Safeguard Strategy - Ensuring Tests Evolve With Codebase

**Date:** 2025-12-24 **Purpose:** Prevent test/code divergence through automated safeguards **Status:** Mandatory Implementation

***

## 🚨 The Problem

**Observed Pattern:**

* Production code changes (new parameters, validation rules, security fixes)
* Tests don't get updated
* CI/CD fails weeks/months later
* Hours wasted debugging obvious mismatches

**Root Cause:** No automated mechanism to detect when tests need updates

***

## 🛡️ Multi-Layer Safeguard Strategy

### Layer 1: Pre-Commit Hooks (Immediate Detection)

**Git Hook:** `.git/hooks/pre-commit`

```bash
#!/bin/bash
# Pre-commit hook to ensure tests are updated with code changes

echo "🔍 Checking if test updates are needed..."

# Get list of changed production files
CHANGED_FILES=$(git diff --cached --name-only --diff-filter=ACMR)

# Check if production code changed
PROD_CHANGED=$(echo "$CHANGED_FILES" | grep -E "src/(main|commonMain|androidMain|jsMain)" | grep -v "Test")

if [ -n "$PROD_CHANGED" ]; then
    echo "📝 Production code changed:"
    echo "$PROD_CHANGED"

    # Check if corresponding tests are also staged
    TEST_CHANGED=$(echo "$CHANGED_FILES" | grep -E "src/(test|commonTest|androidTest)" | grep "Test")

    if [ -z "$TEST_CHANGED" ]; then
        echo "⚠️  WARNING: Production code changed but no test files staged!"
        echo ""
        echo "Changed production files:"
        echo "$PROD_CHANGED"
        echo ""
        echo "Please verify if tests need updates:"
        echo "  - If tests need updates: Stage test files with 'git add'"
        echo "  - If tests don't need updates: Add --no-verify flag"
        echo ""
        read -p "Continue anyway? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        echo "✅ Test files also staged:"
        echo "$TEST_CHANGED"
    fi
fi

# Run tests before commit
echo ""
echo "🧪 Running tests..."

# Kotlin tests (if Kotlin files changed)
if echo "$CHANGED_FILES" | grep -q "\.kt$"; then
    echo "Running Kotlin tests..."
    # Run quick compile check
    ./gradlew compileDebugKotlinAndroid --no-daemon -q
    if [ $? -ne 0 ]; then
        echo "❌ Kotlin compilation failed. Fix errors before committing."
        exit 1
    fi
fi

# JavaScript tests (if JS files changed)
if echo "$CHANGED_FILES" | grep -q "\.js$"; then
    echo "Running JavaScript tests..."
    # Check if tests compile
    npm test -- --listTests &> /dev/null
    if [ $? -ne 0 ]; then
        echo "❌ JavaScript test compilation failed. Fix errors before committing."
        exit 1
    fi
fi

echo "✅ Pre-commit checks passed"
exit 0
```

**Installation:**

```bash
# One-time setup
cp documentation/07-testing/pre-commit-hook.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

***

### Layer 2: CI/CD Test Compilation Check (Build-Time Detection)

**GitHub Actions:** `.github/workflows/test-compilation-check.yml`

```yaml
name: Test Compilation Verification

on:
  pull_request:
    branches: [master, main, develop]
  push:
    branches: [master, main, develop]

jobs:
  verify-test-compilation:
    name: Verify Tests Compile
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Compile All Kotlin Tests
        run: |
          ./gradlew compileDebugUnitTestKotlinAndroid --no-daemon
          ./gradlew compileDebugAndroidTestKotlinAndroid --no-daemon
        continue-on-error: false

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: 'backend/package-lock.json'

      - name: Compile All JavaScript Tests
        run: |
          cd backend && npm ci
          npm test -- --listTests
          cd ../psp-sdk-web && npm ci
          npm test -- --listTests
        continue-on-error: false

      - name: Report Status
        if: failure()
        run: |
          echo "❌ Test compilation failed!"
          echo "This means tests don't match production code."
          echo "Please update tests to match current code signatures."
          exit 1
```

***

### Layer 3: Test Coverage Enforcement (PR Requirement)

**GitHub Branch Protection Rule:**

```yaml
# .github/workflows/coverage-check.yml
name: Test Coverage Check

on: [pull_request]

jobs:
  coverage:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Run Tests with Coverage
        run: |
          ./gradlew test jacocoTestReport
          npm test -- --coverage

      - name: Check Coverage Thresholds
        run: |
          # Kotlin coverage (JaCoCo)
          MIN_COVERAGE=80
          COVERAGE=$(grep -oP 'Total.*?(\d+)%' build/reports/jacoco/test/html/index.html | grep -oP '\d+')
          if [ $COVERAGE -lt $MIN_COVERAGE ]; then
            echo "❌ Coverage $COVERAGE% is below minimum $MIN_COVERAGE%"
            exit 1
          fi

          # JavaScript coverage
          MIN_JS_COVERAGE=80
          JS_COVERAGE=$(grep -oP '"lines":\{"total":\d+,"covered":\d+,"skipped":\d+,"pct":(\d+)' coverage/coverage-summary.json | grep -oP '\d+$')
          if [ $JS_COVERAGE -lt $MIN_JS_COVERAGE ]; then
            echo "❌ JS Coverage $JS_COVERAGE% is below minimum $MIN_JS_COVERAGE%"
            exit 1
          fi

          echo "✅ Coverage checks passed"
```

***

### Layer 4: Automated Test Review Comments (PR Automation)

**GitHub Action:** `.github/workflows/test-review-bot.yml`

```yaml
name: Test Review Bot

on:
  pull_request:
    types: [opened, synchronize]

jobs:
  review-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Analyze Changed Files
        id: analyze
        run: |
          # Get changed files in this PR
          PROD_FILES=$(git diff --name-only origin/master...HEAD | grep -E "src/(main|commonMain)" | grep -v "Test" || true)
          TEST_FILES=$(git diff --name-only origin/master...HEAD | grep -E "src/(test|commonTest)" | grep "Test" || true)

          echo "prod_files<<EOF" >> $GITHUB_OUTPUT
          echo "$PROD_FILES" >> $GITHUB_OUTPUT
          echo "EOF" >> $GITHUB_OUTPUT

          echo "test_files<<EOF" >> $GITHUB_OUTPUT
          echo "$TEST_FILES" >> $GITHUB_OUTPUT
          echo "EOF" >> $GITHUB_OUTPUT

      - name: Check Test Updates
        uses: actions/github-script@v7
        with:
          script: |
            const prodFiles = `${{ steps.analyze.outputs.prod_files }}`.split('\n').filter(f => f);
            const testFiles = `${{ steps.analyze.outputs.test_files }}`.split('\n').filter(f => f);

            if (prodFiles.length > 0 && testFiles.length === 0) {
              // Production code changed but no tests updated
              await github.rest.issues.createComment({
                issue_number: context.issue.number,
                owner: context.repo.owner,
                repo: context.repo.repo,
                body: `⚠️ **Test Update Reminder**\n\n` +
                      `This PR modifies production code but doesn't update any tests:\n\n` +
                      `**Production files changed:**\n` +
                      prodFiles.map(f => `- \`${f}\``).join('\n') + `\n\n` +
                      `**Please verify:**\n` +
                      `- [ ] Tests still pass with new code\n` +
                      `- [ ] Test signatures match updated constructors/functions\n` +
                      `- [ ] New validation rules are tested\n` +
                      `- [ ] Edge cases for new behavior are covered\n\n` +
                      `If tests don't need updates, please explain why in a comment.`
              });
            } else if (prodFiles.length > 0 && testFiles.length > 0) {
              // Both changed - good!
              await github.rest.issues.createComment({
                issue_number: context.issue.number,
                owner: context.repo.owner,
                repo: context.repo.repo,
                body: `✅ **Tests Updated**\n\n` +
                      `Production code and tests both updated - great job!\n\n` +
                      `**Production files:** ${prodFiles.length}\n` +
                      `**Test files:** ${testFiles.length}`
              });
            }
```

***

### Layer 5: Test Signature Validation (Runtime Detection)

**Test Helper:** `test-utils/signature-validator.js`

````javascript
/**
 * Validates that test mocks match production signatures
 *
 * Usage in tests:
 * ```javascript
 * import { validateSignature } from '../test-utils/signature-validator';
 *
 * const ProductionClass = require('../src/ProductionClass');
 *
 * test('should create instance', () => {
 *     // Validate our test data matches production constructor
 *     validateSignature(ProductionClass, {
 *         param1: 'string',
 *         param2: 42,
 *         param3: true
 *     });
 *
 *     const instance = new ProductionClass({ param1: 'test', param2: 42, param3: true });
 *     expect(instance).toBeDefined();
 * });
 * ```
 */

function validateSignature(ProductionClass, testParams) {
    try {
        // Try to construct with test params
        new ProductionClass(testParams);
    } catch (error) {
        if (error.message.includes('is required') ||
            error.message.includes('Cannot find') ||
            error.message.includes('Type mismatch')) {
            throw new Error(
                `❌ TEST DATA MISMATCH: Test parameters don't match production constructor!\n` +
                `Error: ${error.message}\n\n` +
                `This usually means:\n` +
                `1. Production constructor added/removed/renamed a parameter\n` +
                `2. Production validation rules changed\n` +
                `3. Test is using outdated parameter names/types\n\n` +
                `Action: Read production code and update test parameters to match.`
            );
        }
        throw error;
    }
}

module.exports = { validateSignature };
````

***

### Layer 6: Documentation Links in Code (Developer Reminder)

**Add to all production classes:**

```kotlin
/**
 * PSP Configuration
 *
 * ⚠️ IMPORTANT FOR TEST AUTHORS:
 * Before writing tests for this class, read:
 * - This file (understand current constructor signature)
 * - documentation/07-testing/TEST_CREATION_PATTERNS.md (test patterns)
 * - Recent commits (git log -5 -- PSPConfig.kt)
 *
 * Test files: psp-sdk/src/test/kotlin/com/zeropay/psp/PSPConfigTest.kt
 *
 * @property apiKey API key (format: psp_{env}_{pspId}_{random})
 * @property pspId PSP identifier
 * @property environment SANDBOX or PRODUCTION
 */
data class PSPConfig(
    val apiKey: String,
    val pspId: String,
    val environment: PSPEnvironment = PSPEnvironment.PRODUCTION,
    // ...
)
```

***

### Layer 7: Periodic Test Audit (Monthly Maintenance)

**Scheduled GitHub Action:** `.github/workflows/test-audit.yml`

```yaml
name: Monthly Test Audit

on:
  schedule:
    - cron: '0 0 1 * *'  # First day of every month
  workflow_dispatch:  # Manual trigger

jobs:
  audit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Run All Tests
        run: |
          ./gradlew test --no-daemon
          cd backend && npm test
          cd ../psp-sdk-web && npm test

      - name: Generate Test Report
        run: |
          echo "# Monthly Test Audit Report" > test-audit-report.md
          echo "Date: $(date)" >> test-audit-report.md
          echo "" >> test-audit-report.md

          # Count tests
          KOTLIN_TESTS=$(find . -name "TEST-*.xml" | xargs grep -oP 'tests="\d+"' | grep -oP '\d+' | awk '{s+=$1} END {print s}')
          JS_TESTS=$(grep -oP '"numTotalTests":\d+' coverage/coverage-summary.json | grep -oP '\d+')

          echo "## Test Count" >> test-audit-report.md
          echo "- Kotlin Tests: $KOTLIN_TESTS" >> test-audit-report.md
          echo "- JavaScript Tests: $JS_TESTS" >> test-audit-report.md
          echo "- Total: $((KOTLIN_TESTS + JS_TESTS))" >> test-audit-report.md

          # Find outdated timestamp constants
          echo "" >> test-audit-report.md
          echo "## Potential Issues" >> test-audit-report.md

          OLD_TIMESTAMPS=$(grep -r "170000000" --include="*Test.kt" --include="*.test.js" || true)
          if [ -n "$OLD_TIMESTAMPS" ]; then
            echo "⚠️ Found hardcoded old timestamps:" >> test-audit-report.md
            echo "$OLD_TIMESTAMPS" >> test-audit-report.md
          fi

      - name: Create Issue if Problems Found
        if: failure()
        uses: actions/github-script@v7
        with:
          script: |
            await github.rest.issues.create({
              owner: context.repo.owner,
              repo: context.repo.repo,
              title: '🔍 Monthly Test Audit - Issues Found',
              body: require('fs').readFileSync('test-audit-report.md', 'utf8'),
              labels: ['testing', 'maintenance']
            });
```

***

## 📋 Implementation Checklist

### Immediate (Next Commit):

* [ ] Create TEST\_CREATION\_PATTERNS.md
* [ ] Create TEST\_SAFEGUARD\_STRATEGY.md (this file)
* [ ] Add pre-commit hook template
* [ ] Update CLAUDE.md with safeguard references

### Short Term (This Week):

* [ ] Implement test compilation check in CI/CD
* [ ] Add test review bot to GitHub Actions
* [ ] Create signature validation test utility
* [ ] Add documentation links to top 10 most-changed classes

### Medium Term (This Month):

* [ ] Enable branch protection requiring test compilation
* [ ] Implement coverage enforcement
* [ ] Set up monthly test audit
* [ ] Train team on new safeguard processes

### Long Term (Ongoing):

* [ ] Monitor safeguard effectiveness
* [ ] Adjust thresholds based on team feedback
* [ ] Add more automated checks as patterns emerge
* [ ] Document lessons learned

***

## 🎯 Success Metrics

### Before Safeguards (Current State):

* ❌ 23 merchant test failures (missing parameters)
* ❌ 6 PSP-SDK test failures (outdated timestamps, signatures)
* ❌ 7 PSP-SDK-Web test failures (navigation mocking, API key validation)
* ❌ Tests lag behind code by weeks/months
* ❌ Hours wasted debugging obvious mismatches

### After Safeguards (Target State):

* ✅ 100% test compilation rate
* ✅ Tests updated in same commit as code changes
* ✅ Automated detection of test/code mismatches
* ✅ Zero "forgotten test update" incidents
* ✅ <5 minutes to detect test issues (vs hours/days)

***

## 🔧 Quick Reference

**For Developers:**

```bash
# Before committing code changes:
1. Run tests: npm test / ./gradlew test
2. Check if tests compile
3. Update test signatures if code changed
4. Commit code + tests together

# Pre-commit hook will remind you if you forget!
```

**For Reviewers:**

```markdown
# PR Review Checklist:
- [ ] Production code changes?
  - [ ] Are tests also updated?
  - [ ] Do tests compile?
  - [ ] Do tests pass?
- [ ] Test-only changes?
  - [ ] Do they match current production code?
  - [ ] Are signatures up to date?
```

***

## 📊 Safeguard Layer Summary

| Layer                   | Detection Time | Automation Level | Effectiveness               |
| ----------------------- | -------------- | ---------------- | --------------------------- |
| 1. Pre-commit Hook      | Immediate      | Semi-automated   | High (prevents commits)     |
| 2. CI/CD Compilation    | Build-time     | Fully automated  | High (blocks PRs)           |
| 3. Coverage Enforcement | PR-time        | Fully automated  | Medium (ensures coverage)   |
| 4. Test Review Bot      | PR-time        | Fully automated  | Medium (reminders)          |
| 5. Signature Validation | Test runtime   | Manual (opt-in)  | Medium (catches mismatches) |
| 6. Code Documentation   | Development    | Manual           | Low (awareness)             |
| 7. Monthly Audit        | Scheduled      | Fully automated  | Low (cleanup)               |

***

## 💡 Key Insight

**The best safeguard is culture:**

* Train developers to update tests with code
* Make test updates part of definition of done
* Celebrate good test coverage in code reviews
* Make failing tests visible and urgent

**But automation catches what culture misses.**

***

**Last Updated:** 2025-12-24 **Status:** Implementation In Progress **Owner:** Development Team
