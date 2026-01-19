package com.autoadda.apis.other.repository;


import com.autoadda.apis.other.entity.OsAppointmentStatusActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OsAppointmentStatusActivityRepository extends JpaRepository<OsAppointmentStatusActivity, Integer>{

}