/*
 * Module 19: Multiple MCP Servers
 *
 * Demonstrates configuring and using multiple MCP servers simultaneously.
 * This module shows:
 * - Registering multiple MCP servers with different capabilities
 * - Using tools from different servers in the same conversation
 * - MCP tool naming convention with server prefixes
 *
 * Run with: mvn compile exec:java -pl module-19-mcp-spring-ai
 *
 * Prerequisites:
 * - Node.js/npm installed (for npx)
 */
package io.github.markpollack.tutorial.module19;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
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

public class MultipleMcpServersExample {

    public static void main(String[] args) throws IOException {
        System.out.println("=== Module 19: Multiple MCP Servers ===\n");

        // Create test directory with sample files
        Path testDir = Files.createTempDirectory("mcp-multi-test");
        Files.writeString(testDir.resolve("notes.txt"), "Remember: MCP tools use mcp__{server}__{tool} naming.");
        Files.writeString(testDir.resolve("config.json"), "{\"theme\": \"dark\", \"version\": 2}");

        System.out.println("[Info] Test directory: " + testDir);
        System.out.println();

        // Configure MCP Server 1: Filesystem server
        McpServerConfig.McpStdioServerConfig filesystemServer = new McpServerConfig.McpStdioServerConfig(
                "npx",
                List.of("-y", "@modelcontextprotocol/server-filesystem", testDir.toString()),
                Map.of()
        );

        // Configure MCP Server 2: Memory server (key-value store)
        McpServerConfig.McpStdioServerConfig memoryServer = new McpServerConfig.McpStdioServerConfig(
                "npx",
                List.of("-y", "@modelcontextprotocol/server-memory"),
                Map.of()
        );

        System.out.println("[Info] MCP Servers Configured:");
        System.out.println("  1. Filesystem server (fs) - file operations in " + testDir);
        System.out.println("  2. Memory server (mem) - key-value storage");
        System.out.println();

        // Pre-approve MCP tools from both servers
        // Tool naming: mcp__{serverName}__{toolName}
        List<String> allowedTools = List.of(
                // Filesystem server tools
                "mcp__fs__read_file",
                "mcp__fs__list_directory",
                // Memory server tools
                "mcp__mem__store",
                "mcp__mem__retrieve"
        );

        System.out.println("[Info] Allowed MCP tools: " + allowedTools);
        System.out.println();

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(testDir)
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .mcpServer("fs", filesystemServer)    // Register as "fs"
                .mcpServer("mem", memoryServer)       // Register as "mem"
                .allowedTools(allowedTools)
                .build()) {

            // Task 1: List files using filesystem server
            System.out.println("--- Task 1: List files (filesystem server) ---");
            client.connect("List the files in the current directory using the filesystem MCP tools.");
            printResponse(client);

            // Task 2: Store data using memory server
            System.out.println("\n--- Task 2: Store data (memory server) ---");
            client.query("Using the memory MCP tools, store the value 'Hello from MCP' with key 'greeting'.");
            printResponse(client);

            // Task 3: Retrieve from memory
            System.out.println("\n--- Task 3: Retrieve data (memory server) ---");
            client.query("Retrieve the value with key 'greeting' from the memory MCP tools.");
            printResponse(client);

            // Task 4: Cross-server workflow - read file, store summary
            System.out.println("\n--- Task 4: Cross-server workflow ---");
            client.query("Read the notes.txt file using filesystem tools, then store its content in memory with key 'notes_backup' using memory tools.");
            printResponse(client);

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            try {
                Files.deleteIfExists(testDir.resolve("notes.txt"));
                Files.deleteIfExists(testDir.resolve("config.json"));
                Files.deleteIfExists(testDir);
                System.out.println("\n[Cleanup] Test directory removed.");
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }

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
