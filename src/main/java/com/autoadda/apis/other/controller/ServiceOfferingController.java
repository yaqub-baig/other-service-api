package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.request.ServiceOfferingRequest;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.response.ServiceOfferingResponse;
import com.autoadda.apis.other.service.ServiceOfferingService;
import com.autoadda.apis.other.util.StockStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing service offerings.
 * Vendors use this to create and manage their sellable items.
 */
@RestController
@RequestMapping("/service-offerings")
@RequiredArgsConstructor
public class ServiceOfferingController {

    private final ServiceOfferingService offeringService;

    // ==================== CRUD Operations ====================

    /**
     * Create a new service offering.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceOfferingResponse>> create(@RequestBody ServiceOfferingRequest request) {
        ServiceOfferingResponse offering = offeringService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service offering created", offering));
    }

    /**
     * Get offering by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceOfferingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getById(id)));
    }

    /**
     * Get offering by SKU.
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ServiceOfferingResponse>> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getBySku(sku)));
    }

    /**
     * Update an offering.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceOfferingResponse>> update(
            @PathVariable Long id,
            @RequestBody ServiceOfferingRequest request) {
        ServiceOfferingResponse updated = offeringService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service offering updated", updated));
    }

    /**
     * Soft delete an offering.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        offeringService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Service offering deleted", null));
    }

    /**
     * Hard delete an offering (admin only).
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDelete(@PathVariable Long id) {
        offeringService.hardDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Service offering permanently deleted", null));
    }

    /**
     * Reactivate a soft-deleted offering.
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<ServiceOfferingResponse>> reactivate(@PathVariable Long id) {
        ServiceOfferingResponse reactivated = offeringService.reactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Service offering reactivated", reactivated));
    }

    // ==================== Vendor Operations ====================

    /**
     * Get all offerings for a vendor service.
     */
    @GetMapping("/vendor-service/{vendorServiceId}")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getByVendorService(@PathVariable Integer vendorServiceId) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getByVendorService(vendorServiceId)));
    }

    /**
     * Get all offerings for a vendor service (including inactive).
     */
    @GetMapping("/vendor-service/{vendorServiceId}/all")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getAllByVendorService(@PathVariable Integer vendorServiceId) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getAllByVendorService(vendorServiceId)));
    }

    /**
     * Get all offerings for a vendor.
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getByVendor(@PathVariable Integer vendorId) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getByVendor(vendorId)));
    }

    /**
     * Get all offerings for a vendor (including inactive).
     */
    @GetMapping("/vendor/{vendorId}/all")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getAllByVendor(@PathVariable Integer vendorId) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getAllByVendor(vendorId)));
    }

    /**
     * Get offerings by vendor and category.
     */
    @GetMapping("/vendor/{vendorId}/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getByVendorAndCategory(
            @PathVariable Integer vendorId,
            @PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getByVendorAndCategory(vendorId, categoryId)));
    }

    // ==================== Category & Discovery ====================

    /**
     * Get offerings by category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getByCategory(categoryId)));
    }

    /**
     * Get paginated offerings by category.
     */
    @GetMapping("/category/{categoryId}/page")
    public ResponseEntity<ApiResponse<Page<ServiceOfferingResponse>>> getByCategoryPaginated(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getByCategoryPaginated(categoryId, page, size)));
    }

    /**
     * Search offerings by name.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> searchByName(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.searchByName(q)));
    }

    /**
     * Search offerings by name within a category.
     */
    @GetMapping("/category/{categoryId}/search")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> searchByNameInCategory(
            @PathVariable Integer categoryId,
            @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.searchByNameInCategory(categoryId, q)));
    }

    /**
     * Get offerings by price range.
     */
    @GetMapping("/category/{categoryId}/price-range")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getByPriceRange(
            @PathVariable Integer categoryId,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getByPriceRange(categoryId, minPrice, maxPrice)));
    }

    // ==================== Featured & Popular ====================

    /**
     * Get featured offerings.
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getFeatured()));
    }

    /**
     * Get featured offerings by category.
     */
    @GetMapping("/category/{categoryId}/featured")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getFeaturedByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getFeaturedByCategory(categoryId)));
    }

    /**
     * Get top-rated offerings.
     */
    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getTopRated(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getTopRated(limit)));
    }

    /**
     * Get most popular offerings.
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> getMostPopular(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getMostPopular(limit)));
    }

    // ==================== Stock Management ====================

    /**
     * Update stock status.
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ServiceOfferingResponse>> updateStockStatus(
            @PathVariable Long id,
            @RequestParam StockStatus status,
            @RequestParam(required = false) Integer quantity) {
        ServiceOfferingResponse updated = offeringService.updateStockStatus(id, status, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock status updated", updated));
    }

    /**
     * Toggle featured status.
     */
    @PatchMapping("/{id}/featured")
    public ResponseEntity<ApiResponse<ServiceOfferingResponse>> toggleFeatured(@PathVariable Long id) {
        ServiceOfferingResponse updated = offeringService.toggleFeatured(id);
        return ResponseEntity.ok(ApiResponse.success("Featured status toggled", updated));
    }

    // ==================== Attribute Filtering ====================

    /**
     * Filter offerings by a single attribute.
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> filterByAttribute(
            @RequestParam String attribute,
            @RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.filterByAttribute(attribute, value)));
    }

    /**
     * Filter offerings by multiple attributes.
     */
    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<List<ServiceOfferingResponse>>> filterByAttributes(
            @RequestBody Map<String, Object> attributes) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.filterByAttributes(attributes)));
    }

    // ==================== Attribute Definitions (Helper) ====================

    /**
     * Get attribute definitions for a category.
     * Useful for building dynamic forms on the frontend.
     */
    @GetMapping("/category/{categoryId}/attributes")
    public ResponseEntity<ApiResponse<List<ServiceAttributeDefinitionResponse>>> getAttributeDefinitions(
            @PathVariable Integer categoryId,
            @RequestParam(required = false) Integer serviceId) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getAttributeDefinitions(categoryId, serviceId)));
    }

    /**
     * Get required attribute definitions for a category.
     */
    @GetMapping("/category/{categoryId}/attributes/required")
    public ResponseEntity<ApiResponse<List<ServiceAttributeDefinitionResponse>>> getRequiredAttributeDefinitions(
            @PathVariable Integer categoryId,
            @RequestParam(required = false) Integer serviceId) {
        return ResponseEntity.ok(ApiResponse.success(offeringService.getRequiredAttributeDefinitions(categoryId, serviceId)));
    }
}
