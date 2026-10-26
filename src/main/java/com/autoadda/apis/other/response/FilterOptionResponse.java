package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for FilterOption entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterOptionResponse {
    private Long id;
    private String optionValue;
    private String optionLabel; // follows Accept-Language
    private String optionLabelAr;
    private Integer displayOrder;
    private Boolean isDefault;
    private Boolean isActive;
    private String iconUrl;
    private String parentValue;
    private String metadata;
}
