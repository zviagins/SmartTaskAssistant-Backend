package com.smarttaskassistant.task.event;

import com.smarttaskassistant.task.model.Task;

public record TaskUpdatedEvent(Long userId, Task task) {
}
