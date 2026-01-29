package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.ServiceAttributeDefinition;
import com.autoadda.apis.other.request.ServiceAttributeDefinitionRequest;
import com.autoadda.apis.other.response.OsGenericResponse;
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
    public ResponseEntity<OsGenericResponse> create(@RequestBody ServiceAttributeDefinitionRequest request) {
        ServiceAttributeDefinition attribute = attributeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OsGenericResponse.success("Attribute definition created", attribute));
    }

    /**
     * Get all attributes for a service category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ServiceAttributeDefinition>> getByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(attributeService.getByServiceCategory(categoryId));
    }

    /**
     * Get all attributes for a service category (including inactive).
     */
    @GetMapping("/category/{categoryId}/all")
    public ResponseEntity<List<ServiceAttributeDefinition>> getAllByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(attributeService.getAllByServiceCategory(categoryId));
    }

    /**
     * Get filterable attributes for a service category.
     */
    @GetMapping("/category/{categoryId}/filterable")
    public ResponseEntity<List<ServiceAttributeDefinition>> getFilterableByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(attributeService.getFilterableByServiceCategory(categoryId));
    }

    /**
     * Get required attributes for a service category.
     */
    @GetMapping("/category/{categoryId}/required")
    public ResponseEntity<List<ServiceAttributeDefinition>> getRequiredByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(attributeService.getRequiredByServiceCategory(categoryId));
    }

    /**
     * Get attribute by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceAttributeDefinition> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attributeService.getById(id));
    }

    /**
     * Get attribute by code within a category.
     */
    @GetMapping("/category/{categoryId}/code/{code}")
    public ResponseEntity<ServiceAttributeDefinition> getByCode(
            @PathVariable Integer categoryId,
            @PathVariable String code) {
        return ResponseEntity.ok(attributeService.getByCode(categoryId, code));
    }

    /**
     * Update an attribute definition.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OsGenericResponse> update(
            @PathVariable Long id,
            @RequestBody ServiceAttributeDefinitionRequest request) {
        ServiceAttributeDefinition updated = attributeService.update(id, request);
        return ResponseEntity.ok(OsGenericResponse.success("Attribute definition updated", updated));
    }

    /**
     * Soft delete an attribute definition.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<OsGenericResponse> delete(@PathVariable Long id) {
        attributeService.delete(id);
        return ResponseEntity.ok(OsGenericResponse.success("Attribute definition deleted", null));
    }

    /**
     * Hard delete an attribute definition (admin only).
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<OsGenericResponse> hardDelete(@PathVariable Long id) {
        attributeService.hardDelete(id);
        return ResponseEntity.ok(OsGenericResponse.success("Attribute definition permanently deleted", null));
    }

    /**
     * Reactivate a soft-deleted attribute definition.
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<OsGenericResponse> reactivate(@PathVariable Long id) {
        ServiceAttributeDefinition reactivated = attributeService.reactivate(id);
        return ResponseEntity.ok(OsGenericResponse.success("Attribute definition reactivated", reactivated));
    }

    /**
     * Get all active attributes across all categories.
     */
    @GetMapping
    public ResponseEntity<List<ServiceAttributeDefinition>> getAll() {
        return ResponseEntity.ok(attributeService.getAllActive());
    }
}
