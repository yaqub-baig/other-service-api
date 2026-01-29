package com.autoadda.apis.other.controller;


import com.autoadda.apis.other.entity.VendorServiceBridge;
import com.autoadda.apis.other.request.GarageServiceRequest;
import com.autoadda.apis.other.service.VendorServiceBridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vendor-service-bridges")
@RequiredArgsConstructor
public class VendorServiceBridgeController {

    private final VendorServiceBridgeService vendorServiceBridgeService;

    @PostMapping
    public ResponseEntity<VendorServiceBridge> create(@RequestBody GarageServiceRequest request) {
        return ResponseEntity.ok(vendorServiceBridgeService.createVendorServiceBridge(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<VendorServiceBridge>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(vendorServiceBridgeService.getVendorServiceBridgeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorServiceBridge> update(@PathVariable Integer id, @RequestBody VendorServiceBridge updated) {
        return ResponseEntity.ok(vendorServiceBridgeService.updateVendorServiceBridge(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        vendorServiceBridgeService.deleteVendorServiceBridge(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<VendorServiceBridge>> getAll() {
        return ResponseEntity.ok(vendorServiceBridgeService.getAllVendorServiceBridges());
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<VendorServiceBridge>> getByVendor(@PathVariable Integer vendorId) {
        return ResponseEntity.ok(vendorServiceBridgeService.getByVendorId(vendorId));
    }
}
