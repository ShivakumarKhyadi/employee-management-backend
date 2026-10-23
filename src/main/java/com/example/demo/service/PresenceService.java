package com.example.demo.service;

import com.example.demo.dto.PresenceEvent;
import com.example.demo.repository.UserPresenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final Map<String, Integer> openSessionsByUser = new ConcurrentHashMap<>();

    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private UserPresenceRepository userPresenceRepository;

    public void markOnline(String username) {
        openSessionsByUser.merge(username, 1, Integer::sum);
        broadcastPresence(username, true, null);
    }

    public void markOffline(String username) {
        openSessionsByUser.computeIfPresent(username, (k, v) -> (v - 1 <= 0) ? null : v - 1);
        boolean stillOnline = openSessionsByUser.containsKey(username);
        if (!stillOnline) {
            LocalDateTime now = LocalDateTime.now();
            userPresenceRepository.upsertLastSeen(username, now);
            broadcastPresence(username, false, now);
        }
    }

    public boolean isOnline(String username) {
        return openSessionsByUser.containsKey(username);
    }

    public LocalDateTime getLastSeen(String username) {
        return userPresenceRepository.findLastSeen(username);
    }

    private void broadcastPresence(String username, boolean online, LocalDateTime lastSeen) {
        messagingTemplate.convertAndSend("/topic/presence", new PresenceEvent(username, online, lastSeen));
    }
}