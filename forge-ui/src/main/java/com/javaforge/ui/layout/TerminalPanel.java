package com.javaforge.ui.layout;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TerminalPanel extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(TerminalPanel.class);

    private final TextArea outputArea = new TextArea();
    private final TextField inputField = new TextField();
    private Process shellProcess;
    private BufferedWriter processWriter;
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "terminal-io");
        t.setDaemon(true);
        return t;
    });

    public TerminalPanel() {
        getStyleClass().add("terminal-panel");
        buildUI();
        startShell();
    }

    private void buildUI() {
        outputArea.setEditable(false);
        outputArea.setFont(javafx.scene.text.Font.font("Consolas", 13));
        outputArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #d4d4d4;");
        outputArea.setWrapText(false);

        inputField.setFont(javafx.scene.text.Font.font("Consolas", 13));
        inputField.setStyle("-fx-control-inner-background: #252526; -fx-text-fill: #d4d4d4;");
        inputField.setPromptText("Type a command...");

        inputField.setOnAction(e -> sendCommand());

        Label prompt = new Label("$ ");
        prompt.setStyle("-fx-text-fill: #569cd6; -fx-font-family: Consolas; -fx-font-size: 13px;");
        HBox inputBar = new HBox(0, prompt, inputField);
        inputBar.setPadding(new Insets(4, 8, 8, 8));
        HBox.setHgrow(inputField, javafx.scene.layout.Priority.ALWAYS);

        setCenter(outputArea);
        setBottom(inputBar);
    }

    private void startShell() {
        executor.submit(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;
                if (os.contains("win")) {
                    pb = new ProcessBuilder("cmd.exe");
                } else {
                    pb = new ProcessBuilder("/bin/bash", "--login");
                }
                pb.redirectErrorStream(true);
                pb.environment().put("TERM", "xterm-256color");
                shellProcess = pb.start();
                processWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream(),
                        StandardCharsets.UTF_8));

                appendOutput("Java Forge Terminal (real process)\r\n");
                printWorkingDirectory();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        shellProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    char[] buf = new char[4096];
                    int read;
                    while ((read = reader.read(buf, 0, buf.length)) != -1) {
                        String text = new String(buf, 0, read);
                        appendOutput(text);
                    }
                }
            } catch (Exception e) {
                appendOutput("Terminal error: " + e.getMessage() + "\r\n");
                log.error("Terminal process error", e);
            }
        });
    }

    private void sendCommand() {
        String cmd = inputField.getText();
        inputField.clear();
        if (cmd == null || cmd.trim().isEmpty()) return;

        appendOutput("$ " + cmd + "\r\n");

        if (shellProcess != null && shellProcess.isAlive()) {
            executor.submit(() -> {
                try {
                    processWriter.write(cmd);
                    processWriter.newLine();
                    processWriter.flush();
                } catch (IOException e) {
                    appendOutput("Error: " + e.getMessage() + "\r\n");
                }
            });
        } else {
            appendOutput("Terminal process died. Restarting...\r\n");
            startShell();
        }
    }

    private void printWorkingDirectory() {
        try {
            if (shellProcess != null && shellProcess.isAlive()) {
                processWriter.write("cd");
                processWriter.newLine();
                processWriter.flush();
            }
        } catch (IOException e) {
            log.warn("Failed to print working directory", e);
        }
    }

    private void appendOutput(String text) {
        Platform.runLater(() -> {
            outputArea.appendText(text);
            outputArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    public void setDarkMode(boolean dark) {
        if (dark) {
            outputArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #d4d4d4;");
            inputField.setStyle("-fx-control-inner-background: #252526; -fx-text-fill: #d4d4d4;");
        } else {
            outputArea.setStyle("-fx-control-inner-background: #ffffff; -fx-text-fill: #333333;");
            inputField.setStyle("-fx-control-inner-background: #f3f3f3; -fx-text-fill: #333333;");
        }
    }

    public void write(String text) {
        appendOutput(text);
    }

    public void write(String text, String color) {
        appendOutput("[" + color + "] " + text + "\r\n");
    }
}
