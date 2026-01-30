package com.autoadda.apis.other.repository;


import java.time.LocalDate;
import java.util.Optional;

import com.autoadda.apis.other.entity.VendorHolidayOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorHolidayOverrideRepository extends JpaRepository<VendorHolidayOverride, Integer> {
    Optional<VendorHolidayOverride> findByVendorIdAndDate(Integer vendorId, LocalDate date);
}
