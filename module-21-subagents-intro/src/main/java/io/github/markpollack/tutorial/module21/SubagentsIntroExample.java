/*
 * Module 21: Subagents Introduction
 *
 * Demonstrates defining and using custom agents with the Claude Agent SDK.
 * This module shows:
 * - Defining agents with JSON configuration
 * - Using the --agents CLI option
 * - Instructing Claude to spawn a subagent via the Task tool
 *
 * Run with: mvn compile exec:java -pl module-21-subagents-intro
 */
package io.github.markpollack.tutorial.module21;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.Iterator;

public class SubagentsIntroExample {

    public static void main(String[] args) {
        System.out.println("=== Module 21: Subagents Introduction ===\n");

        // Define a code reviewer subagent
        // The agents JSON follows the format: {"name": {"description": "...", "prompt": "..."}}
        String agentsJson = """
            {
              "code-reviewer": {
                "description": "Reviews code for best practices and potential issues",
                "prompt": "You are a code reviewer. Analyze code for bugs, performance issues, security vulnerabilities, and adherence to best practices. Provide constructive feedback in a concise format."
              }
            }
            """;

        System.out.println("[Info] Defined agent: code-reviewer");
        System.out.println("[Info] Description: Reviews code for best practices and potential issues");

        // Build CLI options with the agents definition
        CLIOptions options = CLIOptions.builder()
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .agents(agentsJson)
                .build();

        System.out.println("\n--- Spawning the code-reviewer subagent ---");

        // Use ClaudeClient.sync(options) to pass pre-built CLIOptions with agents
        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(Path.of("."))
                .build()) {

            // Ask Claude to use the code-reviewer subagent
            // Claude will use the Task tool to spawn the subagent
            String prompt = """
                Use the code-reviewer agent to review this Java code:

                ```java
                public class Example {
                    public static void main(String[] args) {
                        String password = "admin123";
                        for(int i=0;i<1000;i++) {
                            System.out.println(password);
                        }
                    }
                }
                ```

                Provide a brief review with 2-3 key issues.
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
