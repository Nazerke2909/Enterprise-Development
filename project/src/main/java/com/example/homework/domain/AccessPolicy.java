package com.example.homework.domain;

public final class AccessPolicy {

    private AccessPolicy() {
    }

    public static boolean canCreate(Role role) {
        return role == Role.TEACHER;
    }

    public static boolean canMove(Role role, AssignmentStatus to) {
        return role != null && switch (role) {
            case STUDENT -> to == AssignmentStatus.SUBMITTED;
            case TEACHER -> to == AssignmentStatus.CHECKED || to == AssignmentStatus.APPROVED;
        };
    }

    public static void requireCreate(AuthenticatedUser user) {
        if (user == null || !canCreate(user.role())) {
            throw new AccessDenied("only a teacher can create assignments");
        }
    }

    public static void requireMove(AuthenticatedUser user, AssignmentStatus to) {
        if (user == null || !canMove(user.role(), to)) {
            throw new AccessDenied(user == null
                    ? "user is required"
                    : user.role() + " cannot move an assignment to " + to);
        }
    }
}
