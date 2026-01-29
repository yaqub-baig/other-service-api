package com.autoadda.apis.other.repository;

import java.time.DayOfWeek;
import java.util.Optional;

import com.autoadda.apis.other.entity.OsWorkHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OsWorkHourRepository extends JpaRepository<OsWorkHours, Integer> {
    Optional<OsWorkHours> findByVendorIdAndDayOfWeek(Integer vendorId, DayOfWeek dayOfWeek);

    @Query("SELECT g FROM OsWorkHours g WHERE g.vendor.id = :vendorId AND g.dayOfWeek = :dayOfWeek")
    Optional<OsWorkHours> findByVendorDetailsIdAndDayOfWeek(@Param("vendorId") Integer vendorId, @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
