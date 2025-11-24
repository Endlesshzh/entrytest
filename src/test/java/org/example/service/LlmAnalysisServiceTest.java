package org.example.service;

import org.example.config.ScriptConfig;
import org.example.model.ScriptAnalysisResult;
import org.example.service.llm.LlmProvider;
import org.example.service.llm.LlmService;
import org.example.service.llm.LlmServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 单元测试类：LlmAnalysisService
 * 测试LLM分析服务的关键方法和流程
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LLM分析服务单元测试")
class LlmAnalysisServiceTest {

    @Mock
    private ScriptConfig scriptConfig;

    @Mock
    private LlmServiceFactory llmServiceFactory;

    @Mock
    private LlmService llmService;

    private LlmAnalysisService llmAnalysisService;

    @BeforeEach
    void setUp() {
        // 初始化配置 - 使用lenient避免不必要的stubbing警告
        lenient().when(scriptConfig.getForbiddenPatterns()).thenReturn(
                Arrays.asList("FLUSHALL", "FLUSHDB", "DEL *", "CONFIG")
        );

        // 创建服务实例
        llmAnalysisService = new LlmAnalysisService(scriptConfig, llmServiceFactory);
    }

    @Test
    @DisplayName("测试分析脚本 - 使用默认提供者")
    void testAnalyzeScript_DefaultProvider() throws Exception {
        // 准备测试数据
        String script = "def value = redis.get('test:key')\nreturn value";
        String llmAnalysis = "这是一个简单的Redis查询脚本，用于获取键值。";

        // Mock LLM服务
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        when(llmService.analyzeScript(script)).thenReturn(llmAnalysis);
        when(llmService.getProvider()).thenReturn(LlmProvider.OLLAMA);

        // 执行分析
        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertEquals(llmAnalysis, result.getLlmAnalysis());
        assertTrue(result.getSecurityScore() >= 0 && result.getSecurityScore() <= 100);
        assertTrue(result.getQualityScore() >= 0 && result.getQualityScore() <= 100);
        assertNotNull(result.getSecurityIssues());
        assertNotNull(result.getQualityIssues());
        assertNotNull(result.getPerformanceSuggestions());
        assertNotNull(result.getBestPractices());

        // 验证调用
        verify(llmServiceFactory, times(1)).getPrimaryService();
        verify(llmService, times(1)).analyzeScript(script);
    }

    @Test
    @DisplayName("测试分析脚本 - 指定提供者")
    void testAnalyzeScript_SpecificProvider() throws Exception {
        // 准备测试数据
        String script = "def value = redis.get('test:key')\nreturn value";
        String llmAnalysis = "这是一个安全的Redis查询脚本。";

        // Mock LLM服务
        when(llmServiceFactory.getService(LlmProvider.OPENAI)).thenReturn(llmService);
        when(llmService.analyzeScript(script)).thenReturn(llmAnalysis);
        when(llmService.getProvider()).thenReturn(LlmProvider.OPENAI);

        // 执行分析
        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script, LlmProvider.OPENAI);

        // 验证结果
        assertNotNull(result);
        assertEquals(llmAnalysis, result.getLlmAnalysis());

        // 验证调用
        verify(llmServiceFactory, times(1)).getService(LlmProvider.OPENAI);
        verify(llmService, times(1)).analyzeScript(script);
    }

    @Test
    @DisplayName("测试分析脚本 - LLM服务失败时的回退")
    void testAnalyzeScript_LlmServiceFailure() throws Exception {
        // 准备测试数据
        String script = "def value = redis.get('test:key')\nreturn value";

        // Mock LLM服务抛出异常
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        when(llmService.analyzeScript(script)).thenThrow(new RuntimeException("LLM service unavailable"));

        // 执行分析
        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果 - 应该返回基础分析结果
        assertNotNull(result);
        assertNotNull(result.getLlmAnalysis());
        assertTrue(result.getLlmAnalysis().contains("无法使用") || result.getLlmAnalysis().contains("unavailable"));
        // 基础分析应该仍然完成
        assertTrue(result.getSecurityScore() >= 0);
    }

    @Test
    @DisplayName("测试基础分析 - 安全脚本")
    void testPerformBasicAnalysis_SafeScript() {
        // 准备安全脚本
        String script = "def value = redis.get('test:key')\nreturn value";

        // Mock配置
        when(scriptConfig.getForbiddenPatterns()).thenReturn(
                Arrays.asList("FLUSHALL", "FLUSHDB", "DEL *", "CONFIG")
        );

        // 执行分析（通过公共方法间接测试）
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSecurityScore() > 0);
        assertTrue(result.isSafeToExecute());
        assertTrue(result.getSecurityIssues().isEmpty() || 
                   result.getSecurityIssues().stream().noneMatch(issue -> issue.contains("forbidden")));
    }

    @Test
    @DisplayName("测试基础分析 - 包含禁止模式的脚本")
    void testPerformBasicAnalysis_ForbiddenPattern() {
        // 准备包含禁止模式的脚本
        String script = "redis.flushall()";

        // Mock配置
        when(scriptConfig.getForbiddenPatterns()).thenReturn(
                Arrays.asList("FLUSHALL", "FLUSHDB", "DEL *", "CONFIG")
        );

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSecurityScore() < 100);
        assertFalse(result.isSafeToExecute());
        assertFalse(result.getSecurityIssues().isEmpty());
    }

    @Test
    @DisplayName("测试基础分析 - 包含系统操作的脚本")
    void testPerformBasicAnalysis_SystemOperations() {
        // 准备包含系统操作的脚本
        String script = "System.exit(0)";

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSecurityScore() < 100);
        assertFalse(result.isSafeToExecute());
        assertFalse(result.getSecurityIssues().isEmpty());
    }

    @Test
    @DisplayName("测试基础分析 - 包含文件操作的脚本")
    void testPerformBasicAnalysis_FileOperations() {
        // 准备包含文件操作的脚本
        String script = "new File('/tmp/test').delete()";

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSecurityScore() < 100);
        assertFalse(result.isSafeToExecute());
    }

    @Test
    @DisplayName("测试基础分析 - 包含动态类加载的脚本")
    void testPerformBasicAnalysis_DynamicClassLoading() {
        // 准备包含动态类加载的脚本
        String script = "Class.forName('java.lang.String')";

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSecurityScore() < 100);
        assertFalse(result.isSafeToExecute());
    }

    @Test
    @DisplayName("测试基础分析 - 过长脚本")
    void testPerformBasicAnalysis_LongScript() {
        // 准备过长脚本
        StringBuilder longScript = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longScript.append("def value").append(i).append(" = redis.get('key").append(i).append("')\n");
        }

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(longScript.toString());

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getQualityScore() < 100);
        assertFalse(result.getQualityIssues().isEmpty());
    }

    @Test
    @DisplayName("测试基础分析 - 未使用Redis操作的脚本")
    void testPerformBasicAnalysis_NoRedisOperations() {
        // 准备未使用Redis操作的脚本
        String script = "def a = 10\ndef b = 20\nreturn a + b";

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getQualityScore() < 100);
        assertFalse(result.getQualityIssues().isEmpty());
    }

    @Test
    @DisplayName("测试基础分析 - 包含KEYS操作的脚本")
    void testPerformBasicAnalysis_KeysOperation() {
        // 准备包含KEYS操作的脚本
        String script = "def keys = redis.keys('user:*')\nreturn keys";

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.getPerformanceSuggestions().isEmpty());
        assertTrue(result.getPerformanceSuggestions().stream()
                .anyMatch(suggestion -> suggestion.contains("KEYS") || suggestion.contains("SCAN")));
    }

    @Test
    @DisplayName("测试基础分析 - 包含循环的脚本")
    void testPerformBasicAnalysis_LoopOperation() {
        // 准备包含循环的脚本
        String script = """
                def keys = redis.keys('user:*')
                for (key in keys) {
                    def data = redis.hgetAll(key)
                }
                return keys
                """;

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.getPerformanceSuggestions().isEmpty());
    }

    @Test
    @DisplayName("测试基础分析 - 无注释的脚本")
    void testPerformBasicAnalysis_NoComments() {
        // 准备无注释的脚本
        String script = "def value = redis.get('test:key')\nreturn value";

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.getBestPractices().isEmpty());
    }

    @Test
    @DisplayName("测试获取可用提供者列表")
    void testGetAvailableProviders() {
        // Mock可用服务
        Map<LlmProvider, LlmService> availableServices = new HashMap<>();
        LlmService mockService = mock(LlmService.class);
        when(mockService.isAvailable()).thenReturn(true);
        availableServices.put(LlmProvider.OLLAMA, mockService);
        availableServices.put(LlmProvider.OPENAI, mockService);

        when(llmServiceFactory.getAvailableServices()).thenReturn(availableServices);

        // 执行获取
        List<String> providers = llmAnalysisService.getAvailableProviders();

        // 验证结果
        assertNotNull(providers);
        assertFalse(providers.isEmpty());
    }

    @Test
    @DisplayName("测试分析脚本 - 分数边界值")
    void testAnalyzeScript_ScoreBounds() {
        // 准备包含多个安全问题的脚本
        String script = "redis.flushall()\nSystem.exit(0)\nnew File('/tmp').delete()";

        // 执行分析
        when(llmServiceFactory.getPrimaryService()).thenReturn(llmService);
        try {
            when(llmService.analyzeScript(anyString())).thenReturn("分析完成");
        } catch (Exception e) {
            // 忽略异常
        }

        ScriptAnalysisResult result = llmAnalysisService.analyzeScript(script);

        // 验证分数在有效范围内
        assertNotNull(result);
        assertTrue(result.getSecurityScore() >= 0 && result.getSecurityScore() <= 100);
        assertTrue(result.getQualityScore() >= 0 && result.getQualityScore() <= 100);
    }
}

