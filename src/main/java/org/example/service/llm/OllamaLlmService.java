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
 * Ollama Local LLM Service Implementation
 */
@Slf4j
@Service
public class OllamaLlmService implements LlmService {

    private final LlmConfig llmConfig;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaLlmService(LlmConfig llmConfig) {
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
        log.info("Analyzing script with Ollama, model: {}", llmConfig.getOllama().getModel());

        String prompt = buildAnalysisPrompt(script);

        // Build Ollama API request
        String requestBody = String.format(
                "{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false, \"options\": {\"temperature\": %.1f, \"num_predict\": %d}}",
                llmConfig.getOllama().getModel(),
                escapeJson(prompt),
                llmConfig.getTemperature(),
                llmConfig.getMaxTokens()
        );

        Request request = new Request.Builder()
                .url(llmConfig.getOllama().getApiUrl())
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama API request failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("response")) {
                return jsonNode.get("response").asText();
            } else {
                throw new IOException("Invalid response format from Ollama API");
            }
        }
    }

    @Override
    public LlmProvider getProvider() {
        return LlmProvider.OLLAMA;
    }

    @Override
    public boolean isAvailable() {
        try {
            Request request = new Request.Builder()
                    .url(llmConfig.getOllama().getApiUrl().replace("/api/generate", "/api/tags"))
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.debug("Ollama service not available: {}", e.getMessage());
            return false;
        }
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
