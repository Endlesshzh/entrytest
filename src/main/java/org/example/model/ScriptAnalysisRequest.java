package org.example.model;

import lombok.Data;


/**
 * Request model for script analysis
 */
@Data
public class ScriptAnalysisRequest {

    /**
     * The script to analyze
     */
    private String script;
}
