package com.example.demo.mapper;

import com.example.demo.dto.AcknowledgmentDTO;
import com.example.demo.model.DocumentAcknowledgment;
import com.example.demo.model.Employee;
import com.example.demo.model.FileEntity;

public class AcknowledgmentMapper {
    public static AcknowledgmentDTO toDto(DocumentAcknowledgment a, FileEntity file, Employee employee) {
        return new AcknowledgmentDTO(
                a.getId(), a.getFileId(),
                file != null ? file.getFilename() : null,
                a.getEmployeeId(),
                employee != null ? employee.getName() : null,
                a.getSignedName(), a.getAcknowledgedDate()
        );
    }
}