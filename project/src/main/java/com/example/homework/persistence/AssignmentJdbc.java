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

    /** A duplicate business_key surfaces as DataIntegrityViolationException (SQLState 23505). */
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

    /** Reads the status text back and turns it into the enum. */
    public AssignmentStatus findStatus(String businessKey) {
        List<String> rows = jdbc.queryForList(
                "SELECT status FROM assignment WHERE business_key = ?",
                String.class,
                businessKey);
        if (rows.isEmpty()) {
            throw new IllegalStateException("No assignment with key " + businessKey);
        }
        return toStatus(rows.get(0));
    }

    /** An unknown text from the database throws. It never becomes a new status. */
    static AssignmentStatus toStatus(String text) {
        try {
            return AssignmentStatus.valueOf(text);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalStateException("Unknown assignment status in database: " + text, ex);
        }
    }
}
