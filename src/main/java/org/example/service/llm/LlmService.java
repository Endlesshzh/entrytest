package org.example.service.llm;

import org.example.model.ScriptAnalysisResult;

/**
 * LLM Service Interface
 * All LLM providers must implement this interface
 */
public interface LlmService {

    /**
     * Analyze script using LLM
     *
     * @param script The script to analyze
     * @return Analysis result
     */
    String analyzeScript(String script) throws Exception;

    /**
     * Get provider name
     *
     * @return Provider name
     */
    LlmProvider getProvider();

    /**
     * Check if the service is available
     *
     * @return true if available
     */
    boolean isAvailable();
}
