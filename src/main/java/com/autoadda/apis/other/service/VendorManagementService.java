package com.autoadda.apis.other.service;


import com.autoadda.apis.other.request.OtherServiceRequest;
import com.autoadda.apis.other.request.vendor.BaseVendorRequest;
import com.autoadda.apis.other.response.VendorResponse;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.util.Status;
import com.autoadda.apis.other.util.VendorServiceType;

import java.util.List;
import java.util.Optional;

public interface VendorManagementService {

    /**
     * Create a new vendor (legacy generic form)
     */
    VendorProfileResponse createVendor(OtherServiceRequest request, String autoAddaId);

    /**
     * Onboard a vendor of a specific service type using the shared basic form.
     * Mirrors garage-api's createGarage: owner validation, working-hours/break
     * rows, owner role update and 60-day slot generation.
     */
    VendorProfileResponse createVendor(BaseVendorRequest request, VendorServiceType type, String autoAddaId);

    /**
     * Update a vendor via the shared basic form (service type is immutable).
     */
    VendorProfileResponse updateVendor(Integer id, BaseVendorRequest request);

    /**
     * List the caller's vendors of a given service type.
     */
    List<VendorProfileResponse> getVendorsByOwnerIdAndType(String autoAddaId, VendorServiceType type);

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
