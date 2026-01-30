package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.request.FilterOptionRequest;
import com.autoadda.apis.other.request.ServiceFilterDefinitionRequest;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.FilterOptionResponse;
import com.autoadda.apis.other.response.ServiceFilterDefinitionResponse;
import com.autoadda.apis.other.service.ServiceFilterDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing service filter definitions.
 * These define the searchable/filterable fields for each service category.
 */
@RestController
@RequestMapping("/admin/service-filters")
@RequiredArgsConstructor
public class ServiceFilterDefinitionController {

    private final ServiceFilterDefinitionService filterService;

    /**
     * Create a new filter definition.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceFilterDefinitionResponse>> create(@RequestBody ServiceFilterDefinitionRequest request) {
        ServiceFilterDefinitionResponse filter = filterService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Filter definition created", filter));
    }

    /**
     * Get all active filters for a service category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ServiceFilterDefinitionResponse>>> getByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(filterService.getByServiceCategory(categoryId)));
    }

    /**
     * Get all filters for a service category (including inactive).
     */
    @GetMapping("/category/{categoryId}/all")
    public ResponseEntity<ApiResponse<List<ServiceFilterDefinitionResponse>>> getAllByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(filterService.getAllByServiceCategory(categoryId)));
    }

    /**
     * Get required filters for a service category.
     */
    @GetMapping("/category/{categoryId}/required")
    public ResponseEntity<ApiResponse<List<ServiceFilterDefinitionResponse>>> getRequiredByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(filterService.getRequiredByServiceCategory(categoryId)));
    }

    /**
     * Get filters that depend on a specific filter.
     */
    @GetMapping("/category/{categoryId}/dependent/{parentFilterCode}")
    public ResponseEntity<ApiResponse<List<ServiceFilterDefinitionResponse>>> getDependentFilters(
            @PathVariable Integer categoryId,
            @PathVariable String parentFilterCode) {
        return ResponseEntity.ok(ApiResponse.success(filterService.getDependentFilters(categoryId, parentFilterCode)));
    }

    /**
     * Get filter by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceFilterDefinitionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(filterService.getById(id)));
    }

    /**
     * Get filter by code within a category.
     */
    @GetMapping("/category/{categoryId}/code/{code}")
    public ResponseEntity<ApiResponse<ServiceFilterDefinitionResponse>> getByCode(
            @PathVariable Integer categoryId,
            @PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(filterService.getByCode(categoryId, code)));
    }

    /**
     * Update a filter definition.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceFilterDefinitionResponse>> update(
            @PathVariable Long id,
            @RequestBody ServiceFilterDefinitionRequest request) {
        ServiceFilterDefinitionResponse updated = filterService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Filter definition updated", updated));
    }

    /**
     * Soft delete a filter definition.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        filterService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Filter definition deleted", null));
    }

    /**
     * Hard delete a filter definition (admin only).
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDelete(@PathVariable Long id) {
        filterService.hardDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Filter definition permanently deleted", null));
    }

    /**
     * Reactivate a soft-deleted filter definition.
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<ServiceFilterDefinitionResponse>> reactivate(@PathVariable Long id) {
        ServiceFilterDefinitionResponse reactivated = filterService.reactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Filter definition reactivated", reactivated));
    }

    /**
     * Get all active filters across all categories.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceFilterDefinitionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(filterService.getAllActive()));
    }

    // Filter Option endpoints

    /**
     * Add an option to a filter.
     */
    @PostMapping("/{filterId}/options")
    public ResponseEntity<ApiResponse<ServiceFilterDefinitionResponse>> addOption(
            @PathVariable Long filterId,
            @RequestBody FilterOptionRequest request) {
        ServiceFilterDefinitionResponse updated = filterService.addOption(filterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Filter option added", updated));
    }

    /**
     * Get options for a filter.
     */
    @GetMapping("/{filterId}/options")
    public ResponseEntity<ApiResponse<List<FilterOptionResponse>>> getOptions(
            @PathVariable Long filterId,
            @RequestParam(required = false) String parentValue) {
        return ResponseEntity.ok(ApiResponse.success(filterService.getOptions(filterId, parentValue)));
    }

    /**
     * Update an option.
     */
    @PutMapping("/options/{optionId}")
    public ResponseEntity<ApiResponse<FilterOptionResponse>> updateOption(
            @PathVariable Long optionId,
            @RequestBody FilterOptionRequest request) {
        FilterOptionResponse updated = filterService.updateOption(optionId, request);
        return ResponseEntity.ok(ApiResponse.success("Filter option updated", updated));
    }

    /**
     * Delete an option.
     */
    @DeleteMapping("/options/{optionId}")
    public ResponseEntity<ApiResponse<Void>> deleteOption(@PathVariable Long optionId) {
        filterService.deleteOption(optionId);
        return ResponseEntity.ok(ApiResponse.success("Filter option deleted", null));
    }
}
