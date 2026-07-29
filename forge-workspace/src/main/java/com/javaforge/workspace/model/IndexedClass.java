package com.javaforge.workspace.model;

import java.util.List;

public class IndexedClass {

    private long id;
    private long fileId;
    private String name;
    private String qualifiedName;
    private String packageName;
    private String type;
    private String superClass;
    private List<String> interfaces;
    private List<IndexedMethod> methods;
    private List<IndexedField> fields;
    private List<IndexedAnnotation> annotations;
    private List<String> imports;
    private int startLine;
    private int endLine;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getFileId() { return fileId; }
    public void setFileId(long fileId) { this.fileId = fileId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQualifiedName() { return qualifiedName; }
    public void setQualifiedName(String qualifiedName) { this.qualifiedName = qualifiedName; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSuperClass() { return superClass; }
    public void setSuperClass(String superClass) { this.superClass = superClass; }

    public List<String> getInterfaces() { return interfaces; }
    public void setInterfaces(List<String> interfaces) { this.interfaces = interfaces; }

    public List<IndexedMethod> getMethods() { return methods; }
    public void setMethods(List<IndexedMethod> methods) { this.methods = methods; }

    public List<IndexedField> getFields() { return fields; }
    public void setFields(List<IndexedField> fields) { this.fields = fields; }

    public List<IndexedAnnotation> getAnnotations() { return annotations; }
    public void setAnnotations(List<IndexedAnnotation> annotations) { this.annotations = annotations; }

    public List<String> getImports() { return imports; }
    public void setImports(List<String> imports) { this.imports = imports; }

    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }

    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }
}
