package com.javaforge.ai.chat;

import com.javaforge.ai.service.AIService;
import com.javaforge.ai.service.OpenAIService;
import com.javaforge.ai.service.OllamaService;
import com.javaforge.workspace.WorkspaceManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class AIChatPanel extends BorderPane {

    private final WebView chatView = new WebView();
    private final TextArea inputArea = new TextArea();
    private final ComboBox<String> providerBox = new ComboBox<>();
    private AIService aiService;
    private final StringBuilder chatHistory = new StringBuilder();
    private boolean darkMode = true;
    private String ollamaUrl = "http://localhost:11434/api/generate";
    private String ollamaModel = "codellama";

    public AIChatPanel() {
        buildUI();
        setupDefaultService();
    }

    private void buildUI() {
        Label header = new Label("AI ASSISTANT");
        header.getStyleClass().add("panel-header");

        providerBox.getItems().addAll("OpenAI GPT-4", "OpenAI GPT-3.5", "Ollama (Local)");
        providerBox.setValue("Ollama (Local)");
        providerBox.setOnAction(e -> setupService());

        Button configBtn = new Button("Configure");
        configBtn.setOnAction(e -> showConfigDialog());

        HBox topBar = new HBox(8, providerBox, configBtn);
        topBar.setPadding(new Insets(8));

        loadChatContent();

        inputArea.setPromptText("Ask AI to write code, explain, refactor...");
        inputArea.setPrefRowCount(3);

        Button sendBtn = new Button("Send");
        sendBtn.setDefaultButton(true);
        sendBtn.setOnAction(e -> sendMessage());

        BorderPane inputBar = new BorderPane();
        inputBar.setCenter(inputArea);
        inputBar.setRight(sendBtn);
        BorderPane.setMargin(sendBtn, new Insets(0, 0, 0, 8));

        VBox bottom = new VBox(topBar, inputBar);
        setTop(header);
        setCenter(chatView);
        setBottom(bottom);
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        loadChatContent();
    }

    private void loadChatContent() {
        String bg = darkMode ? "#1e1e1e" : "#f3f3f3";
        String fg = darkMode ? "#d4d4d4" : "#333333";
        String userBg = darkMode ? "#2d2d2d" : "#ffffff";
        String aiBg = darkMode ? "#1e1e1e" : "#f3f3f3";
        String accent = darkMode ? "#569cd6" : "#0066b8";
        String subtitle = darkMode ? "#6a9955" : "#388e3c";

        chatView.getEngine().loadContent(
            "<html><body style=\"background:" + bg + ";color:" + fg + ";font-family:Segoe UI,sans-serif;padding:12px;\">\n" +
            "<div id=\"messages\">\n" +
            "  <div style=\"color:" + accent + ";font-weight:bold;margin-bottom:12px;\">\n" +
            "    Java Forge AI Assistant<br>\n" +
            "    <span style=\"color:" + subtitle + ";font-size:12px;font-weight:normal;\">Ask me anything about your code</span>\n" +
            "  </div>\n" +
            "</div>\n" +
            "<script>\n" +
            "  function addMessage(role, text, color) {\n" +
            "    var div = document.getElementById('messages');\n" +
            "    var html = '<div style=\"margin:8px 0;padding:8px;background:' +\n" +
            "      (role === 'user' ? '" + userBg + "' : '" + aiBg + "') +\n" +
            "      ';border-left:3px solid ' + color + ';\">' +\n" +
            "      '<b style=\"color:' + color + ';\">' + role + ':</b><br>' +\n" +
            "      '<pre style=\"white-space:pre-wrap;margin:4px 0;font-family:Consolas;font-size:13px;\">' + text + '</pre></div>';\n" +
            "    div.innerHTML += html;\n" +
            "  }\n" +
            "</script>\n" +
            "</body></html>\n"
        );
    }

    private void setupDefaultService() {
        aiService = new OllamaService(ollamaUrl, ollamaModel);
    }

    private void setupService() {
        String selected = providerBox.getValue();
        if (selected == null) return;
        if (selected.startsWith("OpenAI")) {
            String model = selected.contains("GPT-4") ? "gpt-4" : "gpt-3.5-turbo";
            String key = System.getenv("OPENAI_API_KEY");
            if (key == null || key.trim().isEmpty()) {
                aiService = new OpenAIService("sk-placeholder", model);
            } else {
                aiService = new OpenAIService(key, model);
            }
        } else {
            aiService = new OllamaService(ollamaUrl, ollamaModel);
        }
    }

    private void showConfigDialog() {
        Dialog<Void> dialog = new Dialog<Void>();
        dialog.setTitle("AI Configuration");
        dialog.setHeaderText("Configure AI Providers");

        TextField keyField = new TextField(System.getenv("OPENAI_API_KEY"));
        TextField ollamaUrlField = new TextField(ollamaUrl);
        TextField ollamaModelField = new TextField(ollamaModel);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(20));

        TitledPane openaiSection = new TitledPane("OpenAI", new VBox(8,
            new Label("API Key:"), keyField
        ));
        openaiSection.setExpanded(true);

        TitledPane ollamaSection = new TitledPane("Ollama (Local)", new VBox(8,
            new Label("URL:"), ollamaUrlField,
            new Label("Model:"), ollamaModelField
        ));
        ollamaSection.setExpanded(true);

        VBox content = new VBox(12, openaiSection, ollamaSection);
        grid.addRow(0, content);
        GridPane.setColumnSpan(content, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String key = keyField.getText();
                if (!key.trim().isEmpty()) {
                    System.setProperty("OPENAI_API_KEY", key);
                }
                ollamaUrl = ollamaUrlField.getText().trim();
                ollamaModel = ollamaModelField.getText().trim();
                setupService();
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void sendMessage() {
        String text = inputArea.getText().trim();
        if (text.isEmpty() || aiService == null) return;
        inputArea.clear();

        appendMessage("You", text, "#569cd6");

        WorkspaceManager.getInstance().getProjectContext(context -> {
            String augmented = text;
            if (context != null && !context.isEmpty()) {
                augmented = "Project context:\n" + context + "\n\nUser question:\n" + text;
            }
            final String finalText = augmented;
            aiService.ask(finalText).thenAccept(response -> {
                Platform.runLater(() -> appendMessage("AI", response, "#ce9178"));
            });
        });
    }

    private void appendMessage(String role, String text, String color) {
        String escaped = text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
            .replace("\n", "\\n");

        chatView.getEngine().executeScript(
            "addMessage('" + role + "','" + escaped + "','" + color + "')");
    }

    public WebView getChatView() {
        return chatView;
    }
}
