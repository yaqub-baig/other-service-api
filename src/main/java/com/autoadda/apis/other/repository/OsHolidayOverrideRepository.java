package com.autoadda.apis.other.repository;


import java.time.LocalDate;
import java.util.Optional;

import com.autoadda.apis.other.entity.OsHolidayOverride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OsHolidayOverrideRepository extends JpaRepository<OsHolidayOverride, Integer> {
    Optional<OsHolidayOverride> findByOtherServiceIdAndDate(Integer otherServiceId, LocalDate date);
}
