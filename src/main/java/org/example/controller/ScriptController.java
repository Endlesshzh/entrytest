package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;

    public ScriptController(ScriptEngineService scriptEngineService, LlmAnalysisService llmAnalysisService) {
        this.scriptEngineService = scriptEngineService;
        this.llmAnalysisService = llmAnalysisService;
        this.executorService = Executors.newCachedThreadPool();
        this.objectMapper = new ObjectMapper();
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
     * Analyze a script using LLM with streaming response (Server-Sent Events)
     */
    @PostMapping(value = "/analyze/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyzeScriptStream(@RequestBody ScriptAnalysisRequest request) {
        log.info("Streaming analysis for script with provider: {}", request.getProvider() != null ? request.getProvider() : "auto");
        
        SseEmitter emitter = new SseEmitter(300000L); // 5 minutes timeout
        
        CompletableFuture.runAsync(() -> {
            try {
                // Send initial status
                sendEvent(emitter, "status", "开始分析脚本...");
                
                // Perform basic analysis first
                sendEvent(emitter, "status", "执行基础静态分析...");
                ScriptAnalysisResult basicAnalysis = llmAnalysisService.performBasicAnalysis(request.getScript());
                
                // Send basic analysis results
                sendEvent(emitter, "basic_analysis", basicAnalysis);
                
                // Start LLM analysis
                sendEvent(emitter, "status", "开始LLM深度分析（这可能需要一些时间）...");
                
                // Perform LLM analysis
                ScriptAnalysisResult fullResult = llmAnalysisService.analyzeScript(
                        request.getScript(),
                        request.getProvider()
                );
                
                // Send final result
                sendEvent(emitter, "result", fullResult);
                sendEvent(emitter, "status", "分析完成");
                
                emitter.complete();
            } catch (Exception e) {
                log.error("Streaming analysis failed", e);
                try {
                    sendEvent(emitter, "error", "分析失败: " + e.getMessage());
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    log.error("Failed to send error event", ex);
                    emitter.completeWithError(ex);
                }
            }
        }, executorService);
        
        return emitter;
    }
    
    private void sendEvent(SseEmitter emitter, String event, Object data) throws IOException {
        try {
            // For SSE, we send data as-is for strings, or JSON for objects
            if (data instanceof String) {
                // String data is sent directly
                log.debug("Sending SSE event: {} with string data: {}", event, data);
                emitter.send(SseEmitter.event()
                        .name(event)
                        .data((String) data));
            } else {
                // Object data is serialized to JSON
                String jsonData = objectMapper.writeValueAsString(data);
                log.debug("Sending SSE event: {} with JSON data: {}", event, jsonData);
                emitter.send(SseEmitter.event()
                        .name(event)
                        .data(jsonData));
            }
        } catch (IOException e) {
            log.error("Failed to send SSE event", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to serialize SSE event data", e);
            throw new IOException("Failed to serialize event data", e);
        }
    }

    /**
     * 测试运行脚本（dry run）
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
