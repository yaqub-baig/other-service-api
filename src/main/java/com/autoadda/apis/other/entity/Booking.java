package com.autoadda.apis.other.entity;


import com.autoadda.apis.other.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "appointment_reference_id")
    private String appointmentReferenceId;

    @Column(name = "schedule_date")
    private LocalDate scheduleDate;

    @Column(name = "service_start_time")
    private LocalTime serviceStartTime;

    @Column(name = "service_end_time")
    private LocalTime serviceEndTime;

    @Column(name = "delivery_time")
    private LocalTime deliveryTime;

    @Column(name = "status")
    private Status status;

    @Column(name = "comments")
    private String comments;

    @Column(name = "insert_tms")
    private LocalDate insert_tms;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    /**
     * Catalog service booked (optional for bookings made before services existed).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private CatalogService catalogService;

    /**
     * Schema version of the catalog service this was validated against. Stays put when a newer
     * version is published.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schema_version_id")
    private ServiceSchemaVersion schemaVersion;

    /** Comma-separated vendor service ids picked at booking time. */
    @Column(name = "selected_service_ids")
    private String selectedServiceIds;

    @Column(name = "os_subscription_plan_id")
    private String osSubscriptionPlanId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "vehicle_id")
    private Integer vehicleId;

    @ManyToOne
    private BookingSlot slot;

    @Column(name = "technician_id", nullable = true)
    private Integer technicianId;

    // ==================== Quotation Reference ====================

    /**
     * Type of reference (e.g., "QUOTATION", "DIRECT_BOOKING").
     */
    @Column(name = "reference_type", length = 30)
    private String referenceType;

    /**
     * ID of the referenced entity (e.g., VendorQuotation ID).
     */
    @Column(name = "reference_id")
    private Integer referenceId;

    // ==================== Pricing Details ====================

    /**
     * Subtotal before taxes and discounts.
     */
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Tax amount.
     */
    @Column(name = "taxes", precision = 10, scale = 2)
    private BigDecimal taxes;

    /**
     * Discount amount.
     */
    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount;

    /**
     * Final total amount.
     */
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
}
