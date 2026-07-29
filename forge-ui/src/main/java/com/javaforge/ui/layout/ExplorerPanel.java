package com.javaforge.ui.layout;

import com.javaforge.workspace.WorkspaceManager;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ExplorerPanel extends BorderPane {

    private final TreeView<Path> fileTree = new TreeView<>();
    private final Label header = new Label("EXPLORER");
    private Consumer<Path> onFileOpen;

    public ExplorerPanel() {
        getStyleClass().add("explorer-panel");

        header.getStyleClass().add("panel-header");
        TreeItem<Path> root = new TreeItem<>(null);
        root.setExpanded(true);
        fileTree.setRoot(root);
        fileTree.setShowRoot(true);

        fileTree.setCellFactory(tv -> new TreeCell<Path>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(item == null && getTreeItem() == fileTree.getRoot() ? "No workspace opened" : null);
                } else {
                    setText(item.getFileName().toString());
                }
            }
        });

        fileTree.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TreeItem<Path> selected = fileTree.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getValue() != null && !Files.isDirectory(selected.getValue())) {
                    if (onFileOpen != null) {
                        onFileOpen.accept(selected.getValue());
                    }
                }
            }
        });

        setTop(header);
        setCenter(fileTree);
    }

    public void setOnFileOpen(Consumer<Path> callback) {
        this.onFileOpen = callback;
    }

    public void loadProject(Path projectPath) {
        TreeItem<Path> rootItem = new TreeItem<>(projectPath);
        rootItem.setExpanded(true);
        populateTree(projectPath.toFile(), rootItem);
        fileTree.setRoot(rootItem);

        Platform.runLater(() ->
            WorkspaceManager.getInstance().openWorkspace(projectPath)
        );
    }

    private void populateTree(File dir, TreeItem<Path> parent) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            TreeItem<Path> item = new TreeItem<>(file.toPath());
            if (file.isDirectory()) {
                item.setExpanded(false);
                populateTree(file, item);
            }
            parent.getChildren().add(item);
        }
    }

    public TreeView<Path> getFileTree() {
        return fileTree;
    }
}
