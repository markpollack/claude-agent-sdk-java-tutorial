/*
 * Module 01: Hello World
 *
 * This is the simplest possible Claude Agent SDK program.
 * It sends a single query and prints the response.
 *
 * Prerequisites:
 * - Claude CLI installed and authenticated (run: claude login)
 * - Java 21+
 *
 * Run with: mvn compile exec:java -pl module-01-hello-world
 */
package io.github.markpollack.tutorial.module01;

import io.github.markpollack.claude.agent.sdk.Query;

/**
 * Your first Claude Agent SDK program.
 *
 * The Query class provides the simplest way to interact with Claude.
 * It creates a new session for each query - no state is preserved
 * between calls.
 */
public class HelloWorld {

    public static void main(String[] args) {
        System.out.println("=== Module 01: Hello World ===\n");

        // The simplest possible query - one line of code
        String answer = Query.text("What is 2 + 2?");

        System.out.println("Question: What is 2 + 2?");
        System.out.println("Answer: " + answer);
        System.out.println("\n=== Done ===");
    }
}
