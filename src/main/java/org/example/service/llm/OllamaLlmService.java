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
        log.info("开始使用Ollama分析脚本，模型: {}, 超时设置: {}秒", 
                llmConfig.getOllama().getModel(), llmConfig.getTimeout());
        log.debug("构建分析提示词...");

        String prompt = buildAnalysisPrompt(script);
        log.debug("提示词构建完成，长度: {} 字符", prompt.length());

        // Build Ollama API request
        String requestBody = String.format(
                "{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false, \"options\": {\"temperature\": %.1f, \"num_predict\": %d}}",
                llmConfig.getOllama().getModel(),
                escapeJson(prompt),
                llmConfig.getTemperature(),
                llmConfig.getMaxTokens()
        );

        log.debug("发送请求到Ollama API: {}", llmConfig.getOllama().getApiUrl());
        Request request = new Request.Builder()
                .url(llmConfig.getOllama().getApiUrl())
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        long requestStartTime = System.currentTimeMillis();
        try (Response response = httpClient.newCall(request).execute()) {
            long requestDuration = System.currentTimeMillis() - requestStartTime;
            log.debug("收到Ollama API响应，耗时: {}ms", requestDuration);
            
            if (!response.isSuccessful()) {
                log.error("Ollama API请求失败，状态码: {}", response.code());
                throw new IOException("Ollama API request failed: " + response.code());
            }

            log.debug("解析响应内容...");
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("response")) {
                String analysis = jsonNode.get("response").asText();
                log.info("Ollama分析完成，响应长度: {} 字符，总耗时: {}ms", 
                        analysis.length(), requestDuration);
                return analysis;
            } else {
                log.error("Ollama API响应格式无效，缺少'response'字段");
                throw new IOException("Invalid response format from Ollama API");
            }
        } catch (java.net.SocketTimeoutException e) {
            long requestDuration = System.currentTimeMillis() - requestStartTime;
            log.error("Ollama API请求超时，已等待: {}ms，超时设置: {}秒", 
                    requestDuration, llmConfig.getTimeout());
            throw new IOException("Ollama API request timeout after " + 
                    llmConfig.getTimeout() + " seconds: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            long requestDuration = System.currentTimeMillis() - requestStartTime;
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                log.error("Ollama API请求超时（IO异常），已等待: {}ms", requestDuration);
            } else {
                log.error("Ollama API请求IO错误，已等待: {}ms", requestDuration, e);
            }
            throw e;
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
