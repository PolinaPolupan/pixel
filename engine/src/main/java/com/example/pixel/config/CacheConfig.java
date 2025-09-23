package com.example.pixel.config;

import com.example.pixel.node.service.NodeCacheService;
import com.example.pixel.node.service.SpringCacheNodeService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

@Configuration
@EnableCaching
@Profile("default")
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Collections.singletonList("nodeCache"));
        return cacheManager;
    }

    @Bean
    public NodeCacheService nodeCacheService(CacheManager cacheManager) {
        return new SpringCacheNodeService(cacheManager);
    }
}