package com.service.other.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.other.entity.AppointmentServices;

@Repository
public interface AppointmentServiceRepository extends JpaRepository<AppointmentServices, Integer>{

	List<AppointmentServices> findByAppointmentId(Integer appointmentId);

}
