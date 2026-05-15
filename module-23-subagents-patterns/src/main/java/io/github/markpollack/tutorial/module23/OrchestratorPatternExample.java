/*
 * Module 23: Subagent Patterns - Orchestrator
 *
 * Demonstrates the orchestrator pattern for multi-agent systems.
 * In this pattern:
 * - A master orchestrator agent coordinates the workflow
 * - Worker agents are specialized for specific tasks
 * - The orchestrator delegates, collects results, and synthesizes
 *
 * Run with: mvn compile exec:java -pl module-23-subagent-patterns
 */
package io.github.markpollack.tutorial.module23;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.Iterator;

public class OrchestratorPatternExample {

    public static void main(String[] args) {
        System.out.println("=== Module 23: Subagent Patterns - Orchestrator ===\n");

        // Define the orchestrator pattern: master + specialized workers
        // Simplified to 2 workers for faster execution
        String agentsJson = """
            {
              "orchestrator": {
                "description": "Master agent that coordinates analysis and synthesizes results",
                "prompt": "You are an orchestrator. Delegate to worker agents and synthesize their findings into a brief summary."
              },
              "security-worker": {
                "description": "Finds security issues",
                "prompt": "You are a security expert. Find security vulnerabilities. Be very brief."
              },
              "quality-worker": {
                "description": "Reviews code quality",
                "prompt": "You are a code quality expert. Find maintainability issues. Be very brief."
              }
            }
            """;

        System.out.println("[Pattern] Orchestrator Pattern");
        System.out.println("[Agents] Defined hierarchical team:");
        System.out.println("  - orchestrator: Coordinates workflow (MASTER)");
        System.out.println("  - security-worker: Security analysis (WORKER)");
        System.out.println("  - quality-worker: Code quality review (WORKER)");

        CLIOptions options = CLIOptions.builder()
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .agents(agentsJson)
                .build();

        System.out.println("\n--- Starting Orchestrated Analysis ---\n");

        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(Path.of("."))
                .build()) {

            // The orchestrator receives the task and decides how to delegate
            String prompt = """
                You are the ORCHESTRATOR. Analyze this code by delegating to security-worker and quality-worker:

                ```java
                public class UserAuth {
                    String password = "admin123";
                    public boolean login(String user, String pwd) {
                        return pwd.equals(password);
                    }
                }
                ```

                1. Briefly explain delegation strategy
                2. Delegate to BOTH workers
                3. Synthesize findings (max 3 bullet points)
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
