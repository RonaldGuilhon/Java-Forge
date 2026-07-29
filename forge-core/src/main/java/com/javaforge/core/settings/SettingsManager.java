package com.javaforge.core.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SettingsManager {

    private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".javaforge");
    private static final Path SETTINGS_FILE = CONFIG_DIR.resolve("settings.json");

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    public SettingsManager() {
        try {
            Files.createDirectories(CONFIG_DIR);
            load();
        } catch (IOException e) {
            log.warn("Could not create config directory", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (Files.exists(SETTINGS_FILE)) {
            try (BufferedReader reader = Files.newBufferedReader(SETTINGS_FILE, StandardCharsets.UTF_8)) {
                Map<String, Object> data = gson.fromJson(reader, Map.class);
                if (data != null) {
                    cache.putAll(data);
                }
            } catch (IOException e) {
                log.warn("Could not load settings", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        T val = (T) cache.get(key);
        return val != null ? val : defaultValue;
    }

    public void set(String key, Object value) {
        cache.put(key, value);
    }

    public void flush() {
        try (BufferedWriter writer = Files.newBufferedWriter(SETTINGS_FILE, StandardCharsets.UTF_8)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            log.error("Could not save settings", e);
        }
    }

    public Map<String, Object> all() {
        return Collections.unmodifiableMap(cache);
    }
}
