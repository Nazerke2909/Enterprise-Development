package com.example.homework.config;

import com.example.homework.domain.Role;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(List<SeedUser> seed) {
    public AuthProperties {
        seed = seed == null ? List.of() : List.copyOf(seed);
    }

    public record SeedUser(String username, String password, Role role) {
    }
}
