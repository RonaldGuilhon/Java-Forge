package com.javaforge.workspace.model;

import java.util.Map;

public class IndexedAnnotation {

    private long id;
    private String name;
    private Map<String, String> values;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, String> getValues() { return values; }
    public void setValues(Map<String, String> values) { this.values = values; }
}
