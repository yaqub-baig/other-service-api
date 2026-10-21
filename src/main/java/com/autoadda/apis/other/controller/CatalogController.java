package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.CatalogServiceResponse;
import com.autoadda.apis.other.service.ServiceCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer-facing, read-only view of the service catalog (active services only).
 */
@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ServiceCatalogService serviceCatalogService;

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<CatalogServiceResponse>>> listServices(
            @RequestParam(required = false) Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(serviceCatalogService.list(categoryId, true)));
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ApiResponse<CatalogServiceResponse>> getService(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(serviceCatalogService.getById(id)));
    }
}
