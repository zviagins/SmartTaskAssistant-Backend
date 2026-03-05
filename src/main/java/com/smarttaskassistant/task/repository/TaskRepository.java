package com.smarttaskassistant.task.repository;

import com.smarttaskassistant.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY t.updatedAt DESC LIMIT 1")
    Optional<Task> findByTitleContainingAndUserIdOrderByUpdatedAtDesc(@Param("title") String title, @Param("userId") Long userId);
}
