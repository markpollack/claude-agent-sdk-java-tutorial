# Claude Agent SDK Java Tutorial

Progressive tutorial modules for learning the Claude Agent SDK Java.

## Overview

This repository contains 23 standalone tutorial modules, each teaching one concept in ~50-100 lines of focused code. Each module is a complete, runnable console application.

## Prerequisites

- Java 21+
- Maven 3.8+
- Claude Code CLI installed and authenticated (`claude --version`)

## Three API Styles

The Java SDK provides three ways to interact with Claude:

### 1. One-Shot API (`Query`) - Simplest

```java
// One-liner for quick answers
String answer = Query.text("What is 2+2?");
System.out.println(answer);  // "4"

// Full result with metadata
QueryResult result = Query.execute("Explain Java");
System.out.println("Cost: $" + result.metadata().cost().calculateTotal());
```

### 2. Blocking API (`ClaudeSyncClient`) - Multi-turn & Hooks

```java
try (ClaudeSyncClient client = ClaudeClient.sync()
        .workingDirectory(Path.of("."))
        .build()) {

    String response1 = client.connectText("What is the capital of France?");
    System.out.println(response1);

    // Follow-up in same context
    String response2 = client.queryText("What's its population?");
    System.out.println(response2);  // Claude remembers we were talking about Paris
}
```

### 3. Reactive API (`ClaudeAsyncClient`) - Non-blocking

```java
ClaudeAsyncClient client = ClaudeClient.async()
    .workingDirectory(Path.of("."))
    .build();

// Multi-turn with elegant flatMap chaining
client.connect("My favorite color is blue.").text()
    .flatMap(r1 -> client.query("What is my favorite color?").text())
    .doOnSuccess(System.out::println)  // "blue"
    .subscribe();  // Non-blocking
```

### Which API Should I Use?

| Scenario | Recommended API |
|----------|----------------|
| Simple one-shot query | `Query.text()` |
| Need cost/token metadata | `Query.execute()` |
| Multi-turn conversation | `ClaudeSyncClient` |
| Hooks or permissions | `ClaudeSyncClient` or `ClaudeAsyncClient` |
| Non-blocking / reactive | `ClaudeAsyncClient` |

**Guidance:** Start with `Query` for simple tasks, `ClaudeSyncClient` for multi-turn. Both sync and async clients have full feature parity.

## Quick Start

```bash
# Build all modules
./mvnw compile

# Run a specific module
./mvnw compile exec:java -pl module-01-hello-world
```

## Tutorial Structure

### Part 1: Fundamentals
| Module | Topic | Description |
|--------|-------|-------------|
| 01 | Hello World | Your first query with `Query.text()` |
| 02 | Query API | `Query.text()` vs `Query.execute()` |
| 03 | Sync Client | `ClaudeSyncClient` for multi-turn |
| 04 | Async Client | `ClaudeAsyncClient` reactive patterns |
| 05 | Message Types | `ParsedMessage`, `AssistantMessage`, content blocks |

### Part 2: Configuration
| Module | Topic | Description |
|--------|-------|-------------|
| 06 | CLI Options | Model, working directory, system prompt |
| 07 | Tool Permissions | `allowedTools`, `disallowedTools` |
| 08 | Permission Modes | `DEFAULT`, `ACCEPT_EDITS`, `BYPASS_PERMISSIONS` |
| 09 | Structured Outputs | JSON Schema outputs |

### Part 3: Sessions & State
| Module | Topic | Description |
|--------|-------|-------------|
| 10 | Multi-Turn | Conversation context preservation |
| 11 | Session Resume | Resume previous sessions |
| 12 | Session Fork | Fork for experimentation |
| 13 | Async Advanced | Streaming and error handling |

### Part 4: Safety & Control
| Module | Topic | Description |
|--------|-------|-------------|
| 14 | Permission Callbacks | Dynamic permission with hooks |
| 15 | Hooks PreToolUse | Intercept before tool execution |
| 16 | Hooks PostToolUse | Logging, monitoring after tools |
| 17 | Interrupt Handling | Graceful session interruption |

### Part 5: MCP Integration
| Module | Topic | Description |
|--------|-------|-------------|
| 18 | MCP External | External MCP servers (filesystem) |
| 19 | Multiple MCP Servers | Using multiple servers together |
| 20 | MCP with Hooks | Custom behavior via hooks + MCP |

### Part 6: Multi-Agent
| Module | Topic | Description |
|--------|-------|-------------|
| 21 | Subagents Intro | Agent definitions, Task tool |
| 22 | Subagents Parallel | Parallel execution patterns |
| 23 | Orchestrator Pattern | Master/worker coordination |

## Integration Testing

The `integration-testing/` directory contains automated tests for all modules using jbang.

```bash
cd integration-testing
jbang RunIntegrationTest.java module-01-hello-world
```

## Documentation Fragments

The `doc-fragments/` module contains compilable code that mirrors documentation examples:

```bash
mvn compile -pl doc-fragments -q  # Verify all doc samples compile
```

## Attribution

See [ATTRIBUTION.md](ATTRIBUTION.md) for credits to source material.

## Related Resources

- [Claude Agent SDK Java](https://github.com/markpollack/claude-agent-sdk-java) - The SDK
- [Documentation](https://springaicommunity.mintlify.app/agent-sdk) - Full documentation and API reference
- [Claude Code](https://docs.anthropic.com/en/docs/agents-and-tools/claude-code/overview) - Official Claude Code docs
