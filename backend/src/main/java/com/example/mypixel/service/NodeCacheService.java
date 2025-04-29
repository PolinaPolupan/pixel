package com.example.mypixel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Map;

@Service
public class NodeCacheService {

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NodeCacheService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void put(String key, Map<String, Object> outputs) {
        try (Jedis jedis = jedisPool.getResource()) {
            String serializedMap = objectMapper.writeValueAsString(outputs);

            jedis.set(key, serializedMap);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing node outputs", e);
        }
    }

    public Map<String, Object> get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String serializedMap = jedis.get(key);

            if (serializedMap == null) {
                return null;
            }

            return objectMapper.readValue(serializedMap,
                    new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing node outputs", e);
        }
    }
}
