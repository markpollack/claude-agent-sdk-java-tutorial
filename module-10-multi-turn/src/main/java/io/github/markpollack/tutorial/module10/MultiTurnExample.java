/*
 * Module 10: Multi-Turn Conversations
 *
 * Demonstrates how ClaudeSyncClient maintains context across
 * multiple query/response exchanges in the same session.
 *
 * Run with: mvn compile exec:java -pl module-10-multi-turn
 */
package io.github.markpollack.tutorial.module10;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.Iterator;

public class MultiTurnExample {

    public static void main(String[] args) {
        System.out.println("=== Module 10: Multi-Turn Conversations ===\n");

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Turn 1: Establish context
            System.out.println("--- Turn 1: Establishing context ---");
            client.connect("I'm learning Java. My favorite topics are " +
                    "concurrency and functional programming. Remember these facts.");
            printResponse(client);

            // Turn 2: Reference the context
            System.out.println("\n--- Turn 2: Testing memory ---");
            client.query("What programming language am I learning?");
            printResponse(client);

            // Turn 3: Reference more context
            System.out.println("\n--- Turn 3: More memory test ---");
            client.query("What are my two favorite Java topics?");
            printResponse(client);

            // Turn 4: Build on the conversation
            System.out.println("\n--- Turn 4: Follow-up question ---");
            client.query("Given my interests, what Java library would you " +
                    "recommend I explore? Just give me one name.");
            printResponse(client);

            System.out.println("\n=== Done ===");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
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
