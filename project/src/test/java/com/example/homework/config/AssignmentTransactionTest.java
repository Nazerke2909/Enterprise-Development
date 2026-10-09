package com.example.homework.config;

import com.example.homework.persistence.AssignmentJdbc;
import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.DuplicateAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AssignmentTransactionTest {
    @Autowired
    AssignmentService service;
    @Autowired
    AssignmentJdbc assignments;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() throws IOException {
        jdbc.execute("DROP TABLE IF EXISTS assignment");
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/schema.sql")));
    }

    @Test
    void secondStatementRollsBack() {
        assertThrows(DuplicateAssignment.class, () -> service.insertTwice("HW-1042"));
        assertEquals(0, assignments.count("HW-1042"));
    }

    @Test
    void secondRequestKeepsTheFirst() {
        service.register(AssignmentId.newId(), "HW-1042", "Essay");

        assertThrows(DuplicateAssignment.class,
                () -> service.register(AssignmentId.newId(), "HW-1042", "Essay again"));

        assertEquals(1, assignments.count("HW-1042"));
    }

    @Test
    void statusIsStoredAndReadBack() {
        service.register(AssignmentId.newId(), "HW-7", "Lab report");
        assertEquals(AssignmentStatus.ASSIGNED, assignments.findStatus("HW-7"));
    }

    @Test
    void blankBusinessKeyIsRejectedBeforeTheDatabase() {
        assertThrows(IllegalStateException.class,
                () -> service.register(AssignmentId.newId(), " ", "Essay"));
    }

    @Test
    void databaseRefusesUnknownStatus() {
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO assignment (id, business_key, status, title) VALUES (?, ?, ?, ?)",
                java.util.UUID.randomUUID(), "HW-9", "ARCHIVED", "x"));
    }
}