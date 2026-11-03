package com.example.homework.handler;

import com.example.homework.domain.AssignmentNotFound;
import com.example.homework.domain.DuplicateAssignment;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.UUID;

@RestControllerAdvice
public class ApiExceptionHandler {
    static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> onValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .findFirst()
                .map(e -> humanize(e.getField()) + " " + e.getDefaultMessage())
                .orElse("request is not valid");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION", message, correlationId(request));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> onBadPathValue(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = UUID.class.equals(ex.getRequiredType())
                ? ex.getName() + " must be a UUID"
                : ex.getName() + " is invalid";
        return build(HttpStatus.BAD_REQUEST, "VALIDATION", message, correlationId(request));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> onUnreadableBody(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION", "body must be JSON", correlationId(request));
    }

    @ExceptionHandler(AssignmentNotFound.class)
    public ResponseEntity<ApiError> onNotFound(AssignmentNotFound ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), correlationId(request));
    }

    @ExceptionHandler(DuplicateAssignment.class)
    public ResponseEntity<ApiError> onDuplicate(DuplicateAssignment ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "DUPLICATE", ex.getMessage(), correlationId(request));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> onUnsupportedMedia(HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED",
                "content type is not supported", correlationId(request));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> onUnknownPath(HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "resource not found", correlationId(request));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> onWrongMethod(HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "method is not allowed", correlationId(request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onUnexpected(Exception ex, HttpServletRequest request) {
        String correlationId = correlationId(request);
        log.error("Unexpected error, correlationId={}", correlationId, ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL", "unexpected error", correlationId);
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String code, String message, String correlationId) {
        return ResponseEntity.status(status).body(new ApiError(code, message, correlationId));
    }

    private static String correlationId(HttpServletRequest request) {
        String header = request.getHeader(CORRELATION_HEADER);
        return (header == null || header.isBlank()) ? UUID.randomUUID().toString() : header;
    }

    private static String humanize(String field) {
        return field.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
    }
}
