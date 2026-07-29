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
        var html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    html, body { margin: 0; padding: 0; height: 100%; overflow: hidden; }
                </style>
            </head>
            <body>
                <div id="container" style="width:100%;height:100%;"></div>
                <script src="%s"></script>
                <script>
                    require.config({ paths: { vs: '%s' } });
                    require(['vs/editor/editor.main'], function() {
                        var editor = monaco.editor.create(document.getElementById('container'), {
                            value: '',
                            language: 'java',
                            theme: 'vs-dark',
                            automaticLayout: true,
                            fontSize: 13,
                            fontFamily: 'Consolas, "Fira Code", monospace',
                            minimap: { enabled: true },
                            scrollBeyondLastLine: false,
                            wordWrap: 'off',
                            tabSize: 4,
                            insertSpaces: true,
                            bracketPairColorization: { enabled: true },
                            guides: { indentation: true, bracketPairs: true },
                            smoothScrolling: true,
                            cursorBlinking: 'smooth',
                            cursorSmoothCaretAnimation: 'on',
                            renderWhitespace: 'selection',
                            multiCursorModifier: 'ctrlCmd',
                            snippetSuggestions: 'inline',
                            suggestOnTriggerCharacters: true,
                            quickSuggestions: true,
                            codeLens: true,
                            folding: true,
                            foldingHighlight: true,
                            foldingStrategy: 'indentation',
                            autoClosingBrackets: 'always',
                            autoClosingQuotes: 'always',
                            formatOnPaste: true,
                            formatOnType: true,
                            selectionHighlight: true,
                            occurrencesHighlight: 'singleFile',
                            renderLineHighlight: 'all',
                            matchBrackets: 'always',
                            parameterHints: { enabled: true, cycle: true },
                            hover: { enabled: true, delay: 300 },
                            links: true,
                            contextmenu: true,
                            mouseWheelZoom: true,
                            dragAndDrop: true,
                            emptySelectionClipboard: true,
                            copyWithSyntaxHighlighting: true,
                            find: { addExtraSpaceOnTop: false }
                        });
                        window.__editor = editor;
                        window.__setContent = function(text) {
                            editor.setValue(text);
                        };
                        editor.onDidChangeModelContent(function() {
                            var val = editor.getValue();
                            window.__content = val;
                        });
                        editor.focus();
                    });
                </script>
            </body>
            </html>
            """.formatted(MONACO_CDN + "/loader.js", MONACO_CDN);

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
            var result = webView.getEngine().executeScript("window.__content");
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
