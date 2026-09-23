package com.example.homework.domain;

import java.util.List;

public class AssignmentPolicy {
    private final RuleChain rules;

    public AssignmentPolicy() {
        this(new RuleChain(List.of(new ApprovedIsFinalRule(), new TransitionRule())));
    }

    public AssignmentPolicy(RuleChain rules) {
        this.rules = rules;
    }

    public AssignmentStatus move(
            AssignmentId id,
            AssignmentStatus from,
            AssignmentStatus to
    ) {
        if (id == null) {
            throw new IllegalStateException("Assignment id cannot be null");
        }
        if (from == null || to == null) {
            throw new IllegalStateException("Assignment status cannot be null");
        }
        rules.check(from, to);
        return to;
    }
}