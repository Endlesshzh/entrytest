package org.example.config;

import lombok.Data;
import org.example.service.llm.LlmProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM Configuration Properties
 * Supports multiple LLM providers
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {

    /**
     * Primary LLM provider to use
     */
    private LlmProvider primaryProvider = LlmProvider.OLLAMA;

    /**
     * Request timeout in seconds
     */
    private int timeout = 60;

    /**
     * Temperature for generation (0.0 - 1.0)
     */
    private double temperature = 0.3;

    /**
     * Maximum tokens to generate
     */
    private int maxTokens = 2000;

    /**
     * OpenAI configuration
     */
    private OpenAIConfig openai;

    /**
     * Claude configuration
     */
    private ClaudeConfig claude;

    /**
     * Compass configuration
     */
    private CompassConfig compass;

    /**
     * Ollama configuration
     */
    private OllamaConfig ollama = new OllamaConfig();

    /**
     * vLLM configuration
     */
    private VllmConfig vllm;

    @Data
    public static class OpenAIConfig {
        private String apiUrl = "http://compass.llm.shopee.io/compass-api/v1/chat/completions";
        private String apiKey;
        private String model = "gpt-3.5-turbo";
    }

    @Data
    public static class ClaudeConfig {
        private String apiUrl = "http://compass.llm.shopee.io/compass-api/v1/messages";
        private String apiKey;
        private String model = "claude-3-sonnet-20240229";
    }

    @Data
    public static class CompassConfig {
        private String apiUrl = "http://compass.llm.shopee.io/compass-api/v1/chat/completions";
        private String apiKey;
        private String model = "compass-1";
    }

    @Data
    public static class OllamaConfig {
        private String apiUrl = "http://localhost:11434/api/generate";
        private String model = "llama2";
    }

    @Data
    public static class VllmConfig {
        private String apiUrl = "http://localhost:8000/v1/chat/completions";
        private String model = "meta-llama/Llama-2-7b-chat-hf";
    }
}
