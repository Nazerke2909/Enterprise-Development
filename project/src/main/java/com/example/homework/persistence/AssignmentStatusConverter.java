package com.example.homework.persistence;

import com.example.homework.domain.AssignmentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores {@link AssignmentStatus} as plain text and fails loudly on unknown database values.
 */
@Converter
public class AssignmentStatusConverter implements AttributeConverter<AssignmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AssignmentStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public AssignmentStatus convertToEntityAttribute(String text) {
        return toStatus(text);
    }

    static AssignmentStatus toStatus(String text) {
        try {
            return AssignmentStatus.valueOf(text);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalStateException("Unknown assignment status in database: " + text, ex);
        }
    }
}
