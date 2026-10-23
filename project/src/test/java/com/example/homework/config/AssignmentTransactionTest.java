package com.example.homework.config;

import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.DuplicateAssignment;
import com.example.homework.persistence.AssignmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AssignmentTransactionTest {

    @Autowired
    AssignmentService service;

    @Autowired
    AssignmentRepository assignments;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TransactionTemplate transactions;

    @BeforeEach
    void cleanDatabase() {
        assignments.deleteAll();
    }

    @Test
    void secondStatementRollsBack() {
        assertThrows(DuplicateAssignment.class, () -> service.insertTwice("HW-1042"));
        assertEquals(0, assignments.countByBusinessKey("HW-1042"));
    }

    @Test
    void secondRequestKeepsTheFirst() {
        service.register(AssignmentId.newId(), "HW-1042", "Essay");
        assertThrows(DuplicateAssignment.class,
                () -> service.register(AssignmentId.newId(), "HW-1042", "Essay again"));
        assertEquals(1, assignments.countByBusinessKey("HW-1042"));
    }

    @Test
    void statusIsStoredAndReadBack() {
        service.register(AssignmentId.newId(), "HW-7", "Lab report");
        assertEquals(AssignmentStatus.ASSIGNED, service.find("HW-7").status());
    }

    @Test
    void statusChangeIsPersisted() {
        service.register(AssignmentId.newId(), "HW-8", "Essay");
        service.move("HW-8", AssignmentStatus.SUBMITTED);
        assertEquals(AssignmentStatus.SUBMITTED, service.find("HW-8").status());
    }

    @Test
    void blankBusinessKeyIsRejectedBeforeTheDatabase() {
        assertThrows(IllegalStateException.class,
                () -> service.register(AssignmentId.newId(), " ", "Essay"));
    }

    @Test
    void databaseRefusesUnknownStatus() {
        assertThrows(PersistenceException.class, () ->
                transactions.executeWithoutResult(tx ->
                        entityManager.createNativeQuery(
                                        "INSERT INTO assignment (id, business_key, status, title) VALUES (?1, ?2, ?3, ?4)")
                                .setParameter(1, UUID.randomUUID())
                                .setParameter(2, "HW-9")
                                .setParameter(3, "ARCHIVED")
                                .setParameter(4, "x")
                                .executeUpdate()));
    }
}