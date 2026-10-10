package com.example.homework.config;

import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "homework.console.enabled=false")
class AssignmentServiceTest {
    @Autowired
    AssignmentService service;

    @Test
    void allowedMove() {
        assertEquals(AssignmentStatus.SUBMITTED,
                service.move(AssignmentId.newId(), AssignmentStatus.ASSIGNED, AssignmentStatus.SUBMITTED));
    }

    @Test
    void forbiddenMove() {
        assertThrows(IllegalStateException.class,
                () -> service.move(AssignmentId.newId(), AssignmentStatus.APPROVED, AssignmentStatus.SUBMITTED));
    }
}