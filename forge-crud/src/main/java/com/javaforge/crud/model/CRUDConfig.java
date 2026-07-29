package com.javaforge.crud.model;

import java.util.List;

public class CRUDConfig {

    private String tableName;
    private String entityName;
    private String packageName;
    private String outputDir;
    private List<FieldInfo> fields;
    private boolean generateEntity = true;
    private boolean generateRepository = true;
    private boolean generateService = true;
    private boolean generateController = true;
    private boolean generateDTO = true;
    private boolean generateMapper = true;
    private boolean generateValidator = true;
    private boolean generateException = true;
    private boolean generateJunit = true;
    private boolean generateSwagger = true;
    private boolean generateFlyway = true;

    public static class FieldInfo {
        private String name;
        private String type;
        private String columnName;
        private boolean primaryKey;
        private boolean nullable;

        public FieldInfo() {}

        public FieldInfo(String name, String type, String columnName, boolean primaryKey, boolean nullable) {
            this.name = name;
            this.type = type;
            this.columnName = columnName;
            this.primaryKey = primaryKey;
            this.nullable = nullable;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        public boolean isPrimaryKey() { return primaryKey; }
        public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }
        public boolean isNullable() { return nullable; }
        public void setNullable(boolean nullable) { this.nullable = nullable; }
    }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getOutputDir() { return outputDir; }
    public void setOutputDir(String outputDir) { this.outputDir = outputDir; }
    public List<FieldInfo> getFields() { return fields; }
    public void setFields(List<FieldInfo> fields) { this.fields = fields; }
    public boolean isGenerateEntity() { return generateEntity; }
    public void setGenerateEntity(boolean v) { generateEntity = v; }
    public boolean isGenerateRepository() { return generateRepository; }
    public void setGenerateRepository(boolean v) { generateRepository = v; }
    public boolean isGenerateService() { return generateService; }
    public void setGenerateService(boolean v) { generateService = v; }
    public boolean isGenerateController() { return generateController; }
    public void setGenerateController(boolean v) { generateController = v; }
    public boolean isGenerateDTO() { return generateDTO; }
    public void setGenerateDTO(boolean v) { generateDTO = v; }
    public boolean isGenerateMapper() { return generateMapper; }
    public void setGenerateMapper(boolean v) { generateMapper = v; }
    public boolean isGenerateValidator() { return generateValidator; }
    public void setGenerateValidator(boolean v) { generateValidator = v; }
    public boolean isGenerateException() { return generateException; }
    public void setGenerateException(boolean v) { generateException = v; }
    public boolean isGenerateJunit() { return generateJunit; }
    public void setGenerateJunit(boolean v) { generateJunit = v; }
    public boolean isGenerateSwagger() { return generateSwagger; }
    public void setGenerateSwagger(boolean v) { generateSwagger = v; }
    public boolean isGenerateFlyway() { return generateFlyway; }
    public void setGenerateFlyway(boolean v) { generateFlyway = v; }
}
