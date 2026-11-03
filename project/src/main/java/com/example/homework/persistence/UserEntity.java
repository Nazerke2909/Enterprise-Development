package com.example.homework.persistence;

import com.example.homework.domain.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class UserEntity {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String username;
    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;
    @Convert(converter = RoleConverter.class)
    @Column(nullable = false, columnDefinition = "text")
    private Role role;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected UserEntity() {
    }

    public UserEntity(UUID id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}
