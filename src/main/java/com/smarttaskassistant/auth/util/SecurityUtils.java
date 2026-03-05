package com.smarttaskassistant.auth.util;

import com.smarttaskassistant.auth.model.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<Long> getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return Optional.of(user.getId());
        }
        return Optional.empty();
    }
    
    public static Optional<String> getCurrentUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return Optional.ofNullable(user.getName());
        }
        return Optional.empty();
    }
}

