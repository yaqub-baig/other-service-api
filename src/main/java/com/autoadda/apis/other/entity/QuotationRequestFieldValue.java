package com.autoadda.apis.other.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores the value of a dynamic field submitted by a customer in a quotation request.
 * Values are stored as JSON to accommodate any data type.
 */
@Entity
@Table(name = "quotation_request_field_values",
       indexes = {
           @Index(name = "idx_qrfv_request_id", columnList = "quotation_request_id"),
           @Index(name = "idx_qrfv_field_code", columnList = "field_code")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationRequestFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quotation_request_id", nullable = false)
    private QuotationRequest quotationRequest;

    /**
     * The field code corresponding to QuotationFieldDefinition.fieldCode.
     */
    @Column(name = "field_code", nullable = false, length = 50)
    private String fieldCode;

    /**
     * The field label for display purposes.
     */
    @Column(name = "field_label", length = 100)
    private String fieldLabel;

    /**
     * The value submitted by the customer.
     * Stored as JSON to support any type (text, number, array for multi-select, file URL, etc.)
     */
    @Column(name = "field_value", columnDefinition = "JSON")
    private String fieldValue;

    /**
     * For file uploads, the URL of the uploaded file.
     */
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
