package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.SupportMessage;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.PresenceService;
import com.example.demo.service.SupportChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support-chat")
public class SupportChatController {

    @Autowired private SupportChatService supportChatService;
    @Autowired private PresenceService presenceService;

    public record SendMessageRequest(String message) {}
    public record MarkReadRequest(String otherUsername) {}
    public record PresenceResponse(boolean online, java.time.LocalDateTime lastSeen) {}

    @GetMapping("/staff")
    public ResponseEntity<List<StaffContactDTO>> getAvailableStaff() {
        return ResponseEntity.ok(supportChatService.getAvailableStaff());
    }

    @GetMapping("/threads")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<SupportThreadSummaryDTO>> getThreads(Authentication authentication) {
        return ResponseEntity.ok(supportChatService.getThreadsForStaff(authentication.getName()));
    }

    @GetMapping("/{employeeId}/{staffUsername}")
    @PreAuthorize("#employeeId == authentication.principal.employeeId or #staffUsername == authentication.name")
    public ResponseEntity<List<SupportMessageDTO>> getMessages(
            @PathVariable Long employeeId, @PathVariable String staffUsername) {
        return ResponseEntity.ok(supportChatService.getMessages(employeeId, staffUsername));
    }

    @PostMapping("/{employeeId}/{staffUsername}")
    @PreAuthorize("#employeeId == authentication.principal.employeeId or #staffUsername == authentication.name")
    public ResponseEntity<SupportMessageDTO> sendMessage(
            @PathVariable Long employeeId,
            @PathVariable String staffUsername,
            @RequestBody SendMessageRequest request,
            Authentication authentication) {

        boolean isStaffSender = authentication.getName().equals(staffUsername);

        return ResponseEntity.ok(supportChatService.sendMessage(
                employeeId, staffUsername, authentication.getName(), isStaffSender, request.message()));
    }

    @PostMapping(value = "/{employeeId}/{staffUsername}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("#employeeId == authentication.principal.employeeId or #staffUsername == authentication.name")
    public ResponseEntity<SupportMessageDTO> sendThreadAttachment(
            @PathVariable Long employeeId,
            @PathVariable String staffUsername,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        boolean isStaffSender = authentication.getName().equals(staffUsername);
        return ResponseEntity.ok(supportChatService.sendThreadAttachment(
                employeeId, staffUsername, authentication.getName(), isStaffSender, file));
    }

    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<EmployeeContactDTO>> getAvailableEmployees() {
        return ResponseEntity.ok(supportChatService.getAvailableEmployees());
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<StaffContactDTO>> getAvailableContacts(Authentication authentication) {
        return ResponseEntity.ok(supportChatService.getAvailableContacts(authentication.getName()));
    }

    @GetMapping("/my-conversations")
    public ResponseEntity<List<DmThreadSummaryDTO>> getMyConversations(Authentication authentication) {
        return ResponseEntity.ok(supportChatService.getRecentConversations(authentication.getName()));
    }

    @GetMapping("/dm/{otherUsername}")
    public ResponseEntity<List<SupportMessageDTO>> getConversation(
            @PathVariable String otherUsername, Authentication authentication) {
        return ResponseEntity.ok(supportChatService.getConversation(authentication.getName(), otherUsername));
    }

    @PostMapping("/dm/{otherUsername}")
    public ResponseEntity<SupportMessageDTO> sendDirectMessage(
            @PathVariable String otherUsername,
            @RequestBody SendMessageRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(supportChatService.sendDirectMessage(authentication.getName(), otherUsername, request.message()));
    }

    @PostMapping(value = "/dm/{otherUsername}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SupportMessageDTO> sendDirectAttachment(
            @PathVariable String otherUsername,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        return ResponseEntity.ok(supportChatService.sendDirectMessageAttachment(
                authentication.getName(), otherUsername, file));
    }

    @GetMapping("/attachments/{messageId}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long messageId, Authentication authentication) throws IOException {
        SupportMessage entity = supportChatService.getMessageEntity(messageId);

        String me = authentication.getName();
        if (!me.equals(entity.getSenderUsername()) && !me.equals(entity.getReceiverUsername())) {
            throw new AccessDeniedException("You do not have access to this attachment");
        }
        if (entity.getAttachmentPath() == null) {
            throw new IllegalArgumentException("This message has no attachment");
        }

        Path path = Path.of(entity.getAttachmentPath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + entity.getAttachmentName() + "\"")
                .contentType(MediaType.parseMediaType(
                        entity.getAttachmentType() != null ? entity.getAttachmentType() : "application/octet-stream"))
                .body(resource);
    }

    /** Fallback for when the client can't reach the WebSocket (or for initial paint). */
    @PostMapping("/read")
    public ResponseEntity<Void> markRead(@RequestBody MarkReadRequest request, Authentication authentication) {
        supportChatService.markConversationRead(authentication.getName(), request.otherUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", supportChatService.getUnreadCount(authentication.getName())));
    }

    @GetMapping("/presence/{username}")
    public ResponseEntity<PresenceResponse> getPresence(@PathVariable String username) {
        boolean online = presenceService.isOnline(username);
        return ResponseEntity.ok(new PresenceResponse(online, online ? null : presenceService.getLastSeen(username)));
    }
}
