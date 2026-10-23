package com.example.demo.repository;

import com.example.demo.model.ChecklistCategory;
import com.example.demo.model.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    List<ChecklistItem> findByEmployeeIdOrderByIdAsc(Long employeeId);
    List<ChecklistItem> findByCategoryAndCompletedFalseOrderByEmployeeIdAsc(ChecklistCategory category);
    boolean existsByEmployeeIdAndCategory(Long employeeId, ChecklistCategory category);
}