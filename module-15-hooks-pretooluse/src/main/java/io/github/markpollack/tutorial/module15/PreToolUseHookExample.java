/*
 * Module 15: Hooks PreToolUse
 *
 * Demonstrates PreToolUse hooks to intercept tool execution before it happens.
 * This module shows:
 * - Tool-specific hook registration with pattern matching
 * - Logging tool invocations before execution
 * - Blocking specific commands with HookOutput.block()
 *
 * Run with: mvn compile exec:java -pl module-15-hooks-pretooluse
 */
package io.github.markpollack.tutorial.module15;

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

public class PreToolUseHookExample {

    public static void main(String[] args) {
        System.out.println("=== Module 15: Hooks PreToolUse ===\n");

        HookRegistry hooks = new HookRegistry();

        // Hook 1: Log all Bash commands before execution
        hooks.registerPreToolUse("Bash", input -> {
            var preToolUse = (HookInput.PreToolUseInput) input;
            String command = preToolUse.getArgument("command", String.class).orElse("");

            System.out.println("[PreToolUse:Bash] Command: " + command);

            // Block commands containing 'foo.sh' (like Python example)
            if (command.contains("foo.sh")) {
                System.out.println("[PreToolUse:Bash] BLOCKED: Contains 'foo.sh'\n");
                return HookOutput.block("Command contains blocked pattern: foo.sh");
            }

            System.out.println("[PreToolUse:Bash] ALLOWED\n");
            return HookOutput.allow();
        });

        // Hook 2: Log all Read operations
        hooks.registerPreToolUse("Read", input -> {
            var preToolUse = (HookInput.PreToolUseInput) input;
            String filePath = preToolUse.getArgument("file_path", String.class).orElse("");

            System.out.println("[PreToolUse:Read] File: " + filePath);
            System.out.println("[PreToolUse:Read] ALLOWED\n");
            return HookOutput.allow();
        });

        // Hook 3: Global hook for any tool (logging only)
        hooks.registerPreToolUse(input -> {
            var preToolUse = (HookInput.PreToolUseInput) input;
            System.out.println("[PreToolUse:*] Tool invoked: " + preToolUse.toolName());
            return HookOutput.allow();  // Always allow, just logging
        });

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .allowedTools(List.of("Bash", "Read"))
                .hookRegistry(hooks)
                .build()) {

            // Test 1: Safe Bash command (should be logged and allowed)
            System.out.println("--- Test 1: Safe Bash command ---");
            client.connect("Run this exact command: echo 'PreToolUse hook test'");
            printResponse(client);

            // Test 2: Read operation (should trigger Read hook)
            System.out.println("\n--- Test 2: Read operation ---");
            client.query("Read the first 3 lines of pom.xml");
            printResponse(client);

            // Test 3: Blocked command (contains 'foo.sh')
            System.out.println("\n--- Test 3: Blocked command (foo.sh) ---");
            client.query("Run this exact command: ./foo.sh --help");
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
