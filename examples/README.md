# NoTap SDK Examples

This directory contains example applications demonstrating NoTap SDK integration.

***

## 📱 Available Examples

### 1. Android Quick Start

**Path:** `android-quickstart/`

Minimal Android app showing basic enrollment and verification.

**Features:**

* SDK initialization
* User enrollment
* Payment verification
* Error handling

**Run:**

```bash
cd android-quickstart
./gradlew assembleDebug
```

***

### 2. E-Commerce Demo

**Path:** `ecommerce-demo/`

Complete e-commerce app with NoTap checkout.

**Features:**

* Product catalog
* Shopping cart
* NoTap checkout integration
* Stripe payment processing
* Order confirmation

**Run:**

```bash
cd ecommerce-demo
./gradlew assembleDebug
```

***

### 3. Web SDK Demo

**Path:** `web-demo/`

Browser-based authentication demo.

**Features:**

* Web SDK initialization
* In-browser enrollment
* Factor capture (PIN, Pattern, Emoji, etc.)
* Payment verification

**Run:**

```bash
cd web-demo
npm install
npm run dev
# Open http://localhost:3000
```

***

### 4. Blockchain Payments

**Path:** `blockchain-payments/`

Solana blockchain payment integration.

**Features:**

* Phantom wallet linking
* NoTap verification
* Solana transaction signing
* Transaction status polling

**Run:**

```bash
cd blockchain-payments
./gradlew assembleDebug
```

***

## 🚀 Getting Started

### Prerequisites

**For Android examples:**

* Android Studio Hedgehog (2023.1.1+)
* Android SDK 24+
* JDK 17+

**For Web examples:**

* Node.js 18+
* npm or yarn

### Setup

1.  **Clone this repository:**

    ```bash
    git clone https://github.com/keikworld/NoTap.git
    cd NoTap/examples
    ```
2.  **Choose an example:**

    ```bash
    cd android-quickstart  # or ecommerce-demo, web-demo, etc.
    ```
3.  **Add your API key:**

    ```bash
    # Create local.properties (Android)
    echo "notap.api.key=your_api_key_here" > local.properties

    # Or create .env (Web)
    echo "NOTAP_API_KEY=your_api_key_here" > .env
    ```
4.  **Run the example:**

    ```bash
    # Android
    ./gradlew assembleDebug

    # Web
    npm install && npm run dev
    ```

***

## 📚 What You'll Learn

### Android Quick Start

* ✅ How to initialize NoTap SDK
* ✅ Basic enrollment flow
* ✅ Payment verification
* ✅ Error handling patterns

### E-Commerce Demo

* ✅ Full checkout integration
* ✅ Cart management with NoTap
* ✅ Payment gateway integration (Stripe)
* ✅ Production-ready architecture

### Web SDK Demo

* ✅ Browser-based authentication
* ✅ Web SDK initialization
* ✅ Factor capture in the browser
* ✅ Progressive web app patterns

### Blockchain Payments

* ✅ Wallet-free blockchain payments
* ✅ Solana integration
* ✅ Transaction signing and verification
* ✅ Blockchain explorer integration

***

## 🔐 Security Notes

### API Keys

**Never commit API keys to version control!**

```bash
# Add to .gitignore
echo "local.properties" >> .gitignore
echo ".env" >> .gitignore
echo "*.properties" >> .gitignore
```

Use environment variables or build configuration:

**Android (build.gradle.kts):**

```kotlin
android {
    defaultConfig {
        val apiKey = project.findProperty("notap.api.key") as String? ?: ""
        buildConfigField("String", "NOTAP_API_KEY", "\"$apiKey\"")
    }
}
```

**Web (.env):**

```bash
NOTAP_API_KEY=your_key_here
```

***

## 🐛 Troubleshooting

### "API key not found"

**Solution:** Create `local.properties` (Android) or `.env` (Web) with your API key.

### "Module not found" (Web)

**Solution:**

```bash
rm -rf node_modules package-lock.json
npm install
```

### "SDK version mismatch" (Android)

**Solution:** Update dependencies in `build.gradle.kts`:

```kotlin
implementation("com.zeropay:sdk:1.0.0")
```

***

## 📖 Additional Resources

* [Getting Started Guide](../docs/getting-started.md)
* [Integration Guide](/broken/pages/PbuP36zsQV2DenLcwsGH)
* [API Reference](../docs/api-reference.md)
* [FAQ](../docs/faq.md)

***

## 🤝 Need Help?

* 📧 Email: support@notap.io
* 💬 Discussions: [GitHub Discussions](https://github.com/keikworld/NoTap/discussions)
* 🐛 Issues: [GitHub Issues](https://github.com/keikworld/NoTap/issues)

***

**Happy coding! 🚀**
