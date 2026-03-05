package com.smarttaskassistant.notification.service;

import com.smarttaskassistant.auth.util.SecurityUtils;
import com.smarttaskassistant.notification.model.Notification;
import com.smarttaskassistant.notification.model.NotificationRequest;
import com.smarttaskassistant.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getAll() {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("No user found"));

        return notificationRepository.findByUserId(userId);
    }

    /**
     * Get recent notifications for the current user
     * @param limit Maximum number of notifications to return
     * @return List of recent notifications
     */
    public List<Notification> getRecent(int limit) {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("No user found"));

        return notificationRepository.findTopByUserIdOrderByCreatedAtDesc(userId, limit);
    }

    /**
     * Create notification, persist it to db and send to client
     */
    public void handleNotification(NotificationRequest notificationRequest, Long userId) {
        try {
            Notification notification = Notification.builder()
                    .userId(userId)
                    .notification(notificationRequest.notification())
                    .type(notificationRequest.type())
                    .build();

            notificationRepository.save(notification);

            //send notification TODO
            
        } catch (Exception e) {
            log.error("Failed to handle notification: {}", notificationRequest, e);
        }
    }
}
