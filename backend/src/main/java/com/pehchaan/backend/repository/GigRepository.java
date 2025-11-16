package com.pehchaan.backend.repository;

import com.pehchaan.backend.entity.Gig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GigRepository extends JpaRepository<Gig, Long> {

    // Find all gigs for a specific laborer, newest first
    List<Gig> findByLaborerIdOrderByCreatedAtDesc(Long laborerId);

    // Find all gigs for a specific consumer, newest first
    List<Gig> findByConsumerIdOrderByCreatedAtDesc(Long consumerId);

    // Find a laborer's gig that is currently active
    @Query("SELECT g FROM Gig g WHERE g.laborer.id = :laborerId AND g.status IN :statuses")
    Optional<Gig> findActiveGigForLaborer(Long laborerId, List<Gig.GigStatus> statuses);

    // Find a consumer's gig that is currently active
    @Query("SELECT g FROM Gig g WHERE g.consumer.id = :consumerId AND g.status IN :statuses")
    Optional<Gig> findActiveGigForConsumer(Long consumerId, List<Gig.GigStatus> statuses);
}