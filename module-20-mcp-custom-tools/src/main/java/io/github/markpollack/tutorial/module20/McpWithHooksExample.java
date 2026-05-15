/*
 * Module 20: MCP with Hooks Integration
 *
 * Demonstrates combining MCP servers with hooks for advanced control.
 * This module shows:
 * - Using hooks to intercept MCP tool calls
 * - Logging MCP tool usage with custom formatting
 * - Blocking certain MCP operations based on custom rules
 * - Post-processing MCP tool results
 *
 * Run with: mvn compile exec:java -pl module-20-mcp-custom-tools
 *
 * This pattern provides "custom tool behavior" by layering hooks on top of
 * MCP servers, giving you programmatic control over tool execution.
 */
package io.github.markpollack.tutorial.module20;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.types.control.HookInput;
import io.github.markpollack.claude.agent.sdk.types.control.HookOutput;
import io.github.markpollack.claude.agent.sdk.hooks.HookRegistry;
import io.github.markpollack.claude.agent.sdk.mcp.McpServerConfig;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class McpWithHooksExample {

    // Track MCP tool usage statistics
    private static final AtomicInteger mcpToolCallCount = new AtomicInteger(0);
    private static final AtomicInteger mcpToolBlockCount = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        System.out.println("=== Module 20: MCP with Hooks Integration ===\n");

        // Create test directory with files
        Path testDir = Files.createTempDirectory("mcp-hooks-test");
        Files.writeString(testDir.resolve("allowed.txt"), "This file can be read.");
        Files.writeString(testDir.resolve("secret.txt"), "CONFIDENTIAL: This should not be accessible.");
        Files.writeString(testDir.resolve("data.json"), "{\"public\": true}");

        System.out.println("[Info] Test directory: " + testDir);
        System.out.println("[Info] Test files: allowed.txt, secret.txt, data.json\n");

        // Configure MCP filesystem server
        McpServerConfig.McpStdioServerConfig filesystemServer = new McpServerConfig.McpStdioServerConfig(
                "npx",
                List.of("-y", "@modelcontextprotocol/server-filesystem", testDir.toString()),
                Map.of()
        );

        // Create hook registry with MCP-aware hooks
        HookRegistry hooks = new HookRegistry();

        // Pre-tool hook: Log and optionally block MCP tool calls
        hooks.registerPreToolUse(input -> {
            var preToolUse = (HookInput.PreToolUseInput) input;
            String toolName = preToolUse.toolName();

            // Check if this is an MCP tool (format: mcp__{server}__{tool})
            if (toolName.startsWith("mcp__")) {
                mcpToolCallCount.incrementAndGet();

                // Extract server and tool name
                String[] parts = toolName.split("__");
                String serverName = parts.length > 1 ? parts[1] : "unknown";
                String mcpTool = parts.length > 2 ? parts[2] : "unknown";

                System.out.printf("[Hook:PreMCP] Server=%s, Tool=%s%n", serverName, mcpTool);

                // Custom rule: Block access to files containing "secret"
                String filePath = preToolUse.getArgument("path", String.class).orElse("");
                if (filePath.toLowerCase().contains("secret")) {
                    mcpToolBlockCount.incrementAndGet();
                    System.out.println("[Hook:PreMCP] BLOCKED: Access to secret files is not allowed!");
                    return HookOutput.block("Access denied: Cannot read files with 'secret' in the name.");
                }
            }
            return HookOutput.allow();
        });

        // Post-tool hook: Log MCP tool results
        hooks.registerPostToolUse(input -> {
            var postToolUse = (HookInput.PostToolUseInput) input;
            String toolName = postToolUse.toolName();

            if (toolName.startsWith("mcp__")) {
                Object response = postToolUse.toolResponse();
                String responsePreview = String.valueOf(response);
                if (responsePreview.length() > 80) {
                    responsePreview = responsePreview.substring(0, 80) + "...";
                }
                System.out.printf("[Hook:PostMCP] Tool=%s, Response=%s%n", toolName, responsePreview);
            }
            return HookOutput.allow();
        });

        System.out.println("[Info] Hooks registered for MCP tool interception.\n");

        // Pre-approve MCP tools
        List<String> allowedTools = List.of(
                "mcp__fs__read_file",
                "mcp__fs__list_directory"
        );

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(testDir)
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .mcpServer("fs", filesystemServer)
                .allowedTools(allowedTools)
                .hookRegistry(hooks)
                .build()) {

            // Task 1: List files (allowed)
            System.out.println("--- Task 1: List directory (should work) ---");
            client.connect("List the files in the current directory.");
            printResponse(client);

            // Task 2: Read allowed file (should work)
            System.out.println("\n--- Task 2: Read allowed.txt (should work) ---");
            client.query("Read the contents of allowed.txt");
            printResponse(client);

            // Task 3: Try to read secret file (should be blocked by hook)
            System.out.println("\n--- Task 3: Read secret.txt (should be blocked) ---");
            client.query("Read the contents of secret.txt");
            printResponse(client);

            // Task 4: Read data.json (should work)
            System.out.println("\n--- Task 4: Read data.json (should work) ---");
            client.query("Read the contents of data.json");
            printResponse(client);

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
        } finally {
            // Cleanup
            try {
                Files.deleteIfExists(testDir.resolve("allowed.txt"));
                Files.deleteIfExists(testDir.resolve("secret.txt"));
                Files.deleteIfExists(testDir.resolve("data.json"));
                Files.deleteIfExists(testDir);
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }

        // Print statistics
        System.out.println("\n--- MCP Tool Usage Statistics ---");
        System.out.println("Total MCP tool calls intercepted: " + mcpToolCallCount.get());
        System.out.println("MCP tool calls blocked: " + mcpToolBlockCount.get());

        System.out.println("\n[Info] Session completed.");
        System.out.println("=== Done ===");
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
