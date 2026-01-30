package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.QuotationType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for quotation scope and field definitions.
 */
@Data
@Builder
public class QuotationScopeResponse {
    private Integer id;
    private Integer serviceCategoryId;
    private String serviceCategoryName;
    private Boolean isQuotationEnabled;
    private QuotationType quotationType;
    private Integer autoExpiryHours;
    private Boolean allowMultiVendorBroadcast;
    private Integer maxBroadcastVendors;
    private Integer minQuotesRequired;
    private List<FieldDefinitionResponse> fields;

    @Data
    @Builder
    public static class FieldDefinitionResponse {
        private Integer id;
        private String fieldCode;
        private String label;
        private String fieldType;
        private Boolean isRequired;
        private Integer displayOrder;
        private String placeholder;
        private String helpText;
        private String defaultValue;
        private Object options;
        private Object validationRules;
        private Integer dependsOnFieldId;
        private Object dependsOnCondition;
    }
}
