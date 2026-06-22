package com.project.finAudit.Ai.dto;

import enums.ExpenseCategory;
import enums.ExpenseStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Builder
public record ClaimResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String employeeRole,
        BigDecimal amount,
        ExpenseCategory category,
        String description,
        ExpenseStatus status,
        String receiptImagePath,
        String flaggedReason,
        LocalDateTime createdAt
) {}