package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.QuotationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Defines the quotation strategy for a service category.
 * Controls whether quotations are required, optional, or hybrid for a category.
 */
@Entity
@Table(name = "quotation_scope_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationScopeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "service_category_id", unique = true)
    private ServiceCategory serviceCategory;

    /**
     * Whether quotation feature is enabled for this category.
     */
    @Column(name = "is_quotation_enabled", nullable = false)
    @Builder.Default
    private Boolean isQuotationEnabled = false;

    /**
     * Type of quotation strategy for this category.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "quotation_type", nullable = false, length = 20)
    private QuotationType quotationType;

    /**
     * Hours after which a quotation request auto-expires.
     */
    @Column(name = "auto_expiry_hours")
    @Builder.Default
    private Integer autoExpiryHours = 48;

    /**
     * Whether to broadcast quotation requests to multiple vendors.
     */
    @Column(name = "allow_multi_vendor_broadcast", nullable = false)
    @Builder.Default
    private Boolean allowMultiVendorBroadcast = true;

    /**
     * Maximum number of vendors to broadcast to (null = unlimited).
     */
    @Column(name = "max_broadcast_vendors")
    private Integer maxBroadcastVendors;

    /**
     * Minimum number of quotes required before customer can accept.
     */
    @Column(name = "min_quotes_required")
    @Builder.Default
    private Integer minQuotesRequired = 1;

    /**
     * Whether the scope definition is active.
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
