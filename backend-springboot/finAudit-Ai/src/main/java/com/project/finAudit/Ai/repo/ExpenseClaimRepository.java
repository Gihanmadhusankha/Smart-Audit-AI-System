package com.project.finAudit.Ai.repo;


import com.project.finAudit.Ai.entity.ExpenseClaims;
import enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaims, Long> {
    List<ExpenseClaims> findByStatus(ExpenseStatus status);
}
