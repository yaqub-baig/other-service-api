package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.request.OtherServiceRequest;
import com.autoadda.apis.other.response.OsResponse;
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
    public ResponseEntity<?> createVendor(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody OtherServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendorService.createVendor(request, autoAddaId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Vendor>> getAllVendors() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    @GetMapping
    public ResponseEntity<List<Vendor>> getVendorsByOwnerId(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok(vendorService.getVendorsByOwnerId(autoAddaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vendor> getVendorById(@PathVariable Integer id) {
        return vendorService.getVendorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vendor> updateVendor(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody OtherServiceRequest request) {
        return ResponseEntity.ok(vendorService.updateVendor(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Integer id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/brandname/{name}")
    public ResponseEntity<List<Vendor>> searchByBrandName(@PathVariable String name) {
        return ResponseEntity.ok(vendorService.findVendorsByBrandName(name));
    }

    @GetMapping("/service/{serviceType}")
    public ResponseEntity<List<Vendor>> getVendorsByServiceType(@PathVariable String serviceType) {
        return ResponseEntity.ok(vendorService.findVendorsByServiceType(serviceType));
    }

    @GetMapping("/search")
    public ResponseEntity<List<OsResponse>> searchVendors(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) List<Integer> serviceCategoryIds) {
        return ResponseEntity.ok(vendorService.searchVendors(city, zip, vendorName, serviceCategoryIds));
    }
}
