package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.FulfillmentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogServiceResponse {
    private Integer id;
    private Integer serviceCategoryId;
    private String serviceCategoryName;
    private String code;
    private String name;
    private String description;
    private FulfillmentMode fulfillmentMode;
    private Integer estimatedDurationMinutes;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
