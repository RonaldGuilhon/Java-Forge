package com.javaforge.git;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.revwalk.RevCommit;

import java.nio.file.Path;
import java.util.List;

public class GitPanel extends BorderPane {

    private final GitService gitService = new GitService();
    private final TreeView<String> changesTree = new TreeView<>();
    private final Label branchLabel = new Label("No repo");
    private final TextArea commitArea = new TextArea();
    private final ListView<String> logList = new ListView<>();

    public GitPanel() {
        buildUI();
    }

    private void buildUI() {
        Label header = new Label("SOURCE CONTROL");
        header.getStyleClass().add("panel-header");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());

        Button commitBtn = new Button("Commit");
        commitBtn.setOnAction(e -> commit());

        Button pushBtn = new Button("Push");
        pushBtn.setOnAction(e -> push());

        Button pullBtn = new Button("Pull");
        pullBtn.setOnAction(e -> pull());

        ToolBar topBar = new ToolBar(branchLabel, refreshBtn);

        TreeItem<String> root = new TreeItem<>("Changes");
        root.setExpanded(true);
        changesTree.setRoot(root);
        changesTree.setShowRoot(true);

        commitArea.setPromptText("Commit message...");
        commitArea.setPrefRowCount(3);

        ToolBar actions = new ToolBar(commitBtn, pushBtn, pullBtn);

        VBox center = new VBox(topBar, changesTree, commitArea, actions);
        VBox.setVgrow(changesTree, Priority.ALWAYS);

        Label logHeader = new Label("HISTORY");
        logHeader.getStyleClass().add("panel-header");
        VBox bottom = new VBox(logHeader, logList);
        VBox.setVgrow(logList, Priority.ALWAYS);
        bottom.setPrefHeight(200);

        SplitPane split = new SplitPane(center, bottom);
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);

        VBox wrapper = new VBox(header, split);
        VBox.setVgrow(split, Priority.ALWAYS);
        setCenter(wrapper);
    }

    public void openRepo(Path path) {
        if (gitService.open(path)) {
            branchLabel.setText("Branch: " + gitService.branch());
            refresh();
        }
    }

    private void refresh() {
        if (!gitService.isOpen()) return;

        TreeItem<String> root = new TreeItem<>("Changes");
        root.setExpanded(true);
        Status status = gitService.status();
        if (status != null) {
            try {
                for (String file : status.getUntracked()) {
                    root.getChildren().add(new TreeItem<>("U: " + file));
                }
                for (String file : status.getModified()) {
                    root.getChildren().add(new TreeItem<>("M: " + file));
                }
                for (String file : status.getChanged()) {
                    root.getChildren().add(new TreeItem<>("C: " + file));
                }
                for (String file : status.getMissing()) {
                    root.getChildren().add(new TreeItem<>("D: " + file));
                }
            } catch (Exception ignored) {}
        }
        changesTree.setRoot(root);

        logList.getItems().clear();
        List<RevCommit> commits = gitService.log(20);
        for (RevCommit commit : commits) {
            logList.getItems().add(
                commit.getId().abbreviate(8).name() + " - " +
                commit.getShortMessage()
            );
        }
    }

    private void commit() {
        String msg = commitArea.getText();
        if (msg.trim().isEmpty()) return;
        RevCommit commit = gitService.commit(msg);
        if (commit != null) {
            commitArea.clear();
            refresh();
        }
    }

    private void push() {
        if (gitService.push()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Pushed successfully");
            alert.showAndWait();
        }
    }

    private void pull() {
        if (gitService.pull()) {
            refresh();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Pulled successfully");
            alert.showAndWait();
        }
    }
}
