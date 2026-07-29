package com.javaforge.workspace.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.javaforge.workspace.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

public class JavaParserService {

    private static final Logger log = LoggerFactory.getLogger(JavaParserService.class);

    private final JavaParser parser = new JavaParser();

    public IndexedFile parseFile(Path filePath) {
        IndexedFile indexedFile = new IndexedFile();
        try {
            String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            String hash = md5(content);

            indexedFile.setPath(filePath.toAbsolutePath().normalize().toString());
            indexedFile.setFileName(filePath.getFileName().toString());
            indexedFile.setExtension(getExtension(filePath));
            indexedFile.setLastModified(filePath.toFile().lastModified());
            indexedFile.setSize(filePath.toFile().length());
            indexedFile.setHash(hash);

            if ("java".equals(indexedFile.getExtension())) {
                parseJavaContent(content, indexedFile);
            } else {
                indexedFile.setClasses(new ArrayList<>());
                indexedFile.setTodos(extractTodos(content));
            }
        } catch (Exception e) {
            log.warn("Failed to parse file: {}", filePath, e);
            indexedFile.setClasses(new ArrayList<>());
            indexedFile.setTodos(new ArrayList<>());
        }
        return indexedFile;
    }

    private void parseJavaContent(String content, IndexedFile indexedFile) {
        ParseResult<CompilationUnit> result = parser.parse(content);
        if (!result.isSuccessful() || !result.getResult().isPresent()) {
            indexedFile.setClasses(new ArrayList<>());
            indexedFile.setTodos(extractTodos(content));
            return;
        }

        CompilationUnit cu = result.getResult().get();
        List<IndexedClass> classes = new ArrayList<>();
        List<String> todos = new ArrayList<>();

        String packageName = cu.getPackageDeclaration()
                .map(p -> p.getNameAsString())
                .orElse("");

        List<String> imports = cu.getImports().stream()
                .map(i -> i.getName().toString())
                .collect(Collectors.toList());

        for (TypeDeclaration<?> typeDecl : cu.getTypes()) {
            IndexedClass indexedClass = parseTypeDeclaration(typeDecl, packageName, indexedFile);
            indexedClass.setImports(imports);
            classes.add(indexedClass);
        }

        todos.addAll(extractComments(cu));

        indexedFile.setClasses(classes);
        indexedFile.setTodos(todos);
    }

    private IndexedClass parseTypeDeclaration(TypeDeclaration<?> typeDecl, String packageName, IndexedFile indexedFile) {
        IndexedClass cls = new IndexedClass();
        cls.setFileId(indexedFile.getId());
        cls.setName(typeDecl.getNameAsString());
        cls.setPackageName(packageName);
        cls.setQualifiedName(packageName.isEmpty() ? typeDecl.getNameAsString() : packageName + "." + typeDecl.getNameAsString());

        if (typeDecl.isClassOrInterfaceDeclaration()) {
            ClassOrInterfaceDeclaration coi = typeDecl.asClassOrInterfaceDeclaration();
            cls.setType(coi.isInterface() ? "Interface" : "Class");
            coi.getExtendedTypes().stream().findFirst()
                    .ifPresent(t -> cls.setSuperClass(t.getNameAsString()));
            List<String> interfaces = new ArrayList<>();
            for (int i = 0; i < coi.getImplementedTypes().size(); i++) {
                interfaces.add(coi.getImplementedTypes().get(i).getNameAsString());
            }
            cls.setInterfaces(interfaces);
        } else if (typeDecl.isEnumDeclaration()) {
            cls.setType("Enum");
        } else if (typeDecl.isAnnotationDeclaration()) {
            cls.setType("Annotation");
        }

        cls.setStartLine(typeDecl.getBegin().map(n -> n.line).orElse(0));
        cls.setEndLine(typeDecl.getEnd().map(n -> n.line).orElse(0));

        cls.setMethods(parseMethods(typeDecl));
        cls.setFields(parseFields(typeDecl));
        cls.setAnnotations(parseAnnotations(typeDecl.getAnnotations()));

        return cls;
    }

    private List<IndexedMethod> parseMethods(TypeDeclaration<?> typeDecl) {
        List<IndexedMethod> methods = new ArrayList<>();
        for (MethodDeclaration method : typeDecl.getMethods()) {
            IndexedMethod m = new IndexedMethod();
            m.setName(method.getNameAsString());
            m.setReturnType(method.getType().asString());
            List<String> params = new ArrayList<>();
            List<String> paramTypes = new ArrayList<>();
            for (int i = 0; i < method.getParameters().size(); i++) {
                params.add(method.getParameters().get(i).getNameAsString());
                paramTypes.add(method.getParameters().get(i).getType().asString());
            }
            m.setParameters(params);
            m.setParameterTypes(paramTypes);
            StringBuilder modSb = new StringBuilder();
            for (int i = 0; i < method.getModifiers().size(); i++) {
                if (modSb.length() > 0) modSb.append(" ");
                modSb.append(method.getModifiers().get(i).getKeyword().asString());
            }
            m.setModifiers(modSb.toString());
            m.setStartLine(method.getBegin().map(n -> n.line).orElse(0));
            m.setEndLine(method.getEnd().map(n -> n.line).orElse(0));
            m.setAnnotations(parseAnnotations(method.getAnnotations()));
            methods.add(m);
        }
        return methods;
    }

    private List<IndexedField> parseFields(TypeDeclaration<?> typeDecl) {
        List<IndexedField> fields = new ArrayList<>();
        for (FieldDeclaration field : typeDecl.getFields()) {
            for (VariableDeclarator var : field.getVariables()) {
                IndexedField f = new IndexedField();
                f.setName(var.getNameAsString());
                f.setType(var.getType().asString());
                StringBuilder modSb = new StringBuilder();
                for (int i = 0; i < field.getModifiers().size(); i++) {
                    if (modSb.length() > 0) modSb.append(" ");
                    modSb.append(field.getModifiers().get(i).getKeyword().asString());
                }
                f.setModifiers(modSb.toString());
                f.setLine(field.getBegin().map(n -> n.line).orElse(0));
                f.setAnnotations(parseAnnotations(field.getAnnotations()));
                fields.add(f);
            }
        }
        return fields;
    }

    private List<IndexedAnnotation> parseAnnotations(NodeList<AnnotationExpr> annotationList) {
        List<IndexedAnnotation> annotations = new ArrayList<>();
        if (annotationList == null) return annotations;
        for (AnnotationExpr annotation : annotationList) {
            IndexedAnnotation a = new IndexedAnnotation();
            a.setName(annotation.getNameAsString());
            Map<String, String> values = new HashMap<>();
            List<MemberValuePair> pairs = annotation.findAll(MemberValuePair.class);
            for (MemberValuePair pair : pairs) {
                values.put(pair.getNameAsString(), pair.getValue().toString());
            }
            a.setValues(values.isEmpty() ? null : values);
            annotations.add(a);
        }
        return annotations;
    }

    private List<String> extractComments(CompilationUnit cu) {
        List<String> todos = new ArrayList<>();
        for (Comment comment : cu.getAllComments()) {
            String text = comment.getContent();
            if (text.contains("TODO") || text.contains("FIXME") || text.contains("HACK")) {
                int line = comment.getBegin().map(n -> n.line).orElse(0);
                todos.add("L" + line + ": " + text.trim());
            }
        }
        return todos;
    }

    private List<String> extractTodos(String content) {
        List<String> todos = new ArrayList<>();
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("TODO") || line.contains("FIXME") || line.contains("HACK")) {
                todos.add("L" + (i + 1) + ": " + line.trim());
            }
        }
        return todos;
    }

    public List<String> extractReferences(String content) {
        List<String> refs = new ArrayList<>();
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int idx = line.indexOf("import ");
            if (idx >= 0) {
                String imp = line.substring(idx + 7).replace(";", "").trim();
                if (!imp.isEmpty()) refs.add(imp);
            }
        }
        return refs;
    }

    private String getExtension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1).toLowerCase() : "";
    }

    private String md5(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(content.hashCode());
        }
    }
}
