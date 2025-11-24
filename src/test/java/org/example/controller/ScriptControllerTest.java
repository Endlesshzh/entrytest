package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.ScriptAnalysisRequest;
import org.example.model.ScriptAnalysisResult;
import org.example.model.ScriptExecutionRequest;
import org.example.model.ScriptExecutionResult;
import org.example.service.LlmAnalysisService;
import org.example.service.ScriptEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试类：ScriptController
 * 测试所有脚本相关的REST API接口
 */
@WebMvcTest(ScriptController.class)
@DisplayName("脚本控制器测试")
class ScriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScriptEngineService scriptEngineService;

    @MockBean
    private LlmAnalysisService llmAnalysisService;

    private ScriptExecutionRequest executionRequest;
    private ScriptExecutionResult executionResult;
    private ScriptAnalysisRequest analysisRequest;
    private ScriptAnalysisResult analysisResult;

    @BeforeEach
    void setUp() {
        // 准备执行请求
        executionRequest = new ScriptExecutionRequest();
        executionRequest.setScript("def value = redis.get('testkey')\nreturn value");
        executionRequest.setScriptName("test-script");
        executionRequest.setTestRun(false);

        // 准备执行结果
        executionResult = ScriptExecutionResult.builder()
                .success(true)
                .result("test-value")
                .error(null)
                .executionTime(15L)
                .script(executionRequest.getScript())
                .testRun(false)
                .build();

        // 准备分析请求
        analysisRequest = new ScriptAnalysisRequest();
        analysisRequest.setScript("def value = redis.get('testkey')\nreturn value");

        // 准备分析结果
        analysisResult = ScriptAnalysisResult.builder()
                .securityScore(85)
                .securityIssues(Arrays.asList("检测到keys()操作，可能影响性能"))
                .qualityScore(90)
                .qualityIssues(Arrays.asList("建议添加错误处理"))
                .performanceSuggestions(Arrays.asList("使用SCAN代替KEYS以提高性能"))
                .bestPractices(Arrays.asList("建议使用具体的键名而不是通配符查询"))
                .llmAnalysis("该脚本用于获取Redis键值，逻辑简单清晰。建议添加空值检查和异常处理。")
                .safeToExecute(true)
                .build();
    }

    @Test
    @DisplayName("测试执行脚本接口 - 成功场景")
    void testExecuteScript_Success() throws Exception {
        // Mock服务返回成功结果
        when(scriptEngineService.executeScript(anyString(), anyBoolean()))
                .thenReturn(executionResult);

        // 执行请求
        mockMvc.perform(post("/api/script/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executionRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").value("test-value"))
                .andExpect(jsonPath("$.executionTime").value(15))
                .andExpect(jsonPath("$.testRun").value(false))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("测试执行脚本接口 - 失败场景")
    void testExecuteScript_Failure() throws Exception {
        // Mock服务返回失败结果
        ScriptExecutionResult failureResult = ScriptExecutionResult.builder()
                .success(false)
                .result(null)
                .error("Script execution failed: Invalid script syntax")
                .executionTime(5L)
                .script(executionRequest.getScript())
                .testRun(false)
                .build();

        when(scriptEngineService.executeScript(anyString(), anyBoolean()))
                .thenReturn(failureResult);

        // 执行请求
        mockMvc.perform(post("/api/script/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executionRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Script execution failed: Invalid script syntax"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("测试执行脚本接口 - 异常场景")
    void testExecuteScript_Exception() throws Exception {
        // Mock服务抛出异常
        when(scriptEngineService.executeScript(anyString(), anyBoolean()))
                .thenThrow(new RuntimeException("Service error"));

        // 执行请求
        mockMvc.perform(post("/api/script/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executionRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("测试试运行脚本接口")
    void testTestScript() throws Exception {
        // 设置试运行模式
        executionRequest.setTestRun(true);
        executionResult.setTestRun(true);

        // Mock服务返回试运行结果
        when(scriptEngineService.executeScript(anyString(), eq(true)))
                .thenReturn(executionResult);

        // 执行请求
        mockMvc.perform(post("/api/script/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executionRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.testRun").value(true));
    }

    @Test
    @DisplayName("测试分析脚本接口 - 成功场景")
    void testAnalyzeScript_Success() throws Exception {
        // Mock服务返回分析结果
        when(llmAnalysisService.analyzeScript(anyString()))
                .thenReturn(analysisResult);

        // 执行请求
        mockMvc.perform(post("/api/script/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(analysisRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.securityScore").value(85))
                .andExpect(jsonPath("$.qualityScore").value(90))
                .andExpect(jsonPath("$.safeToExecute").value(true))
                .andExpect(jsonPath("$.securityIssues").isArray())
                .andExpect(jsonPath("$.qualityIssues").isArray())
                .andExpect(jsonPath("$.performanceSuggestions").isArray())
                .andExpect(jsonPath("$.bestPractices").isArray())
                .andExpect(jsonPath("$.llmAnalysis").exists());
    }

    @Test
    @DisplayName("测试分析脚本接口 - 失败场景")
    void testAnalyzeScript_Failure() throws Exception {
        // Mock服务返回失败结果
        ScriptAnalysisResult failureResult = ScriptAnalysisResult.builder()
                .securityScore(0)
                .qualityScore(0)
                .safeToExecute(false)
                .llmAnalysis("Analysis failed: LLM service unavailable")
                .build();

        when(llmAnalysisService.analyzeScript(anyString()))
                .thenReturn(failureResult);

        // 执行请求
        mockMvc.perform(post("/api/script/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(analysisRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.securityScore").value(0))
                .andExpect(jsonPath("$.safeToExecute").value(false));
    }

    @Test
    @DisplayName("测试分析脚本接口 - 异常场景")
    void testAnalyzeScript_Exception() throws Exception {
        // Mock服务抛出异常
        when(llmAnalysisService.analyzeScript(anyString()))
                .thenThrow(new RuntimeException("LLM service error"));

        // 执行请求
        mockMvc.perform(post("/api/script/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(analysisRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.securityScore").value(0))
                .andExpect(jsonPath("$.llmAnalysis").exists());
    }

    @Test
    @DisplayName("测试健康检查接口")
    void testHealth() throws Exception {
        // 执行请求
        mockMvc.perform(get("/api/script/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Script service is running"));
    }

    @Test
    @DisplayName("测试执行脚本接口 - 空脚本")
    void testExecuteScript_EmptyScript() throws Exception {
        executionRequest.setScript("");
        
        // 执行请求
        mockMvc.perform(post("/api/script/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executionRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("测试执行脚本接口 - 复杂脚本")
    void testExecuteScript_ComplexScript() throws Exception {
        String complexScript = """
                def keys = redis.keys('user:*')
                def users = []
                keys.each { key ->
                    def userData = redis.hgetAll(key)
                    if (userData && userData.age && Integer.parseInt(userData.age) > 18) {
                        users.add(userData)
                    }
                }
                return users
                """;
        
        executionRequest.setScript(complexScript);
        
        ScriptExecutionResult complexResult = ScriptExecutionResult.builder()
                .success(true)
                .result(Arrays.asList("user1", "user2"))
                .executionTime(50L)
                .script(complexScript)
                .testRun(false)
                .build();

        when(scriptEngineService.executeScript(eq(complexScript), anyBoolean()))
                .thenReturn(complexResult);

        // 执行请求
        mockMvc.perform(post("/api/script/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isArray());
    }

    @Test
    @DisplayName("测试分析脚本接口 - 包含安全问题的脚本")
    void testAnalyzeScript_WithSecurityIssues() throws Exception {
        analysisRequest.setScript("redis.flushall()");
        
        ScriptAnalysisResult unsafeResult = ScriptAnalysisResult.builder()
                .securityScore(20)
                .securityIssues(Arrays.asList("Contains forbidden pattern: FLUSHALL"))
                .qualityScore(50)
                .safeToExecute(false)
                .llmAnalysis("该脚本包含危险操作，不建议执行")
                .build();

        when(llmAnalysisService.analyzeScript(anyString()))
                .thenReturn(unsafeResult);

        // 执行请求
        mockMvc.perform(post("/api/script/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(analysisRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.securityScore").value(20))
                .andExpect(jsonPath("$.safeToExecute").value(false))
                .andExpect(jsonPath("$.securityIssues[0]").exists());
    }
}

