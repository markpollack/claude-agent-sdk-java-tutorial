/*
 * Module 03: ClaudeSyncClient
 *
 * Learn how to use ClaudeSyncClient for multi-turn conversations.
 * Unlike Query, clients maintain context between messages.
 *
 * Run with: mvn compile exec:java -pl module-03-sync-client
 */
package io.github.markpollack.tutorial.module03;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;

import java.nio.file.Path;

public class SyncClientExample {

    public static void main(String[] args) {
        System.out.println("=== Module 03: ClaudeSyncClient ===\n");

        // Try-with-resources ensures client is properly closed
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .build()) {

            // First question - connectText() starts session and returns text
            System.out.println("You: What is the capital of France?");
            String answer1 = client.connectText("What is the capital of France?");
            System.out.println("Claude: " + answer1);

            // Follow-up - queryText() continues conversation, Claude remembers context
            System.out.println("\nYou: What is the population of that city?");
            String answer2 = client.queryText("What is the population of that city?");
            System.out.println("Claude: " + answer2);

            // Another follow-up
            System.out.println("\nYou: What famous landmark is there?");
            String answer3 = client.queryText("What famous landmark is there?");
            System.out.println("Claude: " + answer3);
        }

        System.out.println("\n=== Done ===");
    }
}
