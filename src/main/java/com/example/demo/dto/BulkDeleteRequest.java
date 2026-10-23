package com.example.demo.dto;

import java.util.List;

public record BulkDeleteRequest(List<Long> empIds) {
}
