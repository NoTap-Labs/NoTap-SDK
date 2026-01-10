# Online Demo Deployment

## Overview

The NoTap authentication demo (`demo-pitch/notap-demo-v7.0.html`) is automatically deployed to **app.notap.io/demo** as part of the online-web build process.

## How It Works

### Build Process

1. **Source**: `demo-pitch/notap-demo-v7.0.html` (single-file React demo)
2. **Build Task**: `copyDemoToOutput` (defined in `build.gradle.kts`)
3. **Destination**: `build/dist/js/productionExecutable/demo/index.html`
4. **Deployment**: Dockerfile copies entire `productionExecutable/` to `/app/public`
5. **Access**: https://app.notap.io/demo

### Build Structure

```
build/dist/js/productionExecutable/
├── demo/
│   └── index.html          # NoTap demo (accessible at /demo)
├── index.html              # Main online-web app (accessible at /)
├── online-web.js           # Kotlin/JS compiled bundle
├── online-web.js.map       # Source map
└── wallet.css              # Styles
```

## Building Locally

```bash
# Build production bundle (includes demo copy)
cmd.exe /c 'set "JAVA_HOME=C:\Program Files\Java\jre1.8.0_461" && gradlew.bat :online-web:jsBrowserProductionWebpack --no-daemon'

# Verify demo was copied
ls online-web/build/dist/js/productionExecutable/demo/
```

## Deployment

The demo is included in every Railway deployment:

1. **Automatic**: Push to master → Railway auto-deploys → Demo included
2. **Manual**: `railway up` → Demo included in deployment

## Updating the Demo

When you update `demo-pitch/notap-demo-v7.0.html`:

1. **Commit** the changes
2. **Push** to master
3. **Build** runs automatically on Railway
4. **Demo** updates at app.notap.io/demo

No manual copy steps needed - fully automated!

## Features

The demo works **identically online and offline**:
- ✅ No backend required (fully client-side)
- ✅ localStorage for enrollment data
- ✅ Mock blockchain name resolution
- ✅ All 14 authentication factors
- ✅ Profile-based and business service flows
- ✅ Service-specific dashboards

## URLs

- **Main App**: https://app.notap.io
- **Demo**: https://app.notap.io/demo
- **Repository**: https://github.com/NoTap-Labs/zero-pay-sdk

## Notes

- Demo file size: ~425KB (uncompressed)
- Served statically via `serve` package
- No server-side processing
- Works offline when saved locally
