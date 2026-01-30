package com.autoadda.apis.other.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response wrapper for offering search results.
 * Includes paginated results, facets for filtering, and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferingSearchResponse {

    /**
     * The search results.
     */
    private List<OfferingSearchResult> results;

    /**
     * Total number of matching results.
     */
    private Long totalResults;

    /**
     * Total number of pages.
     */
    private Integer totalPages;

    /**
     * Current page number (0-based).
     */
    private Integer currentPage;

    /**
     * Number of results per page.
     */
    private Integer pageSize;

    /**
     * Whether there are more pages.
     */
    private Boolean hasMore;

    /**
     * Facets/aggregations for building filter UI.
     * Key: attribute code
     * Value: list of facet values with counts
     */
    private Map<String, List<FacetValue>> facets;

    /**
     * Price range in results (for price slider UI).
     */
    private PriceRange priceRange;

    /**
     * Search metadata (timing, query details).
     */
    private SearchMetadata metadata;

    /**
     * Represents a single search result with optional enrichments.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OfferingSearchResult {
        private Long id;
        private String sku;
        private String offeringName;
        private String shortDescription;
        private String pricingType;
        private java.math.BigDecimal price;
        private java.math.BigDecimal minPrice;
        private java.math.BigDecimal maxPrice;
        private String currency;
        private String stockStatus;
        private String imageUrl;
        private java.math.BigDecimal averageRating;
        private Integer totalRatings;
        private Integer totalOrders;
        private Boolean isFeatured;

        /**
         * Parsed filterable attributes.
         */
        private Map<String, Object> attributes;

        /**
         * Vendor details (if requested).
         */
        private VendorSummary vendor;

        /**
         * Distance from search location in km (if geo-search).
         */
        private Double distanceKm;

        /**
         * Relevance score (for text search).
         */
        private Double score;
    }

    /**
     * Summary of vendor info for search results.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VendorSummary {
        private Integer id;
        private String companyBrandName;
        private String companyLogoUrl;
        private String city;
        private String area;
        private java.math.BigDecimal averageRating;
        private Integer totalRatings;
    }

    /**
     * Represents a facet value with count.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacetValue {
        private String value;
        private String label;
        private Long count;
        private Boolean selected;
    }

    /**
     * Price range for filter UI.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceRange {
        private java.math.BigDecimal min;
        private java.math.BigDecimal max;
    }

    /**
     * Search metadata.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchMetadata {
        private Long executionTimeMs;
        private String query;
        private Integer categoryId;
        private Boolean geoSearchApplied;
        private Double searchRadiusKm;
    }
}
