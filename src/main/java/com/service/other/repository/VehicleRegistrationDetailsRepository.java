/**
 * 
 */
package com.service.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.other.entity.VehicleRegistration;

@Repository
public interface VehicleRegistrationDetailsRepository extends JpaRepository<VehicleRegistration, Integer> {}
