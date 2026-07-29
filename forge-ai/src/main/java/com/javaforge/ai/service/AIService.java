package com.javaforge.ai.service;

import java.util.concurrent.CompletableFuture;

public interface AIService {
    CompletableFuture<String> ask(String prompt);
    CompletableFuture<String> askWithContext(String context, String prompt);
    String getProviderName();
}
