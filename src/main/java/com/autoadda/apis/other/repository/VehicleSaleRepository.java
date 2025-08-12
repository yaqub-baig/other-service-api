package com.autoadda.apis.other.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.other.entity.VehicleSale;
import com.autoadda.apis.other.util.Status;

public interface VehicleSaleRepository extends JpaRepository<VehicleSale, Integer> {

	// Custom query methods can be added here if needed
	// For example, to find by vehicle listing ID or status
	List<VehicleSale> findByVehicleListingId(Integer vehicleListingId);

	List<VehicleSale> findByStatus(Status status);
	// Example: Find by vehicle ID

}