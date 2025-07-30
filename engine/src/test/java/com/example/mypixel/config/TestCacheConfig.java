package com.example.mypixel.config;

import com.example.mypixel.service.InMemoryNodeCacheService;
import com.example.mypixel.service.NodeCacheService;
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
    public NodeCacheService nodeCacheService() {
        return new InMemoryNodeCacheService();
    }
}