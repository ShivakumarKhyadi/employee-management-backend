package com.example.demo.service;

import com.example.demo.dto.AcknowledgmentDTO;
import com.example.demo.dto.PendingAcknowledgmentDTO;
import com.example.demo.exception.AcknowledgmentException;
import com.example.demo.mapper.AcknowledgmentMapper;
import com.example.demo.model.DocumentAcknowledgment;
import com.example.demo.model.Employee;
import com.example.demo.model.FileEntity;
import com.example.demo.repository.DocumentAcknowledgmentRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.FileRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcknowledgmentService {

    @Autowired private DocumentAcknowledgmentRepository ackRepository;
    @Autowired private FileRepository fileRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private NotificationService notificationService;

    public record SignRequest(@NotBlank(message = "Typed name is required") String signedName) {}

    public List<AcknowledgmentDTO> getSignersForFile(Long fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        return ackRepository.findByFileId(fileId).stream()
                .map(a -> AcknowledgmentMapper.toDto(a, file, employeeRepository.findById(a.getEmployeeId()).orElse(null)))
                .collect(Collectors.toList());
    }

    public List<PendingAcknowledgmentDTO> getPendingForEmployee(Long employeeId) {
        List<Long> signedFileIds = ackRepository.findByEmployeeId(employeeId).stream()
                .map(DocumentAcknowledgment::getFileId).toList();

        return fileRepository.findAll().stream()
                .filter(FileEntity::isRequiresAcknowledgment)
                .filter(f -> !signedFileIds.contains(f.getId()))
                .map(f -> new PendingAcknowledgmentDTO(f.getId(), f.getFilename(), employeeId))
                .collect(Collectors.toList());
    }

    public AcknowledgmentDTO sign(Long fileId, Long employeeId, SignRequest request) {
        if (ackRepository.findByFileIdAndEmployeeId(fileId, employeeId).isPresent()) {
            throw new AcknowledgmentException("You've already acknowledged this document");
        }

        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        DocumentAcknowledgment ack = new DocumentAcknowledgment();
        ack.setFileId(fileId);
        ack.setEmployeeId(employeeId);
        ack.setSignedName(request.signedName().trim());
        ack.setAcknowledgedDate(LocalDateTime.now());
        DocumentAcknowledgment saved = ackRepository.save(ack);

        notificationService.notifyAdminsAndSupervisors(
                "DOCUMENT_ACKNOWLEDGED",
                employee.getName() + " acknowledged " + file.getFilename(),
                "/documents/" + fileId
        );

        return AcknowledgmentMapper.toDto(saved, file, employee);
    }
}