package com.javaforge.core.plugin;

import com.javaforge.core.event.EventBus;
import com.javaforge.core.settings.SettingsManager;

public class PluginContext {

    private final String pluginDir;
    private final EventBus eventBus;
    private final SettingsManager settings;

    public PluginContext(String pluginDir, EventBus eventBus, SettingsManager settings) {
        this.pluginDir = pluginDir;
        this.eventBus = eventBus;
        this.settings = settings;
    }

    public String getPluginDir() { return pluginDir; }
    public EventBus getEventBus() { return eventBus; }
    public SettingsManager getSettings() { return settings; }
}
