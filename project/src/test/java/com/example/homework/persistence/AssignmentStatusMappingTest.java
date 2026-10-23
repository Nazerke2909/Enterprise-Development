package com.example.homework.persistence;

import com.example.homework.domain.AssignmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssignmentStatusMappingTest {

    private final AssignmentStatusConverter converter = new AssignmentStatusConverter();

    @ParameterizedTest
    @EnumSource(AssignmentStatus.class)
    void everyStatusSurvivesTheRoundTrip(AssignmentStatus status) {
        String column = converter.convertToDatabaseColumn(status);
        assertEquals(status, converter.convertToEntityAttribute(column));
    }

    @Test
    void nullStatusIsStoredAsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void unknownTextThrows() {
        assertThrows(IllegalStateException.class, () ->
                converter.convertToEntityAttribute("ARCHIVED"));
    }

    @Test
    void nullTextThrows() {
        assertThrows(IllegalStateException.class, () ->
                converter.convertToEntityAttribute(null));
    }
}