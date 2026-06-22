package com.project.finAudit.Ai.dto;


import enums.ExpenseCategory;

import java.math.BigDecimal;

public record ClaimRequest(
        Long employeeId,
        BigDecimal amount,
        ExpenseCategory category,
        String description,
        String receiptFileName
) {}