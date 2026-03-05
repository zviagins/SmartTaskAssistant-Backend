package com.smarttaskassistant.notification.repository;

import com.smarttaskassistant.notification.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    
    List<Notification> findByUserIdAndType(Long userId, String type);
    
    List<Notification> findTopByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Convenience method for recent notifications
    default List<Notification> findTopByUserIdOrderByCreatedAtDesc(Long userId, int limit) {
        return findTopByUserIdOrderByCreatedAtDesc(userId, Pageable.ofSize(limit));
    }
}
