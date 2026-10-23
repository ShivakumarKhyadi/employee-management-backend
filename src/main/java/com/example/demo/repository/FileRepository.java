package com.example.demo.repository;

import com.example.demo.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByEmployee_EmpId(Long employeeId);
}