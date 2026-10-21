package com.autoadda.apis.other.request;

import com.autoadda.apis.other.util.PricingType;
import com.autoadda.apis.other.util.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive search request for service offerings.
 * Supports dynamic filtering, geo-spatial search, and pagination.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferingSearchRequest {

    // ==================== Category & Basic Filters ====================

    /**
     * Service category ID to search within.
     */
    private Integer categoryId;

    /**
     * Vendor ID to filter by specific vendor.
     */
    private Integer vendorId;

    /**
     * Text search query (searches offering name, description).
     */
    private String query;

    /**
     * List of SKUs to search for.
     */
    private List<String> skus;

    // ==================== Price Filters ====================

    /**
     * Minimum price filter.
     */
    private BigDecimal minPrice;

    /**
     * Maximum price filter.
     */
    private BigDecimal maxPrice;

    /**
     * Filter by pricing type (FIXED, RANGE, QUOTE_BASED, NEGOTIABLE).
     */
    private List<PricingType> pricingTypes;

    // ==================== Stock & Status Filters ====================

    /**
     * Filter by stock status.
     */
    private List<StockStatus> stockStatuses;

    /**
     * Only show featured offerings.
     */
    private Boolean featuredOnly;

    // ==================== Dynamic Attribute Filters ====================

    /**
     * Dynamic attribute filters based on ServiceAttributeDefinition.
     * Key: attribute code (e.g., "brand", "width")
     * Value: single value or list of values for multi-select
     *
     * Example:
     * {
     *   "brand": "MRF",
     *   "width": ["185", "195"],
     *   "rim_size": "15"
     * }
     */
    private Map<String, Object> attributes;

    // ==================== Geo-Spatial Filters ====================

    /**
     * User's latitude for distance-based search.
     */
    private Double latitude;

    /**
     * User's longitude for distance-based search.
     */
    private Double longitude;

    /**
     * Maximum distance in kilometers from user's location.
     */
    private Double radiusKm;

    /**
     * Filter by city name.
     */
    private String city;

    /**
     * Filter by area within a city.
     */
    private String area;

    /**
     * ISO country code, e.g. "AE". Only vendors in that country, and only catalog services
     * available there, are returned.
     */
    private String countryCode;

    /**
     * Filter by catalog service.
     */
    private Integer serviceId;

    // ==================== Rating Filters ====================

    /**
     * Minimum average rating (1.0 to 5.0).
     */
    private BigDecimal minRating;

    // ==================== Sorting ====================

    /**
     * Sort field. Options: "price", "rating", "popularity", "distance", "newest", "name"
     */
    @Builder.Default
    private String sortBy = "relevance";

    /**
     * Sort direction. Options: "asc", "desc"
     */
    @Builder.Default
    private String sortDirection = "asc";

    // ==================== Pagination ====================

    /**
     * Page number (0-based).
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * Number of results per page.
     */
    @Builder.Default
    private Integer size = 20;

    // ==================== Response Options ====================

    /**
     * Include vendor details in response.
     */
    @Builder.Default
    private Boolean includeVendorDetails = false;

    /**
     * Include distance calculation in response (requires lat/lng).
     */
    @Builder.Default
    private Boolean includeDistance = true;

    /**
     * Include facets/aggregations in response for filter UI.
     */
    @Builder.Default
    private Boolean includeFacets = false;
}
