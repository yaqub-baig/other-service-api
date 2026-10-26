package com.autoadda.apis.other.controller;

import com.autoadda.apis.other.request.vendor.VendorCompanyRequest;
import com.autoadda.apis.other.request.vendor.VendorOnboardingRequest;
import com.autoadda.apis.other.request.vendor.VendorServiceOnboardingRequest;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.response.VendorOnboardingResponse;
import com.autoadda.apis.other.response.VendorResponse;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.response.VendorServiceResponse;
import com.autoadda.apis.other.service.VendorManagementService;
import com.autoadda.apis.other.util.VendorServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Vendor company endpoints, and the services a vendor adds (each with its own
 * hours, licence, slots and details). Every service type goes through the same
 * endpoints; {@code serviceType} in the body picks the category and rules.
 * Pricing for a vendor service is managed under {@code /vendor-services}.
 */
@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorManagementService vendorService;

    @PostMapping
    public ResponseEntity<VendorProfileResponse> createCompany(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody VendorCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendorService.createCompany(request, autoAddaId));
    }

    @PostMapping("/onboard")
    public ResponseEntity<VendorOnboardingResponse> onboard(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody VendorOnboardingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendorService.onboard(request.getCompany(), request.getService(), autoAddaId));
    }

    @PostMapping("/{vendorId}/services")
    public ResponseEntity<VendorServiceResponse> addService(
            @PathVariable Integer vendorId,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody VendorServiceOnboardingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendorService.addService(vendorId, request, autoAddaId));
    }

    @GetMapping("/{vendorId}/services")
    public ResponseEntity<List<VendorServiceResponse>> getServicesByVendor(@PathVariable Integer vendorId) {
        return ResponseEntity.ok(vendorService.getServicesByVendor(vendorId));
    }

    @PutMapping("/services/{vendorServiceId}")
    public ResponseEntity<VendorServiceResponse> updateService(
            @PathVariable Integer vendorServiceId,
            @RequestBody VendorServiceOnboardingRequest request) {
        return ResponseEntity.ok(vendorService.updateService(vendorServiceId, request));
    }

    @GetMapping("/services")
    public ResponseEntity<List<VendorServiceResponse>> getMyServices(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestParam(required = false) VendorServiceType serviceType) {
        return ResponseEntity.ok(vendorService.getServicesByOwner(autoAddaId, serviceType));
    }

    @GetMapping("/services/all")
    public ResponseEntity<List<VendorServiceResponse>> getAllServices(
            @RequestParam(required = false) VendorServiceType serviceType) {
        return ResponseEntity.ok(vendorService.getServices(serviceType));
    }

    /** The details form for a service type, e.g. bay count for a car wash. */
    @GetMapping("/services/attributes")
    public ResponseEntity<List<ServiceAttributeDefinitionResponse>> getServiceAttributes(
            @RequestParam VendorServiceType serviceType) {
        return ResponseEntity.ok(vendorService.getVendorAttributes(serviceType));
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
    public ResponseEntity<VendorProfileResponse> updateCompany(
            @PathVariable Integer id,
            @RequestBody VendorCompanyRequest request) {
        return ResponseEntity.ok(vendorService.updateCompany(id, request));
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

    @GetMapping("/search")
    public ResponseEntity<List<VendorResponse>> searchVendors(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) List<Integer> serviceCategoryIds) {
        return ResponseEntity.ok(vendorService.searchVendors(city, zip, vendorName, serviceCategoryIds));
    }
}
