package com.javaforge.ui.layout;

import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class ConsolePanel extends BorderPane {

    private final TextArea console = new TextArea();

    public ConsolePanel() {
        getStyleClass().add("console-panel");
        console.setEditable(false);
        console.setWrapText(true);
        console.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #d4d4d4; -fx-font-family: Consolas;");
        console.setText("Java Forge Console\n");
        setCenter(console);
    }

    public void log(String message) {
        console.appendText(message + "\n");
    }

    public void clear() {
        console.clear();
    }
}
