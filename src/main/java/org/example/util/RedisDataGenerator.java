package org.example.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis Test Data Generator
 * Generates various types of test data for Redis
 */
@Slf4j
@Component
public class RedisDataGenerator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();

    public RedisDataGenerator(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Generate comprehensive test data
     */
    public void generateTestData(int userCount, int productCount, int orderCount) {
        log.info("Generating test data: {} users, {} products, {} orders", userCount, productCount, orderCount);

        generateUsers(userCount);
        generateProducts(productCount);
        generateOrders(orderCount);
        generateSessions(userCount / 2);
        generateMetrics();

        log.info("Test data generation completed");
    }

    /**
     * Generate user data
     */
    public void generateUsers(int count) {
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "西安"};
        String[] departments = {"技术部", "产品部", "运营部", "市场部", "销售部"};

        for (int i = 1; i <= count; i++) {
            String key = "user:" + i;
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", String.valueOf(i));
            userData.put("name", "用户" + i);
            userData.put("age", String.valueOf(20 + random.nextInt(40)));
            userData.put("city", cities[random.nextInt(cities.length)]);
            userData.put("department", departments[random.nextInt(departments.length)]);
            userData.put("email", "user" + i + "@example.com");
            userData.put("phone", "138" + String.format("%08d", random.nextInt(100000000)));
            userData.put("salary", String.valueOf(5000 + random.nextInt(20000)));
            userData.put("joinDate", "2020-" + String.format("%02d", 1 + random.nextInt(12)) + "-01");

            redisTemplate.opsForHash().putAll(key, userData);
        }

        log.info("Generated {} users", count);
    }

    /**
     * Generate product data
     */
    public void generateProducts(int count) {
        String[] categories = {"电子产品", "服装", "食品", "图书", "家居", "运动"};
        String[] brands = {"品牌A", "品牌B", "品牌C", "品牌D", "品牌E"};

        for (int i = 1; i <= count; i++) {
            String key = "product:" + i;
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", String.valueOf(i));
            productData.put("name", "商品" + i);
            productData.put("category", categories[random.nextInt(categories.length)]);
            productData.put("brand", brands[random.nextInt(brands.length)]);
            productData.put("price", String.valueOf(10 + random.nextInt(1000)));
            productData.put("stock", String.valueOf(random.nextInt(1000)));
            productData.put("sales", String.valueOf(random.nextInt(10000)));
            productData.put("rating", String.format("%.1f", 3.0 + random.nextDouble() * 2));

            redisTemplate.opsForHash().putAll(key, productData);
        }

        log.info("Generated {} products", count);
    }

    /**
     * Generate order data
     */
    public void generateOrders(int count) {
        String[] statuses = {"pending", "paid", "shipped", "delivered", "cancelled"};

        for (int i = 1; i <= count; i++) {
            String key = "order:" + i;
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("id", String.valueOf(i));
            orderData.put("userId", String.valueOf(1 + random.nextInt(100)));
            orderData.put("productId", String.valueOf(1 + random.nextInt(50)));
            orderData.put("quantity", String.valueOf(1 + random.nextInt(5)));
            orderData.put("amount", String.valueOf(10 + random.nextInt(5000)));
            orderData.put("status", statuses[random.nextInt(statuses.length)]);
            orderData.put("createTime", "2024-" + String.format("%02d", 1 + random.nextInt(12)) + "-" + String.format("%02d", 1 + random.nextInt(28)));

            redisTemplate.opsForHash().putAll(key, orderData);
        }

        log.info("Generated {} orders", count);
    }

    /**
     * Generate session data with TTL
     */
    public void generateSessions(int count) {
        for (int i = 1; i <= count; i++) {
            String key = "session:" + UUID.randomUUID().toString();
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", String.valueOf(1 + random.nextInt(100)));
            sessionData.put("loginTime", System.currentTimeMillis());
            sessionData.put("ip", "192.168." + random.nextInt(255) + "." + random.nextInt(255));

            redisTemplate.opsForHash().putAll(key, sessionData);
            redisTemplate.expire(key, 1 + random.nextInt(24), TimeUnit.HOURS);
        }

        log.info("Generated {} sessions with TTL", count);
    }

    /**
     * Generate metrics data
     */
    public void generateMetrics() {
        // Page views
        for (int i = 0; i < 100; i++) {
            redisTemplate.opsForList().rightPush("metrics:pageviews",
                Map.of("page", "/page" + (i % 10), "timestamp", System.currentTimeMillis()));
        }

        // Active users set
        for (int i = 1; i <= 50; i++) {
            redisTemplate.opsForSet().add("metrics:active_users", "user:" + i);
        }

        // Leaderboard
        for (int i = 1; i <= 20; i++) {
            redisTemplate.opsForZSet().add("leaderboard:score", "player" + i, random.nextInt(1000));
        }

        // Tags
        String[] tags = {"redis", "groovy", "spring", "java", "llm", "ai", "database", "cache"};
        for (String tag : tags) {
            redisTemplate.opsForSet().add("tags:all", tag);
        }

        log.info("Generated metrics data");
    }

    /**
     * Clear all test data
     */
    public void clearTestData() {
        Set<String> keys = redisTemplate.keys("*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared {} keys", keys.size());
        }
    }

    /**
     * Get data statistics
     */
    public Map<String, Object> getDataStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalKeys", redisTemplate.keys("*").size());
        stats.put("userCount", redisTemplate.keys("user:*").size());
        stats.put("productCount", redisTemplate.keys("product:*").size());
        stats.put("orderCount", redisTemplate.keys("order:*").size());
        stats.put("sessionCount", redisTemplate.keys("session:*").size());

        return stats;
    }
}
