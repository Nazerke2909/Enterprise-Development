package com.example.homework.config;

import com.example.homework.TestTokens;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceTest {
    private final JwtProperties props = new JwtProperties(TestTokens.SECRET, "homework-control", Duration.ofMinutes(15));
    private final SecurityConfig config = new SecurityConfig();
    private final TokenService tokens = new TokenService(config.jwtEncoder(props), props);

    @Test
    void issuedTokenIsReadBackWithSubjectRolesAndFutureExpiry() {
        String token = tokens.issue("alice", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")));

        Jwt jwt = config.jwtDecoder(props).decode(token);

        assertEquals("alice", jwt.getSubject());
        assertEquals(List.of("TEACHER"), jwt.getClaimAsStringList("roles"));
        assertEquals("homework-control", jwt.getClaimAsString("iss"));
        assertTrue(jwt.getExpiresAt().isAfter(Instant.now()));
        assertEquals(900, tokens.expiresInSeconds());
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        JwtProperties helpdesk = new JwtProperties(TestTokens.SECRET, "helpdesk", Duration.ofMinutes(15));
        String foreign = new TokenService(config.jwtEncoder(helpdesk), helpdesk)
                .issue("alice", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")));

        assertThrows(JwtException.class, () -> config.jwtDecoder(props).decode(foreign));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtProperties other = new JwtProperties("another-secret-another-secret-0000000", "homework-control",
                Duration.ofMinutes(15));
        String forged = new TokenService(config.jwtEncoder(other), other)
                .issue("alice", List.of(new SimpleGrantedAuthority("ROLE_TEACHER")));

        assertThrows(JwtException.class, () -> config.jwtDecoder(props).decode(forged));
    }

    @Test
    void shortSecretFailsAtStartup() {
        assertThrows(IllegalStateException.class,
                () -> new JwtProperties("secret", "homework-control", Duration.ofMinutes(15)));
    }
}