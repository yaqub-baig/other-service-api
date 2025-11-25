package com.autoadda.apis.other.repository.os;

import java.time.DayOfWeek;
import java.util.List;

import com.autoadda.apis.other.entity.os.OsBreakTime;
import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.other.entity.GarageBreakTime;
import org.springframework.stereotype.Repository;


@Repository
public interface OsBreakTimeRepository extends JpaRepository<OsBreakTime, Integer> {
//    List<GarageBreakTime> findByGarageDetailsIdAndDayOfWeek(Integer garageId, DayOfWeek dayOfWeek);


    List<OsBreakTime> findByOtherServiceIdAndDayOfWeek(Integer otherServiceId, DayOfWeek dayOfWeek);


}