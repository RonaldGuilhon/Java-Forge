package com.javaforge.core;

import com.javaforge.core.event.EventBus;
import com.javaforge.core.lifecycle.LifecycleManager;
import com.javaforge.core.plugin.PluginManager;
import com.javaforge.core.settings.SettingsManager;

public final class JavaForge {

    private static JavaForge instance;
    private final LifecycleManager lifecycleManager;
    private final EventBus eventBus;
    private final PluginManager pluginManager;
    private final SettingsManager settingsManager;

    private JavaForge() {
        this.eventBus = new EventBus();
        this.lifecycleManager = new LifecycleManager();
        this.settingsManager = new SettingsManager();
        this.pluginManager = new PluginManager();
    }

    public static JavaForge bootstrap() {
        if (instance == null) {
            instance = new JavaForge();
            instance.lifecycleManager.initialize();
            instance.pluginManager.loadPlugins();
            instance.pluginManager.startPlugins();
            instance.eventBus.post(new AppStartedEvent());
        }
        return instance;
    }

    public static JavaForge getInstance() {
        return instance;
    }

    public static LifecycleManager lifecycle() {
        return instance.lifecycleManager;
    }

    public static EventBus events() {
        return instance.eventBus;
    }

    public static PluginManager plugins() {
        return instance.pluginManager;
    }

    public static SettingsManager settings() {
        return instance.settingsManager;
    }

    public void shutdown() {
        eventBus.post(new AppShuttingDownEvent());
        pluginManager.stopPlugins();
        lifecycleManager.shutdown();
        settingsManager.flush();
    }

    public static final class AppStartedEvent {}
    public static final class AppShuttingDownEvent {}
}
