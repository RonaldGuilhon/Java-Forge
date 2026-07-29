package com.javaforge.database.explorer;

import com.javaforge.database.connector.DatabaseConnector;
import com.javaforge.database.connector.DatabaseConnector.ColumnInfo;
import com.javaforge.database.connector.DatabaseConnector.DatabaseConfig;
import com.javaforge.database.connector.DatabaseConnector.DatabaseType;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;

public class DatabaseExplorerPanel extends BorderPane {

    private final TreeView<String> treeView = new TreeView<>();
    private final DatabaseConnector connector = new DatabaseConnector();
    private final TextArea sqlArea = new TextArea();
    private final TableView<Map<String, Object>> resultTable = new TableView<>();

    public DatabaseExplorerPanel() {
        buildUI();
    }

    private void buildUI() {
        Label header = new Label("DATABASE EXPLORER");
        header.getStyleClass().add("panel-header");

        Button connectBtn = new Button("Connect");
        connectBtn.setOnAction(e -> showConnectDialog());

        TreeItem<String> schemaTree = new TreeItem<>("No connection");
        schemaTree.setExpanded(true);
        treeView.setRoot(schemaTree);
        treeView.setShowRoot(true);

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null && val.getParent() != null && val.getParent().getParent() == treeView.getRoot()) {
                loadTableData(val.getValue());
            }
        });

        VBox left = new VBox(header, connectBtn, treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        left.setPrefWidth(250);

        Button queryBtn = new Button("Run Query");
        queryBtn.setOnAction(e -> executeQuery());
        ToolBar sqlToolbar = new ToolBar(queryBtn);

        BorderPane center = new BorderPane();
        center.setTop(sqlToolbar);
        center.setCenter(sqlArea);
        center.setBottom(resultTable);

        SplitPane split = new SplitPane(left, center);
        setCenter(split);
    }

    private void showConnectDialog() {
        Dialog<DatabaseConfig> dialog = new Dialog<>();
        dialog.setTitle("Database Connection");
        dialog.setHeaderText("Connect to database");

        ComboBox<DatabaseType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(DatabaseType.values());
        typeBox.setValue(DatabaseType.POSTGRESQL);

        TextField hostField = new TextField("localhost");
        TextField portField = new TextField("5432");
        TextField dbField = new TextField("postgres");
        TextField userField = new TextField("postgres");
        PasswordField passField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Type:"), typeBox);
        grid.addRow(1, new Label("Host:"), hostField);
        grid.addRow(2, new Label("Port:"), portField);
        grid.addRow(3, new Label("Database:"), dbField);
        grid.addRow(4, new Label("Username:"), userField);
        grid.addRow(5, new Label("Password:"), passField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new DatabaseConfig(typeBox.getValue(), hostField.getText(),
                    Integer.parseInt(portField.getText()), dbField.getText(),
                    userField.getText(), passField.getText());
            }
            return null;
        });

        DatabaseConfig config = dialog.showAndWait().orElse(null);
        if (config != null) {
            try {
                connector.connect(config);
                loadTree();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void loadTree() {
        try {
            TreeItem<String> root = new TreeItem<>("Database");
            root.setExpanded(true);
            java.util.List<String> tables = connector.listTables();
            for (String table : tables) {
                TreeItem<String> tableItem = new TreeItem<>(table);
                java.util.List<ColumnInfo> columns = connector.listColumns(table);
                for (ColumnInfo col : columns) {
                    String colLabel = String.format("%s : %s(%d)%s",
                        col.getName(), col.getType(), col.getSize(),
                        col.isNullable() ? "" : " NOT NULL");
                    tableItem.getChildren().add(new TreeItem<>(colLabel));
                }
                root.getChildren().add(tableItem);
            }
            treeView.setRoot(root);
        } catch (Exception e) {
            treeView.setRoot(new TreeItem<>("Error: " + e.getMessage()));
        }
    }

    private void loadTableData(String tableName) {
        try {
            java.util.List<Map<String, Object>> data = connector.query("SELECT * FROM " + tableName + " LIMIT 100");
            resultTable.getColumns().clear();
            resultTable.getItems().clear();

            if (!data.isEmpty()) {
                for (String key : data.get(0).keySet()) {
                    TableColumn<Map<String, Object>, String> col = new TableColumn<>(key);
                    col.setCellValueFactory(cell ->
                        new SimpleStringProperty(
                            String.valueOf(cell.getValue().get(key))
                        )
                    );
                    resultTable.getColumns().add(col);
                }
                resultTable.getItems().addAll(data);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.showAndWait();
        }
    }

    private void executeQuery() {
        String sql = sqlArea.getText();
        if (sql.trim().isEmpty()) return;
        try {
            String upper = sql.trim().toUpperCase();
            if (upper.startsWith("SELECT") || upper.startsWith("WITH") || upper.startsWith("EXPLAIN")) {
                java.util.List<Map<String, Object>> data = connector.query(sql);
                resultTable.getColumns().clear();
                resultTable.getItems().clear();
                if (!data.isEmpty()) {
                    for (String key : data.get(0).keySet()) {
                        TableColumn<Map<String, Object>, String> col = new TableColumn<>(key);
                        col.setCellValueFactory(cell ->
                            new SimpleStringProperty(
                                String.valueOf(cell.getValue().get(key))
                            )
                        );
                        resultTable.getColumns().add(col);
                    }
                    resultTable.getItems().addAll(data);
                }
            } else {
                int affected = connector.execute(sql);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, affected + " rows affected");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.showAndWait();
        }
    }
}
