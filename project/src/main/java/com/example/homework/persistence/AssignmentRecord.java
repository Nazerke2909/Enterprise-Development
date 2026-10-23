package com.example.homework.persistence;

import com.example.homework.domain.AssignmentStatus;

import java.util.UUID;

/** Immutable read model returned to callers so JPA entities do not leak out of the service. */
public record AssignmentRecord(
        UUID id,
        String businessKey,
        AssignmentStatus status,
        String title) {

    public static AssignmentRecord from(AssignmentEntity entity) {
        return new AssignmentRecord(
                entity.getId(),
                entity.getBusinessKey(),
                entity.getStatus(),
                entity.getTitle());
    }
}
