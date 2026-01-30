package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.entity.ServiceOffering;
import com.autoadda.apis.other.request.ServiceOfferingRequest;
import com.autoadda.apis.other.response.OsGenericResponse;
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
    public ResponseEntity<OsGenericResponse> create(@RequestBody ServiceOfferingRequest request) {
        ServiceOffering offering = offeringService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OsGenericResponse.success("Service offering created", offering));
    }

    /**
     * Get offering by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceOffering> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offeringService.getById(id));
    }

    /**
     * Get offering by SKU.
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ServiceOffering> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(offeringService.getBySku(sku));
    }

    /**
     * Update an offering.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OsGenericResponse> update(
            @PathVariable Long id,
            @RequestBody ServiceOfferingRequest request) {
        ServiceOffering updated = offeringService.update(id, request);
        return ResponseEntity.ok(OsGenericResponse.success("Service offering updated", updated));
    }

    /**
     * Soft delete an offering.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<OsGenericResponse> delete(@PathVariable Long id) {
        offeringService.delete(id);
        return ResponseEntity.ok(OsGenericResponse.success("Service offering deleted", null));
    }

    /**
     * Hard delete an offering (admin only).
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<OsGenericResponse> hardDelete(@PathVariable Long id) {
        offeringService.hardDelete(id);
        return ResponseEntity.ok(OsGenericResponse.success("Service offering permanently deleted", null));
    }

    /**
     * Reactivate a soft-deleted offering.
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<OsGenericResponse> reactivate(@PathVariable Long id) {
        ServiceOffering reactivated = offeringService.reactivate(id);
        return ResponseEntity.ok(OsGenericResponse.success("Service offering reactivated", reactivated));
    }

    // ==================== Vendor Operations ====================

    /**
     * Get all offerings for a vendor service.
     */
    @GetMapping("/vendor-service/{vendorServiceId}")
    public ResponseEntity<List<ServiceOffering>> getByVendorService(@PathVariable Integer vendorServiceId) {
        return ResponseEntity.ok(offeringService.getByVendorService(vendorServiceId));
    }

    /**
     * Get all offerings for a vendor service (including inactive).
     */
    @GetMapping("/vendor-service/{vendorServiceId}/all")
    public ResponseEntity<List<ServiceOffering>> getAllByVendorService(@PathVariable Integer vendorServiceId) {
        return ResponseEntity.ok(offeringService.getAllByVendorService(vendorServiceId));
    }

    /**
     * Get all offerings for a vendor.
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<ServiceOffering>> getByVendor(@PathVariable Integer vendorId) {
        return ResponseEntity.ok(offeringService.getByVendor(vendorId));
    }

    /**
     * Get all offerings for a vendor (including inactive).
     */
    @GetMapping("/vendor/{vendorId}/all")
    public ResponseEntity<List<ServiceOffering>> getAllByVendor(@PathVariable Integer vendorId) {
        return ResponseEntity.ok(offeringService.getAllByVendor(vendorId));
    }

    /**
     * Get offerings by vendor and category.
     */
    @GetMapping("/vendor/{vendorId}/category/{categoryId}")
    public ResponseEntity<List<ServiceOffering>> getByVendorAndCategory(
            @PathVariable Integer vendorId,
            @PathVariable Integer categoryId) {
        return ResponseEntity.ok(offeringService.getByVendorAndCategory(vendorId, categoryId));
    }

    // ==================== Category & Discovery ====================

    /**
     * Get offerings by category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ServiceOffering>> getByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(offeringService.getByCategory(categoryId));
    }

    /**
     * Get paginated offerings by category.
     */
    @GetMapping("/category/{categoryId}/page")
    public ResponseEntity<Page<ServiceOffering>> getByCategoryPaginated(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(offeringService.getByCategoryPaginated(categoryId, page, size));
    }

    /**
     * Search offerings by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ServiceOffering>> searchByName(@RequestParam String q) {
        return ResponseEntity.ok(offeringService.searchByName(q));
    }

    /**
     * Search offerings by name within a category.
     */
    @GetMapping("/category/{categoryId}/search")
    public ResponseEntity<List<ServiceOffering>> searchByNameInCategory(
            @PathVariable Integer categoryId,
            @RequestParam String q) {
        return ResponseEntity.ok(offeringService.searchByNameInCategory(categoryId, q));
    }

    /**
     * Get offerings by price range.
     */
    @GetMapping("/category/{categoryId}/price-range")
    public ResponseEntity<List<ServiceOffering>> getByPriceRange(
            @PathVariable Integer categoryId,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(offeringService.getByPriceRange(categoryId, minPrice, maxPrice));
    }

    // ==================== Featured & Popular ====================

    /**
     * Get featured offerings.
     */
    @GetMapping("/featured")
    public ResponseEntity<List<ServiceOffering>> getFeatured() {
        return ResponseEntity.ok(offeringService.getFeatured());
    }

    /**
     * Get featured offerings by category.
     */
    @GetMapping("/category/{categoryId}/featured")
    public ResponseEntity<List<ServiceOffering>> getFeaturedByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(offeringService.getFeaturedByCategory(categoryId));
    }

    /**
     * Get top-rated offerings.
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<ServiceOffering>> getTopRated(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(offeringService.getTopRated(limit));
    }

    /**
     * Get most popular offerings.
     */
    @GetMapping("/popular")
    public ResponseEntity<List<ServiceOffering>> getMostPopular(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(offeringService.getMostPopular(limit));
    }

    // ==================== Stock Management ====================

    /**
     * Update stock status.
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<OsGenericResponse> updateStockStatus(
            @PathVariable Long id,
            @RequestParam StockStatus status,
            @RequestParam(required = false) Integer quantity) {
        ServiceOffering updated = offeringService.updateStockStatus(id, status, quantity);
        return ResponseEntity.ok(OsGenericResponse.success("Stock status updated", updated));
    }

    /**
     * Toggle featured status.
     */
    @PatchMapping("/{id}/featured")
    public ResponseEntity<OsGenericResponse> toggleFeatured(@PathVariable Long id) {
        ServiceOffering updated = offeringService.toggleFeatured(id);
        return ResponseEntity.ok(OsGenericResponse.success("Featured status toggled", updated));
    }

    // ==================== Attribute Filtering ====================

    /**
     * Filter offerings by a single attribute.
     */
    @GetMapping("/filter")
    public ResponseEntity<List<ServiceOffering>> filterByAttribute(
            @RequestParam String attribute,
            @RequestParam String value) {
        return ResponseEntity.ok(offeringService.filterByAttribute(attribute, value));
    }

    /**
     * Filter offerings by multiple attributes.
     */
    @PostMapping("/filter")
    public ResponseEntity<List<ServiceOffering>> filterByAttributes(
            @RequestBody Map<String, Object> attributes) {
        return ResponseEntity.ok(offeringService.filterByAttributes(attributes));
    }

    // ==================== Attribute Definitions (Helper) ====================

    /**
     * Get attribute definitions for a category.
     * Useful for building dynamic forms on the frontend.
     */
    @GetMapping("/category/{categoryId}/attributes")
    public ResponseEntity<List<ServiceAttributeDefinition>> getAttributeDefinitions(
            @PathVariable Integer categoryId) {
        return ResponseEntity.ok(offeringService.getAttributeDefinitions(categoryId));
    }

    /**
     * Get required attribute definitions for a category.
     */
    @GetMapping("/category/{categoryId}/attributes/required")
    public ResponseEntity<List<ServiceAttributeDefinition>> getRequiredAttributeDefinitions(
            @PathVariable Integer categoryId) {
        return ResponseEntity.ok(offeringService.getRequiredAttributeDefinitions(categoryId));
    }
}
