package com.example.demo.service;

import com.example.demo.dto.CompanyAssetDTO;
import com.example.demo.exception.AssetNotFoundException;
import com.example.demo.mapper.CompanyAssetMapper;
import com.example.demo.model.AssetStatus;
import com.example.demo.model.AssetType;
import com.example.demo.model.CompanyAsset;
import com.example.demo.model.Employee;
import com.example.demo.repository.CompanyAssetRepository;
import com.example.demo.repository.EmployeeRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetService {

    @Autowired private CompanyAssetRepository assetRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private NotificationService notificationService;

    public record CreateAssetRequest(
            @NotBlank(message = "Asset name is required") String assetName,
            @NotNull(message = "Asset type is required") AssetType assetType,
            String serialNumber,
            String notes
    ) {}

    public record AssignAssetRequest(
            @NotNull(message = "Employee is required") Long employeeId,
            LocalDate returnDueDate
    ) {}

    public List<CompanyAssetDTO> listAll() {
        return assetRepository.findAll().stream().map(CompanyAssetMapper::toDto).collect(Collectors.toList());
    }

    public List<CompanyAssetDTO> listForEmployee(Long employeeId) {
        return assetRepository.findByEmployee_EmpIdOrderByAssignedDateDesc(employeeId).stream()
                .map(CompanyAssetMapper::toDto).collect(Collectors.toList());
    }

    public CompanyAssetDTO createAsset(CreateAssetRequest request) {
        CompanyAsset asset = new CompanyAsset();
        asset.setAssetName(request.assetName().trim());
        asset.setAssetType(request.assetType());
        asset.setSerialNumber(request.serialNumber());
        asset.setNotes(request.notes());
        asset.setStatus(AssetStatus.AVAILABLE);
        return CompanyAssetMapper.toDto(assetRepository.save(asset));
    }

    public CompanyAssetDTO assignAsset(Long assetId, AssignAssetRequest request) {
        CompanyAsset asset = findOrThrow(assetId);
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        asset.setEmployee(employee);
        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setAssignedDate(LocalDate.now());
        asset.setReturnDueDate(request.returnDueDate());
        asset.setReturnedDate(null);
        asset.setReturnNotified(false);
        CompanyAsset saved = assetRepository.save(asset);

        if (employee.getAppUser() != null) {
            notificationService.notify(
                    employee.getAppUser().getUsername(),
                    "ASSET_ASSIGNED",
                    "You've been assigned: " + asset.getAssetName(),
                    "/employees/" + employee.getEmpId()
            );
        }
        return CompanyAssetMapper.toDto(saved);
    }

    public CompanyAssetDTO returnAsset(Long assetId) {
        CompanyAsset asset = findOrThrow(assetId);
        asset.setStatus(AssetStatus.RETURNED);
        asset.setReturnedDate(LocalDate.now());
        return CompanyAssetMapper.toDto(assetRepository.save(asset));
    }

    public void deleteAsset(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new AssetNotFoundException("Asset not found");
        }
        assetRepository.deleteById(assetId);
    }

    private CompanyAsset findOrThrow(Long id) {
        return assetRepository.findById(id).orElseThrow(() -> new AssetNotFoundException("Asset not found"));
    }
}