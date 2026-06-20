# Security Headers Policy

**Last Updated:** 2026-06-20
**Status:** ✅ Active enforcement on all HTTP endpoints

---

## Table of Contents

1. [Policy Overview](#1-policy-overview)
2. [Header Inventory by Service](#2-header-inventory-by-service)
3. [Content-Security-Policy by Service](#3-content-security-policy-by-service)
4. [Non-HTTP Services (Databases)](#4-non-http-services-databases)
5. [Enforcement & Verification](#5-enforcement--verification)
6. [Development Rules](#6-development-rules)
7. [Future Improvements](#7-future-improvements)

---

## 1. Policy Overview

### Why This Exists

Security headers are the first line of defense against:
- **XSS**: CSP prevents injection of unauthorized scripts
- **Clickjacking**: X-Frame-Options / frame-ancestors prevents UI overlay attacks
- **MIME confusion**: X-Content-Type-Options prevents MIME-type sniffing
- **Protocol downgrade**: HSTS forces HTTPS-only connections
- **Referrer leakage**: Referrer-Policy controls what URL data is sent cross-origin
- **API abuse**: Permissions-Policy disables unused browser capabilities
- **Cache-based attacks**: Cache-Control prevents stale/cross-user data exposure (see CACHE_SECURITY_AUDIT.md)

### Coverage

| Service | Subdomain | Headers | CSP | HSTS | HTTPS |
|---------|-----------|---------|-----|------|-------|
| **Backend API** | `api.notap.io` | ✅ Full helmet + extras | ✅ | ✅ | ✅ (Railway edge) |
| **Web Frontend** | `app.notap.io` | ✅ Full helmet | ✅ | ✅ | ✅ (Railway edge) |
| **PostgreSQL** | `db.notap.io` | N/A (TCP protocol) | N/A | N/A | ✅ (Railway edge) |
| **Redis** | `redis.notap.io` | N/A (TCP protocol) | N/A | N/A | ✅ (Railway edge) |

---

## 2. Header Inventory by Service

### 2A. Backend API (`api.notap.io`)

**Source:** `backend/server.js` (helmet) + `backend/middleware/security.js` (securityHeaders)

| Header | Value | Source | Rationale |
|--------|-------|--------|-----------|
| `Content-Security-Policy` | (see §3A) | `helmet` | Restricts script/style sources |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains; preload` | `helmet` | Forces HTTPS, preload ready |
| `X-Content-Type-Options` | `nosniff` | `helmet` + `security.js` | Prevents MIME sniffing |
| `X-Frame-Options` | `DENY` | `helmet` + `security.js` | Clickjacking prevention |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | `security.js` | Leaks origin only on cross-origin |
| `Permissions-Policy` | `geolocation=(), microphone=(), camera=()` | `security.js` | Disables unused browser APIs |
| `Cache-Control` | `no-store, private` | `security.js` | Prevents CDN/proxy caching of dynamic data |
| `Cross-Origin-Opener-Policy` | `same-origin` | `helmet` | Isolates browsing context |
| `Cross-Origin-Resource-Policy` | `same-origin` | `helmet` | Blocks cross-origin reads |
| `X-DNS-Prefetch-Control` | `off` | `helmet` | Prevents DNS prefetch leaks |
| `X-Download-Options` | `noopen` | `helmet` | Prevents IE download auto-open |
| `X-Permitted-Cross-Domain-Policies` | `none` | `helmet` | Blocks Flash cross-domain requests |
| `X-XSS-Protection` | `0` | `helmet` | Disables legacy XSS filter (XSS auditor is deprecated) |
| `Origin-Agent-Cluster` | `?1` | `helmet` | Opts into origin-keyed agent clustering |
| `Vary` | `Origin` | `cors` | CORS preflight caching |

### 2B. Web Frontend (`app.notap.io`)

**Source:** `online-web/server.js` (helmet + manual headers)

| Header | Value | Source | Rationale |
|--------|-------|--------|-----------|
| `Content-Security-Policy` | (see §3B) | `helmet` | Restricts script/style sources |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains; preload` | `helmet` | Forces HTTPS, preload ready |
| `X-Content-Type-Options` | `nosniff` | `helmet` | Prevents MIME sniffing |
| `X-Frame-Options` | `DENY` | `helmet` | Clickjacking prevention |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | `helmet` | Leaks origin only on cross-origin |
| `Permissions-Policy` | `geolocation=(), microphone=(), camera=()` | Manual | Disables unused browser APIs |
| `Cache-Control` | (varies) | `express.static` + `app.get('*')` | HTML: no-cache; assets: 1h |
| `Cross-Origin-Opener-Policy` | `same-origin` | `helmet` | Isolates browsing context |
| `Cross-Origin-Resource-Policy` | `same-origin` | `helmet` | Blocks cross-origin reads |
| `X-DNS-Prefetch-Control` | `off` | `helmet` | Prevents DNS prefetch leaks |
| `X-Download-Options` | `noopen` | `helmet` | Prevents IE download auto-open |
| `X-Permitted-Cross-Domain-Policies` | `none` | `helmet` | Blocks Flash cross-domain requests |
| `X-XSS-Protection` | `0` | `helmet` | Disables legacy XSS filter (XSS auditor is deprecated) |
| `Origin-Agent-Cluster` | `?1` | `helmet` | Opts into origin-keyed agent clustering |

### 2C. Static File Caching

| Path | Cache-Control | Rationale |
|------|---------------|-----------|
| `/` (SPA catch-all) | `no-cache, no-store, must-revalidate` | HTML must always be fresh |
| `/demo/*.html` | `no-cache, no-store, must-revalidate` | HTML must always be fresh |
| `/*.js` | `public, max-age=3600` | JS bundle — content changes per deploy |
| `/*.css` | `public, max-age=3600` | Styles — content changes per deploy |
| `/demo/*` (non-HTML) | `public, max-age=3600` | Assets |

**Note:** 1h TTL is conservative. The JS bundle (`online-web.js`) is NOT content-hashed (Phase 6 deferred), so longer TTL would cause stale-cache issues on deploy. Once content hashing is implemented, increase to `max-age=31536000, immutable`.

---

## 3. Content-Security-Policy by Service

### 3A. Backend API

```http
Content-Security-Policy: default-src 'self';
                         style-src 'self' https://cdn.jsdelivr.net;
                         script-src 'self' https://cdn.jsdelivr.net;
                         img-src 'self' data: https:;
                         connect-src 'self';
                         base-uri 'self';
                         font-src 'self' https: data:;
                         form-action 'self';
                         frame-ancestors 'self';
                         object-src 'none';
                         script-src-attr 'none';
                         upgrade-insecure-requests
```

**Source:** `backend/server.js:264-279`

**Directive rationale:**

| Directive | Allowed Sources | Reason |
|-----------|----------------|--------|
| `default-src` | `'self'` | Fallback for all resource types |
| `script-src` | `'self'`, `cdn.jsdelivr.net` | Chart.js for admin/merchant dashboards |
| `style-src` | `'self'`, `cdn.jsdelivr.net` | Chart.js may load CDN styles |
| `img-src` | `'self'`, `data:`, `https:` | Factor canvas images, inline data URIs |
| `connect-src` | `'self'` | API calls only to same origin |
| `font-src` | `'self'`, `https:`, `data:` | Font loading |
| `frame-ancestors` | `'self'` | Embedding only allowed on same origin |
| `base-uri` | `'self'` | Prevents base tag injection |
| `form-action` | `'self'` | Form submissions only to same origin |
| `object-src` | `'none'` | No plugins (Flash, Java, etc.) |
| `upgrade-insecure-requests` | (flag) | Auto-upgrade HTTP → HTTPS |

**`'unsafe-inline'` removed:** Was present in `style-src` pre-2026-01-20. Removed for CSP Level 3 compliance. All styles must be external or use nonces/hashes.

### 3B. Web Frontend

```http
Content-Security-Policy: default-src 'self';
                         script-src 'self' 'unsafe-inline';
                         style-src 'self' 'unsafe-inline';
                         img-src 'self' data: https:;
                         connect-src 'self' https://api.notap.io;
                         font-src 'self' data:;
                         frame-ancestors 'none';
                         form-action 'self';
                         base-uri 'self';
                         object-src 'none'
```

**Source:** `online-web/server.js`

**Directive rationale:**

| Directive | Allowed Sources | Reason |
|-----------|----------------|--------|
| `default-src` | `'self'` | Fallback for all resource types |
| `script-src` | `'self'`, `'unsafe-inline'` | Inline bootstrap script in index.html; Kotlin/JS bundle |
| `style-src` | `'self'`, `'unsafe-inline'` | Kotlin/JS may inject dynamic `<style>` elements |
| `img-src` | `'self'`, `data:`, `https:` | Factor canvas images, inline data URIs |
| `connect-src` | `'self'`, `https://api.notap.io` | SPA communicates with backend API |
| `font-src` | `'self'`, `data:` | Embedded fonts |
| `frame-ancestors` | `'none'` | No embedding permitted (stricter than API) |
| `base-uri` | `'self'` | Prevents base tag injection |
| `form-action` | `'self'` | Form submissions only to same origin |
| `object-src` | `'none'` | No plugins |

**`'unsafe-inline'` usage:** Required for the index.html bootstrap script block and Kotlin/JS runtime dynamic styles. If the bootstrap script is extracted to an external file and Kotlin/JS stops using inline styles, both `'unsafe-inline'` entries can be removed and replaced with hashes or nonces.

---

## 4. Non-HTTP Services (Databases)

### PostgreSQL (`db.notap.io`) and Redis (`redis.notap.io`)

These are **database protocol services** (PostgreSQL wire protocol, Redis RESP protocol), not HTTP services. They are accessed via Railway's internal networking:

| Security Layer | Implementation |
|----------------|----------------|
| **TLS termination** | Railway edge proxy — HTTPS at `*.notap.io` → internal TCP |
| **Internal network** | `*.railway.internal` hostnames — isolated from public internet |
| **Authentication** | PostgreSQL: password auth; Redis: AUTH with `REDIS_PASSWORD` |
| **HTTP headers** | N/A — these are not HTTP services (Railway returns 502 on HTTP requests) |

**Security considerations for database access:**
- Database credentials must never be logged or exposed in error responses
- Use Railway reference variables (`${{ Postgres.DATABASE_URL }}`) to avoid hardcoding
- Connection strings must use `railway.internal` hostnames, not public subdomains
- `REDIS_TLS_ENABLED` should be `true` in production (currently `false` — planned improvement)

---

## 5. Enforcement & Verification

### Automated Enforcement

- **Backend**: helmet middleware in `server.js` + `securityHeaders()` in `middleware/security.js`
- **Frontend**: helmet middleware in `server.js` (applied globally)
- **Pre-push agent**: `.claude/rules/GOVERNANCE.md` enforces security header compliance

### Manual Verification

```bash
# Check all headers on API
curl -sI -D - https://api.notap.io/health 2>&1 | grep -E "^(HTTP|content-security|strict-transport|x-content-type|x-frame|referrer|cache-control)"

# Check all headers on frontend
curl -sI -D - https://app.notap.io/ 2>&1 | grep -E "^(HTTP|content-security|strict-transport|x-content-type|x-frame|referrer|cache-control)"

# Quick audit: verify required headers present
curl -sk -D - https://api.notap.io/v1/nonce 2>&1 | grep -c "content-security-policy:"
# Expected: 1 (present)
curl -sk -D - https://app.notap.io/demo 2>&1 | grep -c "content-security-policy:"
# Expected: 1 (present)
```

### Expected Header Counts

| Service | Minimum Security Headers | Includes |
|---------|------------------------|----------|
| `api.notap.io` | 15+ | CSP, HSTS, XFO, XCTO, RP, PP, CC, COOP, CRRP, DNSPrefetch, DownloadOpt, PermittedCP, XXP, OAC, Vary |
| `app.notap.io` | 15+ | Same as above (excluding Vary) |

---

## 6. Development Rules

### Rule 1: Every new endpoint must inherit CSP

All routes registered in `server.js` go through helmet middleware. If a router is mounted BEFORE helmet, it will NOT have CSP. **Always mount helmet before routes:**

```javascript
// ✅ CORRECT — helmet before routes
app.use(helmet({ contentSecurityPolicy: { ... } }));
app.use('/v1/verification', verificationRouter);

// ❌ WRONG — no CSP on /v1/verification routes
app.use('/v1/verification', verificationRouter);
app.use(helmet({ contentSecurityPolicy: { ... } }));
```

### Rule 2: Never add `'unsafe-inline'` to backend CSP

The backend CSP was hardened to remove `'unsafe-inline'` from `style-src` (2026-01-20). If a new feature requires inline styles:
1. Use a CSP nonce (`'nonce-<random>'`)
2. Or extract to an external stylesheet
3. If unavoidable, document with a future removal date

### Rule 3: CDN sources require justification

Any `https://cdn.*` entry in CSP requires:
1. A comment in the helmet config explaining which file uses it
2. Documentation in `SECURITY_HEADERS_POLICY.md` §3
3. A plan to bundle the dependency and remove the CDN exception

### Rule 4: HSTS preload requires HTTPS everywhere

`includeSubDomains` + `preload` in HSTS means **every subdomain** of `notap.io` must support HTTPS. Before adding new subdomains, ensure they have TLS.

### Rule 5: `frame-ancestors` should be `'none'` where possible

- Backend API: `'self'` (admin dashboards may need iframing)
- Web frontend: `'none'` (no legitimate reason to iframe the SPA)

### Rule 6: Remove `x-powered-by` on all Express apps

Express enables `x-powered-by` by default. Every server must call `app.disable('x-powered-by')` to prevent server info leakage.

---

## 7. Future Improvements

| Priority | Improvement | Status | Notes |
|----------|-------------|--------|-------|
| P2 | Bundle Chart.js via npm | 🔲 Planned | Removes `cdn.jsdelivr.net` from backend CSP |
| P2 | Content-hash JS bundle filename | 🔲 Deferred | Allows `max-age=31536000, immutable` on JS |
| P2 | Replace `'unsafe-inline'` with nonce on frontend | 🔲 Planned | Extract bootstrap script to external file |
| P3 | Enable `REDIS_TLS_ENABLED` in production | 🔲 Planned | Redis traffic currently unencrypted |
| P3 | Trusted Types enforcement | 🔲 Research | DOM XSS prevention via Trusted Types API |

---

## Appendix A: Quick Reference Card

```text
HEADER                          API                     APP
───────────────────────────────────────────────────────────────
Content-Security-Policy         ✅ (no 'unsafe-inline')  ✅ (has 'unsafe-inline')
Strict-Transport-Security       ✅ 1y preload            ✅ 1y preload
X-Content-Type-Options          ✅ nosniff               ✅ nosniff
X-Frame-Options                 ✅ DENY                  ✅ DENY
Referrer-Policy                 ✅ strict-origin-when-   ✅ strict-origin-when-
Permissions-Policy              ✅ restricted            ✅ restricted
Cache-Control                   ✅ no-store, private     ✅ varies
Cross-Origin-Opener-Policy      ✅ same-origin           ✅ same-origin
Cross-Origin-Resource-Policy    ✅ same-origin           ✅ same-origin
X-DNS-Prefetch-Control          ✅ off                   ✅ off
X-Download-Options              ✅ noopen                ✅ noopen
X-Permitted-Cross-Domain-Pol.   ✅ none                  ✅ none
X-XSS-Protection                ✅ 0                     ✅ 0
Origin-Agent-Cluster            ✅ ?1                    ✅ ?1
x-powered-by                    ❌ disabled              ❌ disabled
```

## Appendix B: Verification Script

```bash
#!/bin/bash
# Quick header audit for all services
SERVICES="api.notap.io app.notap.io"
for s in $SERVICES; do
  echo "=== $s ==="
  curl -sk -D - "https://$s/" 2>&1 | grep -iE "^(HTTP|content-security|strict-transport|x-content-type|x-frame|referrer|permissions-policy|cache-control|cross-origin|access-control|x-dns|x-download|x-permitted|x-xss|origin-agent|x-powered-by)"
  echo ""
done
```
