/*
 * Module 09: Structured Outputs
 *
 * Learn how to get Claude to return structured JSON responses.
 *
 * Key concepts:
 * - JsonSchema for defining output structure
 * - Using jsonSchema with CLIOptions and ClaudeSyncClient
 * - Extracting structured output from ResultMessage (not AssistantMessage!)
 *
 * IMPORTANT: Structured output is returned in ResultMessage.structured_output,
 * NOT in the assistant message text. The assistant message may contain prose
 * explanation while the structured data comes in the result.
 *
 * Run with: mvn compile exec:java -pl module-09-structured-outputs
 */
package io.github.markpollack.tutorial.module09;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.parsing.ParsedMessage;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.JsonSchema;
import io.github.markpollack.claude.agent.sdk.types.Message;
import io.github.markpollack.claude.agent.sdk.types.ResultMessage;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StructuredOutputsExample {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        System.out.println("=== Module 09: Structured Outputs ===\n");

        // Example 1: Simple structured output
        simpleStructuredOutput();

        // Example 2: Complex nested structure
        nestedStructuredOutput();

        System.out.println("\n=== Done ===");
    }

    /**
     * Simple example: Get a structured answer with a numeric result.
     */
    static void simpleStructuredOutput() throws Exception {
        System.out.println("--- Example 1: Simple Structured Output ---\n");

        // Define the output schema: { "answer": number, "explanation": string }
        JsonSchema schema = JsonSchema.ofObject(
                Map.of(
                        "answer", Map.of("type", "number"),
                        "explanation", Map.of("type", "string")
                ),
                List.of("answer", "explanation")  // Required fields
        );

        // Build CLIOptions with jsonSchema
        CLIOptions options = CLIOptions.builder()
                .model(CLIOptions.MODEL_HAIKU)
                .jsonSchema(schema.toMap())
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build();

        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(Path.of("."))
                .build()) {

            client.connect("What is 15 * 7? Provide answer and explanation.");

            // Get structured output from ResultMessage
            Map<String, Object> structuredOutput = getStructuredOutput(client);

            if (structuredOutput != null) {
                System.out.println("Structured output: " + mapper.writeValueAsString(structuredOutput));
                System.out.println("Answer: " + structuredOutput.get("answer"));
                System.out.println("Explanation: " + structuredOutput.get("explanation"));
            } else {
                System.out.println("No structured output received!");
            }
        }
    }

    /**
     * Complex example: Get a structured list of items.
     */
    @SuppressWarnings("unchecked")
    static void nestedStructuredOutput() throws Exception {
        System.out.println("\n--- Example 2: Nested Structured Output ---\n");

        // Define schema for: { "languages": [{ "name": string, "year": number }] }
        JsonSchema schema = JsonSchema.ofObject(
                Map.of(
                        "languages", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "name", Map.of("type", "string"),
                                                "year", Map.of("type", "integer"),
                                                "creator", Map.of("type", "string")
                                        ),
                                        "required", List.of("name", "year")
                                )
                        )
                ),
                List.of("languages")
        );

        CLIOptions options = CLIOptions.builder()
                .model(CLIOptions.MODEL_HAIKU)
                .jsonSchema(schema.toMap())
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build();

        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(Path.of("."))
                .build()) {

            client.connect("List 3 programming languages with their creation year and creator.");

            // Get structured output from ResultMessage
            Map<String, Object> structuredOutput = getStructuredOutput(client);

            if (structuredOutput != null) {
                System.out.println("Structured output: " + mapper.writeValueAsString(structuredOutput));

                // Parse the nested structure
                List<Map<String, Object>> languages =
                        (List<Map<String, Object>>) structuredOutput.get("languages");

                System.out.println("\nParsed languages:");
                for (Map<String, Object> lang : languages) {
                    System.out.printf("  - %s (%s) by %s%n",
                            lang.get("name"),
                            lang.get("year"),
                            lang.getOrDefault("creator", "unknown")
                    );
                }
            } else {
                System.out.println("No structured output received!");
            }
        }
    }

    /**
     * Extract structured output from ResultMessage.
     *
     * IMPORTANT: When using --json-schema, the structured output comes in
     * ResultMessage.structured_output, NOT in the assistant message text.
     * The assistant may still produce prose text, but the validated JSON
     * structure is in the result.
     */
    static Map<String, Object> getStructuredOutput(ClaudeSyncClient client) {
        Iterator<ParsedMessage> response = client.receiveResponse();

        while (response.hasNext()) {
            ParsedMessage parsed = response.next();

            if (parsed.isRegularMessage()) {
                Message message = parsed.asMessage();

                // Structured output is in ResultMessage, not AssistantMessage
                if (message instanceof ResultMessage result) {
                    if (result.hasStructuredOutput()) {
                        return result.getStructuredOutputAsMap();
                    }
                }
            }
        }
        return null;
    }
}
