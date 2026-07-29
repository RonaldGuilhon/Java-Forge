package com.javaforge.ui.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class MonacoOfflineManager {

    private static final Logger log = LoggerFactory.getLogger(MonacoOfflineManager.class);

    private static final String MONACO_VERSION = "0.45.0";
    private static final String CDN_BASE = "https://cdn.jsdelivr.net/npm/monaco-editor@" + MONACO_VERSION + "/min/vs";
    private static final Path CACHE_DIR = Paths.get(System.getProperty("user.home"), ".javaforge", "editor", "vs");
    private static final Path VERSION_FILE = CACHE_DIR.resolve(".version");

    private static boolean initialized = false;

    public static synchronized void ensureCached() {
        if (initialized) return;
        initialized = true;
        try {
            Files.createDirectories(CACHE_DIR);
            if (isCacheValid()) {
                log.info("Monaco editor cache is valid at {}", CACHE_DIR);
                return;
            }
            log.info("Downloading Monaco editor v{} to local cache...", MONACO_VERSION);
            downloadFile("loader.js");
            downloadFile("editor/editor.main.js");
            downloadFile("editor/editor.main.nls.js");
            downloadFile("base/worker/workerMain.js");
            Files.write(VERSION_FILE, (MONACO_VERSION + "\n").getBytes(StandardCharsets.UTF_8));
            log.info("Monaco editor cached successfully");
        } catch (Exception e) {
            log.warn("Failed to cache Monaco editor offline (will use CDN fallback): {}", e.getMessage());
        }
    }

    private static boolean isCacheValid() {
        if (!Files.exists(VERSION_FILE)) return false;
        if (!Files.exists(CACHE_DIR.resolve("loader.js"))) return false;
        if (!Files.exists(CACHE_DIR.resolve("editor/editor.main.js"))) return false;
        try {
            String version = new String(Files.readAllBytes(VERSION_FILE), StandardCharsets.UTF_8).trim();
            return MONACO_VERSION.equals(version);
        } catch (IOException e) {
            return false;
        }
    }

    private static void downloadFile(String relativePath) {
        Path target = CACHE_DIR.resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            URL url = new URL(CDN_BASE + "/" + relativePath);
            try (InputStream in = url.openStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            log.debug("Downloaded: {}", relativePath);
        } catch (Exception e) {
            log.warn("Failed to download {}: {}", relativePath, e.getMessage());
        }
    }

    public static String getLocalBase() {
        Path base = CACHE_DIR.getParent();
        return base.toUri().toString();
    }

    public static String getCdnFallback() {
        return "https://cdn.jsdelivr.net/npm/monaco-editor@" + MONACO_VERSION + "/min/vs";
    }

    public static boolean isOfflineAvailable() {
        return Files.exists(CACHE_DIR.resolve("loader.js"));
    }
}
