package com.example.homework.domain;

public class AssignmentNotFound extends RuntimeException {
    public AssignmentNotFound(String id) {
        super("assignment not found: " + id);
    }
}
