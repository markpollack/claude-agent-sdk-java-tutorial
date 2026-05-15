/*
 * Module 14: Permission Callbacks
 *
 * Demonstrates using PreToolUse hooks for dynamic tool permission decisions.
 * The hook is invoked before each tool execution, allowing you to:
 * - Allow or deny tool usage based on tool name and input
 * - Log all tool permission requests
 *
 * Note: This uses HookRegistry.registerPreToolUse() which provides the same
 * functionality as Python SDK's can_use_tool callback.
 *
 * Run with: mvn compile exec:java -pl module-14-permission-callbacks
 */
package io.github.markpollack.tutorial.module14;

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
import java.util.Iterator;
import java.util.List;

public class PermissionCallbackExample {

    private static final List<String> DANGEROUS_PATTERNS = List.of(
        "rm -rf", "sudo", "chmod 777", "mkfs"
    );

    public static void main(String[] args) {
        System.out.println("=== Module 14: Permission Callbacks ===\n");

        // Create hook registry with permission-checking hooks
        HookRegistry hooks = new HookRegistry();

        // Register a PreToolUse hook for all tools
        hooks.registerPreToolUse(input -> {
            var preToolUse = (HookInput.PreToolUseInput) input;
            String toolName = preToolUse.toolName();

            System.out.println("[Permission] Tool: " + toolName);
            System.out.println("[Permission] Input: " + preToolUse.toolInput());

            // Always allow read-only operations
            if (toolName.equals("Read") || toolName.equals("Glob") || toolName.equals("Grep")) {
                System.out.println("[Permission] ALLOWED (read-only operation)\n");
                return HookOutput.allow();
            }

            // Check Bash commands for dangerous patterns
            if (toolName.equals("Bash")) {
                String command = preToolUse.getArgument("command", String.class).orElse("");

                for (String pattern : DANGEROUS_PATTERNS) {
                    if (command.contains(pattern)) {
                        System.out.println("[Permission] DENIED (dangerous: " + pattern + ")\n");
                        return HookOutput.block("Blocked dangerous command: " + pattern);
                    }
                }

                System.out.println("[Permission] ALLOWED (safe command)\n");
                return HookOutput.allow();
            }

            // Default: allow other tools
            System.out.println("[Permission] ALLOWED (default)\n");
            return HookOutput.allow();
        });

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .allowedTools(List.of("Read", "Bash", "Glob"))
                .hookRegistry(hooks)
                .build()) {

            // Test 1: Safe read operation (should be allowed)
            System.out.println("--- Test 1: Safe read operation ---");
            client.connect("List the files in the current directory using Glob. Be brief.");
            printResponse(client);

            // Test 2: Safe bash command (should be allowed)
            System.out.println("\n--- Test 2: Safe Bash command ---");
            client.query("Run this exact command: echo 'Hello from permission callback!'");
            printResponse(client);

            // Test 3: Dangerous command (should be blocked by our hook)
            System.out.println("\n--- Test 3: Dangerous command (will be blocked) ---");
            client.query("Run this exact command: rm -rf /tmp/test");
            printResponse(client);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
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
