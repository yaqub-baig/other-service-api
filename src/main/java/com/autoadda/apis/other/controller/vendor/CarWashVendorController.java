package com.autoadda.apis.other.controller.vendor;

import com.autoadda.apis.other.request.vendor.CarWashVendorRequest;
import com.autoadda.apis.other.response.vendor.CarWashVendorResponse;
import com.autoadda.apis.other.service.vendor.CarWashVendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Onboarding + management endpoints for Car Wash vendors.
 *
 * <p>Per-service pattern: thin controller with its own request/response DTOs
 * delegating to {@link CarWashVendorService}, which handles the shared vendor
 * lifecycle plus the {@code car_wash_details} table. Generic reads
 * (GET /vendors/&#123;id&#125;, /vendors/search, bookings) stay on the shared
 * controllers.</p>
 *
 * <p>2-step form: step 1 POSTs the base company fields, step 2 PUTs the
 * operational fields (working days/hours) + the car-wash detail fields.</p>
 */
@RestController
@RequestMapping("/vendors/car-wash")
@RequiredArgsConstructor
public class CarWashVendorController {

    private final CarWashVendorService carWashVendorService;

    @PostMapping
    public ResponseEntity<CarWashVendorResponse> create(
            @RequestHeader("X-Auto-Adda-Id") String autoAddaId,
            @RequestBody CarWashVendorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carWashVendorService.create(request, autoAddaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarWashVendorResponse> update(
            @PathVariable Integer id,
            @RequestBody CarWashVendorRequest request) {
        return ResponseEntity.ok(carWashVendorService.update(id, request));
    }

    @GetMapping
    public ResponseEntity<List<CarWashVendorResponse>> mine(
            @RequestHeader("X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok(carWashVendorService.getMine(autoAddaId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CarWashVendorResponse>> all() {
        return ResponseEntity.ok(carWashVendorService.getAll());
    }
}
