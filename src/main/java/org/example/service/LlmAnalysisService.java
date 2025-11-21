package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.ScriptConfig;
import org.example.model.ScriptAnalysisResult;
import org.example.service.llm.LlmProvider;
import org.example.service.llm.LlmService;
import org.example.service.llm.LlmServiceFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 本类用于分析和执行LLM服务
 * 支持多LLM并自动回退
 */
@Slf4j
@Service
public class LlmAnalysisService {

    private final ScriptConfig scriptConfig;
    private final LlmServiceFactory llmServiceFactory;

    public LlmAnalysisService(
            ScriptConfig scriptConfig,
            LlmServiceFactory llmServiceFactory
    ) {
        this.scriptConfig = scriptConfig;
        this.llmServiceFactory = llmServiceFactory;
    }

    /**
     * 分析和执行LLM服务
     */
    public ScriptAnalysisResult analyzeScript(String script) {
        return analyzeScript(script, null);
    }

    /**
     * 分析和执行LLM服务，指定LLM提供者
     */
    public ScriptAnalysisResult analyzeScript(String script, LlmProvider provider) {
        log.info("Analyzing script with LLM, provider: {}", provider != null ? provider : "auto");

        // Perform basic static analysis first
        ScriptAnalysisResult basicAnalysis = performBasicAnalysis(script);

        // Enhance with LLM analysis
        try {
            LlmService llmService = provider != null
                    ? llmServiceFactory.getService(provider)
                    : llmServiceFactory.getPrimaryService();

            String llmAnalysis = llmService.analyzeScript(script);
            basicAnalysis.setLlmAnalysis(llmAnalysis);

            log.info("LLM analysis completed successfully using {}", llmService.getProvider());
        } catch (Exception e) {
            log.error("LLM分析失败，使用原始分析", e);
            basicAnalysis.setLlmAnalysis("LLM分析无法使用: " + e.getMessage());
        }

        return basicAnalysis;
    }

    /**
     * 执行原始分析
     */
    private ScriptAnalysisResult performBasicAnalysis(String script) {
        List<String> securityIssues = new ArrayList<>();
        List<String> qualityIssues = new ArrayList<>();
        List<String> performanceSuggestions = new ArrayList<>();
        List<String> bestPractices = new ArrayList<>();

        int securityScore = 100;
        int qualityScore = 100;
        boolean safeToExecute = true;

        // Check for forbidden patterns
        String upperScript = script.toUpperCase();
        for (String pattern : scriptConfig.getForbiddenPatterns()) {
            if (upperScript.contains(pattern.toUpperCase())) {
                securityIssues.add("Contains forbidden pattern: " + pattern);
                securityScore -= 20;
                safeToExecute = false;
            }
        }

        // Check for dangerous operations
        if (script.contains("System.exit") || script.contains("Runtime.getRuntime")) {
            securityIssues.add("Contains system-level operations");
            securityScore -= 30;
            safeToExecute = false;
        }

        if (script.contains("java.io.File") || script.contains("new File")) {
            securityIssues.add("Contains file system operations");
            securityScore -= 25;
            safeToExecute = false;
        }

        if (script.contains("Class.forName") || script.contains("ClassLoader")) {
            securityIssues.add("Contains dynamic class loading");
            securityScore -= 20;
            safeToExecute = false;
        }

        // Check for code quality issues
        if (script.length() > 1000) {
            qualityIssues.add("Script is too long (>1000 characters), consider breaking it down");
            qualityScore -= 10;
        }

        if (!script.contains("redis.")) {
            qualityIssues.add("Script doesn't appear to use Redis operations");
            qualityScore -= 15;
        }

        // Check for performance issues
        if (script.contains("redis.keys(")) {
            performanceSuggestions.add("Avoid using KEYS command in production, use SCAN instead");
        }

        if (script.contains("while") || script.contains("for")) {
            performanceSuggestions.add("Be cautious with loops, they may cause performance issues");
        }

        // Best practices
        if (!script.contains("//") && !script.contains("/*")) {
            bestPractices.add("Add comments to explain script logic");
        }

        if (script.contains("redis.set") && !script.contains("redis.get")) {
            bestPractices.add("Consider reading data before writing to avoid overwriting");
        }

        // Ensure scores are within bounds
        securityScore = Math.max(0, Math.min(100, securityScore));
        qualityScore = Math.max(0, Math.min(100, qualityScore));

        return ScriptAnalysisResult.builder()
                .securityScore(securityScore)
                .securityIssues(securityIssues)
                .qualityScore(qualityScore)
                .qualityIssues(qualityIssues)
                .performanceSuggestions(performanceSuggestions)
                .bestPractices(bestPractices)
                .safeToExecute(safeToExecute)
                .build();
    }

    /**
     * Get available LLM providers
     */
    public List<String> getAvailableProviders() {
        return llmServiceFactory.getAvailableServices().keySet().stream()
                .map(LlmProvider::getDisplayName)
                .collect(Collectors.toList());
    }
}
