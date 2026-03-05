package com.smarttaskassistant.auth.service;

import com.smarttaskassistant.auth.model.AuthResponse;
import com.smarttaskassistant.auth.model.LoginRequest;
import com.smarttaskassistant.auth.model.SignUpRequest;
import com.smarttaskassistant.auth.model.User;
import com.smarttaskassistant.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public void signUp(SignUpRequest req) {
        if (userRepository.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email already taken");
        User user = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .name(req.name())
                .roles("ROLE_USER")
                .build();
        userRepository.save(user);
        // optionally: send verification email
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        // TODO: persist refresh token (hashed) in DB for revocation
        return new AuthResponse(access, refresh);
    }
}

