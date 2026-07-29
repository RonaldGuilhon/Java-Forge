package com.javaforge.core.plugin;

import com.javaforge.core.JavaForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PluginManager {

    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);
    private final List<Plugin> plugins = new CopyOnWriteArrayList<>();

    public void register(Plugin plugin) {
        plugins.add(plugin);
        var ctx = new PluginContext(
            "plugins/" + plugin.id(),
            JavaForge.events(),
            JavaForge.settings()
        );
        plugin.init(ctx);
        log.info("Registered plugin: {} v{}", plugin.name(), plugin.version());
    }

    public void loadPlugins() {
        var loader = ServiceLoader.load(Plugin.class);
        for (Plugin plugin : loader) {
            register(plugin);
        }
    }

    public void startPlugins() {
        plugins.forEach(Plugin::start);
    }

    public void stopPlugins() {
        plugins.forEach(Plugin::stop);
    }

    public List<Plugin> getPlugins() {
        return List.copyOf(plugins);
    }

    public Optional<Plugin> findById(String id) {
        return plugins.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
