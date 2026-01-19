package com.autoadda.apis.other.repository;

import java.time.DayOfWeek;
import java.util.List;

import com.autoadda.apis.other.entity.OsBreakTime;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


@Repository
public interface OsBreakTimeRepository extends JpaRepository<OsBreakTime, Integer> {
//    List<GarageBreakTime> findByGarageDetailsIdAndDayOfWeek(Integer garageId, DayOfWeek dayOfWeek);


    List<OsBreakTime> findByOtherServiceIdAndDayOfWeek(Integer otherServiceId, DayOfWeek dayOfWeek);


}