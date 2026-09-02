package com.autoadda.apis.other.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Car-wash-specific attributes for a {@link Vendor}.
 *
 * <p>Template for the per-service detail table: shared vendor columns live on
 * {@code vendor}; anything specific to one service type lives in a 1:1 detail
 * table keyed by {@code vendor_id}. Each new service adds its own
 * {@code <service>_details} entity the same way.</p>
 */
@Entity
@Table(name = "car_wash_details")
@Getter
@Setter
@NoArgsConstructor
public class CarWashDetails {

    @Id
    @Column(name = "vendor_id")
    private Integer vendorId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    /** Number of physical wash bays. */
    @Column(name = "bay_count")
    private Integer bayCount;

    /** Typical wash duration in minutes (used as the default slot length). */
    @Column(name = "average_wash_duration_minutes")
    private Integer averageWashDurationMinutes;

    @Column(name = "mobile_service")
    private Boolean mobileService;

    @Column(name = "self_service_available")
    private Boolean selfServiceAvailable;

    @Column(name = "express_wash_available")
    private Boolean expressWashAvailable;

    @Column(name = "interior_detailing_available")
    private Boolean interiorDetailingAvailable;

    @Column(name = "water_recycling")
    private Boolean waterRecycling;

    @Column(name = "accepts_walk_ins")
    private Boolean acceptsWalkIns;
}
