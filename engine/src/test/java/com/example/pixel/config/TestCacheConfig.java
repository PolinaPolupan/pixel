package com.example.pixel.config;

import com.example.pixel.node.InMemoryNodeCache;
import com.example.pixel.node_execution.cache.NodeCache;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Profile("test")
@TestConfiguration
public class TestCacheConfig {

    @Bean
    public RedisCacheManager cacheManager() {
        return Mockito.mock(RedisCacheManager.class);
    }

    @Bean
    public JedisPool jedisPool() {
        JedisPool mockPool = Mockito.mock(JedisPool.class);
        Jedis mockJedis = Mockito.mock(Jedis.class);
        Mockito.when(mockPool.getResource()).thenReturn(mockJedis);
        return mockPool;
    }

    @Bean
    public NodeCache nodeCacheService() {
        return new InMemoryNodeCache();
    }
}