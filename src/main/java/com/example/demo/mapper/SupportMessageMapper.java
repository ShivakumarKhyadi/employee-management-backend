package com.example.demo.mapper;

import com.example.demo.dto.SupportMessageDTO;
import com.example.demo.model.SupportMessage;
import com.example.demo.service.MessageEncryptionService;

public class SupportMessageMapper {
    public static SupportMessageDTO toDto(SupportMessage m, MessageEncryptionService encryptionService) {
        return new SupportMessageDTO(
                m.getId(),
                m.getSenderUsername(),
                m.getReceiverUsername(),
                m.getSenderRole(),
                encryptionService.decrypt(m.getMessage()),
                m.getSentDate(),
                m.isRead(),
                m.getReadAt(),
                m.getAttachmentPath() != null ? "/api/support-chat/attachments/" + m.getId() : null,
                m.getAttachmentName(),
                m.getAttachmentType(),
                m.getAttachmentSize()
        );
    }
}