# NoTap SDK Examples

This directory contains example code demonstrating NoTap SDK integration.
More examples will be added here as they're written and reviewed — this
folder grows in stages, same as the rest of this repo's public docs.

---

## 📱 Available Examples

### Android Quick Start

**Path:** [`android-quickstart/`](android-quickstart/)

A minimal Android activity showing SDK initialization, enrollment, and
verification.

---

## 🔐 Security Notes

**Never commit API keys to version control.** Use environment variables or
your platform's build configuration instead:

**Android (`build.gradle.kts`):**
```kotlin
android {
    defaultConfig {
        val apiKey = project.findProperty("notap.api.key") as String? ?: ""
        buildConfigField("String", "NOTAP_API_KEY", "\"$apiKey\"")
    }
}
```

---

## 🤝 Need help?

- 💬 [GitHub Discussions](https://github.com/NoTap-Labs/NoTap-SDK/discussions)
- 🐛 [GitHub Issues](https://github.com/NoTap-Labs/NoTap-SDK/issues)
