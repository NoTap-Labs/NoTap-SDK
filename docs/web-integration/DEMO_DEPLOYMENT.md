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

**⚠️ CRITICAL: Manual Build Step Required**

When you update `demo-pitch/notap-demo-v7.0.html`, follow this EXACT process:

### Step 1: Run Gradle Copy Task (REQUIRED)
```bash
# From project root
cd online-web
cmd.exe /c 'set "JAVA_HOME=C:\Program Files\Java\jre1.8.0_461" && ..\gradlew.bat :online-web:copyDemoToOutput --no-daemon'
```

This copies `demo-pitch/notap-demo-v7.0.html` → `online-web/build/dist/js/productionExecutable/demo/index.html`

**Why this is required:**
- Railway deploys from the `build/` directory, NOT from `demo-pitch/`
- The Gradle task must run AFTER editing the source file
- Without this step, your changes won't be deployed!

### Step 2: Verify Copy Success
```bash
# Check file was updated
ls -lh build/dist/js/productionExecutable/demo/index.html

# Verify content (should show 1+ matches for recent changes)
grep -c "YourRecentChangeText" build/dist/js/productionExecutable/demo/index.html
```

### Step 3: Commit Build Output
```bash
# Commit the updated build output
git add online-web/build/dist/js/productionExecutable/demo/index.html
git commit -m "deploy(demo): Update deployed demo with [description]"
```

### Step 4: Push to Master
```bash
git push origin master
```

### Step 5: Wait for Railway Deployment
- Railway detects the push automatically
- Builds Docker container (1-2 minutes)
- Deploys to app.notap.io (30 seconds)
- **Total wait time: 2-3 minutes**

### Step 6: Clear Browser Cache
After Railway deploys, users may see cached version:

**Hard Refresh:**
- **Windows/Linux**: Ctrl + Shift + R
- **Mac**: Cmd + Shift + R
- **Alternative**: Open in incognito/private window

## File Path Confusion (Important!)

⚠️ **Windows Case-Insensitivity Issue:**

In Git commit history, you'll see **two** paths that are actually the **same file**:
- `Demo-Pitch/notap-demo-v7.0.html` (Git tracking - capital D, capital P)
- `demo-pitch/notap-demo-v7.0.html` (Gradle reads - lowercase)

**Why they're the same:**
- Windows filesystem is case-insensitive
- Both paths point to the same inode (same physical file)
- Editing one updates the other automatically

**Verification:**
```bash
# These show the same file (same inode number)
ls -i demo-pitch/notap-demo-v7.0.html Demo-Pitch/notap-demo-v7.0.html
```

**Recommendation:** Use lowercase `demo-pitch/` in documentation for consistency with Gradle configuration.

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

## Troubleshooting

### Test User Not Recognized

**Symptom:** After clicking "Load Test User", authentication/payment flows don't recognize test user identifiers (UUID, alias, or blockchain name)

**Root Causes & Solutions:**

#### 1. Browser Cache (Most Common)
The browser is serving an **old cached version** of the demo.

**Solution:**
```bash
# Hard refresh to bypass cache
Windows/Linux: Ctrl + Shift + R
Mac: Cmd + Shift + R

# Or use incognito/private window
```

#### 2. Railway Deployment Pending
Changes pushed to GitHub but Railway hasn't deployed yet.

**Solution:**
- Wait 2-3 minutes after pushing
- Check Railway dashboard for deployment status
- Verify deployment ID matches latest commit

#### 3. Build Output Not Committed
Source file edited but `copyDemoToOutput` task not run.

**Solution:**
```bash
# Run the Gradle task
cd online-web
cmd.exe /c 'set "JAVA_HOME=C:\Program Files\Java\jre1.8.0_461" && ..\gradlew.bat :online-web:copyDemoToOutput --no-daemon'

# Verify file was updated
ls -lh build/dist/js/productionExecutable/demo/index.html

# Commit and push
git add online-web/build/dist/js/productionExecutable/demo/index.html
git commit -m "deploy(demo): Update build output"
git push origin master
```

#### 4. localStorage Cleared
Test user data stored in browser localStorage was cleared.

**Solution:**
- Click "Load Test User" button again
- Verify alert shows:
  ```
  ✅ Test User Loaded!
  UUID: rasta-f4l5-0123-4567-89ab
  Alias: rasta-9876
  Blockchain Name: rastafalso.sol
  ```

#### 5. Wrong Identifier Used
Using incorrect test user credentials.

**Solution:**
Valid test identifiers:
- UUID: `rasta-f4l5-0123-4567-89ab`
- Alias: `rasta-9876`
- Blockchain Name: `rastafalso.sol`

Test values:
- PIN: `1234`
- Pattern: L shape (3 dots: top-left, middle-left, middle-middle)
- Emoji: 😀 😎 🎉 ❤️
- Colors: Red, Blue, Green (#FF0000, #0000FF, #00FF00)
- Words: test, demo, user, rasta

### Verification Checklist

Before reporting issues, verify:

- [ ] Hard refreshed browser (Ctrl+Shift+R)
- [ ] Clicked "Load Test User" button
- [ ] Saw success alert with test credentials
- [ ] Used correct identifier (UUID/alias/blockchain name)
- [ ] Railway deployment completed (check dashboard)
- [ ] Build output file was committed and pushed

### Quick Diagnostic

```bash
# Verify deployed file has latest changes
curl -s https://app.notap.io/demo | grep -c "Authenticating\.\.\."
# Should return 1 (not 0)

# Check file size (should be ~438KB)
curl -sI https://app.notap.io/demo | grep -i content-length

# Verify build output locally
grep -c "modes: \['payment', 'authentication'\]" online-web/build/dist/js/productionExecutable/demo/index.html
# Should return 1
```

## Notes

- Demo file size: ~438KB (uncompressed)
- Served statically via Express server
- No server-side processing required
- Works offline when saved locally
- Test user data stored in browser localStorage (not persistent across sessions unless saved)
