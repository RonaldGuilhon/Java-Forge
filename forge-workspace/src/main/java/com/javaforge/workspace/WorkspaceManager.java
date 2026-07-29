package com.javaforge.workspace;

import com.javaforge.workspace.db.WorkspaceDatabase;
import com.javaforge.workspace.indexer.WorkspaceIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class WorkspaceManager {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceManager.class);

    private static WorkspaceManager instance;

    private final WorkspaceDatabase db;
    private final WorkspaceIndexer indexer;
    private Path currentWorkspacePath;
    private boolean initialized = false;

    private WorkspaceManager() {
        String dbPath = Paths.get(System.getProperty("user.home"), ".javaforge", "workspace.db").toString();
        this.db = new WorkspaceDatabase(dbPath);
        this.indexer = new WorkspaceIndexer(db);
    }

    public static synchronized WorkspaceManager getInstance() {
        if (instance == null) {
            instance = new WorkspaceManager();
        }
        return instance;
    }

    public void initialize() {
        if (initialized) return;
        db.open();
        initialized = true;
        log.info("WorkspaceManager initialized");
    }

    public void shutdown() {
        db.close();
        log.info("WorkspaceManager shut down");
    }

    public CompletableFuture<Void> openWorkspace(Path path) {
        this.currentWorkspacePath = path;
        indexer.setWorkspacePath(path);
        log.info("Opening workspace: {}", path);
        return indexer.indexWorkspace(path);
    }

    public boolean isWorkspaceOpen() {
        return currentWorkspacePath != null;
    }

    public Path getCurrentWorkspacePath() {
        return currentWorkspacePath;
    }

    public WorkspaceIndexer getIndexer() {
        return indexer;
    }

    public WorkspaceDatabase getDatabase() {
        return db;
    }

    public void search(String query, WorkspaceIndexer.IndexSearchCallback callback) {
        indexer.search(query, callback);
    }

    public void getProjectContext(WorkspaceIndexer.ProjectContextCallback callback) {
        indexer.getProjectContext(callback);
    }

    public void addOnIndexCompleteListener(Runnable listener) {
        indexer.addOnCompleteListener(listener);
    }
}
