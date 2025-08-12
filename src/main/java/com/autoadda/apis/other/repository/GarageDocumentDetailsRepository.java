package com.autoadda.apis.other.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.entity.GarageDocumentDetails;

@Repository
public interface GarageDocumentDetailsRepository extends JpaRepository<GarageDocumentDetails, Integer> {

	List<GarageDocumentDetails> findByGarageId(Integer garageId);
}
