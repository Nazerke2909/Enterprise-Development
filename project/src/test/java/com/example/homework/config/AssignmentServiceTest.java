package com.example.homework.config;
import com.example.homework.domain.AssignmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AssignmentServiceTest {
    @Autowired
    AssignmentService service;

    @Test
    void allowedMove() {
        assertEquals(AssignmentStatus.SUBMITTED,
                service.move(AssignmentStatus.ASSIGNED, AssignmentStatus.SUBMITTED));
    }

    @Test
    void forbiddenMove() {
        assertThrows(IllegalStateException.class,
                () -> service.move(AssignmentStatus.APPROVED, AssignmentStatus.SUBMITTED));
    }
}