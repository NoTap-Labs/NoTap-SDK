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

## Related

- `documentation/04-architecture/PAPERCLIP_ORCHESTRATION.md` — Paperclip setup and agent roles
- `documentation/10-internal/AGENT_KNOWLEDGE_BASE.md` — agent domain knowledge
- `documentation/10-internal/AGENTS.md` — development guidelines
- `CLAUDE.md` — project governance
