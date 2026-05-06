package com.movie.api.service.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {
    public static final int TTL = 300; // 5 minutes

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public String buildKey(String... parts) {
        return String.join("::", parts);
    }

    public <T> void put(String key, T value) {
        handlePut(key, value, TTL);
    }

    public <T> void put(String key, T value, Integer ttl) {
        handlePut(key, value, ttl);
    }

    /**
     * Save object to Redis - Always save type JSON
     */
    private <T> void handlePut(String key, T value, Integer ttl) {
        try {
            if (value == null) {
                log.warn("Attempting to save null value for key: {}", key);
                return;
            }

            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);

            log.debug("Saved to Redis - Key: {}, Type: {}, TTL: {}s",
                    key, value.getClass().getSimpleName(), ttl);

        } catch (JsonProcessingException e) {
            log.error("Error serializing object to JSON for key: {}", key, e);
            throw new RuntimeException("Failed to serialize object to Redis", e);
        }
    }

    /**
     * Get object from Redis with Class
     * deserialize JSON
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            String jsonValue = (String) redisTemplate.opsForValue().get(key);

            if (jsonValue == null) {
                log.debug("Key not found in Redis: {}", key);
                return null;
            }

            // Deserialize JSON to object
            T result = objectMapper.readValue(jsonValue, clazz);
            log.debug("Retrieved from Redis - Key: {}, Type: {}", key, clazz.getSimpleName());
            return result;

        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON for key: {}", key, e);
            throw new RuntimeException("Failed to deserialize object from Redis", e);
        }
    }

    /**
     * Get object from Redis with TypeReference (for List, Map, etc.)
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        try {
            String jsonValue = (String) redisTemplate.opsForValue().get(key);

            if (jsonValue == null) {
                log.debug("⚠️ Key not found in Redis: {}", key);
                return null;
            }

            T result = objectMapper.readValue(jsonValue, typeReference);
            log.debug("Retrieved from Redis - Key: {}", key);
            return result;

        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON for key: {}", key, e);
            throw new RuntimeException("Failed to deserialize object from Redis", e);
        }
    }

    public Set<String> getKeysByPrefix(String prefix) {
        return redisTemplate.keys(prefix + "*");
    }

    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("========> key {}", key);
    }

    public void deleteKeys(Collection<String> keys) {
        redisTemplate.delete(keys);
        log.debug("========> keys {}", keys);
    }

    public void deleteByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void refreshTTL(String key, int ttl) {
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
}
