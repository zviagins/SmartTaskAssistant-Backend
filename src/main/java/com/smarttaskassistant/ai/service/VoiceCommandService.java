package com.smarttaskassistant.ai.service;

import com.smarttaskassistant.ai.model.*;
import com.smarttaskassistant.ai.util.JsonUtils;
import com.smarttaskassistant.auth.util.SecurityUtils;
import com.smarttaskassistant.notification.model.Notification;
import com.smarttaskassistant.notification.model.NotificationRequest;
import com.smarttaskassistant.notification.service.NotificationService;
import com.smarttaskassistant.task.model.*;
import com.smarttaskassistant.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceCommandService {

    private final TaskService taskService;
    private final ChatClient chatClient;
    private final NotificationService notificationService;

    private static final String SYSTEM_PROMPT = """
            You are an AI assistant that helps users manage their tasks through voice commands. 
            Your job is to parse natural language commands and convert them into structured JSON commands.
            
            Available task operations:
            1. CREATE_TASK - Create a new task
            2. UPDATE_TASK - Update an existing task by title
            3. DELETE_TASK - Delete a task by title
            4. LIST_TASKS - List tasks with optional filters
            5. GET_TASK - Get a specific task by title
            6. MARK_COMPLETE - Mark a task as completed by title
            7. SET_REMINDER - Set a reminder for a task by title
            
            Task properties:
            - title: String (required for CREATE_TASK, UPDATE_TASK, DELETE_TASK, GET_TASK, MARK_COMPLETE, SET_REMINDER)
            - description: String (optional)
            - due_time: ISO 8601 datetime string (optional)
            - location: String (optional)
            - reminder: ISO 8601 datetime string (optional)
            - severity: Integer 1-10 (optional, default 5)
            - status: String: TODO, IN_PROGRESS, DONE, CANCELLED (default TODO)
            
            Response format (JSON):
            {
                "commandType": "CREATE_TASK|UPDATE_TASK|DELETE_TASK|LIST_TASKS|GET_TASK|MARK_COMPLETE|UNKNOWN",
                "taskId": 123, // only for operations that need it
                "title": "Task title",
                "description": "Task description",
                "dueTime": "2024-01-15T10:00:00",
                "location": "Office",
                "reminder": "2024-01-15T09:30:00",
                "severity": 7,
                "status": "TODO",
                "filters": {
                    "status": "TODO",
                    "severity": 5
                },
                "confidence": 0.95,
                "reasoning": "User wants to create a new task for tomorrow's meeting"
            }
            
            Guidelines:
            - Be confident in your parsing (confidence 0.8+ for clear commands)
            - Extract dates and times from natural language (tomorrow, next week, 3pm, etc.)
            - For LIST_TASKS, use filters to narrow down results
            - If command is unclear, set command_type to UNKNOWN and explain in reasoning
            - Always provide reasoning for your decision
            - Use ISO 8601 format for all datetime fields
            """;

    private static final String SYSTEM_PROMPT_FOR_DAILY_SUMMARY = """
            You are a personal assistant that creates daily task summaries. Create a natural, conversational message that helps the user understand their day ahead.
            
            Guidelines:
            - Use the user's name naturally
            - Be informative but not overly enthusiastic
            - Mention scheduled tasks with their times in a conversational way
            - Highlight the most important unscheduled tasks by priority
            - Keep it concise and practical
            - Use natural speech patterns that work well for text-to-speech
            - Avoid line breaks and formatting - write as continuous text
            - If there are no tasks, mention it briefly
            
            Write as a single flowing sentence or short paragraph that sounds natural when spoken.
            """;

    /**
     * Process voice command asynchronously with notification support
     * @param voiceText The voice command text to process
     * @param userId The user ID (captured from SecurityContext before async execution)
     */
    @Async("voiceCommandExecutor")
    public void processVoiceCommandAsync(String voiceText, Long userId) {
        if (userId == null) {
            log.error("User ID is null. Failed to process voice command {}", voiceText);
            return;
        }

        try {
            String commandId = generateCommandId();

            log.info("Starting async processing of voice command [{}]: {}", commandId, voiceText);

            ParsedCommand parsedCommand = parseVoiceCommand(voiceText);

            if (parsedCommand.confidence() < 0.7) {
                notificationService.handleNotification(NotificationRequest.error("failed to parse command",
                        "Failed to parse command: " + parsedCommand.reasoning()), userId);
            }

            // Execute the parsed command
            NotificationRequest notificationRequest = executeCommand(parsedCommand, userId);

            notificationService.handleNotification(notificationRequest, userId);

            log.info("Completed async processing of voice command [{}]: {}",
                    commandId, notificationRequest.notification());

        } catch (Exception e) {
            log.error("Error processing voice command: {}", voiceText, e);
            notificationService.handleNotification(NotificationRequest.error("Failed to parse request due to exception",
                    "Sorry, I encountered an error processing your request. Please try again."), userId);
        }
    }

    /**
     * Parse voice command with timeout protection
     * @param voiceText The voice command text
     * @param timeout The timeout value
     * @param timeUnit The timeout unit
     * @return ParsedCommand
     */
    private ParsedCommand parseVoiceCommandWithTimeout(String voiceText, long timeout, TimeUnit timeUnit) {
        try {
            CompletableFuture<ParsedCommand> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return parseVoiceCommand(voiceText);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse voice command", e);
                }
            });
            
            return future.get(timeout, timeUnit);
            
        } catch (Exception e) {
            log.error("Timeout or error parsing voice command: {}", voiceText, e);
            throw new RuntimeException("Voice command parsing timed out or failed", e);
        }
    }

    /**
     * Generate a unique command ID for tracking
     * @return A unique command identifier
     */
    private String generateCommandId() {
        return "CMD-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    private ParsedCommand parseVoiceCommand(String voiceText){
        try {
            String userPrompt = "\n\nVoice command text: " + voiceText + "\n\n" +
                "Respond with only a valid JSON object matching the ParsedCommand structure.";

            String aiResponse = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();
            
            log.debug("AI response: {}", aiResponse);

            return JsonUtils.fromJson(aiResponse, ParsedCommand.class);

        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to parse voice command", e);
        }
    }

    private NotificationRequest executeCommand(ParsedCommand command, Long userId) {
        return switch (command.commandType()) {
            case CREATE_TASK -> executeCreateTask(command, userId);
            case UPDATE_TASK -> executeUpdateTask(command, userId);
            case DELETE_TASK -> executeDeleteTask(command, userId);
            case MARK_COMPLETE -> executeMarkComplete(command, userId);
            case UNKNOWN -> NotificationRequest.error("Unknown command", command.reasoning());
        };
    }

    private NotificationRequest executeCreateTask(ParsedCommand command, Long userId) {
        if (command.title() == null || command.title().trim().isEmpty()) {
            return NotificationRequest.error("Missing title", "Task title is required to create a task.");
        }

        TaskCreateRequest request = new TaskCreateRequest(
            command.title(),
            command.description(),
            command.dueTime(),
            command.location(),
            command.reminder(),
            command.severity() != null ? command.severity() : 5,
            command.status() != null ? command.status() : "TODO"
        );

        TaskResponse task = taskService.createTask(request, userId);
        return NotificationRequest.success(
                "task created",
            "Task created successfully: " + task.title()
        );
    }

    private NotificationRequest executeUpdateTask(ParsedCommand command, Long userId) {
        if (command.title() == null || command.title().trim().isEmpty()) {
            return NotificationRequest.error("Missing title", "Task title is required to update a task.");
        }

        TaskUpdateRequest request = new TaskUpdateRequest(
            command.title(),
            command.description(),
            command.dueTime(),
            command.location(),
            command.reminder(),
            command.severity(),
            command.status()
        );

        TaskResponse task = taskService.updateTask(command.title(), request, userId);
        return NotificationRequest.success(
                "task updated",
            "Task updated successfully: " + task.title()
        );
    }

    private NotificationRequest executeDeleteTask(ParsedCommand command, Long userId) {
        if (command.title() == null || command.title().trim().isEmpty()) {
            return NotificationRequest.error("Missing title", "Task title is required to delete a task.");
        }

        try {
            taskService.deleteTaskByTitle(command.title(), userId);
            return NotificationRequest.success(
                    "task deleted",
                "Task deleted successfully: " + command.title());
        } catch (RuntimeException e) {
            return NotificationRequest.error("Failed to delete task", "Failed to delete task: " + e.getMessage());
        }
    }

    /*private VoiceCommandResponse executeListTasks(ParsedCommand command) {
        TaskStatus status = null;
        if (command.filters() != null && command.filters().status() != null) {
            try {
                status = TaskStatus.valueOf(command.filters().status().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", command.filters().status());
            }
        }

        return listTasks(status);
    }

    private VoiceCommandResponse executeGetTask(ParsedCommand command) {
        if (command.title() == null || command.title().trim().isEmpty()) {
            return VoiceCommandResponse.error("Task title is required to get a specific task.");
        }

        try {
            TaskResponse task = taskService.getTaskByTitle(command.title());
            return VoiceCommandResponse.success(
                "Found task: " + task.title(),
                CommandType.GET_TASK,
                task
            );
        } catch (RuntimeException e) {
            return VoiceCommandResponse.error("Failed to get task: " + e.getMessage());
        }
    }*/

    private NotificationRequest executeMarkComplete(ParsedCommand command, Long userId) {
        if (command.title() == null || command.title().trim().isEmpty()) {
            return NotificationRequest.error("Missing title", "Task title is required to update a task.");
        }

        TaskUpdateRequest request = new TaskUpdateRequest(
                command.title(), null, null, null, null, null, "DONE"
        );

        TaskResponse task = taskService.updateTask(command.title(), request, userId);
        return NotificationRequest.success("task updated",
            "Task marked as completed: " + task.title());
    }

    public VoiceCommandResponse listTasks(TaskStatus status, Long userId) {
        try {
            log.info("Generating daily summary");
            
            // Get current user's name
            String userName = SecurityUtils.getCurrentUserName()
                    .orElse("User");
            
            // Get today's date range
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            
            // Get all tasks by status
            List<TaskResponse> allTodoTasks = taskService.getTasks(
                status == null ? TaskStatus.TODO : status,
                null, // any severity
                null, // any due date
                null,
                org.springframework.data.domain.Sort.by("severity").descending().and(
                    org.springframework.data.domain.Sort.by("createdAt").descending()
                ), userId
            );
            
            // Separate scheduled and unscheduled tasks
            List<TaskResponse> scheduledTasks = allTodoTasks.stream()
                .filter(task -> task.dueTime() != null && 
                    task.dueTime().isAfter(startOfDay) && 
                    task.dueTime().isBefore(endOfDay))
                .sorted(Comparator.comparing(TaskResponse::dueTime))
                .toList();
                
            List<TaskResponse> unscheduledTasks = allTodoTasks.stream()
                .filter(task -> task.dueTime() == null)
                .sorted((t1, t2) -> Integer.compare(t2.severity(), t1.severity())) // Higher severity first
                .toList();
            
            // Generate personalized message using AI
            String dailySummary = generateDailySummary(userName, scheduledTasks, unscheduledTasks);
            
            return VoiceCommandResponse.success(
                dailySummary,
                allTodoTasks
            );
            
        } catch (Exception e) {
            log.error("Error generating daily task summary", e);
            return VoiceCommandResponse.error("Sorry, I couldn't generate your daily summary. Please try again.");
        }
    }
    
    private String generateDailySummary(String userName, List<TaskResponse> scheduledTasks, List<TaskResponse> unscheduledTasks) {
            
        StringBuilder taskInfo = new StringBuilder();
        taskInfo.append("User: ").append(userName).append("\n\n");
        
        if (!scheduledTasks.isEmpty()) {
            taskInfo.append("Scheduled tasks for today:\n");
            for (TaskResponse task : scheduledTasks) {
                String timeStr = task.dueTime().toLocalTime().toString();
                taskInfo.append("- ").append(task.title());
                if (task.description() != null && !task.description().trim().isEmpty()) {
                    taskInfo.append(" (").append(task.description()).append(")");
                }
                taskInfo.append(" at ").append(timeStr).append("\n");
            }
        }
        
        if (!unscheduledTasks.isEmpty()) {
            taskInfo.append("\nUnscheduled tasks (by priority):\n");
            for (TaskResponse task : unscheduledTasks) {
                taskInfo.append("- ").append(task.title());
                if (task.description() != null && !task.description().trim().isEmpty()) {
                    taskInfo.append(" (").append(task.description()).append(")");
                }
                taskInfo.append(" [Priority: ").append(task.severity()).append("]\n");
            }
        }
        
        if (scheduledTasks.isEmpty() && unscheduledTasks.isEmpty()) {
            taskInfo.append("No tasks found for today.");
        }
        
        String userPrompt = taskInfo.toString();
        
        return chatClient.prompt()
            .system(SYSTEM_PROMPT_FOR_DAILY_SUMMARY)
            .user(userPrompt)
            .call()
            .content();
    }
}
