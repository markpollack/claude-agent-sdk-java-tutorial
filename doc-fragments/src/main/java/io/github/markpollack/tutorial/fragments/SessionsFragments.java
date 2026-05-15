/*
 * Code fragments for Part 3: Sessions & State documentation.
 * Each method corresponds to a code block in the tutorial docs.
 *
 * If this file doesn't compile, the docs have invalid code.
 */
package io.github.markpollack.tutorial.fragments;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.ClaudeAsyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;
import io.github.markpollack.claude.agent.sdk.types.Message;

import java.nio.file.Path;

/**
 * Sessions & State code fragments from tutorial modules 09-12.
 */
public class SessionsFragments {

    // === 09-multi-turn.md: "The Conversation Loop Pattern" ===
    void multiTurnConversation() {
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Turn 1: Establish context
            String response1 = client.connectText("My favorite programming language is Java.");
            System.out.println(response1);

            // Turn 2: Claude remembers the context from Turn 1
            String response2 = client.queryText("What is my favorite programming language?");
            System.out.println(response2);  // "Java"

            // Turn 3: Continue building on context
            String response3 = client.queryText("Spell it backwards.");
            System.out.println(response3);  // "avaJ"
        }
    }

    // === 09-multi-turn.md: "Full Message Access" ===
    void multiTurnWithMessageAccess(ClaudeSyncClient client) {
        for (Message msg : client.connectAndReceive("Create a test file")) {
            System.out.println(msg);  // All types have useful toString()

            if (msg instanceof AssistantMessage am) {
                am.getToolUses().forEach(tool ->
                    System.out.println("Tool used: " + tool.name()));
            } else if (msg instanceof ResultMessage rm) {
                System.out.printf("Cost: $%.6f%n", rm.totalCostUsd());
            }
        }
    }

    // === 10-session-resume.md: "Resuming a Session" ===
    void sessionResume() {
        String savedSessionId = null;

        // Session 1: Create conversation and save session ID
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Get conversation going and extract session ID
            for (Message msg : client.connectAndReceive("Remember this code: ALPHA-123")) {
                System.out.println(msg);
                if (msg instanceof ResultMessage rm) {
                    savedSessionId = rm.sessionId();
                }
            }
        }

        // Later: Resume the conversation
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            client.connect();  // Connect without initial prompt
            client.query("What code did I tell you?", savedSessionId);

            // Claude remembers: "ALPHA-123"
            for (Message msg : client.messages()) {
                System.out.println(msg);
            }
        }
    }

    // === 11-session-fork.md: "Forking a Session" ===
    void sessionFork() {
        String originalSessionId = null;

        // Original session - extract session ID
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            for (Message msg : client.connectAndReceive("We're building an app with PostgreSQL.")) {
                System.out.println(msg);
                if (msg instanceof ResultMessage rm) {
                    originalSessionId = rm.sessionId();
                }
            }
        }

        // Fork for alternative exploration
        CLIOptions forkOptions = CLIOptions.builder()
            .model(CLIOptions.MODEL_HAIKU)
            .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
            .forkSession(true)  // Enable forking
            .build();

        try (ClaudeSyncClient forkedClient = ClaudeClient.sync(forkOptions)
                .workingDirectory(Path.of("."))
                .build()) {

            forkedClient.connect();
            forkedClient.query("What if we used MongoDB instead?", originalSessionId);

            // Creates a new branch with different session ID
            String forkedSessionId = null;
            for (Message msg : forkedClient.messages()) {
                System.out.println(msg);
                if (msg instanceof ResultMessage rm) {
                    forkedSessionId = rm.sessionId();
                    // forkedSessionId != originalSessionId
                }
            }
        }
    }

    // === 12-streaming.md: "Text Streaming" ===
    void asyncStreamingText() {
        ClaudeAsyncClient client = ClaudeClient.async()
            .workingDirectory(Path.of("."))
            .model(CLIOptions.MODEL_HAIKU)
            .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
            .build();

        // Stream text as it arrives
        client.query("Write a haiku about Java").textStream()
            .doOnNext(System.out::print)
            .subscribe();  // Non-blocking
    }

    // === 12-streaming.md: "Full Message Access" ===
    void asyncStreamingMessages() {
        ClaudeAsyncClient client = ClaudeClient.async()
            .workingDirectory(Path.of("."))
            .model(CLIOptions.MODEL_HAIKU)
            .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
            .build();

        // Access all message types
        client.query("List files").messages()
            .doOnNext(msg -> System.out.println(msg))  // toString() works on all types
            .subscribe();
    }

    // === 12-streaming.md: "Multi-Turn Conversations" ===
    void asyncMultiTurn() {
        ClaudeAsyncClient client = ClaudeClient.async()
            .workingDirectory(Path.of("."))
            .build();

        // Multi-turn with elegant flatMap chaining
        client.connect("My name is Alice").text()
            .flatMap(r1 -> client.query("What is my name?").text())
            .doOnSuccess(response -> {
                // Claude remembers: "Alice"
                System.out.println(response);
            })
            .subscribe();  // Non-blocking
    }
}
