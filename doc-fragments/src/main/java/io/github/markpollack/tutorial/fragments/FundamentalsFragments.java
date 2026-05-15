/*
 * Code fragments for Part 1: Fundamentals (modules 03-04).
 * If this file doesn't compile, the docs have invalid code.
 */
package io.github.markpollack.tutorial.fragments;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.Message;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;
import io.github.markpollack.claude.agent.sdk.types.SystemMessage;
import io.github.markpollack.claude.agent.sdk.types.UserMessage;
import io.github.markpollack.claude.agent.sdk.types.ContentBlock;
import io.github.markpollack.claude.agent.sdk.types.TextBlock;
import io.github.markpollack.claude.agent.sdk.types.ToolResultBlock;
import io.github.markpollack.claude.agent.sdk.types.ToolUseBlock;

import java.nio.file.Path;

/**
 * Fundamentals code fragments from tutorial modules 03-04.
 */
public class FundamentalsFragments {

    // === 03-session-api.md: "Multi-Turn Conversation (Simple)" ===
    void multiTurnSimple() {
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .build()) {

            String answer1 = client.connectText("What's the capital of France?");
            System.out.println(answer1);  // "Paris"

            String answer2 = client.queryText("What's the population of that city?");
            System.out.println(answer2);  // Claude remembers "Paris"

            String answer3 = client.queryText("What are its famous landmarks?");
            System.out.println(answer3);
        }
    }

    // === 03-session-api.md: "Multi-Turn with Message Access" ===
    void multiTurnWithMessages() {
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .build()) {

            for (Message msg : client.connectAndReceive("List files in current directory")) {
                System.out.println(msg);  // All message types have useful toString()

                if (msg instanceof AssistantMessage am) {
                    am.getToolUses().forEach(tool ->
                        System.out.println("Tool used: " + tool.name()));
                } else if (msg instanceof ResultMessage rm) {
                    System.out.printf("Cost: $%.6f%n", rm.totalCostUsd());
                }
            }
        }
    }

    // === 04-message-types.md: "Pattern Matching" ===
    void patternMatching(Message message) {
        switch (message) {
            case UserMessage user ->
                System.out.println("User: " + user);  // toString() works
            case AssistantMessage assistant ->
                System.out.println("Assistant: " + assistant.text());  // text() returns String
            case SystemMessage system ->
                System.out.println("System: " + system.subtype());
            case ResultMessage result ->
                System.out.println(result);  // "[Result: cost=$X.XX, turns=N, session=ID]"
            default ->
                System.out.println("Unknown message type");
        }
    }

    // === 04-message-types.md: "Content Blocks" ===
    void contentBlocks(AssistantMessage assistant) {
        for (ContentBlock block : assistant.content()) {
            switch (block) {
                case TextBlock text ->
                    System.out.println("Text: " + text.text());
                case ToolUseBlock toolUse ->
                    System.out.println("Tool: " + toolUse.name());
                case ToolResultBlock toolResult ->
                    System.out.println("Result: " + toolResult.content());
                default ->
                    System.out.println("Other block type");
            }
        }
    }

    // === 04-message-types.md: "Convenience Methods" ===
    void convenienceMethods(AssistantMessage assistant) {
        // Get all text concatenated (empty string if none)
        String text = assistant.text();

        // Print directly - toString() returns the text
        System.out.println(assistant);  // Same as assistant.text()

        // Get first text block content (Optional)
        assistant.getTextContent().ifPresent(System.out::println);

        // Check for tool use
        if (assistant.hasToolUse()) {
            assistant.getToolUses().forEach(tool ->
                System.out.println("Using tool: " + tool.name()));
        }
    }

    // === 04-message-types.md: "Message toString() Methods" ===
    void messageToString(ClaudeSyncClient client) {
        for (Message msg : client.connectAndReceive("List files")) {
            System.out.println(msg);  // Works for all message types
        }
        // AssistantMessage: prints the text content
        // ResultMessage: "[Result: cost=$0.001234, turns=3, session=abc123]"
        // UserMessage: prints the user input
        // SystemMessage: prints system info
    }
}
