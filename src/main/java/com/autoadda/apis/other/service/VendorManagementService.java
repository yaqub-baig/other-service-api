package com.autoadda.apis.other.service;


import com.autoadda.apis.other.request.OtherServiceRequest;
import com.autoadda.apis.other.response.VendorResponse;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.util.Status;

import java.util.List;
import java.util.Optional;

public interface VendorManagementService {

    /**
     * Create a new vendor
     */
    VendorProfileResponse createVendor(OtherServiceRequest request, String autoAddaId);

    /**
     * Get all vendors
     */
    List<VendorProfileResponse> getAllVendors();

    /**
     * Get all vendors by owner ID
     */
    List<VendorProfileResponse> getVendorsByOwnerId(String autoAddaId);

    /**
     * Get vendor by ID
     */
    Optional<VendorProfileResponse> getVendorById(Integer id);

    /**
     * Update an existing vendor
     */
    VendorProfileResponse updateVendor(Integer id, OtherServiceRequest request);

    /**
     * Delete a vendor
     */
    void deleteVendor(Integer id);

    /**
     * Find vendors by brand name
     */
    List<VendorProfileResponse> findVendorsByBrandName(String name);

    /**
     * Find vendors by service type
     */
    List<VendorProfileResponse> findVendorsByServiceType(String serviceType);

    /**
     * Find vendors by status
     */
    List<VendorProfileResponse> findVendorsByStatus(Status status);

    /**
     * Find vendor by email
     */
    Optional<VendorProfileResponse> findVendorByEmail(String email);

    /**
     * Find vendor by contact number
     */
    Optional<VendorProfileResponse> findVendorByContact(String contact);

    /**
     * Search vendors by criteria
     */
    List<VendorResponse> searchVendors(String city, String zip, String vendorName, List<Integer> serviceCategoryIds);
}
