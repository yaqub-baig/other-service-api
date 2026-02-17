package com.autoadda.apis.other.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCategoryRequest {
    private String name;
    private String description;
    private Long imageId;
    private Integer parentId;
}
