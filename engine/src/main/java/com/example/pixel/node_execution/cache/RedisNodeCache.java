package com.example.pixel.node_execution.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Profile("redis")
@Component
public class RedisNodeCache implements NodeCache {

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Override
    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }
}