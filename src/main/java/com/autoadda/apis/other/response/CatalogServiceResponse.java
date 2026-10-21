package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.FulfillmentMode;
import com.autoadda.apis.other.util.QuotationMode;
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
    // name/description follow Accept-Language (Arabic when available); nameAr/descriptionAr are raw
    private String name;
    private String description;
    private String nameAr;
    private String descriptionAr;
    /** The service's own setting; null means it follows the category. */
    private QuotationMode quotationMode;
    /** What actually applies after falling back to the category's scope. */
    private QuotationMode effectiveQuotationMode;
    private FulfillmentMode fulfillmentMode;
    private Integer estimatedDurationMinutes;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
