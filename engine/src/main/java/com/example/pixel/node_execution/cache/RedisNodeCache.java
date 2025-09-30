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

import static com.example.pixel.common.model.Profiles.*;

@Slf4j
@RequiredArgsConstructor
@Profile(REDIS)
@Component
public class RedisNodeCache implements NodeCache {
    private static final String UNABLE_TO_STORE_KEY_MESSAGE = "Failed to serialize cache data. Unable to store key: %s";
    private static final String UNABLE_TO_RETRIEVE_KEY_MESSAGE = "Failed to deserialize cache data. Unable to retrieve key: %s";

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void put(String key, Map<String, Object> data) {
        try (Jedis jedis = jedisPool.getResource()) {
            String serializedData = objectMapper.writeValueAsString(data);
            jedis.set(key, serializedData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format(UNABLE_TO_STORE_KEY_MESSAGE, key), e);
        }
    }

    @Override
    public Map<String, Object> get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String serializedData = jedis.get(key);
            if (serializedData == null) {
                return null;
            }
            return objectMapper.readValue(serializedData, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(String.format(UNABLE_TO_RETRIEVE_KEY_MESSAGE, key), e);
        }
    }

    @Override
    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }
}
