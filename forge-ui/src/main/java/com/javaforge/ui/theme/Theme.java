package com.javaforge.ui.theme;

import javafx.scene.Scene;
import java.util.List;

public record Theme(String name, boolean dark, List<String> stylesheets) {
    public void apply(Scene scene) {
        scene.getStylesheets().setAll(stylesheets);
    }
}
