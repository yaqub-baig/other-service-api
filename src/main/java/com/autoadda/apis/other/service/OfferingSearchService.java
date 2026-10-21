package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.ServiceFilterDefinition;
import com.autoadda.apis.other.entity.ServiceOffering;
import com.autoadda.apis.other.repository.ServiceFilterDefinitionRepository;
import com.autoadda.apis.other.request.OfferingSearchRequest;
import com.autoadda.apis.other.response.OfferingSearchResponse;
import com.autoadda.apis.other.response.OfferingSearchResponse.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced search service for service offerings.
 * Supports dynamic JSON attribute filtering, geo-spatial search, and faceted results.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferingSearchService {

    private final EntityManager entityManager;
    private final ServiceFilterDefinitionRepository filterDefinitionRepository;
    private final ObjectMapper objectMapper;

    // Earth's radius in kilometers for distance calculations
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Execute a comprehensive search with all filters and options.
     */
    @Transactional(readOnly = true)
    public OfferingSearchResponse search(OfferingSearchRequest request) {
        long startTime = System.currentTimeMillis();

        // Build and execute the main query
        SearchQueryBuilder queryBuilder = new SearchQueryBuilder(request);
        String countSql = queryBuilder.buildCountQuery();
        String searchSql = queryBuilder.buildSearchQuery();
        Map<String, Object> params = queryBuilder.getParameters();

        // Execute count query
        Query countQuery = entityManager.createNativeQuery(countSql);
        setQueryParameters(countQuery, params);
        Long totalResults = ((Number) countQuery.getSingleResult()).longValue();

        // Execute search query
        Query searchQuery = entityManager.createNativeQuery(searchSql, ServiceOffering.class);
        setQueryParameters(searchQuery, params);
        searchQuery.setFirstResult(request.getPage() * request.getSize());
        searchQuery.setMaxResults(request.getSize());

        @SuppressWarnings("unchecked")
        List<ServiceOffering> offerings = searchQuery.getResultList();

        // Transform results
        List<OfferingSearchResult> results = transformResults(offerings, request);

        // Build facets if requested
        Map<String, List<FacetValue>> facets = null;
        if (Boolean.TRUE.equals(request.getIncludeFacets()) && request.getCategoryId() != null) {
            facets = buildFacets(request.getCategoryId(), queryBuilder.buildFacetBaseQuery(), params);
        }

        // Get price range
        PriceRange priceRange = getPriceRange(queryBuilder.buildPriceRangeQuery(), params);

        // Calculate pagination
        int totalPages = (int) Math.ceil((double) totalResults / request.getSize());
        boolean hasMore = request.getPage() < totalPages - 1;

        long executionTime = System.currentTimeMillis() - startTime;

        return OfferingSearchResponse.builder()
                .results(results)
                .totalResults(totalResults)
                .totalPages(totalPages)
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .hasMore(hasMore)
                .facets(facets)
                .priceRange(priceRange)
                .metadata(SearchMetadata.builder()
                        .executionTimeMs(executionTime)
                        .query(request.getQuery())
                        .categoryId(request.getCategoryId())
                        .geoSearchApplied(request.getLatitude() != null && request.getLongitude() != null)
                        .searchRadiusKm(request.getRadiusKm())
                        .build())
                .build();
    }

    /**
     * Get autocomplete suggestions for search.
     */
    @Transactional(readOnly = true)
    public List<String> getAutocompleteSuggestions(String query, Integer categoryId, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT o.offering_name FROM service_offerings o ");
        sql.append("JOIN vendor_service_bridge vs ON o.vendor_service_id = vs.id ");
        sql.append("WHERE o.is_active = true ");
        sql.append("AND LOWER(o.offering_name) LIKE LOWER(:query) ");

        if (categoryId != null) {
            sql.append("AND vs.service_category_id = :categoryId ");
        }

        sql.append("ORDER BY o.offering_name LIMIT :limit");

        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        nativeQuery.setParameter("query", "%" + query + "%");
        nativeQuery.setParameter("limit", limit);
        if (categoryId != null) {
            nativeQuery.setParameter("categoryId", categoryId);
        }

        @SuppressWarnings("unchecked")
        List<String> results = nativeQuery.getResultList();
        return results;
    }

    /**
     * Get available filter options for a category.
     */
    @Transactional(readOnly = true)
    public Map<String, List<FacetValue>> getFilterOptions(Integer categoryId) {
        List<ServiceFilterDefinition> filterDefs =
                filterDefinitionRepository.findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);

        Map<String, List<FacetValue>> filterOptions = new LinkedHashMap<>();

        for (ServiceFilterDefinition filterDef : filterDefs) {
            List<FacetValue> options = filterDef.getOptions().stream()
                    .filter(opt -> Boolean.TRUE.equals(opt.getIsActive()))
                    .map(opt -> FacetValue.builder()
                            .value(opt.getOptionValue())
                            .label(opt.getOptionLabel())
                            .count(0L) // Count will be populated during actual search
                            .selected(false)
                            .build())
                    .collect(Collectors.toList());

            filterOptions.put(filterDef.getFilterCode(), options);
        }

        return filterOptions;
    }

    /**
     * Search offerings near a location.
     */
    @Transactional(readOnly = true)
    public List<OfferingSearchResult> searchNearby(Double latitude, Double longitude, Double radiusKm,
                                                    Integer categoryId, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT o.*, ");
        sql.append("( ").append(EARTH_RADIUS_KM).append(" * acos( ");
        sql.append("cos(radians(:lat)) * cos(radians(l.latitude)) * ");
        sql.append("cos(radians(l.longitude) - radians(:lng)) + ");
        sql.append("sin(radians(:lat)) * sin(radians(l.latitude)) ");
        sql.append(") ) AS distance ");
        sql.append("FROM service_offerings o ");
        sql.append("JOIN vendor_service_bridge vs ON o.vendor_service_id = vs.id ");
        sql.append("JOIN vendor v ON vs.vendor_id = v.id ");
        sql.append("JOIN location l ON v.company_location_id = l.id ");
        sql.append("WHERE o.is_active = true ");
        sql.append("AND l.latitude IS NOT NULL AND l.longitude IS NOT NULL ");

        if (categoryId != null) {
            sql.append("AND vs.service_category_id = :categoryId ");
        }

        sql.append("HAVING distance <= :radius ");
        sql.append("ORDER BY distance ASC ");
        sql.append("LIMIT :limit");

        Query query = entityManager.createNativeQuery(sql.toString(), ServiceOffering.class);
        query.setParameter("lat", latitude);
        query.setParameter("lng", longitude);
        query.setParameter("radius", radiusKm);
        query.setParameter("limit", limit);
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }

        @SuppressWarnings("unchecked")
        List<ServiceOffering> offerings = query.getResultList();

        return transformResults(offerings, OfferingSearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .includeDistance(true)
                .build());
    }

    // ==================== Helper Methods ====================

    private List<OfferingSearchResult> transformResults(List<ServiceOffering> offerings,
                                                         OfferingSearchRequest request) {
        return offerings.stream()
                .map(o -> transformToResult(o, request))
                .collect(Collectors.toList());
    }

    private OfferingSearchResult transformToResult(ServiceOffering offering, OfferingSearchRequest request) {
        OfferingSearchResult.OfferingSearchResultBuilder builder = OfferingSearchResult.builder()
                .id(offering.getId())
                .sku(offering.getSku())
                .offeringName(offering.getOfferingName())
                .shortDescription(offering.getShortDescription())
                .pricingType(offering.getPricingType().name())
                .price(offering.getPrice())
                .minPrice(offering.getMinPrice())
                .maxPrice(offering.getMaxPrice())
                .currency(offering.getCurrency())
                .stockStatus(offering.getStockStatus().name())
                .imageUrl(offering.getImageUrl())
                .averageRating(offering.getAverageRating())
                .totalRatings(offering.getTotalRatings())
                .totalOrders(offering.getTotalOrders())
                .isFeatured(offering.getIsFeatured());

        // Parse attributes
        if (offering.getFilterableAttributes() != null) {
            try {
                Map<String, Object> attrs = objectMapper.readValue(
                        offering.getFilterableAttributes(),
                        new TypeReference<Map<String, Object>>() {});
                builder.attributes(attrs);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse filterable attributes for offering {}", offering.getId());
            }
        }

        // Add vendor details if requested
        if (Boolean.TRUE.equals(request.getIncludeVendorDetails()) && offering.getVendorService() != null) {
            var vendor = offering.getVendorService().getVendor();
            if (vendor != null) {
                VendorSummary.VendorSummaryBuilder vendorBuilder = VendorSummary.builder()
                        .id(vendor.getId())
                        .companyBrandName(vendor.getCompanyBrandName());

                if (vendor.getCompanyLogo() != null) {
                    vendorBuilder.companyLogoUrl(vendor.getCompanyLogo().getFilePath());
                }
                if (vendor.getCompanyLocation() != null) {
                    vendorBuilder.city(vendor.getCompanyLocation().getCity())
                            .area(vendor.getCompanyLocation().getArea());
                }

                builder.vendor(vendorBuilder.build());
            }
        }

        // Calculate distance if geo-search
        if (Boolean.TRUE.equals(request.getIncludeDistance()) &&
                request.getLatitude() != null && request.getLongitude() != null) {
            var vendor = offering.getVendorService().getVendor();
            if (vendor != null && vendor.getCompanyLocation() != null &&
                    vendor.getCompanyLocation().hasCoordinates()) {
                double distance = calculateDistance(
                        request.getLatitude(), request.getLongitude(),
                        vendor.getCompanyLocation().getLatitude(),
                        vendor.getCompanyLocation().getLongitude());
                builder.distanceKm(Math.round(distance * 100.0) / 100.0);
            }
        }

        return builder.build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private void setQueryParameters(Query query, Map<String, Object> params) {
        params.forEach((key, value) -> {
            try {
                query.setParameter(key, value);
            } catch (IllegalArgumentException e) {
                // Parameter not used in this query variant
            }
        });
    }

    private Map<String, List<FacetValue>> buildFacets(Integer categoryId, String baseQuery,
                                                       Map<String, Object> params) {
        Map<String, List<FacetValue>> facets = new LinkedHashMap<>();

        List<ServiceFilterDefinition> filterDefs =
                filterDefinitionRepository.findByServiceCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);

        for (ServiceFilterDefinition filterDef : filterDefs) {
            String facetSql = "SELECT JSON_UNQUOTE(JSON_EXTRACT(o.filterable_attributes, '$." +
                    filterDef.getFilterCode() + "')) as val, COUNT(*) as cnt " +
                    "FROM (" + baseQuery + ") AS filtered " +
                    "JOIN service_offerings o ON filtered.id = o.id " +
                    "WHERE JSON_EXTRACT(o.filterable_attributes, '$." + filterDef.getFilterCode() + "') IS NOT NULL " +
                    "GROUP BY val ORDER BY cnt DESC LIMIT 50";

            try {
                Query facetQuery = entityManager.createNativeQuery(facetSql);
                setQueryParameters(facetQuery, params);

                @SuppressWarnings("unchecked")
                List<Object[]> facetResults = facetQuery.getResultList();

                List<FacetValue> facetValues = facetResults.stream()
                        .map(row -> FacetValue.builder()
                                .value((String) row[0])
                                .label((String) row[0])
                                .count(((Number) row[1]).longValue())
                                .selected(false)
                                .build())
                        .collect(Collectors.toList());

                if (!facetValues.isEmpty()) {
                    facets.put(filterDef.getFilterCode(), facetValues);
                }
            } catch (Exception e) {
                log.warn("Failed to build facet for filter {}: {}", filterDef.getFilterCode(), e.getMessage());
            }
        }

        return facets;
    }

    private PriceRange getPriceRange(String priceRangeQuery, Map<String, Object> params) {
        try {
            Query query = entityManager.createNativeQuery(priceRangeQuery);
            setQueryParameters(query, params);

            Object[] result = (Object[]) query.getSingleResult();
            if (result != null && result[0] != null && result[1] != null) {
                return PriceRange.builder()
                        .min(new BigDecimal(result[0].toString()))
                        .max(new BigDecimal(result[1].toString()))
                        .build();
            }
        } catch (Exception e) {
            log.warn("Failed to get price range: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Inner class to build dynamic SQL queries.
     */
    private class SearchQueryBuilder {
        private final OfferingSearchRequest request;
        private final Map<String, Object> parameters = new HashMap<>();
        private final StringBuilder whereClause = new StringBuilder();
        private final StringBuilder joinClause = new StringBuilder();
        private boolean hasGeoJoin = false;

        public SearchQueryBuilder(OfferingSearchRequest request) {
            this.request = request;
            buildClauses();
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        private void buildClauses() {
            // Base join to vendor_service
            joinClause.append("JOIN vendor_service_bridge vs ON o.vendor_service_id = vs.id ");

            // Always filter active offerings
            whereClause.append("o.is_active = true ");

            // Category filter
            if (request.getCategoryId() != null) {
                whereClause.append("AND vs.service_category_id = :categoryId ");
                parameters.put("categoryId", request.getCategoryId());
            }

            // Vendor filter
            if (request.getVendorId() != null) {
                whereClause.append("AND vs.vendor_id = :vendorId ");
                parameters.put("vendorId", request.getVendorId());
            }

            // Text search
            if (request.getQuery() != null && !request.getQuery().isBlank()) {
                whereClause.append("AND (LOWER(o.offering_name) LIKE LOWER(:query) ");
                whereClause.append("OR LOWER(o.short_description) LIKE LOWER(:query) ");
                whereClause.append("OR LOWER(o.description) LIKE LOWER(:query)) ");
                parameters.put("query", "%" + request.getQuery() + "%");
            }

            // SKU filter
            if (request.getSkus() != null && !request.getSkus().isEmpty()) {
                whereClause.append("AND o.sku IN (:skus) ");
                parameters.put("skus", request.getSkus());
            }

            // Price filters
            if (request.getMinPrice() != null) {
                whereClause.append("AND o.price >= :minPrice ");
                parameters.put("minPrice", request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                whereClause.append("AND o.price <= :maxPrice ");
                parameters.put("maxPrice", request.getMaxPrice());
            }

            // Pricing type filter
            if (request.getPricingTypes() != null && !request.getPricingTypes().isEmpty()) {
                List<String> types = request.getPricingTypes().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());
                whereClause.append("AND o.pricing_type IN (:pricingTypes) ");
                parameters.put("pricingTypes", types);
            }

            // Stock status filter
            if (request.getStockStatuses() != null && !request.getStockStatuses().isEmpty()) {
                List<String> statuses = request.getStockStatuses().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());
                whereClause.append("AND o.stock_status IN (:stockStatuses) ");
                parameters.put("stockStatuses", statuses);
            }

            // Featured filter
            if (Boolean.TRUE.equals(request.getFeaturedOnly())) {
                whereClause.append("AND o.is_featured = true ");
            }

            // Rating filter
            if (request.getMinRating() != null) {
                whereClause.append("AND o.average_rating >= :minRating ");
                parameters.put("minRating", request.getMinRating());
            }

            // Dynamic attribute filters (JSON)
            if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
                int attrIndex = 0;
                for (Map.Entry<String, Object> entry : request.getAttributes().entrySet()) {
                    String attrCode = entry.getKey();
                    Object attrValue = entry.getValue();

                    if (attrValue instanceof List) {
                        // Multi-select: any of the values
                        @SuppressWarnings("unchecked")
                        List<Object> values = (List<Object>) attrValue;
                        whereClause.append("AND (");
                        for (int i = 0; i < values.size(); i++) {
                            if (i > 0) whereClause.append(" OR ");
                            String paramName = "attr_" + attrIndex + "_" + i;
                            whereClause.append("JSON_UNQUOTE(JSON_EXTRACT(o.filterable_attributes, '$.").append(attrCode).append("')) = :").append(paramName);
                            parameters.put(paramName, values.get(i).toString());
                        }
                        whereClause.append(") ");
                    } else {
                        // Single value
                        String paramName = "attr_" + attrIndex;
                        whereClause.append("AND JSON_UNQUOTE(JSON_EXTRACT(o.filterable_attributes, '$.").append(attrCode).append("')) = :").append(paramName).append(" ");
                        parameters.put(paramName, attrValue.toString());
                    }
                    attrIndex++;
                }
            }

            // Geo-spatial filters
            if (request.getCity() != null || request.getArea() != null ||
                    (request.getLatitude() != null && request.getLongitude() != null)) {
                addGeoJoin();

                if (request.getCity() != null) {
                    whereClause.append("AND LOWER(l.city) = LOWER(:city) ");
                    parameters.put("city", request.getCity());
                }
                if (request.getArea() != null) {
                    whereClause.append("AND LOWER(l.area) = LOWER(:area) ");
                    parameters.put("area", request.getArea());
                }
            }
        }

        private void addGeoJoin() {
            if (!hasGeoJoin) {
                joinClause.append("JOIN vendor v ON vs.vendor_id = v.id ");
                joinClause.append("JOIN location l ON v.company_location_id = l.id ");
                hasGeoJoin = true;
            }
        }

        public String buildCountQuery() {
            return "SELECT COUNT(*) FROM service_offerings o " +
                    joinClause.toString() +
                    "WHERE " + whereClause.toString();
        }

        public String buildSearchQuery() {
            StringBuilder sql = new StringBuilder();

            // Distance calculation if geo-search
            if (request.getLatitude() != null && request.getLongitude() != null) {
                addGeoJoin();
                sql.append("SELECT o.*, ");
                sql.append("( ").append(EARTH_RADIUS_KM).append(" * acos( ");
                sql.append("GREATEST(-1, LEAST(1, ");
                sql.append("cos(radians(:lat)) * cos(radians(l.latitude)) * ");
                sql.append("cos(radians(l.longitude) - radians(:lng)) + ");
                sql.append("sin(radians(:lat)) * sin(radians(l.latitude)) ");
                sql.append(")) ) ) AS distance ");
                parameters.put("lat", request.getLatitude());
                parameters.put("lng", request.getLongitude());
            } else {
                sql.append("SELECT o.* ");
            }

            sql.append("FROM service_offerings o ");
            sql.append(joinClause);
            sql.append("WHERE ").append(whereClause);

            // Distance filter (HAVING because distance is calculated)
            if (request.getRadiusKm() != null && request.getLatitude() != null) {
                sql.append("AND l.latitude IS NOT NULL AND l.longitude IS NOT NULL ");
                sql.append("HAVING distance <= :radius ");
                parameters.put("radius", request.getRadiusKm());
            }

            // Sorting
            sql.append("ORDER BY ");
            String sortBy = request.getSortBy() != null ? request.getSortBy() : "relevance";
            String sortDir = "desc".equalsIgnoreCase(request.getSortDirection()) ? "DESC" : "ASC";

            switch (sortBy.toLowerCase()) {
                case "price":
                    sql.append("o.price ").append(sortDir);
                    break;
                case "rating":
                    sql.append("o.average_rating DESC NULLS LAST");
                    break;
                case "popularity":
                    sql.append("o.total_orders DESC");
                    break;
                case "distance":
                    if (request.getLatitude() != null) {
                        sql.append("distance ASC");
                    } else {
                        sql.append("o.display_order ASC");
                    }
                    break;
                case "newest":
                    sql.append("o.created_at DESC");
                    break;
                case "name":
                    sql.append("o.offering_name ").append(sortDir);
                    break;
                default: // relevance
                    if (request.getQuery() != null && !request.getQuery().isBlank()) {
                        // Prioritize exact matches
                        sql.append("CASE WHEN LOWER(o.offering_name) = LOWER(:exactQuery) THEN 0 ");
                        sql.append("WHEN LOWER(o.offering_name) LIKE LOWER(:startQuery) THEN 1 ");
                        sql.append("ELSE 2 END, o.is_featured DESC, o.total_orders DESC");
                        parameters.put("exactQuery", request.getQuery());
                        parameters.put("startQuery", request.getQuery() + "%");
                    } else {
                        sql.append("o.is_featured DESC, o.display_order ASC");
                    }
            }

            return sql.toString();
        }

        public String buildFacetBaseQuery() {
            return "SELECT o.id FROM service_offerings o " +
                    joinClause.toString() +
                    "WHERE " + whereClause.toString();
        }

        public String buildPriceRangeQuery() {
            return "SELECT MIN(o.price), MAX(o.price) FROM service_offerings o " +
                    joinClause.toString() +
                    "WHERE " + whereClause.toString() +
                    "AND o.price IS NOT NULL";
        }
    }
}
