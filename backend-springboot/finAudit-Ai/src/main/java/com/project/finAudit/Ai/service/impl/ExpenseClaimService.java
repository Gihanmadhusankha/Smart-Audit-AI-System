package com.project.finAudit.Ai.service.impl;

import com.project.finAudit.Ai.dto.ClaimRequest;
import com.project.finAudit.Ai.dto.ClaimResponse;
import com.project.finAudit.Ai.dto.UpdateStatusRequest;
import com.project.finAudit.Ai.entity.Employee;
import com.project.finAudit.Ai.entity.ExpenseClaims;
import com.project.finAudit.Ai.repo.EmployeeRepository;
import com.project.finAudit.Ai.repo.ExpenseClaimRepository;
import enums.ExpenseStatus;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseClaimService implements com.project.finAudit.Ai.service.ExpenseClaimService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ExpenseClaimRepository claimRepository;
    private final EmployeeRepository employeeRepository;

    public ExpenseClaimService(ExpenseClaimRepository claimRepository, EmployeeRepository employeeRepository) {
        this.claimRepository = claimRepository;
        this.employeeRepository = employeeRepository;
    }

    // 1. Submit a claim and return DTO
    @Transactional
    public ClaimResponse submitClaim(ClaimRequest request, MultipartFile file) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + request.employeeId()));

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/");

        try {
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + fileName, e);
        }

        ExpenseClaims claim = new ExpenseClaims();
        claim.setEmployee(employee);
        claim.setAmount(request.amount());
        claim.setCategory(request.category());
        claim.setDescription(request.description());
        claim.setReceiptImagePath(uploadPath.resolve(fileName).toString());
        claim.setStatus(ExpenseStatus.PENDING);

        ExpenseClaims savedClaim = claimRepository.save(claim);

        rabbitTemplate.convertAndSend("expense_queue", savedClaim.getId().toString());
        return mapToResponse(savedClaim);
    }

    // 2. Get pending claims as DTO List (For AI Agent)
    public List getPendingClaims() {
        return claimRepository.findByStatus(ExpenseStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClaimResponse updateClaimStatus(Long ClaimId,UpdateStatusRequest updateStatusRequest) {
        ExpenseClaims claims=claimRepository.findById(ClaimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with ID: " + ClaimId));
        claims.setStatus(updateStatusRequest.status());
        if(updateStatusRequest.reason()!=null){
            claims.setFlaggedReason(updateStatusRequest.reason());
        }
        if (updateStatusRequest.status() == ExpenseStatus.APPROVED) {
            Employee employee = claims.getEmployee();
            employee.setSpentThisMonth(employee.getSpentThisMonth().add(claims.getAmount()));
            employeeRepository.save(employee);
        }
        ExpenseClaims updatedClaim = claimRepository.save(claims);
        return mapToResponse(updatedClaim);
    }

    @Override
    public ClaimResponse getClaimById(Long id) {
        ExpenseClaims claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found with ID: " + id));
        return mapToResponse(claim);

    }


    // Helper Method: Entity -> DTO Mapping
    private ClaimResponse mapToResponse(ExpenseClaims claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getEmployee().getId(),
                claim.getEmployee().getName(),
                claim.getEmployee().getRole().name(),
                claim.getAmount(),
                claim.getCategory(),
                claim.getDescription(),
                claim.getStatus(),
                claim.getReceiptImagePath(),
                claim.getFlaggedReason(),
                claim.getCreatedAt()
        );
    }
}