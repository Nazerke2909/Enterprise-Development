package com.example.homework.handler;

public record ApiError(String code, String message, String correlationId) {
}
