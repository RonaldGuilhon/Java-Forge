package com.javaforge.core.plugin;

import com.javaforge.core.event.EventBus;

public interface Plugin {
    String id();
    String name();
    String version();
    void init(PluginContext context);
    void start();
    void stop();
}
