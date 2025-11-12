package com.pehchaan.backend.entity;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import jakarta.persistence.Column; // ✅ --- THIS IS THE FIX ---
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_logs")
public class WorkLog {

    public enum WorkStatus {
        PENDING_APPROVAL,
        APPROVED,
        PAID,
        DISPUTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the Project this log is for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Link to the Laborer this log is for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laborer_id", nullable = false)
    private User laborer;

    @Column(nullable = false)
    private LocalDateTime checkInTime;
    
    private LocalDateTime checkOutTime; // This will be NULL until the user checks out

    // Using BigDecimal is the standard, safest way to handle money in Java
    private BigDecimal wageEarned;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkStatus status;
}