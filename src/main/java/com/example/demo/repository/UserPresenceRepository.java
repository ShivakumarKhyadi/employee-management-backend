package com.example.demo.repository;

import com.example.demo.model.UserPresence;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface UserPresenceRepository extends JpaRepository<UserPresence, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_presence (username, last_seen) VALUES (:username, :lastSeen) " +
            "ON DUPLICATE KEY UPDATE last_seen = :lastSeen", nativeQuery = true)
    void upsertLastSeen(@Param("username") String username, @Param("lastSeen") LocalDateTime lastSeen);

    default LocalDateTime findLastSeen(String username) {
        return findById(username).map(UserPresence::getLastSeen).orElse(null);
    }
}