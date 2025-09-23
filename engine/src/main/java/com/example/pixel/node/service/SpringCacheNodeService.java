package com.example.pixel.node.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@Profile("default")
@RequiredArgsConstructor
public class SpringCacheNodeService implements NodeCacheService {

    private final CacheManager cacheManager;
    private static final String CACHE_NAME = "nodeCache";

    @Override
    public void put(String key, Map<String, Object> outputs) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(key, outputs);
            log.debug("Cached node data with key: {}", key);
        } else {
            log.warn("Cache '{}' not found. Unable to store key: {}", CACHE_NAME, key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String key) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                return (Map<String, Object>) wrapper.get();
            }
        }
        return null;
    }

    @Override
    public boolean exists(String key) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            return cache.get(key) != null;
        }
        return false;
    }
}