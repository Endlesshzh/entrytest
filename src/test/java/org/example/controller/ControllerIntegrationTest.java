package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.ScriptAnalysisRequest;
import org.example.model.ScriptExecutionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 集成测试类：测试所有Controller的集成场景
 * 注意：此测试需要Redis服务运行
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("控制器集成测试")
class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("集成测试：完整的脚本执行流程")
    void testCompleteScriptExecutionFlow() throws Exception {
        // 1. 生成测试数据
        mockMvc.perform(post("/api/data/generate")
                        .param("users", "10")
                        .param("products", "5")
                        .param("orders", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. 执行脚本查询数据
        ScriptExecutionRequest request = new ScriptExecutionRequest();
        request.setScript("def keys = redis.keys('user:*')\nreturn keys.size()");
        request.setScriptName("integration-test");

        mockMvc.perform(post("/api/script/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. 分析脚本
        ScriptAnalysisRequest analysisRequest = new ScriptAnalysisRequest();
        analysisRequest.setScript(request.getScript());

        mockMvc.perform(post("/api/script/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(analysisRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.securityScore").exists());

        // 4. 获取统计数据
        mockMvc.perform(get("/api/data/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. 清理测试数据
        mockMvc.perform(delete("/api/data/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("集成测试：试运行脚本流程")
    void testTestRunFlow() throws Exception {
        // 1. 生成测试数据
        mockMvc.perform(post("/api/data/generate")
                        .param("users", "5")
                        .param("products", "3")
                        .param("orders", "5"))
                .andExpect(status().isOk());

        // 2. 试运行脚本（不应该修改数据）
        ScriptExecutionRequest request = new ScriptExecutionRequest();
        request.setScript("def value = redis.get('test:key')\nreturn value");
        request.setScriptName("test-run");

        mockMvc.perform(post("/api/script/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.testRun").value(true));

        // 3. 清理
        mockMvc.perform(delete("/api/data/clear"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("集成测试：健康检查")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/script/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Script service is running"));
    }

    @Test
    @DisplayName("集成测试：Web页面访问")
    void testWebPageAccess() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
}

