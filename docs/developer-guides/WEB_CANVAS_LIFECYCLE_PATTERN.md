# Web Canvas Lifecycle Pattern

**Version:** 1.0.0
**Date:** 2026-02-02
**Applies To:** online-web factor canvases, verification steps, enrollment steps

---

## Overview

This document defines the REQUIRED pattern for all web UI components that:
- Use async operations (crypto, network calls)
- Attach DOM event listeners
- Render/re-render dynamically

**Purpose:** Prevent memory leaks, race conditions, and orphaned coroutines.

---

## The Problem

### ❌ ANTI-PATTERN (DO NOT USE)

```kotlin
object BadCanvas {
    fun render(root: HTMLDivElement, onComplete: suspend (ByteArray) -> Unit) {
        root.append {
            button {
                attributes["id"] = "submit-btn"
                +"Submit"
            }
        }

        // ❌ WRONG: Event listener without cleanup reference
        val submitBtn = document.getElementById("submit-btn")
        submitBtn?.addEventListener("click", {
            // ❌ WRONG: GlobalScope continues after navigation
            GlobalScope.launch {
                val digest = CryptoUtils.sha256Suspend(data)
                onComplete(digest)  // Called even if user navigated away!
            }
        })
    }
}
```

**Problems:**
1. **GlobalScope.launch** - Coroutine continues after user navigates away (orphaned)
2. **No listener storage** - Can't remove listener on cleanup
3. **No cleanup function** - Resources leak on every re-render
4. **Race conditions** - Multiple crypto operations run if user navigates back/forth

**Impact:**
- After 10 navigation cycles: 10 orphaned coroutines + 20+ orphaned listeners
- Memory leak: ~500KB per session (Web Crypto buffers + listener closures)
- Race conditions: User sees wrong factor's digest if operations overlap

---

## ✅ CORRECT PATTERN (REQUIRED)

### Full Example: PinCanvas.kt

```kotlin
object PinCanvas {

    // ============================================================
    // STEP 1: Store Listener References for Cleanup
    // ============================================================

    // REQUIRED: Store ALL event listener references
    // Why: removeEventListener needs exact function reference
    private var inputListener: ((org.w3c.dom.events.Event) -> Unit)? = null
    private var keypressListener: ((org.w3c.dom.events.Event) -> Unit)? = null
    private var toggleListener: ((org.w3c.dom.events.Event) -> Unit)? = null
    private var submitListener: ((org.w3c.dom.events.Event) -> Unit)? = null

    // ============================================================
    // STEP 2: Cancellable Coroutine Scope (NOT GlobalScope)
    // ============================================================

    // REQUIRED: Use cancellable scope for async operations
    // Why: Prevents orphaned coroutines after navigation
    private var coroutineScope: CoroutineScope? = null

    // ============================================================
    // STEP 3: Cleanup Function (MANDATORY)
    // ============================================================

    /**
     * Cleanup event listeners and coroutines to prevent memory leaks
     *
     * MUST be called:
     * - Before re-rendering canvas
     * - When navigating away from step
     * - Before EnrollmentFlow/VerificationFlow clears DOM
     */
    fun cleanup() {
        // Cancel any running coroutines to prevent race conditions
        coroutineScope?.cancel()
        coroutineScope = null

        // Remove ALL event listeners using stored references
        val pinInput = document.getElementById("pin-input") as? HTMLInputElement
        val toggleBtn = document.getElementById("toggle-pin-visibility") as? org.w3c.dom.HTMLButtonElement
        val submitBtn = document.getElementById("submit-pin") as? org.w3c.dom.HTMLButtonElement

        inputListener?.let { pinInput?.removeEventListener("input", it) }
        keypressListener?.let { pinInput?.removeEventListener("keypress", it) }
        toggleListener?.let { toggleBtn?.removeEventListener("click", it) }
        submitListener?.let { submitBtn?.removeEventListener("click", it) }

        // Clear listener references
        inputListener = null
        keypressListener = null
        toggleListener = null
        submitListener = null
    }

    // ============================================================
    // STEP 4: Render with Cleanup-First Pattern
    // ============================================================

    fun render(root: HTMLDivElement, onComplete: suspend (ByteArray) -> Unit) {
        console.log("🎨 Rendering PIN Canvas...")

        // ✅ CRITICAL: Cleanup BEFORE re-render
        cleanup()

        // ✅ Create new cancellable scope for this render lifecycle
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // Clear existing content
        root.innerHTML = ""

        // Render UI
        root.append {
            div("factor-canvas pin-canvas") {
                // ... UI elements ...
                input(type = InputType.password) {
                    attributes["id"] = "pin-input"
                    // ...
                }
                button {
                    attributes["id"] = "submit-pin"
                    +"Continue"
                }
            }
        }

        // Setup event listeners (stores references)
        setupEventListeners(onComplete)

        console.log("✅ PIN Canvas rendered")
    }

    // ============================================================
    // STEP 5: Setup Listeners with Reference Storage
    // ============================================================

    private fun setupEventListeners(onComplete: suspend (ByteArray) -> Unit) {
        val pinInput = document.getElementById("pin-input") as? HTMLInputElement
        val submitBtn = document.getElementById("submit-pin") as? org.w3c.dom.HTMLButtonElement

        // ✅ Store listener reference for cleanup
        inputListener = { _: org.w3c.dom.events.Event ->
            validatePinRealtime()
        }
        pinInput?.addEventListener("input", inputListener!!)

        // ✅ Store listener reference for cleanup
        submitListener = { _: org.w3c.dom.events.Event ->
            submitPin(onComplete)
        }
        submitBtn?.addEventListener("click", submitListener!!)

        console.log("✅ Event listeners setup")
    }

    // ============================================================
    // STEP 6: Async Operations with Cancellable Scope
    // ============================================================

    private fun submitPin(onComplete: suspend (ByteArray) -> Unit) {
        val input = document.getElementById("pin-input") as? HTMLInputElement ?: return
        val pin = input.value

        // Validate first
        val validation = PinProcessor.validate(pin)
        if (!validation.isValid) {
            console.error("Cannot submit invalid PIN")
            return
        }

        console.log("✅ PIN is valid, generating digest...")

        // ✅ Use cancellable scope instead of GlobalScope
        // This prevents crypto operations from continuing after navigation
        val scope = coroutineScope ?: return
        scope.launch {
            try {
                // Generate SHA-256 digest using SDK CryptoUtils
                val digest = CryptoUtils.sha256Suspend(pin.encodeToByteArray())

                console.log("✅ PIN digest generated: ${digest.size} bytes")

                // Clear input for security
                input.value = ""

                // Call completion callback (within coroutine context)
                onComplete(digest)

            } catch (e: Exception) {
                console.error("Failed to generate PIN digest: ${e.message}", e)
                // Show error to user
            }
        }
    }
}
```

---

## Required Implementation Checklist

For EVERY canvas/step component, you MUST:

- [ ] **Store listener references** - All event listeners stored in private fields
- [ ] **Use cancellable scope** - `private var coroutineScope: CoroutineScope?` instead of GlobalScope
- [ ] **Implement cleanup()** - Cancel scope + remove all listeners
- [ ] **Call cleanup() first in render()** - Before re-rendering
- [ ] **Use scope.launch** - NOT GlobalScope.launch
- [ ] **Check scope exists** - `val scope = coroutineScope ?: return` before launching

---

## Security Considerations

### 1. **Prevent Race Conditions (Crypto Security)**

```kotlin
// ❌ WRONG: GlobalScope allows race condition
GlobalScope.launch {
    val digest = CryptoUtils.sha256Suspend(factorData)
    onComplete(digest)  // Might be wrong factor's digest!
}

// User navigates: PIN → Pattern (quickly)
// Result: PIN digest completes AFTER Pattern canvas renders
// Security Impact: Pattern canvas receives PIN's digest = authentication bypass!
```

```kotlin
// ✅ CORRECT: Cancellable scope prevents race condition
val scope = coroutineScope ?: return
scope.launch {
    val digest = CryptoUtils.sha256Suspend(factorData)
    // If user navigated away, scope is cancelled
    // onComplete never called with wrong digest
    onComplete(digest)
}
```

### 2. **Clear Sensitive Input Immediately**

```kotlin
// ✅ Clear input after digest generation (PinCanvas.kt:317)
input.value = ""
```

### 3. **Prevent Listener Accumulation DoS**

```kotlin
// ❌ WRONG: 10 renders = 10 listeners = 10x event processing
button?.addEventListener("click", { ... })

// Attacker: Rapidly navigate back/forth 100 times
// Result: 100 listeners = 100x CPU per click = DoS

// ✅ CORRECT: Always 1 listener maximum
cleanup()  // Removes old listener
buttonListener = { ... }
button?.addEventListener("click", buttonListener!!)
```

---

## Parent Component Responsibilities

### EnrollmentFlow.kt / VerificationFlow.kt MUST call cleanup

```kotlin
// ✅ CORRECT: Call cleanup before DOM clear
private fun render(rootElement: HTMLDivElement, forceRender: Boolean = false) {
    val needsFullRender = forceRender || lastRenderedStep != currentStep

    if (needsFullRender) {
        // ✅ CRITICAL: Cleanup ALL canvases before DOM clear
        PinCanvas.cleanup()
        PatternCanvas.cleanup()
        EmojiCanvas.cleanup()
        ColorCanvas.cleanup()
        WordsCanvas.cleanup()
        RhythmCanvas.cleanup()
        MouseDrawCanvas.cleanup()
        StylusDrawCanvas.cleanup()
        VoiceCanvas.cleanup()
        ImageTapCanvas.cleanup()

        // Now safe to clear DOM
        rootElement.innerHTML = ""
        renderProgressIndicator(rootElement)
    }

    // ... render current step
}
```

---

## Migration Guide

### Step 1: Add Required Fields

```kotlin
object YourCanvas {
    // Add these fields
    private var coroutineScope: CoroutineScope? = null
    private var submitListener: ((Event) -> Unit)? = null
    private var resetListener: ((Event) -> Unit)? = null
    // ... one field per event listener
}
```

### Step 2: Implement cleanup()

```kotlin
fun cleanup() {
    // Cancel coroutines
    coroutineScope?.cancel()
    coroutineScope = null

    // Remove listeners
    val submitBtn = document.getElementById("submit-btn")
    submitListener?.let { submitBtn?.removeEventListener("click", it) }
    submitListener = null

    // Repeat for all listeners
}
```

### Step 3: Update render()

```kotlin
fun render(root: HTMLDivElement, onComplete: suspend (ByteArray) -> Unit) {
    // Add cleanup first
    cleanup()

    // Create new scope
    coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ... rest of render logic
}
```

### Step 4: Store Listeners

```kotlin
private fun setupListeners() {
    submitListener = { event ->
        // handler logic
    }
    submitBtn?.addEventListener("click", submitListener!!)
}
```

### Step 5: Replace GlobalScope

```kotlin
// OLD
GlobalScope.launch {
    val digest = CryptoUtils.sha256Suspend(data)
    onComplete(digest)
}

// NEW
val scope = coroutineScope ?: return
scope.launch {
    val digest = CryptoUtils.sha256Suspend(data)
    onComplete(digest)
}
```

---

## Testing the Fix

### Manual Test

1. Navigate: UUID Input → Factor Selection → Factor Capture
2. Capture PIN → Back → Capture Pattern → Back → Capture PIN again
3. Open browser DevTools → Performance → Memory
4. Check: Memory should NOT increase after 10 back/forth cycles
5. Check: Only 1 set of listeners per canvas (inspect Event Listeners tab)

### Automated Test (Future)

```kotlin
@Test
fun testCanvasCleanup() {
    val canvas = PinCanvas
    val root = document.createElement("div") as HTMLDivElement

    // Render 10 times
    repeat(10) {
        canvas.render(root) { /* no-op */ }
    }

    canvas.cleanup()

    // Assert: No coroutines running
    // Assert: No listeners attached
}
```

---

## References

- **Correct Implementation:** `online-web/src/jsMain/kotlin/com/zeropay/web/ui/factors/PinCanvas.kt`
- **Lesson 40:** `documentation/LESSONS_LEARNED.md` - Kotlin/JS forceRender pattern
- **Coroutine Docs:** https://kotlinlang.org/docs/coroutines-basics.html#structured-concurrency

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-02-02 | Initial pattern documentation (based on PinCanvas analysis) |
