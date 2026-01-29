package com.autoadda.apis.other.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterOptionRequest {

    private Long id;

    private String optionValue;

    private String optionLabel;

    private Integer displayOrder;

    private Boolean isDefault;

    private Boolean isActive;

    private String iconUrl;

    private String parentValue;

    private String metadata;
}
