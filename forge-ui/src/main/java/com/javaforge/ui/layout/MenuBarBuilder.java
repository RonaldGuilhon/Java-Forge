package com.javaforge.ui.layout;

import com.javaforge.ui.theme.ThemeManager;
import javafx.scene.control.*;

public class MenuBarBuilder extends MenuBar {

    private final ThemeManager themeManager;

    public MenuBarBuilder(ThemeManager themeManager) {
        this.themeManager = themeManager;
        buildMenus();
    }

    private void buildMenus() {
        var fileMenu = new Menu("_File");
        fileMenu.getItems().addAll(
            new MenuItem("New Project..."),
            new MenuItem("New File..."),
            new SeparatorMenuItem(),
            new MenuItem("Open File..."),
            new MenuItem("Open Folder..."),
            new SeparatorMenuItem(),
            new MenuItem("Save"),
            new MenuItem("Save As..."),
            new MenuItem("Save All"),
            new SeparatorMenuItem(),
            new MenuItem("Close"),
            new MenuItem("Exit")
        );

        var editMenu = new Menu("_Edit");
        editMenu.getItems().addAll(
            new MenuItem("Undo"),
            new MenuItem("Redo"),
            new SeparatorMenuItem(),
            new MenuItem("Cut"),
            new MenuItem("Copy"),
            new MenuItem("Paste"),
            new SeparatorMenuItem(),
            new MenuItem("Find"),
            new MenuItem("Replace")
        );

        var viewMenu = new Menu("_View");
        var toggleTheme = new MenuItem("Toggle Theme");
        toggleTheme.setOnAction(e -> themeManager.toggle());
        viewMenu.getItems().addAll(
            toggleTheme,
            new SeparatorMenuItem(),
            new MenuItem("Explorer"),
            new MenuItem("Terminal"),
            new MenuItem("Git"),
            new MenuItem("Database"),
            new SeparatorMenuItem(),
            new MenuItem("Zoom In"),
            new MenuItem("Zoom Out")
        );

        var projectMenu = new Menu("_Project");
        projectMenu.getItems().addAll(
            new MenuItem("Generate CRUD"),
            new MenuItem("Generate Entity"),
            new MenuItem("Generate Service"),
            new MenuItem("Generate Controller"),
            new SeparatorMenuItem(),
            new MenuItem("Build"),
            new MenuItem("Clean"),
            new MenuItem("Run Tests")
        );

        var toolsMenu = new Menu("_Tools");
        toolsMenu.getItems().addAll(
            new MenuItem("Database Explorer"),
            new MenuItem("Docker"),
            new MenuItem("Maven"),
            new MenuItem("Gradle"),
            new MenuItem("AI Assistant"),
            new SeparatorMenuItem(),
            new MenuItem("Extensions"),
            new MenuItem("Settings")
        );

        var helpMenu = new Menu("_Help");
        helpMenu.getItems().addAll(
            new MenuItem("About Java Forge"),
            new MenuItem("Documentation"),
            new MenuItem("Report Issue")
        );

        getMenus().addAll(fileMenu, editMenu, viewMenu, projectMenu, toolsMenu, helpMenu);
    }
}
