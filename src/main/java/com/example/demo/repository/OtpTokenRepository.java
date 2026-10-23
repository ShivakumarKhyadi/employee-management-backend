package com.example.demo.repository;

import com.example.demo.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByUsernameAndUsedFalseOrderByCreatedDateDesc(String username);
}
