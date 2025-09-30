package com.example.pixel.node_execution.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.pixel.common.model.Profiles.*;

@Slf4j
@RequiredArgsConstructor
@Primary
@Profile(DEFAULT)
@Component
public class SpringNodeCache implements NodeCache {
    private final static String UNABLE_TO_STORE_KEY_MESSAGE = "Cache '{}' not found. Unable to store key: {}";
    private final static String UNABLE_TO_RETRIEVE_KEY_MESSAGE = "Cache '{}' not found. Unable to retrieve key: {}";

    private final CacheManager cacheManager;

    @Value("${node.cache}")
    private String CACHE_NAME;

    @Override
    public void put(String key, Map<String, Object> outputs) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(key, outputs);
        } else {
            log.warn(UNABLE_TO_STORE_KEY_MESSAGE, CACHE_NAME, key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String key) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            log.warn(UNABLE_TO_RETRIEVE_KEY_MESSAGE, CACHE_NAME, key);
            return null;
        }

        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper == null) {
            log.warn(UNABLE_TO_RETRIEVE_KEY_MESSAGE, CACHE_NAME, key);
            return null;
        }

        Object value = wrapper.get();
        return (Map<String, Object>) value;
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