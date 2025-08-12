/**
 * 
 */
package com.autoadda.apis.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.entity.VehicleType;


@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Integer> {
} 
