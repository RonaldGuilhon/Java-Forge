package com.javaforge.ui.layout;

import com.javaforge.ai.chat.AIChatPanel;
import com.javaforge.database.explorer.DatabaseExplorerPanel;
import com.javaforge.git.GitPanel;
import com.javaforge.marketplace.ui.MarketplacePanel;
import com.javaforge.server.ui.ServerPanel;
import com.javaforge.ui.editor.MonacoEditorView;
import com.javaforge.ui.theme.ThemeManager;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;

public class MainWindow extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    private final ThemeManager themeManager;
    private final MenuBarBuilder menuBar;
    private final StatusBar statusBar;
    private final ExplorerPanel explorer;
    private final EditorPanel editor;
    private final BottomPanel bottomPanel;
    private final AIChatPanel aiChatPanel;
    private final DatabaseExplorerPanel databasePanel;
    private final GitPanel gitPanel;
    private final ServerPanel serverPanel;
    private final MarketplacePanel marketplacePanel;

    private SplitPane mainSplit;
    private SplitPane sideSplit;
    private boolean aiPanelVisible = false;
    private Path currentProjectPath;

    public MainWindow(ThemeManager themeManager) {
        this.themeManager = themeManager;
        this.aiChatPanel = new AIChatPanel();
        this.databasePanel = new DatabaseExplorerPanel();
        this.gitPanel = new GitPanel();
        this.serverPanel = new ServerPanel();
        this.marketplacePanel = new MarketplacePanel();
        this.menuBar = new MenuBarBuilder(themeManager, this);
        this.statusBar = new StatusBar();
        this.explorer = new ExplorerPanel();
        this.editor = new EditorPanel();
        this.bottomPanel = new BottomPanel();

        aiChatPanel.setOpenFileContext(() -> {
            Path path = editor.getActiveFilePath();
            MonacoEditorView ed = editor.getActiveEditor();
            if (path != null && ed != null) {
                String content = ed.getContent();
                String preview = content.length() > 2000 ? content.substring(0, 2000) + "\n... (truncated)" : content;
                return "File: " + path.toString() + "\n\nContent:\n" + preview;
            }
            return "";
        });

        themeManager.addListener(sheets -> {
            boolean dark = themeManager.getCurrentTheme().isDark();
            bottomPanel.getTerminal().setDarkMode(dark);
            aiChatPanel.setDarkMode(dark);
        });

        explorer.setOnFileOpen(this::openFileInEditor);

        buildLayout();
        startCursorPoller();
    }

    private void buildLayout() {
        setTop(menuBar);

        sideSplit = new SplitPane();
        sideSplit.setOrientation(Orientation.HORIZONTAL);
        sideSplit.getItems().addAll(explorer, editor);
        SplitPane.setResizableWithParent(explorer, Boolean.FALSE);
        explorer.setPrefWidth(260);

        mainSplit = new SplitPane();
        mainSplit.setOrientation(Orientation.VERTICAL);
        mainSplit.getItems().addAll(sideSplit, bottomPanel);
        mainSplit.setDividerPositions(0.72);

        setCenter(mainSplit);
        setBottom(statusBar);
    }

    private void startCursorPoller() {
        Timer timer = new Timer("cursor-poller", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    MonacoEditorView active = editor.getActiveEditor();
                    if (active != null) {
                        try {
                            int line = active.getCursorLine();
                            int col = active.getCursorColumn();
                            statusBar.setCursorPosition(line, col);
                        } catch (Exception e) {
                            // WebView not ready yet
                        }
                    }
                });
            }
        }, 500, 500);
    }

    public void openFileInEditor(Path filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            String content = new String(bytes, StandardCharsets.UTF_8);
            editor.openFile(filePath, content);
            statusBar.setStatus("Opened: " + filePath.getFileName());
        } catch (IOException e) {
            log.error("Failed to open file: {}", filePath, e);
            bottomPanel.getConsole().log("Error opening file: " + e.getMessage());
        }
    }

    public void handleSave() {
        MonacoEditorView active = editor.getActiveEditor();
        Path filePath = editor.getActiveFilePath();
        if (active != null && filePath != null) {
            try {
                String content = active.getContent();
                Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
                statusBar.setStatus("Saved: " + filePath.getFileName());
                bottomPanel.getConsole().log("Saved: " + filePath);
            } catch (IOException e) {
                log.error("Failed to save file: {}", filePath, e);
                bottomPanel.getConsole().log("Error saving file: " + e.getMessage());
            }
        } else if (active != null) {
            handleSaveAs();
        }
    }

    public void handleSaveAs() {
        MonacoEditorView active = editor.getActiveEditor();
        if (active == null) return;
        Stage stage = (Stage) getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save As");
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                String content = active.getContent();
                Path path = file.toPath();
                Files.write(path, content.getBytes(StandardCharsets.UTF_8));
                editor.openFile(path, content);
                statusBar.setStatus("Saved: " + path.getFileName());
                bottomPanel.getConsole().log("Saved: " + path);
            } catch (IOException e) {
                log.error("Failed to save file", e);
                bottomPanel.getConsole().log("Error saving file: " + e.getMessage());
            }
        }
    }

    public void handleSaveAll() {
        for (Tab tab : editor.getTabPane().getTabs()) {
            Path path = editor.getPathForTab(tab);
            MonacoEditorView editorView = editor.getEditorForTab(tab);
            if (path != null && editorView != null) {
                try {
                    String content = editorView.getContent();
                    Files.write(path, content.getBytes(StandardCharsets.UTF_8));
                    bottomPanel.getConsole().log("Saved: " + path);
                } catch (IOException e) {
                    bottomPanel.getConsole().log("Error saving " + path + ": " + e.getMessage());
                }
            }
        }
        statusBar.setStatus("All files saved");
    }

    public void handleOpenFile() {
        Stage stage = (Stage) getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            openFileInEditor(file.toPath());
        }
    }

    public void handleBuild(String goal) {
        if (currentProjectPath == null) {
            bottomPanel.getConsole().log("No project open. Open a project first.");
            return;
        }
        Path projectDir = currentProjectPath;
        String mvnCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
        String[] cmd = { mvnCmd, goal };

        bottomPanel.getConsole().log("Running: mvn " + goal + " in " + projectDir);
        statusBar.setStatus("Building...");

        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(projectDir.toFile());
                pb.redirectErrorStream(true);
                Process process = pb.start();
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String logLine = line;
                        Platform.runLater(() -> bottomPanel.getConsole().log(logLine));
                    }
                }
                int exitCode = process.waitFor();
                Platform.runLater(() -> {
                    bottomPanel.getConsole().log("Build finished with exit code: " + exitCode);
                    statusBar.setStatus(exitCode == 0 ? "Build succeeded" : "Build failed (code " + exitCode + ")");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    bottomPanel.getConsole().log("Build error: " + e.getMessage());
                    statusBar.setStatus("Build error");
                });
            }
        }, "build-thread").start();
    }

    public void loadProject(Path path) {
        this.currentProjectPath = path;
        explorer.loadProject(path);
        statusBar.setStatus("Project loaded: " + path.getFileName());
        bottomPanel.getConsole().log("Opened project: " + path);
    }

    public void setCurrentProjectPath(Path path) {
        this.currentProjectPath = path;
    }

    public void toggleAiPanel() {
        if (aiPanelVisible) {
            sideSplit.getItems().remove(aiChatPanel);
            aiPanelVisible = false;
        } else {
            if (!sideSplit.getItems().contains(aiChatPanel)) {
                sideSplit.getItems().add(aiChatPanel);
            }
            aiChatPanel.setPrefWidth(350);
            aiPanelVisible = true;
        }
    }

    public void openDatabaseExplorer() {
        bottomPanel.openTab("DATABASE", databasePanel);
    }

    public void openGitPanel() {
        bottomPanel.openTab("GIT", gitPanel);
    }

    public void openServerPanel() {
        bottomPanel.openTab("SERVER", serverPanel);
    }

    public void openMarketplace() {
        bottomPanel.openTab("MARKETPLACE", marketplacePanel);
    }

    public AIChatPanel getAiChatPanel() {
        return aiChatPanel;
    }

    public EditorPanel getEditor() {
        return editor;
    }

    public ExplorerPanel getExplorer() {
        return explorer;
    }

    public BottomPanel getBottomPanel() {
        return bottomPanel;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }
}
