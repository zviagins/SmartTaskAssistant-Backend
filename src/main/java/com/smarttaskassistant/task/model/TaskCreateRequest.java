package com.smarttaskassistant.task.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record TaskCreateRequest(
        @NotBlank String title,
        String description,
        LocalDateTime dueTime,
        String location,
        LocalDateTime reminder,
        @Min(1) @Max(10) int severity,
        String status
) {}

