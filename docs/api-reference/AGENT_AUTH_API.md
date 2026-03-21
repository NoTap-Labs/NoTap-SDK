# Agent Authentication API

> Mount Path: `/v1/agent/auth`
> Auth: Agent API Key (via `authenticateAgent` middleware)
> Rate Limiting: Global + replay protection

## Endpoints

| Method | Path | Description | Permission |
|--------|------|-------------|------------|
| POST | `/initiate` | Start authentication session | `agent:initiate` |
| GET | `/status/:session_id` | Check session status | `agent:read` |
| POST | `/cancel/:session_id` | Cancel session | `agent:cancel` |
| GET | `/enrollment/:identifier` | Check enrollment status | `agent:read` |

## POST /initiate

Start an agent-initiated authentication session for a user.

**Request:**
```json
{
  "user_identifier": "abc-123-def",
  "callback_url": "https://agent.example.com/callback",
  "amount": 25.00,
  "currency": "USD",
  "metadata": {}
}
```

**Response (201):**
```json
{
  "success": true,
  "session_id": "sess_...",
  "status": "pending",
  "expires_at": "2026-03-21T12:00:00Z"
}
```

## GET /status/:session_id

Returns session status. Auth token is only returned when status is `verified` — never leaked to agents otherwise.

**Response (200):**
```json
{
  "success": true,
  "status": "pending|verified|cancelled|expired",
  "seal_id": "seal_..."
}
```

## POST /cancel/:session_id

Cancel an active session. Constant-time comparison used for session ownership verification.

## GET /enrollment/:identifier

Check if a user is enrolled (privacy-safe). Optional `?extended=true` requires `agent:read_extended` permission.

## Security

- SSRF validation on callback URLs
- Constant-time session ownership checks
- Sessions stored in Redis with TTL
- Async callback notifications (non-blocking)
- Audit logging for all operations
