/*
 * Module 05: Message Types
 *
 * Understanding the message and content block type hierarchy.
 *
 * Message types:
 * - UserMessage: Your input to Claude
 * - AssistantMessage: Claude's response (contains content blocks)
 * - SystemMessage: System context
 * - ResultMessage: Session completion with metadata
 *
 * Content blocks (inside AssistantMessage):
 * - TextBlock: Regular text response
 * - ToolUseBlock: Claude invoking a tool
 * - ToolResultBlock: Result from a tool
 * - ThinkingBlock: Claude's reasoning (when enabled)
 *
 * Run with: mvn compile exec:java -pl module-05-message-types
 */
package io.github.markpollack.tutorial.module05;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.types.*;

import java.nio.file.Path;
import java.util.Iterator;

public class MessageTypesExample {

    public static void main(String[] args) {
        System.out.println("=== Module 05: Message Types ===\n");

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .build()) {

            // Ask Claude to do something that involves tools
            System.out.println("Prompt: List the files in the current directory\n");
            client.connect("List the files in the current directory");

            Iterator<ParsedMessage> response = client.receiveResponse();

            while (response.hasNext()) {
                ParsedMessage parsed = response.next();

                if (parsed.isRegularMessage()) {
                    processMessage(parsed.asMessage());
                }
            }
        }

        System.out.println("\n=== Done ===");
    }

    /**
     * Process different message types using pattern matching.
     */
    static void processMessage(Message message) {
        switch (message) {
            case UserMessage user -> {
                System.out.println("[USER] " + user.content());
            }

            case AssistantMessage assistant -> {
                System.out.println("[ASSISTANT] Content blocks: " + assistant.content().size());

                // Process each content block
                for (ContentBlock block : assistant.content()) {
                    processContentBlock(block);
                }
            }

            case SystemMessage system -> {
                System.out.println("[SYSTEM] subtype=" + system.subtype());
            }

            case ResultMessage result -> {
                System.out.println("[RESULT]");
                System.out.printf("  Duration: %d ms%n", result.durationMs());
                System.out.printf("  Turns: %d%n", result.numTurns());
                System.out.printf("  Cost: $%.6f%n", result.totalCostUsd());
                if (result.isError()) {
                    System.out.println("  Error: " + result.result());
                }
            }

            default -> {
                System.out.println("[UNKNOWN] " + message.getType());
            }
        }
    }

    /**
     * Process different content block types.
     */
    static void processContentBlock(ContentBlock block) {
        switch (block) {
            case TextBlock text -> {
                System.out.println("  [TEXT] " + truncate(text.text(), 100));
            }

            case ToolUseBlock tool -> {
                System.out.println("  [TOOL_USE] " + tool.name());
                System.out.println("    ID: " + tool.id());
                System.out.println("    Input: " + truncate(tool.input().toString(), 80));
            }

            case ToolResultBlock result -> {
                System.out.println("  [TOOL_RESULT] ID: " + result.toolUseId());
            }

            case ThinkingBlock thinking -> {
                System.out.println("  [THINKING] " + truncate(thinking.thinking(), 80));
            }

            default -> {
                System.out.println("  [UNKNOWN BLOCK] " + block.getType());
            }
        }
    }

    /**
     * Truncate text for display.
     */
    static String truncate(String text, int maxLength) {
        if (text == null) return "(null)";
        String oneLine = text.replace("\n", " ");
        if (oneLine.length() <= maxLength) return oneLine;
        return oneLine.substring(0, maxLength - 3) + "...";
    }
}
