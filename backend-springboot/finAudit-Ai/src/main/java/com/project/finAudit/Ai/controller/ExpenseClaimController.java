package com.project.finAudit.Ai.controller;

import com.project.finAudit.Ai.dto.ClaimRequest;
import com.project.finAudit.Ai.dto.ClaimResponse;
import com.project.finAudit.Ai.dto.UpdateStatusRequest;
import com.project.finAudit.Ai.service.ExpenseClaimService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "*")
public class ExpenseClaimController {
    private final ExpenseClaimService claimService;

    public ExpenseClaimController(ExpenseClaimService expenseClaimService, ExpenseClaimService claimService) {
        this.claimService = claimService;

    }
    @PostMapping(value = "/submit", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ClaimResponse> submitClaim(
            @RequestPart("claim") ClaimRequest claimRequest,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(claimService.submitClaim(claimRequest, file));
    }
    @GetMapping("/pending")
    public ResponseEntity getPendingClaims() {
        return ResponseEntity.ok(claimService.getPendingClaims());

    }
    @PutMapping("{id}/status")
    public ResponseEntity<ClaimResponse> updateClaimStatus(@PathVariable Long  id,@RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(claimService.updateClaimStatus(id,request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long id) {
        // ඔයාගේ service එකේ ක්ලේම් එක හොයන මෙතඩ් එක මෙතනට දාන්න
        return ResponseEntity.ok(claimService.getClaimById(id));
    }
}
