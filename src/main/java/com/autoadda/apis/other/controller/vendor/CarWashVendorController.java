package com.autoadda.apis.other.controller.vendor;

import com.autoadda.apis.other.request.vendor.CarWashVendorRequest;
import com.autoadda.apis.other.request.vendor.CarWashVendorServiceRequest;
import com.autoadda.apis.other.response.vendor.CarWashVendorResponse;
import com.autoadda.apis.other.service.vendor.CarWashVendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Car Wash onboarding + management.
 *
 * <ul>
 *   <li>{@code POST /vendors/car-wash} — create company + first car-wash service</li>
 *   <li>{@code POST /vendors/{vendorId}/services/car-wash} — add a car-wash service to an existing company</li>
 *   <li>{@code PUT /vendors/car-wash/{vendorServiceId}} — update a car-wash service</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
public class CarWashVendorController {

    private final CarWashVendorService carWashVendorService;

    @PostMapping("/vendors/car-wash")
    public ResponseEntity<CarWashVendorResponse> create(
            @RequestHeader("X-Auto-Adda-Id") String autoAddaId,
            @RequestBody CarWashVendorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carWashVendorService.create(request, autoAddaId));
    }

    @PostMapping("/vendors/{vendorId}/services/car-wash")
    public ResponseEntity<CarWashVendorResponse> addToCompany(
            @PathVariable Integer vendorId,
            @RequestHeader("X-Auto-Adda-Id") String autoAddaId,
            @RequestBody CarWashVendorServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carWashVendorService.addToCompany(vendorId, request, autoAddaId));
    }

    @PutMapping("/vendors/car-wash/{vendorServiceId}")
    public ResponseEntity<CarWashVendorResponse> update(
            @PathVariable Integer vendorServiceId,
            @RequestBody CarWashVendorServiceRequest request) {
        return ResponseEntity.ok(carWashVendorService.updateService(vendorServiceId, request));
    }

    @GetMapping("/vendors/car-wash")
    public ResponseEntity<List<CarWashVendorResponse>> mine(
            @RequestHeader("X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok(carWashVendorService.getMine(autoAddaId));
    }

    @GetMapping("/vendors/car-wash/all")
    public ResponseEntity<List<CarWashVendorResponse>> all() {
        return ResponseEntity.ok(carWashVendorService.getAll());
    }
}
