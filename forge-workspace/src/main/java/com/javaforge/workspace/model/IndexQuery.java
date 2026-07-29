package com.javaforge.workspace.model;

import java.util.List;

public class IndexQuery {

    private String text;
    private String type;
    private List<String> modifiers;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getModifiers() { return modifiers; }
    public void setModifiers(List<String> modifiers) { this.modifiers = modifiers; }
}
