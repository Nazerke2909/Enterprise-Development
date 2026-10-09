package com.example.homework.domain;

public class DuplicateAssignment extends RuntimeException {
    public DuplicateAssignment(String businessKey) {
        super("duplicate assignment: " + businessKey);
    }
}