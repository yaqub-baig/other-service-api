package com.autoadda.apis.other.response;

import com.autoadda.apis.other.util.PricingType;
import com.autoadda.apis.other.util.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for ServiceOffering entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOfferingResponse {
    private Long id;
    private Integer vendorServiceId;
    private String vendorServiceName;
    private Integer vendorId;
    private String vendorName;
    private Integer serviceCategoryId;
    private String serviceCategoryName;
    private Integer serviceId;
    private String serviceCode;
    private String serviceName;
    private Integer schemaVersionId;
    private Integer schemaVersionNo;
    private String offeringName;
    private String shortDescription;
    private String description;
    private String sku;
    private PricingType pricingType;
    private BigDecimal price;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal originalPrice;
    private String currency;
    private StockStatus stockStatus;
    private Integer quantityAvailable;
    private String filterableAttributes;
    private String specifications;
    private String imageUrl;
    private String galleryImages;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private Integer totalOrders;
    private Integer displayOrder;
    private Boolean isFeatured;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
