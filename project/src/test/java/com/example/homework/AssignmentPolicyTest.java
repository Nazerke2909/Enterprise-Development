package com.example.homework;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class AssignmentPolicyTest {

    private final AssignmentPolicy policy = new AssignmentPolicy();
    private final AssignmentId id = new AssignmentId("HW-1");

    @ParameterizedTest
    @CsvSource({
            "ASSIGNED, SUBMITTED, true",
            "SUBMITTED, CHECKED, true",
            "CHECKED, ASSIGNED, false",
            "APPROVED, SUBMITTED, false"
    })
    void testMove(
            AssignmentStatus from,
            AssignmentStatus to,
            boolean allowed
    ) {
        if (allowed) {
            AssignmentStatus result = policy.move(id, from, to);
            assertEquals(to, result);
        } else {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> policy.move(id, from, to)
            );
        }
    }
    @Test
    void shouldThrowExceptionWhenAssignmentIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.move(
                        null,
                        AssignmentStatus.ASSIGNED,
                        AssignmentStatus.SUBMITTED
                )
        );
    }
}