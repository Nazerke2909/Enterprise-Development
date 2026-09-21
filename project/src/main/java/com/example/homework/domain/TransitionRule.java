package com.example.homework.domain;

import java.util.Map;
import java.util.Set;

public class TransitionRule implements Rule {

    private static final Map<AssignmentStatus, Set<AssignmentStatus>> ALLOWED = Map.of(
            AssignmentStatus.ASSIGNED, Set.of(AssignmentStatus.SUBMITTED),
            AssignmentStatus.SUBMITTED, Set.of(AssignmentStatus.CHECKED),
            AssignmentStatus.CHECKED, Set.of(AssignmentStatus.APPROVED)
    );

    @Override
    public void check(AssignmentStatus from, AssignmentStatus to) {
        if (from == null || to == null) {
            throw new IllegalStateException("Assignment status cannot be null");
        }
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw new IllegalStateException("Forbidden transition: " + from + " -> " + to);
        }
    }
}