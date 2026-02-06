package org.arkibo.repository;

import org.arkibo.dto.thesis.AuthorCreateReqeust;
import org.arkibo.dto.thesis.KeywordCreateRequest;
import org.arkibo.dto.thesis.ThesisCreateRequest;
import org.arkibo.models.ThesisModels.Author;
import org.arkibo.models.ThesisModels.Keyword;
import org.arkibo.models.ThesisModels.Thesis;
import org.arkibo.models.ThesisModels.ResearchType;
import org.arkibo.services.DatabaseService;

import org.arkibo.dto.Response;
import org.arkibo.utils.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ThesisRepository {
    final DatabaseService db = new DatabaseService();

    public Response<List<Thesis>> getUserSavedThesis(long userId) {
        String sql = """
        SELECT
            t.id             AS thesis_id,
            t.title          AS title,
            t.abstract       AS abstract_text,
            t.year           AS year,
            t.research_type  AS research_type,
            t.college        AS college,
            a.name           AS author_name,
            a.id             AS author_id,
            k.id             AS keyword_id,
            k.word           AS keyword_word,
            tt.id            AS research_type_id,
            tt.type          AS research_type_name
                
        FROM user_saved_theses ust
        JOIN theses t ON ust.thesis_id = t.id
        LEFT JOIN thesis_authors ta ON t.id = ta.thesis_id
        LEFT JOIN authors a ON ta.author_id = a.id
        LEFT JOIN thesis_keywords tk ON t.id = tk.thesis_id
        LEFT JOIN keywords k ON tk.keyword_id = k.id
        LEFT JOIN thesis_types_relation ttr ON t.id = ttr.thesis_id
        LEFT JOIN thesis_types tt ON ttr.type_id = tt.id
                
        WHERE ust.user_id = ?
        """;

        try {
            List<Thesis> theses = db.query(sql, this::mapTheses, userId);

            return Response.success("[THESIS]: Retrieved saved thesis.", theses);
        } catch (SQLException e) {
            return Response.error("[THESIS] Failed to retrieve saved theses: " + e.getMessage());
        }
    }

    public Response<Thesis> addThesis(ThesisCreateRequest thesis) {
        String title = thesis.title();
        List<AuthorCreateReqeust> authors = thesis.authors();
        String abstractText = thesis.abstractText();
        List<KeywordCreateRequest> keywords = thesis.keywords();
        String researchType = thesis.researchType();
        String college = thesis.college();
        int year = thesis.year();
        Long thesisId;

        try {
            db.begin();
            // Thesis insertion
            Logger.log("THESIS", "Thesis insert");
            List<Author> newAuthors = new ArrayList<Author>();
            List<Keyword> newKeywords = new ArrayList<Keyword>();
            String insertThesisQuery = """
                    INSERT INTO theses (title, abstract, year, research_type, college)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT (title) DO NOTHING
                    RETURNING id;
                    """;

            thesisId = db.query(insertThesisQuery, rs -> rs.next() ? rs.getLong("id") : null,
                    title, abstractText, year, researchType, college);
            Logger.log("THESIS", "Inserted thesis " + thesisId);


            // Authors
            Logger.log("THESIS", "Author insert");

            for (AuthorCreateReqeust author : authors) {
                String checkAuthorQuery = "SELECT id FROM authors WHERE name = ?";
                Long authorId = db.query(checkAuthorQuery, rs -> rs.next() ? rs.getLong("id") : null, author.name());
                if (authorId == null) {
                    String insertAuthorQuery = """
                            INSERT INTO authors (name)
                            VALUES (?)
                            ON CONFLICT (name) DO NOTHING
                            RETURNING id;
                            """;
                    authorId = db.query(insertAuthorQuery, rs -> rs.next() ? rs.getLong("id") : null, author.name());
                }
                Author appendedAuthor = new Author(authorId, author.name());
                newAuthors.add(appendedAuthor);

                String authorToThesisQuery = """
                        INSERT INTO thesis_authors (thesis_id, author_id)
                        VALUES (?, ?)
                        ON CONFLICT (thesis_id, author_id) DO NOTHING;
                        """;
                db.update(authorToThesisQuery, thesisId, authorId);
            }

            // Keywords
            Logger.log("THESIS", "Keyword insert");

            for (KeywordCreateRequest keyword : keywords) {
                String checkKeywordQuery = "SELECT id FROM keywords WHERE word = ?";
                Long keywordId = db.query(checkKeywordQuery, rs -> rs.next() ? rs.getLong("id") : null, keyword.word());
                if (keywordId == null) {
                    String insertKeywordQuery = """
                            INSERT INTO keywords (word)
                            VALUES (?)
                            ON CONFLICT (word) DO NOTHING
                            RETURNING id;
                            """;
                    keywordId = db.query(insertKeywordQuery, rs -> rs.next() ? rs.getLong("id") : null, keyword.word());
                }
                Keyword appendedKeyword = new Keyword(keywordId, keyword.word());
                newKeywords.add(appendedKeyword);

                String keywordToThesisQuery = """
                        INSERT INTO thesis_keywords (thesis_id, keyword_id)
                        VALUES (?, ?)
                        ON CONFLICT (thesis_id, keyword_id) DO NOTHING;
                        """;
                db.update(keywordToThesisQuery, thesisId, keywordId);
            }

            // Research Type
            Logger.log("THESIS", "Type insert");

            String checkTypeQuery = "SELECT id FROM thesis_types WHERE type = ?";
            Long typeId = db.query(checkTypeQuery, rs -> rs.next() ? rs.getLong("id") : null, researchType);
            if (typeId == null) {
                String insertTypeQuery = """
                        INSERT INTO thesis_types (type)
                        VALUES (?)
                        ON CONFLICT (type) DO NOTHING
                        RETURNING id;
                        """;
                typeId = db.query(insertTypeQuery, rs -> rs.next() ? rs.getLong("id") : null, researchType);
            }
            ResearchType newType = new ResearchType(typeId, researchType);

            String typeToThesisQuery = """
                    INSERT INTO thesis_types_relation (type_id, thesis_id)
                    VALUES (?, ?)
                    ON CONFLICT (type_id, thesis_id) DO NOTHING;
                    """;
            db.update(typeToThesisQuery, typeId, thesisId);

            db.commit();
            return Response.success("[THESIS]: Successfully added thesis",
                    new Thesis(thesisId, title, abstractText, newAuthors, newKeywords, year, newType, college ));
        } catch (SQLException e) {
            try {
                Logger.log("THESIS", "Error " + e.getMessage() + ", rolling back");
                db.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
                throw new RuntimeException("[THESIS]: Unable to rollback: " + rollbackException.getMessage());
            }
            throw new RuntimeException("[THESIS]: Unable to add thesis: " + e.getMessage());
        }

    }

    public Response<Void> deleteThesis(long thesisId) {
        try {
            db.begin();
            Logger.log("THESIS", "Deleting from user saved theses");
            db.update("DELETE FROM user_saved_theses WHERE thesis_id = ?", thesisId);
            Logger.log("THESIS", "Deleting from theses types relation");
            db.update("DELETE FROM thesis_types_relation WHERE thesis_id = ?", thesisId);

            Logger.log("THESIS", "Getting authors from the thesis");
            List<Long> authorIds = db.query("SELECT author_id FROM thesis_authors WHERE thesis_id = ?", rs -> {
                List<Long> ids = new ArrayList<Long>();
                while (rs.next()) {
                    ids.add(rs.getLong("author_id"));
                };
                return ids;
            }, thesisId);

            Logger.log("THESIS", "Getting keywords from the thesis");
            List<Long> keywordIds = db.query("SELECT keyword_id FROM thesis_keywords WHERE thesis_id = ?", rs -> {
                List<Long> ids = new ArrayList<Long>();
                while (rs.next()) {
                    ids.add(rs.getLong("keyword_id"));
                }
                return ids;
            }, thesisId);

            Logger.log("THESIS", "Removing the references on authors");
            db.update("DELETE FROM thesis_authors WHERE thesis_id = ?", thesisId);
            Logger.log("THESIS", "Removing the references on keywords");
            db.update("DELETE FROM thesis_keywords WHERE thesis_id = ?", thesisId);

            Logger.log("THESIS", "Removing the thesis completely");
            db.update("DELETE FROM theses WHERE id = ?", thesisId);

            for (Long id : authorIds) {
                boolean exists = db.query("SELECT EXISTS (SELECT 1 FROM thesis_authors WHERE author_id = ?) AS exists", rs -> {
                    rs.next();
                    return rs.getBoolean("exists");
                }, id);

                if (!exists) {
                    Logger.log("THESIS", "Deleting the author");

                    db.update("DELETE FROM authors WHERE id = ?", id);
                }
            }

            for (Long id : keywordIds) {
                boolean exists = db.query("SELECT EXISTS (SELECT 1 FROM thesis_keywords WHERE keyword_id = ?) AS exists", rs -> {
                    rs.next();
                    return rs.getBoolean("exists");
                }, thesisId);

                if (!exists) {
                    Logger.log("THESIS", "Deleting the keyword");
                    db.update("DELETE FROM keywords WHERE id = ?", id);
                }
            }

            db.commit();
            return Response.success("[THESIS]: Thesis deleted successfully", null);
        } catch (SQLException e) {
            try {
                Logger.log("THESIS", "Error " + e.getMessage() + ", rolling back");
                db.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
                throw new RuntimeException("[THESIS]: Unable to rollback: " + rollbackException.getMessage());
            }
            throw new RuntimeException("[THESIS]: Unable to delete thesis: " + e.getMessage());
        }
    }

    public Response<List<Thesis>> search(String query, Integer beforeYear, Integer afterYear, String type) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                t.id            AS thesis_id,
                t.title         AS title,
                t.abstract      AS abstract_text,
                t.year          AS year,
                t.research_type AS research_type,
                t.college       AS college,
                a.id            AS author_id,
                a.name          AS author_name,
                k.id            AS keyword_id,
                k.word          AS keyword_word,
                tt.id           AS research_type_id,
                tt.type         AS research_type_name
                
            FROM theses t
            LEFT JOIN thesis_authors ta ON t.id = ta.thesis_id
            LEFT JOIN authors a ON ta.author_id = a.id
            LEFT JOIN thesis_keywords tk ON t.id = tk.thesis_id
            LEFT JOIN keywords k ON tk.keyword_id = k.id
            LEFT JOIN thesis_types_relation ttr ON t.id = ttr.thesis_id
            LEFT JOIN thesis_types tt ON ttr.type_id = tt.id
                
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append("""
                AND (
                    t.title ILIKE ?
                    OR t.abstract ILIKE ?
                    OR a.name ILIKE ?
                    OR k.word ILIKE ?
                )
            """);

            String like = "%" + query + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (afterYear != null) {
            sql.append(" AND t.year >= ?");
            params.add(afterYear);
        }

        if (beforeYear != null) {
            sql.append(" AND t.year <= ?");
            params.add(beforeYear);
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND tt.name ILIKE ?");
            params.add("%" + type + "%");
        }


        try {
            db.begin();
            List<Thesis> theses = db.query(sql.toString(), this::mapTheses, params.toArray());
            db.commit();
            return Response.success("[SEARCH]: Search performed successfully", theses);

        } catch (SQLException e) {
            try {
                Logger.log("THESIS", "Error " + e.getMessage() + ", rolling back");
                db.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
                throw new RuntimeException("[THESIS]: Unable to rollback: " + rollbackException.getMessage());
            }
            throw new RuntimeException("[SEARCH]: Unable to perform search", e);
        }

    }

    public Response<List<Thesis>> keywordInfo(long keywordId) {
        String sqlQuery = """
                SELECT
                    t.id            AS thesis_id,
                    t.title         AS title,
                    t.abstract      AS abstract_text,
                    t.year          AS year,
                    t.college       AS college,
                    t.research_type AS research_type,
                
                    main_k.id   AS main_keyword_id,
                    main_k.word AS main_keyword_word,
                
                    a.id        AS author_id,
                    a.name      AS author_name,
                
                    k.id        AS keyword_id,
                    k.word      AS keyword_word,
                    
                    tt.id           AS research_type_id,
                    tt.type         AS research_type_name
                    
                FROM keywords main_k
                LEFT JOIN thesis_keywords tk ON main_k.id = tk.keyword_id
                LEFT JOIN theses t ON tk.thesis_id = t.id
                LEFT JOIN thesis_authors ta ON t.id = ta.thesis_id
                LEFT JOIN authors a ON ta.author_id = a.id
                LEFT JOIN thesis_keywords tk2 ON t.id = tk2.thesis_id
                LEFT JOIN keywords k ON tk2.keyword_id = k.id
                LEFT JOIN thesis_types_relation ttr ON t.id = ttr.thesis_id
                LEFT JOIN thesis_types tt ON ttr.type_id = tt.id
                WHERE main_k.id = ?;
                """;

        try {
            List<Thesis> theses = db.query(sqlQuery, this::mapTheses, keywordId);
            return Response.success("[THESIS]: Filter from keyword success.", theses);

        } catch (SQLException e) {
            throw new RuntimeException("[SEARCH]: Unable to perform search", e);
        }
    }

    public Response<Thesis> thesisInfo(long thesisId) {
        String query = """
                SELECT
                    t.id            AS thesis_id,
                    t.title         AS title,
                    t.abstract      AS abstract_text,
                    t.year          AS year,
                    t.college       AS college,
                    t.research_type AS research_type,
                
                    a.id            AS author_id,
                    a.name          AS author_name,
                
                    k.id            AS keyword_id,
                    k.word          AS keyword_word,
                
                    tt.id           AS research_type_id,
                    tt.type         AS research_type_name
                
                FROM theses t
                LEFT JOIN thesis_authors ta       ON t.id = ta.thesis_id
                LEFT JOIN authors a               ON ta.author_id = a.id
                LEFT JOIN thesis_keywords tk      ON t.id = tk.thesis_id
                LEFT JOIN keywords k              ON tk.keyword_id = k.id
                LEFT JOIN thesis_types_relation ttr ON t.id = ttr.thesis_id
                LEFT JOIN thesis_types tt         ON ttr.type_id = tt.id
                WHERE t.id = ?;
                    """;

        try {
            Thesis thesis = db.query(query, rs -> {
                ThesisBuilder builder = null;
                while (rs.next()) {
                    if (builder == null) {
                        builder = new ThesisBuilder(
                                rs.getLong("thesis_id"),
                                rs.getString("title"),
                                rs.getString("abstract_text"),
                                rs.getInt("year"),
                                new ResearchType(rs.getLong("research_type_id"), rs.getString("research_type")),
                                rs.getString("college")
                        );
                    }
                    long authorId = rs.getLong("author_id");
                    if (!rs.wasNull()) {
                        builder.addAuthor(
                                authorId,
                                rs.getString("author_name")
                        );
                    }

                    long keywordId = rs.getLong("keyword_id");
                    if (!rs.wasNull()) {
                        builder.addKeyword(
                                keywordId,
                                rs.getString("keyword_word")
                        );
                    }
                }
                return builder != null ? builder.build() : null;
            }, thesisId);

            return Response.success("[THESIS]: Thesis info obtainment succesful", thesis);
        } catch (SQLException e) {
            throw new RuntimeException("[SEARCH]: Unable to perform thesis info lookup", e);

        }
    }

    public Response<List<Thesis>> getAuthorInfo(long authorId) {
        String sqlQuery = """
                SELECT
                    t.id        AS thesis_id,
                    t.title     AS title,
                    t.abstract  AS abstract_text,
                    t.year      AS year,
                    t.college   AS college,
                
                    a.id        AS author_id,
                    a.name      AS author_name,
                
                    k.id        AS keyword_id,
                    k.word      AS keyword_word,
                   
                    tt.id           AS research_type_id,
                    tt.type         AS research_type_name
                    
                    FROM authors main_a
                    
                    JOIN thesis_authors ta_main ON main_a.id = ta_main.author_id
                    JOIN theses t ON ta_main.thesis_id = t.id
                    LEFT JOIN thesis_authors ta ON t.id = ta.thesis_id
                    LEFT JOIN authors a ON ta.author_id = a.id
                    LEFT JOIN thesis_keywords tk ON t.id = tk.thesis_id
                    LEFT JOIN keywords k ON tk.keyword_id = k.id
                    LEFT JOIN thesis_types_relation ttr ON t.id = ttr.thesis_id
                    LEFT JOIN thesis_types tt ON ttr.type_id = tt.id
                    WHERE main_a.id = ?;
                    """;

        try {
            List<Thesis> theses = db.query(sqlQuery, this::mapTheses, authorId);
            return Response.success("[THESIS]: Author lookup successful", theses);
        } catch (SQLException e) {
            throw new RuntimeException("[THESIS]: Unable to lookup author: ", e);
        }
    }

    // helpers
    private static class ThesisBuilder {

        long id;
        String title;
        String abstractText;
        int year;
        ResearchType researchType;
        String college;

        Set<Author> authors = new LinkedHashSet<>();
        Set<Keyword> keywords = new LinkedHashSet<>();

        ThesisBuilder(
                long id,
                String title,
                String abstractText,
                int year,
                ResearchType researchType,
                String college
        ) {
            this.id = id;
            this.title = title;
            this.abstractText = abstractText;
            this.year = year;
            this.researchType = researchType;
            this.college = college;
        }

        void addAuthor(long id, String name) {
            authors.add(new Author(id, name));
        }

        void addKeyword(long id, String word) {
            keywords.add(new Keyword(id, word));
        }

        Thesis build() {
            return new Thesis(
                    id,
                    title,
                    abstractText,
                    List.copyOf(authors),
                    List.copyOf(keywords),
                    year,
                    researchType,
                    college
            );
        }
    }


    private List<Thesis> mapTheses(ResultSet rs) throws SQLException {
        Map<Long, ThesisBuilder> map = new LinkedHashMap<>();

        while (rs.next()) {
            long id = rs.getLong("thesis_id");
            String title = rs.getString("title");
            String abstractText = rs.getString("abstract_text");
            int year = rs.getInt("year");
            String researchType = rs.getString("research_type_name");
            long researchTypeId = rs.getLong("research_type_id");
            String college = rs.getString("college");

            ThesisBuilder builder = map.computeIfAbsent(id, thesis ->
                    new ThesisBuilder(
                            id,
                            title,
                            abstractText,
                            year,
                            new ResearchType(researchTypeId, researchType),
                            college
                    )
            );

            String authorName = rs.getString("author_name");
            long authorId = rs.getLong("author_id");
            if (authorName != null) {
                builder.authors.add(new Author(authorId, authorName));
            }

            String keywordWord = rs.getString("keyword_word");
            long keywordId = rs.getLong("keyword_id");
            if (keywordWord != null) {
                builder.keywords.add(new Keyword(keywordId, keywordWord));
            }
        }

        return map.values()
                .stream()
                .map(ThesisBuilder::build)
                .toList();
    }

}
