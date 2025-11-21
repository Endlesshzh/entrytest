package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result model for script analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptAnalysisResult {

    /**
     * Overall security score (0-100)
     */
    private int securityScore;

    /**
     * Security issues found
     */
    private List<String> securityIssues;

    /**
     * Code quality score (0-100)
     */
    private int qualityScore;

    /**
     * Code quality issues
     */
    private List<String> qualityIssues;

    /**
     * Performance suggestions
     */
    private List<String> performanceSuggestions;

    /**
     * Best practices recommendations
     */
    private List<String> bestPractices;

    /**
     * LLM analysis summary
     */
    private String llmAnalysis;

    /**
     * Whether the script is safe to execute
     */
    private boolean safeToExecute;
}
