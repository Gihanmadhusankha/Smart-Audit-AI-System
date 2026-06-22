package com.project.finAudit.Ai.dto;


import enums.ExpenseStatus;

public record UpdateStatusRequest(

        ExpenseStatus status,
        String reason
) {}