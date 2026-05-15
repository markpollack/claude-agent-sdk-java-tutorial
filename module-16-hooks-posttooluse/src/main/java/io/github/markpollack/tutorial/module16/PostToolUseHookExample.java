/*
 * Module 16: Hooks PostToolUse
 *
 * Demonstrates PostToolUse hooks to monitor and react to tool results.
 * This module shows:
 * - Logging tool execution results
 * - Detecting errors in tool output
 * - Tracking tool usage statistics
 *
 * Run with: mvn compile exec:java -pl module-16-hooks-posttooluse
 */
package io.github.markpollack.tutorial.module16;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.hooks.HookRegistry;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;
import io.github.markpollack.claude.agent.sdk.types.control.HookInput;
import io.github.markpollack.claude.agent.sdk.types.control.HookOutput;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PostToolUseHookExample {

    // Track tool usage statistics
    private static final Map<String, AtomicInteger> toolUsageCount = new HashMap<>();
    private static final AtomicInteger errorCount = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("=== Module 16: Hooks PostToolUse ===\n");

        HookRegistry hooks = new HookRegistry();

        // PostToolUse hook to log results and detect errors
        hooks.registerPostToolUse(input -> {
            var postToolUse = (HookInput.PostToolUseInput) input;
            String toolName = postToolUse.toolName();
            Object response = postToolUse.toolResponse();

            // Track usage
            toolUsageCount.computeIfAbsent(toolName, k -> new AtomicInteger(0)).incrementAndGet();

            System.out.println("[PostToolUse] Tool: " + toolName);
            System.out.println("[PostToolUse] Response preview: " +
                truncate(String.valueOf(response), 100));

            // Detect errors in response
            String responseStr = String.valueOf(response).toLowerCase();
            if (responseStr.contains("error") || responseStr.contains("failed") ||
                responseStr.contains("not found") || responseStr.contains("no such file")) {
                errorCount.incrementAndGet();
                System.out.println("[PostToolUse] WARNING: Possible error detected!");
            }

            System.out.println();
            return HookOutput.allow();  // PostToolUse can't block, just observe
        });

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .allowedTools(List.of("Bash", "Read"))
                .hookRegistry(hooks)
                .build()) {

            // Test 1: Successful command
            System.out.println("--- Test 1: Successful command ---");
            client.connect("Run this exact command: echo 'Success!'");
            printResponse(client);

            // Test 2: Read a file
            System.out.println("\n--- Test 2: Read a file ---");
            client.query("Read the first line of pom.xml");
            printResponse(client);

            // Test 3: Command that produces output containing "error" word
            System.out.println("\n--- Test 3: Command with 'error' in output ---");
            client.query("Run this exact command: echo 'Error: this is a test error message'");
            printResponse(client);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Print summary
        System.out.println("\n--- Usage Statistics ---");
        System.out.println("Tool usage counts:");
        toolUsageCount.forEach((tool, count) ->
            System.out.println("  " + tool + ": " + count.get() + " calls"));
        System.out.println("Errors detected: " + errorCount.get());

        System.out.println("\n=== Done ===");
    }

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
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
