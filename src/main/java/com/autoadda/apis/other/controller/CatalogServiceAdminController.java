package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.request.CatalogServiceRequest;
import com.autoadda.apis.other.request.MarketAvailabilityRequest;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.CatalogServiceResponse;
import com.autoadda.apis.other.response.MarketAvailabilityResponse;
import com.autoadda.apis.other.response.SchemaVersionResponse;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.service.MarketService;
import com.autoadda.apis.other.service.SchemaVersionService;
import com.autoadda.apis.other.service.ServiceAttributeDefinitionService;
import com.autoadda.apis.other.service.ServiceCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin CRUD for catalog services. There is no delete: deactivate instead, because
 * offerings, quotations and bookings keep referencing the service.
 */
@RestController
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class CatalogServiceAdminController {

    private final ServiceCatalogService serviceCatalogService;
    private final MarketService marketService;
    private final SchemaVersionService schemaVersionService;
    private final ServiceAttributeDefinitionService attributeService;

    @PostMapping
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> create(@RequestBody CatalogServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created", serviceCatalogService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CatalogServiceResponse>>> list(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success(serviceCatalogService.list(categoryId, activeOnly)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(serviceCatalogService.getById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(serviceCatalogService.getByCode(code)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> update(@PathVariable Integer id,
                                                                       @RequestBody CatalogServiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Service updated", serviceCatalogService.update(id, request)));
    }

    @GetMapping("/{id}/markets")
    public ResponseEntity<ApiResponse<List<MarketAvailabilityResponse>>> listMarkets(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(marketService.listForService(id)));
    }

    /**
     * Create or update the service's availability and currency in one country.
     */
    @PutMapping("/{id}/markets")
    public ResponseEntity<ApiResponse<MarketAvailabilityResponse>> upsertMarket(@PathVariable Integer id,
                                                                                @RequestBody MarketAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Market saved", marketService.upsert(id, request)));
    }

    /**
     * Drop the service's own quotation mode so it follows its category's scope again.
     */
    @PostMapping("/{id}/quotation-mode/reset")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> resetQuotationMode(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation mode reset", serviceCatalogService.clearQuotationMode(id)));
    }

    @GetMapping("/{id}/schema-versions")
    public ResponseEntity<ApiResponse<List<SchemaVersionResponse>>> listSchemaVersions(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(schemaVersionService.list(id)));
    }

    /**
     * Open a draft (or return the open one). It starts as a copy of the published version;
     * add or edit attributes through /admin/service-attributes with serviceId, then publish.
     */
    @PostMapping("/{id}/schema-versions")
    public ResponseEntity<ApiResponse<SchemaVersionResponse>> openDraft(@PathVariable Integer id,
                                                                         @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(ApiResponse.success("Draft open", schemaVersionService.openDraft(id, notes)));
    }

    @GetMapping("/{id}/schema-versions/{versionId}/attributes")
    public ResponseEntity<ApiResponse<List<ServiceAttributeDefinitionResponse>>> versionAttributes(
            @PathVariable Integer id, @PathVariable Integer versionId) {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getByVersion(id, versionId)));
    }

    /**
     * Publish the draft; the previously published version is retired.
     */
    @PostMapping("/{id}/schema-versions/{versionId}/publish")
    public ResponseEntity<ApiResponse<SchemaVersionResponse>> publish(@PathVariable Integer id,
                                                                       @PathVariable Integer versionId) {
        return ResponseEntity.ok(ApiResponse.success("Schema published", schemaVersionService.publish(id, versionId)));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> deactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Service deactivated", serviceCatalogService.setActive(id, false)));
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> reactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Service reactivated", serviceCatalogService.setActive(id, true)));
    }
}
