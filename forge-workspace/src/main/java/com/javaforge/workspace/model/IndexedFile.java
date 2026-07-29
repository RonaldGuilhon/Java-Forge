package com.javaforge.workspace.model;

import java.util.List;

public class IndexedFile {

    private long id;
    private String path;
    private String fileName;
    private String extension;
    private long lastModified;
    private long size;
    private String hash;
    private List<IndexedClass> classes;
    private List<String> todos;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public List<IndexedClass> getClasses() { return classes; }
    public void setClasses(List<IndexedClass> classes) { this.classes = classes; }

    public List<String> getTodos() { return todos; }
    public void setTodos(List<String> todos) { this.todos = todos; }
}
