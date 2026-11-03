package com.example.homework.config;

import com.example.homework.domain.AuthenticatedUser;
import com.example.homework.domain.AuthenticationFailed;
import com.example.homework.domain.PasswordHasher;
import com.example.homework.domain.Role;
import com.example.homework.domain.UserAlreadyExists;
import com.example.homework.persistence.UserEntity;
import com.example.homework.persistence.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_.-]{3,30}");
    private static final int MIN_PASSWORD = 6;

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public AuthenticatedUser registerStudent(String username, String password) {
        return create(username, password, Role.STUDENT);
    }

    @Transactional
    public AuthenticatedUser create(String username, String password, Role role) {
        String name = username == null ? "" : username.trim();
        if (!USERNAME.matcher(name).matches()) {
            throw new IllegalStateException(
                    "Username must be 3-30 characters: letters, digits, '_', '.', '-'");
        }
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new IllegalStateException("Password must be at least " + MIN_PASSWORD + " characters");
        }
        if (role == null) {
            throw new IllegalStateException("Role cannot be null");
        }
        if (users.existsByUsername(name)) {
            throw new UserAlreadyExists(name);
        }
        try {
            users.saveAndFlush(new UserEntity(UUID.randomUUID(), name, PasswordHasher.hash(password), role));
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExists(name);
        }
        return new AuthenticatedUser(name, role);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser login(String username, String password) {
        String name = username == null ? "" : username.trim();
        UserEntity user = users.findByUsername(name).orElseThrow(AuthenticationFailed::new);
        if (!PasswordHasher.matches(password, user.getPasswordHash())) {
            throw new AuthenticationFailed();
        }
        return new AuthenticatedUser(user.getUsername(), user.getRole());
    }

    @Transactional(readOnly = true)
    public boolean exists(String username) {
        return users.existsByUsername(username);
    }
}
