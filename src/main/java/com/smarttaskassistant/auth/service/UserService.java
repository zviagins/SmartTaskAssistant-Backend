package com.smarttaskassistant.auth.service;

import com.smarttaskassistant.auth.model.User;
import com.smarttaskassistant.auth.repository.UserRepository;
import com.smarttaskassistant.task.event.TaskCreatedEvent;
import com.smarttaskassistant.task.event.TaskDeletedEvent;
import com.smarttaskassistant.task.event.TaskUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
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

    @Transactional
    @EventListener
    public void onTaskCreated(TaskCreatedEvent event) {
        updateRecentlyActiveUser(event.userId());
    }

    @Transactional
    @EventListener
    public void onTaskUpdated(TaskUpdatedEvent event) {
        updateRecentlyActiveUser(event.userId());
    }

    @Transactional
    @EventListener
    public void onTaskDeleted(TaskDeletedEvent event) {
        updateRecentlyActiveUser(event.userId());
    }
}
