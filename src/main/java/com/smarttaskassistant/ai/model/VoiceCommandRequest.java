package com.smarttaskassistant.ai.model;

import jakarta.validation.constraints.NotBlank;

public record VoiceCommandRequest(
        @NotBlank(message = "Voice command text is required")
        String voiceText
) {}
