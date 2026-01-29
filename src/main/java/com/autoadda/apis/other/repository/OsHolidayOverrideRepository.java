package com.autoadda.apis.other.repository;


import java.time.LocalDate;
import java.util.Optional;

import com.autoadda.apis.other.entity.OsHolidayOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OsHolidayOverrideRepository extends JpaRepository<OsHolidayOverride, Integer> {
    Optional<OsHolidayOverride> findByVendorIdAndDate(Integer vendorId, LocalDate date);
}
