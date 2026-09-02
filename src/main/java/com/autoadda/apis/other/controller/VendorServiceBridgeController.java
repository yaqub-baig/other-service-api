package com.autoadda.apis.other.controller;


import com.autoadda.apis.other.entity.VendorServiceBridge;
import com.autoadda.apis.other.request.VendorServiceRequest;
import com.autoadda.apis.other.response.ApiResponse;
import com.autoadda.apis.other.response.VendorServiceBridgeResponse;
import com.autoadda.apis.other.service.VendorServiceBridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendor-service-bridges")
@RequiredArgsConstructor
public class VendorServiceBridgeController {

    private final VendorServiceBridgeService vendorServiceBridgeService;

    @PostMapping
    public ResponseEntity<ApiResponse<VendorServiceBridgeResponse>> create(@RequestBody VendorServiceRequest request) {
        VendorServiceBridgeResponse response = vendorServiceBridgeService.createVendorServiceBridge(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor service bridge created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorServiceBridgeResponse>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(vendorServiceBridgeService.getVendorServiceBridgeById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorServiceBridgeResponse>> update(@PathVariable Integer id, @RequestBody VendorServiceBridge updated) {
        VendorServiceBridgeResponse response = vendorServiceBridgeService.updateVendorServiceBridge(id, updated);
        return ResponseEntity.ok(ApiResponse.success("Vendor service bridge updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        vendorServiceBridgeService.deleteVendorServiceBridge(id);
        return ResponseEntity.ok(ApiResponse.success("Vendor service bridge deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorServiceBridgeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(vendorServiceBridgeService.getAllVendorServiceBridges()));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<VendorServiceBridgeResponse>>> getByVendor(@PathVariable Integer vendorId) {
        return ResponseEntity.ok(ApiResponse.success(vendorServiceBridgeService.getByVendorId(vendorId)));
    }
}
