package com.service.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.other.entity.FutureAppointment;
import com.service.other.util.Status;

public interface FutureAppointmentRepository extends JpaRepository<FutureAppointment, Integer> {
	boolean existsByVehicleIdAndServiceTypeAndStatus(Integer vehicleId, String serviceType, Status status);
}