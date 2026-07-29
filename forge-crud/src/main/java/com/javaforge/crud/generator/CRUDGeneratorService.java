package com.javaforge.crud.generator;

import com.javaforge.crud.model.CRUDConfig;
import com.javaforge.crud.model.CRUDConfig.FieldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CRUDGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(CRUDGeneratorService.class);
    private CRUDConfig config;
    private Path baseDir;

    public void generate(CRUDConfig config) throws IOException {
        this.config = config;
        String pkgPath = config.getPackageName().replace('.', '/');
        baseDir = Paths.get(config.getOutputDir(), "src/main/java", pkgPath);
        Path resourcesDir = Paths.get(config.getOutputDir(), "src/main/resources");

        if (config.isGenerateEntity()) generateEntity();
        if (config.isGenerateRepository()) generateRepository();
        if (config.isGenerateService()) generateService();
        if (config.isGenerateController()) generateController();
        if (config.isGenerateDTO()) generateDTO();
        if (config.isGenerateMapper()) generateMapper();
        if (config.isGenerateValidator()) generateValidator();
        if (config.isGenerateException()) generateException();
        if (config.isGenerateJunit()) generateTests();
        if (config.isGenerateFlyway()) generateMigration(resourcesDir);
        if (config.isGenerateSwagger()) generateSwaggerDoc();

        log.info("CRUD generated for {} at {}", config.getTableName(), baseDir);
    }

    private String cap(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String low(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    private String toCamel(String snake) {
        String[] parts = snake.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(cap(part.toLowerCase()));
        }
        return sb.toString();
    }

    private String entityFields() {
        StringBuilder sb = new StringBuilder();
        for (FieldInfo f : config.getFields()) {
            if (f.isPrimaryKey()) continue;
            sb.append("    private ").append(f.getType()).append(" ").append(f.getName()).append(";\n");
        }
        return sb.toString();
    }

    private String dtoFields() {
        StringBuilder sb = new StringBuilder();
        for (FieldInfo f : config.getFields()) {
            if (f.isPrimaryKey()) continue;
            sb.append("    private ").append(f.getType()).append(" ").append(f.getName()).append(";\n");
        }
        return sb.toString();
    }

    private void writeFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    private void generateEntity() throws IOException {
        Path dir = baseDir.resolve("entity");
        Files.createDirectories(dir);
        FieldInfo pk = null;
        for (FieldInfo f : config.getFields()) {
            if (f.isPrimaryKey()) { pk = f; break; }
        }
        String idField = "";
        if (pk != null) {
            idField = "    @Id\n    @GeneratedValue(strategy = GenerationType.IDENTITY)\n    private " + pk.getType() + " " + pk.getName() + ";\n";
        }

        String content = "package " + config.getPackageName() + ".entity;\n" +
            "\n" +
            "import jakarta.persistence.*;\n" +
            "import lombok.Data;\n" +
            "\n" +
            "@Entity\n" +
            "@Table(name = \"" + config.getTableName() + "\")\n" +
            "@Data\n" +
            "public class " + config.getEntityName() + " {\n" +
            idField +
            entityFields() +
            "}\n";
        writeFile(dir.resolve(config.getEntityName() + ".java"), content);
    }

    private void generateRepository() throws IOException {
        Path dir = baseDir.resolve("repository");
        Files.createDirectories(dir);
        String content = "package " + config.getPackageName() + ".repository;\n" +
            "\n" +
            "import " + config.getPackageName() + ".entity." + config.getEntityName() + ";\n" +
            "import org.springframework.data.jpa.repository.JpaRepository;\n" +
            "import org.springframework.stereotype.Repository;\n" +
            "\n" +
            "@Repository\n" +
            "public interface " + config.getEntityName() + "Repository extends JpaRepository<" + config.getEntityName() + ", Long> {\n" +
            "}\n";
        writeFile(dir.resolve(config.getEntityName() + "Repository.java"), content);
    }

    private void generateService() throws IOException {
        Path dir = baseDir.resolve("service");
        Files.createDirectories(dir);
        String entity = config.getEntityName();
        String varName = low(entity);
        String content = "package " + config.getPackageName() + ".service;\n" +
            "\n" +
            "import " + config.getPackageName() + ".entity." + entity + ";\n" +
            "import " + config.getPackageName() + ".repository." + entity + "Repository;\n" +
            "import lombok.RequiredArgsConstructor;\n" +
            "import org.springframework.stereotype.Service;\n" +
            "import org.springframework.transaction.annotation.Transactional;\n" +
            "import java.util.List;\n" +
            "\n" +
            "@Service\n" +
            "@RequiredArgsConstructor\n" +
            "@Transactional\n" +
            "public class " + entity + "Service {\n" +
            "\n" +
            "    private final " + entity + "Repository " + varName + "Repository;\n" +
            "\n" +
            "    public List<" + entity + "> findAll() {\n" +
            "        return " + varName + "Repository.findAll();\n" +
            "    }\n" +
            "\n" +
            "    public " + entity + " findById(Long id) {\n" +
            "        return " + varName + "Repository.findById(id)\n" +
            "            .orElseThrow(() -> new RuntimeException(\"" + entity + " not found\"));\n" +
            "    }\n" +
            "\n" +
            "    public " + entity + " save(" + entity + " entity) {\n" +
            "        return " + varName + "Repository.save(entity);\n" +
            "    }\n" +
            "\n" +
            "    public " + entity + " update(Long id, " + entity + " entity) {\n" +
            "        " + entity + " existing = findById(id);\n" +
            "        return " + varName + "Repository.save(existing);\n" +
            "    }\n" +
            "\n" +
            "    public void delete(Long id) {\n" +
            "        " + varName + "Repository.deleteById(id);\n" +
            "    }\n" +
            "}\n";
        writeFile(dir.resolve(entity + "Service.java"), content);
    }

    private void generateController() throws IOException {
        Path dir = baseDir.resolve("controller");
        Files.createDirectories(dir);
        String entity = config.getEntityName();
        String content = "package " + config.getPackageName() + ".controller;\n" +
            "\n" +
            "import " + config.getPackageName() + ".entity." + entity + ";\n" +
            "import " + config.getPackageName() + ".service." + entity + "Service;\n" +
            "import lombok.RequiredArgsConstructor;\n" +
            "import org.springframework.http.HttpStatus;\n" +
            "import org.springframework.web.bind.annotation.*;\n" +
            "import java.util.List;\n" +
            "\n" +
            "@RestController\n" +
            "@RequestMapping(\"/api/" + low(entity) + "\")\n" +
            "@RequiredArgsConstructor\n" +
            "public class " + entity + "Controller {\n" +
            "\n" +
            "    private final " + entity + "Service service;\n" +
            "\n" +
            "    @GetMapping\n" +
            "    public List<" + entity + "> findAll() {\n" +
            "        return service.findAll();\n" +
            "    }\n" +
            "\n" +
            "    @GetMapping(\"/{id}\")\n" +
            "    public " + entity + " findById(@PathVariable Long id) {\n" +
            "        return service.findById(id);\n" +
            "    }\n" +
            "\n" +
            "    @PostMapping\n" +
            "    @ResponseStatus(HttpStatus.CREATED)\n" +
            "    public " + entity + " create(@RequestBody " + entity + " entity) {\n" +
            "        return service.save(entity);\n" +
            "    }\n" +
            "\n" +
            "    @PutMapping(\"/{id}\")\n" +
            "    public " + entity + " update(@PathVariable Long id, @RequestBody " + entity + " entity) {\n" +
            "        return service.update(id, entity);\n" +
            "    }\n" +
            "\n" +
            "    @DeleteMapping(\"/{id}\")\n" +
            "    @ResponseStatus(HttpStatus.NO_CONTENT)\n" +
            "    public void delete(@PathVariable Long id) {\n" +
            "        service.delete(id);\n" +
            "    }\n" +
            "}\n";
        writeFile(dir.resolve(entity + "Controller.java"), content);
    }

    private void generateDTO() throws IOException {
        Path dir = baseDir.resolve("dto");
        Files.createDirectories(dir);
        String entity = config.getEntityName();
        String content = "package " + config.getPackageName() + ".dto;\n" +
            "\n" +
            "import lombok.Data;\n" +
            "\n" +
            "@Data\n" +
            "public class " + entity + "DTO {\n" +
            dtoFields() +
            "}\n";
        writeFile(dir.resolve(entity + "DTO.java"), content);
    }

    private void generateMapper() throws IOException {
        Path dir = baseDir.resolve("mapper");
        Files.createDirectories(dir);
        String entity = config.getEntityName();
        String content = "package " + config.getPackageName() + ".mapper;\n" +
            "\n" +
            "import " + config.getPackageName() + ".dto." + entity + "DTO;\n" +
            "import " + config.getPackageName() + ".entity." + entity + ";\n" +
            "import org.mapstruct.Mapper;\n" +
            "\n" +
            "@Mapper(componentModel = \"spring\")\n" +
            "public interface " + entity + "Mapper {\n" +
            "    " + entity + "DTO toDto(" + entity + " entity);\n" +
            "    " + entity + " toEntity(" + entity + "DTO dto);\n" +
            "}\n";
        writeFile(dir.resolve(entity + "Mapper.java"), content);
    }

    private void generateValidator() throws IOException {
        Path dir = baseDir.resolve("validator");
        Files.createDirectories(dir);
        String entity = config.getEntityName();
        String content = "package " + config.getPackageName() + ".validator;\n" +
            "\n" +
            "import " + config.getPackageName() + ".entity." + entity + ";\n" +
            "import org.springframework.stereotype.Component;\n" +
            "\n" +
            "@Component\n" +
            "public class " + entity + "Validator {\n" +
            "\n" +
            "    public void validate(" + entity + " entity) {\n" +
            "        if (entity == null) {\n" +
            "            throw new IllegalArgumentException(\"Entity cannot be null\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        writeFile(dir.resolve(entity + "Validator.java"), content);
    }

    private void generateException() throws IOException {
        Path dir = baseDir.resolve("exception");
        Files.createDirectories(dir);
        String content = "package " + config.getPackageName() + ".exception;\n" +
            "\n" +
            "import org.springframework.http.HttpStatus;\n" +
            "import org.springframework.web.bind.annotation.ResponseStatus;\n" +
            "\n" +
            "@ResponseStatus(HttpStatus.NOT_FOUND)\n" +
            "public class ResourceNotFoundException extends RuntimeException {\n" +
            "    public ResourceNotFoundException(String message) {\n" +
            "        super(message);\n" +
            "    }\n" +
            "}\n";
        writeFile(dir.resolve("ResourceNotFoundException.java"), content);
    }

    private void generateTests() throws IOException {
        Path testDir = Paths.get(config.getOutputDir(), "src/test/java", config.getPackageName().replace('.', '/'), "service");
        Files.createDirectories(testDir);
        String entity = config.getEntityName();
        String content = "package " + config.getPackageName() + ".service;\n" +
            "\n" +
            "import " + config.getPackageName() + ".entity." + entity + ";\n" +
            "import " + config.getPackageName() + ".repository." + entity + "Repository;\n" +
            "import org.junit.jupiter.api.Test;\n" +
            "import org.junit.jupiter.api.extension.ExtendWith;\n" +
            "import org.mockito.InjectMocks;\n" +
            "import org.mockito.Mock;\n" +
            "import org.mockito.junit.jupiter.MockitoExtension;\n" +
            "import java.util.List;\n" +
            "import static org.mockito.Mockito.*;\n" +
            "import static org.junit.jupiter.api.Assertions.*;\n" +
            "\n" +
            "@ExtendWith(MockitoExtension.class)\n" +
            "class " + entity + "ServiceTest {\n" +
            "\n" +
            "    @Mock\n" +
            "    private " + entity + "Repository repository;\n" +
            "\n" +
            "    @InjectMocks\n" +
            "    private " + entity + "Service service;\n" +
            "\n" +
            "    @Test\n" +
            "    void findAll_ReturnsList() {\n" +
            "        when(repository.findAll()).thenReturn(java.util.Collections.emptyList());\n" +
            "        java.util.List<" + entity + "> result = service.findAll();\n" +
            "        assertNotNull(result);\n" +
            "    }\n" +
            "}\n";
        writeFile(testDir.resolve(entity + "ServiceTest.java"), content);
    }

    private void generateMigration(Path resourcesDir) throws IOException {
        Path migrationDir = resourcesDir.resolve("db/migration");
        Files.createDirectories(migrationDir);
        StringBuilder cols = new StringBuilder();
        for (FieldInfo f : config.getFields()) {
            String lowerType = f.getType().toLowerCase();
            String type;
            if ("string".equals(lowerType)) type = "VARCHAR(255)";
            else if ("integer".equals(lowerType) || "int".equals(lowerType)) type = "INTEGER";
            else if ("long".equals(lowerType)) type = "BIGINT";
            else if ("boolean".equals(lowerType)) type = "BOOLEAN";
            else if ("double".equals(lowerType)) type = "DOUBLE PRECISION";
            else if ("bigdecimal".equals(lowerType)) type = "DECIMAL(19,2)";
            else if ("localdate".equals(lowerType)) type = "DATE";
            else if ("localdatetime".equals(lowerType)) type = "TIMESTAMP";
            else type = "VARCHAR(255)";
            cols.append("    ").append(f.getColumnName()).append(" ").append(type);
            if (!f.isNullable()) cols.append(" NOT NULL");
            cols.append(",\n");
        }
        String colsStr = cols.toString();
        if (colsStr.endsWith(",\n")) {
            colsStr = colsStr.substring(0, colsStr.length() - 2);
        }
        String content = "CREATE TABLE " + config.getTableName() + " (\n" +
            "    id BIGSERIAL PRIMARY KEY,\n" +
            colsStr + "\n" +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
            ");\n";
        writeFile(migrationDir.resolve("V1__create_" + config.getTableName() + ".sql"), content);
    }

    private void generateSwaggerDoc() throws IOException {
        Path dir = Paths.get(config.getOutputDir(), "src/main/resources");
        Files.createDirectories(dir);
        String entityName = config.getEntityName();
        String entityLow = low(entityName);
        String content = "{\n" +
            "  \"openapi\": \"3.0.0\",\n" +
            "  \"info\": {\n" +
            "    \"title\": \"" + entityName + " API\",\n" +
            "    \"version\": \"1.0\",\n" +
            "    \"description\": \"CRUD API for " + config.getTableName() + "\"\n" +
            "  },\n" +
            "  \"paths\": {\n" +
            "    \"/api/" + entityLow + "\": {\n" +
            "      \"get\": { \"summary\": \"List all " + entityName + "\" },\n" +
            "      \"post\": { \"summary\": \"Create " + entityName + "\" }\n" +
            "    },\n" +
            "    \"/api/" + entityLow + "/{id}\": {\n" +
            "      \"get\": { \"summary\": \"Get " + entityName + " by ID\" },\n" +
            "      \"put\": { \"summary\": \"Update " + entityName + "\" },\n" +
            "      \"delete\": { \"summary\": \"Delete " + entityName + "\" }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
        writeFile(dir.resolve("api-docs.json"), content);
    }
}
