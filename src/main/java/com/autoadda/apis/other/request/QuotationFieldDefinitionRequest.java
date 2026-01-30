package com.autoadda.apis.other.request;

import com.autoadda.apis.other.util.FieldType;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating/updating quotation field definitions.
 */
@Data
public class QuotationFieldDefinitionRequest {
    private Integer serviceCategoryId;
    private String fieldCode;
    private String label;
    private FieldType fieldType;
    private Boolean isRequired;
    private Integer displayOrder;
    private String placeholder;
    private String helpText;
    private String defaultValue;
    private List<String> options;
    private Map<String, Object> validationRules;
    private Integer dependsOnFieldId;
    private Map<String, Object> dependsOnCondition;
}
