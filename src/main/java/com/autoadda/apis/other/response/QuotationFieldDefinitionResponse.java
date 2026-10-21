package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuotationFieldDefinitionResponse {
    private Integer id;
    private Integer serviceCategoryId; // Only ID to avoid circular dependency
    private String serviceCategoryName; // For display purposes
    private Integer serviceId;
    private String serviceCode;
    private String fieldCode;
    private String label;
    private FieldType fieldType;
    private Boolean isRequired;
    private Integer displayOrder;
    private String placeholder;
    private String helpText;
    private String defaultValue;
    private String options; // Raw JSON string
    private String validationRules; // Raw JSON string
    private Integer dependsOnFieldId; // Only ID of the dependent field
    private String dependsOnCondition; // Raw JSON string
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
