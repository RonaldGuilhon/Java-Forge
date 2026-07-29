package com.javaforge.ui.theme;

import javafx.scene.Scene;
import java.util.List;

public class Theme {

    private String name;
    private boolean dark;
    private List<String> stylesheets;

    public Theme(String name, boolean dark, List<String> stylesheets) {
        this.name = name;
        this.dark = dark;
        this.stylesheets = stylesheets;
    }

    public String getName() { return name; }
    public boolean isDark() { return dark; }
    public List<String> getStylesheets() { return stylesheets; }

    public void apply(Scene scene) {
        scene.getStylesheets().setAll(stylesheets);
    }
}
