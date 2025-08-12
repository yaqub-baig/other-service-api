package com.autoadda.apis.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.other.entity.FutureAppointment;
import com.autoadda.apis.other.util.Status;

public interface FutureAppointmentRepository extends JpaRepository<FutureAppointment, Integer> {
	boolean existsByVehicleIdAndServiceTypeAndStatus(Integer vehicleId, String serviceType, Status status);
}