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
    private final OkHttpClient healthCheckClient;
    private final ObjectMapper objectMapper;

    public OllamaLlmService(LlmConfig llmConfig) {
        this.llmConfig = llmConfig;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(llmConfig.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(llmConfig.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(llmConfig.getTimeout(), TimeUnit.SECONDS)
                .build();
        // 为健康检查使用更短的超时时间（5秒）
        this.healthCheckClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
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
        // 检查配置是否存在
        if (llmConfig.getOllama() == null || llmConfig.getOllama().getApiUrl() == null) {
            log.debug("Ollama configuration is missing");
            return false;
        }

        try {
            String healthCheckUrl = llmConfig.getOllama().getApiUrl().replace("/api/generate", "/api/tags");
            Request request = new Request.Builder()
                    .url(healthCheckUrl)
                    .get()
                    .build();

            try (Response response = healthCheckClient.newCall(request).execute()) {
                boolean available = response.isSuccessful();
                if (available) {
                    log.debug("Ollama service is available at {}", healthCheckUrl);
                } else {
                    log.debug("Ollama service health check failed with status: {}", response.code());
                }
                return available;
            }
        } catch (java.net.ConnectException e) {
            log.debug("Ollama service not reachable at {}: {}", 
                    llmConfig.getOllama().getApiUrl(), e.getMessage());
            return false;
        } catch (java.net.SocketTimeoutException e) {
            log.debug("Ollama service health check timeout: {}", e.getMessage());
            return false;
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
