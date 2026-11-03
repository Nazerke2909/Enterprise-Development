package com.example.homework.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserSeeder implements CommandLineRunner {
    private final UserService users;
    private final AuthProperties properties;

    public UserSeeder(UserService users, AuthProperties properties) {
        this.users = users;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        for (AuthProperties.SeedUser seed : properties.seed()) {
            if (!users.exists(seed.username())) {
                users.create(seed.username(), seed.password(), seed.role());
            }
        }
    }
}
