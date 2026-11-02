package com.example.homework.persistence;

import com.example.homework.domain.AssignmentStatus;

import java.util.UUID;

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
