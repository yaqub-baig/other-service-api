package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.request.QuotationFieldDefinitionRequest;
import com.autoadda.apis.other.request.QuotationScopeDefinitionRequest;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.QuotationFieldDefinitionResponse;
import com.autoadda.apis.other.response.QuotationScopeDefinitionResponse;
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
    public ResponseEntity<ApiResponse<?>> saveScopeDefinition(
            @RequestBody QuotationScopeDefinitionRequest request) {
        QuotationScopeDefinitionResponse scope = adminService.saveScopeDefinition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation scope saved", scope));
    }

    /**
     * Get quotation scope for a service category.
     */
    @GetMapping("/scope/category/{categoryId}")
    public ResponseEntity<ApiResponse<?>> getScopeForCategory(@PathVariable Integer categoryId) {
        QuotationScopeDefinitionResponse scope = adminService.getScopeDefinitionForCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(scope));
    }

    /**
     * Get quotation scope by ID.
     */
    @GetMapping("/scope/{id}")
    public ResponseEntity<ApiResponse<?>> getScopeById(@PathVariable Integer id) {
        QuotationScopeDefinitionResponse scope = adminService.getScopeDefinitionById(id);
        return ResponseEntity.ok(ApiResponse.success(scope));
    }

    /**
     * Check if quotation is enabled for a category.
     */
    @GetMapping("/scope/category/{categoryId}/enabled")
    public ResponseEntity<ApiResponse<?>> isQuotationEnabled(@PathVariable Integer categoryId) {
        boolean enabled = adminService.isQuotationEnabled(categoryId);
        return ResponseEntity.ok(ApiResponse.success(enabled));
    }

    /**
     * Get all categories with quotation enabled.
     */
    @GetMapping("/scope/enabled")
    public ResponseEntity<ApiResponse<?>> getQuotationEnabledCategories() {
        List<QuotationScopeDefinitionResponse> scopes = adminService.getQuotationEnabledCategories();
        return ResponseEntity.ok(ApiResponse.success(scopes));
    }

    /**
     * Disable quotation for a category.
     */
    @PostMapping("/scope/category/{categoryId}/disable")
    public ResponseEntity<ApiResponse<?>> disableQuotation(@PathVariable Integer categoryId) {
        adminService.disableQuotationForCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Quotation disabled for category", null));
    }

    /**
     * Delete scope definition (soft delete).
     */
    @DeleteMapping("/scope/{id}")
    public ResponseEntity<ApiResponse<?>> deleteScopeDefinition(@PathVariable Integer id) {
        adminService.deleteScopeDefinition(id);
        return ResponseEntity.ok(ApiResponse.success("Scope definition deleted", null));
    }

    // ==================== Field Definition Endpoints ====================

    /**
     * Create a new field definition for a service category.
     */
    @PostMapping("/field")
    public ResponseEntity<ApiResponse<?>> createFieldDefinition(
            @RequestBody QuotationFieldDefinitionRequest request) {
        QuotationFieldDefinitionResponse field = adminService.createFieldDefinition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Field definition created", field));
    }

    /**
     * Update a field definition.
     */
    @PutMapping("/field/{fieldId}")
    public ResponseEntity<ApiResponse<?>> updateFieldDefinition(
            @PathVariable Integer fieldId,
            @RequestBody QuotationFieldDefinitionRequest request) {
        QuotationFieldDefinitionResponse field = adminService.updateFieldDefinition(fieldId, request);
        return ResponseEntity.ok(ApiResponse.success("Field definition updated", field));
    }

    /**
     * Get all fields for a service category.
     */
    @GetMapping("/field/category/{categoryId}")
    public ResponseEntity<ApiResponse<?>> getFieldsForCategory(@PathVariable Integer categoryId) {
        List<QuotationFieldDefinitionResponse> fields = adminService.getFieldsForCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(fields));
    }

    /**
     * Get required fields for a service category.
     */
    @GetMapping("/field/category/{categoryId}/required")
    public ResponseEntity<ApiResponse<?>> getRequiredFieldsForCategory(@PathVariable Integer categoryId) {
        List<QuotationFieldDefinitionResponse> fields = adminService.getRequiredFieldsForCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(fields));
    }

    /**
     * Get field by ID.
     */
    @GetMapping("/field/{fieldId}")
    public ResponseEntity<ApiResponse<?>> getFieldById(@PathVariable Integer fieldId) {
        QuotationFieldDefinitionResponse field = adminService.getFieldById(fieldId);
        return ResponseEntity.ok(ApiResponse.success(field));
    }

    /**
     * Delete a field definition (soft delete).
     */
    @DeleteMapping("/field/{fieldId}")
    public ResponseEntity<ApiResponse<?>> deleteFieldDefinition(@PathVariable Integer fieldId) {
        adminService.deleteFieldDefinition(fieldId);
        return ResponseEntity.ok(ApiResponse.success("Field definition deleted", null));
    }

    /**
     * Reorder fields within a category.
     */
    @PostMapping("/field/category/{categoryId}/reorder")
    public ResponseEntity<ApiResponse<?>> reorderFields(
            @PathVariable Integer categoryId,
            @RequestBody List<Integer> fieldIds) {
        adminService.reorderFields(categoryId, fieldIds);
        return ResponseEntity.ok(ApiResponse.success("Fields reordered", null));
    }
}
