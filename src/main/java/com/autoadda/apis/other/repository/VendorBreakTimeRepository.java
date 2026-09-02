package com.autoadda.apis.other.repository;

import java.time.DayOfWeek;
import java.util.List;

import com.autoadda.apis.other.entity.VendorBreakTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VendorBreakTimeRepository extends JpaRepository<VendorBreakTime, Integer> {
    List<VendorBreakTime> findByVendorServiceIdAndDayOfWeek(Integer vendorServiceId, DayOfWeek dayOfWeek);

    List<VendorBreakTime> findByVendorServiceId(Integer vendorServiceId);
}
