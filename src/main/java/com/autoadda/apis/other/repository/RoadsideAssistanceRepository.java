package com.autoadda.apis.other.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.entity.RoadsideAssistance;

@Repository
public interface RoadsideAssistanceRepository extends JpaRepository<RoadsideAssistance, Integer> {
	List<RoadsideAssistance> findByVehicleOwnerId(Integer vehicleOwnerId);
}
