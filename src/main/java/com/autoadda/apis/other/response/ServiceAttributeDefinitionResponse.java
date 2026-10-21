package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.DataType;
import com.autoadda.apis.other.util.InputContext;
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
    private Integer serviceId;
    private String serviceCode;
    private String attributeCode;
    private String attributeName; // follows Accept-Language
    private String attributeNameAr;
    private String descriptionAr;
    private DataType dataType;
    private String unit;
    private Boolean isRequired;
    private Boolean isFilterable;
    private Boolean isSearchable;
    private Integer displayOrder;
    private String description;
    private String defaultValue;
    private String validationRules;
    private String options; // Raw JSON array
    private InputContext inputContext;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
