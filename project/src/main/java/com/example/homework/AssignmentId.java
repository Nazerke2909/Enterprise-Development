package com.example.homework;

public record AssignmentId(String value) {
    public AssignmentId {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Assignment id cannot be null or blank");
        }
    }
}