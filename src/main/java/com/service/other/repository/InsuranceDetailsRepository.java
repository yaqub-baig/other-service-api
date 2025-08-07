/**
 * 
 */
package com.service.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.other.entity.InsuranceDetails;

@Repository
public interface InsuranceDetailsRepository extends JpaRepository<InsuranceDetails, Integer> {}
