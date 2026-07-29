package com.javaforge.server.manager;

import com.javaforge.server.model.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

public class ServerManager {

    private static final Logger log = LoggerFactory.getLogger(ServerManager.class);
    private Process serverProcess;
    private ServerConfig config;
    private Thread outputThread;
    private Thread errorThread;

    public ServerManager(ServerConfig config) {
        this.config = config;
    }

    public synchronized boolean start() {
        if (isRunning()) {
            log.warn("Server already running");
            return false;
        }
        try {
            Path home = Paths.get(config.getHomeDir());
            if (!Files.exists(home)) {
                log.error("Server home not found: {}", config.getHomeDir());
                return false;
            }

            boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
            String scriptName = isWin ? config.getType().getWinScript() : config.getType().getUnixScript();

            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(home.toFile());

            ServerConfig.ServerType type = config.getType();
            if (type == ServerConfig.ServerType.TOMCAT) {
                Path bin = home.resolve("bin");
                Path script = bin.resolve(scriptName);
                if (isWin) {
                    pb.command("cmd.exe", "/c", script.toString(), "run");
                } else {
                    pb.command("sh", script.toString(), "run");
                }
            } else if (type == ServerConfig.ServerType.WILDFLY) {
                Path bin = home.resolve("bin");
                Path script = bin.resolve(scriptName);
                if (isWin) {
                    pb.command("cmd.exe", "/c", script.toString());
                } else {
                    pb.command("sh", script.toString());
                }
            } else if (type == ServerConfig.ServerType.JETTY) {
                Path jar = home.resolve("start.jar");
                pb.command("java", "-jar", jar.toString());
            } else {
                log.warn("Auto-start not fully implemented for {}", config.getType());
                return false;
            }

            Map<String, String> env = pb.environment();
            env.put("JAVA_HOME", config.getJavaHome());
            env.put("CATALINA_HOME", config.getHomeDir());
            env.put("CATALINA_BASE", config.getHomeDir());

            pb.redirectErrorStream(false);
            serverProcess = pb.start();

            outputThread = new Thread(() -> captureOutput(serverProcess.getInputStream(), false));
            errorThread = new Thread(() -> captureOutput(serverProcess.getErrorStream(), true));
            outputThread.setDaemon(true);
            errorThread.setDaemon(true);
            outputThread.start();
            errorThread.start();

            log.info("Server {} started", config.getType().getLabel());
            return true;

        } catch (Exception e) {
            log.error("Failed to start server", e);
            return false;
        }
    }

    public synchronized boolean stop() {
        if (!isRunning()) return false;
        try {
            if (serverProcess != null) {
                serverProcess.destroyForcibly();
                serverProcess.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
                serverProcess = null;
            }
            log.info("Server stopped");
            return true;
        } catch (Exception e) {
            log.error("Failed to stop server", e);
            return false;
        }
    }

    public boolean isRunning() {
        return serverProcess != null && serverProcess.isAlive();
    }

    public boolean deploy(Path warFile) {
        if (config.getDeployDir().isEmpty()) {
            log.warn("Deploy directory not configured");
            return false;
        }
        try {
            Path deployDir = Paths.get(config.getDeployDir());
            Files.createDirectories(deployDir);
            Path target = deployDir.resolve(warFile.getFileName());
            Files.copy(warFile, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Deployed {} to {}", warFile.getFileName(), deployDir);
            return true;
        } catch (Exception e) {
            log.error("Deploy failed", e);
            return false;
        }
    }

    public boolean undeploy(String warName) {
        if (config.getDeployDir().isEmpty()) return false;
        try {
            Files.deleteIfExists(Paths.get(config.getDeployDir(), warName));
            return true;
        } catch (Exception e) {
            log.error("Undeploy failed", e);
            return false;
        }
    }

    public String getLogs() {
        return "Server logs will appear in the console panel";
    }

    private void captureOutput(InputStream stream, boolean isError) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isError) {
                    log.warn("[SERVER-ERR] {}", line);
                } else {
                    log.info("[SERVER-OUT] {}", line);
                }
            }
        } catch (IOException e) {
            if (!e.getMessage().contains("Stream closed")) {
                log.error("Error capturing output", e);
            }
        }
    }

    public void setConfig(ServerConfig config) {
        this.config = config;
    }

    public ServerConfig getConfig() {
        return config;
    }
}
