package com.javaforge.core.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LifecycleManager {

    private static final Logger log = LoggerFactory.getLogger(LifecycleManager.class);
    private final Map<String, LifecycleHook> hooks = new LinkedHashMap<>();
    private volatile State state = State.CREATED;

    public enum State { CREATED, INITIALIZING, RUNNING, SHUTTING_DOWN, TERMINATED }

    public void register(String name, LifecycleHook hook) {
        hooks.put(name, hook);
    }

    public void initialize() {
        state = State.INITIALIZING;
        hooks.forEach((name, hook) -> {
            try {
                hook.onInitialize();
                log.debug("Initialized lifecycle hook: {}", name);
            } catch (Exception e) {
                log.error("Failed to initialize hook: {}", name, e);
            }
        });
        state = State.RUNNING;
        log.info("Java Forge initialized successfully");
    }

    public void shutdown() {
        state = State.SHUTTING_DOWN;
        var reversed = new LinkedHashMap<>(hooks);
        var keys = reversed.keySet().stream().toList();
        for (int i = keys.size() - 1; i >= 0; i--) {
            var name = keys.get(i);
            try {
                hooks.get(name).onShutdown();
            } catch (Exception e) {
                log.error("Failed to shutdown hook: {}", name, e);
            }
        }
        state = State.TERMINATED;
    }

    public State getState() {
        return state;
    }
}
