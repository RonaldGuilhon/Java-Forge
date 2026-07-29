package com.javaforge.core.plugin;

public interface Plugin {
    String getId();
    String getName();
    String getVersion();
    void init(PluginContext context);
    void start();
    void stop();
}
