package com.project.finAudit.Ai.entity;

import enums.EmployeeRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name="employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,length = 100)
    private String name;
    @Column(nullable = false,unique = true,length = 100)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EmployeeRole role;
    @Column(name = "monthly_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "spent_this_month", precision = 10, scale = 2)
    private BigDecimal spentThisMonth = BigDecimal.ZERO;

}
