package com.pehchaan.backend.controller;

import com.pehchaan.backend.dto.gig.GigRequest;
import com.pehchaan.backend.dto.gig.GigResponse;
import com.pehchaan.backend.dto.gig.InvoiceRequest;
import com.pehchaan.backend.dto.gig.PaymentRequest;
import com.pehchaan.backend.service.GigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gigs")
@RequiredArgsConstructor
public class GigController {

    private final GigService gigService;

    /**
     * POST /api/gigs/request
     * Consumer requests a gig from a laborer.
     */
    @PostMapping("/request")
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<GigResponse> requestGig(@RequestBody GigRequest request) {
        return ResponseEntity.ok(gigService.requestGig(request));
    }

    /**
     * GET /api/gigs/my-gigs
     * Gets all gigs for the logged-in user (both consumer and laborer).
     */
    @GetMapping("/my-gigs")
    @PreAuthorize("hasAnyRole('CONSUMER', 'LABOR')")
    public ResponseEntity<List<GigResponse>> getMyGigs() {
        return ResponseEntity.ok(gigService.getMyGigs());
    }

    /**
     * POST /api/gigs/{id}/accept
     * Laborer accepts an incoming gig request.
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('LABOR')")
    public ResponseEntity<GigResponse> acceptGig(@PathVariable Long id) {
        return ResponseEntity.ok(gigService.acceptGig(id));
    }

    /**
     * POST /api/gigs/{id}/start
     * Laborer marks the gig as in-progress.
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('LABOR')")
    public ResponseEntity<GigResponse> startWork(@PathVariable Long id) {
        return ResponseEntity.ok(gigService.startWork(id));
    }

    /**
     * POST /api/gigs/{id}/complete
     * Laborer completes the gig and adds additional invoice charges.
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('LABOR')")
    public ResponseEntity<GigResponse> completeGig(
            @PathVariable Long id,
            @RequestBody InvoiceRequest request
    ) {
        return ResponseEntity.ok(gigService.completeAndInvoiceGig(id, request));
    }

    /**
     * POST /api/gigs/{id}/pay
     * Consumer or Laborer marks the gig as paid.
     */
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('CONSUMER', 'LABOR')")
    public ResponseEntity<GigResponse> markAsPaid(
            @PathVariable Long id,
            @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.ok(gigService.markAsPaid(id, request));
    }
}