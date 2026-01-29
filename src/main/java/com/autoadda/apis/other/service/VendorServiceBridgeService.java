package com.autoadda.apis.other.service;


import com.autoadda.apis.other.entity.VendorServiceBridge;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorServiceBridgeRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.request.GarageServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VendorServiceBridgeService {
    private final VendorRepository vendorRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final VendorServiceBridgeRepository vendorServiceBridgeRepository;

    private VendorServiceBridge mapToEntity(GarageServiceRequest request) {
        VendorServiceBridge bridge = new VendorServiceBridge();
        bridge.setIsActive(request.isActive());
        bridge.setVendor(vendorRepository.findById(request.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found")));
        bridge.setServiceCategory(serviceCategoryRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found")));
        return bridge;
    }

    public VendorServiceBridge createVendorServiceBridge(GarageServiceRequest request) {
        return vendorServiceBridgeRepository.save(mapToEntity(request));
    }

    public Optional<VendorServiceBridge> getVendorServiceBridgeById(Integer id) {
        return vendorServiceBridgeRepository.findById(id);
    }

    public VendorServiceBridge updateVendorServiceBridge(Integer id, VendorServiceBridge updated) {
        return vendorServiceBridgeRepository.findById(id).map(existing -> {
            existing.setIsActive(updated.getIsActive());
            existing.setServiceCategory(updated.getServiceCategory());
            existing.setVendor(updated.getVendor());
            return vendorServiceBridgeRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Vendor service bridge not found with ID: " + id));
    }

    public void deleteVendorServiceBridge(Integer id) {
        vendorServiceBridgeRepository.deleteById(id);
    }

    public List<VendorServiceBridge> getAllVendorServiceBridges() {
        return vendorServiceBridgeRepository.findAll();
    }

    public List<VendorServiceBridge> getByVendorId(Integer vendorId) {
        return vendorServiceBridgeRepository.findByVendorId(vendorId);
    }
}
