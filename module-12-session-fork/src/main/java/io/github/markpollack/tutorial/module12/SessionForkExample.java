/*
 * Module 12: Session Fork
 *
 * Demonstrates forking a session to create parallel conversation branches.
 * A forked session inherits the context but has its own independent history.
 *
 * Run with: mvn compile exec:java -pl module-12-session-fork
 */
package io.github.markpollack.tutorial.module12;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.Iterator;

public class SessionForkExample {

    public static void main(String[] args) {
        System.out.println("=== Module 12: Session Fork ===\n");

        String originalSessionId;

        // Original session: Establish base context
        System.out.println("--- Original Session: Establishing context ---");
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            client.connect("We're building a web application. " +
                    "The tech stack is: Java backend, React frontend, PostgreSQL database.");
            originalSessionId = processAndGetSessionId(client);
            System.out.println("Original session: " + originalSessionId);
        }

        // Fork the session for a different line of inquiry
        System.out.println("\n--- Forked Session: Alternative exploration ---");

        // Create options with forkSession enabled
        CLIOptions forkOptions = CLIOptions.builder()
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .forkSession(true)  // Enable session forking
                .build();

        try (ClaudeSyncClient forkedClient = ClaudeClient.sync(forkOptions)
                .workingDirectory(Path.of("."))
                .build()) {

            // Resume with fork - creates a new branch from the original
            forkedClient.connect();
            forkedClient.query(
                "What if we used MongoDB instead of PostgreSQL? " +
                "How would that change our architecture?",
                originalSessionId);

            String forkedSessionId = processAndGetSessionId(forkedClient);
            System.out.println("Forked session: " + forkedSessionId);
            System.out.println("(Different from original: " +
                !forkedSessionId.equals(originalSessionId) + ")");
        }

        // Original session is unchanged - can continue on original path
        System.out.println("\n--- Continue Original Session ---");
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            client.connect();
            client.query(
                "Given our PostgreSQL database, what ORM would you recommend?",
                originalSessionId);
            processAndGetSessionId(client);
        }

        System.out.println("\n=== Done ===");
    }

    private static String processAndGetSessionId(ClaudeSyncClient client) {
        String sessionId = null;
        Iterator<ParsedMessage> response = client.receiveResponse();

        while (response.hasNext()) {
            ParsedMessage msg = response.next();
            if (msg.isRegularMessage()) {
                if (msg.asMessage() instanceof AssistantMessage am) {
                    am.getTextContent().ifPresent(text ->
                        System.out.println("Claude: " + text.substring(0,
                            Math.min(200, text.length())) + "..."));
                } else if (msg.asMessage() instanceof ResultMessage rm) {
                    sessionId = rm.sessionId();
                }
            }
        }
        return sessionId;
    }
}
