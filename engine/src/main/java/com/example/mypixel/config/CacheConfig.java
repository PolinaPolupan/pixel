package com.example.mypixel.config;

import com.example.mypixel.service.NodeCacheService;
import com.example.mypixel.service.RedisNodeCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@Profile("!test")
@Configuration
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    @Bean
    public JedisPool jedisPool(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port) {

        JedisPoolConfig poolConfig = new JedisPoolConfig();

        return new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT);
    }

    @Bean
    public NodeCacheService nodeCacheService(JedisPool jedisPool) {
        return new RedisNodeCacheService(jedisPool);
    }
}
