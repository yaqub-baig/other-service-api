package com.autoadda.apis.other.repository;


import com.autoadda.apis.other.entity.VendorService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorServiceRepository extends JpaRepository<VendorService, Integer> {

    List<VendorService> findByVendorId(Integer vendorId);

    List<VendorService> findByVendorIdAndServiceCategoryId(Integer vendorId, Integer serviceCategoryId);

    List<VendorService> findByVendorIdAndServiceType(Integer vendorId, String serviceType);

    List<VendorService> findByIsActiveTrue();

    List<VendorService> findByServiceTypeAndIsActiveTrue(String serviceType);

    List<VendorService> findByVendor_CompanyOwnerAndServiceTypeAndIsActiveTrue(Integer companyOwner, String serviceType);
}
