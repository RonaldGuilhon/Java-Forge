package com.javaforge.core.plugin;

import com.javaforge.core.event.EventBus;
import com.javaforge.core.settings.SettingsManager;

public record PluginContext(
    String pluginDir,
    EventBus eventBus,
    SettingsManager settings
) {}
