package com.javaforge.project.model;

public enum ProjectType {
    SPRING_BOOT("Spring Boot"),
    JAKARTA_EE("Jakarta EE"),
    QUARKUS("Quarkus"),
    MICRONAUT("Micronaut"),
    JSF_PRIMEFACES("JSF + PrimeFaces"),
    SIMPLE_JAVA("Simple Java");

    private final String label;

    ProjectType(String label) { this.label = label; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
