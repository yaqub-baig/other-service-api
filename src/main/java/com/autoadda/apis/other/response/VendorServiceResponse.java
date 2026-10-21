package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.PricingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for the VendorService entity (vendor ↔ service category).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorServiceResponse {
    private Integer id;
    private Boolean isActive;
    private Integer vendorId;
    private String vendorName;
    private Integer serviceCategoryId;
    private String serviceCategoryName;
    private PricingType pricingType;
    private BigDecimal price;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String currency;
    private String description;
}
