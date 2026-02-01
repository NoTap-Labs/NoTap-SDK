# NoTap Web App - Authentication Setup

## Overview

The NoTap web application now has authentication to protect the main app while keeping the demo public.

**Access Control:**
- ✅ **Public (No Auth):** `/demo` - Demo remains accessible to everyone
- 🔒 **Protected (Auth Required):** `/` - Main app requires username/password
  - Enrollment flow
  - Verification flow
  - Developer portal
  - API documentation
  - Management dashboard

---

## How It Works

The server uses HTTP Basic Authentication to protect all routes except `/demo`.

**Authentication Flow:**
1. User visits `https://app.notap.io/`
2. Browser shows login popup
3. User enters username and password
4. If correct: Access granted to main app
5. If incorrect: Access denied

**Public Access:**
- `https://app.notap.io/demo` - No authentication required
- Demo works exactly as before

---

## Environment Variables

Set these in your deployment environment (Railway, Heroku, etc.):

```bash
# Required for authentication
ADMIN_USERNAME=your_username_here
ADMIN_PASSWORD=your_secure_password_here

# Optional - port (usually auto-set by hosting platform)
PORT=3000
```

---

## Railway Deployment Setup

### Step 1: Set Environment Variables

1. Go to Railway dashboard
2. Select your project
3. Click on the service (online-web)
4. Go to **Variables** tab
5. Add the following variables:

```
ADMIN_USERNAME = your_chosen_username
ADMIN_PASSWORD = your_strong_password
```

### Step 2: Generate Strong Credentials

**Username:**
- Don't use "admin", "user", or obvious names
- Example: `notap_admin_2026`

**Password:**
- Minimum 16 characters
- Mix of uppercase, lowercase, numbers, symbols
- Example: `Secure!P@ssw0rd#2026$NoTap`

**Generate secure password:**
```bash
# Linux/Mac
openssl rand -base64 24

# Or use a password manager (1Password, Bitwarden, etc.)
```

### Step 3: Deploy

After setting environment variables, Railway will automatically redeploy with authentication enabled.

---

## Local Development Setup

### Option 1: Using .env file

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and set your credentials:
   ```bash
   ADMIN_USERNAME=local_admin
   ADMIN_PASSWORD=local_password_for_testing
   ```

3. Install dependencies:
   ```bash
   npm install
   ```

4. Start server:
   ```bash
   npm start
   ```

5. Visit:
   - `http://localhost:3000/` - Requires auth
   - `http://localhost:3000/demo` - No auth required

### Option 2: Using environment variables directly

```bash
# Linux/Mac
ADMIN_USERNAME=test ADMIN_PASSWORD=test123 npm start

# Windows CMD
set ADMIN_USERNAME=test && set ADMIN_PASSWORD=test123 && npm start

# Windows PowerShell
$env:ADMIN_USERNAME="test"; $env:ADMIN_PASSWORD="test123"; npm start
```

---

## Default Credentials (Development Only)

If no environment variables are set, the app uses default credentials:

```
Username: admin
Password: changeme
```

⚠️ **WARNING:** Never use default credentials in production!

---

## Testing Authentication

### Test Protected Route
```bash
# Should return 401 Unauthorized
curl https://app.notap.io/

# With credentials (should return 200 OK)
curl -u username:password https://app.notap.io/
```

### Test Public Route
```bash
# Should return 200 OK without credentials
curl https://app.notap.io/demo
```

---

## Security Best Practices

✅ **DO:**
- Use strong, unique passwords
- Change credentials regularly
- Use a password manager
- Keep credentials in environment variables (never commit to git)
- Use HTTPS (already enabled on Railway)

❌ **DON'T:**
- Use default credentials in production
- Share credentials publicly
- Commit `.env` file to git (it's in `.gitignore`)
- Use simple passwords like "password123"
- Reuse passwords from other services

---

## Troubleshooting

### "Authentication Required" popup keeps appearing

**Cause:** Wrong username or password

**Solution:**
1. Check Railway environment variables
2. Verify exact spelling (case-sensitive)
3. Check for extra spaces in credentials
4. Redeploy after updating variables

### Demo not loading (asks for password)

**Cause:** Authentication middleware applied to demo route

**Solution:** Check `server.js` - demo routes should be BEFORE authentication middleware

### Static assets not loading (CSS/JS)

**Cause:** Static files protected by authentication

**Solution:** Static files are served before auth middleware, they should work. Check browser console for errors.

### Can't access main app even with correct credentials

**Cause:** Browser cached incorrect credentials

**Solution:**
1. Clear browser cache
2. Use Incognito/Private browsing
3. Try different browser

---

## How to Change Credentials

### Production (Railway)

1. Go to Railway dashboard → Variables
2. Update `ADMIN_USERNAME` and/or `ADMIN_PASSWORD`
3. Railway auto-redeploys
4. Use new credentials on next visit

### Local Development

1. Update `.env` file
2. Restart server (`npm start`)
3. Clear browser cache or use Incognito

---

## Removing Authentication (NOT RECOMMENDED)

If you want to make the main app public again:

1. Edit `server.js`
2. Comment out or remove the authentication middleware section
3. Redeploy

⚠️ **This will expose:** Developer API keys, enrollment data, user management

---

## Support

If you have issues with authentication:
1. Check Railway logs for error messages
2. Verify environment variables are set correctly
3. Test with `curl` to isolate browser issues
4. Clear browser cache and cookies

---

## Summary

- 🔒 Main app protected with Basic Auth
- ✅ Demo remains public at `/demo`
- 🔑 Set credentials via environment variables
- 🚀 Railway auto-deploys with auth enabled
- 🛡️ Use strong passwords for production
