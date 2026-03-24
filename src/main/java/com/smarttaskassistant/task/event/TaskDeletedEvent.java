package com.smarttaskassistant.task.event;

public record TaskDeletedEvent(Long userId, String title, Long taskId) {
}
