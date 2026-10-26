package com.autoadda.apis.other.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Whether a catalog service is offered in a country, and in which currency it is priced there.
 * Availability is not proof of local regulatory compliance (e.g. window tint limits); record
 * country rules in regulatoryConfig and the notes.
 */
@Entity
@Table(name = "service_market_availability",
       uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "country_code"}),
       indexes = @Index(name = "idx_market_country_available", columnList = "country_code, is_available"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceMarketAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private CatalogService catalogService;

    /** ISO 3166-1 alpha-2, e.g. "AE". */
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    /** ISO 4217, e.g. "AED". Offerings for this service in this country must use it. */
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    /** Country-specific rules as JSON, e.g. {"maxTintPercent": 50}. */
    @Column(name = "regulatory_config", columnDefinition = "JSON")
    private String regulatoryConfig;

    @Column(name = "regulatory_note", length = 600)
    private String regulatoryNote;

    @Column(name = "regulatory_note_ar", length = 600)
    private String regulatoryNoteAr;

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
