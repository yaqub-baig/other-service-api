package com.autoadda.apis.other.service;

import com.autoadda.apis.other.entity.VendorService;
import com.autoadda.apis.other.exception.ResourceNotFoundException;
import com.autoadda.apis.other.mapper.VendorServiceMapper;
import com.autoadda.apis.other.repository.ServiceCategoryRepository;
import com.autoadda.apis.other.repository.VendorServiceRepository;
import com.autoadda.apis.other.repository.VendorRepository;
import com.autoadda.apis.other.request.VendorServiceRequest;
import com.autoadda.apis.other.response.VendorServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorServiceAssignmentService {
    private final VendorRepository vendorRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final VendorServiceRepository vendorServiceRepository;
    private final VendorServiceMapper vendorServiceMapper;

    @Transactional
    public VendorServiceResponse createVendorService(VendorServiceRequest request) {
        VendorService saved = createVendorServiceEntity(request);
        return vendorServiceMapper.toResponse(saved);
    }

    /**
     * Creates the vendor ↔ category row, or reactivates and updates it when the vendor already has one
     * (one row per vendor and category). Used by VendorManagementService too.
     */
    @Transactional
    public VendorService createVendorServiceEntity(VendorServiceRequest request) {
        VendorService vendorService = vendorServiceRepository
                .findByVendorIdAndServiceCategoryId(request.getVendorId(), request.getServiceCategoryId())
                .stream().findFirst()
                .orElseGet(() -> {
                    VendorService created = new VendorService();
                    created.setVendor(vendorRepository.findById(request.getVendorId())
                            .orElseThrow(() -> new ResourceNotFoundException("Vendor not found")));
                    created.setServiceCategory(serviceCategoryRepository.findById(request.getServiceCategoryId())
                            .orElseThrow(() -> new ResourceNotFoundException("Service category not found")));
                    return created;
                });
        vendorService.setIsActive(request.isActive());
        applyPricing(vendorService, request);
        return vendorServiceRepository.save(vendorService);
    }

    public VendorServiceResponse getVendorServiceById(Integer id) {
        return vendorServiceMapper.toResponse(findEntity(id));
    }

    @Transactional
    public VendorServiceResponse updateVendorService(Integer id, VendorServiceRequest request) {
        VendorService existing = findEntity(id);
        existing.setIsActive(request.isActive());
        if (request.getServiceCategoryId() != null) {
            existing.setServiceCategory(serviceCategoryRepository.findById(request.getServiceCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service category not found")));
        }
        applyPricing(existing, request);
        return vendorServiceMapper.toResponse(vendorServiceRepository.save(existing));
    }

    /**
     * Deactivates rather than deletes: bookings and offerings keep pointing at this row.
     */
    @Transactional
    public void deactivateVendorService(Integer id) {
        VendorService existing = findEntity(id);
        existing.setIsActive(false);
        vendorServiceRepository.save(existing);
    }

    public List<VendorServiceResponse> getAllVendorServices() {
        return vendorServiceMapper.toResponseList(vendorServiceRepository.findAll());
    }

    public List<VendorServiceResponse> getByVendorId(Integer vendorId) {
        return vendorServiceMapper.toResponseList(vendorServiceRepository.findByVendorId(vendorId));
    }

    private VendorService findEntity(Integer id) {
        return vendorServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor service not found with id: " + id));
    }

    private void applyPricing(VendorService target, VendorServiceRequest request) {
        if (request.getPricingType() != null) target.setPricingType(request.getPricingType());
        if (request.getPrice() != null) target.setPrice(request.getPrice());
        if (request.getMinPrice() != null) target.setMinPrice(request.getMinPrice());
        if (request.getMaxPrice() != null) target.setMaxPrice(request.getMaxPrice());
        if (request.getCurrency() != null) target.setCurrency(request.getCurrency());
        if (request.getDescription() != null) target.setDescription(request.getDescription());
    }
}
