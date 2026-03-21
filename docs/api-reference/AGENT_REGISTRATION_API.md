# Agent Registration API

> Mount Path: `/v1/developer/agents`
> Auth: Developer JWT (all endpoints)
> Rate Limiting: Global + replay protection

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Register new agent |
| GET | `/` | List developer's agents |
| GET | `/:agent_id` | Get agent details |
| PUT | `/:agent_id` | Update agent configuration |
| DELETE | `/:agent_id` | Deactivate agent (soft delete) |
| POST | `/:agent_id/rotate-key` | Rotate agent API key |
| GET | `/:agent_id/usage` | Get agent usage statistics |

## POST /

Register a new agent with callback URL and permissions.

**Request:**
```json
{
  "name": "Payment Agent",
  "callback_url_default": "https://agent.example.com/callback",
  "permissions": ["agent:initiate", "agent:read"]
}
```

**Response (201):**
```json
{
  "success": true,
  "agent_id": "...",
  "api_key": "ak_..."
}
```

## Security

- SSRF validation on `callback_url_default`
- Developer scoped (can only access own agents)
- Soft delete on deactivation (revokes API key)
- Key rotation creates new key, invalidates old
- Audit logging for all mutations
