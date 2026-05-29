# Paperclip AI Orchestration — ZeroPay (NoTap)

**Status:** Operational
**Version:** 2026.517.0
**Setup Date:** 2026-05-24
**Last Updated:** 2026-05-25 (Model: gemma4:e4b → qwen2.5-coder:3b)

## Architecture

Paperclip is an open-source agent orchestration platform running locally with zero internet dependency. It manages 7 AI agents that collaborate using the OpenClaw gateway, which routes all inference through Ollama running Qwen 2.5 Coder 3B (coding-specialized, fast CPU inference).

```
┌─────────────────────────────────────────────────────────────────┐
│                      Paperclip Server                           │
│                   http://127.0.0.1:3100                          │
│                    embedded PostgreSQL                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  CEO ─┬── Software Engineer                                      │
│       ├── Security Auditor                                        │
│       ├── DPO (Data Protection Officer)                           │
│       ├── Compliance Officer                                      │
│       ├── CRO (Chief Risk Officer)                                │
│       └── ISO/NIST/BP Chief Officer                               │
│                                                                   │
│  All agents use openclaw_gateway adapter                         │
│  Session strategy: issue (per-task isolation)                    │
│  Heartbeat: enabled (30s interval)                               │
└─────────────────────────────────────────────────────────────────┘
                              │ ws://127.0.0.1:18789
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     OpenClaw Gateway                             │
│                   http://127.0.0.1:18789                         │
│                    loopback-only, token auth                     │
│                    rate limited, Ed25519 device pairing          │
├─────────────────────────────────────────────────────────────────┤
│                              │                                   │
│              OpenAI-compatible API (v1/chat/completions)         │
│                              │                                   │
└──────────────────────────────┼──────────────────────────────────┘
                               │ http://127.0.0.1:11434
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Ollama                                      │
│                   http://127.0.0.1:11434                         │
│                    Model: qwen2.5-coder:3b (3B Q4_K_M)          │
│                    Hardware: local CPU (coding-optimized)        │
└─────────────────────────────────────────────────────────────────┘
```

## Services

| Service | Port | Access | Auth |
|---------|------|--------|------|
| Paperclip | 3100 | loopback | JWT (agent) |
| OpenClaw Gateway | 18789 | loopback | Token + Ed25519 |
| Ollama | 11434 | loopback | None (internal) |

### Systemd Units (User-Level)

```bash
# Paperclip
systemctl --user status paperclip.service
# Config: ~/.config/systemd/user/paperclip.service

# OpenClaw Gateway
systemctl --user status openclaw-gateway.service
# Config: ~/.config/systemd/user/openclaw-gateway.service
```

## Configuration Files

| File | Purpose |
|------|---------|
| `~/.paperclip/instances/default/config.json` | Paperclip server config |
| `~/.paperclip/instances/default/.env` | Agent JWT secret |
| `~/.openclaw/openclaw.json` | OpenClaw gateway config |
| `~/.openclaw/.env` | Gateway auth token |

## Agent Adapter: OpenClaw Gateway

All 7 agents use `openclaw_gateway` adapter which connects to OpenClaw over WebSocket. Key config:

- **URL:** `ws://127.0.0.1:18789`
- **Model:** `qwen2.5-coder:3b`
- **Auto-pair:** enabled (Ed25519 keypair on first connect)
- **Session strategy:** `issue` (each task gets isolated session)
- **Timeout:** 600s (CPU inference, increased from 300s during model migration)
- **Heartbeat:** enabled at 30s intervals

## Agent Roles & Responsibilities

### 1. CEO (Chief Executive Officer)
- **Adapter:** openclaw_gateway
- **Capabilities:** Strategy, oversight, decision-making, stakeholder communication
- **Reports to:** none (top of org)
- **Heartbeat:** 3 concurrent runs
- **Role:** Strategic direction, priority decisions, inter-agent coordination, escalation point

### 2. Software Engineer
- **Adapter:** openclaw_gateway
- **Capabilities:** Full-stack development, architecture, code review, testing, deployment
- **Reports to:** CEO
- **Heartbeat:** 5 concurrent runs
- **Timeout:** 600s (longer for coding tasks)
- **Role:** Implements features, fixes bugs, writes tests, performs code reviews

### 3. Security Auditor
- **Adapter:** openclaw_gateway
- **Capabilities:** Vulnerability assessment, penetration testing, compliance auditing, threat modeling
- **Reports to:** CEO
- **Heartbeat:** 3 concurrent runs
- **Timeout:** 600s
- **Role:** Security reviews, vulnerability scanning, penetration testing, threat modeling

### 4. DPO (Data Protection Officer)
- **Adapter:** openclaw_gateway
- **Capabilities:** Data privacy, GDPR compliance, LGPD compliance, data protection impact assessment, privacy policies
- **Reports to:** CEO
- **Heartbeat:** 3 concurrent runs
- **Role:** Privacy impact assessments, data retention audits, consent management review, privacy documentation

### 5. Compliance Officer
- **Adapter:** openclaw_gateway
- **Capabilities:** Regulatory compliance, policy enforcement, audit management, risk assessment
- **Reports to:** CEO
- **Heartbeat:** 3 concurrent runs
- **Role:** PSD3/SCA compliance, regulatory audits, policy enforcement, compliance gap analysis

### 6. CRO (Chief Risk Officer)
- **Adapter:** openclaw_gateway
- **Capabilities:** Risk management, financial risk, operational risk, risk mitigation strategies, business continuity
- **Reports to:** CEO
- **Heartbeat:** 3 concurrent runs
- **Role:** Risk assessments, threat scenario analysis, business continuity planning, risk register maintenance

### 7. ISO/NIST/BP Chief Officer
- **Adapter:** openclaw_gateway
- **Capabilities:** ISO 27001, NIST framework, SOC 2, best practices, security frameworks, process optimization, quality management
- **Reports to:** CEO
- **Heartbeat:** 3 concurrent runs
- **Role:** ISO 27001 readiness, NIST CSF mapping, SOC 2 compliance, security framework alignment, process optimization, quality management

## Model Migration: gemma4:e4b → qwen2.5-coder:3b (2026-05-25)

**Why:** Gemma 4 4B was too slow on CPU-only inference (9.6 GB, 5+ minute timeouts). Qwen 2.5 Coder is coding-specialized and 5x faster.

| Model | Size | Speed | Use Case |
|-------|------|-------|----------|
| `gemma4:e4b` (old) | 9.6 GB | 300s+ timeout | General purpose, too slow for CPU |
| `qwen2.5-coder:3b` (new) | 1.9 GB | ~1s per inference | Coding-specialized, fast CPU inference |
| `llama3.2:3b` (available) | ~2 GB | similar | Tool-use optimized |

## Cold Start & Model Warm-Up

The Qwen 2.5 Coder 3B model (1.9 GB weights) loads in seconds on CPU. Cold start is essentially instant compared to the previous 9.6 GB Gemma model.

### How It Works

1. **OLLAMA_KEEP_ALIVE=30m** — After the last inference, Ollama keeps the model cached in RAM for 30 minutes. If no request arrives within that window, the model is automatically unloaded (RAM freed).
2. **Warm-up script (`paperclip-warmup`)** — Sends a dummy inference request to Ollama, forcing the model to load.
3. **Agent adapter timeout (600s)** — All agents now have a 600-second adapter timeout (was 300s) to accommodate CPU inference.

### Workflow

```
┌─────────────────────────────────────────────────────────────────┐
│  BEFORE agent session:                                           │
│                                                                   │
│  paperclip-warmup              ─►  Ollama loads qwen2.5 (~5 sec) │
│                                                                   │
│  ─── then ───                                                     │
│                                                                   │
│  paperclipai heartbeat run ...  ─►  Agent works (instant)         │
│                                                                   │
│  ─── 30 min idle ───                                              │
│                                                                   │
│  Model auto-unloads             ─►  RAM freed (~2 GB)             │
└─────────────────────────────────────────────────────────────────┘
```

### Warm-Up Script

Location: `~/.paperclip/scripts/warmup-model.sh`
Symlink: `~/.local/bin/paperclip-warmup`

```bash
# Warm up the model (default: qwen2.5-coder:3b)
paperclip-warmup

# Optionally specify a different model
paperclip-warmup llama3.2:3b
```

The script:
1. Verifies Ollama is running
2. Sends a generate request with `keep_alive: "30m"` and waits up to 600s
3. If the generate times out but the model loaded, it detects this via `ollama ps`
4. Does a quick verification inference to confirm the model responds

### Cold Start Troubleshooting

```bash
# Check if model is already loaded
ollama ps

# If warmup hangs or fails
journalctl -u ollama -n 20 --no-pager

# Manually load model with verbose output
curl -v -X POST http://127.0.0.1:11434/api/generate \
  -d '{"model": "qwen2.5-coder:3b", "prompt": "hello", "stream": false}' \
  --max-time 600

# After warmup, verify with a quick inference
timeout 15 curl -s -X POST http://127.0.0.1:11434/api/generate \
  -d '{"model": "qwen2.5-coder:3b", "prompt": "hi", "stream": false}' | python3 -c "import json,sys; d=json.load(sys.stdin); print(d.get('response',''))"
```

### Systemd Override (OLLAMA_KEEP_ALIVE)

The 30-minute keep-alive is configured via a systemd drop-in:

```
/etc/systemd/system/ollama.service.d/keep-alive.conf
```

```ini
[Service]
Environment="OLLAMA_KEEP_ALIVE=30m"
```

To change the idle timeout, edit the drop-in and reload:
```bash
sudo systemctl edit ollama.service
# Then set Environment="OLLAMA_KEEP_ALIVE=<new-value>"
sudo systemctl daemon-reload && sudo systemctl restart ollama
```

## Compliance Requirements (Non-Negotiable)

> **Critical:** Paperclip orchestration for ZeroPay/NoTap MUST use local models only. No cloud inference (Anthropic, OpenAI, etc.) is permitted for agent orchestration due to fintech/payment SDK data privacy requirements.

- **Sensitive data:** Payment credentials, PII, authentication factors must never leave the device
- **Local inference only:** Ollama with local models (`qwen2.5-coder:3b`, `llama3.2:3b`)
- **Exception:** Cloud models may be used for isolated code generation tasks only if NO sensitive data is included in the prompt (agent orchestration itself must remain local)

## Modular Provider Configuration

OpenClaw gateway is configured for modular multi-provider switching. Future Anthropic integration is pre-configured but disabled by default.

| Provider | Models | Status | Use Case |
|----------|--------|--------|----------|
| `ollama` | `qwen2.5-coder:3b` | **Active (Primary)** | Agent orchestration, coding tasks |
| `ollama` | `llama3.2:3b` | Available (fallback) | Tool-use optimized tasks |
| `anthropic` | `claude-*-*` | Pre-configured, disabled | Future cloud option (compliance-restricted) |
| `openai` | `gpt-*-*` | Pre-configured, disabled | Future cloud option (compliance-restricted) |

**Config location:** `~/.openclaw/openclaw.json`
**Env vars:** `~/.openclaw/.env` (API key placeholders commented out)

## Security Properties

- **Loopback-only:** All services bind to 127.0.0.1 only — no network exposure
- **Token auth:** OpenClaw gateway requires token for all API access
- **Ed25519 pairing:** Agent-to-gateway connections use cryptographic device identity
- **Rate limiting:** OpenClaw configured with 5 attempts/60s window, 10min lockout
- **No telemetry:** Paperclip telemetry is disabled in config
- **Zero internet dependency:** All model inference is local via Ollama
- **Compliance-first architecture:** Local inference mandate for fintech/payment SDK privacy

## API Access

```bash
# Paperclip API (REST)
curl http://127.0.0.1:3100/api/companies
curl http://127.0.0.1:3100/api/companies/{id}/agents
curl http://127.0.0.1:3100/api/agents/{id}

# Paperclip UI
# Open http://127.0.0.1:3100 in a browser
```

## Troubleshooting

### Paperclip won't start
```bash
journalctl --user -u paperclip.service --no-pager -n 50
```

### Port conflict
```bash
lsof -i :3100
lsof -i :18789
lsof -i :11434
```

### Agent not responding
```bash
# Check Paperclip health
curl http://127.0.0.1:3100/api/health

# Check agent list
curl http://127.0.0.1:3100/api/companies/{companyId}/agents | python3 -m json.tool
```

### Re-pair OpenClaw devices
```bash
openclaw devices list
openclaw devices approve --latest
```
