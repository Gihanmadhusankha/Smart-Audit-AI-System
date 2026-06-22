package com.project.finAudit.Ai.service;

import com.project.finAudit.Ai.dto.ClaimRequest;
import com.project.finAudit.Ai.dto.ClaimResponse;
import com.project.finAudit.Ai.dto.UpdateStatusRequest;
import enums.ExpenseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExpenseClaimService {
    public ClaimResponse submitClaim(ClaimRequest claimRequest, MultipartFile file);
    public List getPendingClaims();
    public ClaimResponse updateClaimStatus(Long ClaimId,UpdateStatusRequest updateStatusRequest);

    ClaimResponse getClaimById(Long id);
}
