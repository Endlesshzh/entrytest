package org.example.service.llm;

import lombok.extern.slf4j.Slf4j;
import org.example.config.LlmConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理 LLM 服务的工厂模式，提供 OPENAI、CLAUDE、COMPASS、OLLAMA、VLLM等大模型服务；
 * Manages multiple LLM providers and selects the appropriate one
 */
@Slf4j
@Component
public class LlmServiceFactory {

    private final Map<LlmProvider, LlmService> services;
    private final LlmConfig llmConfig;

    public LlmServiceFactory(
            LlmConfig llmConfig,
            OpenAILlmService openAILlmService,
            ClaudeLlmService claudeLlmService,
            CompassLlmService compassLlmService,
            OllamaLlmService ollamaLlmService,
            VllmLlmService vllmLlmService
    ) {
        this.llmConfig = llmConfig;
        this.services = new HashMap<>();

        services.put(LlmProvider.OPENAI, openAILlmService);
        services.put(LlmProvider.CLAUDE, claudeLlmService);
        services.put(LlmProvider.COMPASS, compassLlmService);
        services.put(LlmProvider.OLLAMA, ollamaLlmService);
        services.put(LlmProvider.VLLM, vllmLlmService);

        log.info("LLM Service Factory initialized with {} providers", services.size());
    }

    /**
     * Get LLM service by provider
     */
    public LlmService getService(LlmProvider provider) {
        LlmService service = services.get(provider);
        if (service == null) {
            throw new IllegalArgumentException("Unsupported LLM provider: " + provider);
        }
        return service;
    }

    /**
     * Get the default/primary LLM service based on configuration
     */
    public LlmService getPrimaryService() {
        LlmProvider primaryProvider = llmConfig.getPrimaryProvider();
        LlmService service = services.get(primaryProvider);

        if (service != null && service.isAvailable()) {
            log.debug("Using primary LLM provider: {}", primaryProvider);
            return service;
        }

        // Fallback to first available service
        log.warn("Primary provider {} not available, falling back to available provider", primaryProvider);
        return getFirstAvailableService();
    }

    /**
     * Get first available LLM service
     */
    public LlmService getFirstAvailableService() {
        for (Map.Entry<LlmProvider, LlmService> entry : services.entrySet()) {
            if (entry.getValue().isAvailable()) {
                log.info("Using available LLM provider: {}", entry.getKey());
                return entry.getValue();
            }
        }

        throw new IllegalStateException("No LLM service is available");
    }

    /**
     * Get all available services
     */
    public Map<LlmProvider, LlmService> getAvailableServices() {
        Map<LlmProvider, LlmService> available = new HashMap<>();
        for (Map.Entry<LlmProvider, LlmService> entry : services.entrySet()) {
            if (entry.getValue().isAvailable()) {
                available.put(entry.getKey(), entry.getValue());
            }
        }
        return available;
    }

    /**
     * Check if any service is available
     */
    public boolean hasAvailableService() {
        return services.values().stream().anyMatch(LlmService::isAvailable);
    }

    /**
     * Get LLM configuration
     */
    public LlmConfig getLlmConfig() {
        return llmConfig;
    }
}
