package com.javaforge.project.model;

public enum BuildTool {
    MAVEN("Maven", "pom.xml"),
    GRADLE("Gradle", "build.gradle");

    private final String label;
    private final String buildFileName;

    BuildTool(String label, String buildFileName) {
        this.label = label;
        this.buildFileName = buildFileName;
    }

    public String getLabel() { return label; }
    public String getBuildFileName() { return buildFileName; }

    @Override
    public String toString() { return label; }
}
