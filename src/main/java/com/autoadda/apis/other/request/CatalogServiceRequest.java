package com.autoadda.apis.other.request;

import com.autoadda.apis.other.util.FulfillmentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogServiceRequest {
    private Integer serviceCategoryId;
    private String code;
    private String name;
    private String description;
    private FulfillmentMode fulfillmentMode;
    private Integer estimatedDurationMinutes;
    private Integer displayOrder;
}
