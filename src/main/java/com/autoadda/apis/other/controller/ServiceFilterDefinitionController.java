package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.FilterOption;
import com.autoadda.apis.other.entity.ServiceFilterDefinition;
import com.autoadda.apis.other.request.FilterOptionRequest;
import com.autoadda.apis.other.request.ServiceFilterDefinitionRequest;
import com.autoadda.apis.other.response.OsGenericResponse;
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
    public ResponseEntity<OsGenericResponse> create(@RequestBody ServiceFilterDefinitionRequest request) {
        ServiceFilterDefinition filter = filterService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OsGenericResponse.success("Filter definition created", filter));
    }

    /**
     * Get all active filters for a service category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ServiceFilterDefinition>> getByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(filterService.getByServiceCategory(categoryId));
    }

    /**
     * Get all filters for a service category (including inactive).
     */
    @GetMapping("/category/{categoryId}/all")
    public ResponseEntity<List<ServiceFilterDefinition>> getAllByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(filterService.getAllByServiceCategory(categoryId));
    }

    /**
     * Get required filters for a service category.
     */
    @GetMapping("/category/{categoryId}/required")
    public ResponseEntity<List<ServiceFilterDefinition>> getRequiredByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(filterService.getRequiredByServiceCategory(categoryId));
    }

    /**
     * Get filters that depend on a specific filter.
     */
    @GetMapping("/category/{categoryId}/dependent/{parentFilterCode}")
    public ResponseEntity<List<ServiceFilterDefinition>> getDependentFilters(
            @PathVariable Integer categoryId,
            @PathVariable String parentFilterCode) {
        return ResponseEntity.ok(filterService.getDependentFilters(categoryId, parentFilterCode));
    }

    /**
     * Get filter by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceFilterDefinition> getById(@PathVariable Long id) {
        return ResponseEntity.ok(filterService.getById(id));
    }

    /**
     * Get filter by code within a category.
     */
    @GetMapping("/category/{categoryId}/code/{code}")
    public ResponseEntity<ServiceFilterDefinition> getByCode(
            @PathVariable Integer categoryId,
            @PathVariable String code) {
        return ResponseEntity.ok(filterService.getByCode(categoryId, code));
    }

    /**
     * Update a filter definition.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OsGenericResponse> update(
            @PathVariable Long id,
            @RequestBody ServiceFilterDefinitionRequest request) {
        ServiceFilterDefinition updated = filterService.update(id, request);
        return ResponseEntity.ok(OsGenericResponse.success("Filter definition updated", updated));
    }

    /**
     * Soft delete a filter definition.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<OsGenericResponse> delete(@PathVariable Long id) {
        filterService.delete(id);
        return ResponseEntity.ok(OsGenericResponse.success("Filter definition deleted", null));
    }

    /**
     * Hard delete a filter definition (admin only).
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<OsGenericResponse> hardDelete(@PathVariable Long id) {
        filterService.hardDelete(id);
        return ResponseEntity.ok(OsGenericResponse.success("Filter definition permanently deleted", null));
    }

    /**
     * Reactivate a soft-deleted filter definition.
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<OsGenericResponse> reactivate(@PathVariable Long id) {
        ServiceFilterDefinition reactivated = filterService.reactivate(id);
        return ResponseEntity.ok(OsGenericResponse.success("Filter definition reactivated", reactivated));
    }

    /**
     * Get all active filters across all categories.
     */
    @GetMapping
    public ResponseEntity<List<ServiceFilterDefinition>> getAll() {
        return ResponseEntity.ok(filterService.getAllActive());
    }

    // Filter Option endpoints

    /**
     * Add an option to a filter.
     */
    @PostMapping("/{filterId}/options")
    public ResponseEntity<OsGenericResponse> addOption(
            @PathVariable Long filterId,
            @RequestBody FilterOptionRequest request) {
        ServiceFilterDefinition updated = filterService.addOption(filterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OsGenericResponse.success("Filter option added", updated));
    }

    /**
     * Get options for a filter.
     */
    @GetMapping("/{filterId}/options")
    public ResponseEntity<List<FilterOption>> getOptions(
            @PathVariable Long filterId,
            @RequestParam(required = false) String parentValue) {
        return ResponseEntity.ok(filterService.getOptions(filterId, parentValue));
    }

    /**
     * Update an option.
     */
    @PutMapping("/options/{optionId}")
    public ResponseEntity<OsGenericResponse> updateOption(
            @PathVariable Long optionId,
            @RequestBody FilterOptionRequest request) {
        FilterOption updated = filterService.updateOption(optionId, request);
        return ResponseEntity.ok(OsGenericResponse.success("Filter option updated", updated));
    }

    /**
     * Delete an option.
     */
    @DeleteMapping("/options/{optionId}")
    public ResponseEntity<OsGenericResponse> deleteOption(@PathVariable Long optionId) {
        filterService.deleteOption(optionId);
        return ResponseEntity.ok(OsGenericResponse.success("Filter option deleted", null));
    }
}
