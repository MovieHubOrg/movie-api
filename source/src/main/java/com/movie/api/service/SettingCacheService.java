package com.movie.api.service;

import com.movie.api.constant.BaseConstant;
import com.movie.api.exception.NotFoundException;
import com.movie.api.service.redis.RedisService;
import com.movie.api.storage.model.Setting;
import com.movie.api.storage.repository.SettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SettingCacheService {
    private static final String SETTING_REDIS_KEY_PREFIX = "setting";

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private RedisService redisService;

    /**
     * key: keyName, value: Setting
     */
    private final ConcurrentHashMap<String, Setting> globalSettingMaps = new ConcurrentHashMap<>();

    public Setting get(String keyName) {
        if (isRedisCacheKey(keyName)) {
            Setting redisCached = getFromRedis(keyName);
            if (redisCached != null) {
                globalSettingMaps.put(keyName, redisCached);
                return redisCached;
            }
        }

        Setting cached = globalSettingMaps.get(keyName);

        if (cached != null) return cached; // cache hit

        // Cache miss → query DB
        log.debug("Cache miss for key: {}", keyName);
        Setting setting = settingRepository.findByKeyName(keyName)
                .orElseThrow(() -> new NotFoundException("Setting not found for key: " + keyName));
        put(setting);
        return setting;
    }

    public String getValue(String keyName) {
        Setting setting = get(keyName);
        return setting != null ? setting.getValueData() : null;
    }

    public Integer getIntegerValue(String keyName) {
        String value = getValue(keyName);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid integer setting for key: " + keyName + ", value: " + value, ex);
        }
    }

    public void put(Setting setting) {
        globalSettingMaps.put(setting.getKeyName(), setting);
        if (isRedisCacheKey(setting.getKeyName())) {
            putToRedis(setting);
        }
    }

    public void remove(String keyName) {
        globalSettingMaps.remove(keyName);
        if (isRedisCacheKey(keyName)) {
            removeFromRedis(keyName);
        }
    }

    private boolean isRedisCacheKey(String keyName) {
        return BaseConstant.SETTING_REDIS_CACHE_KEYS.contains(keyName);
    }

    private String buildRedisKey(String keyName) {
        return redisService.buildKey(SETTING_REDIS_KEY_PREFIX, keyName);
    }

    private Setting getFromRedis(String keyName) {
        try {
            return redisService.get(buildRedisKey(keyName), Setting.class);
        } catch (RuntimeException ex) {
            log.warn("Failed to get setting from Redis, keyName={}", keyName, ex);
            return null;
        }
    }

    private void putToRedis(Setting setting) {
        try {
            redisService.put(buildRedisKey(setting.getKeyName()), setting, 24 * 60 * 60); // Cache for 24 hours
        } catch (RuntimeException ex) {
            log.warn("Failed to cache setting to Redis, keyName={}", setting.getKeyName(), ex);
        }
    }

    private void removeFromRedis(String keyName) {
        try {
            redisService.delete(buildRedisKey(keyName));
        } catch (RuntimeException ex) {
            log.warn("Failed to remove setting from Redis, keyName={}", keyName, ex);
        }
    }
}
