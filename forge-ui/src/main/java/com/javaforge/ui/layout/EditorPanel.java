package com.javaforge.ui.layout;

import com.javaforge.ui.editor.MonacoEditorView;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EditorPanel extends BorderPane {

    private final TabPane tabPane = new TabPane();
    private final Map<Tab, Path> tabToPath = new HashMap<>();
    private final Map<Tab, MonacoEditorView> tabToEditor = new HashMap<>();
    private int untitledCount = 0;

    public EditorPanel() {
        getStyleClass().add("editor-panel");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        openNewEditor();
        setCenter(tabPane);
    }

    public Tab openNewEditor() {
        untitledCount++;
        Tab tab = new Tab("Untitled-" + untitledCount);
        MonacoEditorView editor = new MonacoEditorView();
        tab.setContent(editor);
        tabToEditor.put(tab, editor);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }

    public Tab openFile(Path filePath, String content) {
        for (Map.Entry<Tab, Path> entry : tabToPath.entrySet()) {
            if (entry.getValue().equals(filePath)) {
                tabPane.getSelectionModel().select(entry.getKey());
                return entry.getKey();
            }
        }
        String fileName = filePath.getFileName().toString();
        Tab tab = new Tab(fileName);
        MonacoEditorView editor = new MonacoEditorView();
        editor.setContent(content);
        editor.setLanguage(detectLanguage(fileName));
        tab.setContent(editor);
        tabToPath.put(tab, filePath);
        tabToEditor.put(tab, editor);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }

    public MonacoEditorView getActiveEditor() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        return tab != null ? tabToEditor.get(tab) : null;
    }

    public Path getActiveFilePath() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        return tab != null ? tabToPath.get(tab) : null;
    }

    public Tab getActiveTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    public Path getPathForTab(Tab tab) {
        return tabToPath.get(tab);
    }

    public MonacoEditorView getEditorForTab(Tab tab) {
        return tabToEditor.get(tab);
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    private String detectLanguage(String fileName) {
        if (fileName.endsWith(".java")) return "java";
        if (fileName.endsWith(".xml")) return "xml";
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "html";
        if (fileName.endsWith(".css")) return "css";
        if (fileName.endsWith(".js")) return "javascript";
        if (fileName.endsWith(".json")) return "json";
        if (fileName.endsWith(".sql")) return "sql";
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) return "yaml";
        if (fileName.endsWith(".md")) return "markdown";
        if (fileName.endsWith(".properties")) return "plaintext";
        if (fileName.endsWith(".sh") || fileName.endsWith(".bat")) return "shell";
        if (fileName.endsWith(".py")) return "python";
        if (fileName.endsWith(".kt") || fileName.endsWith(".kts")) return "kotlin";
        if (fileName.endsWith(".groovy")) return "groovy";
        return "plaintext";
    }
}
