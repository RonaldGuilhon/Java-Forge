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
        PluginContext ctx = new PluginContext(
            "plugins/" + plugin.getId(),
            JavaForge.events(),
            JavaForge.settings()
        );
        plugin.init(ctx);
        log.info("Registered plugin: {} v{}", plugin.getName(), plugin.getVersion());
    }

    public void loadPlugins() {
        ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class);
        for (Plugin plugin : loader) {
            register(plugin);
        }
    }

    public void startPlugins() {
        for (Plugin plugin : plugins) {
            plugin.start();
        }
    }

    public void stopPlugins() {
        for (Plugin plugin : plugins) {
            plugin.stop();
        }
    }

    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public Optional<Plugin> findById(String id) {
        for (Plugin p : plugins) {
            if (p.getId().equals(id)) return Optional.of(p);
        }
        return Optional.empty();
    }
}
