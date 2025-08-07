package com.service.other.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.other.entity.VehicleListing;
import com.service.other.util.Status;

public interface VehicleListingRepository extends JpaRepository<VehicleListing, Integer> {
	List<VehicleListing> findByStatus(Status status);

	List<VehicleListing> findByGarageId(Integer garageId);

	List<VehicleListing> findByVin(String vin);

	List<VehicleListing> findByMakeAndModelAndYear(String make, String model, int year);
}