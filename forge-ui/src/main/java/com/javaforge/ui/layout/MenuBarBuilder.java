package com.javaforge.ui.layout;

import com.javaforge.crud.wizard.CRUDWizard;
import com.javaforge.documentation.ui.DocumentationWizard;
import com.javaforge.project.wizard.ProjectWizard;
import com.javaforge.ui.theme.ThemeManager;
import javafx.scene.control.*;

public class MenuBarBuilder extends MenuBar {

    private final ThemeManager themeManager;
    private final MainWindow mainWindow;

    public MenuBarBuilder(ThemeManager themeManager) {
        this(themeManager, null);
    }

    public MenuBarBuilder(ThemeManager themeManager, MainWindow mainWindow) {
        this.themeManager = themeManager;
        this.mainWindow = mainWindow;
        buildMenus();
    }

    private void buildMenus() {
        MenuItem newProjectItem = new MenuItem("New Project...");
        newProjectItem.setOnAction(e -> ProjectWizard.showAndGenerate(path -> {
            if (mainWindow != null) {
                mainWindow.loadProject(path);
            }
        }));

        MenuItem openFileItem = new MenuItem("Open File...");
        openFileItem.setOnAction(e -> { if (mainWindow != null) mainWindow.handleOpenFile(); });

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> { if (mainWindow != null) mainWindow.handleSave(); });
        saveItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+S"));

        MenuItem saveAsItem = new MenuItem("Save As...");
        saveAsItem.setOnAction(e -> { if (mainWindow != null) mainWindow.handleSaveAs(); });

        MenuItem saveAllItem = new MenuItem("Save All");
        saveAllItem.setOnAction(e -> { if (mainWindow != null) mainWindow.handleSaveAll(); });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> javafx.application.Platform.exit());

        Menu fileMenu = new Menu("_File");
        fileMenu.getItems().addAll(
            newProjectItem,
            new MenuItem("New File..."),
            new SeparatorMenuItem(),
            openFileItem,
            new MenuItem("Open Folder..."),
            new SeparatorMenuItem(),
            saveItem,
            saveAsItem,
            saveAllItem,
            new SeparatorMenuItem(),
            new MenuItem("Close"),
            exitItem
        );

        Menu editMenu = new Menu("_Edit");
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

        Menu viewMenu = new Menu("_View");
        MenuItem toggleTheme = new MenuItem("Toggle Theme");
        toggleTheme.setOnAction(e -> themeManager.toggle());
        MenuItem showAiPanel = new MenuItem("AI Assistant");
        showAiPanel.setOnAction(e -> { if (mainWindow != null) mainWindow.toggleAiPanel(); });
        viewMenu.getItems().addAll(
            toggleTheme,
            new SeparatorMenuItem(),
            new MenuItem("Explorer"),
            new MenuItem("Terminal"),
            new MenuItem("Git"),
            new MenuItem("Database"),
            showAiPanel,
            new SeparatorMenuItem(),
            new MenuItem("Zoom In"),
            new MenuItem("Zoom Out")
        );

        MenuItem generateCrudItem = new MenuItem("Generate CRUD");
        generateCrudItem.setOnAction(e -> CRUDWizard.showAndGenerate());

        MenuItem buildItem = new MenuItem("Build");
        buildItem.setOnAction(e -> { if (mainWindow != null) mainWindow.handleBuild("compile"); });
        buildItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+B"));

        MenuItem cleanItem = new MenuItem("Clean");
        cleanItem.setOnAction(e -> { if (mainWindow != null) mainWindow.handleBuild("clean"); });

        MenuItem runTestsItem = new MenuItem("Run Tests");
        runTestsItem.setOnAction(e -> { if (mainWindow != null) mainWindow.handleBuild("test"); });

        Menu projectMenu = new Menu("_Project");
        projectMenu.getItems().addAll(
            generateCrudItem,
            new MenuItem("Generate Entity"),
            new MenuItem("Generate Service"),
            new MenuItem("Generate Controller"),
            new SeparatorMenuItem(),
            buildItem,
            cleanItem,
            runTestsItem
        );

        MenuItem openDbItem = new MenuItem("Database Explorer");
        openDbItem.setOnAction(e -> { if (mainWindow != null) mainWindow.openDatabaseExplorer(); });
        MenuItem openAiItem = new MenuItem("AI Assistant");
        openAiItem.setOnAction(e -> { if (mainWindow != null) mainWindow.toggleAiPanel(); });

        MenuItem serverItem = new MenuItem("Server Manager");
        serverItem.setOnAction(e -> { if (mainWindow != null) mainWindow.openServerPanel(); });

        MenuItem marketplaceItem = new MenuItem("Extensions Marketplace");
        marketplaceItem.setOnAction(e -> { if (mainWindow != null) mainWindow.openMarketplace(); });

        MenuItem docItem = new MenuItem("Documentation Generator");
        docItem.setOnAction(e -> DocumentationWizard.showAndGenerate());

        Menu toolsMenu = new Menu("_Tools");
        toolsMenu.getItems().addAll(
            openDbItem,
            serverItem,
            marketplaceItem,
            docItem,
            new MenuItem("Docker"),
            new MenuItem("Maven"),
            new MenuItem("Gradle"),
            openAiItem,
            new SeparatorMenuItem(),
            new MenuItem("Settings")
        );

        Menu helpMenu = new Menu("_Help");
        helpMenu.getItems().addAll(
            new MenuItem("About Java Forge"),
            new MenuItem("Documentation"),
            new MenuItem("Report Issue")
        );

        getMenus().addAll(fileMenu, editMenu, viewMenu, projectMenu, toolsMenu, helpMenu);
    }
}
