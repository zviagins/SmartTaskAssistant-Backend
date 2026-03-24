package com.smarttaskassistant.ai.controller;

import com.smarttaskassistant.ai.model.VoiceCommandRequest;
import com.smarttaskassistant.ai.model.VoiceCommandResponse;
import com.smarttaskassistant.ai.service.VoiceCommandService;
import com.smarttaskassistant.auth.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        
        // Get userId from SecurityContext before async execution
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        
        voiceCommandService.processVoiceCommandAsync(request.voiceText(), userId);

        VoiceCommandResponse immediateResponse = VoiceCommandResponse.success(
                "Voice command processing started. You will receive a notification when complete."
        );

        return ResponseEntity.ok(immediateResponse);
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<VoiceCommandResponse> getDailySummary() {
        log.info("Generating daily summary");

        // Get userId from SecurityContext before async execution
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        
        VoiceCommandResponse response = voiceCommandService.getDailySummary(userId);
        
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
