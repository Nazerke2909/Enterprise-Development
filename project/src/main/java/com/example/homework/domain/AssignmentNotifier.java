package com.example.homework.domain;

public interface AssignmentNotifier {
    void statusChanged(AssignmentId id, AssignmentStatus newStatus);
}