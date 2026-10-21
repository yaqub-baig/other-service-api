package com.autoadda.apis.other.request;

import com.autoadda.apis.other.util.PricingType;
import com.autoadda.apis.other.util.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOfferingRequest {

    /**
     * The vendor service this offering belongs to.
     * Required for creation.
     */
    private Integer vendorServiceId;

    /**
     * Catalog service this offering sells (optional). Must belong to the vendor service's category.
     */
    private Integer serviceId;

    /**
     * Name of the offering.
     * Example: "MRF ZLX 185/65 R15 Tubeless Car Tyre"
     */
    private String offeringName;

    /**
     * Short description or tagline.
     */
    private String shortDescription;

    /**
     * Detailed description.
     */
    private String description;

    /**
     * Stock Keeping Unit (optional, auto-generated if not provided).
     */
    private String sku;

    /**
     * Pricing strategy.
     */
    private PricingType pricingType;

    /**
     * Base/fixed price.
     */
    private BigDecimal price;

    /**
     * Minimum price (for RANGE pricing).
     */
    private BigDecimal minPrice;

    /**
     * Maximum price (for RANGE pricing).
     */
    private BigDecimal maxPrice;

    /**
     * Original price (for showing discounts).
     */
    private BigDecimal originalPrice;

    /**
     * Currency code (ISO 4217). Defaults to "AED".
     */
    private String currency;

    /**
     * Stock/availability status.
     */
    private StockStatus stockStatus;

    /**
     * Quantity available.
     */
    private Integer quantityAvailable;

    /**
     * Filterable attributes as a map.
     * Will be converted to JSON and validated against ServiceAttributeDefinition.
     * Example: {"brand": "MRF", "width": "185", "height": "65"}
     */
    private Map<String, Object> filterableAttributes;

    /**
     * Additional specifications as a map.
     * Example: {"warranty": "5 years", "country_of_origin": "India"}
     */
    private Map<String, Object> specifications;

    /**
     * Primary image URL.
     */
    private String imageUrl;

    /**
     * Gallery images (as a list of URLs, will be stored as JSON).
     */
    private java.util.List<String> galleryImages;

    /**
     * Display order within vendor's offerings.
     */
    private Integer displayOrder;

    /**
     * Whether this offering is featured.
     */
    private Boolean isFeatured;
}
