package com.smarttaskassistant.auth.repository;

import com.smarttaskassistant.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> getUsersByRecentlyActiveAtAfter(Instant recentlyActiveAt);

    @Modifying
    @Query("UPDATE User u SET u.recentlyActiveAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateRecentlyActiveAtById(@Param("id") Long userId);
}

