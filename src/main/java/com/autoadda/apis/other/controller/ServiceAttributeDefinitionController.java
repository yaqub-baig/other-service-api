package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.request.ServiceAttributeDefinitionRequest;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.service.ServiceAttributeDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing service attribute definitions.
 * These define the measurable characteristics for each service category.
 */
@RestController
@RequestMapping("/admin/service-attributes")
@RequiredArgsConstructor
public class ServiceAttributeDefinitionController {

    private final ServiceAttributeDefinitionService attributeService;

    /**
     * Create a new attribute definition.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceAttributeDefinitionResponse>> create(@RequestBody ServiceAttributeDefinitionRequest request) {
        ServiceAttributeDefinitionResponse attribute = attributeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attribute definition created", attribute));
    }

    /**
     * Get all attributes for a service category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ServiceAttributeDefinitionResponse>>> getByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getByServiceCategory(categoryId)));
    }

    /**
     * Get all attributes for a service category (including inactive).
     */
    @GetMapping("/category/{categoryId}/all")
    public ResponseEntity<ApiResponse<List<ServiceAttributeDefinitionResponse>>> getAllByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllByServiceCategory(categoryId)));
    }

    /**
     * Get filterable attributes for a service category.
     */
    @GetMapping("/category/{categoryId}/filterable")
    public ResponseEntity<ApiResponse<List<ServiceAttributeDefinitionResponse>>> getFilterableByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getFilterableByServiceCategory(categoryId)));
    }

    /**
     * Get required attributes for a service category.
     */
    @GetMapping("/category/{categoryId}/required")
    public ResponseEntity<ApiResponse<List<ServiceAttributeDefinitionResponse>>> getRequiredByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getRequiredByServiceCategory(categoryId)));
    }

    /**
     * Get attribute by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceAttributeDefinitionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getById(id)));
    }

    /**
     * Get attribute by code within a category.
     */
    @GetMapping("/category/{categoryId}/code/{code}")
    public ResponseEntity<ApiResponse<ServiceAttributeDefinitionResponse>> getByCode(
            @PathVariable Integer categoryId,
            @PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getByCode(categoryId, code)));
    }

    /**
     * Update an attribute definition.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceAttributeDefinitionResponse>> update(
            @PathVariable Long id,
            @RequestBody ServiceAttributeDefinitionRequest request) {
        ServiceAttributeDefinitionResponse updated = attributeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Attribute definition updated", updated));
    }

    /**
     * Soft delete an attribute definition.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        attributeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Attribute definition deleted", null));
    }

    /**
     * Hard delete an attribute definition (admin only).
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<String>> hardDelete(@PathVariable Long id) {
        attributeService.hardDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Attribute definition permanently deleted", null));
    }

    /**
     * Reactivate a soft-deleted attribute definition.
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<ServiceAttributeDefinitionResponse>> reactivate(@PathVariable Long id) {
        ServiceAttributeDefinitionResponse reactivated = attributeService.reactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Attribute definition reactivated", reactivated));
    }

    /**
     * Get all active attributes across all categories.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceAttributeDefinitionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllActive()));
    }
}
