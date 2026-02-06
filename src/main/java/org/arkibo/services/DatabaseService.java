package org.arkibo.services;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class DatabaseService {
    Dotenv dotenv = Dotenv.load();
    String connString = dotenv.get("DATABASE_URL");
    Connection conn;

    public DatabaseService() {
        try {
            this.conn = DriverManager.getConnection(connString);
            System.out.println("[DATABASE]: Connection established.");
        } catch (Exception e) {
            throw new RuntimeException("[DATABASE]: Connection unsuccessful: ", e);
        }
    }

    public <T> T query(String sql, SqlMapper<T> mapper, Object... params) throws SQLException {
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
        if (conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
    }

    public void commit() throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    public void rollback() throws SQLException {
        conn.rollback();
        conn.setAutoCommit(true);
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
