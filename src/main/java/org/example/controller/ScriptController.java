package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.model.ScriptAnalysisRequest;
import org.example.model.ScriptAnalysisResult;
import org.example.model.ScriptExecutionRequest;
import org.example.model.ScriptExecutionResult;
import org.example.service.LlmAnalysisService;
import org.example.service.ScriptEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Script execution failed", e);
            return ResponseEntity.ok(ScriptExecutionResult.builder()
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
        log.info("Analyzing script");

        try {
            ScriptAnalysisResult result = llmAnalysisService.analyzeScript(request.getScript());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Script analysis failed", e);
            return ResponseEntity.ok(ScriptAnalysisResult.builder()
                    .securityScore(0)
                    .qualityScore(0)
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
}
