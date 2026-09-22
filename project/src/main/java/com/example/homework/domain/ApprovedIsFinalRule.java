package com.example.homework.domain;

public class ApprovedIsFinalRule implements Rule {
    @Override
    public void check(AssignmentStatus from, AssignmentStatus to) {
        if (from == AssignmentStatus.APPROVED) {
            throw new IllegalStateException("Approved assignment is final: " + from + " -> " + to);
        }
    }
}