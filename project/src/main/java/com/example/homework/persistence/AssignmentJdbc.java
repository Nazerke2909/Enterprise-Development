package com.example.homework.persistence;

import com.example.homework.domain.AssignmentStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AssignmentJdbc {
    private final JdbcTemplate jdbc;

    public AssignmentJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(UUID id, String businessKey, AssignmentStatus status, String title) {
        jdbc.update("""
                INSERT INTO assignment (id, business_key, status, title)
                VALUES (?, ?, ?, ?)
                """, id, businessKey, status.name(), title);
    }

    public int count(String businessKey) {
        Integer n = jdbc.queryForObject(
                "SELECT count(*) FROM assignment WHERE business_key = ?",
                Integer.class,
                businessKey);
        return n == null ? 0 : n;
    }

    public AssignmentRecord find(String businessKey) {
        List<AssignmentRecord> rows = jdbc.query("""
                SELECT id, business_key, status, title
                FROM assignment
                WHERE business_key = ?
                """, (rs, rowNum) -> new AssignmentRecord(
                rs.getObject("id", UUID.class),
                rs.getString("business_key"),
                toStatus(rs.getString("status")),
                rs.getString("title")), businessKey);
        if (rows.isEmpty()) {
            throw new IllegalStateException("No assignment with key " + businessKey);
        }
        return rows.get(0);
    }

    public List<AssignmentRecord> findAll() {
        return jdbc.query("""
                SELECT id, business_key, status, title
                FROM assignment
                ORDER BY created_at, business_key
                """, (rs, rowNum) -> new AssignmentRecord(
                rs.getObject("id", UUID.class),
                rs.getString("business_key"),
                toStatus(rs.getString("status")),
                rs.getString("title")));
    }

    public void updateStatus(UUID id, AssignmentStatus status) {
        jdbc.update(
                "UPDATE assignment SET status = ? WHERE id = ?",
                status.name(), id);
    }

    public AssignmentStatus findStatus(String businessKey) {
        return find(businessKey).status();
    }

    static AssignmentStatus toStatus(String text) {
        try {
            return AssignmentStatus.valueOf(text);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalStateException("Unknown assignment status in database: " + text, ex);
        }
    }

    public record AssignmentRecord(
            UUID id,
            String businessKey,
            AssignmentStatus status,
            String title) {
    }
}
