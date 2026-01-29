package com.autoadda.apis.other.repository;

import java.util.List;

import com.autoadda.apis.other.entity.VendorService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorServiceRepository extends JpaRepository<VendorService, Integer> {

    List<VendorService> findByVendorId(Integer vendorId);

    List<VendorService> findByIsActive(Boolean isActive);

    List<VendorService> findByIsActiveAndVendorId(Boolean isActive, Integer vendorId);

    List<VendorService> findByServiceCategoryNameContainingIgnoreCase(String name);

    List<VendorService> findByServiceCategoryNameContainingIgnoreCaseAndIsActive(String name, Boolean isActive);

    List<VendorService> findByServiceCategoryNameContainingIgnoreCaseAndIsActiveAndVendorId(String name,
                                                                                                    Boolean isActive, Integer vendorId);

    List<VendorService> findByServiceCategoryId(Integer serviceId);

    List<VendorService> findByVendorIdAndIsActive(Integer vendorId, Boolean isActive);

    List<VendorService> findByVendorIdAndServiceCategoryId(Integer vendorId, Integer serviceId);
}
