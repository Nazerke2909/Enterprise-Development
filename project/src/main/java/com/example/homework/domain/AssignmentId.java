package com.example.homework.domain;

import java.util.UUID;

public record AssignmentId(String value) {
    public AssignmentId {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Assignment id cannot be null or blank");
        }
    }

    public static AssignmentId newId() {
        return new AssignmentId(UUID.randomUUID().toString());
    }
    public UUID uuid() {
        return UUID.fromString(value);
    }
}