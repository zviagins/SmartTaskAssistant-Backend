package com.smarttaskassistant.ai.model;

import java.time.LocalDateTime;

public record ParsedCommand(
        CommandType commandType,
        String title,
        String description,
        LocalDateTime dueTime,
        String location,
        LocalDateTime reminder,
        Integer severity,
        String status,
        TaskFilters filters,
        Double confidence,
        String reasoning
) {
    
    public record TaskFilters(
            String status,
            Integer severity
    ) {}
}
