package com.smarttaskassistant.task.model;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TaskUpdateRequest(
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,
        String description,
        LocalDateTime dueTime,
        String location,
        LocalDateTime reminder,
        Integer severity,  // must be nullable (not int), so "not provided" is distinguishable
        String status
) {}


