# Research Agents — Architecture & Containment

**Last Updated:** 2026-05-26
**Status:** Operational

## Overview

Two research agents augment the Paperclip team with internet-based competitive intelligence and technology scouting. They are **deliberately isolated** from the core Paperclip instance to prevent any attack vector from internet-sourced content.

### The Two Agents

| Agent | Responsibility |
|-------|---------------|
| **Market Research Officer (MRO)** | Competitive landscape, market trends, pricing, funding, regulatory movements |
| **Technology Research Officer (TRO)** | ZK proofs, crypto tools, blockchain identity, auth standards, security tools, agentic frameworks |

### Key Constraint

**No internet communication to the Paperclip core team.** Research agents run as standalone Node.js processes. Their output goes through an audited bridge before the Paperclip CEO can read it. There is no mechanism for research agents to create issues, send messages, or modify anything in the core instance.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│  INTERNET (untrusted)                                                    │
│                                                                          │
│  Search engines, docs sites, GitHub, whitepapers, blogs                  │
└─────────────────────────┬───────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  RESEARCH SANDBOX (separate process)                                     │
│                                                                          │
│  Agents: MRO (mro.js) / TRO (tro.js)                                    │
│  Model: big-pickle via Opencode Zen (https://opencode.ai/zen/v1)        │
│  Runtime: Node.js 18+, standalone process                                │
│  Tools: web_search (DuckDuckGo / Google / Bing), web_fetch (HTTP GET)   │
│                                                                          │
│  ╔══ CONTAINMENT BOUNDARY ═══════════════════════════════════════════╗   │
│  ║  - No connection to Paperclip API                                   ║   │
│  ║  - No access to project secrets or credentials                      ║   │
│  ║  - Receives only public product context (no internal details)        ║   │
│  ║  - No ability to create tasks, issues, or messages                   ║   │
│  ╚══════════════════════════════════════════════════════════════════════╝   │
└─────────────────────────┬───────────────────────────────────────────────┘
                          │ writes structured JSON report
                          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  AUDITED BRIDGE (bridge.js)                                              │
│                                                                          │
│  1. JSON Schema validation                                               │
│  2. Content sanitization (strip scripts, JS URLs, eval, etc.)           │
│  3. SHA-256 hash computation for integrity                               │
│  4. Append-only audit log entry                                          │
│  5. Rejected reports → quarantine directory                              │
│  6. Valid reports → reports/research/ with AUDITED status                │
│                                                                          │
│  ╔══ ONE-WAY GATE ═══════════════════════════════════════════════════╗   │
│  ║  - Reports flow: research → bridge → storage (one direction)       ║   │
│  ║  - No channel for content to flow back into the research agent      ║   │
│  ║  - Hash chain ensures tamper detection                              ║   │
│  ╚═══════════════════════════════════════════════════════════════════╝   │
└─────────────────────────┬───────────────────────────────────────────────┘
                          │ CEO reads audited report
                          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  CORE PAPERCLIP (127.0.0.1:3100, fully isolated)                         │
│                                                                          │
│  7 agents: CEO, Engineer, Security, DPO, Compliance, CRO, ISO/NIST      │
│                                                                          │
│  Workflow:                                                               │
│  1. CEO reads audited report from reports/research/                      │
│  2. CEO decides which findings warrant action                            │
│  3. CEO creates Paperclip issues for Engineer, Security, etc.            │
│  4. Never automated — always human (or CEO) gated                       │
│                                                                          │
│  ╔══ AIR GAP ════════════════════════════════════════════════════════╗   │
│  ║  - Zero internet access                                              ║   │
│  ║  - No direct connection to research agents                           ║   │
│  ║  - Loopback-only services (Paperclip + OpenClaw + Ollama)            ║   │
│  ╚═══════════════════════════════════════════════════════════════════╝   │
└─────────────────────────────────────────────────────────────────────────┘
```

## Containment Layers

| Layer | What It Prevents | Implementation |
|-------|-----------------|----------------|
| **Process isolation** | No WS/API access to Paperclip | Separate Node.js process, not a Paperclip agent |
| **No secrets** | Data leakage of credentials | System prompt contains only public product context |
| **Schema validation** | Malformed or unexpected data | JSON Schema enforced before any storage |
| **Content sanitization** | XSS, injection via report fields | Strips script tags, JS URLs, eval(), on* handlers |
| **Hash chain** | Tampering with stored reports | SHA-256 hash of normalized report; verify via --check-hash |
| **Audit log** | Undetected access/modification | Append-only .audit.log with timestamps |
| **Manual CEO gate** | Automated action on untrusted data | CEO must read and decide before any Paperclip issue |

## Threat Model

| Attack Vector | Mitigation |
|--------------|-----------|
| **Prompt injection** — malicious webpage tricks agent into ignoring system prompt | Agent outputs JSON only. Bridge rejects any non-JSON content. No auto-action. |
| **Command injection** — web content contains shell commands | Bridge strips `eval(`, `require(`, `import`. No shell execution in scripts. |
| **SSRF via research** — agent fetches internal URLs | `web_fetch` blocks 127.0.0.1, localhost, 0.0.0.0. Only https/http allowed. |
| **Hallucinated sources** — agent invents URLs or citations | Sources required on every finding. Bridge validates field lengths, no URLs → possible confidence penalty. |
| **Malicious recommendations** — compromised tool recommended | CEO reviews all recommendations. No auto-import. Findings include confidence + source quality. |
| **Data exfiltration** — agent sends project data to external servers | Agents have NO access to project secrets, keys, or internal architecture. Only public context. |

## Report Lifecycle

```
1. User request (topic) ──►
2. MRO/TRO research (web search + fetch) ──►
3. Raw JSON output ──►
4. Bridge validation (schema + sanitize + hash) ──►
   ├── VALID: reports/research/{agent}-{type}-{ts}.json [AUDITED]
   └── INVALID: reports/research/.quarantine/{file} [REJECTED]
5. CEO reviews audited reports manually ──►
   ├── Actionable: CEO creates Paperclip issue
   └── Not relevant: archived
```

## File Locations

| File | Purpose |
|------|---------|
| `scripts/research-agents/mro.js` | MRO agent implementation |
| `scripts/research-agents/mro.sh` | MRO shell launcher |
| `scripts/research-agents/tro.js` | TRO agent implementation |
| `scripts/research-agents/tro.sh` | TRO shell launcher |
| `scripts/research-agents/bridge.js` | Report validation + audit |
| `scripts/research-agents/report-schema.json` | JSON Schema for validation |
| `scripts/research-agents/lib/api.js` | Zen API tool-calling loop |
| `scripts/research-agents/lib/search.js` | Web search + fetch implementations |
| `scripts/research-agents/README.md` | Usage guide |
| `reports/research/` | Audited report storage |
| `reports/research/.quarantine/` | Rejected reports |
| `reports/research/.audit.log` | Append-only integrity log |

## Dependencies

- **Runtime:** Node.js 18+ (built-in `fetch`, no npm packages required)
- **Model:** Big Pickle via Opencode Zen (free, https://opencode.ai/zen)
- **Search:** DuckDuckGo lite (default, no API key) or Google/Bing/Serper (configurable)
- **No Paperclip dependency:** Agents run independently of Paperclip server

## Related

- `documentation/04-architecture/PAPERCLIP_ORCHESTRATION.md` — Core agent orchestration
- `documentation/10-internal/AGENT_KNOWLEDGE_BASE.md` — Agent domain knowledge
- `documentation/03-developer-guides/DISPOSITION_GUIDE.md` — Paperclip disposition guide
