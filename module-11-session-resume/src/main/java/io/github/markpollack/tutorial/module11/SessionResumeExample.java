/*
 * Module 11: Session Resume
 *
 * Demonstrates resuming a previous conversation using its session ID.
 * The session ID is extracted from ResultMessage after a completed turn.
 *
 * Run with: mvn compile exec:java -pl module-11-session-resume
 */
package io.github.markpollack.tutorial.module11;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.AssistantMessage;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.Iterator;

public class SessionResumeExample {

    public static void main(String[] args) {
        System.out.println("=== Module 11: Session Resume ===\n");

        String savedSessionId;

        // Session 1: Create initial conversation and save the session ID
        System.out.println("--- Session 1: Creating conversation ---");
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            client.connect("Remember this secret code: ALPHA-7749. " +
                    "I will ask you about it later.");
            savedSessionId = processAndGetSessionId(client);
            System.out.println("Session ID saved: " + savedSessionId);
        }

        // Simulate time passing or application restart
        System.out.println("\n--- Application restarted ---\n");

        // Session 2: Resume the previous conversation using --resume flag
        System.out.println("--- Session 2: Resuming conversation ---");

        // Build CLIOptions with resume(sessionId) to use --resume flag
        CLIOptions resumeOptions = CLIOptions.builder()
                .model(CLIOptions.MODEL_HAIKU)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .resume(savedSessionId)  // Pass session ID via --resume flag
                .build();

        try (ClaudeSyncClient client = ClaudeClient.sync(resumeOptions)
                .workingDirectory(Path.of("."))
                .build()) {

            // With --resume, connect() continues the previous session
            client.connect("What was the secret code I told you?");
            processAndGetSessionId(client);
        }

        System.out.println("\n=== Done ===");
    }

    /**
     * Processes response messages and extracts the session ID from ResultMessage.
     */
    private static String processAndGetSessionId(ClaudeSyncClient client) {
        String sessionId = null;
        Iterator<ParsedMessage> response = client.receiveResponse();

        while (response.hasNext()) {
            ParsedMessage msg = response.next();
            if (msg.isRegularMessage()) {
                if (msg.asMessage() instanceof AssistantMessage am) {
                    am.getTextContent().ifPresent(text ->
                        System.out.println("Claude: " + text));
                } else if (msg.asMessage() instanceof ResultMessage rm) {
                    sessionId = rm.sessionId();
                    System.out.printf("  [Turns: %d, Session: %s]%n",
                        rm.numTurns(), sessionId);
                }
            }
        }
        return sessionId;
    }
}
