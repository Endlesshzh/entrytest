package org.example;

import org.example.model.ScriptAnalysisRequest;
import org.example.model.ScriptExecutionRequest;

/**
 * 测试工具类
 * 提供测试中常用的辅助方法
 */
public class TestUtils {

    /**
     * 创建简单的脚本执行请求
     */
    public static ScriptExecutionRequest createSimpleExecutionRequest(String script) {
        ScriptExecutionRequest request = new ScriptExecutionRequest();
        request.setScript(script);
        request.setScriptName("test-script");
        request.setTestRun(false);
        return request;
    }

    /**
     * 创建试运行脚本请求
     */
    public static ScriptExecutionRequest createTestRunRequest(String script) {
        ScriptExecutionRequest request = new ScriptExecutionRequest();
        request.setScript(script);
        request.setScriptName("test-run-script");
        request.setTestRun(true);
        return request;
    }

    /**
     * 创建脚本分析请求
     */
    public static ScriptAnalysisRequest createAnalysisRequest(String script) {
        ScriptAnalysisRequest request = new ScriptAnalysisRequest();
        request.setScript(script);
        return request;
    }

    /**
     * 获取示例脚本 - 简单查询
     */
    public static String getSimpleQueryScript() {
        return "def value = redis.get('testkey')\nreturn value";
    }

    /**
     * 获取示例脚本 - Hash查询
     */
    public static String getHashQueryScript() {
        return "def userData = redis.hgetAll('user:1001')\nreturn userData";
    }

    /**
     * 获取示例脚本 - 键列表查询
     */
    public static String getKeysQueryScript() {
        return "def keys = redis.keys('user:*')\nreturn keys";
    }

    /**
     * 获取示例脚本 - 复杂查询
     */
    public static String getComplexQueryScript() {
        return """
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
    }

    /**
     * 获取包含安全问题的脚本
     */
    public static String getUnsafeScript() {
        return "redis.flushall()";
    }
}

