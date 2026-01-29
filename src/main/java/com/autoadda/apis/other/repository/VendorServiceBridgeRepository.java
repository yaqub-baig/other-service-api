package com.autoadda.apis.other.repository;


import com.autoadda.apis.other.entity.VendorServiceBridge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorServiceBridgeRepository extends JpaRepository<VendorServiceBridge, Integer> {
    List<VendorServiceBridge> findByVendorId(Integer vendorId);
    List<VendorServiceBridge> findByVendorIdAndServiceCategoryId(Integer vendorId, Integer serviceCategoryId);
}
