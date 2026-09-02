package com.autoadda.apis.other.service;


import com.autoadda.apis.other.request.vendor.BaseVendorServiceRequest;
import com.autoadda.apis.other.request.vendor.VendorCompanyRequest;
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
     * Add a service (with its own hours, capacity, licence, location, slots) to a vendor.
     */
    VendorServiceResponse addService(Integer vendorId, BaseVendorServiceRequest request,
                                     VendorServiceType type, String autoAddaId);

    VendorServiceResponse updateService(Integer vendorServiceId, BaseVendorServiceRequest request);

    void deleteService(Integer vendorServiceId);

    VendorServiceResponse getService(Integer vendorServiceId);

    List<VendorServiceResponse> getServicesByVendor(Integer vendorId);

    List<VendorServiceResponse> getServicesByOwnerAndType(String autoAddaId, VendorServiceType type);

    List<VendorServiceResponse> getServicesByType(VendorServiceType type);
}
