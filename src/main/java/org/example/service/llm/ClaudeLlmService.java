package org.example.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.example.config.LlmConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Claude (Anthropic) LLM Service Implementation
 */
@Slf4j
@Service
public class ClaudeLlmService implements LlmService {

    private final LlmConfig llmConfig;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ClaudeLlmService(LlmConfig llmConfig) {
        this.llmConfig = llmConfig;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(llmConfig.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(llmConfig.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(llmConfig.getTimeout(), TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String analyzeScript(String script) throws Exception {
        log.info("Analyzing script with Claude, model: {}", llmConfig.getClaude().getModel());

        String prompt = buildAnalysisPrompt(script);

        // Build Claude API request
        String requestBody = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": %d, \"temperature\": %.1f}",
                llmConfig.getClaude().getModel(),
                escapeJson(prompt),
                llmConfig.getMaxTokens(),
                llmConfig.getTemperature()
        );

        Request request = new Request.Builder()
                .url(llmConfig.getClaude().getApiUrl())
                .header("Authorization", "Bearer " + llmConfig.getClaude().getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Claude API request failed: {} - {}", response.code(), errorBody);
                throw new IOException("Claude API request failed: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // Try OpenAI-compatible format first (Compass proxy format)
            if (jsonNode.has("choices") && jsonNode.get("choices").size() > 0) {
                return jsonNode.get("choices").get(0).get("message").get("content").asText();
            }
            // Try official Claude API format
            else if (jsonNode.has("content") && jsonNode.get("content").size() > 0) {
                return jsonNode.get("content").get(0).get("text").asText();
            } else {
                throw new IOException("Invalid response format from Claude API: " + responseBody);
            }
        }
    }

    @Override
    public LlmProvider getProvider() {
        return LlmProvider.CLAUDE;
    }

    @Override
    public boolean isAvailable() {
        return llmConfig.getClaude() != null
                && llmConfig.getClaude().getApiKey() != null
                && !llmConfig.getClaude().getApiKey().isEmpty();
    }

    private String buildAnalysisPrompt(String script) {
        return "You are a code security and quality analyst. Analyze the following Groovy script that will be executed against a Redis database.\n\n" +
                "Script:\n```groovy\n" + script + "\n```\n\n" +
                "Please analyze this script and provide:\n" +
                "1. Security concerns (if any)\n" +
                "2. Code quality assessment\n" +
                "3. Performance considerations\n" +
                "4. Best practices recommendations\n" +
                "5. Overall safety verdict (safe/unsafe to execute)\n\n" +
                "Keep your analysis concise and focused on the most important points.";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
