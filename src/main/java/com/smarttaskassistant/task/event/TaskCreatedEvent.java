package com.smarttaskassistant.task.event;

import com.smarttaskassistant.task.model.Task;

public record TaskCreatedEvent(Long userId, Task task) {
}
