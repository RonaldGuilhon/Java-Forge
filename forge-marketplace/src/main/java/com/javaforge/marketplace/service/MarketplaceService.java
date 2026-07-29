package com.javaforge.marketplace.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.javaforge.marketplace.model.PluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarketplaceService {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceService.class);
    private static final String REGISTRY_URL = "https://raw.githubusercontent.com/java-forge/marketplace/main/registry.json";
    private static final Path PLUGINS_DIR = Paths.get(System.getProperty("user.home"), ".javaforge", "plugins");

    private final Gson gson = new Gson();
    private List<PluginInfo> cache = new ArrayList<>();

    public MarketplaceService() {
        try {
            Files.createDirectories(PLUGINS_DIR);
        } catch (IOException e) {
            log.warn("Could not create plugins dir", e);
        }
    }

    public List<PluginInfo> fetchAvailable() {
        try {
            URL url = new URL(REGISTRY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            int status = conn.getResponseCode();
            if (status == 200) {
                String body = readAll(conn.getInputStream());
                java.lang.reflect.Type type = new TypeToken<List<PluginInfo>>() {}.getType();
                List<PluginInfo> remote = gson.fromJson(body, type);
                if (remote != null) {
                    cache = remote.stream()
                        .map(p -> new PluginInfo(
                            p.getId(), p.getName(), p.getVersion(), p.getAuthor(),
                            p.getDescription(), p.getCategory(), p.getDownloadUrl(),
                            p.getIconUrl(), p.getDownloads(), p.getRating(), p.getTags(),
                            isInstalled(p.getId())
                        ))
                        .collect(Collectors.toList());
                    return cache;
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch marketplace registry, using defaults", e);
        }
        return getDefaultPlugins();
    }

    private static String readAll(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public List<PluginInfo> getDefaultPlugins() {
        if (!cache.isEmpty()) return cache;
        cache = Arrays.asList(
            new PluginInfo("theme-monokai", "Monokai Pro", "1.0", "Java Forge",
                "Monokai Pro theme with enhanced contrast", "Theme",
                "", "", 1500, 4.8, Arrays.asList("theme", "dark"), false),
            new PluginInfo("theme-one-dark", "One Dark Pro", "1.2", "Java Forge",
                "Atom One Dark theme", "Theme",
                "", "", 1200, 4.7, Arrays.asList("theme", "dark"), false),
            new PluginInfo("lang-kotlin", "Kotlin Support", "2.0", "Java Forge",
                "Kotlin language support with LSP", "Language Support",
                "", "", 890, 4.5, Arrays.asList("language", "kotlin"), false),
            new PluginInfo("lang-python", "Python Support", "1.5", "Java Forge",
                "Python language support", "Language Support",
                "", "", 750, 4.3, Arrays.asList("language", "python"), false),
            new PluginInfo("gen-crud-advanced", "Advanced CRUD Generator", "1.0", "Java Forge",
                "Extended CRUD generator with React/Angular frontend", "Generator",
                "", "", 600, 4.6, Arrays.asList("generator", "crud"), false),
            new PluginInfo("docker-compose", "Docker Compose", "1.1", "Java Forge",
                "Docker Compose file generator for microservices", "Tool",
                "", "", 450, 4.4, Arrays.asList("docker", "devops"), false),
            new PluginInfo("ai-copilot", "AI Copilot Pro", "2.5", "Java Forge",
                "Enhanced AI assistant with code completion", "AI",
                "", "", 2000, 4.9, Arrays.asList("ai", "copilot"), false),
            new PluginInfo("db-viewer", "Database Visualizer", "1.3", "Java Forge",
                "Visual ER diagram viewer for database schemas", "Database",
                "", "", 340, 4.2, Arrays.asList("database", "diagram"), false)
        );
        return cache;
    }

    public boolean install(PluginInfo plugin) {
        Path targetDir = PLUGINS_DIR.resolve(plugin.getId());
        try {
            Files.createDirectories(targetDir);
            Path metaFile = targetDir.resolve("plugin.json");
            Files.write(metaFile, gson.toJson(plugin).getBytes(StandardCharsets.UTF_8));
            log.info("Installed plugin: {} v{}", plugin.getName(), plugin.getVersion());
            return true;
        } catch (IOException e) {
            log.error("Failed to install plugin {}", plugin.getId(), e);
            return false;
        }
    }

    public boolean uninstall(String pluginId) {
        Path targetDir = PLUGINS_DIR.resolve(pluginId);
        try {
            if (Files.exists(targetDir)) {
                try (Stream<Path> files = Files.walk(targetDir)) {
                    files.sorted(Comparator.reverseOrder())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
                }
                log.info("Uninstalled plugin: {}", pluginId);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to uninstall plugin {}", pluginId, e);
            return false;
        }
    }

    public boolean isInstalled(String pluginId) {
        return Files.exists(PLUGINS_DIR.resolve(pluginId));
    }

    public List<PluginInfo> getInstalled() {
        List<PluginInfo> list = new ArrayList<>();
        try (Stream<Path> files = Files.list(PLUGINS_DIR)) {
            files.filter(Files::isDirectory).forEach(dir -> {
                Path metaFile = dir.resolve("plugin.json");
                if (Files.exists(metaFile)) {
                    try {
                        byte[] data = Files.readAllBytes(metaFile);
                        PluginInfo info = gson.fromJson(new String(data, StandardCharsets.UTF_8), PluginInfo.class);
                        list.add(info);
                    } catch (IOException ignored) {}
                }
            });
        } catch (IOException ignored) {}
        return list;
    }

    public Path getPluginsDir() {
        return PLUGINS_DIR;
    }
}
