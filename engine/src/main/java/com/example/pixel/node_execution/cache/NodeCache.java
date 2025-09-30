package com.example.pixel.node_execution.cache;

import java.util.Map;

public interface NodeCache {
    void put(String key, Map<String, Object> data);
    Map<String, Object> get(String key);
    boolean exists(String key);
}
