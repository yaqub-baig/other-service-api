package com.autoadda.apis.other.controller.vendor;

import com.autoadda.apis.other.request.vendor.CarWashVendorRequest;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.service.VendorManagementService;
import com.autoadda.apis.other.util.VendorServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Onboarding + management endpoints for Car Wash vendors.
 *
 * <p>Reference implementation of the per-service pattern: a thin controller with
 * its own request DTO that delegates to the shared {@link VendorManagementService}
 * with a fixed {@link VendorServiceType}. Generic reads (GET /vendors/&#123;id&#125;,
 * /vendors/search, bookings) stay on the shared controllers.</p>
 */
@RestController
@RequestMapping("/vendors/car-wash")
@RequiredArgsConstructor
public class CarWashVendorController {

    private static final VendorServiceType TYPE = VendorServiceType.CAR_WASH;

    private final VendorManagementService vendorService;

    @PostMapping
    public ResponseEntity<VendorProfileResponse> create(
            @RequestHeader("X-Auto-Adda-Id") String autoAddaId,
            @RequestBody CarWashVendorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vendorService.createVendor(request, TYPE, autoAddaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorProfileResponse> update(
            @PathVariable Integer id,
            @RequestBody CarWashVendorRequest request) {
        return ResponseEntity.ok(vendorService.updateVendor(id, request));
    }

    @GetMapping
    public ResponseEntity<List<VendorProfileResponse>> mine(
            @RequestHeader("X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok(vendorService.getVendorsByOwnerIdAndType(autoAddaId, TYPE));
    }

    @GetMapping("/all")
    public ResponseEntity<List<VendorProfileResponse>> all() {
        return ResponseEntity.ok(vendorService.findVendorsByServiceType(TYPE.name()));
    }
}
