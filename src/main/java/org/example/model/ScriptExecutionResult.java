package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result model for script execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptExecutionResult {

    /**
     * Whether the execution was successful
     */
    private boolean success;

    /**
     * The result data from script execution
     */
    private Object result;

    /**
     * Error message if execution failed
     */
    private String error;

    /**
     * Execution time in milliseconds
     */
    private long executionTime;

    /**
     * Script that was executed
     */
    private String script;

    /**
     * Whether this was a test run
     */
    private boolean testRun;
}
