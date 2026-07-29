package com.javaforge.ui.layout;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.File;

public class ExplorerPanel extends BorderPane {

    private final TreeView<String> fileTree = new TreeView<>();
    private final Label header = new Label("EXPLORER");

    public ExplorerPanel() {
        getStyleClass().add("explorer-panel");

        header.getStyleClass().add("panel-header");
        TreeItem<String> root = new TreeItem<>("No workspace opened");
        root.setExpanded(true);
        fileTree.setRoot(root);
        fileTree.setShowRoot(true);

        setTop(header);
        setCenter(fileTree);
    }

    public void loadProject(java.nio.file.Path projectPath) {
        TreeItem<String> rootItem = new TreeItem<>(projectPath.getFileName().toString());
        rootItem.setExpanded(true);
        populateTree(projectPath.toFile(), rootItem);
        fileTree.setRoot(rootItem);
    }

    private void populateTree(File dir, TreeItem<String> parent) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            TreeItem<String> item = new TreeItem<>(file.getName());
            if (file.isDirectory()) {
                item.setExpanded(false);
                populateTree(file, item);
            }
            parent.getChildren().add(item);
        }
    }

    public TreeView<String> getFileTree() {
        return fileTree;
    }
}
