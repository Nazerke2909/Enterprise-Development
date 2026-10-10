package com.example.homework.domain;

public record AssignmentKey(String value) {
    public AssignmentKey {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Assignment business key cannot be null or blank");
        }
    }
}