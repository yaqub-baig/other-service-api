package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.request.CatalogServiceRequest;
import com.autoadda.apis.other.request.MarketAvailabilityRequest;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.CatalogServiceResponse;
import com.autoadda.apis.other.response.MarketAvailabilityResponse;
import com.autoadda.apis.other.service.MarketService;
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

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> deactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Service deactivated", serviceCatalogService.setActive(id, false)));
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> reactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Service reactivated", serviceCatalogService.setActive(id, true)));
    }
}
