package com.example.homework.domain;

public class UserAlreadyExists extends RuntimeException {
    public UserAlreadyExists(String username) {
        super("user already exists: " + username);
    }
}
