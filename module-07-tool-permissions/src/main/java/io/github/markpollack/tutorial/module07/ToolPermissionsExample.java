/*
 * Module 07: Tool Permissions
 *
 * Learn how to control which tools Claude can use.
 *
 * Key concepts:
 * - allowedTools: Explicitly allow specific tools (only these are available)
 * - disallowedTools: Block specific tools (all others remain available)
 *
 * Common tools: Read, Write, Edit, Bash, Glob, Grep, WebSearch, WebFetch
 *
 * Run with: mvn compile exec:java -pl module-07-tool-permissions
 */
package io.github.markpollack.tutorial.module07;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;

import java.nio.file.Path;
import java.util.List;

public class ToolPermissionsExample {

    public static void main(String[] args) {
        System.out.println("=== Module 07: Tool Permissions ===\n");

        // Example 1: Allow only specific tools
        allowedToolsExample();

        // Example 2: Block dangerous tools
        disallowedToolsExample();

        System.out.println("\n=== Done ===");
    }

    /**
     * allowedTools: Explicitly allow specific tools.
     * Claude can ONLY use tools in this list.
     * Use when you want maximum control over tool access.
     */
    static void allowedToolsExample() {
        System.out.println("--- Example 1: Allowed Tools ---\n");
        System.out.println("Only Read and Grep tools are allowed.\n");

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .allowedTools(List.of("Read", "Grep"))  // Only these tools
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Claude can read files and search, but cannot write or execute
            String answer = client.connectText("What files are in the current directory? Just list a few.");
            System.out.println("Claude: " + answer);
        }
    }

    /**
     * disallowedTools: Block specific tools.
     * All tools are available EXCEPT these.
     * Use when you want to block specific dangerous operations.
     */
    static void disallowedToolsExample() {
        System.out.println("\n--- Example 2: Disallowed Tools ---\n");
        System.out.println("Bash and Write tools are blocked.\n");

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .disallowedTools(List.of("Bash", "Write", "Edit"))  // Block these
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Claude can read files but cannot execute commands or write files
            String answer = client.connectText("Read the pom.xml file and tell me the project name.");
            System.out.println("Claude: " + answer);
        }
    }
}
