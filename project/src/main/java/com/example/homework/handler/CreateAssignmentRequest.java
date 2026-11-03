package com.example.homework.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAssignmentRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "HW-1001")
        @NotBlank(message = "must not be blank")
        String businessKey,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 120, example = "Essay")
        @NotBlank(message = "must not be blank")
        @Size(max = 120, message = "size must be between 0 and 120")
        String title) {
}
