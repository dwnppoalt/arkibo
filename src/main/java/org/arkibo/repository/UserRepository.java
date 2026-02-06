package org.arkibo.repository;

import org.arkibo.dto.UserCreateRequest;
import org.arkibo.models.User;
import org.arkibo.services.DatabaseService;
import org.arkibo.dto.Response;
import org.arkibo.utils.Logger;

import java.sql.SQLException;

public class UserRepository {
    final DatabaseService db = new DatabaseService();

    public Response<User> addUser(UserCreateRequest user) {
        String sql = """
        INSERT INTO users (name, email)
        VALUES (?, ?)
        ON CONFLICT (email) DO NOTHING
        RETURNING id
        """;

        try {
            db.begin();
            Long id = db.query(
                    sql,
                    rs -> rs.next() ? rs.getLong("id") : null,
                    user.name(),
                    user.email()
            );

            if (id == null) return Response.error("[USER]: User already exists.");
            User created = new User(id, user.name(), user.email(), null);
            db.commit();
            return Response.success(String.format("[USER]: User %s added.", created.name()), created);
        } catch (SQLException e) {
            try {
                Logger.log("USER", "Error " + e.getMessage() + ", rolling back");
                db.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
                throw new RuntimeException("[USER]: Unable to rollback: " + rollbackException.getMessage());
            }
            throw new RuntimeException("[USER]: Failed to add user: ", e);
        }
    }

    public Response<Void> addThesisToSaved(long userId, long thesisId) {
        String sql = """
                INSERT INTO user_saved_theses (user_id, thesis_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING;
                """;

        try {
            db.begin();
            int rows = db.update(sql, userId, thesisId);

            if (rows > 0) return Response.success("[USER]: Successfully added thesis.", null);
            db.commit();
            return Response.success("[USER]: Thesis already saved.", null);
        } catch (SQLException e) {
            try {
                Logger.log("USER", "Error " + e.getMessage() + ", rolling back");
                db.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
                throw new RuntimeException("[USER]: Unable to rollback: " + rollbackException.getMessage());
            }
            throw new RuntimeException("[USER]: Unable to add thesis: ", e);
        }
    }

    public Response<Void> removeThesisFromSaved(long userId, long thesisId) {
        String sql = """
                DELETE FROM user_saved_theses
                WHERE user_id = ? AND thesis_id = ?""";

        try {
            db.begin();

            int rows = db.update(sql, userId, thesisId);
            if (rows > 0) return Response.success("[USER]: Thesis removed from user.", null);
            return Response.error("[USER]: Thesis not found from the library");

        } catch (SQLException e) {
            throw new RuntimeException("[USER]: Unable to remove thesis from saved", e);
        }
    }

}
