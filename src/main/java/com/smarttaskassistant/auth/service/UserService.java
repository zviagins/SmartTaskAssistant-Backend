package com.smarttaskassistant.auth.service;

import com.smarttaskassistant.auth.model.User;
import com.smarttaskassistant.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateRecentlyActiveUser(Long userId) {
        userRepository.updateRecentlyActiveAtById(userId);
    }

    public List<Long> findUserIdsActiveOn(LocalDate date) {
        return userRepository.getUsersByRecentlyActiveAtAfter(Instant.from(date)).stream().map(User::getId).toList();
    }

    public Optional<User> getById(Long userId) {
        return userRepository.findById(userId);
    }
}
