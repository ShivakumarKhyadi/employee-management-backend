package com.example.demo.dto;

import java.util.List;

public record BulkDepartmentRequest(List<Long> empIds, Long departmentId) {
}
