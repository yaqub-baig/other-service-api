package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.request.OfferingSearchRequest;
import com.autoadda.apis.other.response.OfferingSearchResponse;
import com.autoadda.apis.other.response.OfferingSearchResponse.FacetValue;
import com.autoadda.apis.other.response.OfferingSearchResponse.OfferingSearchResult;
import com.autoadda.apis.other.service.OfferingSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for searching and discovering service offerings.
 * Provides dynamic filtering, geo-spatial search, and faceted results.
 */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final OfferingSearchService searchService;

    /**
     * Main search endpoint with comprehensive filtering options.
     *
     * Example request body:
     * {
     *   "categoryId": 1,
     *   "query": "MRF tyre",
     *   "minPrice": 2000,
     *   "maxPrice": 10000,
     *   "attributes": {
     *     "brand": "MRF",
     *     "width": ["185", "195"]
     *   },
     *   "latitude": 25.2048,
     *   "longitude": 55.2708,
     *   "radiusKm": 10,
     *   "sortBy": "price",
     *   "sortDirection": "asc",
     *   "page": 0,
     *   "size": 20,
     *   "includeFacets": true
     * }
     */
    @PostMapping("/offerings")
    public ResponseEntity<OfferingSearchResponse> search(@RequestBody OfferingSearchRequest request) {
        OfferingSearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Quick search with query parameters (simpler alternative to POST).
     */
    @GetMapping("/offerings")
    public ResponseEntity<OfferingSearchResponse> quickSearch(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radius,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        OfferingSearchRequest request = OfferingSearchRequest.builder()
                .categoryId(categoryId)
                .query(q)
                .minPrice(minPrice != null ? java.math.BigDecimal.valueOf(minPrice) : null)
                .maxPrice(maxPrice != null ? java.math.BigDecimal.valueOf(maxPrice) : null)
                .city(city)
                .area(area)
                .latitude(lat)
                .longitude(lng)
                .radiusKm(radius)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .page(page)
                .size(size)
                .includeVendorDetails(true)
                .includeDistance(lat != null && lng != null)
                .build();

        return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * Search offerings by category with attribute filters.
     */
    @PostMapping("/category/{categoryId}")
    public ResponseEntity<OfferingSearchResponse> searchByCategory(
            @PathVariable Integer categoryId,
            @RequestBody(required = false) OfferingSearchRequest request) {

        if (request == null) {
            request = new OfferingSearchRequest();
        }
        request.setCategoryId(categoryId);

        return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * Search offerings near a location.
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<OfferingSearchResult>> searchNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10") Double radius,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "20") Integer limit) {

        List<OfferingSearchResult> results = searchService.searchNearby(lat, lng, radius, categoryId, limit);
        return ResponseEntity.ok(results);
    }

    /**
     * Get autocomplete suggestions for search.
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String q,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "10") Integer limit) {

        List<String> suggestions = searchService.getAutocompleteSuggestions(q, categoryId, limit);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get available filter options for a category.
     * Useful for building dynamic filter UI.
     */
    @GetMapping("/filters/{categoryId}")
    public ResponseEntity<Map<String, List<FacetValue>>> getFilterOptions(@PathVariable Integer categoryId) {
        Map<String, List<FacetValue>> filterOptions = searchService.getFilterOptions(categoryId);
        return ResponseEntity.ok(filterOptions);
    }

    /**
     * Search by vendor.
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<OfferingSearchResponse> searchByVendor(
            @PathVariable Integer vendorId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        OfferingSearchRequest request = OfferingSearchRequest.builder()
                .vendorId(vendorId)
                .query(q)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * Search featured offerings.
     */
    @GetMapping("/featured")
    public ResponseEntity<OfferingSearchResponse> searchFeatured(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        OfferingSearchRequest request = OfferingSearchRequest.builder()
                .categoryId(categoryId)
                .featuredOnly(true)
                .page(page)
                .size(size)
                .sortBy("popularity")
                .build();

        return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * Multi-filter search - search across multiple categories.
     */
    @PostMapping("/multi")
    public ResponseEntity<Map<Integer, OfferingSearchResponse>> multiCategorySearch(
            @RequestBody List<OfferingSearchRequest> requests) {

        Map<Integer, OfferingSearchResponse> results = new java.util.LinkedHashMap<>();

        for (OfferingSearchRequest request : requests) {
            if (request.getCategoryId() != null) {
                OfferingSearchResponse response = searchService.search(request);
                results.put(request.getCategoryId(), response);
            }
        }

        return ResponseEntity.ok(results);
    }
}
