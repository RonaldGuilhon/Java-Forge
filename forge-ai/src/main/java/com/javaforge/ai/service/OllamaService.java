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
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(60000);

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

                String raw = sb.toString();
                Map<String, Object> json = gson.fromJson(raw, Map.class);

                Object error = json.get("error");
                if (error != null) {
                    return "Ollama error: " + error.toString();
                }

                Object result = json.get("response");
                if (result != null) {
                    return result.toString();
                }

                return "No response from Ollama. Verify the model is downloaded and Ollama is running.\nRaw response: " + raw;

            } catch (java.net.ConnectException e) {
                return "Error: Cannot connect to Ollama at " + baseUrl + ".\nMake sure Ollama is installed and running (https://ollama.com).";
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
