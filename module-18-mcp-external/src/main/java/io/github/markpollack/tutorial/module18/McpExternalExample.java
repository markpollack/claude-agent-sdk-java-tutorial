/*
 * Module 18: MCP External Server Integration
 *
 * Demonstrates connecting to an external MCP server using McpStdioServerConfig.
 * This module shows:
 * - Configuring an stdio-based MCP server (filesystem server via npx)
 * - Making MCP tools available to Claude
 * - Using allowedTools to pre-approve MCP tools
 *
 * Run with: mvn compile exec:java -pl module-18-mcp-external
 *
 * Prerequisites:
 * - Node.js/npm installed (for npx)
 * - @modelcontextprotocol/server-filesystem available
 */
package io.github.markpollack.tutorial.module18;

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

public class McpExternalExample {

    public static void main(String[] args) throws IOException {
        System.out.println("=== Module 18: MCP External Server Integration ===\n");

        // Create a test directory with some files for the filesystem MCP server
        Path testDir = Files.createTempDirectory("mcp-test");
        Files.writeString(testDir.resolve("hello.txt"), "Hello from MCP filesystem server!");
        Files.writeString(testDir.resolve("data.json"), "{\"version\": \"1.0\", \"status\": \"ok\"}");

        System.out.println("[Info] Test directory created: " + testDir);
        System.out.println("[Info] Test files: hello.txt, data.json\n");

        // Configure the external MCP filesystem server
        // This server provides tools like: read_file, write_file, list_directory
        McpServerConfig.McpStdioServerConfig filesystemServer = new McpServerConfig.McpStdioServerConfig(
                "npx",
                List.of("-y", "@modelcontextprotocol/server-filesystem", testDir.toString()),
                Map.of()  // No extra environment variables needed
        );

        System.out.println("[Info] MCP Server Configuration:");
        System.out.println("  Type: " + filesystemServer.type());
        System.out.println("  Command: " + filesystemServer.command());
        System.out.println("  Args: " + filesystemServer.args());
        System.out.println();

        // Pre-approve MCP tools so Claude can use them without permission prompts
        // MCP tool naming convention: mcp__{serverName}__{toolName}
        List<String> allowedMcpTools = List.of(
                "mcp__fs__read_file",
                "mcp__fs__list_directory",
                "mcp__fs__get_file_info"
        );

        System.out.println("[Info] Allowed MCP tools: " + allowedMcpTools);
        System.out.println();

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(testDir)
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .mcpServer("fs", filesystemServer)  // Register server with name "fs"
                .allowedTools(allowedMcpTools)
                .build()) {

            // Task 1: List files in directory
            System.out.println("--- Task 1: List directory contents ---");
            client.connect("Use the filesystem MCP tools to list files in the current directory. Just show the file names.");
            printResponse(client);

            // Task 2: Read a file
            System.out.println("\n--- Task 2: Read file contents ---");
            client.query("Read the contents of hello.txt using the filesystem MCP tools.");
            printResponse(client);

            // Task 3: Get file info
            System.out.println("\n--- Task 3: Get file information ---");
            client.query("Get information about data.json (like size, type) using the filesystem MCP tools.");
            printResponse(client);

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup test directory
            try {
                Files.deleteIfExists(testDir.resolve("hello.txt"));
                Files.deleteIfExists(testDir.resolve("data.json"));
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
