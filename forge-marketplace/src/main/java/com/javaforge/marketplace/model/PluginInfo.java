package com.javaforge.marketplace.model;

import java.util.List;

public class PluginInfo {

    private String id;
    private String name;
    private String version;
    private String author;
    private String description;
    private String category;
    private String downloadUrl;
    private String iconUrl;
    private long downloads;
    private double rating;
    private List<String> tags;
    private boolean installed;

    public PluginInfo() {}

    public PluginInfo(String id, String name, String version, String author,
                      String description, String category, String downloadUrl,
                      String iconUrl, long downloads, double rating,
                      List<String> tags, boolean installed) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
        this.category = category;
        this.downloadUrl = downloadUrl;
        this.iconUrl = iconUrl;
        this.downloads = downloads;
        this.rating = rating;
        this.tags = tags;
        this.installed = installed;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public long getDownloads() { return downloads; }
    public void setDownloads(long downloads) { this.downloads = downloads; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public boolean isInstalled() { return installed; }
    public void setInstalled(boolean installed) { this.installed = installed; }

    public enum Category {
        THEME("Theme"),
        FRAMEWORK("Framework"),
        TEMPLATE("Template"),
        AI("AI"),
        GENERATOR("Generator"),
        DATABASE("Database"),
        TOOL("Tool"),
        LANGUAGE("Language Support");

        private final String label;
        Category(String label) { this.label = label; }
        public String getLabel() { return label; }
        public String toString() { return label; }
    }
}
