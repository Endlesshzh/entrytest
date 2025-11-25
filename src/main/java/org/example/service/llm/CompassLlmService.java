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
 * Compass LLM Service Implementation
 * 支持百度文心一言等国内大模型
 */
@Slf4j
@Service
public class CompassLlmService implements LlmService {

    private final LlmConfig llmConfig;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CompassLlmService(LlmConfig llmConfig) {
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
        log.info("Analyzing script with Compass, model: {}", llmConfig.getCompass().getModel());

        String prompt = buildAnalysisPrompt(script);

        // Build Compass API request (compatible with OpenAI format)
        String requestBody = String.format(
                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"temperature\": %.1f, \"max_tokens\": %d}",
                llmConfig.getCompass().getModel(),
                escapeJson(prompt),
                llmConfig.getTemperature(),
                llmConfig.getMaxTokens()
        );

        Request request = new Request.Builder()
                .url(llmConfig.getCompass().getApiUrl())
                .header("Authorization", "Bearer " + llmConfig.getCompass().getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Compass API request failed: {} - {}", response.code(), errorBody);
                throw new IOException("Compass API request failed: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // Try OpenAI-compatible format first
            if (jsonNode.has("choices") && jsonNode.get("choices").size() > 0) {
                return jsonNode.get("choices").get(0).get("message").get("content").asText();
            }
            // Try alternative format
            else if (jsonNode.has("result")) {
                return jsonNode.get("result").asText();
            } else {
                throw new IOException("Invalid response format from Compass API");
            }
        }
    }

    @Override
    public LlmProvider getProvider() {
        return LlmProvider.COMPASS;
    }

    @Override
    public boolean isAvailable() {
        return llmConfig.getCompass() != null
                && llmConfig.getCompass().getApiKey() != null
                && !llmConfig.getCompass().getApiKey().isEmpty();
    }

    private String buildAnalysisPrompt(String script) {
        return "你是一个代码安全和质量分析专家。请分析以下将在Redis数据库上执行的Groovy脚本。\n\n" +
                "脚本:\n```groovy\n" + script + "\n```\n\n" +
                "请提供以下分析:\n" +
                "1. 安全性问题（如果有）\n" +
                "2. 代码质量评估\n" +
                "3. 性能考虑\n" +
                "4. 最佳实践建议\n" +
                "5. 总体安全性判断（安全/不安全执行）\n\n" +
                "请保持分析简洁，专注于最重要的要点。";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
