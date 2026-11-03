package com.example.homework.handler;

import com.example.homework.persistence.AssignmentRecord;

import java.util.UUID;

public record AssignmentResponse(UUID id, String businessKey, String status, String title) {

    public static AssignmentResponse from(AssignmentRecord record) {
        return new AssignmentResponse(
                record.id(), record.businessKey(), record.status().name(), record.title());
    }
}
