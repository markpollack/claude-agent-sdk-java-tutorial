/*
 * Code fragments for Part 2: Configuration (modules 05-08).
 * If this file doesn't compile, the docs have invalid code.
 */
package io.github.markpollack.tutorial.fragments;

import io.github.markpollack.claude.agent.sdk.ClaudeClient;
import io.github.markpollack.claude.agent.sdk.ClaudeSyncClient;
import io.github.markpollack.claude.agent.sdk.config.PermissionMode;
import io.github.markpollack.claude.agent.sdk.transport.CLIOptions;
import io.github.markpollack.claude.agent.sdk.types.JsonSchema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Configuration code fragments from tutorial modules 05-08.
 */
public class ConfigurationFragments {

    // === 06-cli-options.md: "Approach 1: Fluent Builder" ===
    void fluentBuilder() {
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .appendSystemPrompt("Be concise. Answer in one sentence.")  // Add to defaults
                .timeout(Duration.ofMinutes(2))
                .maxTurns(5)
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            String answer = client.connectText("What is Java?");
            System.out.println(answer);
        }
    }

    // === 06-cli-options.md: "Approach 2: Pre-built CLIOptions" ===
    void prebuiltOptions() {
        CLIOptions options = CLIOptions.builder()
            .model(CLIOptions.MODEL_HAIKU)
            .appendSystemPrompt("Answer like a pirate.")  // Add to defaults
            .maxTokens(500)
            .maxBudgetUsd(0.10)
            .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
            .build();

        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(Path.of("."))
                .timeout(Duration.ofMinutes(1))
                .build()) {

            String answer = client.connectText("What is the best programming language?");
            System.out.println(answer);
        }
    }

    // === 07-tool-permissions.md: "Approach 1: Allowed Tools" ===
    void allowedTools() {
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .allowedTools(List.of("Read", "Grep"))  // ONLY these tools
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Claude can read files and search, but cannot write or execute
            String answer = client.connectText("What files are in the current directory?");
            System.out.println(answer);
        }
    }

    // === 07-tool-permissions.md: "Approach 2: Disallowed Tools" ===
    void disallowedTools() {
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .model(CLIOptions.MODEL_HAIKU)
                .disallowedTools(List.of("Bash", "Write", "Edit"))  // Block these
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Claude can read files but cannot execute commands or modify files
            String answer = client.connectText("Read the pom.xml and tell me the project name.");
            System.out.println(answer);
        }
    }

    // === 07-permission-modes.md: "Permission Mode Examples" ===
    void permissionModes() {
        // BYPASS_PERMISSIONS - most common for automated scripts
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .build()) {

            // Claude executes tools without prompting
            String answer = client.connectText("List files in the current directory");
            System.out.println(answer);
        }
    }

    // === 07-permission-modes.md: "Combining with Tool Permissions" ===
    void combiningWithToolPermissions() {
        try (ClaudeSyncClient client = ClaudeClient.sync()
                .workingDirectory(Path.of("."))
                .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
                .allowedTools(List.of("Read", "Grep", "Glob"))  // Only these tools
                .build()) {

            // Claude can only read, not modify
            String answer = client.connectText("What's in the README?");
            System.out.println(answer);
        }
    }

    // === 08-structured-outputs.md: "JsonSchema" ===
    void structuredOutputs() throws Exception {
        JsonSchema schema = JsonSchema.ofObject(
            Map.of(
                "answer", Map.of("type", "number"),
                "explanation", Map.of("type", "string")
            ),
            List.of("answer", "explanation")
        );

        CLIOptions options = CLIOptions.builder()
            .model(CLIOptions.MODEL_HAIKU)
            .jsonSchema(schema.toMap())
            .permissionMode(PermissionMode.BYPASS_PERMISSIONS)
            .build();

        try (ClaudeSyncClient client = ClaudeClient.sync(options)
                .workingDirectory(Path.of("."))
                .build()) {

            String response = client.connectText("What is 15 * 7? Provide answer and explanation.");

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);
            int answer = json.get("answer").asInt();
            String explanation = json.get("explanation").asText();

            System.out.println("Answer: " + answer);
            System.out.println("Explanation: " + explanation);
        }
    }

    // === 08-structured-outputs.md: "Nested Schema" ===
    void nestedSchema() {
        JsonSchema schema = JsonSchema.ofObject(
            Map.of(
                "languages", Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "name", Map.of("type", "string"),
                            "year", Map.of("type", "integer")
                        ),
                        "required", List.of("name", "year")
                    )
                )
            ),
            List.of("languages")
        );
    }
}
