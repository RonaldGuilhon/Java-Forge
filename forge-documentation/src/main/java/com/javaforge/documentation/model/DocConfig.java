package com.javaforge.documentation.model;

import java.nio.file.Path;

public class DocConfig {

    private String projectDir = System.getProperty("user.dir");
    private String projectName = "My Project";
    private String projectVersion = "1.0.0";
    private String author = "Java Forge";
    private String outputDir = "docs";
    private boolean generateJavaDoc;
    private boolean generateSwagger;
    private boolean generateReadme;
    private boolean generateUml;
    private boolean generateArchitecture;
    private boolean generateApiDocs;
    private boolean generateDatabaseDoc;

    public String getProjectDir() { return projectDir; }
    public void setProjectDir(String v) { projectDir = v; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String v) { projectName = v; }
    public String getProjectVersion() { return projectVersion; }
    public void setProjectVersion(String v) { projectVersion = v; }
    public String getAuthor() { return author; }
    public void setAuthor(String v) { author = v; }
    public String getOutputDir() { return outputDir; }
    public void setOutputDir(String v) { outputDir = v; }
    public boolean isGenerateJavaDoc() { return generateJavaDoc; }
    public void setGenerateJavaDoc(boolean v) { generateJavaDoc = v; }
    public boolean isGenerateSwagger() { return generateSwagger; }
    public void setGenerateSwagger(boolean v) { generateSwagger = v; }
    public boolean isGenerateReadme() { return generateReadme; }
    public void setGenerateReadme(boolean v) { generateReadme = v; }
    public boolean isGenerateUml() { return generateUml; }
    public void setGenerateUml(boolean v) { generateUml = v; }
    public boolean isGenerateArchitecture() { return generateArchitecture; }
    public void setGenerateArchitecture(boolean v) { generateArchitecture = v; }
    public boolean isGenerateApiDocs() { return generateApiDocs; }
    public void setGenerateApiDocs(boolean v) { generateApiDocs = v; }
    public boolean isGenerateDatabaseDoc() { return generateDatabaseDoc; }
    public void setGenerateDatabaseDoc(boolean v) { generateDatabaseDoc = v; }
}
