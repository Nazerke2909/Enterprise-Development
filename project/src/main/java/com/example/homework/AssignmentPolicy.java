package com.example.homework;

public class AssignmentPolicy {

    public AssignmentStatus move(
            AssignmentId id,
            AssignmentStatus from,
            AssignmentStatus to
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Assignment id cannot be null");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("Assignment status cannot be null");
        }
        if (from == AssignmentStatus.ASSIGNED
                && to == AssignmentStatus.SUBMITTED) {
            return to;
        }
        if (from == AssignmentStatus.SUBMITTED
                && to == AssignmentStatus.CHECKED) {
            return to;
        }
        throw new IllegalArgumentException(
                "Forbidden transition: " + from + " -> " + to
        );
    }
}