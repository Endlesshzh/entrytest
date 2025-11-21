package org.example.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ScriptConfig;
import org.example.model.ScriptExecutionResult;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * Service for executing Groovy scripts against Redis
 * Optimized for high performance (500+ QPS)
 */
@Slf4j
@Service
public class ScriptEngineService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ScriptConfig scriptConfig;
    private final ExecutorService executorService;
    private final Cache<String, Script> scriptCache;

    public ScriptEngineService(
            RedisTemplate<String, Object> redisTemplate,
            ScriptConfig scriptConfig
    ) {
        this.redisTemplate = redisTemplate;
        this.scriptConfig = scriptConfig;

        // Use cached thread pool for better performance
        this.executorService = Executors.newCachedThreadPool();

        // Use Caffeine cache for better performance
        this.scriptCache = Caffeine.newBuilder()
                .maximumSize(scriptConfig.getCacheSize())
                .recordStats()
                .build();

        log.info("ScriptEngineService initialized with cache size: {}", scriptConfig.getCacheSize());
    }

    /**
     * Execute a Groovy script
     */
    public ScriptExecutionResult executeScript(String scriptText, boolean testRun) {
        long startTime = System.nanoTime();

        try {
            // Add execution info to MDC for logging
            MDC.put("scriptLength", String.valueOf(scriptText.length()));
            MDC.put("testRun", String.valueOf(testRun));

            log.debug("Executing script, testRun: {}, length: {}", testRun, scriptText.length());

            // Validate script
            validateScript(scriptText);

            // Try to get cached script
            String cacheKey = Integer.toHexString(scriptText.hashCode());
            Script script = scriptCache.get(cacheKey, key -> {
                log.debug("Script not in cache, parsing: {}", key);
                Binding binding = createBinding();
                GroovyShell shell = new GroovyShell(binding);
                return shell.parse(scriptText);
            });

            // Create new binding for execution
            Binding binding = createBinding();
            script.setBinding(binding);

            // Execute with timeout
            Object result = executeWithTimeout(script, scriptConfig.getMaxExecutionTime());

            long executionTime = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms

            // Log performance metrics
            MDC.put("executionTime", String.valueOf(executionTime));
            log.info("Script executed successfully in {}ms", executionTime);

            return ScriptExecutionResult.builder()
                    .success(true)
                    .result(result)
                    .executionTime(executionTime)
                    .script(scriptText)
                    .testRun(testRun)
                    .build();

        } catch (TimeoutException e) {
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Script execution timeout after {}ms", executionTime, e);

            return ScriptExecutionResult.builder()
                    .success(false)
                    .error("Script execution timeout after " + scriptConfig.getMaxExecutionTime() + "ms")
                    .executionTime(executionTime)
                    .script(scriptText)
                    .testRun(testRun)
                    .build();
        } catch (Exception e) {
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Script execution failed after {}ms", executionTime, e);

            return ScriptExecutionResult.builder()
                    .success(false)
                    .error("Script execution failed: " + e.getMessage())
                    .executionTime(executionTime)
                    .script(scriptText)
                    .testRun(testRun)
                    .build();
        } finally {
            MDC.clear();
        }
    }

    /**
     * Validate script for security issues
     */
    private void validateScript(String script) {
        if (script == null || script.trim().isEmpty()) {
            throw new IllegalArgumentException("Script cannot be empty");
        }

        // Check for forbidden patterns
        String upperScript = script.toUpperCase();
        for (String pattern : scriptConfig.getForbiddenPatterns()) {
            if (upperScript.contains(pattern.toUpperCase())) {
                throw new SecurityException("Script contains forbidden pattern: " + pattern);
            }
        }

        // Additional security checks
        if (script.contains("System.exit") || script.contains("Runtime.getRuntime")) {
            throw new SecurityException("Script contains forbidden system operations");
        }

        if (script.contains("java.io.File") || script.contains("new File")) {
            throw new SecurityException("Script contains forbidden file operations");
        }
    }

    /**
     * Create binding with Redis operations
     */
    private Binding createBinding() {
        Binding binding = new Binding();

        // Provide Redis operations wrapper
        RedisOperations redisOps = new RedisOperations(redisTemplate, scriptConfig);
        binding.setVariable("redis", redisOps);
        binding.setVariable("log", log);

        return binding;
    }

    /**
     * Execute script with timeout
     */
    private Object executeWithTimeout(Script script, long timeoutMs) throws TimeoutException, ExecutionException, InterruptedException {
        Future<Object> future = executorService.submit(() -> script.run());

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
    }

    /**
     * Inner class to provide safe Redis operations to scripts
     */
    public static class RedisOperations {
        private final RedisTemplate<String, Object> redisTemplate;
        private final ScriptConfig scriptConfig;

        public RedisOperations(RedisTemplate<String, Object> redisTemplate, ScriptConfig scriptConfig) {
            this.redisTemplate = redisTemplate;
            this.scriptConfig = scriptConfig;
        }

        public Object get(String key) {
            return redisTemplate.opsForValue().get(key);
        }

        public void set(String key, Object value) {
            redisTemplate.opsForValue().set(key, value);
        }

        public Object hget(String key, String field) {
            return redisTemplate.opsForHash().get(key, field);
        }

        public Object hgetAll(String key) {
            return redisTemplate.opsForHash().entries(key);
        }

        public void hset(String key, String field, Object value) {
            redisTemplate.opsForHash().put(key, field, value);
        }

        public Object keys(String pattern) {
            return redisTemplate.keys(pattern);
        }

        public Object lrange(String key, long start, long end) {
            return redisTemplate.opsForList().range(key, start, end);
        }

        public Object smembers(String key) {
            return redisTemplate.opsForSet().members(key);
        }

        public Object zrange(String key, long start, long end) {
            return redisTemplate.opsForZSet().range(key, start, end);
        }

        public Boolean exists(String key) {
            return redisTemplate.hasKey(key);
        }

        public Long ttl(String key) {
            return redisTemplate.getExpire(key);
        }
    }
}
