package com.javaforge.workspace.model;

import java.util.List;

public class IndexedMethod {

    private long id;
    private long classId;
    private String name;
    private String returnType;
    private List<String> parameters;
    private List<String> parameterTypes;
    private String modifiers;
    private List<IndexedAnnotation> annotations;
    private int startLine;
    private int endLine;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClassId() { return classId; }
    public void setClassId(long classId) { this.classId = classId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }

    public List<String> getParameters() { return parameters; }
    public void setParameters(List<String> parameters) { this.parameters = parameters; }

    public List<String> getParameterTypes() { return parameterTypes; }
    public void setParameterTypes(List<String> parameterTypes) { this.parameterTypes = parameterTypes; }

    public String getModifiers() { return modifiers; }
    public void setModifiers(String modifiers) { this.modifiers = modifiers; }

    public List<IndexedAnnotation> getAnnotations() { return annotations; }
    public void setAnnotations(List<IndexedAnnotation> annotations) { this.annotations = annotations; }

    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }

    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }
}
