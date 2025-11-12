package com.pehchaan.backend.service;

import com.pehchaan.backend.dto.profile.ProfileResponse;
import com.pehchaan.backend.repository.WorkerSearchRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerSearchRepository workerSearchRepository;
    private final GeometryFactory geometryFactory;

    /**
     * Finds nearby available workers based on skill and consumer's location.
     */
    public List<ProfileResponse> findNearbyWorkers(String skill, double latitude, double longitude) {
        // 1. Create a PostGIS Point for the consumer's location
        Point consumerLocation = geometryFactory.createPoint(
            new Coordinate(longitude, latitude)
        );

        // 2. Call the repository to find workers
        return workerSearchRepository.findNearbyAvailableWorkers(skill, consumerLocation)
                .stream()
                .map(ProfileResponse::fromEntity) // Convert User entities to safe DTOs
                .collect(Collectors.toList());
    }
}