package com.pehchaan.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assignments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "laborer_id"}))
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laborer_id", nullable = false)
    private User laborer;

    @Column(nullable = false)
    private BigDecimal wageRate;

    @Column(nullable = false)
    private String wageType; // "DAILY", "HOURLY"
}