package com.pehchaan.backend.entity;

import org.locationtech.jts.geom.Point; // This is the PostGIS Point type

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    // This is the CRUCIAL field for our geofence.
    // It tells the database to use a "geometry" type column to store the (lat, lon)
    // 4326 is the standard "WGS 84" code for world GPS coordinates.
    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;
    
    // We link the project to the contractor (a User) who created it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id", nullable = false)
    private User contractor;
    
    // We'll also add wage details here
    @Column(nullable = false)
    private BigDecimal wageRate; // e.g., 800

    @Column(nullable = false)
    private String wageType; // e.g., "DAILY", "HOURLY"
}