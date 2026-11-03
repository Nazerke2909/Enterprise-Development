package com.example.homework.persistence;

import com.example.homework.domain.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        return role == null ? null : role.name();
    }

    @Override
    public Role convertToEntityAttribute(String text) {
        try {
            return Role.valueOf(text);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalStateException("Unknown role in database: " + text, ex);
        }
    }
}
