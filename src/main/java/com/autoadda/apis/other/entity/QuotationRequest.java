package com.autoadda.apis.other.entity;

import com.autoadda.apis.other.util.QuotationRequestStatus;
import com.autoadda.apis.other.util.ServiceLocationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer's request for quotation from vendors.
 * Contains all the details submitted by the customer including dynamic field values.
 */
@Entity
@Table(name = "quotation_requests",
       indexes = {
           @Index(name = "idx_qr_customer_id", columnList = "customer_id"),
           @Index(name = "idx_qr_status", columnList = "status"),
           @Index(name = "idx_qr_service_category", columnList = "service_category_id"),
           @Index(name = "idx_qr_expires_at", columnList = "expires_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Unique quotation request number for reference.
     */
    @Column(name = "quotation_number", unique = true, nullable = false, length = 20)
    private String quotationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    /**
     * Customer ID from the user service.
     */
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    /**
     * Customer's preferred service location.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "service_location_type", length = 20)
    private ServiceLocationType serviceLocationType;

    /**
     * City for geo-search matching.
     */
    @Column(name = "city", length = 100)
    private String city;

    /**
     * Area/neighborhood for geo-search matching.
     */
    @Column(name = "area", length = 100)
    private String area;

    /**
     * Full address if service is at customer location.
     */
    @Column(name = "address", length = 500)
    private String address;

    /**
     * Latitude for proximity search.
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Longitude for proximity search.
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Current status of the quotation request.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private QuotationRequestStatus status = QuotationRequestStatus.REQUESTED;

    /**
     * Additional notes from the customer.
     */
    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    /**
     * When this request expires (auto-calculated from scope definition).
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Number of vendors this request was broadcast to.
     */
    @Column(name = "broadcast_count")
    @Builder.Default
    private Integer broadcastCount = 0;

    /**
     * Number of quotes received.
     */
    @Column(name = "quotes_received_count")
    @Builder.Default
    private Integer quotesReceivedCount = 0;

    /**
     * Dynamic field values submitted by the customer.
     */
    @OneToMany(mappedBy = "quotationRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuotationRequestFieldValue> fieldValues = new ArrayList<>();

    /**
     * Vendor quotations received for this request.
     */
    @OneToMany(mappedBy = "quotationRequest", cascade = CascadeType.ALL)
    @Builder.Default
    private List<VendorQuotation> vendorQuotations = new ArrayList<>();

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
     * Helper method to add a field value.
     */
    public void addFieldValue(QuotationRequestFieldValue fieldValue) {
        fieldValues.add(fieldValue);
        fieldValue.setQuotationRequest(this);
    }

    /**
     * Helper method to remove a field value.
     */
    public void removeFieldValue(QuotationRequestFieldValue fieldValue) {
        fieldValues.remove(fieldValue);
        fieldValue.setQuotationRequest(null);
    }
}
