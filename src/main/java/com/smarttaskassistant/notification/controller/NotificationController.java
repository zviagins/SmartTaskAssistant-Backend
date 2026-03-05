package com.smarttaskassistant.notification.controller;

import com.smarttaskassistant.notification.model.Notification;
import com.smarttaskassistant.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        log.debug("Fetching all notifications for current user");
        return ResponseEntity.ok(notificationService.getAll());
    }

    /**
     * Get recent notifications (last N notifications)
     * @param limit Maximum number of notifications to return (default: 10)
     * @return List of recent notifications
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Notification>> getRecent(@RequestParam(defaultValue = "10") int limit) {
        log.debug("Fetching recent notifications, limit: {}", limit);
        List<Notification> notifications = notificationService.getRecent(limit);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark a notification as read (if you add a read status field later)
     * @param id The notification ID
     * @return Success response
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        log.debug("Marking notification {} as read", id);
        // This would be implemented if you add a read status field to Notification
        // notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
