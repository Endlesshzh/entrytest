package org.example.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试类：WebController
 * 测试Web页面相关的接口
 */
@WebMvcTest(WebController.class)
@DisplayName("Web控制器测试")
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("测试获取主页面接口")
    void testIndex() throws Exception {
        // 执行请求
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("测试主页面返回正确的视图名称")
    void testIndex_ViewName() throws Exception {
        // 执行请求并验证视图名称
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(forwardedUrl("index"));
    }

    @Test
    @DisplayName("测试根路径重定向")
    void testRootPath() throws Exception {
        // 测试根路径访问
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}

