package com.javaforge.ui.layout;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

public class TerminalPanel extends BorderPane {

    private final WebView terminalView = new WebView();
    private boolean darkMode = true;

    public TerminalPanel() {
        getStyleClass().add("terminal-panel");
        loadContent();
        setCenter(terminalView);
    }

    private void loadContent() {
        String bg = darkMode ? "#1e1e1e" : "#ffffff";
        String fg = darkMode ? "#d4d4d4" : "#333333";
        String accent = darkMode ? "#569cd6" : "#0066b8";
        String success = darkMode ? "#6a9955" : "#388e3c";
        terminalView.getEngine().loadContent(
            "<html><body style=\"background:" + bg + ";color:" + fg + ";font-family:Consolas,monospace;padding:8px;\">\n" +
            "<div id=\"output\">\n" +
            "  <span style=\"color:" + accent + ";\">Java Forge Terminal</span><br>\n" +
            "  <span style=\"color:" + success + ";\">Ready</span><br>\n" +
            "</div>\n" +
            "<script>\n" +
            "  var output = document.getElementById('output');\n" +
            "  function writeLine(text, color) {\n" +
            "    output.innerHTML += '<span style=\"color:' + (color || '" + fg + "') + ';\">' + text + '</span><br>';\n" +
            "  }\n" +
            "</script>\n" +
            "</body></html>"
        );
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        loadContent();
    }

    public void write(String text) {
        terminalView.getEngine().executeScript("writeLine('" + escape(text) + "')");
    }

    public void write(String text, String color) {
        terminalView.getEngine().executeScript("writeLine('" + escape(text) + "','" + color + "')");
    }

    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
    }

    public WebView getWebView() {
        return terminalView;
    }
}
