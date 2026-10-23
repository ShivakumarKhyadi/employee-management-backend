package com.example.demo.config;

import com.example.demo.model.BlacklistedToken;
import com.example.demo.repository.BlacklistedTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenBlacklistService {

    @Autowired
    private BlacklistedTokenRepository repository;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public void blacklist(String token) {
        if (repository.existsByToken(token)) return;
        BlacklistedToken entry = new BlacklistedToken();
        entry.setToken(token);
        entry.setExpiresAt(jwtUtil.extractExpiration(token));
        repository.save(entry);
    }

    public boolean isBlacklisted(String token) {
        return repository.existsByToken(token);
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void purgeExpired() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}