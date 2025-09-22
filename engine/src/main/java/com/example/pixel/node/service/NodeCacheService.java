package com.example.pixel.node.service;

import java.util.Map;

public interface NodeCacheService {
    void put(String key, Map<String, Object> outputs);
    Map<String, Object> get(String key);
    boolean exists(String key);
}
