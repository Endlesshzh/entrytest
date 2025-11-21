package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Script Engine Configuration Properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "script")
public class ScriptConfig {

    /**
     * Maximum script execution time in milliseconds
     */
    private long maxExecutionTime = 5000;

    /**
     * Enable/disable script caching
     */
    private boolean cacheEnabled = true;

    /**
     * Maximum number of cached scripts
     */
    private int cacheSize = 100;

    /**
     * Allowed Redis commands (whitelist)
     */
    private List<String> allowedCommands;

    /**
     * Forbidden patterns in scripts
     */
    private List<String> forbiddenPatterns;
}
