package com.smarttaskassistant.task.controller;

import com.smarttaskassistant.task.model.TaskCreateRequest;
import com.smarttaskassistant.task.model.TaskResponse;
import com.smarttaskassistant.task.model.TaskStatus;
import com.smarttaskassistant.task.model.TaskUpdateRequest;
import com.smarttaskassistant.task.service.TaskService;
import com.smarttaskassistant.task.util.SortUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public TaskResponse createTask(@Valid @RequestBody TaskCreateRequest request) {
        return taskService.createTask(request);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request){
        return taskService.updateTask(id, request);
    }

    @GetMapping
    public List<TaskResponse> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Integer severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueAfter,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        return taskService.getTasks(status, severity, dueBefore, dueAfter, SortUtils.buildSort(sort));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

}
