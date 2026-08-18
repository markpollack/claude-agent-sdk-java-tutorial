# Attribution

This tutorial is adapted from multiple sources. We gratefully acknowledge:

## Kenneth Liao Tutorial
- **Source:** https://github.com/kenneth-liao/claude-agent-sdk-intro
- **Modules inspired by:** Message handling, options configuration, conversation loop, MCP, subagents
- **License:** none declared. The repository publishes no LICENSE file, and GitHub reports
  no license for it, so its own code is all rights reserved.
- **What that means here:** nothing from it was copied. It is a Python tutorial; these are
  Java modules covering the same SDK features, and no distinctive string, example, prompt or
  domain scenario from it appears anywhere in this repository — its telescope-shopping,
  YouTube-analyst, Playwright and product-search examples have no counterpart here. What
  carried over is which SDK capabilities are worth teaching and in what order, which is not
  something copyright protects. The acknowledgement below is courtesy, not a licence
  obligation, and no permission is required.

## Official Anthropic Documentation
- **Source:** https://docs.anthropic.com/en/docs/agent-sdk/
- **Adapted for:** Reference documentation, API patterns
- **License:** Anthropic's published documentation terms. Facts about an API — flag names,
  message shapes, option semantics — are not owned by their documentation; no documentation
  prose is reproduced here.

## Official Anthropic Demos
- **Source:** https://github.com/anthropics/claude-agent-sdk-demos
- **Adapted for:** Email Agent, Research Agent patterns
- **License:** none declared — verified 2026-08-18. The repository has no LICENSE file and
  GitHub reports no license, so it is all rights reserved. This entry previously said MIT,
  which was wrong.
- **What that means here:** this tutorial copies nothing from it. Its final research-agent
  project uses its own short inline prompts. Note that the **SDK repository's**
  `examples/research-agent` is a separate matter and does reuse prompt text from this
  source; that is tracked in the SDK's own review, not here.

## Wally869 Rust SDK
- **Source:** https://github.com/Wally869/claude_agent_sdk_rust
- **Adapted for:** Hook system patterns, permission callbacks, session resume/fork, interrupt handling
- **Useful as:** Reference for adapting Python patterns to typed language
- **License:** none declared — verified 2026-08-18. No LICENSE file; GitHub reports no
  license. This entry previously said MIT, which was wrong. Nothing was copied from it; it
  was read as a worked example of expressing the same SDK concepts in a statically typed
  language.

## Spring AI MCP Patterns
- **Source:** https://github.com/spring-projects/spring-ai
- **Used for:** Annotation-based MCP programming model (`@McpTool`, `@McpToolParam`)
- **License:** Apache 2.0

## Claude Agent SDK for Java
- **Source:** https://github.com/markpollack/claude-agent-sdk-java
- **Used for:** Java SDK patterns, existing examples as inspiration
- **License:** Apache 2.0 — verified. (This is the SDK this tutorial teaches. It was
  previously listed under the heading "Spring AI Community", a former organisation name.)

---

## Module-Specific Credits

There are none. This document is the complete attribution record for the repository. An
earlier version of this file stated that each module directory contains a `CREDITS.md`
file; no such file has ever existed in any module.

## Note on Adaptation

All modules contain new standalone code written specifically for this tutorial. The source
material listed above served as inspiration for patterns and concepts; code was not copied
verbatim but reimplemented in idiomatic Java following the SDK's conventions.

This was checked rather than asserted, on 2026-08-18: every distinctive string literal in
the Kenneth Liao tutorial was searched for across this repository's Java, Markdown and POM
files, with zero matches, and none of its example domains appear here. The licence status
of all six sources above was verified against the GitHub API on the same date.
