package org.example.model;

import lombok.Data;

/**
 * Request model for script execution
 */
@Data
public class ScriptExecutionRequest {

    /**
     * The Groovy script to execute
     */
    private String script;

    /**
     * Optional script name/identifier
     */
    private String scriptName;

    /**
     * Whether this is a test run (dry run)
     */
    private boolean testRun = false;
}
