package com.javaforge.ui.layout;

import com.javaforge.ui.editor.MonacoEditorView;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

public class EditorPanel extends BorderPane {

    private final TabPane tabPane = new TabPane();
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
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }

    public Tab openFile(String fileName, String content) {
        Tab tab = new Tab(fileName);
        MonacoEditorView editor = new MonacoEditorView();
        editor.setContent(content);
        tab.setContent(editor);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }

    public TabPane getTabPane() {
        return tabPane;
    }
}
