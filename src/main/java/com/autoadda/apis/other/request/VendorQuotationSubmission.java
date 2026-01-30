package com.autoadda.apis.other.request;

import com.autoadda.apis.other.util.VendorPricingType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for vendor submitting a quotation.
 */
@Data
public class VendorQuotationSubmission {
    private Integer quotationRequestId;
    private Integer vendorId;
    private VendorPricingType pricingType;
    private BigDecimal totalAmount;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal inspectionFee;
    private String estimatedDuration;
    private String warrantyInfo;
    private LocalDate validUntil;
    private String vendorNotes;
    private List<LineItemSubmission> lineItems;

    @Data
    public static class LineItemSubmission {
        private String itemType;
        private String title;
        private String description;
        private Integer quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private BigDecimal taxRate;
        private Boolean isOptional;
    }
}
