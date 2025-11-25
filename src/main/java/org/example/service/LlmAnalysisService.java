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
        log.info("开始分析脚本，LLM提供者: {}", provider != null ? provider : "auto");
        log.debug("分析步骤 1/4: 开始基础静态分析");

        // Perform basic static analysis first
        ScriptAnalysisResult basicAnalysis = performBasicAnalysis(script);
        log.debug("分析步骤 2/4: 基础静态分析完成，安全性评分: {}, 质量评分: {}", 
                basicAnalysis.getSecurityScore(), basicAnalysis.getQualityScore());

        // Enhance with LLM analysis
        log.debug("分析步骤 3/4: 开始LLM深度分析");
        try {
            LlmService llmService;
            
            if (provider != null) {
                // 如果指定了 provider，先尝试使用它
                log.debug("检查请求的LLM服务: {}", provider);
                LlmService requestedService = llmServiceFactory.getService(provider);
                
                // 检查请求的服务是否可用
                if (requestedService.isAvailable()) {
                    llmService = requestedService;
                    log.info("使用请求的LLM提供者: {}", provider);
                } else {
                    // 如果请求的服务不可用，记录警告并回退到可用的服务
                    log.warn("请求的LLM提供者 {} 不可用，回退到可用的提供者", provider);
                    llmService = llmServiceFactory.getFirstAvailableService();
                    log.info("回退到可用的LLM提供者: {}", llmService.getProvider());
                }
            } else {
                // 如果没有指定 provider，使用主服务（会自动回退）
                log.debug("使用主LLM服务（自动选择）");
                llmService = llmServiceFactory.getPrimaryService();
                log.info("使用主LLM提供者: {}", llmService.getProvider());
            }

            log.info("正在调用LLM服务进行分析，这可能需要一些时间...");
            long startTime = System.currentTimeMillis();
            String llmAnalysis = llmService.analyzeScript(script);
            long duration = System.currentTimeMillis() - startTime;
            log.info("LLM分析完成，耗时: {}ms", duration);
            
            basicAnalysis.setLlmAnalysis(llmAnalysis);
            log.debug("分析步骤 4/4: LLM深度分析完成");

            log.info("脚本分析完成，使用LLM提供者: {}", llmService.getProvider());
        } catch (IllegalStateException e) {
            // 没有可用的服务
            log.error("没有可用的LLM服务", e);
            basicAnalysis.setLlmAnalysis("LLM分析无法使用: 没有可用的LLM服务。请检查LLM服务配置和连接状态。");
        } catch (java.net.SocketTimeoutException e) {
            log.error("LLM分析超时", e);
            int timeout = llmServiceFactory.getLlmConfig().getTimeout();
            basicAnalysis.setLlmAnalysis("LLM分析超时: 请求时间超过" + timeout + "秒。\n\n" +
                    "可能的原因：\n" +
                    "1. LLM服务响应较慢（本地模型可能需要更长时间）\n" +
                    "2. 网络连接问题\n" +
                    "3. 模型正在加载中\n\n" +
                    "建议：\n" +
                    "- 检查LLM服务是否正常运行\n" +
                    "- 尝试使用更快的模型或云服务\n" +
                    "- 增加超时时间配置（当前: " + timeout + "秒，可在application.yml中修改llm.timeout）");
        } catch (java.io.IOException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("timeout")) {
                log.error("LLM分析超时（IO异常）", e);
                basicAnalysis.setLlmAnalysis("LLM分析超时: " + errorMsg + 
                        "\n\n请检查LLM服务是否正常运行，或尝试增加超时时间配置。");
            } else {
                log.error("LLM分析IO错误", e);
                basicAnalysis.setLlmAnalysis("LLM分析失败（IO错误）: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("LLM分析失败", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("timeout") || 
                errorMsg != null && errorMsg.contains("Read timed out")) {
                basicAnalysis.setLlmAnalysis("LLM分析超时: " + errorMsg + 
                        "\n\n建议检查LLM服务状态或增加超时时间配置。");
            } else {
                basicAnalysis.setLlmAnalysis("LLM分析无法使用: " + errorMsg);
            }
        }

        return basicAnalysis;
    }

    /**
     * 执行原始分析
     */
    public ScriptAnalysisResult performBasicAnalysis(String script) {
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
