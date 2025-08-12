/**
 * 
 */
package com.autoadda.apis.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.entity.InsuranceDetails;

@Repository
public interface InsuranceDetailsRepository extends JpaRepository<InsuranceDetails, Integer> {}
