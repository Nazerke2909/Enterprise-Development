package com.example.homework.persistence;

import com.example.homework.domain.AssignmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssignmentStatusMappingTest {
    @ParameterizedTest
    @EnumSource(AssignmentStatus.class)
    void everyStatusSurvivesTheRoundTrip(AssignmentStatus status) {
        assertEquals(status, AssignmentJdbc.toStatus(status.name()));
    }

    @Test
    void unknownTextThrows() {
        assertThrows(IllegalStateException.class, () -> AssignmentJdbc.toStatus("ARCHIVED"));
    }

    @Test
    void nullTextThrows() {
        assertThrows(IllegalStateException.class, () -> AssignmentJdbc.toStatus(null));
    }
}
