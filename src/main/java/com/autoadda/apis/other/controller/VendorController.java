package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.request.OtherServiceRequest;
import com.autoadda.apis.other.response.VendorResponse;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.service.VendorManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorManagementService vendorService;

    @PostMapping
    public ResponseEntity<VendorProfileResponse> createVendor(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody OtherServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendorService.createVendor(request, autoAddaId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<VendorProfileResponse>> getAllVendors() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    @GetMapping
    public ResponseEntity<List<VendorProfileResponse>> getVendorsByOwnerId(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok(vendorService.getVendorsByOwnerId(autoAddaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorProfileResponse> getVendorById(@PathVariable Integer id) {
        return vendorService.getVendorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorProfileResponse> updateVendor(
            @PathVariable Integer id,
            @RequestBody OtherServiceRequest request) {
        // The autoAddaId from the header is not used in the updated service method, so it can be removed.
        return ResponseEntity.ok(vendorService.updateVendor(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Integer id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/brandname/{name}")
    public ResponseEntity<List<VendorProfileResponse>> searchByBrandName(@PathVariable String name) {
        return ResponseEntity.ok(vendorService.findVendorsByBrandName(name));
    }

    @GetMapping("/service/{serviceType}")
    public ResponseEntity<List<VendorProfileResponse>> getVendorsByServiceType(@PathVariable String serviceType) {
        return ResponseEntity.ok(vendorService.findVendorsByServiceType(serviceType));
    }

    @GetMapping("/search")
    public ResponseEntity<List<VendorResponse>> searchVendors(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) List<Integer> serviceCategoryIds) {
        return ResponseEntity.ok(vendorService.searchVendors(city, zip, vendorName, serviceCategoryIds));
    }
}

