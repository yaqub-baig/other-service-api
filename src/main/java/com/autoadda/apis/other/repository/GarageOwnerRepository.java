package com.autoadda.apis.other.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.other.entity.GarageOwner;

@Repository
public interface GarageOwnerRepository extends JpaRepository<GarageOwner, Integer> {

//	List<GarageOwner> findByGarageId(Integer garageId);
}
