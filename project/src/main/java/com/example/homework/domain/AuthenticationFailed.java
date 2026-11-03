package com.example.homework.domain;

public class AuthenticationFailed extends RuntimeException {
    public AuthenticationFailed() {
        super("invalid username or password");
    }
}
