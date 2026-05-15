/*
 * Module 22: Subagents Parallel Execution
 *
 * Demonstrates defining multiple subagents and asking Claude to
 * execute them in parallel. This module shows:
 * - Defining multiple agents in a single JSON configuration
 * - Instructing Claude to use multiple agents for a task
 * - Coordinating results from parallel agent executions
 *
 * Run with: mvn compile exec:java -pl module-22-subagents-parallel
 */
package io.github.markpollack.tutorial.module22;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.Iterator;

public class SubagentsParallelExample {

    public static void main(String[] args) {
        System.out.println("=== Module 22: Subagents Parallel Execution ===\n");

        // Define multiple specialized agents
        String agentsJson = """
            {
              "analyzer": {
                "description": "Analyzes code structure, patterns, and architecture",
                "prompt": "You are a code analyzer. Examine code structure, design patterns, and architecture decisions. Provide concise analysis in 2-3 bullet points."
              },
              "security-auditor": {
                "description": "Audits code for security vulnerabilities",
                "prompt": "You are a security auditor. Find security vulnerabilities like injection flaws, hardcoded secrets, or unsafe operations. Be brief and specific."
              },
              "performance-reviewer": {
                "description": "Reviews code for performance issues",
                "prompt": "You are a performance expert. Identify inefficiencies, memory leaks, or optimization opportunities. Keep your review concise."
              }
            }
            """;

        System.out.println("[Info] Defined 3 agents:");
        System.out.println("  - analyzer: Analyzes code structure and patterns");
        System.out.println("  - security-auditor: Finds security vulnerabilities");
        System.out.println("  - performance-reviewer: Identifies performance issues");

        // Build CLI options with the multiple agents
        CLIOptions options = CLIOptions.builder()
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .agents(agentsJson)
                .build();

        System.out.println("\n--- Requesting parallel agent analysis ---\n");

        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(Path.of("."))
                .build()) {

            // Ask Claude to use all three agents to review the code
            String prompt = """
                Review this code using ALL THREE agents (analyzer, security-auditor, performance-reviewer) IN PARALLEL:

                ```java
                public class UserService {
                    private static final String DB_PASSWORD = "secret123";

                    public User findUser(String userId) {
                        String query = "SELECT * FROM users WHERE id = '" + userId + "'";
                        List<User> results = new ArrayList<>();
                        for (int i = 0; i < 1000000; i++) {
                            results.add(executeQuery(query));
                        }
                        return results.get(0);
                    }

                    private User executeQuery(String sql) {
                        // Simulated database query
                        return new User(sql.hashCode());
                    }
                }
                ```

                Run all three agents simultaneously using the Task tool and summarize their findings.
                """;

            client.connect(prompt);
            printResponse(client);

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Done ===");
    }

    private static void printResponse(ClaudeSyncClient client) {
        Iterator<ParsedMessage> response = client.receiveResponse();
        while (response.hasNext()) {
            ParsedMessage msg = response.next();
            if (msg.isRegularMessage()) {
                if (msg.asMessage() instanceof AssistantMessage am) {
                    am.getTextContent().ifPresent(text ->
                        System.out.println("Claude: " + text));
                } else if (msg.asMessage() instanceof ResultMessage rm) {
                    if (rm.totalCostUsd() != null) {
                        System.out.printf("  [Cost: $%.6f]%n", rm.totalCostUsd());
                    }
                }
            }
        }
    }
}
