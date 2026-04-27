package com.movie.api.service;

import com.movie.api.exception.NotFoundException;
import com.movie.api.storage.model.Setting;
import com.movie.api.storage.repository.SettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SettingCacheService {
    @Autowired
    private SettingRepository settingRepository;

    /**
     * key: keyName, value: Setting
     */
    private final ConcurrentHashMap<String, Setting> globalSettingMaps = new ConcurrentHashMap<>();

    public Setting get(String keyName) {
        Setting cached = globalSettingMaps.get(keyName);

        if (cached != null) return cached; // cache hit

        // Cache miss → query DB
        log.debug("Cache miss for key: {}", keyName);
        Setting setting = settingRepository.findByKeyName(keyName)
                .orElseThrow(() -> new NotFoundException("Setting not found for key: " + keyName));
        globalSettingMaps.put(keyName, setting);
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
    }

    public void remove(String keyName) {
        globalSettingMaps.remove(keyName);
    }
}
