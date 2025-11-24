package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.model.ScriptAnalysisRequest;
import org.example.model.ScriptAnalysisResult;
import org.example.model.ScriptExecutionRequest;
import org.example.model.ScriptExecutionResult;
import org.example.service.LlmAnalysisService;
import org.example.service.ScriptEngineService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
/**
 * REST API Controller for script operations
 */
@Slf4j
@RestController
@RequestMapping("/api/script")
@CrossOrigin(origins = "*")
public class ScriptController {

    private final ScriptEngineService scriptEngineService;
    private final LlmAnalysisService llmAnalysisService;

    public ScriptController(ScriptEngineService scriptEngineService, LlmAnalysisService llmAnalysisService) {
        this.scriptEngineService = scriptEngineService;
        this.llmAnalysisService = llmAnalysisService;
    }

    /**
     * Execute a script
     */
    @PostMapping("/execute")
    public ResponseEntity<ScriptExecutionResult> executeScript( @RequestBody ScriptExecutionRequest request) {
        log.info("Executing script: {}", request.getScriptName());

        try {
            ScriptExecutionResult result = scriptEngineService.executeScript(
                    request.getScript(),
                    request.isTestRun()
            );
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        } catch (Exception e) {
            log.error("Script execution failed", e);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ScriptExecutionResult.builder()
                            .success(false)
                            .error("Execution failed: " + e.getMessage())
                            .script(request.getScript())
                            .testRun(request.isTestRun())
                            .build());
        }
    }

    /**
     * Analyze a script using LLM
     */
    @PostMapping("/analyze")
    public ResponseEntity<ScriptAnalysisResult> analyzeScript( @RequestBody ScriptAnalysisRequest request) {
        log.info("Analyzing script with provider: {}", request.getProvider() != null ? request.getProvider() : "auto");

        try {
            ScriptAnalysisResult result = llmAnalysisService.analyzeScript(
                    request.getScript(),
                    request.getProvider()
            );
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        } catch (Exception e) {
            log.error("Script analysis failed", e);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ScriptAnalysisResult.builder()
                            .securityScore(0)
                            .securityIssues(java.util.Collections.emptyList())
                            .qualityScore(0)
                            .qualityIssues(java.util.Collections.emptyList())
                            .performanceSuggestions(java.util.Collections.emptyList())
                            .bestPractices(java.util.Collections.emptyList())
                            .safeToExecute(false)
                            .llmAnalysis("Analysis failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Test run a script (dry run)
     */
    @PostMapping("/test")
    public ResponseEntity<ScriptExecutionResult> testScript( @RequestBody ScriptExecutionRequest request) {
        log.info("Test running script: {}", request.getScriptName());

        request.setTestRun(true);
        return executeScript(request);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Script service is running");
    }

    /**
     * Get available LLM providers
     */
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getAvailableProviders() {
        log.info("Getting available LLM providers");
        
        List<String> providers = llmAnalysisService.getAvailableProviders();
        
        List<Map<String, String>> allProviders = new ArrayList<>();
        allProviders.add(createProviderInfo("OPENAI", "OpenAI", "cloud"));
        allProviders.add(createProviderInfo("CLAUDE", "Claude", "cloud"));
        allProviders.add(createProviderInfo("COMPASS", "Compass", "cloud"));
        allProviders.add(createProviderInfo("OLLAMA", "Ollama (Local)", "local"));
        allProviders.add(createProviderInfo("VLLM", "vLLM (Local)", "local"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("providers", providers);
        response.put("allProviders", allProviders);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    private Map<String, String> createProviderInfo(String value, String label, String type) {
        Map<String, String> info = new HashMap<>();
        info.put("value", value);
        info.put("label", label);
        info.put("type", type);
        return info;
    }
}
