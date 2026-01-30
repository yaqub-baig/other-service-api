package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.QuotationFieldDefinition;
import com.autoadda.apis.other.entity.QuotationScopeDefinition;
import com.autoadda.apis.other.request.QuotationFieldDefinitionRequest;
import com.autoadda.apis.other.request.QuotationScopeDefinitionRequest;
import com.autoadda.apis.other.response.OsGenericResponse;
import com.autoadda.apis.other.service.QuotationAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing quotation configurations.
 * Handles scope definitions and field definitions for service categories.
 */
@RestController
@RequestMapping("/api/admin/quotation")
@RequiredArgsConstructor
public class QuotationAdminController {

    private final QuotationAdminService adminService;

    // ==================== Scope Definition Endpoints ====================

    /**
     * Create or update quotation scope for a service category.
     */
    @PostMapping("/scope")
    public ResponseEntity<OsGenericResponse> saveScopeDefinition(
            @RequestBody QuotationScopeDefinitionRequest request) {
        QuotationScopeDefinition scope = adminService.saveScopeDefinition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OsGenericResponse.success("Quotation scope saved", scope));
    }

    /**
     * Get quotation scope for a service category.
     */
    @GetMapping("/scope/category/{categoryId}")
    public ResponseEntity<OsGenericResponse> getScopeForCategory(@PathVariable Integer categoryId) {
        QuotationScopeDefinition scope = adminService.getScopeDefinitionForCategory(categoryId);
        return ResponseEntity.ok(OsGenericResponse.success(scope));
    }

    /**
     * Get quotation scope by ID.
     */
    @GetMapping("/scope/{id}")
    public ResponseEntity<OsGenericResponse> getScopeById(@PathVariable Integer id) {
        QuotationScopeDefinition scope = adminService.getScopeDefinitionById(id);
        return ResponseEntity.ok(OsGenericResponse.success(scope));
    }

    /**
     * Check if quotation is enabled for a category.
     */
    @GetMapping("/scope/category/{categoryId}/enabled")
    public ResponseEntity<OsGenericResponse> isQuotationEnabled(@PathVariable Integer categoryId) {
        boolean enabled = adminService.isQuotationEnabled(categoryId);
        return ResponseEntity.ok(OsGenericResponse.success(enabled));
    }

    /**
     * Get all categories with quotation enabled.
     */
    @GetMapping("/scope/enabled")
    public ResponseEntity<OsGenericResponse> getQuotationEnabledCategories() {
        List<QuotationScopeDefinition> scopes = adminService.getQuotationEnabledCategories();
        return ResponseEntity.ok(OsGenericResponse.success(scopes));
    }

    /**
     * Disable quotation for a category.
     */
    @PostMapping("/scope/category/{categoryId}/disable")
    public ResponseEntity<OsGenericResponse> disableQuotation(@PathVariable Integer categoryId) {
        adminService.disableQuotationForCategory(categoryId);
        return ResponseEntity.ok(OsGenericResponse.success("Quotation disabled for category", null));
    }

    /**
     * Delete scope definition (soft delete).
     */
    @DeleteMapping("/scope/{id}")
    public ResponseEntity<OsGenericResponse> deleteScopeDefinition(@PathVariable Integer id) {
        adminService.deleteScopeDefinition(id);
        return ResponseEntity.ok(OsGenericResponse.success("Scope definition deleted", null));
    }

    // ==================== Field Definition Endpoints ====================

    /**
     * Create a new field definition for a service category.
     */
    @PostMapping("/field")
    public ResponseEntity<OsGenericResponse> createFieldDefinition(
            @RequestBody QuotationFieldDefinitionRequest request) {
        QuotationFieldDefinition field = adminService.createFieldDefinition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OsGenericResponse.success("Field definition created", field));
    }

    /**
     * Update a field definition.
     */
    @PutMapping("/field/{fieldId}")
    public ResponseEntity<OsGenericResponse> updateFieldDefinition(
            @PathVariable Integer fieldId,
            @RequestBody QuotationFieldDefinitionRequest request) {
        QuotationFieldDefinition field = adminService.updateFieldDefinition(fieldId, request);
        return ResponseEntity.ok(OsGenericResponse.success("Field definition updated", field));
    }

    /**
     * Get all fields for a service category.
     */
    @GetMapping("/field/category/{categoryId}")
    public ResponseEntity<OsGenericResponse> getFieldsForCategory(@PathVariable Integer categoryId) {
        List<QuotationFieldDefinition> fields = adminService.getFieldsForCategory(categoryId);
        return ResponseEntity.ok(OsGenericResponse.success(fields));
    }

    /**
     * Get required fields for a service category.
     */
    @GetMapping("/field/category/{categoryId}/required")
    public ResponseEntity<OsGenericResponse> getRequiredFieldsForCategory(@PathVariable Integer categoryId) {
        List<QuotationFieldDefinition> fields = adminService.getRequiredFieldsForCategory(categoryId);
        return ResponseEntity.ok(OsGenericResponse.success(fields));
    }

    /**
     * Get field by ID.
     */
    @GetMapping("/field/{fieldId}")
    public ResponseEntity<OsGenericResponse> getFieldById(@PathVariable Integer fieldId) {
        QuotationFieldDefinition field = adminService.getFieldById(fieldId);
        return ResponseEntity.ok(OsGenericResponse.success(field));
    }

    /**
     * Delete a field definition (soft delete).
     */
    @DeleteMapping("/field/{fieldId}")
    public ResponseEntity<OsGenericResponse> deleteFieldDefinition(@PathVariable Integer fieldId) {
        adminService.deleteFieldDefinition(fieldId);
        return ResponseEntity.ok(OsGenericResponse.success("Field definition deleted", null));
    }

    /**
     * Reorder fields within a category.
     */
    @PostMapping("/field/category/{categoryId}/reorder")
    public ResponseEntity<OsGenericResponse> reorderFields(
            @PathVariable Integer categoryId,
            @RequestBody List<Integer> fieldIds) {
        adminService.reorderFields(categoryId, fieldIds);
        return ResponseEntity.ok(OsGenericResponse.success("Fields reordered", null));
    }
}
