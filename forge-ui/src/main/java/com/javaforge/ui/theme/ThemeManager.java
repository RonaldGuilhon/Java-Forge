package com.javaforge.ui.theme;

import com.javaforge.core.settings.SettingsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class ThemeManager {

    private static final String SETTING_KEY = "ui.theme";

    private final List<Theme> themes = new ArrayList<>();
    private Theme current;
    private final SettingsManager settings;
    private final List<Consumer<List<String>>> listeners = new ArrayList<>();

    public ThemeManager(SettingsManager settings) {
        this.settings = settings;
        registerTheme(new Theme("Dark", true, Arrays.asList(
            getClass().getResource("/css/dark.css").toExternalForm()
        )));
        registerTheme(new Theme("Light", false, Arrays.asList(
            getClass().getResource("/css/light.css").toExternalForm()
        )));
        String saved = settings.get(SETTING_KEY, "Dark");
        current = themes.stream()
            .filter(t -> t.getName().equals(saved))
            .findFirst()
            .orElse(themes.get(0));
    }

    public void registerTheme(Theme theme) {
        themes.add(theme);
    }

    public void setTheme(String name) {
        themes.stream()
            .filter(t -> t.getName().equals(name))
            .findFirst()
            .ifPresent(theme -> {
                current = theme;
                settings.set(SETTING_KEY, name);
                notifyListeners(theme.getStylesheets());
            });
    }

    public void toggle() {
        boolean isDark = current.isDark();
        Theme next = isDark
            ? themes.stream().filter(t -> !t.isDark()).findFirst().orElse(current)
            : themes.stream().filter(Theme::isDark).findFirst().orElse(current);
        setTheme(next.getName());
    }

    public Theme getCurrentTheme() {
        return current;
    }

    public List<Theme> getThemes() {
        return Collections.unmodifiableList(themes);
    }

    public void addListener(Consumer<List<String>> listener) {
        listeners.add(listener);
    }

    private void notifyListeners(List<String> stylesheets) {
        for (Consumer<List<String>> l : listeners) {
            l.accept(stylesheets);
        }
    }
}
