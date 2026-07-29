package com.javaforge.ui.layout;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class StatusBar extends BorderPane {

    private final Label leftStatus = new Label("Ready");
    private final Label cursorPosition = new Label("Ln 1, Col 1");
    private final Label encoding = new Label("UTF-8");
    private final Label language = new Label("Java");
    private final Label gitBranch = new Label("main");

    public StatusBar() {
        getStyleClass().add("status-bar");

        var left = new HBox(8, leftStatus, createSeparator(), gitBranch);
        var right = new HBox(8, language, createSeparator(), encoding, createSeparator(), cursorPosition);

        left.setPadding(new Insets(2, 8, 2, 8));
        right.setPadding(new Insets(2, 8, 2, 8));

        setLeft(left);
        setRight(right);
    }

    private Label createSeparator() {
        var sep = new Label("|");
        sep.getStyleClass().add("status-separator");
        return sep;
    }

    public void setStatus(String text) {
        leftStatus.setText(text);
    }

    public void setCursorPosition(int line, int col) {
        cursorPosition.setText("Ln " + line + ", Col " + col);
    }

    public void setLanguage(String lang) {
        language.setText(lang);
    }

    public void setGitBranch(String branch) {
        gitBranch.setText(branch);
    }
}
