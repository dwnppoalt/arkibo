package org.arkibo.services;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class DatabaseService {
    Dotenv dotenv = Dotenv.load();
    String connString = dotenv.get("DATABASE_URL");
    Connection conn;

    public DatabaseService() {
        connect();
    }

    private void connect() {
        try {
            this.conn = DriverManager.getConnection(connString);
            System.out.println("[DATABASE]: Connection established.");
        } catch (Exception e) {
            throw new RuntimeException("[DATABASE]: Connection unsuccessful: ", e);
        }
    }

    private void ensureConnection() throws SQLException {
        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
            System.out.println("[DATABASE]: Connection lost, reconnecting...");
            connect();
        }
    }

    public <T> T query(String sql, SqlMapper<T> mapper, Object... params) throws SQLException {
        ensureConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return mapper.map(rs);
            }
        }
    }

    public int update(String sql, Object... params) throws SQLException {
        ensureConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }

    public boolean exists(String sql, Object... params) throws SQLException {
        return query(sql, ResultSet::next, params);
    }

    public void begin() throws SQLException {
        ensureConnection();
        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
    }

    public void commit() throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    public void rollback() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.rollback();
            conn.setAutoCommit(true);
        }
    }

    public void close() throws SQLException {
        if (this.conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @FunctionalInterface
    public interface SqlMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

}
