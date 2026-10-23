package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.mapper.SupportMessageMapper;
import com.example.demo.model.Employee;
import com.example.demo.model.SenderRole;
import com.example.demo.model.SupportMessage;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.SupportMessageRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SupportChatService {

    private static final String ATTACHMENT_DIR = "uploads/chat-attachments/";

    @Autowired private SupportMessageRepository supportMessageRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private MessageEncryptionService encryptionService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    public List<SupportMessageDTO> getMessages(Long employeeId, String otherPartyUsername) {
        return supportMessageRepository
                .findByEmployeeIdAndOtherPartyUsernameOrderBySentDateAsc(employeeId, otherPartyUsername)
                .stream()
                .map(m -> SupportMessageMapper.toDto(m, encryptionService))
                .collect(Collectors.toList());
    }

    public SupportMessageDTO sendMessage(Long employeeId, String otherPartyUsername,
                                         String senderUsername, boolean isStaffSender, String text) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        String receiverUsername = isStaffSender
                ? (employee.getAppUser() != null ? employee.getAppUser().getUsername() : null)
                : otherPartyUsername;

        if (receiverUsername == null) {
            throw new IllegalArgumentException("This employee has no login account to receive messages");
        }

        SupportMessage message = new SupportMessage();
        message.setEmployeeId(employeeId);
        message.setOtherPartyUsername(otherPartyUsername);
        message.setSenderUsername(senderUsername);
        message.setReceiverUsername(receiverUsername);
        message.setSenderRole(isStaffSender ? SenderRole.STAFF : SenderRole.EMPLOYEE);
        message.setMessage(encryptionService.encrypt(text));
        message.setSentDate(LocalDateTime.now());
        message = supportMessageRepository.save(message);

        String senderDisplayName = isStaffSender
                ? displayNameOf(senderUsername)
                : employee.getName();

        notificationService.notify(
                receiverUsername,
                isStaffSender ? "SUPPORT_REPLY" : "SUPPORT_MESSAGE",
                senderDisplayName + " sent you a message",
                isStaffSender ? "/direct-message/" + otherPartyUsername : "/support-inbox/" + employeeId
        );

        return SupportMessageMapper.toDto(message, encryptionService);
    }

    private String displayNameOf(String username){
        return employeeRepository.findByAppUser_Username(username)
                .map(Employee::getName)
                .orElse(username);
    }

    public List<SupportThreadSummaryDTO> getThreadsForStaff(String staffUsername) {
        return supportMessageRepository.findThreadsForStaff(staffUsername).stream()
                .map(row -> {
                    Long empId = (Long) row[0];
                    String otherParty = (String) row[1];
                    List<SupportMessage> msgs = supportMessageRepository
                            .findByEmployeeIdAndOtherPartyUsernameOrderBySentDateAsc(empId, otherParty);
                    SupportMessage last = msgs.get(msgs.size() - 1);
                    Employee employee = employeeRepository.findById(empId).orElse(null);
                    return new SupportThreadSummaryDTO(
                            empId,
                            employee != null ? employee.getName() : "Unknown",
                            otherParty,
                            encryptionService.decrypt(last.getMessage()),
                            last.getSentDate()
                    );
                })
                .sorted(Comparator.comparing(SupportThreadSummaryDTO::lastSentDate).reversed())
                .collect(Collectors.toList());
    }

    public List<StaffContactDTO> getAvailableStaff() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoleCode() != null &&
                        (u.getRoleCode().equalsIgnoreCase("ADMIN") || u.getRoleCode().equalsIgnoreCase("SUPERVISOR")))
                .map(u -> {
                    String displayName = employeeRepository.findByAppUser_UserId(u.getUserId())
                            .map(Employee::getName)
                            .orElse(u.getUsername());
                    return new StaffContactDTO(u.getUsername(), displayName, u.getRoleCode());
                })
                .collect(Collectors.toList());
    }

    public List<EmployeeContactDTO> getAvailableEmployees() {
        return employeeRepository.findAll().stream()
                .map(e -> new EmployeeContactDTO(
                        e.getEmpId(),
                        e.getName(),
                        e.getDepartment() != null ? e.getDepartment().getDepartmentName() : null
                ))
                .collect(Collectors.toList());
    }

    public List<StaffContactDTO> getAvailableContacts(String excludeUsername) {
        return userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals(excludeUsername))
                .map(u -> {
                    String displayName = employeeRepository.findByAppUser_UserId(u.getUserId())
                            .map(Employee::getName)
                            .orElse(u.getUsername());
                    return new StaffContactDTO(u.getUsername(), displayName, u.getRoleCode());
                })
                .collect(Collectors.toList());
    }

    public List<SupportMessageDTO> getConversation(String myUsername, String otherUsername) {
        return supportMessageRepository.findConversationBetween(myUsername, otherUsername).stream()
                .map(m -> SupportMessageMapper.toDto(m, encryptionService))
                .collect(Collectors.toList());
    }

    public List<DmThreadSummaryDTO> getRecentConversations(String myUsername) {
        return supportMessageRepository.findDistinctContactsForUser(myUsername).stream()
                .map(otherUsername -> {
                    List<SupportMessage> convo = supportMessageRepository.findConversationBetween(myUsername, otherUsername);
                    if (convo.isEmpty()) return null;
                    SupportMessage last = convo.get(convo.size() - 1);
                    String displayName = employeeRepository.findByAppUser_Username(otherUsername)
                            .map(Employee::getName)
                            .orElse(otherUsername);
                    String roleCode = userRepository.findByUsername(otherUsername)
                            .map(u -> u.getRoleCode())
                            .orElse("");
                    long unread = supportMessageRepository
                            .countBySenderUsernameAndReceiverUsernameAndIsReadFalse(otherUsername, myUsername);
                    return new DmThreadSummaryDTO(
                            otherUsername, displayName, roleCode,
                            encryptionService.decrypt(last.getMessage()), last.getSentDate(), unread);
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(DmThreadSummaryDTO::lastSentDate).reversed())
                .collect(Collectors.toList());
    }

    public SupportMessageDTO sendDirectMessage(String senderUsername, String receiverUsername, String text) {
        Long senderEmployeeId = employeeRepository.findByAppUser_Username(senderUsername)
                .map(Employee::getEmpId)
                .orElseThrow(() -> new IllegalArgumentException("You must have an employee record to send messages"));

        boolean senderIsStaff = userRepository.findByUsername(senderUsername)
                .map(u -> u.getRoleCode() != null &&
                        (u.getRoleCode().equalsIgnoreCase("ADMIN") || u.getRoleCode().equalsIgnoreCase("SUPERVISOR")))
                .orElse(false);

        SupportMessage message = new SupportMessage();
        message.setEmployeeId(senderEmployeeId);
        message.setOtherPartyUsername(receiverUsername);
        message.setSenderUsername(senderUsername);
        message.setReceiverUsername(receiverUsername);
        message.setSenderRole(senderIsStaff ? SenderRole.STAFF : SenderRole.EMPLOYEE);
        message.setMessage(encryptionService.encrypt(text));
        message.setSentDate(LocalDateTime.now());
        message = supportMessageRepository.save(message);

        boolean receiverIsStaff = userRepository.findByUsername(receiverUsername)
                .map(u -> u.getRoleCode() != null &&
                        (u.getRoleCode().equalsIgnoreCase("ADMIN") || u.getRoleCode().equalsIgnoreCase("SUPERVISOR")))
                .orElse(false);

        notificationService.notify(
                receiverUsername,
                senderIsStaff ? "SUPPORT_REPLY" : "SUPPORT_MESSAGE",
                displayNameOf(senderUsername) + " sent you a message",
                receiverIsStaff ? "/support-inbox/" + senderEmployeeId : "/direct-message/" + senderUsername
        );

        return SupportMessageMapper.toDto(message, encryptionService);
    }

    private boolean isStaff(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getRoleCode() != null &&
                        (u.getRoleCode().equalsIgnoreCase("ADMIN") || u.getRoleCode().equalsIgnoreCase("SUPERVISOR")))
                .orElse(false);
    }

    public void broadcastNewMessage(SupportMessageDTO dto) {
        messagingTemplate.convertAndSendToUser(dto.receiverUsername(), "/queue/messages", dto);
        messagingTemplate.convertAndSendToUser(dto.senderUsername(), "/queue/messages", dto);
        pushUnreadCount(dto.receiverUsername());
    }

    public void pushUnreadCount(String username) {
        messagingTemplate.convertAndSendToUser(
                username, "/queue/unread-count", new UnreadCountEvent(getUnreadCount(username)));
    }

    public long getUnreadCount(String username) {
        return supportMessageRepository.countByReceiverUsernameAndIsReadFalse(username);
    }

    @Transactional
    public void markConversationRead(String meUsername, String otherUsername) {
        List<SupportMessage> unread = supportMessageRepository
                .findBySenderUsernameAndReceiverUsernameAndIsReadFalse(otherUsername, meUsername);
        if (unread.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        unread.forEach(m -> {
            m.setRead(true);
            m.setReadAt(now);
        });
        supportMessageRepository.saveAll(unread);

        messagingTemplate.convertAndSendToUser(
                otherUsername, "/queue/read-receipts",
                new ReadReceiptEvent(meUsername, otherUsername, now));
        pushUnreadCount(meUsername);
    }

    public SupportMessageDTO sendDirectMessageAttachment(String senderUsername, String receiverUsername,
                                                         MultipartFile file) throws IOException {
        Long senderEmployeeId = employeeRepository.findByAppUser_Username(senderUsername)
                .map(Employee::getEmpId)
                .orElseThrow(() -> new IllegalArgumentException("You must have an employee record to send messages"));

        boolean senderIsStaff = isStaff(senderUsername);

        SupportMessage message = new SupportMessage();
        message.setEmployeeId(senderEmployeeId);
        message.setOtherPartyUsername(receiverUsername);
        message.setSenderUsername(senderUsername);
        message.setReceiverUsername(receiverUsername);
        message.setSenderRole(senderIsStaff ? SenderRole.STAFF : SenderRole.EMPLOYEE);
        message.setMessage(encryptionService.encrypt(""));
        message.setSentDate(LocalDateTime.now());
        attachFile(message, file);
        message = supportMessageRepository.save(message);

        boolean receiverIsStaff = isStaff(receiverUsername);
        notificationService.notify(
                receiverUsername,
                senderIsStaff ? "SUPPORT_REPLY" : "SUPPORT_MESSAGE",
                displayNameOf(senderUsername) + " sent you a file",
                receiverIsStaff ? "/support-inbox/" + senderEmployeeId : "/direct-message/" + senderUsername
        );

        SupportMessageDTO dto = SupportMessageMapper.toDto(message, encryptionService);
        broadcastNewMessage(dto);
        return dto;
    }

    public SupportMessageDTO sendThreadAttachment(Long employeeId, String otherPartyUsername, String senderUsername,
                                                  boolean isStaffSender, MultipartFile file) throws IOException {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        String receiverUsername = isStaffSender
                ? (employee.getAppUser() != null ? employee.getAppUser().getUsername() : null)
                : otherPartyUsername;
        if (receiverUsername == null) {
            throw new IllegalArgumentException("This employee has no login account to receive messages");
        }

        SupportMessage message = new SupportMessage();
        message.setEmployeeId(employeeId);
        message.setOtherPartyUsername(otherPartyUsername);
        message.setSenderUsername(senderUsername);
        message.setReceiverUsername(receiverUsername);
        message.setSenderRole(isStaffSender ? SenderRole.STAFF : SenderRole.EMPLOYEE);
        message.setMessage(encryptionService.encrypt(""));
        message.setSentDate(LocalDateTime.now());
        attachFile(message, file);
        message = supportMessageRepository.save(message);

        String senderDisplayName = isStaffSender ? displayNameOf(senderUsername) : employee.getName();
        notificationService.notify(
                receiverUsername,
                isStaffSender ? "SUPPORT_REPLY" : "SUPPORT_MESSAGE",
                senderDisplayName + " sent you a file",
                isStaffSender ? "/direct-message/" + otherPartyUsername : "/support-inbox/" + employeeId
        );

        SupportMessageDTO dto = SupportMessageMapper.toDto(message, encryptionService);
        broadcastNewMessage(dto);
        return dto;
    }

    private void attachFile(SupportMessage message, MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(ATTACHMENT_DIR));
        String storeName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = Paths.get(ATTACHMENT_DIR, storeName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        message.setAttachmentPath(target.toString());
        message.setAttachmentName(file.getOriginalFilename());
        message.setAttachmentType(file.getContentType());
        message.setAttachmentSize(file.getSize());
    }

    public SupportMessage getMessageEntity(Long id) {
        return supportMessageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
    }

}