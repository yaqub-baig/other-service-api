package com.autoadda.apis.other.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an individual line item in a vendor's quotation.
 * Used for detailed pricing breakdowns (parts, labor, etc.).
 */
@Entity
@Table(name = "vendor_quotation_line_items",
       indexes = {
           @Index(name = "idx_vqli_quotation_id", columnList = "vendor_quotation_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorQuotationLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_quotation_id", nullable = false)
    private VendorQuotation vendorQuotation;

    /**
     * Type of line item. Example: "PART", "LABOR", "SERVICE", "FEE"
     */
    @Column(name = "item_type", length = 20)
    private String itemType;

    /**
     * Title/name of the line item.
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Detailed description of the item.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Quantity of items.
     */
    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 1;

    /**
     * Unit of measurement. Example: "pcs", "hours", "liters"
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * Price per unit.
     */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Discount amount (if any).
     */
    @Column(name = "discount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    /**
     * Tax rate (percentage).
     */
    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    /**
     * Total price for this line item (quantity * unitPrice - discount + tax).
     */
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Display order in the quotation.
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Whether this item is optional (customer can choose to exclude).
     */
    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateTotalPrice();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotalPrice();
    }

    /**
     * Calculate total price from unit price, quantity, discount, and tax.
     */
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal discountAmount = discount != null ? discount : BigDecimal.ZERO;
            BigDecimal afterDiscount = subtotal.subtract(discountAmount);
            BigDecimal taxAmount = taxRate != null ?
                afterDiscount.multiply(taxRate).divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
            this.totalPrice = afterDiscount.add(taxAmount);
        }
    }
}
