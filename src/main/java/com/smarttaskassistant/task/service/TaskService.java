package com.smarttaskassistant.task.service;

import com.smarttaskassistant.auth.util.SecurityUtils;
import com.smarttaskassistant.task.model.*;
import com.smarttaskassistant.task.repository.TaskRepository;
import com.smarttaskassistant.task.spec.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;

    public TaskResponse createTask(TaskCreateRequest task) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Failed to get user from request"));
        TaskStatus status;

        try {
            status = TaskStatus.valueOf(task.status());
        } catch (NullPointerException | IllegalArgumentException e) {
            status = TaskStatus.TODO;
        }
        Task newTask = repository.save(Task.builder()
                        .title(task.title())
                        .description(task.description())
                        .dueTime(task.dueTime())
                        .location(task.location())
                        .reminder(task.reminder())
                        .severity(task.severity())
                        .status(status)
                        .userId(userId)
                .build());

        return TaskResponse.fromEntity(newTask);
    }

    public TaskResponse updateTask(String title, TaskUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("Failed to get user from request"));

        Task task = repository.findByTitleContainingAndUserIdOrderByUpdatedAtDesc(title, userId)
                .orElseThrow(() -> new RuntimeException("Task not found with title containing: " + title));

        return updateTask(task, request);
    }

    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("Failed to get user from request"));

        Task task = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        return updateTask(task, request);
    }

    private TaskResponse updateTask(Task task, TaskUpdateRequest request) {

        if (request.title() != null && !request.title().isBlank()) {
            task.setTitle(request.title());
        }
        if (request.description() != null && !request.description().isBlank()) {
            task.setDescription(request.description());
        }
        if (request.dueTime() != null) {
            task.setDueTime(request.dueTime());
        }
        if (request.location() != null) {
            task.setLocation(request.location());
        }
        if (request.reminder() != null) {
            task.setReminder(request.reminder());
        }
        if (request.severity() != null) {
            task.setSeverity(request.severity());
        }
        if (request.status() != null) {
            task.setStatus(TaskStatus.valueOf(request.status()));
        }

        Task saved = repository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    public List<TaskResponse> getTasks(TaskStatus status,
                                       Integer severity,
                                       LocalDateTime dueBefore,
                                       LocalDateTime dueAfter,
                                       Sort sort) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("No user found"));

        Specification<Task> spec = Specification.allOf(
                TaskSpecification.hasUserId(userId),
                TaskSpecification.hasStatus(status),
                TaskSpecification.hasSeverity(severity),
                TaskSpecification.dueBefore(dueBefore),
                TaskSpecification.dueAfter(dueAfter)
        );

        return repository.findAll(spec, sort).stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }

    public void deleteTask(Long id) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("No user found"));
        repository.deleteByIdAndUserId(id, userId);
    }

    public TaskResponse getTaskByTitle(String title) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("Failed to get user from request"));

        Task task = repository.findByTitleContainingAndUserIdOrderByUpdatedAtDesc(title, userId)
                .orElseThrow(() -> new RuntimeException("Task not found with title containing: " + title));

        return TaskResponse.fromEntity(task);
    }

    public void deleteTaskByTitle(String title) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("Failed to get user from request"));

        Task task = repository.findByTitleContainingAndUserIdOrderByUpdatedAtDesc(title, userId)
                .orElseThrow(() -> new RuntimeException("Task not found with title containing: " + title));

        repository.deleteByIdAndUserId(task.getId(), userId);
    }
}

