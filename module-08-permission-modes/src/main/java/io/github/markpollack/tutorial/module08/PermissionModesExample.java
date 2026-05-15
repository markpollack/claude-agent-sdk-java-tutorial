/*
 * Module 08: Permission Modes
 *
 * Learn how to control tool permission prompting behavior.
 *
 * Permission modes determine how Claude handles tool execution approval:
 * - DEFAULT: Prompt for each tool use (interactive)
 * - ACCEPT_EDITS: Auto-approve file edits, prompt for others
 * - BYPASS_PERMISSIONS: Skip all permission prompts
 * - DANGEROUSLY_SKIP_PERMISSIONS: Same as bypass (for sandboxed environments)
 *
 * Run with: mvn compile exec:java -pl module-08-permission-modes
 */
package io.github.markpollack.tutorial.module08;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.Message;

import java.nio.file.Path;
import java.util.Iterator;

public class PermissionModesExample {

    public static void main(String[] args) {
        System.out.println("=== Module 08: Permission Modes ===\n");

        // For tutorials and scripts, BYPASS_PERMISSIONS is typical
        bypassPermissionsExample();

        // Show all available modes
        listPermissionModes();

        System.out.println("\n=== Done ===");
    }

    /**
     * BYPASS_PERMISSIONS: Most common for automated scripts.
     * Claude executes tools without asking for approval.
     */
    static void bypassPermissionsExample() {
        System.out.println("--- BYPASS_PERMISSIONS Mode ---\n");
        System.out.println("Claude will execute tools without prompting.\n");

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            client.connect("What is 2 + 2? Answer briefly.");
            printResponse(client);
        }
    }

    /**
     * Display all available permission modes and their use cases.
     */
    static void listPermissionModes() {
        System.out.println("\n--- Permission Modes Reference ---\n");

        System.out.println("| Mode | CLI Flag | Use Case |");
        System.out.println("|------|----------|----------|");
        System.out.printf("| %-30s | %-25s | %s%n",
                "DEFAULT", "--permission-mode default",
                "Interactive CLI, human approval");
        System.out.printf("| %-30s | %-25s | %s%n",
                "ACCEPT_EDITS", "--permission-mode acceptEdits",
                "Auto-approve edits, prompt for Bash");
        System.out.printf("| %-30s | %-25s | %s%n",
                "BYPASS_PERMISSIONS", "--permission-mode bypassPermissions",
                "Automated scripts, CI/CD");
        System.out.printf("| %-30s | %-25s | %s%n",
                "DANGEROUSLY_SKIP_PERMISSIONS", "--dangerously-skip-permissions",
                "Sandboxed environments only");
    }

    static void printResponse(ClaudeSyncClient client) {
        Iterator<ParsedMessage> response = client.receiveResponse();

        while (response.hasNext()) {
            ParsedMessage parsed = response.next();

            if (parsed.isRegularMessage()) {
                Message message = parsed.asMessage();

                if (message instanceof AssistantMessage assistant) {
                    assistant.getTextContent().ifPresent(text ->
                            System.out.println("Claude: " + text));
                }
            }
        }
    }
}
