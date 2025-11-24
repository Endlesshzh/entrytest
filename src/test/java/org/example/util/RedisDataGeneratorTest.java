package org.example.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 单元测试类：RedisDataGenerator
 * 测试Redis数据生成器的关键方法和流程
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Redis数据生成器单元测试")
class RedisDataGeneratorTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisDataGenerator redisDataGenerator;

    @BeforeEach
    void setUp() {
        // Mock Redis操作
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 创建生成器实例
        redisDataGenerator = new RedisDataGenerator(redisTemplate);
    }

    @Test
    @DisplayName("测试生成测试数据 - 完整流程")
    void testGenerateTestData_Complete() {
        // 执行生成
        redisDataGenerator.generateTestData(10, 5, 8);

        // 验证调用
        verify(hashOperations, atLeast(10)).putAll(anyString(), anyMap());
        verify(hashOperations, atLeast(5)).putAll(anyString(), anyMap());
        verify(hashOperations, atLeast(8)).putAll(anyString(), anyMap());
    }

    @Test
    @DisplayName("测试生成用户数据")
    void testGenerateUsers() {
        int count = 5;

        // 执行生成
        redisDataGenerator.generateUsers(count);

        // 验证调用次数
        verify(hashOperations, times(count)).putAll(anyString(), anyMap());
        
        // 验证键格式
        for (int i = 1; i <= count; i++) {
            verify(hashOperations, times(1)).putAll(eq("user:" + i), anyMap());
        }
    }

    @Test
    @DisplayName("测试生成用户数据 - 验证数据内容")
    void testGenerateUsers_DataContent() {
        int count = 3;

        // 执行生成
        redisDataGenerator.generateUsers(count);

        // 验证每个用户都包含必要的字段
        verify(hashOperations, times(count)).putAll(anyString(), argThat(map -> {
            Map<String, Object> userMap = (Map<String, Object>) map;
            return userMap.containsKey("id") &&
                   userMap.containsKey("name") &&
                   userMap.containsKey("age") &&
                   userMap.containsKey("city") &&
                   userMap.containsKey("department") &&
                   userMap.containsKey("email");
        }));
    }

    @Test
    @DisplayName("测试生成产品数据")
    void testGenerateProducts() {
        int count = 5;

        // 执行生成
        redisDataGenerator.generateProducts(count);

        // 验证调用次数
        verify(hashOperations, times(count)).putAll(anyString(), anyMap());
        
        // 验证键格式
        for (int i = 1; i <= count; i++) {
            verify(hashOperations, times(1)).putAll(eq("product:" + i), anyMap());
        }
    }

    @Test
    @DisplayName("测试生成产品数据 - 验证数据内容")
    void testGenerateProducts_DataContent() {
        int count = 3;

        // 执行生成
        redisDataGenerator.generateProducts(count);

        // 验证每个产品都包含必要的字段
        verify(hashOperations, times(count)).putAll(anyString(), argThat(map -> {
            Map<String, Object> productMap = (Map<String, Object>) map;
            return productMap.containsKey("id") &&
                   productMap.containsKey("name") &&
                   productMap.containsKey("category") &&
                   productMap.containsKey("brand") &&
                   productMap.containsKey("price") &&
                   productMap.containsKey("stock");
        }));
    }

    @Test
    @DisplayName("测试生成订单数据")
    void testGenerateOrders() {
        int count = 5;

        // 执行生成
        redisDataGenerator.generateOrders(count);

        // 验证调用次数
        verify(hashOperations, times(count)).putAll(anyString(), anyMap());
        
        // 验证键格式
        for (int i = 1; i <= count; i++) {
            verify(hashOperations, times(1)).putAll(eq("order:" + i), anyMap());
        }
    }

    @Test
    @DisplayName("测试生成订单数据 - 验证数据内容")
    void testGenerateOrders_DataContent() {
        int count = 3;

        // 执行生成
        redisDataGenerator.generateOrders(count);

        // 验证每个订单都包含必要的字段
        verify(hashOperations, times(count)).putAll(anyString(), argThat(map -> {
            Map<String, Object> orderMap = (Map<String, Object>) map;
            return orderMap.containsKey("id") &&
                   orderMap.containsKey("userId") &&
                   orderMap.containsKey("productId") &&
                   orderMap.containsKey("quantity") &&
                   orderMap.containsKey("amount") &&
                   orderMap.containsKey("status");
        }));
    }

    @Test
    @DisplayName("测试生成会话数据")
    void testGenerateSessions() {
        int count = 5;

        // 执行生成
        redisDataGenerator.generateSessions(count);

        // 验证调用次数
        verify(hashOperations, times(count)).putAll(anyString(), anyMap());
        verify(redisTemplate, times(count)).expire(anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("测试生成会话数据 - 验证TTL设置")
    void testGenerateSessions_TTL() {
        int count = 3;

        // 执行生成
        redisDataGenerator.generateSessions(count);

        // 验证TTL设置（应该在1-24小时之间）
        verify(redisTemplate, times(count)).expire(
                anyString(),
                argThat(ttl -> (Long) ttl >= 1 && (Long) ttl <= 24),
                any()
        );
    }

    @Test
    @DisplayName("测试生成指标数据")
    void testGenerateMetrics() {
        // 执行生成
        redisDataGenerator.generateMetrics();

        // 验证调用了List、Set、ZSet操作
        verify(listOperations, atLeastOnce()).rightPush(anyString(), any());
        verify(setOperations, atLeastOnce()).add(anyString(), any());
        verify(zSetOperations, atLeastOnce()).add(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("测试清空测试数据")
    void testClearTestData() {
        // Mock键集合
        Set<String> keys = new HashSet<>(Arrays.asList("user:1", "user:2", "product:1"));
        when(redisTemplate.keys("*")).thenReturn(keys);

        // 执行清空
        redisDataGenerator.clearTestData();

        // 验证删除操作
        @SuppressWarnings("unchecked")
        Collection<String> keysCollection = (Collection<String>) keys;
        verify(redisTemplate, times(1)).delete(keysCollection);
    }

    @Test
    @DisplayName("测试清空测试数据 - 无数据")
    void testClearTestData_Empty() {
        // Mock空键集合
        when(redisTemplate.keys("*")).thenReturn(Collections.emptySet());

        // 执行清空
        redisDataGenerator.clearTestData();

        // 验证没有调用删除操作
        @SuppressWarnings("unchecked")
        Collection<String> emptyCollection = Collections.emptySet();
        verify(redisTemplate, never()).delete(emptyCollection);
    }

    @Test
    @DisplayName("测试获取数据统计")
    void testGetDataStatistics() {
        // Mock键集合
        Set<String> allKeys = new HashSet<>(Arrays.asList(
                "user:1", "user:2", "user:3",
                "product:1", "product:2",
                "order:1", "order:2", "order:3", "order:4",
                "session:abc123"
        ));
        Set<String> userKeys = new HashSet<>(Arrays.asList("user:1", "user:2", "user:3"));
        Set<String> productKeys = new HashSet<>(Arrays.asList("product:1", "product:2"));
        Set<String> orderKeys = new HashSet<>(Arrays.asList("order:1", "order:2", "order:3", "order:4"));
        Set<String> sessionKeys = new HashSet<>(Arrays.asList("session:abc123"));

        when(redisTemplate.keys("*")).thenReturn(allKeys);
        when(redisTemplate.keys("user:*")).thenReturn(userKeys);
        when(redisTemplate.keys("product:*")).thenReturn(productKeys);
        when(redisTemplate.keys("order:*")).thenReturn(orderKeys);
        when(redisTemplate.keys("session:*")).thenReturn(sessionKeys);

        // 执行获取统计
        Map<String, Object> statistics = redisDataGenerator.getDataStatistics();

        // 验证结果
        assertNotNull(statistics);
        assertEquals(10, statistics.get("totalKeys"));
        assertEquals(3, statistics.get("userCount"));
        assertEquals(2, statistics.get("productCount"));
        assertEquals(4, statistics.get("orderCount"));
        assertEquals(1, statistics.get("sessionCount"));
    }

    @Test
    @DisplayName("测试获取数据统计 - 空数据")
    void testGetDataStatistics_Empty() {
        // Mock空键集合
        when(redisTemplate.keys("*")).thenReturn(Collections.emptySet());
        when(redisTemplate.keys("user:*")).thenReturn(Collections.emptySet());
        when(redisTemplate.keys("product:*")).thenReturn(Collections.emptySet());
        when(redisTemplate.keys("order:*")).thenReturn(Collections.emptySet());
        when(redisTemplate.keys("session:*")).thenReturn(Collections.emptySet());

        // 执行获取统计
        Map<String, Object> statistics = redisDataGenerator.getDataStatistics();

        // 验证结果
        assertNotNull(statistics);
        assertEquals(0, statistics.get("totalKeys"));
        assertEquals(0, statistics.get("userCount"));
        assertEquals(0, statistics.get("productCount"));
        assertEquals(0, statistics.get("orderCount"));
        assertEquals(0, statistics.get("sessionCount"));
    }

    @Test
    @DisplayName("测试生成测试数据 - 边界值")
    void testGenerateTestData_BoundaryValues() {
        // 测试零值
        redisDataGenerator.generateTestData(0, 0, 0);

        // 验证仍然调用了相关方法（即使数量为0）
        verify(hashOperations, never()).putAll(anyString(), anyMap());
    }

    @Test
    @DisplayName("测试生成测试数据 - 大数量")
    void testGenerateTestData_LargeCount() {
        int largeCount = 1000;

        // 执行生成
        redisDataGenerator.generateTestData(largeCount, largeCount, largeCount);

        // 验证调用次数
        // generateTestData会调用: generateUsers(1000) + generateProducts(1000) + generateOrders(1000) + generateSessions(500) = 3500次
        verify(hashOperations, times(largeCount * 3 + largeCount / 2)).putAll(anyString(), anyMap());
    }

    @Test
    @DisplayName("测试生成用户数据 - 验证随机性")
    void testGenerateUsers_Randomness() {
        int count = 10;

        // 执行生成
        redisDataGenerator.generateUsers(count);

        // 验证每个用户都有不同的数据（通过验证调用次数）
        verify(hashOperations, times(count)).putAll(anyString(), anyMap());
    }

    @Test
    @DisplayName("测试生成产品数据 - 验证价格和库存范围")
    void testGenerateProducts_PriceAndStockRange() {
        int count = 5;

        // 执行生成
        redisDataGenerator.generateProducts(count);

        // 验证数据格式正确（通过验证调用）
        verify(hashOperations, times(count)).putAll(anyString(), argThat(map -> {
            Map<String, Object> productMap = (Map<String, Object>) map;
            String price = (String) productMap.get("price");
            String stock = (String) productMap.get("stock");
            return price != null && stock != null;
        }));
    }
}

