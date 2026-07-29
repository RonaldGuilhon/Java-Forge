package com.javaforge.ui.editor;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

public class MonacoEditorView extends BorderPane {

    private final WebView webView = new WebView();
    private boolean initialized = false;

    public MonacoEditorView() {
        getStyleClass().add("monaco-editor");
        webView.setContextMenuEnabled(true);
        MonacoOfflineManager.ensureCached();
        loadEditor();
        setCenter(webView);
    }

    private void loadEditor() {
        String vsPath;
        String loader;
        if (MonacoOfflineManager.isOfflineAvailable()) {
            String localBase = MonacoOfflineManager.getLocalBase();
            vsPath = localBase + "vs";
            loader = localBase + "vs/loader.js";
        } else {
            vsPath = MonacoOfflineManager.getCdnFallback();
            loader = vsPath + "/loader.js";
        }

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
            "            window.__content = '';\n" +
            "            window.__setContent = function(text) {\n" +
            "                editor.setValue(text);\n" +
            "                window.__content = text;\n" +
            "            };\n" +
            "            window.__setLanguage = function(lang) {\n" +
            "                var model = editor.getModel();\n" +
            "                if (model) monaco.editor.setModelLanguage(model, lang);\n" +
            "            };\n" +
            "            editor.onDidChangeModelContent(function() {\n" +
            "                window.__content = editor.getValue();\n" +
            "            });\n" +
            "            editor.onDidChangeCursorPosition(function(e) {\n" +
            "                window.__cursorLine = e.position.lineNumber;\n" +
            "                window.__cursorCol = e.position.column;\n" +
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
        String escaped = escapeJson(text);
        String script = "if (window.__setContent) { window.__setContent(" + escaped + "); }";
        if (initialized) {
            webView.getEngine().executeScript(script);
        } else {
            webView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    webView.getEngine().executeScript(script);
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

    public void setLanguage(String language) {
        if (initialized) {
            webView.getEngine().executeScript("if (window.__setLanguage) window.__setLanguage('" + language + "')");
        } else {
            webView.getEngine().documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    webView.getEngine().executeScript("if (window.__setLanguage) window.__setLanguage('" + language + "')");
                }
            });
        }
    }

    public int getCursorLine() {
        Object result = webView.getEngine().executeScript("window.__cursorLine || 1");
        return result != null ? Integer.parseInt(result.toString()) : 1;
    }

    public int getCursorColumn() {
        Object result = webView.getEngine().executeScript("window.__cursorCol || 1");
        return result != null ? Integer.parseInt(result.toString()) : 1;
    }

    public WebView getWebView() {
        return webView;
    }

    private String escapeJson(String text) {
        if (text == null) return "\"\"";
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
