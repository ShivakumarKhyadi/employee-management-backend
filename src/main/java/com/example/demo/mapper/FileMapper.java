package com.example.demo.mapper;

import com.example.demo.dto.FileDTO;
import com.example.demo.model.FileEntity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FileMapper {

    public static FileDTO toDto(FileEntity f){
        return new FileDTO(
                f.getId(),
                f.getFilename(),
                f.getSize(),
                f.getType(),
                f.getEmployee() != null ? f.getEmployee().getEmpId() : null,
                f.getDocumentType(),
                f.getIssueDate(),
                f.getExpiryDate(),
                expiryStatus(f.getExpiryDate()),
                f.getCreatedBy(),
                f.getCreatedDate(),
                f.getLastModifiedBy(),
                f.getLastModifiedDate()
        );
    }

    private static String expiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) return null;
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) return "EXPIRED";
        if (ChronoUnit.DAYS.between(today, expiryDate) <= 15) return "EXPIRING";
        return "VALID";
    }
}