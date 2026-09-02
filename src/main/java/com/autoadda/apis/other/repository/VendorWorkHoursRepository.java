package com.autoadda.apis.other.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import com.autoadda.apis.other.entity.VendorWorkHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorWorkHoursRepository extends JpaRepository<VendorWorkHours, Integer> {

    Optional<VendorWorkHours> findByVendorServiceIdAndDayOfWeek(Integer vendorServiceId, DayOfWeek dayOfWeek);

    List<VendorWorkHours> findByVendorServiceId(Integer vendorServiceId);
}
