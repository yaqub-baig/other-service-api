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

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorServiceCrudService {
    private final VendorRepository vendorRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final VendorServiceRepository vendorServiceRepository;
    private final VendorServiceMapper bridgeMapper;

    private VendorService mapToEntity(VendorServiceRequest request) {
        VendorService bridge = new VendorService();
        bridge.setIsActive(request.isActive());
        bridge.setVendor(vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found")));
        bridge.setServiceCategory(serviceCategoryRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service category not found")));
        return bridge;
    }

    public VendorServiceResponse createVendorService(VendorServiceRequest request) {
        VendorService saved = createVendorServiceEntity(request);
        return bridgeMapper.toResponse(saved);
    }

    /**
     * Internal method for creating and returning the entity (used by VendorManagementService).
     */
    public VendorService createVendorServiceEntity(VendorServiceRequest request) {
        return vendorServiceRepository.save(mapToEntity(request));
    }

    public VendorServiceResponse getVendorServiceById(Integer id) {
        VendorService bridge = vendorServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor service bridge not found with id: " + id));
        return bridgeMapper.toResponse(bridge);
    }

    public VendorServiceResponse updateVendorService(Integer id, VendorService updated) {
        VendorService existing = vendorServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor service bridge not found with ID: " + id));

        existing.setIsActive(updated.getIsActive());
        existing.setServiceCategory(updated.getServiceCategory());
        existing.setVendor(updated.getVendor());
        VendorService saved = vendorServiceRepository.save(existing);
        return bridgeMapper.toResponse(saved);
    }

    public void deleteVendorService(Integer id) {
        if (!vendorServiceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vendor service bridge not found with id: " + id);
        }
        vendorServiceRepository.deleteById(id);
    }

    public List<VendorServiceResponse> getAllVendorServices() {
        List<VendorService> bridges = vendorServiceRepository.findAll();
        return bridgeMapper.toResponseList(bridges);
    }

    public List<VendorServiceResponse> getByVendorId(Integer vendorId) {
        List<VendorService> bridges = vendorServiceRepository.findByVendorId(vendorId);
        return bridgeMapper.toResponseList(bridges);
    }
}
