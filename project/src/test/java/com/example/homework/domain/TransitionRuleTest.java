package com.example.homework.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TransitionRuleTest {
    private final TransitionRule rule = new TransitionRule();

    @ParameterizedTest
    @CsvSource({
            "ASSIGNED, SUBMITTED, true",
            "SUBMITTED, CHECKED, true",
            "CHECKED, APPROVED, true",
            "CHECKED, ASSIGNED, false",
            "ASSIGNED, CHECKED, false",
            "APPROVED, SUBMITTED, false"
    })
    void checkTransition(AssignmentStatus from, AssignmentStatus to, boolean allowed) {
        if (allowed) {
            assertDoesNotThrow(() -> rule.check(from, to));
        } else {
            assertThrows(IllegalStateException.class, () -> rule.check(from, to));
        }
    }
}