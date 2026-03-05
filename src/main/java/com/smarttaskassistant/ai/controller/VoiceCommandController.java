package com.smarttaskassistant.ai.controller;

import com.smarttaskassistant.ai.model.VoiceCommandRequest;
import com.smarttaskassistant.ai.model.VoiceCommandResponse;
import com.smarttaskassistant.ai.service.VoiceCommandService;
import com.smarttaskassistant.task.model.TaskStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
@Slf4j
public class VoiceCommandController {

    private final VoiceCommandService voiceCommandService;

    @PostMapping("/command")
    public ResponseEntity<VoiceCommandResponse> processVoiceCommandAsync(
            @Valid @RequestBody VoiceCommandRequest request) {
        
        log.info("Received voice command request: {}", request.voiceText());
        
        voiceCommandService.processVoiceCommandAsync(request.voiceText());

        VoiceCommandResponse immediateResponse = VoiceCommandResponse.success(
                "Voice command processing started. You will receive a notification when complete."
        );

        return ResponseEntity.ok(immediateResponse);
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<VoiceCommandResponse> getDailySummary() {
        log.info("Generating daily summary");
        
        VoiceCommandResponse response = voiceCommandService.listTasks(TaskStatus.TODO);
        
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
