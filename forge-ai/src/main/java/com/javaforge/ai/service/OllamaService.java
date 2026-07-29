package com.javaforge.ai.service;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OllamaService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);
    private static final String DEFAULT_URL = "http://localhost:11434/api/generate";

    private final Gson gson = new Gson();
    private final String baseUrl;
    private final String model;

    public OllamaService(String baseUrl, String model) {
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_URL;
        this.model = model != null ? model : "codellama";
    }

    @Override
    public CompletableFuture<String> ask(String prompt) {
        return askWithContext("", prompt);
    }

    @Override
    public CompletableFuture<String> askWithContext(String context, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fullPrompt = context.trim().isEmpty() ? prompt : context + "\n\n" + prompt;

                Map<String, Object> body = new HashMap<String, Object>();
                body.put("model", model);
                body.put("prompt", fullPrompt);
                body.put("stream", false);

                URL url = new URL(baseUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
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
                Object result = json.get("response");
                return result != null ? result.toString() : "No response from Ollama";

            } catch (Exception e) {
                log.error("Ollama API error", e);
                return "Error: " + e.getMessage();
            }
        });
    }

    @Override
    public String getProviderName() {
        return "Ollama (" + model + ")";
    }
}
