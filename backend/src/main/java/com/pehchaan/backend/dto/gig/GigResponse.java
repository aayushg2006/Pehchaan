package com.pehchaan.backend.dto.gig;

import com.pehchaan.backend.entity.Gig;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GigResponse {
    private Long id;
    private Gig.GigStatus status;
    private Gig.PaymentMethod paymentMethod;
    private String skill;

    // Consumer info
    private Long consumerId;
    private String consumerName;
    private double consumerLatitude;
    private double consumerLongitude;
    private String consumerAddress;

    // Laborer info
    private Long laborerId;
    private String laborerName;

    // Time & Money
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime workStartedAt;
    private LocalDateTime completedAt;
    private LocalDateTime paidAt;

    private BigDecimal visitingCharge;
    private BigDecimal totalAmount;
    private Integer rating;

    public static GigResponse fromEntity(Gig gig) {
        return GigResponse.builder()
                .id(gig.getId())
                .status(gig.getStatus())
                .paymentMethod(gig.getPaymentMethod())
                .skill(gig.getSkill())
                .consumerId(gig.getConsumer().getId())
                .consumerName(gig.getConsumer().getFirstName() + " " + gig.getConsumer().getLastName())
                .consumerLatitude(gig.getConsumerLocation().getY()) // Lat is Y
                .consumerLongitude(gig.getConsumerLocation().getX()) // Lon is X
                .consumerAddress(gig.getConsumerAddress())
                .laborerId(gig.getLaborer().getId())
                .laborerName(gig.getLaborer().getFirstName() + " " + gig.getLaborer().getLastName())
                .createdAt(gig.getCreatedAt())
                .acceptedAt(gig.getAcceptedAt())
                .workStartedAt(gig.getWorkStartedAt())
                .completedAt(gig.getCompletedAt())
                .paidAt(gig.getPaidAt())
                .visitingCharge(gig.getVisitingCharge())
                .totalAmount(gig.getTotalAmount())
                .rating(gig.getRating())
                .build();
    }
}