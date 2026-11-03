package com.example.homework.domain;

public record AuthenticatedUser(String username, Role role) {
    public AuthenticatedUser {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Username cannot be null or blank");
        }
        if (role == null) {
            throw new IllegalStateException("Role cannot be null");
        }
    }
}
