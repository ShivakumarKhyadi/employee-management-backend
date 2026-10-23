package com.example.demo.repository;

import com.example.demo.model.AssetStatus;
import com.example.demo.model.CompanyAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompanyAssetRepository extends JpaRepository<CompanyAsset, Long> {
    List<CompanyAsset> findByEmployee_EmpIdOrderByAssignedDateDesc(Long employeeId);
    List<CompanyAsset> findByStatusOrderByAssetNameAsc(AssetStatus status);
    List<CompanyAsset> findByReturnDueDateIsNotNullAndReturnNotifiedFalse();
}