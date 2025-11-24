package org.example.controller;

import org.example.util.RedisDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试类：DataController
 * 测试数据管理相关的REST API接口
 */
@WebMvcTest(DataController.class)
@DisplayName("数据控制器测试")
class DataControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private RedisDataGenerator dataGenerator;

    private Map<String, Object> statistics;

    @BeforeEach
    void setUp() {
        // 准备统计数据
        statistics = new HashMap<>();
        statistics.put("totalUsers", 100);
        statistics.put("totalProducts", 50);
        statistics.put("totalOrders", 200);
        statistics.put("totalKeys", 350);
    }

    @Test
    @DisplayName("测试生成测试数据接口 - 使用默认参数")
    void testGenerateTestData_WithDefaults() throws Exception {
        // Mock服务
        doNothing().when(dataGenerator).generateTestData(100, 50, 200);
        when(dataGenerator.getDataStatistics()).thenReturn(statistics);

        // 执行请求
        mockMvc.perform(post("/api/data/generate"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Test data generated successfully"))
                .andExpect(jsonPath("$.statistics.totalUsers").value(100))
                .andExpect(jsonPath("$.statistics.totalProducts").value(50))
                .andExpect(jsonPath("$.statistics.totalOrders").value(200))
                .andExpect(jsonPath("$.statistics.totalKeys").value(350));

        // 验证调用
        verify(dataGenerator, times(1)).generateTestData(100, 50, 200);
        verify(dataGenerator, times(1)).getDataStatistics();
    }

    @Test
    @DisplayName("测试生成测试数据接口 - 使用自定义参数")
    void testGenerateTestData_WithCustomParams() throws Exception {
        int users = 200;
        int products = 100;
        int orders = 500;

        // 更新统计数据
        Map<String, Object> customStats = new HashMap<>();
        customStats.put("totalUsers", users);
        customStats.put("totalProducts", products);
        customStats.put("totalOrders", orders);
        customStats.put("totalKeys", users + products + orders);

        // Mock服务
        doNothing().when(dataGenerator).generateTestData(users, products, orders);
        when(dataGenerator.getDataStatistics()).thenReturn(customStats);

        // 执行请求
        mockMvc.perform(post("/api/data/generate")
                        .param("users", String.valueOf(users))
                        .param("products", String.valueOf(products))
                        .param("orders", String.valueOf(orders)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statistics.totalUsers").value(users))
                .andExpect(jsonPath("$.statistics.totalProducts").value(products))
                .andExpect(jsonPath("$.statistics.totalOrders").value(orders));

        // 验证调用
        verify(dataGenerator, times(1)).generateTestData(users, products, orders);
    }

    @Test
    @DisplayName("测试生成测试数据接口 - 异常场景")
    void testGenerateTestData_Exception() throws Exception {
        // Mock服务抛出异常
        doThrow(new RuntimeException("Redis connection failed"))
                .when(dataGenerator).generateTestData(anyInt(), anyInt(), anyInt());

        // 执行请求
        mockMvc.perform(post("/api/data/generate"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Redis connection failed"));
    }

    @Test
    @DisplayName("测试清空测试数据接口 - 成功场景")
    void testClearTestData_Success() throws Exception {
        // Mock服务
        doNothing().when(dataGenerator).clearTestData();

        // 执行请求
        mockMvc.perform(delete("/api/data/clear"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All test data cleared"));

        // 验证调用
        verify(dataGenerator, times(1)).clearTestData();
    }

    @Test
    @DisplayName("测试清空测试数据接口 - 异常场景")
    void testClearTestData_Exception() throws Exception {
        // Mock服务抛出异常
        doThrow(new RuntimeException("Clear operation failed"))
                .when(dataGenerator).clearTestData();

        // 执行请求
        mockMvc.perform(delete("/api/data/clear"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Clear operation failed"));
    }

    @Test
    @DisplayName("测试获取数据统计接口 - 成功场景")
    void testGetStatistics_Success() throws Exception {
        // Mock服务
        when(dataGenerator.getDataStatistics()).thenReturn(statistics);

        // 执行请求
        mockMvc.perform(get("/api/data/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statistics.totalUsers").value(100))
                .andExpect(jsonPath("$.statistics.totalProducts").value(50))
                .andExpect(jsonPath("$.statistics.totalOrders").value(200))
                .andExpect(jsonPath("$.statistics.totalKeys").value(350));

        // 验证调用
        verify(dataGenerator, times(1)).getDataStatistics();
    }

    @Test
    @DisplayName("测试获取数据统计接口 - 空数据场景")
    void testGetStatistics_EmptyData() throws Exception {
        // Mock服务返回空统计数据
        Map<String, Object> emptyStats = new HashMap<>();
        emptyStats.put("totalUsers", 0);
        emptyStats.put("totalProducts", 0);
        emptyStats.put("totalOrders", 0);
        emptyStats.put("totalKeys", 0);

        when(dataGenerator.getDataStatistics()).thenReturn(emptyStats);

        // 执行请求
        mockMvc.perform(get("/api/data/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statistics.totalUsers").value(0))
                .andExpect(jsonPath("$.statistics.totalKeys").value(0));
    }

    @Test
    @DisplayName("测试获取数据统计接口 - 异常场景")
    void testGetStatistics_Exception() throws Exception {
        // Mock服务抛出异常
        when(dataGenerator.getDataStatistics())
                .thenThrow(new RuntimeException("Failed to get statistics"));

        // 执行请求
        mockMvc.perform(get("/api/data/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Failed to get statistics"));
    }

    @Test
    @DisplayName("测试生成测试数据接口 - 边界值测试")
    void testGenerateTestData_BoundaryValues() throws Exception {
        // 测试最小值和最大值
        int minUsers = 1;
        int minProducts = 1;
        int minOrders = 1;

        Map<String, Object> minStats = new HashMap<>();
        minStats.put("totalUsers", minUsers);
        minStats.put("totalProducts", minProducts);
        minStats.put("totalOrders", minOrders);
        minStats.put("totalKeys", minUsers + minProducts + minOrders);

        doNothing().when(dataGenerator).generateTestData(minUsers, minProducts, minOrders);
        when(dataGenerator.getDataStatistics()).thenReturn(minStats);

        // 执行请求
        mockMvc.perform(post("/api/data/generate")
                        .param("users", String.valueOf(minUsers))
                        .param("products", String.valueOf(minProducts))
                        .param("orders", String.valueOf(minOrders)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(dataGenerator, times(1)).generateTestData(minUsers, minProducts, minOrders);
    }
}

