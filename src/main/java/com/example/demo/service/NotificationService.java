package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.model.AppUser;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;

    public void notify(String username, String type, String message, String link) {
        Notification n = new Notification();
        n.setRecipientUsername(username);
        n.setType(type);
        n.setMessage(message);
        n.setLink(link);
        n.setRead(false);
        n.setCreatedDate(LocalDateTime.now());
        notificationRepository.save(n);
    }

    public void notifyAdminsAndSupervisors(String type, String message, String link) {
        userRepository.findAll().stream()
                .filter(u -> u.getRoleCode() != null &&
                        (u.getRoleCode().equalsIgnoreCase("ADMIN") || u.getRoleCode().equalsIgnoreCase("SUPERVISOR")))
                .forEach(u -> notify(u.getUsername(), type, message, link));
    }

    public List<NotificationDTO> getMyNotifications(String username) {
        return notificationRepository.findByRecipientUsernameOrderByCreatedDateDesc(username).stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countByRecipientUsernameAndIsReadFalse(username);
    }

    public void markRead(Long id, String username) {
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getRecipientUsername().equals(username)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    public void markAllRead(String username) {
        List<Notification> unread = notificationRepository.findByRecipientUsernameOrderByCreatedDateDesc(username)
                .stream().filter(n -> !n.isRead()).collect(Collectors.toList());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public void notifyAllUsers(String type, String message, String link){
        userRepository.findAll().forEach(u -> notify(u.getUsername(), type, message, link));
    }
}