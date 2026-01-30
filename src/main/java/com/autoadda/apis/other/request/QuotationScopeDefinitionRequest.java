package com.autoadda.apis.other.request;

import com.autoadda.apis.other.util.QuotationType;
import lombok.Data;

/**
 * Request DTO for creating/updating quotation scope definitions.
 */
@Data
public class QuotationScopeDefinitionRequest {
    private Integer serviceCategoryId;
    private Boolean isQuotationEnabled;
    private QuotationType quotationType;
    private Integer autoExpiryHours;
    private Boolean allowMultiVendorBroadcast;
    private Integer maxBroadcastVendors;
    private Integer minQuotesRequired;
}
