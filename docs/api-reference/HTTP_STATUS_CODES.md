# HTTP Status Codes Reference

This document explains the HTTP status codes returned by the NoTap API.

## Success Codes (2xx)

| Code | Name | Description | Example |
|------|------|-------------|---------|
| **200** | OK | Request succeeded | `{"success": true, "data": {...}}` |
| **201** | Created | Resource created | After enrollment, webhook registration |
| **204** | No Content | Success, no body | After deletion |

## Client Error Codes (4xx)

| Code | Name | Description | Common Causes |
|------|------|-------------|---------------|
| **400** | Bad Request | Invalid request format | Missing required fields, wrong data types, invalid UUID format |
| **401** | Unauthorized | No authentication provided | Missing API key, missing Bearer token, expired token |
| **403** | Forbidden | Authentication invalid/insufficient | Wrong API key, insufficient permissions, account suspended |
| **404** | Not Found | Resource or endpoint doesn't exist | Wrong URL path, deleted enrollment, non-existent UUID |
| **409** | Conflict | Resource already exists | Duplicate enrollment, alias already taken |
| **422** | Unprocessable Entity | Validation failed | Factor count mismatch, invalid factor data |
| **429** | Too Many Requests | Rate limit exceeded | Too many API calls, brute force protection triggered |

## Server Error Codes (5xx)

| Code | Name | Description | Common Causes |
|------|------|-------------|---------------|
| **500** | Internal Server Error | Unexpected error | Bug in code, unhandled exception |
| **502** | Bad Gateway | Upstream service error | Redis/PostgreSQL connection issues |
| **503** | Service Unavailable | Service temporarily down | Database disabled, maintenance mode |
| **504** | Gateway Timeout | Request timed out | Slow database query, blockchain RPC timeout |

## Error Response Format

All errors follow this format:

```json
{
  "success": false,
  "error": "Short error description",
  "message": "Detailed explanation (optional)",
  "code": "ERROR_CODE (optional)"
}
```

## Common Error Scenarios

### 400 Bad Request

```json
// Missing required fields
{
  "success": false,
  "error": "Missing required fields: user_uuid, factors, device_id"
}

// Invalid UUID format
{
  "success": false,
  "error": "Invalid UUID format"
}

// Invalid factor data
{
  "success": false,
  "error": "Factor count mismatch",
  "message": "You must complete all 3 enrolled factors"
}
```

### 401 Unauthorized

```json
// Missing API key
{
  "success": false,
  "error": "Authentication required"
}

// Missing Bearer token
{
  "success": false,
  "error": "UNAUTHORIZED",
  "message": "Missing or invalid Authorization header. Expected: Bearer psp_..."
}
```

### 403 Forbidden

```json
// Invalid API key
{
  "success": false,
  "error": "Unauthorized. Valid API key required."
}

// Admin-only endpoint
{
  "success": false,
  "error": "Unauthorized. Valid admin API key required."
}
```

### 404 Not Found

```json
// Enrollment not found
{
  "success": false,
  "error": "Enrollment not found or expired"
}

// Wrong endpoint path
{
  "error": "Not found",
  "path": "/v1/wrong/path"
}
```

### 429 Too Many Requests

```json
{
  "success": false,
  "error": "Too many requests, please try again later",
  "retry_after": 60
}
```

### 503 Service Unavailable

```json
{
  "success": false,
  "error": "Service temporarily unavailable - database not configured",
  "code": "DATABASE_DISABLED"
}
```

## Best Practices for API Consumers

1. **Always check `success` field** - Don't rely solely on HTTP status code
2. **Handle rate limits gracefully** - Implement exponential backoff on 429
3. **Log error details** - Include `error` and `message` in logs
4. **Retry on 5xx errors** - These are usually temporary
5. **Don't retry on 4xx errors** - These indicate client-side issues

## Rate Limits by Endpoint

| Endpoint Category | Rate Limit | Window |
|-------------------|------------|--------|
| Enrollment | 10 requests | 15 minutes |
| Verification | 30 requests | 1 minute |
| Admin | 100 requests | 1 minute |
| Webhook | 60 requests | 1 minute |
| Crypto payments | 5 requests | 1 minute |

---

**Last Updated**: 2025-12-27
