/*
 * Module 02: Query API
 *
 * Learn the full Query API including:
 * - Query.execute() for metadata (cost, tokens)
 * - QueryOptions for configuration
 * - Query.query() for streaming iteration
 *
 * Run with: mvn compile exec:java -pl module-02-query-api
 */
package io.github.markpollack.tutorial.module02;

import io.github.markpollack.claude.agent.sdk.Query;
import io.github.markpollack.claude.agent.sdk.QueryOptions;
import io.github.markpollack.claude.agent.sdk.types.QueryResult;

import java.time.Duration;

public class QueryApiExample {

    public static void main(String[] args) {
        System.out.println("=== Module 02: Query API ===\n");

        // Part 1: Query.execute() returns full result with metadata
        executeWithMetadata();

        // Part 2: QueryOptions for configuration
        queryWithOptions();

        System.out.println("\n=== Done ===");
    }

    /**
     * Query.execute() returns QueryResult with metadata.
     * Use this when you need cost, token usage, or session info.
     */
    static void executeWithMetadata() {
        System.out.println("--- Part 1: Query.execute() with metadata ---\n");

        QueryResult result = Query.execute("Write a haiku about Java programming");

        // Get the text response
        String text = result.text().orElse("(no response)");
        System.out.println("Response:\n" + text);

        // Access metadata
        System.out.println("\nMetadata:");
        System.out.printf("  Cost: $%.6f%n", result.metadata().cost().calculateTotal());
        System.out.printf("  Input tokens: %d%n", result.metadata().usage().inputTokens());
        System.out.printf("  Output tokens: %d%n", result.metadata().usage().outputTokens());
        System.out.printf("  Duration: %d ms%n", result.metadata().durationMs());
        System.out.printf("  Session ID: %s%n", result.metadata().sessionId());
    }

    /**
     * QueryOptions configures model, timeout, system prompt, etc.
     */
    static void queryWithOptions() {
        System.out.println("\n--- Part 2: QueryOptions configuration ---\n");

        QueryOptions options = QueryOptions.builder()
                .model("claude-sonnet-4-20250514")
                .appendSystemPrompt("Be concise. Answer in one sentence.")  // Add to defaults
                .timeout(Duration.ofMinutes(2))
                .build();

        String response = Query.text("What is dependency injection?", options);
        System.out.println("Response: " + response);
    }
}
