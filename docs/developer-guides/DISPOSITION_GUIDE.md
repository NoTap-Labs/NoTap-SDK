# Paperclip Disposition Guide — ZeroPay (NoTap)

**Version:** 1.0
**Last Updated:** 2026-05-25
**Paperclip Version:** 2026.517.0

---

## What is a Disposition?

When a Paperclip agent finishes a run successfully but the system can't determine what to do next, the issue enters a "missing disposition" state (`successfulRunHandoff.state = required`). The human (board operator) or a supervising agent must provide a **disposition** — a decision on what happens next.

A disposition is simply: **update the issue status + add a comment with the convention**.

---

## The 5 Core Disposition Actions

These map to Paperclip's native issue statuses:

| Action | Paperclip Status | When to Use | Comment Pattern |
|--------|-----------------|-------------|----------------|
| **done** | `done` | Task completed successfully | `Done: <summary>. /gates:<tags>` |
| **cancelled** | `cancelled` | Task no longer needed | `Cancelled: <reason>` |
| **blocked** | `blocked` | Waiting on external dependency | `Blocked: <reason>. /blocker:<issue-id>` |
| **delegate** | `todo` (+ reassign) | Hand off to another agent | `Delegated to <agent>: <brief>` |
| **in_review** | `in_review` (+ reviewers) | Needs human/agent sign-off | `Ready for review by <reviewer>` |

### API Usage

```bash
# Mark done
curl -X PATCH http://127.0.0.1:3100/api/issues/{issueId} \
  -H "Content-Type: application/json" \
  -d '{"status": "done", "comment": "Done: Implemented PIN factor. /gate:tests /gate:docs /needs:security"}'

# Mark cancelled
curl -X PATCH http://127.0.0.1:3100/api/issues/{issueId} \
  -H "Content-Type: application/json" \
  -d '{"status": "cancelled", "comment": "Cancelled: No longer in scope."}'

# Mark blocked
curl -X PATCH http://127.0.0.1:3100/api/issues/{issueId} \
  -H "Content-Type: application/json" \
  -d '{"status": "blocked", "comment": "Blocked: Waiting on Redis upgrade. /blocker:INFRA-3"}'

# Delegate (reassign + status=todo)
curl -X PATCH http://127.0.0.1:3100/api/issues/{issueId} \
  -H "Content-Type: application/json" \
  -d '{"status": "todo", "assigneeAgentId": "{targetAgentId}", "comment": "Delegated to Security Auditor: Review SSRF implementation."}'
```

---

## Modifier Tags

Attach these after the status verb in comments to add context for downstream agents and the board:

### Gate Tags (blocking requirements before close)

| Tag | Meaning | Checked By |
|-----|---------|------------|
| `/gate:agent_checks` | Must pass `./scripts/agent @all` before closing | Sentry (pre-push) |
| `/gate:tests` | Tests must pass (`npm test`, `./gradlew :sdk:test`) | CI / Board |
| `/gate:docs` | task.md + planning.md must be updated | Sentry (pre-push) |
| `/gate:migration` | DB migration required | Board / Engineer |
| `/gate:env_sync` | Env vars need propagation to all env files | Sentry (pre-push) |
| `/gate:changelog` | Changelog must be updated | Board |

### Needs Tags (cross-agent review required)

| Tag | Reviewer | When |
|-----|----------|------|
| `/needs:security` | Security Auditor | New endpoint / crypto change |
| `/needs:compliance` | Compliance Officer | New data collection / regulatory change |
| `/needs:privacy` | DPO | New user data handling |
| `/needs:risk` | CRO | Major architectural change |
| `/needs:iso` | ISO/NIST Chief | Framework / policy change |
| `/needs:ceo` | CEO | Strategic decision needed |

### Blocker Tags

| Tag | Meaning |
|-----|---------|
| `/blocker:<issue-id>` | Links the blocking issue by identifier |
| `/blocker:external:<description>` | External dependency (non-Paperclip) |

---

## Per-Agent Disposition Guidance

### CEO
```
Done: Strategic plan for Q3 finalized. /needs:security /needs:compliance
```
```
Done: CEO test — model verified. System prompt needs tuning for instruction-following. /needs:ceo
```
```
Delegated to Software Engineer: Implement PIN factor. /gate:tests /gate:docs
```

### Software Engineer
```
Done: Implemented PIN factor with full test suite. 140/140 tests passing. /gate:tests /gate:docs /needs:security
```
```
Blocked: Redis dependency — waiting on DevOps to provision cluster. /blocker:INFRA-3
```
```
In review by Security Auditor: SSRF middleware audit needed before merge. /needs:security
```

### Security Auditor
```
Done: SSRF audit complete. 0 vulnerabilities found. All constant-time patterns verified.
```
```
Blocked: Need updated threat model document before completing audit.
```
```
Delegated to Software Engineer: Fix 2 timing windows found in replayProtection.js.
```

### DPO
```
Done: DPIA for biometric consent feature complete. All GDPR Art. 6 basis documented.
```
```
In review by Compliance Officer: DPIA needs PSD3 cross-reference.
```

### Compliance Officer
```
Done: PSD3 compliance check passed. All 23 pre-push compliance checks green.
```
```
Blocked: BIPA consent flow needs UI implementation before compliance sign-off. /blocker:ZER-15
```

### CRO
```
Done: Risk assessment for PSP parallel sessions complete. Residual risk: LOW.
```
```
In review by CEO: Risk assessment recommends delaying blockchain anchoring to Phase 5.
```

### ISO/NIST Chief
```
Done: ISO 27001 gap analysis updated. 3 new controls mapped.
```
```
Blocked: Need formal information security policy document before proceeding. /blocker:external:Legal review
```

---

## Lifecycle: How to Avoid "Missing Disposition"

```
                 ┌──────────────────────────────────────────┐
                 │  Agent completes run successfully         │
                 └──────────┬───────────────────────────────┘
                            │
                    ┌───────▼────────┐
                    │  Was there a   │
                    │ clear outcome? │───No──► "Missing Disposition" state
                    └───────┬────────┘        (recovery action created)
                            │ Yes
                    ┌───────▼────────┐
                    │  Set status +  │
                    │  add comment   │
                    │  with tags     │
                    └───────┬────────┘
                            │
              ┌─────────────┼────────────────┐
              ▼              ▼                 ▼
         done/         blocked/           delegate/
        cancelled     in_review          reassign
```

### As an Agent (to prevent disposition stalls):

1. **Always end with a concrete status**. Your final comment should include `status: "done"`, `"blocked"`, or `"in_review"`.
2. **If you can't decide**, leave a comment saying why and tag `/needs:ceo` for escalation.
3. **If delegating**, create subtasks via `POST /api/issues` with `parentId` set, then reassign.
4. **If blocked**, always include a reason and link the blocking issue via `/blocker:`.

### As a Board Operator (to resolve dispositions):

1. Open the issue in the Paperclip UI or use the API:
   ```bash
   curl -s http://127.0.0.1:3100/api/issues/{issueId} | python3 -m json.tool
   ```
2. Read the agent's last comment to understand what happened.
3. Choose a core action (done/cancelled/blocked/delegate/in_review).
4. Apply via PATCH with a comment using disposition conventions.

---

## Stalled Run Lifecycle

When a Paperclip agent starts a run but doesn't complete in a reasonable time, the issue enters a **stalled** state. This is common with CPU-only inference (qwen2.5-coder:3b at ~50-100ms/token) when the agent's context is large.

```
Stalled Run Detection:
         │
         ▼
  ┌──────────────────┐
  │ Run in_progress  │
  │ for > 10 min     │
  └────────┬─────────┘
           │
    ┌──────▼──────┐
    │ Check last  │
    │ heartbeat?  │
    └──────┬──────┘
           │
    ┌──────┴──────────────────┐
    ▼                         ▼
  Heartbeat                   No recent
  recent →                    heartbeat →
  agent is busy               agent may be
  but slow.                   dead/hung.
  Wait longer.                Force cancel.
           │                         │
     ┌─────▼─────┐           ┌───────▼────────┐
     │ Release   │           │ Cancel issue + │
     │ run, set  │           │ release run.   │
     │ blocked   │           │ /gate:watchdog │
     │ with      │           └────────────────┘
     │ /stalled  │
     └───────────┘
```

### Detection & Resolution

| Signal | Assessment | Action |
|--------|-----------|--------|
| Run > 10 min, last heartbeat < 2 min ago | Agent is slow but alive | Release run + set `blocked` with `/stalled:timeout` |
| Run > 10 min, last heartbeat > 5 min ago | Agent may be hung | Cancel issue + release run. Create retry if needed. |
| Run completed but no status set | Missing disposition | Apply disposition manually |
| Run accumulating log but not progressing | Stuck in loop | Cancel + `/stalled:loop` |

### Watchdog Script

Use the built-in watchdog to detect and auto-resolve stalled runs:

```bash
# Dry-run: list issues that would be affected
bash scripts/paperclip-watchdog.sh --dry-run

# Auto-resolve: cancel issues stalled > 15 min
bash scripts/paperclip-watchdog.sh --timeout 15

# Force-clean: release stuck runs without cancelling issues
bash scripts/paperclip-watchdog.sh --release-only
```

The watchdog script lives at `scripts/paperclip-watchdog.sh` and:
1. Lists all issues `in_progress` for longer than `--timeout` minutes
2. For each: checks the last heartbeat, logs the situation
3. If confirmed stalled: releases the run + cancels the issue with `/stalled:<reason>`
4. If heartbeat is recent: warns but does not cancel

### For Agent Instructions (preventing stalls)

Add this to each agent's instruction file under a "Time Management" section:

```
### Time Management
- You have 5 minutes of wall-clock time per task. If you can't finish, stop and say why.
- Always set a status when done. Never leave an issue `in_progress` without a result.
- If your model is slow, output a minimal response first, then elaborate if time permits.
- For ping/health-check tasks: respond immediately with just the status line.
```

---

## Paperclip Activity API (Monitoring)

Use the activity endpoint to monitor agent state without polling issues:

```bash
# Recent activity
curl -s "http://127.0.0.1:3100/api/companies/${COMPANY_ID}/activity?limit=10"

# Live runs (currently executing)
curl -s "http://127.0.0.1:3100/api/companies/${COMPANY_ID}/live-runs"

# Live runs with minimum count filter
curl -s "http://127.0.0.1:3100/api/companies/${COMPANY_ID}/live-runs?minCount=4"
```

---

## Model Management

The local agent stack runs on Ollama with `qwen2.5-coder:3b` (~3GB loaded, CPU-only). On a laptop CPU (Ryzen 5 7520U), each agent takes 5-10 minutes to complete a task because the full instruction context (~3-5KB) must be processed before the model can respond. Managing model state is essential for reliable operation.

### Quick Reference

```bash
# ── Model Lifecycle ────────────────────────────────────

# Check if model is loaded and ready (exit 0 = ready, 1 = loading, 2 = down)
bash scripts/paperclip-warmup.sh --status

# Warm up the model (loads into memory, sets keep_alive=-1)
bash scripts/paperclip-warmup.sh

# Unload the model from memory
ollama stop qwen2.5-coder:3b

# Check model state directly
curl -s http://127.0.0.1:11434/api/ps

# ── Pre-flight Check (before dispatching tasks) ───────

# Use in scripts or CI to gate task dispatch:
if bash scripts/paperclip-warmup.sh --check; then
    echo "Model ready — dispatch task"
    # create Paperclip issue here
else
    echo "Model not ready — warming up..."
    bash scripts/paperclip-warmup.sh
fi

# ── Keep-alive (optional) ──────────────────────────────

# Send a minimal request to keep model loaded for 30 more min
bash scripts/paperclip-warmup.sh --keep-alive

# Set up a cron job to keep model alive permanently
crontab -e
# Add: */25 * * * * /path/scripts/paperclip-warmup.sh --keep-alive > /dev/null 2>&1

# Remove keep-alive cron:
(crontab -l | grep -v paperclip-warmup) | crontab -

# ── Diagnostics ────────────────────────────────────────

# Is Ollama running?
curl -s --max-time 5 http://127.0.0.1:11434/api/tags > /dev/null && echo "Ollama OK" || echo "Ollama down"

# What is the model doing?
ps aux | grep "ollama runner" | grep -v grep

# Is Paperclip receiving agent heartbeats?
curl -s http://127.0.0.1:3100/api/companies/YOUR_COMPANY_ID/agents | python3 -c "import json,sys; [print(f'{a[\"name\"]:25s} status={a[\"status\"]:10s} last_hb={a.get(\"lastHeartbeatAt\",\"\")[:19]}') for a in json.load(sys.stdin)]"

# Are agent runs making progress? (check log offsets)
journalctl --user -u paperclip --no-pager -n 10 | grep "GET /heartbeat-runs/" | grep -oP 'offset=\K[0-9]+' | sort -n | tail -3
```

### Why Agents Are Slow

| Factor | Typical Value | Impact |
|--------|--------------|--------|
| Model | qwen2.5-coder:3b (3.1B params, Q4_K_M) | ~3GB RAM when loaded |
| Hardware | 4-core CPU (no GPU) | ~2-5 tok/s inference |
| Context per agent | ~3-5KB instructions + history | 1000-2000 prompt tokens |
| First token (cold model) | ~3m40s | Model loads from disk (3GB file) |
| First token (warm model) | ~30-60s | Full context processing |
| Completion (simple task) | 5-10 min per agent | 7 agents × concurrent = queue |

The model must process the entire agent instruction set before generating any output. Reducing instruction size improves speed, but the fundamental bottleneck is CPU-only inference.

### When to Warm vs. When to Stop

| Scenario | Action |
|----------|--------|
| About to create a Paperclip task | Run `bash scripts/paperclip-warmup.sh` first |
| Frequent tasks throughout the day | Keep model loaded (use keep-alive cron) |
| Done working for the day | `ollama stop qwen2.5-coder:3b` |
| Debugging agent issues | Check model status first — tasks fail silently if model is cold |
| Seeing heartbeat reaper warnings | Model is likely cold — warm it up |

---

## Related

- `documentation/04-architecture/PAPERCLIP_ORCHESTRATION.md` — Paperclip setup and agent roles
- `documentation/10-internal/AGENT_KNOWLEDGE_BASE.md` — agent domain knowledge
- `documentation/10-internal/AGENTS.md` — development guidelines
- `CLAUDE.md` — project governance
- `scripts/paperclip-watchdog.sh` — Watchdog script for stalled run detection
- `scripts/paperclip-warmup.sh` — Model pre-flight check and warm-up
