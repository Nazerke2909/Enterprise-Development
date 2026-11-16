package com.example.homework;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public final class TestTokens {
    public static final String SECRET = "test-secret-test-secret-test-secret-0123";

    private TestTokens() {
    }

    public static RequestPostProcessor teacher() {
        return jwt().jwt(j -> j.subject("tom")).authorities(new SimpleGrantedAuthority("ROLE_TEACHER"));
    }

    public static RequestPostProcessor student() {
        return jwt().jwt(j -> j.subject("sam")).authorities(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }
}