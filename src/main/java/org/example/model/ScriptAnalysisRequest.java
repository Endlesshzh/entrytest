package org.example.model;

import com.fasterxml.jackson.annotation.JsonSetter;
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
     * Supports both enum and string values (e.g., "COMPASS" or LlmProvider.COMPASS)
     * Spring Boot's Jackson will automatically convert string to enum
     */
    private LlmProvider provider;

    /**
     * Custom setter to handle string to enum conversion for compatibility
     */
    @JsonSetter("provider")
    public void setProviderFromString(Object providerValue) {
        if (providerValue == null) {
            this.provider = null;
            return;
        }
        
        if (providerValue instanceof LlmProvider) {
            this.provider = (LlmProvider) providerValue;
        } else if (providerValue instanceof String) {
            String providerStr = ((String) providerValue).toUpperCase();
            try {
                this.provider = LlmProvider.valueOf(providerStr);
            } catch (IllegalArgumentException e) {
                // If invalid, set to null and let the service handle it
                this.provider = null;
            }
        }
    }
}
