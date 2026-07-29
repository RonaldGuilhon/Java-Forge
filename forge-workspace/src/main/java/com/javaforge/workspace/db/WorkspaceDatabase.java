package com.javaforge.workspace.db;

import com.javaforge.workspace.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceDatabase {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceDatabase.class);

    private final String dbPath;
    private Connection connection;

    public WorkspaceDatabase(String dbPath) {
        this.dbPath = dbPath;
    }

    public void open() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.setAutoCommit(false);
            createTables();
        } catch (Exception e) {
            log.error("Failed to open workspace database: {}", dbPath, e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Failed to close workspace database", e);
        }
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS files (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "path TEXT UNIQUE NOT NULL," +
                "file_name TEXT NOT NULL," +
                "extension TEXT," +
                "last_modified INTEGER," +
                "size INTEGER," +
                "hash TEXT" +
                ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS classes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "qualified_name TEXT," +
                "package_name TEXT," +
                "type TEXT," +
                "super_class TEXT," +
                "start_line INTEGER," +
                "end_line INTEGER," +
                "FOREIGN KEY(file_id) REFERENCES files(id) ON DELETE CASCADE" +
                ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS methods (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "class_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "return_type TEXT," +
                "parameters TEXT," +
                "parameter_types TEXT," +
                "modifiers TEXT," +
                "start_line INTEGER," +
                "end_line INTEGER," +
                "FOREIGN KEY(class_id) REFERENCES classes(id) ON DELETE CASCADE" +
                ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS fields (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "class_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "type TEXT," +
                "modifiers TEXT," +
                "line INTEGER," +
                "FOREIGN KEY(class_id) REFERENCES classes(id) ON DELETE CASCADE" +
                ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS imports (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_id INTEGER NOT NULL," +
                "import_text TEXT NOT NULL," +
                "FOREIGN KEY(file_id) REFERENCES files(id) ON DELETE CASCADE" +
                ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS annotations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "owner_type TEXT NOT NULL," +
                "owner_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "values TEXT" +
                ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS todos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_id INTEGER NOT NULL," +
                "text TEXT NOT NULL," +
                "line INTEGER," +
                "FOREIGN KEY(file_id) REFERENCES files(id) ON DELETE CASCADE" +
                ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS references_table (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "source_file_id INTEGER," +
                "source_class_id INTEGER," +
                "target_qualified_name TEXT NOT NULL," +
                "type TEXT," +
                "line INTEGER," +
                "FOREIGN KEY(source_file_id) REFERENCES files(id) ON DELETE CASCADE" +
                ")");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_classes_file ON classes(file_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_classes_name ON classes(name)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_classes_qualified ON classes(qualified_name)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_methods_class ON methods(class_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_fields_class ON fields(class_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_imports_file ON imports(file_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_todos_file ON todos(file_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_references_target ON references_table(target_qualified_name)");
        connection.commit();
        stmt.close();
    }

    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void commitTransaction() throws SQLException {
        connection.commit();
    }

    public void rollbackTransaction() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error("Rollback failed", e);
        }
    }

    public long upsertFile(IndexedFile file) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO files (path, file_name, extension, last_modified, size, hash) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(path) DO UPDATE SET " +
                "file_name=excluded.file_name, extension=excluded.extension, " +
                "last_modified=excluded.last_modified, size=excluded.size, hash=excluded.hash " +
                "RETURNING id"
        );
        ps.setString(1, file.getPath());
        ps.setString(2, file.getFileName());
        ps.setString(3, file.getExtension());
        ps.setLong(4, file.getLastModified());
        ps.setLong(5, file.getSize());
        ps.setString(6, file.getHash());
        ResultSet rs = ps.executeQuery();
        long id = rs.next() ? rs.getLong(1) : -1;
        rs.close();
        ps.close();
        file.setId(id);
        return id;
    }

    public void deleteFile(long fileId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM files WHERE id = ?");
        ps.setLong(1, fileId);
        ps.executeUpdate();
        ps.close();
    }

    public List<IndexedFile> getAllFiles() throws SQLException {
        List<IndexedFile> files = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, path, file_name, extension, last_modified, size, hash FROM files");
        while (rs.next()) {
            IndexedFile f = new IndexedFile();
            f.setId(rs.getLong("id"));
            f.setPath(rs.getString("path"));
            f.setFileName(rs.getString("file_name"));
            f.setExtension(rs.getString("extension"));
            f.setLastModified(rs.getLong("last_modified"));
            f.setSize(rs.getLong("size"));
            f.setHash(rs.getString("hash"));
            files.add(f);
        }
        rs.close();
        stmt.close();
        return files;
    }

    public long insertClass(IndexedClass cls) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO classes (file_id, name, qualified_name, package_name, type, super_class, start_line, end_line) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
        );
        ps.setLong(1, cls.getFileId());
        ps.setString(2, cls.getName());
        ps.setString(3, cls.getQualifiedName());
        ps.setString(4, cls.getPackageName());
        ps.setString(5, cls.getType());
        ps.setString(6, cls.getSuperClass());
        ps.setInt(7, cls.getStartLine());
        ps.setInt(8, cls.getEndLine());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        long id = rs.next() ? rs.getLong(1) : -1;
        rs.close();
        ps.close();
        cls.setId(id);
        return id;
    }

    public long insertMethod(IndexedMethod method) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO methods (class_id, name, return_type, parameters, parameter_types, modifiers, start_line, end_line) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
        );
        ps.setLong(1, method.getClassId());
        ps.setString(2, method.getName());
        ps.setString(3, method.getReturnType());
        ps.setString(4, joinList(method.getParameters()));
        ps.setString(5, joinList(method.getParameterTypes()));
        ps.setString(6, method.getModifiers());
        ps.setInt(7, method.getStartLine());
        ps.setInt(8, method.getEndLine());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        long id = rs.next() ? rs.getLong(1) : -1;
        rs.close();
        ps.close();
        return id;
    }

    public long insertField(IndexedField field) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO fields (class_id, name, type, modifiers, line) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        ps.setLong(1, field.getClassId());
        ps.setString(2, field.getName());
        ps.setString(3, field.getType());
        ps.setString(4, field.getModifiers());
        ps.setInt(5, field.getLine());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        long id = rs.next() ? rs.getLong(1) : -1;
        rs.close();
        ps.close();
        return id;
    }

    public void insertImport(long fileId, String importText) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO imports (file_id, import_text) VALUES (?, ?)"
        );
        ps.setLong(1, fileId);
        ps.setString(2, importText);
        ps.executeUpdate();
        ps.close();
    }

    public void insertAnnotation(String ownerType, long ownerId, String name, String values) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO annotations (owner_type, owner_id, name, values) VALUES (?, ?, ?, ?)"
        );
        ps.setString(1, ownerType);
        ps.setLong(2, ownerId);
        ps.setString(3, name);
        ps.setString(4, values);
        ps.executeUpdate();
        ps.close();
    }

    public void insertTodo(long fileId, String text, int line) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO todos (file_id, text, line) VALUES (?, ?, ?)"
        );
        ps.setLong(1, fileId);
        ps.setString(2, text);
        ps.setInt(3, line);
        ps.executeUpdate();
        ps.close();
    }

    public void insertReference(long sourceFileId, Long sourceClassId, String target, String type, int line) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO references_table (source_file_id, source_class_id, target_qualified_name, type, line) VALUES (?, ?, ?, ?, ?)"
        );
        ps.setLong(1, sourceFileId);
        if (sourceClassId != null) {
            ps.setLong(2, sourceClassId);
        } else {
            ps.setNull(2, Types.INTEGER);
        }
        ps.setString(3, target);
        ps.setString(4, type);
        ps.setInt(5, line);
        ps.executeUpdate();
        ps.close();
    }

    public void clearFileData(long fileId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM imports WHERE file_id = ?"
        );
        ps.setLong(1, fileId);
        ps.executeUpdate();
        ps.close();

        ps = connection.prepareStatement(
                "DELETE FROM todos WHERE file_id = ?"
        );
        ps.setLong(1, fileId);
        ps.executeUpdate();
        ps.close();

        ps = connection.prepareStatement(
                "DELETE FROM references_table WHERE source_file_id = ?"
        );
        ps.setLong(1, fileId);
        ps.executeUpdate();
        ps.close();

        ps = connection.prepareStatement(
                "DELETE FROM annotations WHERE owner_type='file' AND owner_id = ?"
        );
        ps.setLong(1, fileId);
        ps.executeUpdate();
        ps.close();

        ps = connection.prepareStatement(
                "DELETE FROM methods WHERE class_id IN (SELECT id FROM classes WHERE file_id = ?)"
        );
        ps.setLong(1, fileId);
        ps.executeUpdate();
        ps.close();

        ps = connection.prepareStatement(
                "DELETE FROM fields WHERE class_id IN (SELECT id FROM classes WHERE file_id = ?)"
        );
        ps.setLong(1, fileId);
        ps.executeUpdate();
        ps.close();

        ps = connection.prepareStatement(
                "DELETE FROM annotations WHERE (owner_type='class' OR owner_type='method' OR owner_type='field') AND owner_id IN " +
                "(SELECT id FROM classes WHERE file_id = ?)"
        );
        ps.setLong(1, fileId);
        ps.executeUpdate();
        ps.close();

        ps = connection.prepareStatement("DELETE FROM classes WHERE file_id = ?");
        ps.setLong(1, fileId);
        ps.executeUpdate();
        ps.close();
    }

    public List<IndexedClass> findClassesByName(String name) throws SQLException {
        List<IndexedClass> results = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT c.*, f.path as file_path FROM classes c JOIN files f ON c.file_id = f.id WHERE c.name = ?"
        );
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            results.add(mapClass(rs));
        }
        rs.close();
        ps.close();
        return results;
    }

    public List<IndexedClass> searchClasses(String query) throws SQLException {
        List<IndexedClass> results = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT c.*, f.path as file_path FROM classes c JOIN files f ON c.file_id = f.id " +
                "WHERE c.name LIKE ? OR c.qualified_name LIKE ?"
        );
        String pattern = "%" + query + "%";
        ps.setString(1, pattern);
        ps.setString(2, pattern);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            results.add(mapClass(rs));
        }
        rs.close();
        ps.close();
        return results;
    }

    public List<IndexedMethod> findMethodsByClass(long classId) throws SQLException {
        List<IndexedMethod> results = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM methods WHERE class_id = ? ORDER BY start_line"
        );
        ps.setLong(1, classId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            IndexedMethod m = new IndexedMethod();
            m.setId(rs.getLong("id"));
            m.setClassId(rs.getLong("class_id"));
            m.setName(rs.getString("name"));
            m.setReturnType(rs.getString("return_type"));
            m.setParameters(splitList(rs.getString("parameters")));
            m.setParameterTypes(splitList(rs.getString("parameter_types")));
            m.setModifiers(rs.getString("modifiers"));
            m.setStartLine(rs.getInt("start_line"));
            m.setEndLine(rs.getInt("end_line"));
            results.add(m);
        }
        rs.close();
        ps.close();
        return results;
    }

    public List<IndexedField> findFieldsByClass(long classId) throws SQLException {
        List<IndexedField> results = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM fields WHERE class_id = ?"
        );
        ps.setLong(1, classId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            IndexedField f = new IndexedField();
            f.setId(rs.getLong("id"));
            f.setClassId(rs.getLong("class_id"));
            f.setName(rs.getString("name"));
            f.setType(rs.getString("type"));
            f.setModifiers(rs.getString("modifiers"));
            f.setLine(rs.getInt("line"));
            results.add(f);
        }
        rs.close();
        ps.close();
        return results;
    }

    public List<String> findImportsByFile(long fileId) throws SQLException {
        List<String> results = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT import_text FROM imports WHERE file_id = ?"
        );
        ps.setLong(1, fileId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            results.add(rs.getString("import_text"));
        }
        rs.close();
        ps.close();
        return results;
    }

    public List<String> findTodosByFile(long fileId) throws SQLException {
        List<String> results = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT text FROM todos WHERE file_id = ?"
        );
        ps.setLong(1, fileId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            results.add(rs.getString("text"));
        }
        rs.close();
        ps.close();
        return results;
    }

    public List<String> getUnreferencedMethods() throws SQLException {
        List<String> results = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT m.name, c.qualified_name, c.file_id FROM methods m " +
                "JOIN classes c ON m.class_id = c.id " +
                "WHERE m.name NOT IN (" +
                "  SELECT DISTINCT substr(target_qualified_name, instr(target_qualified_name, '.') + 1) " +
                "  FROM references_table WHERE type = 'METHOD_CALL'" +
                ") AND m.name NOT IN ('main', 'toString', 'hashCode', 'equals', 'init', 'destroy')"
        );
        while (rs.next()) {
            results.add(rs.getString("qualified_name") + "#" + rs.getString("name"));
        }
        rs.close();
        stmt.close();
        return results;
    }

    public String getProjectContext() throws SQLException {
        StringBuilder ctx = new StringBuilder();
        ctx.append("=== PROJECT INDEX ===\n\n");

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM classes");
        if (rs.next()) ctx.append("Total classes: ").append(rs.getInt(1)).append("\n");
        rs.close();

        rs = stmt.executeQuery("SELECT COUNT(*) FROM methods");
        if (rs.next()) ctx.append("Total methods: ").append(rs.getInt(1)).append("\n");
        rs.close();

        rs = stmt.executeQuery("SELECT COUNT(*) FROM files");
        if (rs.next()) ctx.append("Total files: ").append(rs.getInt(1)).append("\n");
        rs.close();

        rs = stmt.executeQuery("SELECT COUNT(*) FROM todos");
        if (rs.next() && rs.getInt(1) > 0) ctx.append("TODOs: ").append(rs.getInt(1)).append("\n");
        rs.close();

        ctx.append("\n--- Classes ---\n");
        rs = stmt.executeQuery("SELECT c.name, c.package_name, c.type FROM classes c ORDER BY c.qualified_name LIMIT 100");
        while (rs.next()) {
            ctx.append(rs.getString("type")).append(" ")
               .append(rs.getString("qualified_name")).append("\n");
        }
        rs.close();
        stmt.close();

        return ctx.toString();
    }

    private IndexedClass mapClass(ResultSet rs) throws SQLException {
        IndexedClass c = new IndexedClass();
        c.setId(rs.getLong("id"));
        c.setFileId(rs.getLong("file_id"));
        c.setName(rs.getString("name"));
        c.setQualifiedName(rs.getString("qualified_name"));
        c.setPackageName(rs.getString("package_name"));
        c.setType(rs.getString("type"));
        c.setSuperClass(rs.getString("super_class"));
        c.setStartLine(rs.getInt("start_line"));
        c.setEndLine(rs.getInt("end_line"));
        return c;
    }

    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (sb.length() > 0) sb.append(",");
            sb.append(s);
        }
        return sb.toString();
    }

    private List<String> splitList(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) return result;
        for (String s : text.split(",")) {
            result.add(s.trim());
        }
        return result;
    }
}
