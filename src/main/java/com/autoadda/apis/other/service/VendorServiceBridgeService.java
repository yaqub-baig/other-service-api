package com.autoadda.apis.other.service;


import com.autoadda.apis.other.entity.VendorServiceBridge;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.VendorServiceBridgeMapper;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorServiceBridgeRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.request.GarageServiceRequest;
import com.autoadda.apis.other.response.VendorServiceBridgeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorServiceBridgeService {
    private final VendorRepository vendorRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final VendorServiceBridgeRepository vendorServiceBridgeRepository;
    private final VendorServiceBridgeMapper bridgeMapper;

    private VendorServiceBridge mapToEntity(GarageServiceRequest request) {
        VendorServiceBridge bridge = new VendorServiceBridge();
        bridge.setIsActive(request.isActive());
        bridge.setVendor(vendorRepository.findById(request.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found")));
        bridge.setServiceCategory(serviceCategoryRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found")));
        return bridge;
    }

    public VendorServiceBridgeResponse createVendorServiceBridge(GarageServiceRequest request) {
        VendorServiceBridge saved = createVendorServiceBridgeEntity(request);
        return bridgeMapper.toResponse(saved);
    }

    /**
     * Internal method for creating and returning the entity (used by VendorManagementService).
     */
    public VendorServiceBridge createVendorServiceBridgeEntity(GarageServiceRequest request) {
        return vendorServiceBridgeRepository.save(mapToEntity(request));
    }

    public VendorServiceBridgeResponse getVendorServiceBridgeById(Integer id) {
        VendorServiceBridge bridge = vendorServiceBridgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor service bridge not found with id: " + id));
        return bridgeMapper.toResponse(bridge);
    }

    public VendorServiceBridgeResponse updateVendorServiceBridge(Integer id, VendorServiceBridge updated) {
        VendorServiceBridge existing = vendorServiceBridgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor service bridge not found with ID: " + id));

        existing.setIsActive(updated.getIsActive());
        existing.setServiceCategory(updated.getServiceCategory());
        existing.setVendor(updated.getVendor());
        VendorServiceBridge saved = vendorServiceBridgeRepository.save(existing);
        return bridgeMapper.toResponse(saved);
    }

    public void deleteVendorServiceBridge(Integer id) {
        if (!vendorServiceBridgeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vendor service bridge not found with id: " + id);
        }
        vendorServiceBridgeRepository.deleteById(id);
    }

    public List<VendorServiceBridgeResponse> getAllVendorServiceBridges() {
        List<VendorServiceBridge> bridges = vendorServiceBridgeRepository.findAll();
        return bridgeMapper.toResponseList(bridges);
    }

    public List<VendorServiceBridgeResponse> getByVendorId(Integer vendorId) {
        List<VendorServiceBridge> bridges = vendorServiceBridgeRepository.findByVendorId(vendorId);
        return bridgeMapper.toResponseList(bridges);
    }
}
