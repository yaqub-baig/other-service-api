package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.FulfillmentMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A canonical service in the catalog, e.g. "Tire Replacement" under "Tires & Wheels".
 * Sits between ServiceCategory and the vendor's ServiceOffering, and anchors
 * service-level attributes, quotation settings and market availability.
 * Named CatalogService (table "services") to avoid clashing with Spring's @Service.
 */
@Entity
@Table(name = "services",
       indexes = @Index(name = "idx_services_category_active", columnList = "service_category_id, is_active, display_order"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    /**
     * Stable machine code, e.g. "TIRE_REPLACEMENT". Unique across the catalog.
     */
    @Column(name = "code", nullable = false, unique = true, length = 80)
    private String code;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_mode", nullable = false, length = 20)
    @Builder.Default
    private FulfillmentMode fulfillmentMode = FulfillmentMode.WORKSHOP;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

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
