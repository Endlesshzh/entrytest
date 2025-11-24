package org.example.service;

import org.example.config.ScriptConfig;
import org.example.model.ScriptExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 单元测试类：ScriptEngineService
 * 测试脚本执行引擎的关键方法和流程
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("脚本执行引擎服务单元测试")
class ScriptEngineServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    private ScriptConfig scriptConfig;
    private ScriptEngineService scriptEngineService;

    @BeforeEach
    void setUp() {
        // 初始化配置
        scriptConfig = new ScriptConfig();
        scriptConfig.setMaxExecutionTime(5000L);
        scriptConfig.setCacheEnabled(true);
        scriptConfig.setCacheSize(100);
        scriptConfig.setForbiddenPatterns(Arrays.asList("FLUSHALL", "FLUSHDB", "DEL *", "CONFIG"));

        // Mock Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // 创建服务实例
        scriptEngineService = new ScriptEngineService(redisTemplate, scriptConfig);
    }

    @Test
    @DisplayName("测试执行简单脚本 - 成功场景")
    void testExecuteScript_SimpleScript_Success() {
        // 准备测试数据
        String script = "def value = redis.get('test:key')\nreturn value";
        when(valueOperations.get("test:key")).thenReturn("test-value");

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("test-value", result.getResult());
        assertNull(result.getError());
        assertTrue(result.getExecutionTime() > 0);
        assertFalse(result.isTestRun());
        assertEquals(script, result.getScript());
    }

    @Test
    @DisplayName("测试执行Hash查询脚本")
    void testExecuteScript_HashQuery() {
        // 准备测试数据
        String script = "def userData = redis.hgetAll('user:1')\nreturn userData";
        Map<Object, Object> userData = new HashMap<>();
        userData.put("name", "张三");
        userData.put("age", "25");
        when(hashOperations.entries("user:1")).thenReturn(userData);

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertTrue(result.getResult() instanceof Map);
    }

    @Test
    @DisplayName("测试执行键列表查询脚本")
    void testExecuteScript_KeysQuery() {
        // 准备测试数据
        String script = "def keys = redis.keys('user:*')\nreturn keys.size()";
        Set<String> keys = new HashSet<>(Arrays.asList("user:1", "user:2", "user:3"));
        when(redisTemplate.keys("user:*")).thenReturn(keys);

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals(3, result.getResult());
    }

    @Test
    @DisplayName("测试执行脚本 - 空脚本异常")
    void testExecuteScript_EmptyScript_ThrowsException() {
        // 执行空脚本
        ScriptExecutionResult result = scriptEngineService.executeScript("", false);

        // 验证结果
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("empty"));
    }

    @Test
    @DisplayName("测试执行脚本 - 包含禁止模式的脚本")
    void testExecuteScript_ForbiddenPattern_ThrowsException() {
        // 准备包含禁止模式的脚本
        String script = "redis.flushall()";

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("forbidden") || result.getError().contains("SecurityException"));
    }

    @Test
    @DisplayName("测试执行脚本 - 包含系统操作的脚本")
    void testExecuteScript_SystemOperations_ThrowsException() {
        // 准备包含系统操作的脚本
        String script = "System.exit(0)";

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    @DisplayName("测试执行脚本 - 包含文件操作的脚本")
    void testExecuteScript_FileOperations_ThrowsException() {
        // 准备包含文件操作的脚本
        String script = "new File('/tmp/test').delete()";

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    @DisplayName("测试执行脚本 - 试运行模式")
    void testExecuteScript_TestRun() {
        // 准备测试数据
        String script = "def value = redis.get('test:key')\nreturn value";
        when(valueOperations.get("test:key")).thenReturn("test-value");

        // 执行脚本（试运行模式）
        ScriptExecutionResult result = scriptEngineService.executeScript(script, true);

        // 验证结果
        assertTrue(result.isSuccess());
        assertTrue(result.isTestRun());
    }

    @Test
    @DisplayName("测试执行脚本 - 复杂脚本")
    void testExecuteScript_ComplexScript() {
        // 准备复杂脚本
        String script = """
                def keys = redis.keys('user:*')
                def users = []
                keys.each { key ->
                    def userData = redis.hgetAll(key)
                    if (userData && userData.name) {
                        users.add(userData.name)
                    }
                }
                return users
                """;

        // Mock数据
        Set<String> keys = new HashSet<>(Arrays.asList("user:1", "user:2"));
        when(redisTemplate.keys("user:*")).thenReturn(keys);

        Map<Object, Object> user1 = new HashMap<>();
        user1.put("name", "张三");
        Map<Object, Object> user2 = new HashMap<>();
        user2.put("name", "李四");
        when(hashOperations.entries("user:1")).thenReturn(user1);
        when(hashOperations.entries("user:2")).thenReturn(user2);

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
    }

    @Test
    @DisplayName("测试执行脚本 - 脚本语法错误")
    void testExecuteScript_SyntaxError() {
        // 准备语法错误的脚本
        String script = "def value = redis.get('test:key'\nreturn value"; // 缺少右括号

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    @DisplayName("测试执行脚本 - Redis操作返回null")
    void testExecuteScript_RedisReturnsNull() {
        // 准备测试数据
        String script = "def value = redis.get('test:key')\nreturn value";
        when(valueOperations.get("test:key")).thenReturn(null);

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertNull(result.getResult());
    }

    @Test
    @DisplayName("测试脚本缓存功能")
    void testScriptCaching() {
        // 准备测试数据
        String script = "def value = redis.get('test:key')\nreturn value";
        when(valueOperations.get("test:key")).thenReturn("test-value");

        // 第一次执行
        ScriptExecutionResult result1 = scriptEngineService.executeScript(script, false);
        assertTrue(result1.isSuccess());

        // 第二次执行（应该使用缓存）
        ScriptExecutionResult result2 = scriptEngineService.executeScript(script, false);
        assertTrue(result2.isSuccess());

        // 验证两次执行都成功
        assertEquals(result1.getResult(), result2.getResult());
    }

    @Test
    @DisplayName("测试执行脚本 - 计算操作")
    void testExecuteScript_Calculation() {
        // 准备计算脚本
        String script = "def a = 10\ndef b = 20\nreturn a + b";

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals(30, result.getResult());
    }

    @Test
    @DisplayName("测试执行脚本 - 条件判断")
    void testExecuteScript_Conditional() {
        // 准备条件判断脚本
        String script = """
                def value = redis.get('test:key')
                if (value == 'active') {
                    return 'enabled'
                } else {
                    return 'disabled'
                }
                """;
        when(valueOperations.get("test:key")).thenReturn("active");

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("enabled", result.getResult());
    }

    @Test
    @DisplayName("测试执行脚本 - 循环操作")
    void testExecuteScript_Loop() {
        // 准备循环脚本
        String script = """
                def sum = 0
                for (int i = 1; i <= 5; i++) {
                    sum += i
                }
                return sum
                """;

        // 执行脚本
        ScriptExecutionResult result = scriptEngineService.executeScript(script, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals(15, result.getResult());
    }
}

