package com.javaforge.workspace.indexer;

import com.javaforge.workspace.db.WorkspaceDatabase;
import com.javaforge.workspace.model.*;
import com.javaforge.workspace.parser.JavaParserService;
import com.javaforge.workspace.watch.FileWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class WorkspaceIndexer {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceIndexer.class);

    private final WorkspaceDatabase db;
    private final JavaParserService parser = new JavaParserService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "workspace-indexer");
        t.setDaemon(true);
        return t;
    });

    private Path workspacePath;
    private FileWatcher fileWatcher;
    private volatile boolean indexing = false;
    private final List<Runnable> onIndexComplete = new CopyOnWriteArrayList<>();

    public WorkspaceIndexer(WorkspaceDatabase db) {
        this.db = db;
    }

    public void setWorkspacePath(Path path) {
        this.workspacePath = path;
    }

    public void addOnCompleteListener(Runnable listener) {
        onIndexComplete.add(listener);
    }

    public boolean isIndexing() {
        return indexing;
    }

    public CompletableFuture<Void> indexWorkspace(Path path) {
        this.workspacePath = path;
        indexing = true;

        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Indexing workspace: {}", path);
                long start = System.currentTimeMillis();

                db.clearFileData(-1);
                db.beginTransaction();

                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (isSupportedFile(file)) {
                            try {
                doIndexFile(file);
                            } catch (SQLException e) {
                                log.warn("Error indexing file: {}", file, e);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return isIgnored(dir) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                    }
                });

                db.commitTransaction();
                log.info("Indexing complete in {} ms", System.currentTimeMillis() - start);

                startFileWatcher();
                notifyComplete();
            } catch (Exception e) {
                db.rollbackTransaction();
                log.error("Indexing failed", e);
            } finally {
                indexing = false;
            }
        }, executor);
    }

    public CompletableFuture<Void> indexFile(Path file) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!Files.exists(file)) return;
                db.beginTransaction();

                String pathStr = file.toAbsolutePath().normalize().toString();
                IndexedFile existing = findExistingFile(pathStr);
                if (existing != null) {
                    long currentModified = file.toFile().lastModified();
                    if (existing.getLastModified() == currentModified && existing.getHash() != null) {
                        db.commitTransaction();
                        return;
                    }
                    db.clearFileData(existing.getId());
                }

                indexFile(file);
                db.commitTransaction();
                notifyComplete();
            } catch (Exception e) {
                db.rollbackTransaction();
                log.warn("Error indexing single file: {}", file, e);
            }
        }, executor);
    }

    public CompletableFuture<Void> removeFile(Path file) {
        return CompletableFuture.runAsync(() -> {
            try {
                String pathStr = file.toAbsolutePath().normalize().toString();
                IndexedFile existing = findExistingFile(pathStr);
                if (existing != null) {
                    db.beginTransaction();
                    db.clearFileData(existing.getId());
                    db.deleteFile(existing.getId());
                    db.commitTransaction();
                }
            } catch (Exception e) {
                db.rollbackTransaction();
                log.warn("Error removing file from index: {}", file, e);
            }
        }, executor);
    }

    public void search(String query, IndexSearchCallback callback) {
        executor.submit(() -> {
            try {
                List<IndexedClass> classes = db.searchClasses(query);
                callback.onResult(classes);
            } catch (SQLException e) {
                log.error("Search failed", e);
                callback.onResult(Collections.emptyList());
            }
        });
    }

    public void getProjectContext(ProjectContextCallback callback) {
        executor.submit(() -> {
            try {
                String context = db.getProjectContext();
                callback.onResult(context);
            } catch (SQLException e) {
                log.error("Failed to get project context", e);
                callback.onResult("");
            }
        });
    }

    private IndexedFile findExistingFile(String pathStr) throws SQLException {
        List<IndexedFile> allFiles = db.getAllFiles();
        for (IndexedFile f : allFiles) {
            if (f.getPath().equals(pathStr)) return f;
        }
        return null;
    }

    private void doIndexFile(Path file) throws SQLException {
        IndexedFile indexedFile = parser.parseFile(file);
        long fileId = db.upsertFile(indexedFile);

        for (IndexedClass cls : indexedFile.getClasses()) {
            cls.setFileId(fileId);
            long classId = db.insertClass(cls);
            storeMethods(classId, cls.getMethods());
            storeFields(classId, cls.getFields());
            storeAnnotations("class", classId, cls.getAnnotations());
            storeImports(fileId, cls.getImports());
            storeReferences(fileId, classId, cls.getImports());
        }

        if (indexedFile.getTodos() != null) {
            for (String todo : indexedFile.getTodos()) {
                db.insertTodo(fileId, todo, 0);
            }
        }
    }

    private void storeMethods(long classId, List<IndexedMethod> methods) throws SQLException {
        if (methods == null) return;
        for (IndexedMethod m : methods) {
            m.setClassId(classId);
            long methodId = db.insertMethod(m);
            storeAnnotations("method", methodId, m.getAnnotations());
        }
    }

    private void storeFields(long classId, List<IndexedField> fields) throws SQLException {
        if (fields == null) return;
        for (IndexedField f : fields) {
            f.setClassId(classId);
            long fieldId = db.insertField(f);
            storeAnnotations("field", fieldId, f.getAnnotations());
        }
    }

    private void storeAnnotations(String ownerType, long ownerId, List<IndexedAnnotation> annotations) throws SQLException {
        if (annotations == null) return;
        for (IndexedAnnotation a : annotations) {
            String values = a.getValues() != null ? a.getValues().toString() : null;
            db.insertAnnotation(ownerType, ownerId, a.getName(), values);
        }
    }

    private void storeImports(long fileId, List<String> imports) throws SQLException {
        if (imports == null) return;
        for (String imp : imports) {
            db.insertImport(fileId, imp);
        }
    }

    private void storeReferences(long fileId, long classId, List<String> imports) throws SQLException {
        if (imports == null) return;
        for (String imp : imports) {
            db.insertReference(fileId, classId, imp, "IMPORT", 0);
        }
    }

    private void startFileWatcher() {
        if (workspacePath == null) return;
        if (fileWatcher != null) fileWatcher.stop();

        fileWatcher = new FileWatcher(workspacePath);
        fileWatcher.addListener(event -> {
            WatchEvent.Kind<?> kind = event.kind();
            Path context = (Path) event.context();
            if (workspacePath != null) {
                Path fullPath = workspacePath.resolve(context);
                if (isSupportedFile(fullPath)) {
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        indexFile(fullPath);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        removeFile(fullPath);
                    }
                }
            }
        });
        fileWatcher.start();
    }

    private boolean isSupportedFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return name.endsWith(".java") || name.endsWith(".xml") || name.endsWith(".properties")
                || name.endsWith(".yaml") || name.endsWith(".yml") || name.endsWith(".json")
                || name.endsWith(".sql") || name.endsWith(".xhtml") || name.endsWith(".css")
                || name.endsWith(".js") || name.endsWith(".html") || name.endsWith(".gradle");
    }

    private boolean isIgnored(Path dir) {
        String name = dir.getFileName().toString();
        return name.equals(".git") || name.equals("target") || name.equals("build")
                || name.equals("node_modules") || name.equals(".idea") || name.equals(".settings")
                || name.startsWith(".");
    }

    private void notifyComplete() {
        for (Runnable r : onIndexComplete) {
            try {
                r.run();
            } catch (Exception e) {
                log.warn("Index complete listener error", e);
            }
        }
    }

    public interface IndexSearchCallback {
        void onResult(List<IndexedClass> results);
    }

    public interface ProjectContextCallback {
        void onResult(String context);
    }
}
