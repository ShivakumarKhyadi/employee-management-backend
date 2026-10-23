package com.example.demo.service;

import com.example.demo.dto.FileDTO;
import com.example.demo.mapper.FileMapper;
import com.example.demo.model.Employee;
import com.example.demo.model.FileEntity;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {
    private static final String UPLOAD_DIR = "uploads/files/";

    @Autowired private FileRepository fileRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private NotificationService notificationService;

    public FileDTO storeFile(Long employeeId, MultipartFile multipartFile, String uploadedByUsername,
                             String documentType, LocalDate issueDate, LocalDate expiryDate) throws IOException {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Files.createDirectories(Paths.get(UPLOAD_DIR));

        String storeName = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
        Path target = Paths.get(UPLOAD_DIR, storeName);
        Files.copy(multipartFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        FileEntity entity = new FileEntity();
        entity.setFilename(multipartFile.getOriginalFilename());
        entity.setSize(multipartFile.getSize());
        entity.setType(multipartFile.getContentType());
        entity.setPath(target.toString());
        entity.setEmployee(employee);
        entity.setDocumentType(documentType);
        entity.setIssueDate(issueDate);
        entity.setExpiryDate(expiryDate);

        entity = fileRepository.save(entity);

        if (employee.getAppUser() != null && !employee.getAppUser().getUsername().equals(uploadedByUsername)) {
            notificationService.notify(
                    employee.getAppUser().getUsername(),
                    "FILE_UPLOADED",
                    uploadedByUsername + " uploaded a file to your profile: " + multipartFile.getOriginalFilename(),
                    "/employees/" + employeeId
            );
        }
        return FileMapper.toDto(entity);
    }

    public List<FileDTO> getFilesForEmployee(Long employeeId) {
        return fileRepository.findByEmployee_EmpId(employeeId).stream()
                .map(FileMapper::toDto)
                .collect(Collectors.toList());
    }

    public FileEntity getFileEntity(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File Not Found"));
    }

    public void deleteFile(Long id) throws IOException {
        FileEntity entity = getFileEntity(id);
        Files.deleteIfExists(Paths.get(entity.getPath()));
        fileRepository.delete(entity);
    }
}