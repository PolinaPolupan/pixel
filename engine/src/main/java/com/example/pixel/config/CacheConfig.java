package com.example.pixel.config;

import com.example.pixel.node_execution.cache.NodeCache;
import com.example.pixel.node_execution.cache.SpringNodeCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

import static com.example.pixel.common.model.Profiles.*;

@Configuration
@EnableCaching
@Profile(DEFAULT)
public class CacheConfig {

    @Value("${node.cache}")
    private String CACHE_NAME;

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Collections.singletonList(CACHE_NAME));
        return cacheManager;
    }

    @Bean
    public NodeCache nodeCache(CacheManager cacheManager) {
        return new SpringNodeCache(cacheManager);
    }
}