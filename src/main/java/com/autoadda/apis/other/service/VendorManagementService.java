package com.autoadda.apis.other.service;


import com.autoadda.apis.other.request.vendor.VendorServiceOnboardingRequest;
import com.autoadda.apis.other.request.vendor.VendorCompanyRequest;
import com.autoadda.apis.other.response.ServiceAttributeDefinitionResponse;
import com.autoadda.apis.other.response.VendorOnboardingResponse;
import com.autoadda.apis.other.response.VendorProfileResponse;
import com.autoadda.apis.other.response.VendorResponse;
import com.autoadda.apis.other.response.VendorServiceResponse;
import com.autoadda.apis.other.util.Status;
import com.autoadda.apis.other.util.VendorServiceType;

import java.util.List;
import java.util.Optional;

public interface VendorManagementService {

    // ---- company ----

    VendorProfileResponse createCompany(VendorCompanyRequest request, String autoAddaId);

    VendorProfileResponse updateCompany(Integer vendorId, VendorCompanyRequest request);

    void deleteVendor(Integer vendorId);

    List<VendorProfileResponse> getAllVendors();

    List<VendorProfileResponse> getVendorsByOwnerId(String autoAddaId);

    Optional<VendorProfileResponse> getVendorById(Integer id);

    List<VendorProfileResponse> findVendorsByBrandName(String name);

    List<VendorProfileResponse> findVendorsByStatus(Status status);

    Optional<VendorProfileResponse> findVendorByEmail(String email);

    Optional<VendorProfileResponse> findVendorByContact(String contact);

    List<VendorResponse> searchVendors(String city, String zip, String vendorName, List<Integer> serviceCategoryIds);

    // ---- services ----

    /**
     * Create the company and its first service in one call.
     */
    VendorOnboardingResponse onboard(VendorCompanyRequest company, VendorServiceOnboardingRequest service,
                                     String autoAddaId);

    /**
     * Add a service (with its own hours, capacity, licence, location, slots and details) to a vendor.
     * The request's serviceType picks the category and the onboarding rules.
     */
    VendorServiceResponse addService(Integer vendorId, VendorServiceOnboardingRequest request, String autoAddaId);

    VendorServiceResponse updateService(Integer vendorServiceId, VendorServiceOnboardingRequest request);

    void deleteService(Integer vendorServiceId);

    VendorServiceResponse getService(Integer vendorServiceId);

    List<VendorServiceResponse> getServicesByVendor(Integer vendorId);

    /** The caller's active services; all types when type is null. */
    List<VendorServiceResponse> getServicesByOwner(String autoAddaId, VendorServiceType type);

    /** Every active service; all types when type is null. */
    List<VendorServiceResponse> getServices(VendorServiceType type);

    /** The details form for a service type: its category's VENDOR attributes. */
    List<ServiceAttributeDefinitionResponse> getVendorAttributes(VendorServiceType type);
}
