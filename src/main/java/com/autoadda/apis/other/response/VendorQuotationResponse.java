package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.VendorPricingType;
import com.autoadda.apis.other.util.VendorQuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorQuotationResponse {
    private Integer id;
    private String quoteNumber;
    private Integer quotationRequestId; // ID of the parent request
    private VendorProfileResponse vendor; // Nested DTO
    private VendorPricingType pricingType;
    private BigDecimal totalAmount;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal inspectionFee;
    private String estimatedDuration;
    private String warrantyInfo;
    private LocalDate validUntil;
    private VendorQuotationStatus status;
    private String vendorNotes;
    private String rejectionReason;
    private LocalDateTime respondedAt;
    private List<VendorQuotationLineItemResponse> lineItems; // Nested DTOs
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}