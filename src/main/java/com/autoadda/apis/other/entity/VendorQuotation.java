package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.VendorPricingType;
import com.autoadda.apis.other.util.VendorQuotationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a vendor's response/quotation to a customer's quotation request.
 * Contains pricing details, line items, and validity information.
 */
@Entity
@Table(name = "vendor_quotations",
       indexes = {
           @Index(name = "idx_vq_request_id", columnList = "quotation_request_id"),
           @Index(name = "idx_vq_vendor_id", columnList = "vendor_id"),
           @Index(name = "idx_vq_status", columnList = "status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Unique quotation reference number.
     */
    @Column(name = "quote_number", unique = true, nullable = false, length = 20)
    private String quoteNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quotation_request_id", nullable = false)
    private QuotationRequest quotationRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    /**
     * Type of pricing provided by vendor.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false, length = 25)
    private VendorPricingType pricingType;

    /**
     * Total amount for FIXED pricing.
     */
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Minimum amount for RANGE pricing.
     */
    @Column(name = "min_amount", precision = 10, scale = 2)
    private BigDecimal minAmount;

    /**
     * Maximum amount for RANGE pricing.
     */
    @Column(name = "max_amount", precision = 10, scale = 2)
    private BigDecimal maxAmount;

    /**
     * Inspection fee for INSPECTION_REQUIRED pricing.
     */
    @Column(name = "inspection_fee", precision = 10, scale = 2)
    private BigDecimal inspectionFee;

    /**
     * Estimated service duration. Example: "2-4 hours", "1-2 days"
     */
    @Column(name = "estimated_duration", length = 50)
    private String estimatedDuration;

    /**
     * Warranty information if applicable.
     */
    @Column(name = "warranty_info", length = 255)
    private String warrantyInfo;

    /**
     * Quote validity date.
     */
    @Column(name = "valid_until")
    private LocalDate validUntil;

    /**
     * Current status of the quotation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VendorQuotationStatus status = VendorQuotationStatus.DRAFT;

    /**
     * Vendor's notes or terms and conditions.
     */
    @Column(name = "vendor_notes", columnDefinition = "TEXT")
    private String vendorNotes;

    /**
     * Reason if quote was rejected by customer.
     */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    /**
     * When the quote was accepted/rejected by customer.
     */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    /**
     * Line item breakdown for BREAKUP pricing.
     */
    @OneToMany(mappedBy = "vendorQuotation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VendorQuotationLineItem> lineItems = new ArrayList<>();

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

    /**
     * Helper method to add a line item.
     */
    public void addLineItem(VendorQuotationLineItem lineItem) {
        lineItems.add(lineItem);
        lineItem.setVendorQuotation(this);
    }

    /**
     * Helper method to remove a line item.
     */
    public void removeLineItem(VendorQuotationLineItem lineItem) {
        lineItems.remove(lineItem);
        lineItem.setVendorQuotation(null);
    }

    /**
     * Calculate total from line items.
     */
    public BigDecimal calculateLineItemsTotal() {
        return lineItems.stream()
                .filter(li -> li.getTotalPrice() != null)
                .map(VendorQuotationLineItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
