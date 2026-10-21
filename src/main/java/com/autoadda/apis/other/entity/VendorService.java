package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.PricingType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * A vendor offering a service category: the single vendor ↔ category join.
 * Bookings, booked services and offerings all hang off this row.
 * (Previously split across two entities, VendorService and VendorServiceBridge.)
 */
@Entity
@Table(name = "vendor_service_bridge",
       uniqueConstraints = @UniqueConstraint(columnNames = {"vendor_id", "service_category_id"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonBackReference("vendor-services")
    private Vendor vendor;

    @ManyToOne
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    /**
     * The pricing strategy for this service.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", length = 20)
    @Builder.Default
    private PricingType pricingType = PricingType.FIXED;

    /**
     * The base/fixed price (used when pricingType is FIXED).
     */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Minimum price (used when pricingType is RANGE).
     */
    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    /**
     * Maximum price (used when pricingType is RANGE).
     */
    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;

    /**
     * Currency code (ISO 4217).
     */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "AED";

    /**
     * Description of the service offered by this vendor.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Average rating for this vendor service.
     */
    @Column(name = "average_rating", precision = 2, scale = 1)
    private BigDecimal averageRating;

    /**
     * Total number of ratings received.
     */
    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;
}
