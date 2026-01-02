# NoTap Web Frontend Deployment Guide

## Overview

This document explains how to deploy the NoTap web frontend (enrollment, verification, management portal) to make it accessible to users for testing.

## Architecture

```
Frontend: https://app.notap.io (or your custom domain)
Backend:  https://api.notap.io (already deployed)
```

The web frontend is a **static site** (HTML/CSS/JavaScript) that calls the backend API.

## Deployment Options

### Option 1: Railway (Recommended)

**Pros:**
- ✅ Same platform as backend (easy management)
- ✅ Custom domain support
- ✅ Automatic HTTPS
- ✅ Git-based deployments
- ✅ Environment variables for API_URL

**Steps:**

1. **Navigate to online-web directory:**
   ```bash
   cd online-web
   ```

2. **Initialize Railway (if not done):**
   ```bash
   npx railway login
   npx railway init
   # Select: Create new project
   # Name: notap-web-frontend
   ```

3. **Link to Railway service:**
   ```bash
   npx railway link
   # Select: notap-web-frontend
   ```

4. **Set environment variables:**
   ```bash
   npx railway variables set API_URL=https://api.notap.io
   ```

5. **Deploy:**
   ```bash
   npx railway up
   ```

6. **Get deployment URL:**
   ```bash
   npx railway domain
   # Example output: https://notap-web-frontend-production.up.railway.app
   ```

7. **Set custom domain (optional):**
   ```bash
   npx railway domain add app.notap.io
   # Then add DNS record: CNAME app.notap.io -> <railway-url>
   ```

### Option 2: Netlify (Alternative)

**Pros:**
- ✅ Free tier generous for static sites
- ✅ Excellent CDN performance
- ✅ Easy custom domains

**Steps:**

1. **Install Netlify CLI:**
   ```bash
   npm install -g netlify-cli
   ```

2. **Build production bundle:**
   ```bash
   cd ..
   ./gradlew :online-web:jsBrowserProductionWebpack
   ```

3. **Deploy:**
   ```bash
   cd online-web
   netlify deploy --prod --dir=build/dist/js/productionExecutable
   ```

4. **Result:**
   ```
   Website URL: https://notap-app.netlify.app
   ```

### Option 3: Vercel (Alternative)

**Steps:**

1. **Install Vercel CLI:**
   ```bash
   npm install -g vercel
   ```

2. **Build and deploy:**
   ```bash
   cd ..
   ./gradlew :online-web:jsBrowserProductionWebpack
   cd online-web
   vercel --prod
   # Select: build/dist/js/productionExecutable as public directory
   ```

### Option 4: GitHub Pages (Free)

**Pros:**
- ✅ Completely free
- ✅ Good for testing/demos

**Cons:**
- ⚠️ No custom domain on free tier
- ⚠️ Public repository required

**Steps:**

1. **Create gh-pages branch:**
   ```bash
   git checkout -b gh-pages
   ```

2. **Copy build output:**
   ```bash
   cp -r online-web/build/dist/js/productionExecutable/* .
   git add .
   git commit -m "Deploy web frontend"
   git push origin gh-pages
   ```

3. **Enable GitHub Pages:**
   - Go to repository Settings → Pages
   - Source: gh-pages branch
   - Result: `https://yourusername.github.io/zero-pay-sdk/`

## Testing URLs

Once deployed, users can access:

### Enrollment Flow
```
https://app.notap.io/enroll
```
Users can:
- Select 3+ factors (PIN, Pattern, Emoji, etc.)
- Register blockchain name (alice.sol, vitalik.eth)
- Complete enrollment wizard
- Receive UUID for verification

### Verification Flow
```
https://app.notap.io/verify
```
Users can:
- Enter UUID, Alias, or SNS name
- Complete factor challenges
- Test payment authentication

### Management Portal
```
https://app.notap.io/manage
```
Users can:
- View enrolled factors
- Update auto-renewal settings
- Check crypto balance (if wallet connected)
- View transaction history

### Developer Portal
```
https://app.notap.io/developer
```
Developers can:
- Generate API keys
- View webhooks
- Access sandbox mode
- Read integration guides

## Environment Variables

The frontend needs to know the backend API URL:

**Railway:**
```bash
npx railway variables set API_URL=https://api.notap.io
```

**Netlify:**
```bash
# netlify.toml
[build.environment]
  API_URL = "https://api.notap.io"
```

**Vercel:**
```bash
# vercel.json
{
  "env": {
    "API_URL": "https://api.notap.io"
  }
}
```

## Build Commands

**Development (local testing):**
```bash
./gradlew :online-web:jsBrowserDevelopmentRun
# Server: http://localhost:8080
```

**Production (deployment):**
```bash
./gradlew :online-web:jsBrowserProductionWebpack
# Output: online-web/build/dist/js/productionExecutable/
```

## Post-Deployment Checklist

After deploying:

- [ ] Test enrollment flow end-to-end
- [ ] Test verification flow with UUID/Alias/SNS
- [ ] Test all 10 factor canvases (PIN, Pattern, Emoji, etc.)
- [ ] Test management portal features
- [ ] Test developer portal API key generation
- [ ] Verify CORS is configured on backend for frontend domain
- [ ] Test WebAuthn biometric assist (Chrome/Safari/Firefox)
- [ ] Test auto-renewal settings toggle

## CORS Configuration (Backend)

Ensure backend (`api.notap.io`) allows requests from frontend domain:

**backend/server.js:**
```javascript
const cors = require('cors');

const allowedOrigins = [
  'https://app.notap.io',
  'https://notap-web-frontend-production.up.railway.app',
  'http://localhost:8080' // Development
];

app.use(cors({
  origin: (origin, callback) => {
    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('CORS not allowed'));
    }
  },
  credentials: true
}));
```

## Sharing with Users

**For Merchants (Testing Payment Authentication):**
```
Hi! Please test our payment authentication at:

1. Enroll: https://app.notap.io/enroll
   - Choose 3+ factors (PIN, Pattern, Emoji recommended)
   - Save your UUID (you'll need it for verification)

2. Verify: https://app.notap.io/verify
   - Enter your UUID
   - Complete factor challenges
   - Simulate payment authentication

Feedback welcome!
```

**For Users (Testing Enrollment):**
```
Try NoTap device-free authentication:

https://app.notap.io/enroll

Features to test:
✅ 10 different authentication factors
✅ Blockchain name registration (.sol, .eth)
✅ Wallet connection (Phantom, MetaMask)
✅ Auto-renewal settings
✅ Biometric assist (if you fail a challenge)

Let us know your experience!
```

## Troubleshooting

**Issue: CORS errors**
- Solution: Add frontend domain to backend CORS allowlist

**Issue: API calls fail**
- Solution: Check API_URL environment variable is set correctly

**Issue: Build fails**
- Solution: Run `./gradlew clean` then rebuild

**Issue: 404 on routes**
- Solution: Configure server to route all paths to index.html (SPA mode)

## Monitoring

**Railway:**
```bash
npx railway logs
```

**Netlify:**
```bash
netlify logs:function
```

## Cost Estimate

| Platform | Free Tier | Cost (1,000 users/day) |
|----------|-----------|------------------------|
| Railway | 500 hours/month | ~$5/month |
| Netlify | 100GB bandwidth | Free |
| Vercel | 100GB bandwidth | Free |
| GitHub Pages | Unlimited | Free |

**Recommendation:** Start with Netlify (free) or Railway (easy management with backend).

## Next Steps

1. Build production bundle (running now)
2. Choose deployment platform (Railway recommended)
3. Deploy using steps above
4. Test all flows
5. Share URL with merchants and users
6. Collect feedback

## Support

Issues? Check:
- Backend logs: `npx railway logs` (in backend directory)
- Frontend logs: Browser DevTools → Console
- API status: https://api.notap.io/health
