/*
 * Module 06: CLI Options
 *
 * Learn how to configure Claude clients using the fluent builder and CLIOptions.
 *
 * Two approaches:
 * 1. Fluent Builder: Configure everything via ClaudeClient.sync() methods
 * 2. CLIOptions: Pre-build options separately, useful for sharing/loading config
 *
 * Run with: mvn compile exec:java -pl module-06-cli-options
 */
package io.github.markpollack.tutorial.module06;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;

import java.nio.file.Path;
import java.time.Duration;

public class CLIOptionsExample {

    public static void main(String[] args) {
        System.out.println("=== Module 06: CLI Options ===\n");

        // Approach 1: Fluent builder (all-in-one)
        fluentBuilderApproach();

        // Approach 2: Pre-built CLIOptions (reusable config)
        cliOptionsApproach();

        System.out.println("\n=== Done ===");
    }

    /**
     * Approach 1: Configure everything via the fluent builder.
     * Best for simple, one-off configurations.
     */
    static void fluentBuilderApproach() {
        System.out.println("--- Approach 1: Fluent Builder ---\n");

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)                      // Fast model for demos
                .appendSystemPrompt("Be concise. Answer in one sentence.")  // Add to defaults
                .timeout(Duration.ofMinutes(2))                     // Operation timeout
                .maxTurns(5)                                        // Limit conversation turns
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)  // Skip permission prompts
                .build()) {

            String answer = client.connectText("What is Java?");
            System.out.println("Claude: " + answer);
        }
    }

    /**
     * Approach 2: Build CLIOptions separately, then pass to client.
     * Best when options are loaded from config or shared across clients.
     */
    static void cliOptionsApproach() {
        System.out.println("\n--- Approach 2: Pre-built CLIOptions ---\n");

        // Build options separately (could load from config file)
        CLIOptions options = CLIOptions.builder()
                .model(CLIOptions.MODEL_HAIKU)
                .appendSystemPrompt("Answer like a pirate.")  // Add to defaults
                .maxTokens(500)                               // Limit response length
                .maxBudgetUsd(0.10)                           // Limit cost to 10 cents
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build();

        // Pass pre-built options - only session config available now
        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(Path.of("."))
                .timeout(Duration.ofMinutes(1))
                // Note: .model() is NOT available here - already in options!
                .build()) {

            String answer = client.connectText("What is the best programming language?");
            System.out.println("Claude: " + answer);
        }
    }
}
