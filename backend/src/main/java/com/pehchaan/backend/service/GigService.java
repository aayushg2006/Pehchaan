package com.pehchaan.backend.service;

import com.pehchaan.backend.dto.gig.GigRequest;
import com.pehchaan.backend.dto.gig.GigResponse;
import com.pehchaan.backend.dto.gig.InvoiceRequest;
import com.pehchaan.backend.dto.gig.PaymentRequest;
import com.pehchaan.backend.entity.Gig;
import com.pehchaan.backend.entity.User;
import com.pehchaan.backend.repository.GigRepository;
import com.pehchaan.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GigService {

    private final GigRepository gigRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory;

    // --- Define constants for your business logic ---
    private static final BigDecimal VISITING_CHARGE = new BigDecimal("110.00");
    private static final BigDecimal PLATFORM_FEE = new BigDecimal("10.00");
    private static final BigDecimal LABORER_VISITING_PAYOUT = new BigDecimal("100.00");
    private static final List<Gig.GigStatus> ACTIVE_GIG_STATUSES = List.of(
            Gig.GigStatus.REQUESTED,
            Gig.GigStatus.ACCEPTED,
            Gig.GigStatus.IN_PROGRESS
    );

    /**
     * Consumer requests a gig from a specific laborer.
     */
    @Transactional
    public GigResponse requestGig(GigRequest request) {
        User consumer = getAuthenticatedUser();
        User laborer = userRepository.findById(request.getLaborerId())
                .orElseThrow(() -> new IllegalArgumentException("Laborer not found"));

        if (request.getAddress() == null || request.getAddress().isBlank()) {
            throw new IllegalArgumentException("Address is required.");
        }

        // --- Validation Checks ---
        if (!"AVAILABLE".equals(laborer.getStatus())) {
            throw new IllegalStateException("This worker is no longer available.");
        }
        gigRepository.findActiveGigForLaborer(laborer.getId(), ACTIVE_GIG_STATUSES)
                .ifPresent(g -> {
                    throw new IllegalStateException("This worker is already handling a request.");
                });
        gigRepository.findActiveGigForConsumer(consumer.getId(), ACTIVE_GIG_STATUSES)
                .ifPresent(g -> {
                    throw new IllegalStateException("You already have an active gig request.");
                });

        // --- Create the Gig ---
        Point locationPoint = geometryFactory.createPoint(
            new Coordinate(request.getLongitude(), request.getLatitude())
        );

        Gig newGig = Gig.builder()
                .consumer(consumer)
                .laborer(laborer)
                .skill(request.getSkill().toUpperCase())
                .consumerLocation(locationPoint)
                .consumerAddress(request.getAddress())
                .status(Gig.GigStatus.REQUESTED)
                .visitingCharge(VISITING_CHARGE)
                .platformFee(PLATFORM_FEE)
                .laborerVisitingPayout(LABORER_VISITING_PAYOUT)
                .totalAmount(VISITING_CHARGE) // The initial total is just the visiting charge
                .paymentMethod(Gig.PaymentMethod.PENDING)
                .build();

        Gig savedGig = gigRepository.save(newGig);
        return GigResponse.fromEntity(savedGig);
    }

    /**
     * Laborer accepts an incoming gig request.
     */
    @Transactional
    public GigResponse acceptGig(Long gigId) {
        User laborer = getAuthenticatedUser();
        Gig gig = getGigForLaborer(gigId, laborer.getId());

        if (gig.getStatus() != Gig.GigStatus.REQUESTED) {
            throw new IllegalStateException("This gig is no longer in a 'REQUESTED' state.");
        }

        // --- Update State ---
        gig.setStatus(Gig.GigStatus.ACCEPTED);
        gig.setAcceptedAt(LocalDateTime.now());
        laborer.setStatus("OFFLINE"); // Laborer is now busy
        userRepository.save(laborer);

        Gig savedGig = gigRepository.save(gig);
        return GigResponse.fromEntity(savedGig);
    }

    /**
     * Laborer marks the gig as "In Progress" (e.g., they have arrived).
     */
    @Transactional
    public GigResponse startWork(Long gigId) {
        User laborer = getAuthenticatedUser();
        Gig gig = getGigForLaborer(gigId, laborer.getId());

        if (gig.getStatus() != Gig.GigStatus.ACCEPTED) {
            throw new IllegalStateException("Gig must be in 'ACCEPTED' state to start work.");
        }

        gig.setStatus(Gig.GigStatus.IN_PROGRESS);
        gig.setWorkStartedAt(LocalDateTime.now());
        Gig savedGig = gigRepository.save(gig);
        return GigResponse.fromEntity(savedGig);
    }

    /**
     * Laborer adds additional charges and submits the final invoice.
     */
    @Transactional
    public GigResponse completeAndInvoiceGig(Long gigId, InvoiceRequest request) {
        User laborer = getAuthenticatedUser();
        Gig gig = getGigForLaborer(gigId, laborer.getId());

        if (!List.of(Gig.GigStatus.ACCEPTED, Gig.GigStatus.IN_PROGRESS).contains(gig.getStatus())) {
            throw new IllegalStateException("This gig cannot be completed from its current state.");
        }

        BigDecimal additionalAmount = request.getAdditionalAmount() != null ? request.getAdditionalAmount() : BigDecimal.ZERO;
        if (additionalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Additional amount cannot be negative.");
        }

        // --- Update Gig State ---
        gig.setStatus(Gig.GigStatus.PENDING_PAYMENT);
        // The total is the visiting charge PLUS any additional amount
        gig.setTotalAmount(gig.getVisitingCharge().add(additionalAmount));
        gig.setCompletedAt(LocalDateTime.now());

        Gig savedGig = gigRepository.save(gig);
        return GigResponse.fromEntity(savedGig);
    }

    /**
     * Consumer or Laborer marks the gig as paid (e.g., Cash payment).
     */
    @Transactional
    public GigResponse markAsPaid(Long gigId, PaymentRequest request) {
        User user = getAuthenticatedUser();
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));

        // Security check: Only consumer or laborer on the gig can mark it as paid
        if (!gig.getLaborer().getId().equals(user.getId()) && !gig.getConsumer().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to update this gig.");
        }

        if (gig.getStatus() != Gig.GigStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Gig is not pending payment.");
        }

        gig.setStatus(Gig.GigStatus.COMPLETED);
        gig.setPaymentMethod(request.getPaymentMethod());
        gig.setPaidAt(LocalDateTime.now());

        Gig savedGig = gigRepository.save(gig);
        return GigResponse.fromEntity(savedGig);
    }


    /**
     * Gets all gigs for the currently logged-in user (both consumer and laborer).
     */
    public List<GigResponse> getMyGigs() {
        User user = getAuthenticatedUser();
        List<Gig> gigs;

        if (user.getRole() == com.pehchaan.backend.entity.Role.ROLE_LABOR) {
            gigs = gigRepository.findByLaborerIdOrderByCreatedAtDesc(user.getId());
        } else if (user.getRole() == com.pehchaan.backend.entity.Role.ROLE_CONSUMER) {
            gigs = gigRepository.findByConsumerIdOrderByCreatedAtDesc(user.getId());
        } else {
            gigs = List.of(); // Empty list for other roles (like contractor)
        }

        return gigs.stream()
                .map(GigResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---
    private User getAuthenticatedUser() {
        String userPhone = SecurityContextHolder.getContext()
                                                .getAuthentication()
                                                .getName();
        return userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userPhone));
    }

    private Gig getGigForLaborer(Long gigId, Long laborerId) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new IllegalArgumentException("Gig not found"));
        if (!gig.getLaborer().getId().equals(laborerId)) {
            throw new AccessDeniedException("You are not authorized to access this gig.");
        }
        return gig;
    }
}