/*
 * Final Project: Research Agent
 *
 * A comprehensive example combining all tutorial concepts:
 * - Hooks for monitoring tool usage
 * - MCP server for file access
 * - Custom agents for specialized research
 * - Multi-turn conversation for iterative research
 *
 * Run with: mvn compile exec:java -pl final-project-research-agent
 */
package io.github.markpollack.tutorial.finalproject;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.hooks.HookRegistry;
import io.github.markpollack.claude.agent.sdk.mcp.McpServerConfig;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;
import io.github.markpollack.claude.agent.sdk.types.control.HookInput;
import io.github.markpollack.claude.agent.sdk.types.control.HookOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ResearchAgentExample {

    // Statistics for hook monitoring
    private static final AtomicInteger toolCalls = new AtomicInteger(0);
    private static final AtomicInteger mcpCalls = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        System.out.println("=== Final Project: Research Agent ===\n");
        System.out.println("Combining: Hooks + MCP + Agents + Multi-turn\n");

        // Create test research data
        Path researchDir = Files.createTempDirectory("research-agent");
        Files.writeString(researchDir.resolve("findings.txt"),
                "Research Finding: AI adoption in enterprises grew 35% in 2024.\n" +
                "Key drivers: cost reduction, automation, and improved accuracy.");
        Files.writeString(researchDir.resolve("summary.txt"),
                "Executive Summary: The AI market is projected to reach $500B by 2027.");

        System.out.println("[Setup] Research directory: " + researchDir);

        // 1. Configure hooks for monitoring
        HookRegistry hooks = new HookRegistry();
        hooks.registerPreToolUse(input -> {
            var preToolUse = (HookInput.PreToolUseInput) input;
            String tool = preToolUse.toolName();
            toolCalls.incrementAndGet();
            if (tool.startsWith("mcp__")) {
                mcpCalls.incrementAndGet();
                System.out.println("[Hook] MCP tool: " + tool);
            }
            return HookOutput.allow();
        });

        // 2. Configure MCP filesystem server
        McpServerConfig.McpStdioServerConfig fsServer = new McpServerConfig.McpStdioServerConfig(
                "npx",
                List.of("-y", "@modelcontextprotocol/server-filesystem", researchDir.toString()),
                Map.of()
        );

        // 3. Define research agents
        String agentsJson = """
            {
              "analyst": {
                "description": "Analyzes research data and extracts insights",
                "prompt": "You are a research analyst. Extract key insights from data. Be concise."
              },
              "summarizer": {
                "description": "Creates executive summaries",
                "prompt": "You are a summarizer. Create brief, actionable summaries."
              }
            }
            """;

        // 4. Build CLI options combining all features
        CLIOptions options = CLIOptions.builder()
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .agents(agentsJson)
                .mcpServers(Map.of("fs", fsServer))
                .allowedTools(List.of("mcp__fs__list_directory", "mcp__fs__read_file", "mcp__fs__read_text_file", "mcp__fs__write_file"))
                .build();

        System.out.println("[Setup] Agents: analyst, summarizer");
        System.out.println("[Setup] MCP server: filesystem");
        System.out.println("[Setup] Hooks: monitoring enabled\n");

        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(researchDir)
                .hookRegistry(hooks)
                .build()) {

            // Turn 1: Initial research request
            System.out.println("--- Turn 1: Initial Research ---");
            client.connect("List the research files available, then use the analyst agent to analyze findings.txt");
            printResponse(client);

            // Turn 2: Follow-up with context
            System.out.println("\n--- Turn 2: Summary Request ---");
            client.query("Now use the summarizer agent to read summary.txt and combine with the previous analysis.");
            printResponse(client);

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
        } finally {
            // Cleanup - recursively delete all files in temp directory
            try {
                Files.walk(researchDir)
                        .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete files before dirs
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {}
                        });
            } catch (IOException ignored) {}
        }

        // Print statistics
        System.out.println("\n--- Agent Statistics ---");
        System.out.println("Total tool calls: " + toolCalls.get());
        System.out.println("MCP tool calls: " + mcpCalls.get());

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
