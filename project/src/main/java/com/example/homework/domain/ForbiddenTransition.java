package com.example.homework.domain;

public class ForbiddenTransition extends IllegalStateException {
    public ForbiddenTransition(String message) {
        super(message);
    }
    
}
