/*
 * Code fragments for java.md reference documentation.
 * Each method corresponds to a code block in the docs.
 *
 * If this file doesn't compile, the docs have invalid code.
 */
package io.github.markpollack.tutorial.fragments;

import io.github.markpollack.claude.agent.sdk.Query;
import io.github.markpollack.claude.agent.sdk.QueryOptions;
import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.ClaudeAsyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.QueryResult;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * Code fragments from java.md reference documentation.
 * Updated to use three-API architecture: Query, ClaudeSyncClient, ClaudeAsyncClient.
 */
public class JavaMdFragments {

    // === java.md: "Example - Simple query" ===
    void simpleQuery() {
        // Simplest usage - one-liner
        String answer = Query.text("What is 2+2?");
        System.out.println(answer);  // "4"
    }

    // === java.md: "Example - With options" ===
    void withOptions() {
        QueryOptions options = QueryOptions.builder()
            .model("claude-sonnet-4-20250514")
            .systemPrompt("You are an expert Java developer")
            .timeout(Duration.ofMinutes(5))
            .build();

        String response = Query.text("Create a Java web server", options);
        System.out.println(response);
    }

    // === java.md: "Example - Full result with metadata" ===
    void fullResult() {
        QueryResult result = Query.execute("Write a haiku about Java");

        System.out.println(result.text().orElse(""));
        System.out.println("Cost: $" + result.metadata().cost().calculateTotal());
        System.out.println("Tokens: " + result.metadata().usage().inputTokens()
            + " in, " + result.metadata().usage().outputTokens() + " out");
    }

    // === index.md: "Multi-Turn Conversation" ===
    void multiTurnConversation() {
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .build()) {

            String response1 = client.connectText("My favorite color is blue.");
            System.out.println(response1);

            String response2 = client.queryText("What is my favorite color?");
            System.out.println(response2);  // "blue"

            String response3 = client.queryText("Spell it backwards.");
            System.out.println(response3);  // "eulb"
        }
    }

    // === java.md: "Builder Example" (QueryOptions) ===
    void queryOptionsBuilder() {
        QueryOptions options = QueryOptions.builder()
            .model("claude-sonnet-4-20250514")
            .systemPrompt("You are a helpful assistant")
            .timeout(Duration.ofMinutes(5))
            .allowedTools(List.of("Read", "Write", "Bash"))
            .disallowedTools(List.of("WebSearch"))
            .maxTurns(10)
            .workingDirectory(Path.of("/home/user/project"))
            .build();
    }

    // === java.md: "Example - CLIOptions Builder" ===
    void cliOptionsBuilder() {
        CLIOptions options = CLIOptions.builder()
            .model(CLIOptions.MODEL_SONNET)
            .systemPrompt("You are a helpful assistant")
            .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
            .timeout(Duration.ofMinutes(5))
            .allowedTools(List.of("Read", "Write"))
            .maxTurns(10)
            .build();

        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(Path.of("."))
                .build()) {
            // Use client...
        }
    }

    // === index.md: "Multi-Turn Conversation" (Reactive tab) ===
    void multiTurnConversationReactive() {
        ClaudeAsyncClient client = ClaudeClient.async()
            .workingDirectory(Path.of("."))
            .build();

        // Multi-turn with elegant flatMap chaining
        client.connect("My favorite color is blue.").text()
            .doOnSuccess(System.out::println)
            .flatMap(r1 -> client.query("What is my favorite color?").text())
            .doOnSuccess(System.out::println)  // "blue"
            .flatMap(r2 -> client.query("Spell it backwards.").text())
            .doOnSuccess(System.out::println)  // "eulb"
            .subscribe();  // Non-blocking
    }
}
