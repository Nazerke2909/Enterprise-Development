package com.example.homework.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

public final class ErrorResponses {
    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    private ErrorResponses() {
    }

    public static String correlationId(HttpServletRequest request) {
        String header = request.getHeader(CORRELATION_HEADER);
        return (header == null || header.isBlank()) ? UUID.randomUUID().toString() : header;
    }

    public static void write(HttpServletRequest request, HttpServletResponse response,
                             int status, String code, String message) throws IOException {
        String correlationId = correlationId(request);
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(CORRELATION_HEADER, correlationId);
        response.getWriter().write("{\"code\":\"" + escape(code)
                + "\",\"message\":\"" + escape(message)
                + "\",\"correlationId\":\"" + escape(correlationId) + "\"}");
    }

    private static String escape(String value) {
        StringBuilder out = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (c == '"' || c == '\\') {
                out.append('\\').append(c);
            } else if (c >= 0x20) {
                out.append(c);
            }
        }
        return out.toString();
    }
}