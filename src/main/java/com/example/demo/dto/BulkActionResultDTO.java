package com.example.demo.dto;

import java.util.List;

public record BulkActionResultDTO(int requested, int succeeded, List<BulkActionError> errors) {
}
