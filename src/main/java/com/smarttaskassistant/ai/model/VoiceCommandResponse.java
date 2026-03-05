package com.smarttaskassistant.ai.model;

import com.smarttaskassistant.task.model.TaskResponse;

import java.util.List;

public record VoiceCommandResponse(
        String message,
        TaskResponse task,
        List<TaskResponse> tasks,
        boolean success,
        String errorMessage
) {
    public static VoiceCommandResponse success(String message, List<TaskResponse> tasks) {
        return new VoiceCommandResponse(message, null, tasks, true, null);
    }

    public static VoiceCommandResponse success(String message) {
        return new VoiceCommandResponse(message, null, null, true, null);
    }
    
    public static VoiceCommandResponse error(String errorMessage) {
        return new VoiceCommandResponse(null, null, null, false, errorMessage);
    }
}
