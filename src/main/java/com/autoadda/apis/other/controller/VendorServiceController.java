package com.autoadda.apis.other.controller;


import com.autoadda.apis.other.request.VendorServiceRequest;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.VendorServiceResponse;
import com.autoadda.apis.other.service.VendorServiceCrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendor-services")
@RequiredArgsConstructor
public class VendorServiceController {

    private final VendorServiceCrudService vendorServiceCrudService;

    @PostMapping
    public ResponseEntity<ApiResponse<VendorServiceResponse>> create(@RequestBody VendorServiceRequest request) {
        VendorServiceResponse response = vendorServiceCrudService.createVendorService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor service created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorServiceResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(vendorServiceCrudService.getVendorServiceById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorServiceResponse>> update(@PathVariable Integer id, @RequestBody VendorServiceRequest request) {
        VendorServiceResponse response = vendorServiceCrudService.updateVendorService(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vendor service updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        vendorServiceCrudService.deactivateVendorService(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor service deactivated", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorServiceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(vendorServiceCrudService.getAllVendorServices()));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<VendorServiceResponse>>> getByVendor(@PathVariable Integer vendorId) {
        return ResponseEntity.ok(ApiResponse.success(vendorServiceCrudService.getByVendorId(vendorId)));
    }
}
