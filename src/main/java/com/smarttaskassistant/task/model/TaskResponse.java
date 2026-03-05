package com.smarttaskassistant.task.model;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDateTime dueTime,
        String location,
        LocalDateTime reminder,
        int severity,
        String status,
        boolean scheduled
) {
    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueTime(),
                task.getLocation(),
                task.getReminder(),
                task.getSeverity(),
                task.getStatus().toString(),
                task.isScheduled()
        );
    }
}


