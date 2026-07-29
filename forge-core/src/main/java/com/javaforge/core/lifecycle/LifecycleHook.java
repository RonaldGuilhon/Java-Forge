package com.javaforge.core.lifecycle;

public interface LifecycleHook {
    void onInitialize();
    void onShutdown();
}
