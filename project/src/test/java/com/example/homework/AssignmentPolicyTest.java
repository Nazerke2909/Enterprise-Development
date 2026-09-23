package com.example.homework;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.homework.domain.ApprovedIsFinalRule;
import com.example.homework.domain.AssignmentId;
import com.example.homework.domain.AssignmentPolicy;
import com.example.homework.domain.AssignmentStatus;
import com.example.homework.domain.RuleChain;
import com.example.homework.domain.TransitionRule;

class AssignmentPolicyTest {

        private final AssignmentPolicy policy = new AssignmentPolicy(
            new RuleChain(List.of(new ApprovedIsFinalRule(), new TransitionRule())));
    private final AssignmentId id = new AssignmentId("HW-1");

    @ParameterizedTest
    @CsvSource({
            "ASSIGNED, SUBMITTED, true",
            "SUBMITTED, CHECKED, true",
            "CHECKED, APPROVED, true",
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
                    IllegalStateException.class,
                    () -> policy.move(id, from, to)
            );
        }
    }

    @Test
    void shouldThrowExceptionWhenAssignmentIdIsNull() {
        assertThrows(
                IllegalStateException.class,
                () -> policy.move(
                        null,
                        AssignmentStatus.ASSIGNED,
                        AssignmentStatus.SUBMITTED
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenAssignmentIdIsBlank() {
        assertThrows(
                IllegalStateException.class,
                () -> new AssignmentId("")
        );
    }
}