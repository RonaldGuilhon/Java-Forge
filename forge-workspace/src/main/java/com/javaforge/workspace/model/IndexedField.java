package com.javaforge.workspace.model;

import java.util.List;

public class IndexedField {

    private long id;
    private long classId;
    private String name;
    private String type;
    private String modifiers;
    private List<IndexedAnnotation> annotations;
    private int line;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClassId() { return classId; }
    public void setClassId(long classId) { this.classId = classId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getModifiers() { return modifiers; }
    public void setModifiers(String modifiers) { this.modifiers = modifiers; }

    public List<IndexedAnnotation> getAnnotations() { return annotations; }
    public void setAnnotations(List<IndexedAnnotation> annotations) { this.annotations = annotations; }

    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }
}
