package com.example.demo.mapper;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.Notification;

public class NotificationMapper {
    public static NotificationDTO toDto(Notification n) {
        return new NotificationDTO(n.getId(), n.getType(), n.getMessage(), n.getLink(), n.isRead(), n.getCreatedDate());
    }
}