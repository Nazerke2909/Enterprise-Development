package com.example.homework.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ApprovedIsFinalRuleTest {
    private final ApprovedIsFinalRule rule = new ApprovedIsFinalRule();

    @ParameterizedTest
    @EnumSource(AssignmentStatus.class)
    void nothingLeavesApproved(AssignmentStatus to) {
        assertThrows(IllegalStateException.class, () -> rule.check(AssignmentStatus.APPROVED, to));
    }

    @ParameterizedTest
    @EnumSource(value = AssignmentStatus.class, names = "APPROVED", mode = EnumSource.Mode.EXCLUDE)
    void otherStatusesPass(AssignmentStatus from) {
        assertDoesNotThrow(() -> rule.check(from, AssignmentStatus.SUBMITTED));
    }
}