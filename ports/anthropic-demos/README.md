# Anthropic Demo Ports

Java ports of the official [Anthropic Claude Agent SDK demos](https://github.com/anthropics/claude-agent-sdk-demos),
built against `io.github.markpollack:claude-code-sdk`.

**This is not part of the tutorial.** The 23 numbered modules in the repository root are
the teaching material: each is ~50–100 lines, introduces one concept, and builds on the
one before it. These four are the opposite — complete applications, ported to show the SDK
carrying a realistic workload, with no pedagogical sequence and no attempt to isolate a
single idea. They are kept here because they belong next to learning material rather than
inside the SDK library repository, and they are deliberately outside the tutorial reactor
so they never affect its module or test counts.

Read [`NOTICE.md`](NOTICE.md) before reusing anything from `research-agent`.

## The four ports

| Port | Upstream | Shape |
|---|---|---|
| `hello-world` | `hello-world` | Console; the three API styles side by side |
| `research-agent` | `research-agent` | Console; lead agent orchestrating researcher and report-writer subagents |
| `excel-demo` | `excel-demo` | Spring Boot + Vaadin; streaming spreadsheet assistant |
| `email-agent` | `email-agent` | Spring Boot + Vaadin; inbox triage over IMAP |

## Building

```bash
cd ports/anthropic-demos
../../mvnw clean package
```

Requires **Java 21+**, and a `claude` CLI on `PATH`, installed and authenticated, to run
any of them. `excel-demo` and `email-agent` additionally run a Vaadin frontend build, which
needs Node and network access on first build.

The SDK version is set once, in this directory's `pom.xml`, as
`claude-agent-sdk.version`; the two Spring Boot ports repeat it locally because they
inherit `spring-boot-starter-parent` instead.

> **Running any of these costs money.** Each starts a real Claude session.

## A note on their previous state

These four lived in the SDK repository's `examples/` and **none of them could be built**.
`hello-world` and `research-agent` inherited an `examples-parent` whose own parent was
pinned five versions behind and no longer resolved; all four took the SDK version from
`${project.version}`, which under `spring-boot-starter-parent` means the example's own
version, not the SDK's. They were never in any reactor, so no build ever caught it. The
move fixed both.
