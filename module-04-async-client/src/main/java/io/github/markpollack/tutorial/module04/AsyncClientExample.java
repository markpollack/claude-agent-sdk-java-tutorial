/*
 * Module 04: ClaudeAsyncClient
 *
 * Reactive, composable, non-blocking chains with Project Reactor.
 * Uses TurnSpec pattern for elegant response handling.
 *
 * Run with: mvn compile exec:java -pl module-04-async-client
 */
package io.github.markpollack.tutorial.module04;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeAsyncClient;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public class AsyncClientExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Module 04: ClaudeAsyncClient ===\n");

        ClaudeAsyncClient client = ClaudeClient.async()
                .workingDirectory(Path.of("."))
                .build();

        // Latch to wait for async completion (for demo purposes only)
        CountDownLatch latch = new CountDownLatch(1);

        // Multi-turn conversation with elegant flatMap chaining
        // Each .text() returns Mono<String>, enabling the chain
        System.out.println("You: What is the capital of France?");
        client.connect("What is the capital of France?").text()
                .doOnSuccess(response -> System.out.println("Claude: " + response))

                .flatMap(r1 -> {
                    System.out.println("\nYou: What is the population of that city?");
                    return client.query("What is the population of that city?").text();
                })
                .doOnSuccess(response -> System.out.println("Claude: " + response))

                .flatMap(r2 -> {
                    System.out.println("\nYou: What famous landmark is there?");
                    return client.query("What famous landmark is there?").text();
                })
                .doOnSuccess(response -> System.out.println("Claude: " + response))

                .doOnTerminate(latch::countDown)
                .subscribe();

        // Wait for completion (in real reactive apps, this isn't needed)
        latch.await();
        System.out.println("\n=== Done ===");
    }
}
