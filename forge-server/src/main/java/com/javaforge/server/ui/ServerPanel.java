package com.javaforge.server.ui;

import com.javaforge.server.manager.ServerManager;
import com.javaforge.server.model.ServerConfig;
import com.javaforge.server.model.ServerConfig.ServerType;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;

public class ServerPanel extends BorderPane {

    private final ServerManager serverManager;
    private ServerConfig config = new ServerConfig();

    private final ComboBox<ServerType> typeBox = new ComboBox<>();
    private final TextField homeField = new TextField();
    private final TextField deployField = new TextField();
    private final TextField portField = new TextField("8080");
    private final TextField debugPortField = new TextField("5005");
    private final TextField minMemField = new TextField("256");
    private final TextField maxMemField = new TextField("1024");
    private final TextField jvmArgsField = new TextField();
    private final Label statusLabel = new Label("Stopped");
    private final TextArea logArea = new TextArea();
    private final Button startBtn = new Button("Start");
    private final Button stopBtn = new Button("Stop");
    private final Button deployBtn = new Button("Deploy WAR");

    public ServerPanel() {
        this.serverManager = new ServerManager(config);
        buildUI();
    }

    private void buildUI() {
        Label header = new Label("SERVER MANAGER");
        header.getStyleClass().add("panel-header");

        typeBox.getItems().addAll(ServerType.values());
        typeBox.setValue(ServerType.TOMCAT);
        typeBox.setOnAction(e -> updateDefaults());

        Button browseHomeBtn = new Button("...");
        browseHomeBtn.setOnAction(e -> {
            javafx.stage.DirectoryChooser chooser = new javafx.stage.DirectoryChooser();
            Window window = getScene() != null ? getScene().getWindow() : null;
            File dir = chooser.showDialog(window);
            if (dir != null) homeField.setText(dir.getAbsolutePath());
        });
        HBox homeRow = new HBox(4, homeField, browseHomeBtn);
        HBox.setHgrow(homeField, Priority.ALWAYS);

        Button browseDeployBtn = new Button("...");
        browseDeployBtn.setOnAction(e -> {
            javafx.stage.DirectoryChooser chooser = new javafx.stage.DirectoryChooser();
            Window window = getScene() != null ? getScene().getWindow() : null;
            File dir = chooser.showDialog(window);
            if (dir != null) deployField.setText(dir.getAbsolutePath());
        });
        HBox deployRow = new HBox(4, deployField, browseDeployBtn);
        HBox.setHgrow(deployField, Priority.ALWAYS);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.setPadding(new Insets(10));
        int r = 0;
        form.addRow(r++, new Label("Type:"), typeBox);
        form.addRow(r++, new Label("Home:"), homeRow);
        form.addRow(r++, new Label("Deploy Dir:"), deployRow);
        form.addRow(r++, new Label("HTTP Port:"), portField);
        form.addRow(r++, new Label("Debug Port:"), debugPortField);
        form.addRow(r++, new Label("Min Mem (MB):"), minMemField);
        form.addRow(r++, new Label("Max Mem (MB):"), maxMemField);
        form.addRow(r++, new Label("JVM Args:"), jvmArgsField);
        form.addRow(r++, new Label("Status:"), statusLabel);

        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ce9178;");

        startBtn.setOnAction(e -> startServer());
        stopBtn.setOnAction(e -> stopServer());
        stopBtn.setDisable(true);
        deployBtn.setOnAction(e -> deployWar());

        ToolBar toolbar = new ToolBar(startBtn, stopBtn, deployBtn);
        logArea.setEditable(false);
        logArea.setPromptText("Server logs...");

        TitledPane configSection = new TitledPane("Configuration", form);
        configSection.setExpanded(true);

        VBox center = new VBox(configSection, toolbar, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        VBox wrapper = new VBox(header, center);
        VBox.setVgrow(center, Priority.ALWAYS);
        setCenter(wrapper);
    }

    private void updateDefaults() {
        ServerType type = typeBox.getValue();
        if (type == null) return;
        String port;
        if (type == ServerType.TOMCAT || type == ServerType.JETTY || type == ServerType.WILDFLY) {
            port = "8080";
        } else if (type == ServerType.PAYARA || type == ServerType.GLASSFISH) {
            port = "4848";
        } else {
            port = "8080";
        }
        portField.setText(port);
        debugPortField.setText("5005");
    }

    private void readConfig() {
        config.setType(typeBox.getValue());
        config.setHomeDir(homeField.getText());
        config.setDeployDir(deployField.getText());
        try { config.setHttpPort(Integer.parseInt(portField.getText())); } catch (Exception ignored) {}
        try { config.setDebugPort(Integer.parseInt(debugPortField.getText())); } catch (Exception ignored) {}
        try { config.setMinMemory(Integer.parseInt(minMemField.getText())); } catch (Exception ignored) {}
        try { config.setMaxMemory(Integer.parseInt(maxMemField.getText())); } catch (Exception ignored) {}
        config.setJvmArgs(jvmArgsField.getText());
        serverManager.setConfig(config);
    }

    private void startServer() {
        readConfig();
        new Thread(() -> {
            boolean ok = serverManager.start();
            javafx.application.Platform.runLater(() -> {
                if (ok) {
                    statusLabel.setText("Running on port " + config.getHttpPort());
                    statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4ec9b0;");
                    startBtn.setDisable(true);
                    stopBtn.setDisable(false);
                    logArea.appendText("Server started on port " + config.getHttpPort() + "\n");
                } else {
                    logArea.appendText("Failed to start server\n");
                }
            });
        }).start();
    }

    private void stopServer() {
        new Thread(() -> {
            boolean ok = serverManager.stop();
            javafx.application.Platform.runLater(() -> {
                statusLabel.setText("Stopped");
                statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ce9178;");
                startBtn.setDisable(false);
                stopBtn.setDisable(true);
                logArea.appendText("Server stopped\n");
            });
        }).start();
    }

    private void deployWar() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAR files", "*.war"));
        Window window = getScene() != null ? getScene().getWindow() : null;
        File file = chooser.showOpenDialog(window);
        if (file != null) {
            if (serverManager.deploy(file.toPath())) {
                logArea.appendText("Deployed: " + file.getName() + "\n");
            } else {
                logArea.appendText("Deploy failed\n");
            }
        }
    }
}
