package com.javaforge.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class GitService implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(GitService.class);
    private Git git;
    private Repository repository;
    private Path repoPath;

    public boolean open(Path path) {
        try {
            this.repoPath = path;
            repository = new RepositoryBuilder()
                .findGitDir(path.toFile())
                .build();
            if (repository != null) {
                git = new Git(repository);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to open git repo at {}", path, e);
            return false;
        }
    }

    public boolean init(Path path) {
        try {
            git = Git.init().setDirectory(path.toFile()).call();
            repository = git.getRepository();
            repoPath = path;
            return true;
        } catch (Exception e) {
            log.error("Failed to init git repo", e);
            return false;
        }
    }

    public Status status() {
        try {
            return git.status().call();
        } catch (Exception e) {
            log.error("Failed to get status", e);
            return null;
        }
    }

    public String branch() {
        try {
            return repository.getBranch();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public List<String> branches() {
        List<String> list = new ArrayList<>();
        try {
            List<Ref> refs = git.branchList().call();
            for (Ref ref : refs) {
                list.add(Repository.shortenRefName(ref.getName()));
            }
        } catch (Exception e) {
            log.error("Failed to list branches", e);
        }
        return list;
    }

    public RevCommit commit(String message) {
        try {
            return git.commit()
                .setMessage(message)
                .setAuthor("Java Forge", "forge@javaforge.com")
                .call();
        } catch (Exception e) {
            log.error("Failed to commit", e);
            return null;
        }
    }

    public boolean checkout(String branch) {
        try {
            git.checkout().setName(branch).call();
            return true;
        } catch (Exception e) {
            log.error("Failed to checkout {}", branch, e);
            return false;
        }
    }

    public boolean checkoutCreate(String branch) {
        try {
            git.checkout().setCreateBranch(true).setName(branch).call();
            return true;
        } catch (Exception e) {
            log.error("Failed to create and checkout {}", branch, e);
            return false;
        }
    }

    public boolean push() {
        try {
            git.push().call();
            return true;
        } catch (Exception e) {
            log.error("Failed to push", e);
            return false;
        }
    }

    public boolean pull() {
        try {
            git.pull().call();
            return true;
        } catch (Exception e) {
            log.error("Failed to pull", e);
            return false;
        }
    }

    public List<RevCommit> log(int max) {
        List<RevCommit> list = new ArrayList<>();
        try {
            git.log().setMaxCount(max).call().forEach(list::add);
        } catch (Exception e) {
            log.error("Failed to get log", e);
        }
        return list;
    }

    public String diff() {
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            StringBuilder out = new StringBuilder();
            List<DiffEntry> diffEntries = git.diff().call();
            for (DiffEntry entry : diffEntries) {
                out.append("Diff: ").append(entry.getNewPath()).append("\n");
            }
            return out.toString();
        } catch (Exception e) {
            log.error("Failed to diff", e);
            return "";
        }
    }

    public boolean isOpen() {
        return git != null;
    }

    @Override
    public void close() {
        if (git != null) git.close();
    }
}
