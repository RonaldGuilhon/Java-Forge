package com.javaforge.workspace.watch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(FileWatcher.class);

    private final Path rootPath;
    private final List<Consumer<WatchEvent<?>>> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean running = false;
    private Thread watchThread;

    public FileWatcher(Path rootPath) {
        this.rootPath = rootPath;
    }

    public void addListener(Consumer<WatchEvent<?>> listener) {
        listeners.add(listener);
    }

    public void start() {
        if (running) return;
        running = true;
        watchThread = new Thread(this, "workspace-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
    }

    public void stop() {
        running = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
    }

    @Override
    public void run() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            registerAll(watcher, rootPath);

            while (running) {
                WatchKey key;
                try {
                    key = watcher.poll(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == OVERFLOW) continue;
                    notifyListeners(event);
                }

                key.reset();
            }
        } catch (IOException e) {
            log.error("File watcher failed", e);
        }
    }

    private void registerAll(WatchService watcher, Path dir) throws IOException {
        Files.walk(dir)
                .filter(Files::isDirectory)
                .filter(p -> !isIgnored(p))
                .forEach(p -> {
                    try {
                        p.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    } catch (IOException e) {
                        log.warn("Could not register directory: {}", p, e);
                    }
                });
    }

    private boolean isIgnored(Path path) {
        String name = path.getFileName().toString();
        return name.equals(".git")
                || name.equals("target")
                || name.equals("build")
                || name.equals("node_modules")
                || name.startsWith(".");
    }

    private void notifyListeners(WatchEvent<?> event) {
        for (Consumer<WatchEvent<?>> listener : listeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.warn("FileWatcher listener error", e);
            }
        }
    }
}
