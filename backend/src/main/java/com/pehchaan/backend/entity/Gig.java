package com.pehchaan.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gigs")
public class Gig {

    public enum GigStatus {
        REQUESTED,      // Consumer has sent the request
        ACCEPTED,       // Laborer has accepted (Visiting charge is now due)
        IN_PROGRESS,    // Laborer has arrived and started work
        PENDING_PAYMENT,// Laborer has submitted final invoice
        COMPLETED,      // Consumer has paid
        CANCELLED       // Request was cancelled by either party
    }

    public enum PaymentMethod {
        CASH,
        ONLINE,
        PENDING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    private User consumer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laborer_id", nullable = false)
    private User laborer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GigStatus status;

    @Column(nullable = false)
    private String skill; // e.g., "PLUMBER"

    // This is the location of the job (the consumer's location)
    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point consumerLocation;

    @Column(nullable = false)
    private String consumerAddress; // The full address string

    // --- Price Breakdown ---
    @Column(nullable = false)
    private BigDecimal visitingCharge; // e.g., 110.00

    @Column(nullable = false)
    private BigDecimal platformFee; // e.g., 10.00

    @Column(nullable = false)
    private BigDecimal laborerVisitingPayout; // e.g., 100.00

    // This is the total amount to be paid
    @Column(nullable = false)
    private BigDecimal totalAmount; // Starts as 110, can be increased

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.PENDING;

    private Integer rating; // Consumer can add this after completion

    // --- Timestamps ---
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime acceptedAt;
    private LocalDateTime workStartedAt;
    private LocalDateTime completedAt; // When invoice is submitted
    private LocalDateTime paidAt; // When payment is confirmed
}