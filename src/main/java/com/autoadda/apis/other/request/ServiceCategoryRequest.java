package com.autoadda.apis.other.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCategoryRequest {
    private String code;
    private String name;
    private String description;
    private String nameAr;
    private String descriptionAr;
    private Integer displayOrder;
    private Long imageId;
    private Integer parentId;
}
