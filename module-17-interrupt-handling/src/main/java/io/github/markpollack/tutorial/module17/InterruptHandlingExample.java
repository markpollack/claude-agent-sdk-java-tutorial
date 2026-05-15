/*
 * Module 17: Interrupt Handling
 *
 * Demonstrates graceful handling of interrupts during Claude execution.
 * This module shows:
 * - Setting up JVM shutdown hooks for clean termination
 * - Using client.interrupt() to stop ongoing operations
 * - Proper resource cleanup on shutdown
 *
 * Run with: mvn compile exec:java -pl module-17-interrupt-handling
 */
package io.github.markpollack.tutorial.module17;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class InterruptHandlingExample {

    private static final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private static final AtomicReference<ClaudeSyncClient> activeClient = new AtomicReference<>();

    public static void main(String[] args) {
        System.out.println("=== Module 17: Interrupt Handling ===\n");

        // Register shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Shutdown] Signal received, initiating graceful shutdown...");
            shutdownRequested.set(true);

            ClaudeSyncClient client = activeClient.get();
            if (client != null) {
                try {
                    System.out.println("[Shutdown] Interrupting active Claude session...");
                    client.interrupt();
                    System.out.println("[Shutdown] Session interrupted successfully.");
                } catch (Exception e) {
                    System.out.println("[Shutdown] Error during interrupt: " + e.getMessage());
                }
            }
            System.out.println("[Shutdown] Cleanup complete.");
        }, "claude-shutdown-hook"));

        System.out.println("[Info] Shutdown hook registered.");
        System.out.println("[Info] Press Ctrl+C during execution to test interrupt handling.\n");

        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Store reference for shutdown hook
            activeClient.set(client);

            // Task 1: Quick task
            System.out.println("--- Task 1: Quick query ---");
            client.connect("What is 2 + 2? Answer in one word.");
            printResponse(client);

            // Task 2: Slightly longer task
            System.out.println("\n--- Task 2: Medium query ---");
            client.query("List three programming languages. Be very brief.");
            printResponse(client);

            // Task 3: Demonstrate interrupt check
            System.out.println("\n--- Task 3: Final query ---");
            if (shutdownRequested.get()) {
                System.out.println("[Info] Shutdown requested, skipping final query.");
            } else {
                client.query("What is the capital of France? One word answer.");
                printResponse(client);
            }

            // Clear reference before normal close
            activeClient.set(null);

        } catch (Exception e) {
            System.err.println("[Error] " + e.getMessage());
        }

        System.out.println("\n[Info] Session completed normally.");
        System.out.println("=== Done ===");
    }

    private static void printResponse(ClaudeSyncClient client) {
        if (shutdownRequested.get()) {
            System.out.println("[Info] Response skipped due to shutdown.");
            return;
        }

        Iterator<ParsedMessage> response = client.receiveResponse();
        while (response.hasNext() && !shutdownRequested.get()) {
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
