package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.PricingType;
import com.autoadda.apis.other.util.StockStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a specific, sellable offering from a vendor.
 * This is the core entity for the marketplace - it represents what customers can actually buy.
 *
 * Example: A tyre shop (Vendor) that offers "Tyre Services" (VendorService)
 * might have specific offerings like:
 * - MRF ZLX 185/65 R15 Tyre
 * - Apollo Amazer 4G Life 165/80 R14 Tyre
 * - Wheel Alignment Service
 *
 * The filterableAttributes JSON stores the attributes that can be searched/filtered:
 * {
 *   "brand": "MRF",
 *   "width": "185",
 *   "height": "65",
 *   "rim_size": "15",
 *   "tyre_type": "Tubeless"
 * }
 */
@Entity
@Table(name = "service_offerings",
       indexes = {
           @Index(name = "idx_offering_vendor_service", columnList = "vendor_service_id"),
           @Index(name = "idx_offering_sku", columnList = "sku"),
           @Index(name = "idx_offering_status", columnList = "stock_status"),
           @Index(name = "idx_offering_active", columnList = "is_active")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The vendor service this offering belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_service_id", nullable = false)
    private VendorService vendorService;

    /**
     * Name of the offering (displayed to customers).
     * Example: "MRF ZLX 185/65 R15 Tubeless Car Tyre"
     */
    @Column(name = "offering_name", nullable = false, length = 255)
    private String offeringName;

    /**
     * Short description or tagline.
     */
    @Column(name = "short_description", length = 500)
    private String shortDescription;

    /**
     * Detailed description.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Stock Keeping Unit - unique identifier for this offering.
     */
    @Column(name = "sku", length = 50, unique = true)
    private String sku;

    /**
     * The pricing strategy for this offering.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false, length = 20)
    @Builder.Default
    private PricingType pricingType = PricingType.FIXED;

    /**
     * The base/fixed price.
     */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Minimum price (for RANGE pricing).
     */
    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    /**
     * Maximum price (for RANGE pricing).
     */
    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;

    /**
     * Original price (for showing discounts).
     */
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    /**
     * Currency code (ISO 4217).
     */
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "AED";

    /**
     * Current stock/availability status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false, length = 20)
    @Builder.Default
    private StockStatus stockStatus = StockStatus.IN_STOCK;

    /**
     * Quantity available (if applicable).
     */
    @Column(name = "quantity_available")
    private Integer quantityAvailable;

    /**
     * CRUCIAL: JSON field storing filterable/searchable attributes.
     * This is queried using MySQL JSON functions.
     *
     * Example for Tyre:
     * {"brand": "MRF", "width": "185", "height": "65", "rim_size": "15"}
     */
    @Column(name = "filterable_attributes", columnDefinition = "JSON")
    private String filterableAttributes;

    /**
     * Additional specifications as JSON.
     */
    @Column(name = "specifications", columnDefinition = "JSON")
    private String specifications;

    /**
     * Primary image URL.
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Additional images as JSON array.
     */
    @Column(name = "gallery_images", columnDefinition = "JSON")
    private String galleryImages;

    /**
     * Average rating for this offering.
     */
    @Column(name = "average_rating", precision = 2, scale = 1)
    private BigDecimal averageRating;

    /**
     * Total number of ratings received.
     */
    @Column(name = "total_ratings")
    @Builder.Default
    private Integer totalRatings = 0;

    /**
     * Total number of orders/bookings for this offering.
     */
    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;

    /**
     * Display order within the vendor's offerings.
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Whether this offering is featured/promoted.
     */
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    /**
     * Whether this offering is currently active.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
