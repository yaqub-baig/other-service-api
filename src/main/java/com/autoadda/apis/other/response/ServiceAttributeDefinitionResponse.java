package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.DataType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAttributeDefinitionResponse {
    private Long id;
    private Integer serviceCategoryId;
    private String serviceCategoryName;
    private String attributeCode;
    private String attributeName;
    private DataType dataType;
    private String unit;
    private Boolean isRequired;
    private Boolean isFilterable;
    private Boolean isSearchable;
    private Integer displayOrder;
    private String description;
    private String defaultValue;
    private String validationRules;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
