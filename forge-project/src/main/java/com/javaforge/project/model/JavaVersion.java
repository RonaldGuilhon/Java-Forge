package com.javaforge.project.model;

public enum JavaVersion {
    JAVA_8("1.8", "Java 8"),
    JAVA_11("11", "Java 11"),
    JAVA_17("17", "Java 17"),
    JAVA_21("21", "Java 21");

    private final String version;
    private final String label;

    JavaVersion(String version, String label) {
        this.version = version;
        this.label = label;
    }

    public String getVersion() { return version; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
