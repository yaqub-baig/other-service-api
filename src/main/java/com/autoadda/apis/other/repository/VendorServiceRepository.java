package com.autoadda.apis.other.repository;

import java.util.List;

import com.autoadda.apis.other.entity.VendorService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorServiceRepository extends JpaRepository<VendorService, Integer> {

    //find by id



    List<VendorService> findByVendorId(Integer vendorId);

    List<VendorService> findByIsActive(Boolean isActive);

    List<VendorService> findByIsActiveAndVendorId(Boolean isActive, Integer vendorId);

    List<VendorService> findByAvailableService_NameContainingIgnoreCase(String name);

    List<VendorService> findByAvailableService_NameContainingIgnoreCaseAndIsActive(String name, Boolean isActive);

    List<VendorService> findByAvailableService_NameContainingIgnoreCaseAndIsActiveAndVendorId(String name,
                                                                                                    Boolean isActive, Integer vendorId);

    List<VendorService> findByAvailableService_Id(Integer serviceId);

    List<VendorService> findByVendor_IdAndIsActive(Integer vendorId, Boolean isActive);

    List<VendorService> findByVendor_IdAndAvailableService_Id(Integer vendorId, Integer serviceId);
}
