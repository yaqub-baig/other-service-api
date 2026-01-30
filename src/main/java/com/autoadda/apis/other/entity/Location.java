package com.autoadda.apis.other.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a physical location with address and geo-coordinates.
 * Supports geo-spatial queries for distance-based search.
 */
@Entity
@Table(name = "location",
       indexes = {
           @Index(name = "idx_location_city", columnList = "city"),
           @Index(name = "idx_location_area", columnList = "area"),
           @Index(name = "idx_location_coordinates", columnList = "latitude, longitude")
       })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "street_address_1")
    private String streetAddress1;

    @Column(name = "street_address_2")
    private String streetAddress2;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "area")
    private String area;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "gmap")
    private String gmap;

    @Column(name = "type")
    private String type;

    /**
     * Latitude coordinate for geo-spatial queries.
     * Example: 25.2048 (Dubai)
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Longitude coordinate for geo-spatial queries.
     * Example: 55.2708 (Dubai)
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Check if this location has valid geo-coordinates.
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}