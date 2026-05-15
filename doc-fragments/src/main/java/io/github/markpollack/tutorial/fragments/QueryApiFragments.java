/*
 * Code fragments for java.md documentation.
 * Each method corresponds to a code block in the docs.
 *
 * If this file doesn't compile, the docs have invalid code.
 */
package io.github.markpollack.tutorial.fragments;

import io.github.markpollack.claude.agent.sdk.Query;
import io.github.markpollack.claude.agent.sdk.QueryOptions;
import io.github.markpollack.claude.agent.sdk.types.QueryResult;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.Message;

import java.time.Duration;

/**
 * Query API code fragments from java.md reference documentation.
 */
public class QueryApiFragments {

    // === java.md: "Quick Start" section ===
    void quickStart() {
        String answer = Query.text("What is 2+2?");
        System.out.println(answer);
    }

    // === java.md: "Full Results" section ===
    void fullResults() {
        QueryResult result = Query.execute("Explain Java");
        System.out.println("Cost: $" + result.metadata().cost().calculateTotal());
    }

    // === java.md: "With Options" section ===
    void withOptions() {
        String response = Query.text("Explain quantum computing",
            QueryOptions.builder()
                .model("claude-sonnet-4-5-20250929")
                .systemPrompt("Be concise")
                .timeout(Duration.ofMinutes(5))
                .build());
        System.out.println(response);
    }

    // === java.md: "Streaming-style processing" section ===
    void streamingStyle() {
        for (Message msg : Query.query("Explain recursion")) {
            if (msg instanceof AssistantMessage assistant) {
                assistant.getTextContent().ifPresent(System.out::print);
            }
        }
    }
}
