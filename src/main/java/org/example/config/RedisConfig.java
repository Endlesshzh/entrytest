package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration
 * 开机自启动：brew services start redis 已设置为开机自启动
 * 配置文件位置：/usr/local/etc/redis.conf
 * 数据存储位置：/usr/local/var/db/redis/
 * 日志位置：/usr/local/var/log/redis.log
 * 现在可以启动 Spring Boot 应用，它会自动连接到本地 Redis。
 */
@Configuration
public class RedisConfig {

    /**
     * Configure RedisTemplate with proper serializers
     * 使用 StringRedisSerializer 以兼容通过 redis-cli 直接存储的字符串数据
     * 如果需要存储复杂对象，可以在应用层进行 JSON 序列化/反序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys and values
        // 这样可以兼容通过 redis-cli 直接存储的数据
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
