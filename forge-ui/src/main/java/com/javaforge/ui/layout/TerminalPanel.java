package com.javaforge.ui.layout;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

public class TerminalPanel extends BorderPane {

    private final WebView terminalView = new WebView();

    public TerminalPanel() {
        getStyleClass().add("terminal-panel");
        terminalView.getEngine().loadContent("""
            <html><body style="background:#1e1e1e;color:#d4d4d4;font-family:Consolas,monospace;padding:8px;">
            <div id="output">
              <span style="color:#569cd6;">Java Forge Terminal</span><br>
              <span style="color:#6a9955;">Ready</span><br>
            </div>
            <script>
              var output = document.getElementById('output');
              function writeLine(text, color) {
                output.innerHTML += '<span style=\"color:' + (color || '#d4d4d4') + ';\">' + text + '</span><br>';
              }
            </script>
            </body></html>
            """);
        setCenter(terminalView);
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
