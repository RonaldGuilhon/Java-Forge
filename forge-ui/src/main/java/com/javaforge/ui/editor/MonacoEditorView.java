package com.javaforge.ui.editor;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

public class MonacoEditorView extends BorderPane {

    private static final String MONACO_CDN = "https://cdn.jsdelivr.net/npm/monaco-editor@0.45.0/min/vs";

    private final WebView webView = new WebView();
    private boolean initialized = false;

    public MonacoEditorView() {
        getStyleClass().add("monaco-editor");
        webView.setContextMenuEnabled(true);
        loadEditor();
        setCenter(webView);
    }

    private void loadEditor() {
        String loader = MONACO_CDN + "/loader.js";
        String vsPath = MONACO_CDN;
        String html = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <style>\n" +
            "        html, body { margin: 0; padding: 0; height: 100%; overflow: hidden; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div id=\"container\" style=\"width:100%;height:100%;\"></div>\n" +
            "    <script src=\"" + loader + "\"></script>\n" +
            "    <script>\n" +
            "        require.config({ paths: { vs: '" + vsPath + "' } });\n" +
            "        require(['vs/editor/editor.main'], function() {\n" +
            "            var editor = monaco.editor.create(document.getElementById('container'), {\n" +
            "                value: '',\n" +
            "                language: 'java',\n" +
            "                theme: 'vs-dark',\n" +
            "                automaticLayout: true,\n" +
            "                fontSize: 13,\n" +
            "                fontFamily: 'Consolas, \"Fira Code\", monospace',\n" +
            "                minimap: { enabled: true },\n" +
            "                scrollBeyondLastLine: false,\n" +
            "                wordWrap: 'off',\n" +
            "                tabSize: 4,\n" +
            "                insertSpaces: true,\n" +
            "                bracketPairColorization: { enabled: true },\n" +
            "                guides: { indentation: true, bracketPairs: true },\n" +
            "                smoothScrolling: true,\n" +
            "                cursorBlinking: 'smooth',\n" +
            "                cursorSmoothCaretAnimation: 'on',\n" +
            "                renderWhitespace: 'selection',\n" +
            "                multiCursorModifier: 'ctrlCmd',\n" +
            "                snippetSuggestions: 'inline',\n" +
            "                suggestOnTriggerCharacters: true,\n" +
            "                quickSuggestions: true,\n" +
            "                codeLens: true,\n" +
            "                folding: true,\n" +
            "                foldingHighlight: true,\n" +
            "                foldingStrategy: 'indentation',\n" +
            "                autoClosingBrackets: 'always',\n" +
            "                autoClosingQuotes: 'always',\n" +
            "                formatOnPaste: true,\n" +
            "                formatOnType: true,\n" +
            "                selectionHighlight: true,\n" +
            "                occurrencesHighlight: 'singleFile',\n" +
            "                renderLineHighlight: 'all',\n" +
            "                matchBrackets: 'always',\n" +
            "                parameterHints: { enabled: true, cycle: true },\n" +
            "                hover: { enabled: true, delay: 300 },\n" +
            "                links: true,\n" +
            "                contextmenu: true,\n" +
            "                mouseWheelZoom: true,\n" +
            "                dragAndDrop: true,\n" +
            "                emptySelectionClipboard: true,\n" +
            "                copyWithSyntaxHighlighting: true,\n" +
            "                find: { addExtraSpaceOnTop: false }\n" +
            "            });\n" +
            "            window.__editor = editor;\n" +
            "            window.__setContent = function(text) {\n" +
            "                editor.setValue(text);\n" +
            "            };\n" +
            "            editor.onDidChangeModelContent(function() {\n" +
            "                var val = editor.getValue();\n" +
            "                window.__content = val;\n" +
            "            });\n" +
            "            editor.focus();\n" +
            "        });\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";

        webView.getEngine().loadContent(html);
        webView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
            if (newDoc != null) {
                initialized = true;
            }
        });
    }

    public void setContent(String text) {
        if (initialized) {
            webView.getEngine().executeScript("window.__setContent('" + escape(text) + "')");
        } else {
            webView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    webView.getEngine().executeScript("window.__setContent('" + escape(text) + "')");
                }
            });
        }
    }

    public String getContent() {
        if (initialized) {
            Object result = webView.getEngine().executeScript("window.__content");
            return result != null ? result.toString() : "";
        }
        return "";
    }

    public WebView getWebView() {
        return webView;
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
