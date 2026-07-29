package com.javaforge.crud.wizard;

import com.javaforge.crud.generator.CRUDGeneratorService;
import com.javaforge.crud.model.CRUDConfig;
import com.javaforge.crud.model.CRUDConfig.FieldInfo;
import com.javaforge.database.connector.DatabaseConnector;
import com.javaforge.database.connector.DatabaseConnector.ColumnInfo;
import com.javaforge.database.connector.DatabaseConnector.DatabaseConfig;
import com.javaforge.database.connector.DatabaseConnector.DatabaseType;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CRUDWizard extends Dialog<CRUDConfig> {

    private final ComboBox<DatabaseType> typeBox = new ComboBox<>();
    private final TextField hostField = new TextField("localhost");
    private final TextField portField = new TextField("5432");
    private final TextField dbField = new TextField("postgres");
    private final TextField userField = new TextField("postgres");
    private final PasswordField passField = new PasswordField();
    private final ComboBox<String> tableBox = new ComboBox<>();
    private final TextField packageField = new TextField("com.example");
    private final TextField outputField = new TextField(System.getProperty("user.dir"));
    private final CheckBox entityCheck = new CheckBox("Entity");
    private final CheckBox repositoryCheck = new CheckBox("Repository");
    private final CheckBox serviceCheck = new CheckBox("Service");
    private final CheckBox controllerCheck = new CheckBox("Controller");
    private final CheckBox dtoCheck = new CheckBox("DTO");
    private final CheckBox mapperCheck = new CheckBox("Mapper");
    private final CheckBox validatorCheck = new CheckBox("Validator");
    private final CheckBox exceptionCheck = new CheckBox("Exception");
    private final CheckBox junitCheck = new CheckBox("JUnit Tests");
    private final CheckBox swaggerCheck = new CheckBox("Swagger");
    private final CheckBox flywayCheck = new CheckBox("Flyway Migration");

    private final DatabaseConnector connector = new DatabaseConnector();
    private List<FieldInfo> fields = new ArrayList<>();

    public CRUDWizard() {
        setTitle("Generate CRUD");
        setHeaderText("Connect to database, select table, and generate CRUD");
        buildUI();
    }

    private void buildUI() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(20));

        typeBox.getItems().addAll(DatabaseType.values());
        typeBox.setValue(DatabaseType.POSTGRESQL);

        Button connectBtn = new Button("Connect & Load Tables");
        connectBtn.setOnAction(e -> loadTables());

        Button browseBtn = new Button("Browse");
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            java.io.File dir = chooser.showDialog(getDialogPane().getScene().getWindow());
            if (dir != null) outputField.setText(dir.getAbsolutePath());
        });

        entityCheck.setSelected(true);
        repositoryCheck.setSelected(true);
        serviceCheck.setSelected(true);
        controllerCheck.setSelected(true);
        dtoCheck.setSelected(true);
        mapperCheck.setSelected(true);
        validatorCheck.setSelected(true);
        exceptionCheck.setSelected(true);
        junitCheck.setSelected(true);
        swaggerCheck.setSelected(true);
        flywayCheck.setSelected(true);

        int row = 0;
        grid.addRow(row++, new Label("Database Type:"), typeBox);
        grid.addRow(row++, new Label("Host:"), hostField);
        grid.addRow(row++, new Label("Port:"), portField);
        grid.addRow(row++, new Label("Database:"), dbField);
        grid.addRow(row++, new Label("Username:"), userField);
        grid.addRow(row++, new Label("Password:"), passField);
        grid.addRow(row++, connectBtn);
        grid.addRow(row++, new Label("Table:"), tableBox);
        grid.addRow(row++, new Label("Package:"), packageField);
        grid.addRow(row++, new Label("Output:"), outputField);
        grid.addRow(row++, browseBtn);

        TitledPane options = new TitledPane("Generate", new VBox(4,
            entityCheck, repositoryCheck, serviceCheck, controllerCheck, dtoCheck,
            mapperCheck, validatorCheck, exceptionCheck, junitCheck, swaggerCheck, flywayCheck));
        options.setExpanded(false);
        grid.addRow(row, options);
        GridPane.setColumnSpan(options, 2);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        getDialogPane().setContent(scroll);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        setResultConverter(btn -> btn == ButtonType.OK ? collectConfig() : null);

        setWidth(550);
        setHeight(600);
    }

    private void loadTables() {
        try {
            DatabaseConfig config = new DatabaseConfig(typeBox.getValue(), hostField.getText(),
                Integer.parseInt(portField.getText()), dbField.getText(),
                userField.getText(), passField.getText());
            connector.connect(config);
            tableBox.getItems().setAll(connector.listTables());
            if (!tableBox.getItems().isEmpty()) tableBox.setValue(tableBox.getItems().get(0));
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            alert.showAndWait();
        }
    }

    private CRUDConfig collectConfig() {
        CRUDConfig cfg = new CRUDConfig();
        cfg.setTableName(tableBox.getValue());
        cfg.setEntityName(toCamel(tableBox.getValue()));
        cfg.setPackageName(packageField.getText());
        cfg.setOutputDir(outputField.getText());

        try {
            fields = connector.listColumns(tableBox.getValue()).stream()
                .map(c -> new FieldInfo(
                    toCamel(c.getName()),
                    mapType(c.getType()),
                    c.getName(),
                    c.isAutoIncrement(),
                    c.isNullable()
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            fields = new ArrayList<>();
        }
        cfg.setFields(fields);

        cfg.setGenerateEntity(entityCheck.isSelected());
        cfg.setGenerateRepository(repositoryCheck.isSelected());
        cfg.setGenerateService(serviceCheck.isSelected());
        cfg.setGenerateController(controllerCheck.isSelected());
        cfg.setGenerateDTO(dtoCheck.isSelected());
        cfg.setGenerateMapper(mapperCheck.isSelected());
        cfg.setGenerateValidator(validatorCheck.isSelected());
        cfg.setGenerateException(exceptionCheck.isSelected());
        cfg.setGenerateJunit(junitCheck.isSelected());
        cfg.setGenerateSwagger(swaggerCheck.isSelected());
        cfg.setGenerateFlyway(flywayCheck.isSelected());

        return cfg;
    }

    private String toCamel(String snake) {
        String[] parts = snake.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private String mapType(String sqlType) {
        String upper = sqlType.toUpperCase();
        if (upper.contains("INT")) return "Integer";
        if (upper.contains("BIGINT") || upper.contains("SERIAL")) return "Long";
        if (upper.contains("VARCHAR") || upper.contains("CHAR") || upper.contains("TEXT")) return "String";
        if (upper.contains("DECIMAL") || upper.contains("NUMERIC")) return "BigDecimal";
        if (upper.contains("FLOAT") || upper.contains("DOUBLE")) return "Double";
        if (upper.contains("BOOLEAN")) return "Boolean";
        if (upper.contains("DATE") || upper.contains("TIMESTAMP")) return "LocalDateTime";
        if (upper.contains("BLOB") || upper.contains("BINARY")) return "byte[]";
        return "String";
    }

    public static void showAndGenerate() {
        CRUDWizard wizard = new CRUDWizard();
        CRUDConfig config = wizard.showAndWait().orElse(null);
        if (config != null) {
            try {
                new CRUDGeneratorService().generate(config);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("CRUD Generated");
                alert.setContentText("CRUD generated successfully for table " + config.getTableName());
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
