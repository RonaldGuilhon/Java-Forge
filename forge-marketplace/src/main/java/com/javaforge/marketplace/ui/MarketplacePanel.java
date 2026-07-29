package com.javaforge.marketplace.ui;

import com.javaforge.marketplace.model.PluginInfo;
import com.javaforge.marketplace.service.MarketplaceService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MarketplacePanel extends BorderPane {

    private final MarketplaceService service = new MarketplaceService();
    private final ListView<PluginInfo> pluginList = new ListView<>();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final Label detailLabel = new Label("Select a plugin to see details");
    private final Button installBtn = new Button("Install");
    private final Button uninstallBtn = new Button("Uninstall");
    private final Label statusLabel = new Label("");

    private List<PluginInfo> allPlugins = new ArrayList<>();

    public MarketplacePanel() {
        buildUI();
        loadPlugins();
    }

    private void buildUI() {
        Label header = new Label("EXTENSIONS: MARKETPLACE");
        header.getStyleClass().add("panel-header");

        categoryFilter.getItems().addAll("All", "Theme", "Framework", "Template", "AI",
            "Generator", "Database", "Tool", "Language Support");
        categoryFilter.setValue("All");
        categoryFilter.setOnAction(e -> filter());

        searchField.setPromptText("Search plugins...");
        searchField.textProperty().addListener((obs, o, n) -> filter());

        HBox filterBar = new HBox(8, new Label("Category:"), categoryFilter, searchField);
        filterBar.setPadding(new Insets(8));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        pluginList.setCellFactory(lv -> new ListCell<PluginInfo>() {
            @Override
            protected void updateItem(PluginInfo p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                } else {
                    String stars = "";
                    for (int i = 0; i < (int) Math.round(p.getRating()); i++) stars += "\u2605";
                    String status = p.isInstalled() ? " [INSTALLED]" : "";
                    setText(p.getName() + "  v" + p.getVersion() + status + "\n" + p.getDescription());
                    setStyle(p.isInstalled() ? "-fx-text-fill: #4ec9b0;" : "");
                }
            }
        });

        pluginList.getSelectionModel().selectedItemProperty().addListener((obs, old, p) -> {
            if (p != null) {
                detailLabel.setText(
                    "Name: " + p.getName() + "\n" +
                    "Version: " + p.getVersion() + "\n" +
                    "Author: " + p.getAuthor() + "\n" +
                    "Category: " + p.getCategory() + "\n" +
                    "Downloads: " + p.getDownloads() + "\n" +
                    "Rating: " + String.format("%.1f", p.getRating()) + "/5.0\n" +
                    "Tags: " + String.join(", ", p.getTags()));
                installBtn.setDisable(p.isInstalled());
                uninstallBtn.setDisable(!p.isInstalled());
            }
        });

        installBtn.setOnAction(e -> {
            PluginInfo plugin = pluginList.getSelectionModel().getSelectedItem();
            if (plugin != null && service.install(plugin)) {
                statusLabel.setText("Installed: " + plugin.getName());
                loadPlugins();
            }
        });

        uninstallBtn.setOnAction(e -> {
            PluginInfo plugin = pluginList.getSelectionModel().getSelectedItem();
            if (plugin != null && service.uninstall(plugin.getId())) {
                statusLabel.setText("Uninstalled: " + plugin.getName());
                loadPlugins();
            }
        });

        VBox detailPanel = new VBox(6, detailLabel, installBtn, uninstallBtn);
        detailPanel.setPadding(new Insets(8));
        detailPanel.setPrefWidth(250);

        VBox center = new VBox(filterBar, pluginList);
        VBox.setVgrow(pluginList, Priority.ALWAYS);

        SplitPane split = new SplitPane(center, detailPanel);
        VBox wrapper = new VBox(header, split, statusLabel);
        VBox.setVgrow(split, Priority.ALWAYS);
        setCenter(wrapper);
    }

    private void filter() {
        String cat = categoryFilter.getValue();
        String search = searchField.getText().toLowerCase();
        List<PluginInfo> filtered = allPlugins.stream()
            .filter(p -> cat.equals("All") || p.getCategory().equals(cat))
            .filter(p -> search.isEmpty() || p.getName().toLowerCase().contains(search)
                || p.getDescription().toLowerCase().contains(search))
            .collect(Collectors.toList());
        pluginList.getItems().setAll(filtered);
    }

    private void loadPlugins() {
        new Thread(() -> {
            List<PluginInfo> plugins = service.fetchAvailable();
            Platform.runLater(() -> {
                allPlugins = plugins;
                filter();
                statusLabel.setText(plugins.size() + " plugins available");
            });
        }).start();
    }
}
