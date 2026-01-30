package com.autoadda.apis.other.repository;

import java.time.DayOfWeek;
import java.util.Optional;

import com.autoadda.apis.other.entity.VendorWorkHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorWorkHoursRepository extends JpaRepository<VendorWorkHours, Integer> {
    Optional<VendorWorkHours> findByVendorIdAndDayOfWeek(Integer vendorId, DayOfWeek dayOfWeek);

    @Query("SELECT g FROM VendorWorkHours g WHERE g.vendor.id = :vendorId AND g.dayOfWeek = :dayOfWeek")
    Optional<VendorWorkHours> findByVendorDetailsIdAndDayOfWeek(@Param("vendorId") Integer vendorId, @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
