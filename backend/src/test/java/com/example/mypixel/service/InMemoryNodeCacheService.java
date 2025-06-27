package com.example.mypixel.service;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemoryNodeCacheService implements NodeCacheService {

    private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String key, Map<String, Object> outputs) {
        Map<String, Object> copy = new HashMap<>(outputs);
        cache.put(key, copy);
        log.debug("Stored data in memory cache with key: {}", key);
    }

    @Override
    public Map<String, Object> get(String key) {
        Map<String, Object> result = cache.get(key);
        if (result == null) {
            log.debug("Memory cache miss for key: {}", key);
            return null;
        }

        log.debug("Memory cache hit for key: {}", key);
        return result;
    }

    public void clear() {
        cache.clear();
        log.debug("Memory cache cleared");
    }
}