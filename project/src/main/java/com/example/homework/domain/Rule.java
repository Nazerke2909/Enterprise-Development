package com.example.homework.domain;

public interface Rule {
    void check(AssignmentStatus from, AssignmentStatus to);
}