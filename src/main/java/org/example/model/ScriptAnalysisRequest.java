package org.example.model;

import lombok.Data;
import org.example.service.llm.LlmProvider;


/**
 * Request model for script analysis
 */
@Data
public class ScriptAnalysisRequest {

    /**
     * The script to analyze
     */
    private String script;

    /**
     * Optional LLM provider to use for analysis
     * If not specified, uses the primary provider from configuration
     */
    private LlmProvider provider;
}
