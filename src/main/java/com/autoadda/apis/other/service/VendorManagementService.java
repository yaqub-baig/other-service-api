package com.autoadda.apis.other.service;


import com.autoadda.apis.other.entity.Vendor;
import com.autoadda.apis.other.request.OtherServiceRequest;
import com.autoadda.apis.other.response.OsResponse;
import com.autoadda.apis.other.util.Status;

import java.util.List;
import java.util.Optional;

public interface VendorManagementService {

    /**
     * Create a new vendor
     */
    Vendor createVendor(OtherServiceRequest request, String autoAddaId);

    /**
     * Get all vendors
     */
    List<Vendor> getAllVendors();

    /**
     * Get all vendors by owner ID
     */
    List<Vendor> getVendorsByOwnerId(String autoAddaId);

    /**
     * Get vendor by ID
     */
    Optional<Vendor> getVendorById(Integer id);

    /**
     * Update an existing vendor
     */
    Vendor updateVendor(Integer id, OtherServiceRequest request);

    /**
     * Delete a vendor
     */
    void deleteVendor(Integer id);

    /**
     * Find vendors by brand name
     */
    List<Vendor> findVendorsByBrandName(String name);

    /**
     * Find vendors by service type
     */
    List<Vendor> findVendorsByServiceType(String serviceType);

    /**
     * Find vendors by status
     */
    List<Vendor> findVendorsByStatus(Status status);

    /**
     * Find vendor by email
     */
    Optional<Vendor> findVendorByEmail(String email);

    /**
     * Find vendor by contact number
     */
    Optional<Vendor> findVendorByContact(String contact);

    /**
     * Search vendors by criteria
     */
    List<OsResponse> searchVendors(String city, String zip, String vendorName, List<Integer> serviceCategoryIds);
}
