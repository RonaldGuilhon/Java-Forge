package com.javaforge.ai.service;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OpenAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final Gson gson = new Gson();
    private final String apiKey;
    private final String model;

    public OpenAIService(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model != null ? model : "gpt-4";
    }

    @Override
    public CompletableFuture<String> ask(String prompt) {
        return askWithContext("", prompt);
    }

    @Override
    public CompletableFuture<String> askWithContext(String systemContext, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> systemMsg = new HashMap<String, Object>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemContext.trim().isEmpty() ?
                    "You are an expert Java developer assistant embedded in an IDE. " +
                    "Provide concise, accurate code solutions." : systemContext);
                Map<String, Object> userMsg = new HashMap<String, Object>();
                userMsg.put("role", "user");
                userMsg.put("content", prompt);
                List<Map<String, Object>> messages = new ArrayList<Map<String, Object>>();
                messages.add(systemMsg);
                messages.add(userMsg);

                Map<String, Object> body = new HashMap<String, Object>();
                body.put("model", model);
                body.put("messages", messages);
                body.put("temperature", 0.2);
                body.put("max_tokens", 4096);

                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);

                String jsonInput = gson.toJson(body);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                int status = conn.getResponseCode();
                BufferedReader br;
                if (status >= 200 && status < 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                }
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line.trim());
                }
                br.close();
                conn.disconnect();

                Map<String, Object> json = gson.fromJson(sb.toString(), Map.class);

                List<Map<String, Object>> choices = (List<Map<String, Object>>) json.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
                    return message.get("content");
                }
                return "No response from AI";

            } catch (Exception e) {
                log.error("OpenAI API error", e);
                return "Error: " + e.getMessage();
            }
        });
    }

    @Override
    public String getProviderName() {
        return "OpenAI (" + model + ")";
    }
}
