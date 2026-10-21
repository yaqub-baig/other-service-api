package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.CatalogServiceResponse;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.service.ServiceAttributeDefinitionService;
import com.autoadda.apis.other.service.ServiceCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer-facing, read-only view of the service catalog (active services only).
 * Pass ?country=AE to see only the services offered in that country.
 */
@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ServiceCatalogService serviceCatalogService;
    private final ServiceAttributeDefinitionService attributeService;

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<CatalogServiceResponse>>> listServices(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String country) {
        return ResponseEntity.ok(ApiResponse.success(country != null
                ? serviceCatalogService.listAvailableInCountry(categoryId, country)
                : serviceCatalogService.list(categoryId, true)));
    }

    /**
     * The live attribute form for a service: its published schema plus category defaults.
     * Each attribute carries schemaVersionId (null for category defaults).
     */
    @GetMapping("/services/{id}/attributes")
    public ResponseEntity<ApiResponse<List<ServiceAttributeDefinitionResponse>>> getServiceAttributes(@PathVariable Integer id) {
        CatalogServiceResponse service = serviceCatalogService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(attributeService.getResolved(service.getServiceCategoryId(), id)));
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> getService(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(serviceCatalogService.getById(id)));
    }
}
