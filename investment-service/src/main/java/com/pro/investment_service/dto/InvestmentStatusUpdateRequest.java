package com.pro.investment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InvestmentStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(?i)(PENDING|APPROVED|REJECTED|COMPLETED)$",
            message = "Status must be one of PENDING, APPROVED, REJECTED, COMPLETED")
    private String status;
}
