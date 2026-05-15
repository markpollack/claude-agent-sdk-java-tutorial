/*
 * Module 13: Advanced Async Patterns
 *
 * Cross-turn handlers and advanced reactive patterns with ClaudeAsyncClient.
 * This module covers patterns beyond basic TurnSpec usage.
 *
 * Run with: mvn compile exec:java -pl module-13-async-advanced
 */
package io.github.markpollack.tutorial.module13;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeAsyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncAdvancedExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Module 13: Advanced Async Patterns ===\n");

        // Example 1: Cross-turn message handlers
        crossTurnHandlersExample();

        // Example 2: Error handling patterns
        errorHandlingExample();
    }

    /**
     * Cross-turn message handlers receive ALL messages across ALL turns.
     * Useful for logging, metrics, and monitoring.
     */
    static void crossTurnHandlersExample() throws InterruptedException {
        System.out.println("--- Cross-Turn Message Handlers ---\n");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Double> totalCost = new AtomicReference<>(0.0);
        AtomicReference<Integer> totalTurns = new AtomicReference<>(0);

        ClaudeAsyncClient client = ClaudeClient.async()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build();

        // Register cross-turn handlers BEFORE starting conversation
        client.onMessage(msg -> {
            System.out.println("[Handler] Message type: " + msg.getType());
        });

        client.onResult(result -> {
            totalCost.updateAndGet(c -> c + result.totalCostUsd());
            totalTurns.updateAndGet(t -> t + result.numTurns());
        });

        // Multi-turn conversation with handlers active
        client.connect("What is 2+2?").text()
                .doOnSuccess(r -> System.out.println("Turn 1: " + r))
                .flatMap(r1 -> client.query("What is 3+3?").text())
                .doOnSuccess(r -> System.out.println("Turn 2: " + r))
                .doOnTerminate(() -> {
                    System.out.printf("%nMetrics: Total cost: $%.6f, Total turns: %d%n",
                            totalCost.get(), totalTurns.get());
                    latch.countDown();
                })
                .subscribe();

        latch.await();
        System.out.println();
    }

    /**
     * Error handling in reactive streams.
     */
    static void errorHandlingExample() throws InterruptedException {
        System.out.println("--- Error Handling Patterns ---\n");

        CountDownLatch latch = new CountDownLatch(1);

        ClaudeAsyncClient client = ClaudeClient.async()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build();

        client.connect("Say hello").text()
                .doOnSuccess(System.out::println)
                .doOnError(error -> {
                    // Log the error
                    System.err.println("Stream error: " + error.getMessage());
                })
                .onErrorResume(error -> {
                    // Return fallback value
                    return reactor.core.publisher.Mono.just("(Error occurred, using fallback)");
                })
                .doOnTerminate(latch::countDown)
                .subscribe();

        latch.await();
        System.out.println("\n=== Done ===");
    }
}
