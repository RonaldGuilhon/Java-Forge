package com.javaforge.project.service;

import com.javaforge.project.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(ProjectGeneratorService.class);

    public Path generate(ProjectConfig config) throws IOException {
        Path baseDir = Paths.get(config.getOutputDir(), config.getName());
        Files.createDirectories(baseDir);

        Path srcMainJava = baseDir.resolve("src/main/java/" + config.getPackageName().replace('.', '/'));
        Path srcMainResources = baseDir.resolve("src/main/resources");
        Path srcTestJava = baseDir.resolve("src/test/java/" + config.getPackageName().replace('.', '/'));
        Files.createDirectories(srcMainJava);
        Files.createDirectories(srcMainResources);
        Files.createDirectories(srcTestJava);

        generatePom(baseDir, config);
        generateApplicationProperties(srcMainResources, config);
        generateMainClass(srcMainJava, config);
        generateGitignore(baseDir, config);
        generateReadme(baseDir, config);

        if (config.isDocker()) generateDockerfile(baseDir, config);
        if (config.isSwagger()) generateSwaggerConfig(srcMainJava, config);
        if (config.isJwt()) generateJwtConfig(srcMainJava, config);

        log.info("Project generated at: {}", baseDir);
        return baseDir;
    }

    private void writeFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    private void generatePom(Path baseDir, ProjectConfig config) throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
            "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>" + config.getGroupId() + "</groupId>\n" +
            "    <artifactId>" + config.getArtifactId() + "</artifactId>\n" +
            "    <version>1.0.0-SNAPSHOT</version>\n" +
            "    <name>" + config.getName() + "</name>\n" +
            "    <description>" + config.getDescription() + "</description>\n" +
            "    <properties>\n" +
            "        <maven.compiler.source>" + config.getJavaVersion().getVersion() + "</maven.compiler.source>\n" +
            "        <maven.compiler.target>" + config.getJavaVersion().getVersion() + "</maven.compiler.target>\n" +
            "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
            "    </properties>\n" +
            "</project>\n";
        writeFile(baseDir.resolve("pom.xml"), content);
    }

    private void generateApplicationProperties(Path resourcesDir, ProjectConfig config) throws IOException {
        StringBuilder props = new StringBuilder();
        props.append("# ").append(config.getName()).append("\n");
        props.append("spring.application.name=").append(config.getName()).append("\n");

        String db = config.getDatabase();
        if ("PostgreSQL".equals(db)) {
            props.append("spring.datasource.url=jdbc:postgresql://localhost:5432/").append(config.getName()).append("\n");
            props.append("spring.datasource.driver-class-name=org.postgresql.Driver\n");
        } else if ("MySQL".equals(db)) {
            props.append("spring.datasource.url=jdbc:mysql://localhost:3306/").append(config.getName()).append("\n");
            props.append("spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver\n");
        } else if ("Oracle".equals(db)) {
            props.append("spring.datasource.url=jdbc:oracle:thin:@localhost:1521:").append(config.getName()).append("\n");
            props.append("spring.datasource.driver-class-name=oracle.jdbc.OracleDriver\n");
        } else if ("SQL Server".equals(db)) {
            props.append("spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=").append(config.getName()).append("\n");
            props.append("spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver\n");
        } else {
            props.append("# H2 in-memory database\n");
            props.append("spring.datasource.url=jdbc:h2:mem:testdb\n");
            props.append("spring.datasource.driver-class-name=org.h2.Driver\n");
        }
        props.append("spring.datasource.username=sa\n");
        props.append("spring.datasource.password=\n");
        props.append("spring.jpa.hibernate.ddl-auto=update\n");
        props.append("spring.jpa.show-sql=true\n");
        props.append("server.port=8080\n");

        if (config.isSwagger()) {
            props.append("springdoc.api-docs.path=/api-docs\n");
            props.append("springdoc.swagger-ui.path=/swagger-ui.html\n");
        }
        if (config.isFlyway()) {
            props.append("spring.flyway.enabled=true\n");
            props.append("spring.flyway.locations=classpath:db/migration\n");
        }
        if (config.isLiquibase()) {
            props.append("spring.liquibase.enabled=true\n");
            props.append("spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml\n");
        }
        writeFile(resourcesDir.resolve("application.properties"), props.toString());
    }

    private void generateMainClass(Path srcMainJava, ProjectConfig config) throws IOException {
        String className = config.getName().substring(0, 1).toUpperCase() + config.getName().substring(1) + "Application";
        String content = "package " + config.getPackageName() + ";\n" +
            "\n" +
            "import org.springframework.boot.SpringApplication;\n" +
            "import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
            "\n" +
            "@SpringBootApplication\n" +
            "public class " + className + " {\n" +
            "    public static void main(String[] args) {\n" +
            "        SpringApplication.run(" + className + ".class, args);\n" +
            "    }\n" +
            "}\n";
        writeFile(srcMainJava.resolve(className + ".java"), content);
    }

    private void generateGitignore(Path baseDir, ProjectConfig config) throws IOException {
        String content = "target/\n" +
            "*.class\n" +
            "*.jar\n" +
            "*.war\n" +
            ".idea/\n" +
            "*.iml\n" +
            ".settings/\n" +
            ".project\n" +
            ".classpath\n" +
            ".vscode/\n" +
            ".DS_Store\n" +
            "node_modules/\n" +
            "build/\n" +
            ".gradle/\n";
        writeFile(baseDir.resolve(".gitignore"), content);
    }

    private void generateReadme(Path baseDir, ProjectConfig config) throws IOException {
        String content = "# " + config.getName() + "\n\n" +
            config.getDescription() + "\n\n" +
            "## Build\n\n```bash\nmvn clean install\n```\n\n## Run\n\n```bash\nmvn spring-boot:run\n```\n\n" +
            "## Technologies\n\n- Java " + config.getJavaVersion().getVersion() + "\n" +
            "- " + config.getBuildTool().getLabel() + "\n" +
            "- " + config.getProjectType().getLabel() + "\n";
        writeFile(baseDir.resolve("README.md"), content);
    }

    private void generateDockerfile(Path baseDir, ProjectConfig config) throws IOException {
        String content = "FROM eclipse-temurin:" + config.getJavaVersion().getVersion() + "-jdk-alpine\n" +
            "WORKDIR /app\n" +
            "COPY target/*.jar app.jar\n" +
            "EXPOSE 8080\n" +
            "ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]\n";
        writeFile(baseDir.resolve("Dockerfile"), content);
    }

    private void generateSwaggerConfig(Path srcMainJava, ProjectConfig config) throws IOException {
        Path pkg = srcMainJava.resolve("config");
        Files.createDirectories(pkg);
        String content = "package " + config.getPackageName() + ".config;\n\n" +
            "import io.swagger.v3.oas.models.OpenAPI;\n" +
            "import io.swagger.v3.oas.models.info.Info;\n" +
            "import org.springframework.context.annotation.Bean;\n" +
            "import org.springframework.context.annotation.Configuration;\n\n" +
            "@Configuration\n" +
            "public class SwaggerConfig {\n" +
            "    @Bean\n" +
            "    public OpenAPI customOpenAPI() {\n" +
            "        return new OpenAPI()\n" +
            "            .info(new Info()\n" +
            "                .title(\"" + config.getName() + " API\")\n" +
            "                .version(\"1.0\")\n" +
            "                .description(\"" + config.getDescription() + "\"));\n" +
            "    }\n" +
            "}\n";
        writeFile(pkg.resolve("SwaggerConfig.java"), content);
    }

    private void generateJwtConfig(Path srcMainJava, ProjectConfig config) throws IOException {
        Path pkg = srcMainJava.resolve("config");
        Files.createDirectories(pkg);
        String content = "package " + config.getPackageName() + ".config;\n\n" +
            "import org.springframework.context.annotation.Bean;\n" +
            "import org.springframework.context.annotation.Configuration;\n" +
            "import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;\n" +
            "import org.springframework.security.config.annotation.web.builders.HttpSecurity;\n" +
            "import org.springframework.security.web.SecurityFilterChain;\n\n" +
            "@Configuration\n" +
            "@EnableWebSecurity\n" +
            "public class SecurityConfig {\n" +
            "    @Bean\n" +
            "    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {\n" +
            "        http.csrf(csrf -> csrf.disable())\n" +
            "            .authorizeHttpRequests(auth -> auth\n" +
            "                .requestMatchers(\"/api/public/**\").permitAll()\n" +
            "                .anyRequest().authenticated());\n" +
            "        return http.build();\n" +
            "    }\n" +
            "}\n";
        writeFile(pkg.resolve("SecurityConfig.java"), content);
    }
}
