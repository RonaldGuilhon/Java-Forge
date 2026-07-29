package com.javaforge.ui.theme;

import com.javaforge.core.settings.SettingsManager;
import javafx.scene.Scene;

import java.util.ArrayList;
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
        registerTheme(new Theme("Dark", true, List.of(
            getClass().getResource("/css/dark.css").toExternalForm()
        )));
        registerTheme(new Theme("Light", false, List.of(
            getClass().getResource("/css/light.css").toExternalForm()
        )));
        var saved = settings.get(SETTING_KEY, "Dark");
        current = themes.stream()
            .filter(t -> t.name().equals(saved))
            .findFirst()
            .orElse(themes.getFirst());
    }

    public void registerTheme(Theme theme) {
        themes.add(theme);
    }

    public void setTheme(String name) {
        themes.stream()
            .filter(t -> t.name().equals(name))
            .findFirst()
            .ifPresent(theme -> {
                current = theme;
                settings.set(SETTING_KEY, name);
                notifyListeners(theme.stylesheets());
            });
    }

    public void toggle() {
        var next = current.dark()
            ? themes.stream().filter(t -> !t.dark()).findFirst().orElse(current)
            : themes.stream().filter(Theme::dark).findFirst().orElse(current);
        setTheme(next.name());
    }

    public Theme getCurrentTheme() {
        return current;
    }

    public List<Theme> getThemes() {
        return List.copyOf(themes);
    }

    public void addListener(Consumer<List<String>> listener) {
        listeners.add(listener);
    }

    private void notifyListeners(List<String> stylesheets) {
        listeners.forEach(l -> l.accept(stylesheets));
    }
}
