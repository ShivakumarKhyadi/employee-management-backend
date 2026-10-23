package com.example.demo.controller;

import com.example.demo.dto.SupportMessageDTO;
import com.example.demo.dto.TypingEvent;
import com.example.demo.service.SupportChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    @Autowired private SupportChatService supportChatService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    public record SendChatMessageRequest(
            String receiverUsername,
            Long employeeId,
            String staffUsername,
            String message
    ) {}

    public record TypingRequest(String toUsername, boolean typing) {}
    public record ReadRequest(String otherUsername) {}

    @MessageMapping("/chat.send")
    public void send(@Payload SendChatMessageRequest request, Principal principal) {
        String me = principal.getName();

        SupportMessageDTO dto;
        if (request.employeeId() != null && request.staffUsername() != null) {
            boolean isStaffSender = me.equals(request.staffUsername());
            dto = supportChatService.sendMessage(
                    request.employeeId(), request.staffUsername(), me, isStaffSender, request.message());
        } else if (request.receiverUsername() != null) {
            dto = supportChatService.sendDirectMessage(me, request.receiverUsername(), request.message());
        } else {
            throw new IllegalArgumentException("Either receiverUsername, or employeeId + staffUsername, is required");
        }
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingRequest request, Principal principal) {
        String me = principal.getName();
        messagingTemplate.convertAndSendToUser(
                request.toUsername(), "/queue/typing",
                new TypingEvent(me, request.toUsername(), request.typing()));
    }

    @MessageMapping("/chat.read")
    public void read(@Payload ReadRequest request, Principal principal) {
        supportChatService.markConversationRead(principal.getName(), request.otherUsername());
    }
}
