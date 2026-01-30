package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.VendorPricingType;
import com.autoadda.apis.other.util.VendorQuotationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for vendor quotation details.
 */
@Data
@Builder
public class VendorQuotationResponse {
    private Integer id;
    private String quoteNumber;
    private Integer quotationRequestId;
    private String quotationNumber;
    private Integer vendorId;
    private String vendorName;
    private VendorPricingType pricingType;
    private Double totalAmount;
    private Double minAmount;
    private Double maxAmount;
    private Double inspectionFee;
    private String estimatedDuration;
    private String warrantyInfo;
    private LocalDate validUntil;
    private VendorQuotationStatus status;
    private String vendorNotes;
    private String rejectionReason;
    private List<LineItemResponse> lineItems;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    @Data
    @Builder
    public static class LineItemResponse {
        private Integer id;
        private String itemType;
        private String title;
        private String description;
        private Integer quantity;
        private String unit;
        private Double unitPrice;
        private Double discount;
        private Double taxRate;
        private Double totalPrice;
        private Boolean isOptional;
    }
}
