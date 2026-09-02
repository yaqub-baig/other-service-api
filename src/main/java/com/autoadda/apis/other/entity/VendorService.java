package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One service a {@link Vendor} offers (e.g. a car wash, a battery shop).
 *
 * <p>A vendor may have many of these, each with its own working days/hours,
 * capacity, trade licence, branch address, branch images and booking slots.
 * This is the unit that bookings and the per-service schedule attach to.</p>
 *
 * <p>DB table kept as {@code vendor_service_bridge} for continuity.</p>
 */
@Entity
@Table(name = "vendor_service_bridge")
@Getter
@Setter
public class VendorService {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    @JsonBackReference("vendor-services")
    private Vendor vendor;

    @ManyToOne
    @JoinColumn(name = "service_category_id")
    private ServiceCategory serviceCategory;

    /** {@code VendorServiceType.name()} — redundant with serviceCategory but convenient for filters. */
    @Column(name = "service_type")
    private String serviceType;

    // ---- operating window (summary strings; the real schedule is VendorWorkHours) ----
    @Column(name = "opening_time")
    private String openingTime;

    @Column(name = "closing_time")
    private String closingTime;

    @Column(name = "working_days")
    private String workingDays;

    @Column(name = "concurrent_appointment_capacity")
    private Integer concurrentAppointmentCapacity;

    // ---- per-service trade licence ----
    @Column(name = "trade_licence_number")
    private String tradeLicenceNumber;

    @OneToMany
    @JoinColumn(name = "service_trade_license")
    private List<MediaFile> tradeLicenseAttachment = new ArrayList<>();

    // ---- per-branch address + gallery ----
    @OneToOne
    @JoinColumn(name = "service_location_id")
    private Location serviceLocation;

    @OneToMany
    @JoinColumn(name = "service_images")
    private List<MediaFile> serviceImages = new ArrayList<>();

    // ---- lifecycle ----
    @Column(name = "account_status")
    private Status accountStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}
