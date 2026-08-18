# NOTICE — third-party provenance

The applications in this directory are Java ports of demos published by Anthropic at:

> **https://github.com/anthropics/claude-agent-sdk-demos**

Verified 2026-08-18: that repository is public and actively maintained (last pushed
2026-03-13, not archived), and still ships all four upstream demos this directory ports —
`hello-world`, `research-agent`, `excel-demo` and `email-agent`.

## Licensing position — read this before reusing anything here

**The upstream repository declares no licence.** It contains no `LICENSE` file and the
GitHub API reports `license: null`. Absent a licence, its content is all rights reserved
by its authors. Anthropic publishing demos for SDK users to learn from carries an obvious
implied purpose, but an implication is not a grant.

**Some content here is not merely inspired by the upstream — it is copied from it.**
Measured line-for-line on 2026-08-18:

| File | Substantive lines | Verbatim from upstream |
|---|---|---|
| `research-agent/src/main/resources/prompts/lead_agent.txt` | 49 | **37 (75%)** — opening block byte-identical |
| `research-agent/src/main/resources/prompts/report_writer.txt` | 20 | 5 (25%) |
| `research-agent/src/main/resources/prompts/researcher.txt` | 29 | 4 (13%) |

Four subagent description strings in `research-agent/.../ResearchAgent.java` are likewise
verbatim. The `excel-demo` and `email-agent` system prompts share **no** lines with
upstream, and the Java application code throughout is an independent implementation.

Moving this directory out of the Claude Agent SDK repository removed a specific problem:
that repository's root Apache 2.0 `LICENSE` purported to license text it does not own.
This repository makes no licence claim over the material above, and none should be
inferred. **The underlying question — that upstream text is redistributed here without a
grant — is not resolved by the move.** It remains open, and the honest options are to
rewrite the affected prompts in original words, or to obtain terms from Anthropic.

## What is original

Everything else: the Java implementations, the Maven build, the Vaadin user interfaces in
`excel-demo` and `email-agent`, and the `excel-demo`/`email-agent` system prompts. Those
are covered by whatever licence this repository adopts.
