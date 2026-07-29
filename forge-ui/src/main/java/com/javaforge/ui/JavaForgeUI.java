package com.javaforge.ui;

import com.javaforge.core.JavaForge;
import com.javaforge.core.settings.SettingsManager;
import com.javaforge.ui.layout.MainWindow;
import com.javaforge.ui.theme.ThemeManager;
import com.javaforge.workspace.WorkspaceManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaForgeUI extends Application {

    private static final String APP_NAME = "Java Forge IDE";
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 900;

    @Override
    public void start(Stage stage) {
        JavaForge.bootstrap();

        WorkspaceManager.getInstance().initialize();

        SettingsManager forgeSettings = JavaForge.settings();
        ThemeManager themeManager = new ThemeManager(forgeSettings);

        MainWindow mainWindow = new MainWindow(themeManager);
        Scene scene = new Scene(mainWindow, WIDTH, HEIGHT);

        themeManager.getCurrentTheme().apply(scene);
        themeManager.addListener(sheets -> scene.getStylesheets().setAll(sheets));

        stage.setTitle(APP_NAME);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            JavaForge.getInstance().shutdown();
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
