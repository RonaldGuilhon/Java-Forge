package com.javaforge.documentation.ui;

import com.javaforge.documentation.generator.DocumentationGenerator;
import com.javaforge.documentation.model.DocConfig;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DocumentationWizard extends Dialog<DocConfig> {

    private final TextField projectDirField = new TextField(System.getProperty("user.dir"));
    private final TextField projectNameField = new TextField("My Project");
    private final TextField versionField = new TextField("1.0.0");
    private final TextField authorField = new TextField("Java Forge");
    private final CheckBox readmeCheck = new CheckBox("README.md");
    private final CheckBox swaggerCheck = new CheckBox("Swagger/OpenAPI");
    private final CheckBox javadocCheck = new CheckBox("JavaDoc");
    private final CheckBox umlCheck = new CheckBox("UML Diagrams (PlantUML)");
    private final CheckBox archCheck = new CheckBox("Architecture Document");
    private final CheckBox apiCheck = new CheckBox("API Reference");
    private final CheckBox dbCheck = new CheckBox("Database Documentation");

    public DocumentationWizard() {
        setTitle("Generate Documentation");
        setHeaderText("Configure documentation generation options");
        buildUI();
    }

    private void buildUI() {
        Button browseBtn = new Button("Browse");
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File dir = chooser.showDialog(getDialogPane().getScene().getWindow());
            if (dir != null) projectDirField.setText(dir.getAbsolutePath());
        });

        HBox dirRow = new HBox(8, projectDirField, browseBtn);
        HBox.setHgrow(projectDirField, Priority.ALWAYS);

        readmeCheck.setSelected(true);
        swaggerCheck.setSelected(true);
        javadocCheck.setSelected(true);
        umlCheck.setSelected(true);
        archCheck.setSelected(true);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(20));

        int r = 0;
        form.addRow(r++, new Label("Project Directory:"), dirRow);
        form.addRow(r++, new Label("Project Name:"), projectNameField);
        form.addRow(r++, new Label("Version:"), versionField);
        form.addRow(r++, new Label("Author:"), authorField);

        VBox options = new VBox(6,
            readmeCheck, swaggerCheck, javadocCheck, umlCheck,
            archCheck, apiCheck, dbCheck);
        options.setPadding(new Insets(10, 0, 0, 0));
        form.addRow(r, new Label("Generate:"), options);

        getDialogPane().setContent(new ScrollPane(form));
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        setResultConverter(btn -> btn == ButtonType.OK ? collectConfig() : null);
        setWidth(550);
        setHeight(500);
    }

    private DocConfig collectConfig() {
        DocConfig config = new DocConfig();
        config.setProjectDir(projectDirField.getText());
        config.setProjectName(projectNameField.getText());
        config.setProjectVersion(versionField.getText());
        config.setAuthor(authorField.getText());
        config.setGenerateReadme(readmeCheck.isSelected());
        config.setGenerateSwagger(swaggerCheck.isSelected());
        config.setGenerateJavaDoc(javadocCheck.isSelected());
        config.setGenerateUml(umlCheck.isSelected());
        config.setGenerateArchitecture(archCheck.isSelected());
        config.setGenerateApiDocs(apiCheck.isSelected());
        config.setGenerateDatabaseDoc(dbCheck.isSelected());
        return config;
    }

    public static void showAndGenerate() {
        DocumentationWizard wizard = new DocumentationWizard();
        DocConfig config = wizard.showAndWait().orElse(null);
        if (config != null) {
            try {
                new DocumentationGenerator().generate(config);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Documentation Generated");
                alert.setContentText("Documentation generated at: " +
                    Paths.get(config.getProjectDir(), config.getOutputDir()));
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
