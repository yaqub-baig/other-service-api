package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.VendorServiceBridgeMapper;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorServiceRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.request.GarageServiceRequest;
import com.autoadda.apis.other.response.VendorServiceBridgeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorServiceBridgeService {
    private final VendorRepository vendorRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final VendorServiceRepository vendorServiceRepository;
    private final VendorServiceBridgeMapper bridgeMapper;

    @Transactional
    public VendorServiceBridgeResponse createVendorServiceBridge(GarageServiceRequest request) {
        VendorService saved = createVendorServiceBridgeEntity(request);
        return bridgeMapper.toResponse(saved);
    }

    /**
     * Creates the vendor ↔ category row, or reactivates and updates it when the vendor already has one
     * (one row per vendor and category). Used by VendorManagementService too.
     */
    @Transactional
    public VendorService createVendorServiceBridgeEntity(GarageServiceRequest request) {
        VendorService vendorService = vendorServiceRepository
                .findByVendorIdAndServiceCategoryId(request.getGarageId(), request.getServiceId())
                .stream().findFirst()
                .orElseGet(() -> {
                    VendorService created = new VendorService();
                    created.setVendor(vendorRepository.findById(request.getGarageId())
                            .orElseThrow(() -> new ResourceNotFoundException("Vendor not found")));
                    created.setServiceCategory(serviceCategoryRepository.findById(request.getServiceId())
                            .orElseThrow(() -> new ResourceNotFoundException("Service category not found")));
                    return created;
                });
        vendorService.setIsActive(request.isActive());
        applyPricing(vendorService, request);
        return vendorServiceRepository.save(vendorService);
    }

    public VendorServiceBridgeResponse getVendorServiceBridgeById(Integer id) {
        return bridgeMapper.toResponse(findEntity(id));
    }

    @Transactional
    public VendorServiceBridgeResponse updateVendorServiceBridge(Integer id, GarageServiceRequest request) {
        VendorService existing = findEntity(id);
        existing.setIsActive(request.isActive());
        if (request.getServiceId() != null) {
            existing.setServiceCategory(serviceCategoryRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service category not found")));
        }
        applyPricing(existing, request);
        return bridgeMapper.toResponse(vendorServiceRepository.save(existing));
    }

    /**
     * Deactivates rather than deletes: bookings and offerings keep pointing at this row.
     */
    @Transactional
    public void deleteVendorServiceBridge(Integer id) {
        VendorService existing = findEntity(id);
        existing.setIsActive(false);
        vendorServiceRepository.save(existing);
    }

    public List<VendorServiceBridgeResponse> getAllVendorServiceBridges() {
        return bridgeMapper.toResponseList(vendorServiceRepository.findAll());
    }

    public List<VendorServiceBridgeResponse> getByVendorId(Integer vendorId) {
        return bridgeMapper.toResponseList(vendorServiceRepository.findByVendorId(vendorId));
    }

    private VendorService findEntity(Integer id) {
        return vendorServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor service bridge not found with id: " + id));
    }

    private void applyPricing(VendorService target, GarageServiceRequest request) {
        if (request.getPricingType() != null) target.setPricingType(request.getPricingType());
        if (request.getPrice() != null) target.setPrice(request.getPrice());
        if (request.getMinPrice() != null) target.setMinPrice(request.getMinPrice());
        if (request.getMaxPrice() != null) target.setMaxPrice(request.getMaxPrice());
        if (request.getCurrency() != null) target.setCurrency(request.getCurrency());
        if (request.getDescription() != null) target.setDescription(request.getDescription());
    }
}
