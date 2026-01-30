package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.QuotationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuotationScopeDefinitionResponse {
    private Integer id;
    private Integer serviceCategoryId; // Only ID to avoid circular dependency
    private String serviceCategoryName; // For display purposes
    private Boolean isQuotationEnabled;
    private QuotationType quotationType;
    private Integer autoExpiryHours;
    private Boolean allowMultiVendorBroadcast;
    private Integer maxBroadcastVendors;
    private Integer minQuotesRequired;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
